# Gestor Verde — Base de datos MySQL

Scripts de la base de datos del sistema de trazabilidad para viveros citrícolas **Gestor Verde**.
Trabajo Práctico 2 — Seminario de Práctica, Universidad Siglo 21. Autor: Matías Fernando Mulki.

## Requisitos
MySQL Server 8.0 o superior (motor InnoDB, codificación utf8mb4).

## Orden de ejecución

| Script | Contenido |
|---|---|
| `01_esquema.sql` | Crea la base `gestorverde`: 29 tablas, índices, 6 triggers y 2 vistas. |
| `02_datos.sql` | Inserta los datos iniciales y de prueba. |
| `03_consultas.sql` | Consultas Q-01 a Q-13. |
| `04_operaciones_y_borrado.sql` | Inserción transaccional, anulación por asiento inverso y borrado. Contiene errores provocados a propósito para demostrar las restricciones: ejecutar con `--force`. |
| `05_seguridad.sql` | Roles y usuarios con privilegios mínimos. |
| `06_prueba_rendimiento.sql` | Opcional. Carga 500.000 eventos para la prueba de rendimiento (solo en una base de prueba). |


## Diagrama entidad-relación
La carpeta `der/` contiene la vista general y las dos vistas de detalle del modelo.
