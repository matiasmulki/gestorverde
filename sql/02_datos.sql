-- =====================================================================
--  GESTOR VERDE - Script 02: insercion de datos iniciales y de prueba
--  Escenario: campanias 2024-2026 y 2025-2027 de Vivero Citrus.
--  Las cantidades del lote L0003 reproducen el ejemplo de injertacion
--  documentado en el Trabajo Practico 1 (5.000 injertos, 350 no prendidos).
--  Clave de todos los usuarios de prueba: Vivero2026!
-- =====================================================================
USE gestorverde;
SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- Seguridad y personal
-- ---------------------------------------------------------------------
INSERT INTO rol (id_rol, nombre, descripcion) VALUES
 (1, 'ADMINISTRADOR',        'Gestiona usuarios, permisos y parámetros del sistema'),
 (2, 'ENCARGADO_PRODUCCION', 'Planifica campañas y registra operaciones productivas'),
 (3, 'OPERARIO',             'Registra operaciones de campo desde puestos fijos'),
 (4, 'RESPONSABLE_TECNICO',  'Acredita origen y sanidad ante el organismo de control'),
 (5, 'VENTAS',               'Gestiona clientes, pedidos, reservas y despachos'),
 (6, 'GERENCIA',             'Consulta reportes, indicadores y liquidaciones');

INSERT INTO empleado (id_empleado, legajo, apellido, nombre, modalidad, fecha_alta) VALUES
 (1, 'E-001', 'Pérez',    'Julián',  'PERMANENTE', '2012-03-01'),
 (2, 'E-014', 'Soria',    'Marcelo', 'PERMANENTE', '2014-08-11'),
 (3, 'E-022', 'Gómez',    'Ramón',   'TEMPORARIO', '2025-09-01'),
 (4, 'E-031', 'Juárez',   'Carla',   'TEMPORARIO', '2024-07-01'),
 (5, 'E-035', 'Medina',   'Hugo',    'TEMPORARIO', '2024-06-15'),
 (6, 'E-005', 'Ferreyra', 'Lucía',   'PERMANENTE', '2016-02-01');

INSERT INTO usuario (id_usuario, nombre_usuario, sal, hash_clave, id_rol, id_empleado) VALUES
 (1, 'admin',    'b74f6838cdb34e83260ece70ae59bfbe', '29f5fb9b4d0666dc2c8ebc7a9c66bc1cad1efaf0d83065fea09acfd2bc6e82b4', 1, NULL),
 (2, 'jperez',   'dff9a04fbedc95fde10ea633cfeeed44', '6412e3e697625d0f972a3f2f9240938024048f208d8bc1df56481965bc9e5ba3', 2, 1),
 (3, 'msoria',   '9c5cd58c6d98281279bc49049da074b3', '1c0137b4ce35b9777914a02d8bb077ef542037e1bb061a9bf0a310db9edf9980', 3, 2),
 (4, 'lferreyra','2c409c636021d6ef0aa33149e7ba4b76', '3e56f2afc26aeefded5a9ad95464953aaaddb355f421ebd615b07988d647e1c9', 4, 6),
 (5, 'ventas',   '37bcbd2fc143b6a71382cba644d24d19', 'c1a14a236b40964cc70e4e6f47ef0303156ad7503c0895d283a3030e1f90a539', 5, NULL),
 (6, 'gerencia', '6dc0f400d5a5c13207edd5e79f5b5e59', '58a4159b4ac3a27ef6c9d4ffbb4b8a70ec3faf44f180260792ea0c12c468df51', 6, NULL);

-- ---------------------------------------------------------------------
-- Estructura fisica (arbol parametrizable, incluye el "Modulo 10 y medio")
-- ---------------------------------------------------------------------
INSERT INTO tipo_ubicacion (id_tipo_ubicacion, nombre, nivel, admite_lotes) VALUES
 (1, 'Vivero',                 0, FALSE),
 (2, 'Invernadero',            1, FALSE),
 (3, 'Nave',                   1, FALSE),
 (4, 'Área de rustificación',  1, FALSE),
 (5, 'Sector',                 2, FALSE),
 (6, 'Cantero',                3, TRUE),
 (7, 'Cámara de germinación',  1, TRUE),
 (8, 'Bloque de incremento',   1, TRUE);

INSERT INTO ubicacion (id_ubicacion, codigo, nombre, id_tipo_ubicacion, id_ubicacion_padre, capacidad_maxima) VALUES
 ( 1, 'VC',         'Vivero Citrus',                    1, NULL,  NULL),
 ( 2, 'M10',        'Invernadero Módulo 10',            2, 1,     NULL),
 ( 3, 'M10-N',      'Sector Norte',                     5, 2,     NULL),
 ( 4, 'M10-N-C01',  'Cantero 1',                        6, 3,     5200),
 ( 5, 'M10-N-C05',  'Cantero 5',                        6, 3,     5200),
 ( 6, 'M10-N-C12',  'Cantero 12',                       6, 3,     5000),
 ( 7, 'M10-S',      'Sector Sur',                       5, 2,     NULL),
 ( 8, 'M10-S-C01',  'Cantero 1',                        6, 7,     5200),
 ( 9, 'M10M',       'Invernadero Módulo 10 y medio',    2, 1,     NULL),
 (10, 'M10M-U',     'Sector Único',                     5, 9,     NULL),
 (11, 'M10M-U-C01', 'Cantero 1',                        6, 10,    3000),
 (12, 'M02',        'Invernadero Módulo 2',             2, 1,     NULL),
 (13, 'M02-N',      'Sector Norte',                     5, 12,    NULL),
 (14, 'M02-N-C03',  'Cantero 3',                        6, 13,    4800),
 (15, 'NC1',        'Nave Cotigoy 1',                   3, 1,     NULL),
 (16, 'NC1-E',      'Sector Este',                      5, 15,    NULL),
 (17, 'NC1-E-C01',  'Cantero 1',                        6, 16,    6400),
 (18, 'NC1-E-C02',  'Cantero 2',                        6, 16,    6400),
 (19, 'NC1-O',      'Sector Oeste',                     5, 15,    NULL),
 (20, 'NC1-O-C01',  'Cantero 1',                        6, 19,    6400),
 (21, 'MS',         'Módulo Sombra',                    4, 1,     NULL),
 (22, 'MS-C01',     'Cantero 1',                        6, 21,    8000),
 (23, 'CG1',        'Cámara de germinación 1',          7, 1,     60000),
 (24, 'BI1',        'Bloque de incremento 1',           8, 1,     20000);

-- ---------------------------------------------------------------------
-- Catalogos controlados (eliminan el texto libre detectado en las planillas)
-- ---------------------------------------------------------------------
INSERT INTO especie (id_especie, nombre_comun, nombre_cientifico) VALUES
 (1, 'Limonero',            'Citrus limon'),
 (2, 'Naranjo dulce',       'Citrus sinensis'),
 (3, 'Mandarino Cleopatra', 'Citrus reshni'),
 (4, 'Lima de Volkamer',    'Citrus volkameriana'),
 (5, 'Citrumelo',           'Citrus paradisi x Poncirus trifoliata'),
 (6, 'Trifolio',            'Poncirus trifoliata');

INSERT INTO cultivar (id_cultivar, nombre, id_especie, uso) VALUES
 ( 1, 'Genova E.E.A.T.',   1, 'COPA'),
 ( 2, 'Lisbon',            1, 'COPA'),
 ( 3, 'Limoneira 8A',      1, 'COPA'),
 ( 4, 'Eureka',            1, 'COPA'),
 ( 5, 'Valencia E.E.L.T.', 2, 'COPA'),
 ( 6, 'Valencia Seedless', 2, 'COPA'),
 ( 7, 'Salustiana',        2, 'COPA'),
 ( 8, 'Westin',            2, 'COPA'),
 ( 9, 'Cleopatra',         3, 'PORTAINJERTO'),
 (10, 'Volkameriana',      4, 'PORTAINJERTO'),
 (11, 'Citrumelo Swingle', 5, 'PORTAINJERTO'),
 (12, 'Trifolia',          6, 'PORTAINJERTO');

INSERT INTO organismo_emisor (id_organismo, sigla, nombre) VALUES
 (1, 'INASE',  'Instituto Nacional de Semillas'),
 (2, 'EEAOC',  'Estación Experimental Agroindustrial Obispo Colombres'),
 (3, 'INTA',   'Instituto Nacional de Tecnología Agropecuaria'),
 (4, 'SENASA', 'Servicio Nacional de Sanidad y Calidad Agroalimentaria');

INSERT INTO material_certificado
 (id_material, tipo, id_cultivar, precinto, nro_certificado, id_organismo,
  fecha_cosecha, fecha_ingreso, fecha_vencimiento, cantidad_ingresada) VALUES
 (1, 'SEMILLA',  9, '154918', '44.13', 1, '2024-05-20', '2024-06-10', '2026-06-10', 6000),
 (2, 'YEMA',     1, '160233', '51.07', 2, NULL,         '2025-09-15', '2026-09-15', 5200),
 (3, 'SEMILLA', 10, '154930', '44.21', 1, '2024-05-28', '2024-06-12', '2026-06-12', 7000),
 (4, 'YEMA',     2, '160241', '51.12', 2, NULL,         '2025-06-01', '2025-08-31',  800),
 (5, 'YEMA',     5, '160257', '51.19', 3, NULL,         '2025-10-01', '2026-10-01', 4000),
 (6, 'SEMILLA', 11, '161002', '45.02', 1, '2025-05-18', '2025-06-15', '2027-06-15', 8000),
 (7, 'YEMA',     3, '162115', '52.04', 2, NULL,         '2026-08-20', '2027-08-20', 6000);

INSERT INTO campania (id_campania, nombre, fecha_inicio, fecha_fin) VALUES
 (1, '2024-2026', '2024-06-01', '2026-12-31'),
 (2, '2025-2027', '2025-06-01', '2027-12-31');

INSERT INTO estado_lote (id_estado, codigo, nombre, orden) VALUES
 ( 1, 'GERMINACION',     'Germinación',                     1),
 ( 2, 'PLANTIN',         'Plantín',                         2),
 ( 3, 'DESARROLLO_PIE',  'En desarrollo (portainjerto)',    3),
 ( 4, 'INJERTADO',       'Injertado',                       4),
 ( 5, 'DESARROLLO_INJ',  'En desarrollo (planta injertada)',5),
 ( 6, 'RUSTIFICACION',   'Rustificación',                   6),
 ( 7, 'LISTO_VENTA',     'Listo para venta',                7),
 ( 8, 'RESERVADO',       'Reservado',                       8),
 ( 9, 'VENDIDO',         'Vendido',                         9),
 (10, 'DESCARTADO',      'Descartado',                     10),
 (11, 'CERRADO',         'Cerrado (consumido)',            11),
 (12, 'DISPONIBLE',      'Yemas disponibles',              12);

INSERT INTO tipo_evento (id_tipo_evento, codigo, nombre, signo) VALUES
 ( 1, 'SIEMBRA',             'Siembra de semilla certificada',       1),
 ( 2, 'INGRESO_YEMAS',       'Ingreso de yemas certificadas',        1),
 ( 3, 'TRASPLANTE',          'Trasplante a maceta',                  0),
 ( 4, 'MOVIMIENTO',          'Movimiento de ubicación',              0),
 ( 5, 'DIVISION_SALIDA',     'Salida por división de lote',         -1),
 ( 6, 'DIVISION_ALTA',       'Alta por división de lote',            1),
 ( 7, 'CONSUMO_INJERTACION', 'Consumo por injertación',             -1),
 ( 8, 'INJERTACION',         'Injertación (alta de planta injertada)',1),
 ( 9, 'MERMA',               'Merma o pérdida',                     -1),
 (10, 'TRATAMIENTO',         'Tratamiento aplicado',                 0),
 (11, 'EGRESO_VENTA',        'Egreso por venta',                    -1),
 (12, 'SALDO_INICIAL',       'Saldo inicial migrado',                1),
 (13, 'REVERSION_ALTA',      'Asiento inverso de un alta',          -1),
 (14, 'REVERSION_BAJA',      'Asiento inverso de una baja',          1),
 (15, 'CAMBIO_ETAPA',        'Cambio de etapa del ciclo',            0);

INSERT INTO motivo_egreso (id_motivo, nombre, etapa) VALUES
 (1, 'No emergencia',        'Germinación'),
 (2, 'Fuera de tipo',        'Plantín'),
 (3, 'Injerto no prendido',  'Injertación'),
 (4, 'Sanitario',            'Desarrollo'),
 (5, 'Daño climático',      'Desarrollo');

-- ---------------------------------------------------------------------
-- Lotes (la existencia inicia en cero y la calculan los eventos via trigger)
-- ---------------------------------------------------------------------
INSERT INTO lote (id_lote, codigo, clase, id_campania, id_cultivar_portainjerto, id_cultivar_copa,
                  id_material_origen, id_ubicacion, id_estado, es_saldo_inicial, fecha_creacion) VALUES
 ( 1, 'L0001', 'PORTAINJERTO', 1,    9, NULL, 1,     5, 11, FALSE, '2024-07-01 08:30'),
 ( 2, 'L0002', 'YEMAS',        1, NULL,    1, 2,    24, 12, FALSE, '2025-09-15 11:00'),
 ( 3, 'L0003', 'INJERTADA',    1,    9,    1, NULL,  5,  5, FALSE, '2025-10-12 17:00'),
 ( 4, 'L0004', 'PORTAINJERTO', 1,   10, NULL, 3,    17, 11, FALSE, '2024-07-05 09:00'),
 ( 5, 'L0005', 'PORTAINJERTO', 1,   10, NULL, NULL, 18,  3, FALSE, '2025-03-18 08:00'),
 ( 6, 'L0006', 'YEMAS',        1, NULL,    2, 4,    24, 12, FALSE, '2025-06-01 11:00'),
 ( 7, 'L0007', 'YEMAS',        1, NULL,    5, 5,    24, 12, FALSE, '2025-10-01 11:00'),
 ( 8, 'L0008', 'INJERTADA',    1,   10,    5, NULL, 17,  5, FALSE, '2025-10-20 17:00'),
 ( 9, 'L0009', 'INJERTADA',    1,    9,    2, NULL, 14,  6, TRUE,  '2026-01-05 08:00'),
 (10, 'L0010', 'INJERTADA',    1,   11,    7, NULL, 22,  9, TRUE,  '2026-01-05 08:00'),
 (11, 'L0011', 'PORTAINJERTO', 2,   11, NULL, 6,    20,  3, FALSE, '2025-07-01 08:00'),
 (12, 'L0012', 'YEMAS',        2, NULL,    3, 7,    24, 12, FALSE, '2026-08-20 10:00'),
 (13, 'L0013', 'INJERTADA',    1,   12,    4, NULL,  4,  7, TRUE,  '2026-01-05 08:00'),
 (14, 'L0014', 'INJERTADA',    1,    9,    8, NULL, 11,  6, TRUE,  '2026-01-05 08:00');

-- ---------------------------------------------------------------------
-- Eventos (historia inmutable de cada lote)
-- ---------------------------------------------------------------------
INSERT INTO evento_lote
 (id_evento, id_lote, id_tipo_evento, fecha_hora, cantidad, id_ubicacion_origen, id_ubicacion_destino,
  id_empleado, id_motivo, id_evento_relacionado, id_usuario, observacion) VALUES
 ( 1,  1,  1, '2024-07-01 08:30', 6000, NULL, 23,   5, NULL, NULL, 2, 'Siembra en almácigo'),
 ( 2,  4,  1, '2024-07-05 09:00', 7000, NULL, 23,   5, NULL, NULL, 2, 'Siembra en almácigo'),
 ( 3,  1,  9, '2024-08-20 10:00',  600, 23,   NULL, NULL, 1, NULL, 2, NULL),
 ( 4,  4,  9, '2024-08-21 10:00',  800, 23,   NULL, NULL, 1, NULL, 2, NULL),
 ( 5,  1,  9, '2024-09-10 08:00',  400, 23,   NULL, NULL, 2, NULL, 2, 'Selección de plantines'),
 ( 6,  1,  3, '2024-09-10 09:00', 5000, 23,   5,    4, NULL, NULL, 2, 'Trasplante a maceta'),
 ( 7,  4,  3, '2024-09-12 09:00', 6200, 23,   17,   4, NULL, NULL, 2, 'Trasplante a maceta'),
 ( 8,  4,  5, '2025-03-18 08:00', 3000, 17,   18,   5, NULL, NULL, 2, 'División por densidad'),
 ( 9,  5,  6, '2025-03-18 08:00', 3000, 17,   18,   5, NULL, 8,    2, 'Lote derivado de L0004'),
 (10, 11,  1, '2025-07-01 08:00', 8000, NULL, 23,   5, NULL, NULL, 2, 'Siembra en almácigo'),
 (11,  2,  2, '2025-09-15 11:00', 5200, NULL, 24, NULL, NULL, NULL, 2, NULL),
 (12,  6,  2, '2025-06-01 11:00',  800, NULL, 24, NULL, NULL, NULL, 2, NULL),
 (13,  7,  2, '2025-10-01 11:00', 4000, NULL, 24, NULL, NULL, NULL, 2, NULL),
 (14, 11,  9, '2025-08-25 10:00',  900, 23,   NULL, NULL, 1, NULL, 2, NULL),
 (15,  3,  8, '2025-10-12 17:00', 5000, NULL, 5,    2, NULL, NULL, 2, 'Injertación en el lugar'),
 (16,  1,  7, '2025-10-12 17:00', 5000, 5,    NULL, 2, NULL, 15,   2, NULL),
 (17,  2,  7, '2025-10-12 17:00', 5000, 24,   NULL, 2, NULL, 15,   2, NULL),
 (18,  8,  8, '2025-10-20 17:00', 3200, NULL, 17,   3, NULL, NULL, 2, 'Injertación en el lugar'),
 (19,  4,  7, '2025-10-20 17:00', 3200, 17,   NULL, 3, NULL, 18,   2, NULL),
 (20,  7,  7, '2025-10-20 17:00', 3200, 24,   NULL, 3, NULL, 18,   2, NULL),
 (21, 11,  9, '2025-09-30 08:00', 1100, 23,   NULL, NULL, 2, NULL, 2, 'Selección de plantines'),
 (22, 11,  3, '2025-09-30 09:00', 6000, 23,   20,   4, NULL, NULL, 2, 'Trasplante a maceta'),
 (23,  3,  9, '2025-11-20 10:00',  350, 5,    NULL, NULL, 3, 15,   2, 'Control de prendimiento'),
 (24,  8,  9, '2025-11-27 10:00',  150, 17,   NULL, NULL, 3, 18,   2, 'Control de prendimiento'),
 (25,  8, 10, '2026-03-10 07:30', 3050, 17,   NULL, 5, NULL, NULL, 2, NULL),
 (26,  3, 10, '2026-03-10 09:00', 4650, 5,    NULL, 5, NULL, NULL, 2, NULL),
 (27,  9, 12, '2026-01-05 08:00', 2600, NULL, 14, NULL, NULL, NULL, 1, 'Migración planilla de stock 2025'),
 (28, 10, 12, '2026-01-05 08:00', 1800, NULL, 22, NULL, NULL, NULL, 1, 'Migración planilla de stock 2025'),
 (29, 13, 12, '2026-01-05 08:00', 5200, NULL, 4,  NULL, NULL, NULL, 1, 'Migración planilla de stock 2025'),
 (30, 14, 12, '2026-01-05 08:00', 2100, NULL, 11, NULL, NULL, NULL, 1, 'Migración planilla de stock 2025'),
 (31, 12,  2, '2026-08-20 10:00', 6000, NULL, 24, NULL, NULL, NULL, 2, NULL),
 (32, 10, 11, '2026-09-10 10:00', 1800, 22,   NULL, NULL, NULL, NULL, 5, 'Despacho pedido 2');

INSERT INTO lote_genealogia (id_lote, id_lote_origen, vinculo, id_evento) VALUES
 (3, 1, 'PORTAINJERTO', 15),
 (3, 2, 'YEMA',         15),
 (5, 4, 'DIVISION',      9),
 (8, 4, 'PORTAINJERTO', 18),
 (8, 7, 'YEMA',         18);

INSERT INTO tratamiento (id_evento, tipo, producto, principio_activo, dosis, unidad_dosis) VALUES
 (25, 'FITOSANITARIO', 'Oxicloruro de cobre 84 %', 'Oxicloruro de cobre', 3.00, 'g/L'),
 (26, 'FITOSANITARIO', 'Oxicloruro de cobre 84 %', 'Oxicloruro de cobre', 3.00, 'g/L');

-- ---------------------------------------------------------------------
-- Gestion comercial
-- ---------------------------------------------------------------------
INSERT INTO cliente (id_cliente, razon_social, cuit, localidad, provincia, telefono, email) VALUES
 (1, 'Finca Los Nogales S.A.',            '30-71234567-3', 'Famaillá',   'Tucumán', '381 400-1122', 'compras@losnogales.example'),
 (2, 'Cooperativa Citrícola del Sur Ltda.','30-70987654-1', 'Monteros',   'Tucumán', '3863 42-3344', 'admin@citricoladelsur.example'),
 (3, 'Agro Yungas S.R.L.',                '30-71555222-8', 'Tafí Viejo', 'Tucumán', '381 461-7788', NULL),
 (4, 'Citrus del Valle S.A.S.',           '30-71999888-0', 'Concepción', 'Tucumán', NULL,           NULL);

INSERT INTO pedido (id_pedido, id_cliente, fecha_pedido, fecha_compromiso, estado) VALUES
 (1, 1, '2026-02-10', '2026-11-30', 'RESERVADO'),
 (2, 2, '2026-03-02', '2026-09-15', 'DESPACHADO'),
 (3, 3, '2026-04-15', '2026-12-15', 'PENDIENTE'),
 (4, 1, '2026-09-01', '2027-03-31', 'PENDIENTE');

INSERT INTO pedido_item (id_item, id_pedido, id_cultivar_copa, id_cultivar_portainjerto, cantidad) VALUES
 (1, 1, 1,  9, 4000),
 (2, 2, 7, 11, 1800),
 (3, 3, 5, 10, 3000),
 (4, 3, 2,  9, 1500),
 (5, 4, 3, 10, 3000);

INSERT INTO reserva (id_reserva, id_item, id_lote, cantidad, fecha_hora, estado, id_usuario) VALUES
 (1, 1,  3, 4000, '2026-02-10 12:00', 'VIGENTE',    5),
 (2, 2, 10, 1800, '2026-03-02 12:30', 'DESPACHADA', 5);

INSERT INTO despacho (id_despacho, id_pedido, fecha, nro_dtv, id_usuario) VALUES
 (1, 2, '2026-09-10', 'DTVE-2026-0045872', 5);

INSERT INTO despacho_detalle (id_despacho, id_evento, id_reserva) VALUES
 (1, 32, 2);

-- ---------------------------------------------------------------------
-- Trabajo del personal (las quincenas se cierran despues de cargar su trabajo)
-- ---------------------------------------------------------------------
INSERT INTO tarea (id_tarea, nombre, unidad_medida, id_tipo_evento) VALUES
 (1, 'Injerto',              'injerto', 8),
 (2, 'Trasplante a maceta',  'planta',  3),
 (3, 'Llenado de macetas',   'maceta',  NULL),
 (4, 'Curación de cantero',  'cantero', 10),
 (5, 'Zarandeo de sustrato', 'm3',      NULL);

INSERT INTO tarifa_tarea (id_tarea, vigencia_desde, valor_unitario) VALUES
 (1, '2025-07-01',   95.00), (1, '2026-01-01',  120.00),
 (2, '2024-07-01',   18.00), (2, '2025-07-01',   24.00),
 (3, '2025-07-01',   30.00), (3, '2026-01-01',   38.00),
 (4, '2025-07-01', 5200.00), (4, '2026-01-01', 6400.00),
 (5, '2025-07-01', 6500.00), (5, '2026-01-01', 8000.00);

INSERT INTO quincena (id_quincena, anio, mes, numero, fecha_desde, fecha_hasta) VALUES
 (1, 2025, 10, 1, '2025-10-01', '2025-10-15'),
 (2, 2025, 10, 2, '2025-10-16', '2025-10-31'),
 (3, 2026,  9, 1, '2026-09-01', '2026-09-15'),
 (4, 2026,  9, 2, '2026-09-16', '2026-09-30');

INSERT INTO registro_trabajo (id_empleado, id_tarea, id_quincena, fecha, cantidad, valor_unitario, id_evento, id_usuario) VALUES
 (2, 1, 1, '2025-10-12', 5000,   95.00, 15,   2),
 (3, 1, 2, '2025-10-20', 3200,   95.00, 18,   2),
 (4, 3, 2, '2025-10-22', 1500,   30.00, NULL, 2),
 (5, 3, 2, '2025-10-23', 1300,   30.00, NULL, 2),
 (4, 3, 3, '2026-09-03', 1400,   38.00, NULL, 2),
 (5, 5, 3, '2026-09-04',    5, 8000.00, NULL, 2),
 (4, 3, 4, '2026-09-21', 1200,   38.00, NULL, 2),
 (5, 5, 4, '2026-09-22',    6, 8000.00, NULL, 2),
 (3, 3, 4, '2026-09-23',  900,   38.00, NULL, 2),
 (5, 4, 4, '2026-09-24',    3, 6400.00, NULL, 2);

UPDATE quincena SET liquidada = TRUE, fecha_liquidacion = '2025-10-17 18:00' WHERE id_quincena = 1;
UPDATE quincena SET liquidada = TRUE, fecha_liquidacion = '2025-11-03 18:00' WHERE id_quincena = 2;
UPDATE quincena SET liquidada = TRUE, fecha_liquidacion = '2026-09-17 18:00' WHERE id_quincena = 3;

-- ---------------------------------------------------------------------
-- Auditoria e intentos rechazados de ejemplo
-- ---------------------------------------------------------------------
INSERT INTO auditoria (id_usuario, fecha_hora, accion, entidad, id_entidad, detalle) VALUES
 (1, '2026-01-05 07:55:00.000', 'LOGIN',        'usuario',     1,  'Inicio de sesión'),
 (1, '2026-01-05 08:00:00.000', 'ALTA',         'lote',        9,  'Saldo inicial migrado: 2600 plantas en M02-N-C03'),
 (2, '2025-10-12 17:05:00.000', 'ALTA',         'evento_lote', 15, 'Injertación L0001 + L0002 -> L0003 (5000)'),
 (5, '2026-09-10 10:02:00.000', 'ALTA',         'despacho',    1,  'Despacho pedido 2 - DTVE-2026-0045872'),
 (4, '2026-09-18 09:40:00.000', 'CONSULTA',     'lote',        3,  'Consulta de trazabilidad de L0003');

INSERT INTO intento_rechazado (id_usuario, fecha_hora, operacion, regla, id_lote, id_ubicacion, cantidad_pretendida, motivo) VALUES
 (2, '2025-10-25 16:10:00.000', 'INJERTACION', 'RN-03', 6, NULL, 800,
     'Certificado 51.12 del lote de yemas L0006 vencido el 2025-08-31'),
 (3, '2026-02-02 08:20:00.000', 'MOVIMIENTO',  'RN-05', 5, 4, 3000,
     'Capacidad insuficiente en M10-N-C01: disponible 0, pretendido 3000');
