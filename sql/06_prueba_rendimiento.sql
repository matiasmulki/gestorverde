-- =====================================================================
--  GESTOR VERDE - Script 06 (opcional): prueba de rendimiento RNF-02
--  Genera 2.000 lotes sinteticos y 500.000 eventos para medir la consulta
--  de trazabilidad. Ejecutar SOLO en una base de pruebas, despues de 01 y 02.
-- =====================================================================
USE gestorverde;
SET NAMES utf8mb4;
SET SESSION cte_max_recursion_depth = 1000000;

-- 2.000 lotes de portainjerto sinteticos distribuidos en los canteros
INSERT INTO lote (codigo, clase, id_campania, id_cultivar_portainjerto, id_ubicacion, id_estado, fecha_creacion)
WITH RECURSIVE n AS (SELECT 1 AS i UNION ALL SELECT i + 1 FROM n WHERE i < 2000)
SELECT CONCAT('P', LPAD(i, 5, '0')), 'PORTAINJERTO', 2, 9 + (i MOD 4),
       ELT(1 + (i MOD 6), 4, 6, 8, 14, 17, 20), 3, '2025-07-01'
  FROM n;

-- 500.000 eventos (tratamientos y movimientos: no alteran el saldo)
INSERT INTO evento_lote (id_lote, id_tipo_evento, fecha_hora, cantidad, id_ubicacion_origen, id_usuario)
WITH RECURSIVE n AS (SELECT 1 AS i UNION ALL SELECT i + 1 FROM n WHERE i < 500000)
SELECT (SELECT MIN(id_lote) FROM lote WHERE codigo = 'P00001') + (i MOD 2000),
       IF(i MOD 3 = 0, 4, 10),
       TIMESTAMPADD(MINUTE, i, '2025-07-02 00:00:00'),
       100 + (i MOD 900), 4, 2
  FROM n;

ANALYZE TABLE evento_lote, lote, lote_genealogia;

SELECT COUNT(*) AS eventos_totales FROM evento_lote;

-- Plan de ejecucion de la consulta de trazabilidad (Q-05)
EXPLAIN FORMAT = TREE
WITH RECURSIVE ancestros AS (
    SELECT id_lote FROM lote WHERE codigo = 'L0003'
    UNION ALL
    SELECT g.id_lote_origen FROM ancestros a JOIN lote_genealogia g ON g.id_lote = a.id_lote
)
SELECT e.fecha_hora, e.id_lote, e.id_tipo_evento, e.cantidad
  FROM evento_lote e JOIN ancestros a ON a.id_lote = e.id_lote
 ORDER BY e.fecha_hora;
