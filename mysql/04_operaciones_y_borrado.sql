-- =====================================================================
--  GESTOR VERDE - Script 04: insercion transaccional, anulacion y borrado
--  Ejecutar DESPUES de 01 y 02. Algunas sentencias fallan A PROPOSITO para
--  demostrar las restricciones de integridad; ejecutar con:
--      mysql -u root -p --force < 04_operaciones_y_borrado.sql
-- =====================================================================
USE gestorverde;
SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- A. INSERCION SIMPLE: alta de un cliente y de su pedido
-- ---------------------------------------------------------------------
INSERT INTO cliente (razon_social, cuit, localidad, provincia, telefono)
VALUES ('Huerta Grande S.R.L.', '30-71444333-5', 'Lules', 'Tucumán', '381 481-2020');

INSERT INTO pedido (id_cliente, fecha_pedido, fecha_compromiso)
VALUES (LAST_INSERT_ID(), '2026-09-28', '2027-05-15');

INSERT INTO pedido_item (id_pedido, id_cultivar_copa, id_cultivar_portainjerto, cantidad)
VALUES (LAST_INSERT_ID(), 1, 9, 2500);

-- ---------------------------------------------------------------------
-- B. INSERCION TRANSACCIONAL: registrar una injertacion (CU-03)
--    Portainjerto L0005 (Volkameriana) + yemas L0012 (Limoneira 8A).
--    Todas las escrituras forman una unica transaccion: se confirman juntas o ninguna.
-- ---------------------------------------------------------------------
-- Usuario 2 = jperez (encargado de produccion); empleado 2 = Soria, Marcelo (injertador)
SET @usuario  = 2;
SET @operario = 2;
SET @fecha    = '2026-09-28 16:30:00';
SET @cantidad = 3000;

START TRANSACTION;

-- Bloqueo pesimista de los lotes involucrados para evitar cargas concurrentes
SELECT id_lote, id_ubicacion, id_cultivar_portainjerto
  INTO @pie, @ubicacion, @cv_pie
  FROM lote WHERE codigo = 'L0005' FOR UPDATE;

SELECT id_lote, id_cultivar_copa
  INTO @yemas, @cv_copa
  FROM lote WHERE codigo = 'L0012' FOR UPDATE;

INSERT INTO lote (codigo, clase, id_campania, id_cultivar_portainjerto, id_cultivar_copa,
                  id_ubicacion, id_estado, fecha_creacion)
VALUES ('PENDIENTE', 'INJERTADA', 2, @cv_pie, @cv_copa, @ubicacion, 4, @fecha);
SET @nuevo = LAST_INSERT_ID();
UPDATE lote SET codigo = CONCAT('L', LPAD(@nuevo, 4, '0')) WHERE id_lote = @nuevo;

INSERT INTO evento_lote (id_lote, id_tipo_evento, fecha_hora, cantidad, id_ubicacion_destino,
                         id_empleado, id_usuario, observacion)
VALUES (@nuevo, 8, @fecha, @cantidad, @ubicacion, @operario, @usuario, 'Injertación en el lugar');
SET @evento = LAST_INSERT_ID();

INSERT INTO evento_lote (id_lote, id_tipo_evento, fecha_hora, cantidad, id_ubicacion_origen,
                         id_empleado, id_evento_relacionado, id_usuario)
VALUES (@pie,   7, @fecha, @cantidad, @ubicacion, @operario, @evento, @usuario),
       (@yemas, 7, @fecha, @cantidad, 24,         @operario, @evento, @usuario);

INSERT INTO lote_genealogia (id_lote, id_lote_origen, vinculo, id_evento)
VALUES (@nuevo, @pie, 'PORTAINJERTO', @evento),
       (@nuevo, @yemas, 'YEMA', @evento);

UPDATE lote SET id_estado = 11 WHERE id_lote = @pie AND cantidad_actual = 0;

-- El mismo evento alimenta la liquidacion del injertador (sin doble carga)
INSERT INTO registro_trabajo (id_empleado, id_tarea, id_quincena, fecha, cantidad,
                              valor_unitario, id_evento, id_usuario)
VALUES (@operario, 1, 4, DATE(@fecha), @cantidad, 120.00, @evento, @usuario);

INSERT INTO auditoria (id_usuario, accion, entidad, id_entidad, detalle)
VALUES (@usuario, 'ALTA', 'evento_lote', @evento,
        CONCAT('Injertación L0005 + L0012 -> L', LPAD(@nuevo, 4, '0'), ' (', @cantidad, ')'));

COMMIT;

SELECT codigo, clase, copa, portainjerto, ubicacion, estado, cantidad_actual
  FROM v_existencia_lote
 WHERE codigo IN ('L0005', 'L0012', CONCAT('L', LPAD(@nuevo, 4, '0')))
 ORDER BY codigo;

-- ---------------------------------------------------------------------
-- C. RESERVA contra el pedido 4 (CU-11): resuelve la alerta de la consulta Q-09
-- ---------------------------------------------------------------------
INSERT INTO reserva (id_item, id_lote, cantidad, fecha_hora, id_usuario)
VALUES (5, @nuevo, 3000, '2026-09-28 17:00:00', 5);
UPDATE pedido SET estado = 'RESERVADO' WHERE id_pedido = 4;

-- ---------------------------------------------------------------------
-- D. RECHAZO POR INTEGRIDAD: una merma mayor que la existencia (RN-01)
--    El trigger intenta dejar el saldo en -300 y el CHECK lo impide.
-- ---------------------------------------------------------------------
INSERT INTO evento_lote (id_lote, id_tipo_evento, fecha_hora, cantidad, id_motivo, id_usuario)
VALUES (2, 9, '2026-09-28 17:10:00', 500, 4, 2);

-- ---------------------------------------------------------------------
-- E. ANULACION POR ASIENTO INVERSO (RF-26): una merma cargada por error
-- ---------------------------------------------------------------------
INSERT INTO evento_lote (id_lote, id_tipo_evento, fecha_hora, cantidad, id_ubicacion_origen,
                         id_motivo, id_usuario, observacion)
VALUES (8, 9, '2026-09-28 17:20:00', 50, 17, 4, 3, 'Carga errónea');
SET @erroneo = LAST_INSERT_ID();

INSERT INTO evento_lote (id_lote, id_tipo_evento, fecha_hora, cantidad, id_ubicacion_destino,
                         id_evento_anulado, id_usuario, observacion)
VALUES (8, 14, '2026-09-28 17:25:00', 50, 17, @erroneo, 2, 'Anula merma cargada por error');

INSERT INTO auditoria (id_usuario, accion, entidad, id_entidad, detalle)
VALUES (2, 'ANULACION', 'evento_lote', @erroneo, 'Asiento inverso de merma errónea (50)');

SELECT e.id_evento, t.codigo AS tipo, e.cantidad, t.signo * e.cantidad AS efecto,
       e.id_evento_anulado, l.cantidad_actual AS saldo_actual
  FROM evento_lote e
  JOIN tipo_evento t ON t.id_tipo_evento = e.id_tipo_evento
  JOIN lote l        ON l.id_lote = e.id_lote
 WHERE e.id_lote = 8
 ORDER BY e.id_evento;

-- ---------------------------------------------------------------------
-- F. RN-08: no se admite trabajo en una quincena ya liquidada
-- ---------------------------------------------------------------------
INSERT INTO registro_trabajo (id_empleado, id_tarea, id_quincena, fecha, cantidad, valor_unitario, id_usuario)
VALUES (4, 3, 3, '2026-09-10', 300, 38.00, 2);

-- ---------------------------------------------------------------------
-- G. BORRADO DE REGISTROS
-- ---------------------------------------------------------------------
-- G1. Cliente sin operaciones: el borrado fisico esta permitido
DELETE FROM cliente WHERE cuit = '30-71999888-0';

-- G2. Cliente con pedidos: la clave foranea (RESTRICT) lo impide
DELETE FROM cliente WHERE id_cliente = 1;

-- G3. Pedido cancelado sin reservas: se elimina y sus items caen en cascada
SELECT COUNT(*) AS items_antes FROM pedido_item WHERE id_pedido = 3;
UPDATE pedido SET estado = 'CANCELADO' WHERE id_pedido = 3;
DELETE FROM pedido WHERE id_pedido = 3;
SELECT COUNT(*) AS items_despues FROM pedido_item WHERE id_pedido = 3;

-- G4. Material certificado con lotes asociados: no puede eliminarse
DELETE FROM material_certificado WHERE precinto = '154918';

-- G5. Evento de trazabilidad: el trigger lo impide (RN-02, RNF-08)
DELETE FROM evento_lote WHERE id_evento = 23;

-- G6. Registro de auditoria: tampoco puede eliminarse (RF-25)
DELETE FROM auditoria WHERE id_auditoria = 1;

-- G7. Baja logica de usuario: su historia se conserva (RF-25)
UPDATE usuario SET activo = FALSE WHERE nombre_usuario = 'msoria';
INSERT INTO auditoria (id_usuario, accion, entidad, id_entidad, detalle)
VALUES (1, 'BAJA_LOGICA', 'usuario', 3, 'Baja del usuario msoria');

SELECT u.nombre_usuario, u.activo,
       (SELECT COUNT(*) FROM intento_rechazado i WHERE i.id_usuario = u.id_usuario) AS rechazos_conservados,
       (SELECT COUNT(*) FROM evento_lote e WHERE e.id_usuario = u.id_usuario)       AS eventos_conservados
  FROM usuario u
 WHERE u.nombre_usuario = 'msoria';
