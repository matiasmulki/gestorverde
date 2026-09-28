-- =====================================================================
--  GESTOR VERDE - Script 03: consultas SQL del sistema
--  Cada consulta indica el requerimiento funcional (RF) que resuelve.
-- =====================================================================
USE gestorverde;
SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- Q-01. Estructura del vivero con capacidad y ocupacion acumulada por nivel
--       (RF-01, RF-11). Recorre el arbol con CTE recursivos: el primero arma
--       la ruta de cada nodo; el segundo, la clausura ancestro-descendiente.
-- ---------------------------------------------------------------------
WITH RECURSIVE arbol AS (
    SELECT id_ubicacion, codigo, nombre, 0 AS nivel,
           CAST(codigo AS CHAR(200)) AS ruta
      FROM ubicacion
     WHERE id_ubicacion_padre IS NULL
    UNION ALL
    SELECT u.id_ubicacion, u.codigo, u.nombre, a.nivel + 1,
           CONCAT(a.ruta, ' > ', u.codigo)
      FROM ubicacion u
      JOIN arbol a ON u.id_ubicacion_padre = a.id_ubicacion
),
clausura AS (
    SELECT id_ubicacion AS ancestro, id_ubicacion AS descendiente FROM ubicacion
    UNION ALL
    SELECT c.ancestro, u.id_ubicacion
      FROM clausura c
      JOIN ubicacion u ON u.id_ubicacion_padre = c.descendiente
)
SELECT CONCAT(REPEAT('    ', a.nivel), a.nombre)   AS ubicacion,
       a.codigo,
       SUM(d.capacidad_maxima)                      AS capacidad,
       COALESCE(SUM(o.ocupacion), 0)                AS ocupacion,
       ROUND(100 * COALESCE(SUM(o.ocupacion), 0) / SUM(d.capacidad_maxima), 1) AS pct
  FROM arbol a
  JOIN clausura c             ON c.ancestro = a.id_ubicacion
  JOIN ubicacion d            ON d.id_ubicacion = c.descendiente
  LEFT JOIN v_ocupacion_ubicacion o ON o.id_ubicacion = d.id_ubicacion
 WHERE a.nivel <= 2
 GROUP BY a.id_ubicacion, a.nombre, a.codigo, a.nivel, a.ruta
 ORDER BY a.ruta;

-- ---------------------------------------------------------------------
-- Q-02. Mapa de ocupacion: estado de cada ubicacion receptora (RF-11, RNF-03)
-- ---------------------------------------------------------------------
SELECT o.codigo,
       o.capacidad_maxima AS capacidad,
       o.ocupacion,
       o.disponible,
       o.porcentaje       AS pct,
       CASE
         WHEN o.ocupacion = 0                 THEN 'VACIA'
         WHEN o.ocupacion >= o.capacidad_maxima THEN 'COMPLETA'
         WHEN o.porcentaje >= 85              THEN 'ALTA'
         ELSE 'NORMAL'
       END AS estado
  FROM v_ocupacion_ubicacion o
 ORDER BY o.porcentaje DESC, o.codigo;

-- ---------------------------------------------------------------------
-- Q-03. Existencias por combinacion copa / portainjerto y estado (RF-21)
-- ---------------------------------------------------------------------
SELECT COALESCE(copa, '(sin injertar)')        AS copa,
       COALESCE(portainjerto, '(yemas)')       AS portainjerto,
       estado,
       COUNT(*)                                AS lotes,
       SUM(cantidad_actual)                    AS existencia
  FROM v_existencia_lote
 WHERE cantidad_actual > 0
 GROUP BY copa, portainjerto, estado
 ORDER BY existencia DESC;

-- ---------------------------------------------------------------------
-- Q-04. Trazabilidad hacia atras de un lote hasta el material de origen (RF-09)
--       Recorre lote_genealogia de forma recursiva y agrega el certificado.
-- ---------------------------------------------------------------------
WITH RECURSIVE ancestros AS (
    SELECT l.id_lote, 0 AS nivel, CAST('LOTE CONSULTADO' AS CHAR(20)) AS vinculo
      FROM lote l
     WHERE l.codigo = 'L0003'
    UNION ALL
    SELECT g.id_lote_origen, a.nivel + 1, CAST(g.vinculo AS CHAR(20))
      FROM ancestros a
      JOIN lote_genealogia g ON g.id_lote = a.id_lote
)
SELECT a.nivel,
       a.vinculo,
       l.codigo                                   AS lote,
       CONCAT_WS(' / ', cp.nombre, pi.nombre)      AS cultivar,
       m.tipo                                     AS material,
       m.precinto,
       m.nro_certificado                          AS certificado,
       org.sigla                                  AS emisor,
       m.fecha_vencimiento                        AS vence
  FROM ancestros a
  JOIN lote l                  ON l.id_lote = a.id_lote
  LEFT JOIN cultivar cp        ON cp.id_cultivar = l.id_cultivar_copa
  LEFT JOIN cultivar pi        ON pi.id_cultivar = l.id_cultivar_portainjerto
  LEFT JOIN material_certificado m ON m.id_material = l.id_material_origen
  LEFT JOIN organismo_emisor org   ON org.id_organismo = m.id_organismo
 ORDER BY a.nivel, lote;

-- ---------------------------------------------------------------------
-- Q-05. Linea de tiempo del lote y de sus antecesores (RF-09, CU-07)
-- ---------------------------------------------------------------------
WITH RECURSIVE ancestros AS (
    SELECT id_lote FROM lote WHERE codigo = 'L0003'
    UNION ALL
    SELECT g.id_lote_origen FROM ancestros a JOIN lote_genealogia g ON g.id_lote = a.id_lote
)
SELECT DATE_FORMAT(e.fecha_hora, '%d/%m/%Y') AS fecha,
       l.codigo                               AS lote,
       t.nombre                               AS evento,
       t.signo * e.cantidad                   AS efecto,
       COALESCE(ud.codigo, uo.codigo)         AS ubicacion,
       COALESCE(CONCAT(emp.apellido, ', ', emp.nombre), '-') AS operario,
       COALESCE(mo.nombre, '-')               AS motivo
  FROM evento_lote e
  JOIN ancestros a        ON a.id_lote = e.id_lote
  JOIN lote l             ON l.id_lote = e.id_lote
  JOIN tipo_evento t      ON t.id_tipo_evento = e.id_tipo_evento
  LEFT JOIN ubicacion uo  ON uo.id_ubicacion = e.id_ubicacion_origen
  LEFT JOIN ubicacion ud  ON ud.id_ubicacion = e.id_ubicacion_destino
  LEFT JOIN empleado emp  ON emp.id_empleado = e.id_empleado
  LEFT JOIN motivo_egreso mo ON mo.id_motivo = e.id_motivo
 ORDER BY e.fecha_hora, e.id_evento;

-- ---------------------------------------------------------------------
-- Q-06. Trazabilidad hacia adelante: desde un precinto certificado hasta
--       todos los lotes derivados y su destino comercial (RF-10)
-- ---------------------------------------------------------------------
WITH RECURSIVE descendientes AS (
    SELECT l.id_lote, 0 AS generacion
      FROM lote l
      JOIN material_certificado m ON m.id_material = l.id_material_origen
     WHERE m.precinto = '154930'
    UNION ALL
    SELECT g.id_lote, d.generacion + 1
      FROM descendientes d
      JOIN lote_genealogia g ON g.id_lote_origen = d.id_lote
)
SELECT d.generacion,
       v.codigo  AS lote,
       v.clase,
       CONCAT_WS(' / ', v.copa, v.portainjerto) AS combinacion,
       v.ubicacion,
       v.estado,
       v.cantidad_actual AS existencia,
       COALESCE(GROUP_CONCAT(DISTINCT c.razon_social SEPARATOR '; '), '-') AS comprometido_con
  FROM descendientes d
  JOIN v_existencia_lote v ON v.id_lote = d.id_lote
  LEFT JOIN reserva r      ON r.id_lote = d.id_lote AND r.estado <> 'LIBERADA'
  LEFT JOIN pedido_item pi ON pi.id_item = r.id_item
  LEFT JOIN pedido p       ON p.id_pedido = pi.id_pedido
  LEFT JOIN cliente c      ON c.id_cliente = p.id_cliente
 GROUP BY d.generacion, v.codigo, v.clase, v.copa, v.portainjerto, v.ubicacion, v.estado, v.cantidad_actual
 ORDER BY d.generacion, v.codigo;

-- ---------------------------------------------------------------------
-- Q-07. Rendimiento de la injertacion por injertador (RF-22)
--       Injertos realizados vs. mermas "injerto no prendido" vinculadas.
-- ---------------------------------------------------------------------
SELECT CONCAT(emp.apellido, ', ', emp.nombre)         AS injertador,
       COUNT(DISTINCT inj.id_evento)                  AS operaciones,
       SUM(inj.cantidad)                              AS injertados,
       COALESCE(SUM(mer.no_prendidos), 0)             AS no_prendidos,
       ROUND(100 * (SUM(inj.cantidad) - COALESCE(SUM(mer.no_prendidos), 0))
                 / SUM(inj.cantidad), 1)              AS pct_prendimiento
  FROM evento_lote inj
  JOIN tipo_evento t ON t.id_tipo_evento = inj.id_tipo_evento AND t.codigo = 'INJERTACION'
  JOIN empleado emp  ON emp.id_empleado = inj.id_empleado
  LEFT JOIN (
        SELECT m.id_evento_relacionado, SUM(m.cantidad) AS no_prendidos
          FROM evento_lote m
          JOIN motivo_egreso mo ON mo.id_motivo = m.id_motivo AND mo.nombre = 'Injerto no prendido'
         GROUP BY m.id_evento_relacionado
       ) mer ON mer.id_evento_relacionado = inj.id_evento
 GROUP BY emp.id_empleado, emp.apellido, emp.nombre
 ORDER BY pct_prendimiento DESC;

-- ---------------------------------------------------------------------
-- Q-08. Perdidas por etapa y motivo en cada campania (RF-22)
-- ---------------------------------------------------------------------
SELECT c.nombre       AS campania,
       mo.etapa,
       mo.nombre      AS motivo,
       COUNT(*)       AS eventos,
       SUM(e.cantidad) AS plantas_perdidas
  FROM evento_lote e
  JOIN tipo_evento t    ON t.id_tipo_evento = e.id_tipo_evento AND t.codigo = 'MERMA'
  JOIN motivo_egreso mo ON mo.id_motivo = e.id_motivo
  JOIN lote l           ON l.id_lote = e.id_lote
  JOIN campania c       ON c.id_campania = l.id_campania
 WHERE NOT EXISTS (SELECT 1 FROM evento_lote r WHERE r.id_evento_anulado = e.id_evento)
 GROUP BY c.nombre, mo.etapa, mo.nombre
 ORDER BY c.nombre, plantas_perdidas DESC;

-- ---------------------------------------------------------------------
-- Q-09. Produccion comprometida vs. existencia disponible (RF-16, RF-18)
--       Una diferencia negativa dispara la alerta de sobrecompromiso.
-- ---------------------------------------------------------------------
WITH pendiente AS (
    SELECT pi.id_cultivar_copa, pi.id_cultivar_portainjerto,
           SUM(pi.cantidad - COALESCE(r.reservado, 0)) AS sin_reservar,
           MIN(p.fecha_compromiso)                     AS primer_compromiso
      FROM pedido_item pi
      JOIN pedido p ON p.id_pedido = pi.id_pedido
                   AND p.estado IN ('PENDIENTE','RESERVADO_PARCIAL')
      LEFT JOIN (SELECT id_item, SUM(cantidad) AS reservado
                   FROM reserva WHERE estado = 'VIGENTE' GROUP BY id_item) r
             ON r.id_item = pi.id_item
     GROUP BY pi.id_cultivar_copa, pi.id_cultivar_portainjerto
),
libre AS (
    SELECT l.id_cultivar_copa, l.id_cultivar_portainjerto,
           SUM(l.cantidad_actual - COALESCE(r.reservado, 0)) AS disponible
      FROM lote l
      LEFT JOIN (SELECT id_lote, SUM(cantidad) AS reservado
                   FROM reserva WHERE estado = 'VIGENTE' GROUP BY id_lote) r
             ON r.id_lote = l.id_lote
     WHERE l.clase = 'INJERTADA' AND l.cantidad_actual > 0
     GROUP BY l.id_cultivar_copa, l.id_cultivar_portainjerto
)
SELECT cp.nombre AS copa,
       pi.nombre AS portainjerto,
       p.primer_compromiso,
       p.sin_reservar                    AS comprometido,
       COALESCE(l.disponible, 0)         AS disponible,
       COALESCE(l.disponible, 0) - p.sin_reservar AS diferencia,
       IF(COALESCE(l.disponible, 0) < p.sin_reservar, 'ALERTA', 'OK') AS situacion
  FROM pendiente p
  JOIN cultivar cp ON cp.id_cultivar = p.id_cultivar_copa
  JOIN cultivar pi ON pi.id_cultivar = p.id_cultivar_portainjerto
  LEFT JOIN libre l ON l.id_cultivar_copa = p.id_cultivar_copa
                   AND l.id_cultivar_portainjerto = p.id_cultivar_portainjerto
 ORDER BY diferencia;

-- ---------------------------------------------------------------------
-- Q-10. Liquidacion quincenal por operario y tarea (RF-20)
-- ---------------------------------------------------------------------
SELECT IF(GROUPING(x.operario), 'TOTAL QUINCENA', x.operario)          AS operario,
       IF(GROUPING(x.tarea), IF(GROUPING(x.operario), '', 'Subtotal'), x.tarea) AS tarea,
       IF(GROUPING(x.tarea), NULL, SUM(x.cantidad))                    AS cantidad,
       IF(GROUPING(x.tarea), NULL, MAX(x.unidad_medida))               AS unidad,
       IF(GROUPING(x.tarea), NULL, MAX(x.valor_unitario))              AS valor_unitario,
       SUM(x.importe)                                                  AS importe
  FROM (SELECT CONCAT(emp.legajo, ' ', emp.apellido, ', ', emp.nombre) AS operario,
               t.nombre AS tarea, t.unidad_medida, r.cantidad, r.valor_unitario, r.importe
          FROM registro_trabajo r
          JOIN quincena q   ON q.id_quincena = r.id_quincena
          JOIN empleado emp ON emp.id_empleado = r.id_empleado
          JOIN tarea t      ON t.id_tarea = r.id_tarea
         WHERE q.anio = 2026 AND q.mes = 9 AND q.numero = 2) x
 GROUP BY x.operario, x.tarea WITH ROLLUP;

-- ---------------------------------------------------------------------
-- Q-11. Control de integridad: saldo del lote vs. suma de sus eventos
--       (debe devolver cero filas; se ejecuta en cada respaldo diario)
-- ---------------------------------------------------------------------
SELECT l.codigo,
       l.cantidad_actual               AS saldo_registrado,
       SUM(t.signo * e.cantidad)       AS saldo_por_eventos
  FROM lote l
  JOIN evento_lote e ON e.id_lote = l.id_lote
  JOIN tipo_evento t ON t.id_tipo_evento = e.id_tipo_evento
 GROUP BY l.id_lote, l.codigo, l.cantidad_actual
HAVING saldo_registrado <> saldo_por_eventos;

-- ---------------------------------------------------------------------
-- Q-12. Yemas con certificado vencido o por vencer en 30 dias (RN-03)
-- ---------------------------------------------------------------------
SELECT m.precinto,
       m.nro_certificado                  AS certificado,
       cu.nombre                          AS cultivar,
       l.codigo                           AS lote,
       l.cantidad_actual                  AS existencia,
       m.fecha_vencimiento                AS vence,
       DATEDIFF(m.fecha_vencimiento, CURDATE())                  AS dias,
       IF(m.fecha_vencimiento < CURDATE(), 'VENCIDO', 'POR VENCER') AS situacion
  FROM material_certificado m
  JOIN cultivar cu ON cu.id_cultivar = m.id_cultivar
  JOIN lote l      ON l.id_material_origen = m.id_material
                  AND l.clase = 'YEMAS' AND l.cantidad_actual > 0
 WHERE m.fecha_vencimiento < DATE_ADD(CURDATE(), INTERVAL 30 DAY)
 ORDER BY m.fecha_vencimiento;

-- ---------------------------------------------------------------------
-- Q-13. Actividad y rechazos por usuario (RF-02, RF-25)
-- ---------------------------------------------------------------------
SELECT u.nombre_usuario                      AS usuario,
       r.nombre                              AS rol,
       IF(u.activo, 'Activo', 'Baja logica') AS situacion,
       (SELECT COUNT(*) FROM auditoria a WHERE a.id_usuario = u.id_usuario)          AS acciones,
       (SELECT COUNT(*) FROM evento_lote e WHERE e.id_usuario = u.id_usuario)        AS eventos,
       (SELECT COUNT(*) FROM intento_rechazado i WHERE i.id_usuario = u.id_usuario)  AS rechazos
  FROM usuario u
  JOIN rol r ON r.id_rol = u.id_rol
 ORDER BY eventos DESC, usuario;
