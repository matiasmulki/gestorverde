-- =====================================================================
--  GESTOR VERDE - Script 05: usuarios de base de datos y privilegios
--  Principio de minimo privilegio: la aplicacion nunca se conecta como root.
--  Se usan ROLES de MySQL 8 para no repetir los permisos por cada host.
--  Reemplazar las claves antes de instalar en produccion.
-- =====================================================================

CREATE ROLE IF NOT EXISTS 'rol_gv_app', 'rol_gv_reportes', 'rol_gv_backup';

-- Rol de la aplicacion: lectura general y escritura controlada
GRANT SELECT ON gestorverde.* TO 'rol_gv_app';
GRANT INSERT, UPDATE ON gestorverde.lote                 TO 'rol_gv_app';
GRANT INSERT, UPDATE ON gestorverde.ubicacion            TO 'rol_gv_app';
GRANT INSERT, UPDATE ON gestorverde.material_certificado TO 'rol_gv_app';
GRANT INSERT, UPDATE ON gestorverde.cliente              TO 'rol_gv_app';
GRANT INSERT, UPDATE ON gestorverde.pedido               TO 'rol_gv_app';
GRANT INSERT, UPDATE, DELETE ON gestorverde.pedido_item  TO 'rol_gv_app';
GRANT INSERT, UPDATE ON gestorverde.reserva              TO 'rol_gv_app';
GRANT INSERT, UPDATE ON gestorverde.quincena             TO 'rol_gv_app';
GRANT INSERT, UPDATE ON gestorverde.usuario              TO 'rol_gv_app';
-- Tablas de historia: solo INSERT (ademas, los triggers bloquean UPDATE y DELETE)
GRANT INSERT ON gestorverde.evento_lote        TO 'rol_gv_app';
GRANT INSERT ON gestorverde.lote_genealogia    TO 'rol_gv_app';
GRANT INSERT ON gestorverde.tratamiento        TO 'rol_gv_app';
GRANT INSERT ON gestorverde.despacho           TO 'rol_gv_app';
GRANT INSERT ON gestorverde.despacho_detalle   TO 'rol_gv_app';
GRANT INSERT ON gestorverde.registro_trabajo   TO 'rol_gv_app';
GRANT INSERT ON gestorverde.auditoria          TO 'rol_gv_app';
GRANT INSERT ON gestorverde.intento_rechazado  TO 'rol_gv_app';

-- Rol de reportes: solo lectura
GRANT SELECT ON gestorverde.* TO 'rol_gv_reportes';

-- Rol de respaldo: lo necesario para mysqldump --single-transaction (RNF-07)
GRANT SELECT, SHOW VIEW, TRIGGER, LOCK TABLES, EVENT ON gestorverde.* TO 'rol_gv_backup';
GRANT PROCESS ON *.* TO 'rol_gv_backup';

-- Cuentas: puestos de la LAN del vivero (con TLS obligatorio) y desarrollo local
CREATE USER IF NOT EXISTS 'gv_app'@'192.168.10.%' IDENTIFIED BY 'CambiarEstaClave#2026' REQUIRE SSL;
CREATE USER IF NOT EXISTS 'gv_app'@'localhost'    IDENTIFIED BY 'CambiarEstaClave#2026';
CREATE USER IF NOT EXISTS 'gv_reportes'@'192.168.10.%' IDENTIFIED BY 'CambiarEstaClave#Rep' REQUIRE SSL;
CREATE USER IF NOT EXISTS 'gv_backup'@'localhost' IDENTIFIED BY 'CambiarEstaClave#Bkp';

GRANT 'rol_gv_app'      TO 'gv_app'@'192.168.10.%', 'gv_app'@'localhost';
GRANT 'rol_gv_reportes' TO 'gv_reportes'@'192.168.10.%';
GRANT 'rol_gv_backup'   TO 'gv_backup'@'localhost';

SET DEFAULT ROLE ALL TO 'gv_app'@'192.168.10.%', 'gv_app'@'localhost',
                        'gv_reportes'@'192.168.10.%', 'gv_backup'@'localhost';
