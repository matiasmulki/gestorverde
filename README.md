# Gestor Verde

Sistema de trazabilidad para viveros citrícolas (Vivero Citrus, Tucumán).
Seminario de Práctica · Licenciatura en Informática · Universidad Siglo 21.
Autor: Matías Fernando Mulki.

| Carpeta | Contenido | Entrega |
|---|---|---|
| `mysql/` | Scripts de la base de datos MySQL 8 (esquema, datos, consultas, operaciones, seguridad, rendimiento) y DER. | TP2 |
| `prototipo-java/` | Prototipo en Java 17 con interfaz gráfica Swing, POO, JDBC y pruebas JUnit 5. | TP3 |
| `docs/diagramas/` | Diagramas UML del prototipo (fuentes PlantUML e imágenes). | TP3 |
| `docs/capturas/` | Capturas de la interfaz gráfica. | TP3 |

## Puesta en marcha rápida

1. Crear la base y el usuario de la aplicación (MySQL 8.0 o superior):
   ```
   cd mysql
   mysql -u root -p --default-character-set=utf8mb4 < 01_esquema.sql
   mysql -u root -p --default-character-set=utf8mb4 < 02_datos.sql
   mysql -u root -p --default-character-set=utf8mb4 < 05_seguridad.sql
   ```
2. Compilar y ejecutar el prototipo (JDK 17 o superior y Maven):
   ```
   cd prototipo-java
   mvn clean package
   java -jar target/gestorverde-prototipo-1.0-jar-with-dependencies.jar
   ```
3. Ingresar con `jperez` / `Vivero2026!` (encargado de producción). Todos los usuarios de prueba usan la misma clave.

Más detalles en `prototipo-java/README.md` y `mysql/README.md`.
