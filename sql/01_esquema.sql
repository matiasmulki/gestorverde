-- =====================================================================
--  GESTOR VERDE - Sistema de trazabilidad para viveros citricolas
--  Script 01: creacion del esquema, tablas, indices, vistas y triggers
--  Motor: MySQL 8.0 o superior | Codificacion: utf8mb4
--  Autor: Matias Fernando Mulki - Seminario de Practica (Univ. Siglo 21)
-- =====================================================================

SET NAMES utf8mb4;
DROP DATABASE IF EXISTS gestorverde;
CREATE DATABASE gestorverde
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE gestorverde;

-- ---------------------------------------------------------------------
-- 1. SEGURIDAD Y PERSONAL
-- ---------------------------------------------------------------------
CREATE TABLE rol (
  id_rol        TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre        VARCHAR(40)  NOT NULL,
  descripcion   VARCHAR(150) NULL,
  CONSTRAINT pk_rol PRIMARY KEY (id_rol),
  CONSTRAINT uq_rol_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

CREATE TABLE empleado (
  id_empleado   INT UNSIGNED NOT NULL AUTO_INCREMENT,
  legajo        VARCHAR(10)  NOT NULL,
  apellido      VARCHAR(60)  NOT NULL,
  nombre        VARCHAR(60)  NOT NULL,
  modalidad     ENUM('PERMANENTE','TEMPORARIO') NOT NULL,
  fecha_alta    DATE         NOT NULL,
  activo        BOOLEAN      NOT NULL DEFAULT TRUE,
  CONSTRAINT pk_empleado PRIMARY KEY (id_empleado),
  CONSTRAINT uq_empleado_legajo UNIQUE (legajo)
) ENGINE = InnoDB;

CREATE TABLE usuario (
  id_usuario     INT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre_usuario VARCHAR(30)  NOT NULL,
  hash_clave     CHAR(64)     NOT NULL COMMENT 'PBKDF2-HMAC-SHA256 en hexadecimal',
  sal            CHAR(32)     NOT NULL COMMENT 'Sal aleatoria de 16 bytes en hexadecimal',
  id_rol         TINYINT UNSIGNED NOT NULL,
  id_empleado    INT UNSIGNED NULL,
  activo         BOOLEAN      NOT NULL DEFAULT TRUE,
  fecha_alta     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ultimo_acceso  DATETIME     NULL,
  CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
  CONSTRAINT uq_usuario_nombre UNIQUE (nombre_usuario),
  CONSTRAINT uq_usuario_empleado UNIQUE (id_empleado),
  CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol (id_rol),
  CONSTRAINT fk_usuario_empleado FOREIGN KEY (id_empleado) REFERENCES empleado (id_empleado)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- 2. ESTRUCTURA FISICA DEL VIVERO (jerarquia recursiva parametrizable)
-- ---------------------------------------------------------------------
CREATE TABLE tipo_ubicacion (
  id_tipo_ubicacion TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre            VARCHAR(40) NOT NULL,
  nivel             TINYINT UNSIGNED NOT NULL,
  admite_lotes      BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT pk_tipo_ubicacion PRIMARY KEY (id_tipo_ubicacion),
  CONSTRAINT uq_tipo_ubicacion_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

CREATE TABLE ubicacion (
  id_ubicacion       INT UNSIGNED NOT NULL AUTO_INCREMENT,
  codigo             VARCHAR(20) NOT NULL,
  nombre             VARCHAR(60) NOT NULL,
  id_tipo_ubicacion  TINYINT UNSIGNED NOT NULL,
  id_ubicacion_padre INT UNSIGNED NULL,
  capacidad_maxima   INT NULL,
  activa             BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT pk_ubicacion PRIMARY KEY (id_ubicacion),
  CONSTRAINT uq_ubicacion_codigo UNIQUE (codigo),
  CONSTRAINT fk_ubicacion_tipo FOREIGN KEY (id_tipo_ubicacion)
      REFERENCES tipo_ubicacion (id_tipo_ubicacion),
  CONSTRAINT fk_ubicacion_padre FOREIGN KEY (id_ubicacion_padre)
      REFERENCES ubicacion (id_ubicacion),
  CONSTRAINT ck_ubicacion_capacidad CHECK (capacidad_maxima IS NULL OR capacidad_maxima > 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- 3. CATALOGOS CONTROLADOS
-- ---------------------------------------------------------------------
CREATE TABLE especie (
  id_especie        SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre_comun      VARCHAR(50) NOT NULL,
  nombre_cientifico VARCHAR(60) NOT NULL,
  CONSTRAINT pk_especie PRIMARY KEY (id_especie),
  CONSTRAINT uq_especie_cientifico UNIQUE (nombre_cientifico)
) ENGINE = InnoDB;

CREATE TABLE cultivar (
  id_cultivar  SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre       VARCHAR(60) NOT NULL,
  id_especie   SMALLINT UNSIGNED NOT NULL,
  uso          ENUM('COPA','PORTAINJERTO') NOT NULL,
  activo       BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT pk_cultivar PRIMARY KEY (id_cultivar),
  CONSTRAINT uq_cultivar_nombre UNIQUE (nombre),
  CONSTRAINT fk_cultivar_especie FOREIGN KEY (id_especie) REFERENCES especie (id_especie)
) ENGINE = InnoDB;

CREATE TABLE organismo_emisor (
  id_organismo  SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
  sigla         VARCHAR(15)  NOT NULL,
  nombre        VARCHAR(120) NOT NULL,
  CONSTRAINT pk_organismo PRIMARY KEY (id_organismo),
  CONSTRAINT uq_organismo_sigla UNIQUE (sigla)
) ENGINE = InnoDB;

CREATE TABLE material_certificado (
  id_material        INT UNSIGNED NOT NULL AUTO_INCREMENT,
  tipo               ENUM('SEMILLA','YEMA') NOT NULL,
  id_cultivar        SMALLINT UNSIGNED NOT NULL,
  precinto           VARCHAR(20) NOT NULL,
  nro_certificado    VARCHAR(20) NOT NULL,
  id_organismo       SMALLINT UNSIGNED NOT NULL,
  fecha_cosecha      DATE NULL,
  fecha_ingreso      DATE NOT NULL,
  fecha_vencimiento  DATE NOT NULL,
  cantidad_ingresada INT NOT NULL,
  CONSTRAINT pk_material PRIMARY KEY (id_material),
  CONSTRAINT uq_material_precinto UNIQUE (precinto),
  CONSTRAINT fk_material_cultivar FOREIGN KEY (id_cultivar) REFERENCES cultivar (id_cultivar),
  CONSTRAINT fk_material_organismo FOREIGN KEY (id_organismo) REFERENCES organismo_emisor (id_organismo),
  CONSTRAINT ck_material_cantidad CHECK (cantidad_ingresada > 0),
  CONSTRAINT ck_material_fechas CHECK (fecha_vencimiento > fecha_ingreso)
) ENGINE = InnoDB;

CREATE TABLE campania (
  id_campania   SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre        VARCHAR(20) NOT NULL,
  fecha_inicio  DATE NOT NULL,
  fecha_fin     DATE NOT NULL,
  CONSTRAINT pk_campania PRIMARY KEY (id_campania),
  CONSTRAINT uq_campania_nombre UNIQUE (nombre),
  CONSTRAINT ck_campania_fechas CHECK (fecha_fin > fecha_inicio)
) ENGINE = InnoDB;

CREATE TABLE estado_lote (
  id_estado  TINYINT UNSIGNED NOT NULL,
  codigo     VARCHAR(20) NOT NULL,
  nombre     VARCHAR(40) NOT NULL,
  orden      TINYINT UNSIGNED NOT NULL,
  CONSTRAINT pk_estado_lote PRIMARY KEY (id_estado),
  CONSTRAINT uq_estado_lote_codigo UNIQUE (codigo)
) ENGINE = InnoDB;

CREATE TABLE tipo_evento (
  id_tipo_evento TINYINT UNSIGNED NOT NULL,
  codigo         VARCHAR(25) NOT NULL,
  nombre         VARCHAR(50) NOT NULL,
  signo          TINYINT NOT NULL COMMENT '+1 suma existencia, -1 resta, 0 no la modifica',
  CONSTRAINT pk_tipo_evento PRIMARY KEY (id_tipo_evento),
  CONSTRAINT uq_tipo_evento_codigo UNIQUE (codigo),
  CONSTRAINT ck_tipo_evento_signo CHECK (signo IN (-1, 0, 1))
) ENGINE = InnoDB;

CREATE TABLE motivo_egreso (
  id_motivo  SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre     VARCHAR(60) NOT NULL,
  etapa      VARCHAR(30) NOT NULL,
  CONSTRAINT pk_motivo_egreso PRIMARY KEY (id_motivo),
  CONSTRAINT uq_motivo_egreso_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- 4. NUCLEO PRODUCTIVO: LOTES, GENEALOGIA Y EVENTOS
-- ---------------------------------------------------------------------
CREATE TABLE lote (
  id_lote                  INT UNSIGNED NOT NULL AUTO_INCREMENT,
  codigo                   VARCHAR(12) NOT NULL,
  clase                    ENUM('PORTAINJERTO','YEMAS','INJERTADA') NOT NULL,
  id_campania              SMALLINT UNSIGNED NOT NULL,
  id_cultivar_portainjerto SMALLINT UNSIGNED NULL,
  id_cultivar_copa         SMALLINT UNSIGNED NULL,
  id_material_origen       INT UNSIGNED NULL,
  id_ubicacion             INT UNSIGNED NULL,
  id_estado                TINYINT UNSIGNED NOT NULL,
  cantidad_actual          INT NOT NULL DEFAULT 0
                           COMMENT 'Saldo mantenido por trigger a partir de evento_lote',
  es_saldo_inicial         BOOLEAN NOT NULL DEFAULT FALSE,
  fecha_creacion           DATETIME NOT NULL,
  CONSTRAINT pk_lote PRIMARY KEY (id_lote),
  CONSTRAINT uq_lote_codigo UNIQUE (codigo),
  CONSTRAINT fk_lote_campania FOREIGN KEY (id_campania) REFERENCES campania (id_campania),
  CONSTRAINT fk_lote_portainjerto FOREIGN KEY (id_cultivar_portainjerto) REFERENCES cultivar (id_cultivar),
  CONSTRAINT fk_lote_copa FOREIGN KEY (id_cultivar_copa) REFERENCES cultivar (id_cultivar),
  CONSTRAINT fk_lote_material FOREIGN KEY (id_material_origen) REFERENCES material_certificado (id_material),
  CONSTRAINT fk_lote_ubicacion FOREIGN KEY (id_ubicacion) REFERENCES ubicacion (id_ubicacion),
  CONSTRAINT fk_lote_estado FOREIGN KEY (id_estado) REFERENCES estado_lote (id_estado),
  CONSTRAINT ck_lote_existencia CHECK (cantidad_actual >= 0),
  CONSTRAINT ck_lote_clase CHECK (
       (clase = 'PORTAINJERTO' AND id_cultivar_portainjerto IS NOT NULL AND id_cultivar_copa IS NULL)
    OR (clase = 'YEMAS'        AND id_cultivar_copa IS NOT NULL AND id_cultivar_portainjerto IS NULL)
    OR (clase = 'INJERTADA'    AND id_cultivar_copa IS NOT NULL AND id_cultivar_portainjerto IS NOT NULL))
) ENGINE = InnoDB;

CREATE TABLE evento_lote (
  id_evento             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_lote               INT UNSIGNED NOT NULL,
  id_tipo_evento        TINYINT UNSIGNED NOT NULL,
  fecha_hora            DATETIME NOT NULL COMMENT 'Momento en que ocurrio la operacion',
  cantidad              INT NOT NULL,
  id_ubicacion_origen   INT UNSIGNED NULL,
  id_ubicacion_destino  INT UNSIGNED NULL,
  id_empleado           INT UNSIGNED NULL COMMENT 'Operario que ejecuto la tarea',
  id_motivo             SMALLINT UNSIGNED NULL,
  id_evento_relacionado BIGINT UNSIGNED NULL COMMENT 'Vincula consumos con la injertacion o division que los origina',
  id_evento_anulado     BIGINT UNSIGNED NULL COMMENT 'Asiento inverso: evento que se revierte',
  id_usuario            INT UNSIGNED NOT NULL COMMENT 'Usuario que registro el evento',
  registrado_en         TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  observacion           VARCHAR(255) NULL,
  CONSTRAINT pk_evento_lote PRIMARY KEY (id_evento),
  CONSTRAINT uq_evento_anulado UNIQUE (id_evento_anulado),
  CONSTRAINT fk_evento_lote FOREIGN KEY (id_lote) REFERENCES lote (id_lote),
  CONSTRAINT fk_evento_tipo FOREIGN KEY (id_tipo_evento) REFERENCES tipo_evento (id_tipo_evento),
  CONSTRAINT fk_evento_ubic_origen FOREIGN KEY (id_ubicacion_origen) REFERENCES ubicacion (id_ubicacion),
  CONSTRAINT fk_evento_ubic_destino FOREIGN KEY (id_ubicacion_destino) REFERENCES ubicacion (id_ubicacion),
  CONSTRAINT fk_evento_empleado FOREIGN KEY (id_empleado) REFERENCES empleado (id_empleado),
  CONSTRAINT fk_evento_motivo FOREIGN KEY (id_motivo) REFERENCES motivo_egreso (id_motivo),
  CONSTRAINT fk_evento_relacionado FOREIGN KEY (id_evento_relacionado) REFERENCES evento_lote (id_evento),
  CONSTRAINT fk_evento_anulado FOREIGN KEY (id_evento_anulado) REFERENCES evento_lote (id_evento),
  CONSTRAINT fk_evento_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
  CONSTRAINT ck_evento_cantidad CHECK (cantidad > 0)
) ENGINE = InnoDB;

CREATE TABLE lote_genealogia (
  id_lote         INT UNSIGNED NOT NULL COMMENT 'Lote derivado',
  id_lote_origen  INT UNSIGNED NOT NULL COMMENT 'Lote antecesor',
  vinculo         ENUM('PORTAINJERTO','YEMA','DIVISION') NOT NULL,
  id_evento       BIGINT UNSIGNED NOT NULL COMMENT 'Evento que produjo el vinculo',
  CONSTRAINT pk_lote_genealogia PRIMARY KEY (id_lote, id_lote_origen),
  CONSTRAINT fk_genealogia_lote FOREIGN KEY (id_lote) REFERENCES lote (id_lote),
  CONSTRAINT fk_genealogia_origen FOREIGN KEY (id_lote_origen) REFERENCES lote (id_lote),
  CONSTRAINT fk_genealogia_evento FOREIGN KEY (id_evento) REFERENCES evento_lote (id_evento),
  CONSTRAINT ck_genealogia_distintos CHECK (id_lote <> id_lote_origen)
) ENGINE = InnoDB;

CREATE TABLE tratamiento (
  id_evento         BIGINT UNSIGNED NOT NULL,
  tipo              ENUM('FITOSANITARIO','RIEGO','FERTILIZACION') NOT NULL,
  producto          VARCHAR(80) NOT NULL,
  principio_activo  VARCHAR(80) NULL,
  dosis             DECIMAL(8,2) NOT NULL,
  unidad_dosis      VARCHAR(10) NOT NULL,
  CONSTRAINT pk_tratamiento PRIMARY KEY (id_evento),
  CONSTRAINT fk_tratamiento_evento FOREIGN KEY (id_evento) REFERENCES evento_lote (id_evento),
  CONSTRAINT ck_tratamiento_dosis CHECK (dosis > 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- 5. GESTION COMERCIAL
-- ---------------------------------------------------------------------
CREATE TABLE cliente (
  id_cliente    INT UNSIGNED NOT NULL AUTO_INCREMENT,
  razon_social  VARCHAR(120) NOT NULL,
  cuit          CHAR(13)     NOT NULL,
  localidad     VARCHAR(60)  NOT NULL,
  provincia     VARCHAR(40)  NOT NULL,
  telefono      VARCHAR(30)  NULL,
  email         VARCHAR(120) NULL,
  CONSTRAINT pk_cliente PRIMARY KEY (id_cliente),
  CONSTRAINT uq_cliente_cuit UNIQUE (cuit),
  CONSTRAINT ck_cliente_cuit CHECK (cuit REGEXP '^[0-9]{2}-[0-9]{8}-[0-9]$')
) ENGINE = InnoDB;

CREATE TABLE pedido (
  id_pedido         INT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_cliente        INT UNSIGNED NOT NULL,
  fecha_pedido      DATE NOT NULL,
  fecha_compromiso  DATE NOT NULL,
  estado            ENUM('PENDIENTE','RESERVADO_PARCIAL','RESERVADO','DESPACHADO','CANCELADO')
                    NOT NULL DEFAULT 'PENDIENTE',
  CONSTRAINT pk_pedido PRIMARY KEY (id_pedido),
  CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente),
  CONSTRAINT ck_pedido_fechas CHECK (fecha_compromiso >= fecha_pedido)
) ENGINE = InnoDB;

CREATE TABLE pedido_item (
  id_item                  INT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_pedido                INT UNSIGNED NOT NULL,
  id_cultivar_copa         SMALLINT UNSIGNED NOT NULL,
  id_cultivar_portainjerto SMALLINT UNSIGNED NOT NULL,
  cantidad                 INT NOT NULL,
  CONSTRAINT pk_pedido_item PRIMARY KEY (id_item),
  CONSTRAINT uq_pedido_item_combinacion UNIQUE (id_pedido, id_cultivar_copa, id_cultivar_portainjerto),
  CONSTRAINT fk_item_pedido FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido) ON DELETE CASCADE,
  CONSTRAINT fk_item_copa FOREIGN KEY (id_cultivar_copa) REFERENCES cultivar (id_cultivar),
  CONSTRAINT fk_item_portainjerto FOREIGN KEY (id_cultivar_portainjerto) REFERENCES cultivar (id_cultivar),
  CONSTRAINT ck_item_cantidad CHECK (cantidad > 0)
) ENGINE = InnoDB;

CREATE TABLE reserva (
  id_reserva   INT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_item      INT UNSIGNED NOT NULL,
  id_lote      INT UNSIGNED NOT NULL,
  cantidad     INT NOT NULL,
  fecha_hora   DATETIME NOT NULL,
  estado       ENUM('VIGENTE','DESPACHADA','LIBERADA') NOT NULL DEFAULT 'VIGENTE',
  id_usuario   INT UNSIGNED NOT NULL,
  CONSTRAINT pk_reserva PRIMARY KEY (id_reserva),
  CONSTRAINT fk_reserva_item FOREIGN KEY (id_item) REFERENCES pedido_item (id_item),
  CONSTRAINT fk_reserva_lote FOREIGN KEY (id_lote) REFERENCES lote (id_lote),
  CONSTRAINT fk_reserva_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
  CONSTRAINT ck_reserva_cantidad CHECK (cantidad > 0)
) ENGINE = InnoDB;

CREATE TABLE despacho (
  id_despacho  INT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_pedido    INT UNSIGNED NOT NULL,
  fecha        DATE NOT NULL,
  nro_dtv      VARCHAR(30) NOT NULL COMMENT 'Documento de Transito Vegetal electronico',
  id_usuario   INT UNSIGNED NOT NULL,
  CONSTRAINT pk_despacho PRIMARY KEY (id_despacho),
  CONSTRAINT uq_despacho_dtv UNIQUE (nro_dtv),
  CONSTRAINT fk_despacho_pedido FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido),
  CONSTRAINT fk_despacho_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
) ENGINE = InnoDB;

CREATE TABLE despacho_detalle (
  id_despacho  INT UNSIGNED NOT NULL,
  id_evento    BIGINT UNSIGNED NOT NULL COMMENT 'Egreso por venta generado en el lote',
  id_reserva   INT UNSIGNED NULL,
  CONSTRAINT pk_despacho_detalle PRIMARY KEY (id_despacho, id_evento),
  CONSTRAINT uq_despacho_detalle_evento UNIQUE (id_evento),
  CONSTRAINT fk_detalle_despacho FOREIGN KEY (id_despacho) REFERENCES despacho (id_despacho),
  CONSTRAINT fk_detalle_evento FOREIGN KEY (id_evento) REFERENCES evento_lote (id_evento),
  CONSTRAINT fk_detalle_reserva FOREIGN KEY (id_reserva) REFERENCES reserva (id_reserva)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- 6. TRABAJO DEL PERSONAL Y LIQUIDACION QUINCENAL
-- ---------------------------------------------------------------------
CREATE TABLE tarea (
  id_tarea        SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre          VARCHAR(50) NOT NULL,
  unidad_medida   VARCHAR(20) NOT NULL,
  id_tipo_evento  TINYINT UNSIGNED NULL COMMENT 'Evento productivo del que puede derivarse',
  CONSTRAINT pk_tarea PRIMARY KEY (id_tarea),
  CONSTRAINT uq_tarea_nombre UNIQUE (nombre),
  CONSTRAINT fk_tarea_tipo_evento FOREIGN KEY (id_tipo_evento) REFERENCES tipo_evento (id_tipo_evento)
) ENGINE = InnoDB;

CREATE TABLE tarifa_tarea (
  id_tarea        SMALLINT UNSIGNED NOT NULL,
  vigencia_desde  DATE NOT NULL,
  valor_unitario  DECIMAL(10,2) NOT NULL,
  CONSTRAINT pk_tarifa_tarea PRIMARY KEY (id_tarea, vigencia_desde),
  CONSTRAINT fk_tarifa_tarea FOREIGN KEY (id_tarea) REFERENCES tarea (id_tarea),
  CONSTRAINT ck_tarifa_valor CHECK (valor_unitario > 0)
) ENGINE = InnoDB;

CREATE TABLE quincena (
  id_quincena        SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
  anio               SMALLINT UNSIGNED NOT NULL,
  mes                TINYINT UNSIGNED NOT NULL,
  numero             TINYINT UNSIGNED NOT NULL,
  fecha_desde        DATE NOT NULL,
  fecha_hasta        DATE NOT NULL,
  liquidada          BOOLEAN NOT NULL DEFAULT FALSE,
  fecha_liquidacion  DATETIME NULL,
  CONSTRAINT pk_quincena PRIMARY KEY (id_quincena),
  CONSTRAINT uq_quincena_periodo UNIQUE (anio, mes, numero),
  CONSTRAINT ck_quincena_numero CHECK (numero IN (1, 2)),
  CONSTRAINT ck_quincena_mes CHECK (mes BETWEEN 1 AND 12),
  CONSTRAINT ck_quincena_fechas CHECK (fecha_hasta >= fecha_desde)
) ENGINE = InnoDB;

CREATE TABLE registro_trabajo (
  id_registro     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_empleado     INT UNSIGNED NOT NULL,
  id_tarea        SMALLINT UNSIGNED NOT NULL,
  id_quincena     SMALLINT UNSIGNED NOT NULL,
  fecha           DATE NOT NULL,
  cantidad        INT NOT NULL,
  valor_unitario  DECIMAL(10,2) NOT NULL COMMENT 'Tarifa vigente congelada al registrar',
  importe         DECIMAL(12,2) GENERATED ALWAYS AS (cantidad * valor_unitario) STORED,
  id_evento       BIGINT UNSIGNED NULL COMMENT 'Evento productivo que origina el trabajo',
  id_usuario      INT UNSIGNED NOT NULL,
  CONSTRAINT pk_registro_trabajo PRIMARY KEY (id_registro),
  CONSTRAINT fk_trabajo_empleado FOREIGN KEY (id_empleado) REFERENCES empleado (id_empleado),
  CONSTRAINT fk_trabajo_tarea FOREIGN KEY (id_tarea) REFERENCES tarea (id_tarea),
  CONSTRAINT fk_trabajo_quincena FOREIGN KEY (id_quincena) REFERENCES quincena (id_quincena),
  CONSTRAINT fk_trabajo_evento FOREIGN KEY (id_evento) REFERENCES evento_lote (id_evento),
  CONSTRAINT fk_trabajo_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
  CONSTRAINT ck_trabajo_cantidad CHECK (cantidad > 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- 7. AUDITORIA E INTENTOS RECHAZADOS
-- ---------------------------------------------------------------------
CREATE TABLE auditoria (
  id_auditoria  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_usuario    INT UNSIGNED NOT NULL,
  fecha_hora    TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  accion        ENUM('LOGIN','ALTA','MODIFICACION','ANULACION','BAJA_LOGICA','CONSULTA') NOT NULL,
  entidad       VARCHAR(40)  NOT NULL,
  id_entidad    BIGINT UNSIGNED NULL,
  detalle       VARCHAR(500) NULL,
  CONSTRAINT pk_auditoria PRIMARY KEY (id_auditoria),
  CONSTRAINT fk_auditoria_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
) ENGINE = InnoDB;

CREATE TABLE intento_rechazado (
  id_intento           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_usuario           INT UNSIGNED NOT NULL,
  fecha_hora           TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  operacion            VARCHAR(40)  NOT NULL,
  regla                CHAR(5)      NOT NULL COMMENT 'Regla de negocio que motivo el rechazo',
  id_lote              INT UNSIGNED NULL,
  id_ubicacion         INT UNSIGNED NULL,
  cantidad_pretendida  INT NULL,
  motivo               VARCHAR(255) NOT NULL,
  CONSTRAINT pk_intento_rechazado PRIMARY KEY (id_intento),
  CONSTRAINT fk_intento_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
  CONSTRAINT fk_intento_lote FOREIGN KEY (id_lote) REFERENCES lote (id_lote),
  CONSTRAINT fk_intento_ubicacion FOREIGN KEY (id_ubicacion) REFERENCES ubicacion (id_ubicacion)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- 8. INDICES SECUNDARIOS (rendimiento: RNF-02 y RNF-03)
-- ---------------------------------------------------------------------
CREATE INDEX ix_evento_lote_fecha      ON evento_lote (id_lote, fecha_hora);
CREATE INDEX ix_evento_tipo_fecha      ON evento_lote (id_tipo_evento, fecha_hora);
CREATE INDEX ix_evento_empleado_fecha  ON evento_lote (id_empleado, fecha_hora);
CREATE INDEX ix_lote_ubicacion_cant    ON lote (id_ubicacion, cantidad_actual);
CREATE INDEX ix_lote_combinacion       ON lote (id_cultivar_copa, id_cultivar_portainjerto, id_estado);
CREATE INDEX ix_genealogia_origen      ON lote_genealogia (id_lote_origen);
CREATE INDEX ix_trabajo_quincena_emp   ON registro_trabajo (id_quincena, id_empleado);
CREATE INDEX ix_auditoria_usuario_fh   ON auditoria (id_usuario, fecha_hora);
CREATE INDEX ix_reserva_lote_estado    ON reserva (id_lote, estado);

-- ---------------------------------------------------------------------
-- 9. TRIGGERS: reglas que la base garantiza por si misma
-- ---------------------------------------------------------------------
DELIMITER $$

-- RN-02 / RNF-08: los eventos son inmutables; se corrigen por asiento inverso
CREATE TRIGGER trg_evento_bu_inmutable BEFORE UPDATE ON evento_lote
FOR EACH ROW
BEGIN
  SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'RN-02: los eventos de lote son inmutables. Registre un asiento inverso.';
END$$

CREATE TRIGGER trg_evento_bd_inmutable BEFORE DELETE ON evento_lote
FOR EACH ROW
BEGIN
  SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'RN-02: no se permite eliminar eventos de lote. Registre un asiento inverso.';
END$$

-- Mantiene el saldo del lote a partir de cada evento (fuente unica de verdad).
-- Si el saldo quedara negativo, el CHECK ck_lote_existencia aborta la operacion (RN-01).
CREATE TRIGGER trg_evento_ai_existencia AFTER INSERT ON evento_lote
FOR EACH ROW
BEGIN
  DECLARE v_signo TINYINT;
  SELECT signo INTO v_signo FROM tipo_evento WHERE id_tipo_evento = NEW.id_tipo_evento;
  IF v_signo <> 0 THEN
    UPDATE lote
       SET cantidad_actual = cantidad_actual + v_signo * CAST(NEW.cantidad AS SIGNED)
     WHERE id_lote = NEW.id_lote;
  END IF;
END$$

-- La auditoria tampoco puede alterarse (RF-25)
CREATE TRIGGER trg_auditoria_bu_inmutable BEFORE UPDATE ON auditoria
FOR EACH ROW
BEGIN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'RF-25: el registro de auditoria no puede modificarse.';
END$$

CREATE TRIGGER trg_auditoria_bd_inmutable BEFORE DELETE ON auditoria
FOR EACH ROW
BEGIN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'RF-25: el registro de auditoria no puede eliminarse.';
END$$

-- RN-08: una quincena liquidada no admite nuevas cargas; la fecha debe pertenecer a la quincena
CREATE TRIGGER trg_trabajo_bi_quincena BEFORE INSERT ON registro_trabajo
FOR EACH ROW
BEGIN
  DECLARE v_liquidada BOOLEAN;
  DECLARE v_desde DATE;
  DECLARE v_hasta DATE;
  SELECT liquidada, fecha_desde, fecha_hasta INTO v_liquidada, v_desde, v_hasta
    FROM quincena WHERE id_quincena = NEW.id_quincena;
  IF v_liquidada THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'RN-08: la quincena ya fue liquidada. Registre el ajuste en la quincena siguiente.';
  END IF;
  IF NEW.fecha NOT BETWEEN v_desde AND v_hasta THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'RN-08: la fecha del trabajo no pertenece a la quincena indicada.';
  END IF;
END$$

DELIMITER ;

-- ---------------------------------------------------------------------
-- 10. VISTAS DE CONSULTA FRECUENTE
-- ---------------------------------------------------------------------
-- Ocupacion de cada ubicacion receptora (base del mapa de ocupacion, RF-11)
CREATE VIEW v_ocupacion_ubicacion AS
SELECT u.id_ubicacion,
       u.codigo,
       u.nombre,
       u.capacidad_maxima,
       COALESCE(SUM(l.cantidad_actual), 0)                                   AS ocupacion,
       u.capacidad_maxima - COALESCE(SUM(l.cantidad_actual), 0)              AS disponible,
       ROUND(100 * COALESCE(SUM(l.cantidad_actual), 0) / u.capacidad_maxima, 1) AS porcentaje
  FROM ubicacion u
  JOIN tipo_ubicacion t ON t.id_tipo_ubicacion = u.id_tipo_ubicacion AND t.admite_lotes = TRUE
  LEFT JOIN lote l ON l.id_ubicacion = u.id_ubicacion AND l.cantidad_actual > 0
 GROUP BY u.id_ubicacion, u.codigo, u.nombre, u.capacidad_maxima;

-- Existencias con descripcion legible de la combinacion copa / portainjerto (RF-21)
CREATE VIEW v_existencia_lote AS
SELECT l.id_lote,
       l.codigo,
       l.clase,
       cp.nombre  AS copa,
       pi.nombre  AS portainjerto,
       u.codigo   AS ubicacion,
       e.nombre   AS estado,
       c.nombre   AS campania,
       l.cantidad_actual,
       l.es_saldo_inicial
  FROM lote l
  JOIN estado_lote e   ON e.id_estado = l.id_estado
  JOIN campania c      ON c.id_campania = l.id_campania
  LEFT JOIN cultivar cp ON cp.id_cultivar = l.id_cultivar_copa
  LEFT JOIN cultivar pi ON pi.id_cultivar = l.id_cultivar_portainjerto
  LEFT JOIN ubicacion u ON u.id_ubicacion = l.id_ubicacion;
