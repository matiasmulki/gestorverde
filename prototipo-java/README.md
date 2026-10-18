# Gestor Verde · Prototipo Java (TP3)

Aplicación de escritorio con interfaz gráfica **Java Swing** que implementa, sobre la base MySQL del TP2,
los casos de uso de mayor riesgo del sistema:

| Pantalla / formulario | Funcionalidad | Requerimiento |
|---|---|---|
| Ingreso | Inicio de sesión con roles (máximo tres intentos) | CU-16 |
| Mapa de ocupación | Árbol de la estructura del vivero y ocupación de cada ubicación | RF-11 |
| Lotes e historia | Listado ordenado con quicksort, filtro por cultivar, búsqueda binaria por código e historia del lote | RF-21, RF-09 |
| Registrar injertación | Formulario con validación de reglas de negocio | CU-03 |
| Movilizar lote | Traslado total o parcial | CU-04 |
| Registrar merma | Baja de plantas con motivo | CU-06 |
| Trazabilidad | Árbol hacia el origen (pila) y hacia adelante (cola) | CU-07 |
| Intentos rechazados | Operaciones rechazadas por las reglas | RF-02 |

El menú de selección está en la barra de menús (con atajos de teclado) y en la botonera lateral.
También existe un modo de texto: `java -jar ... --consola`.

## Requisitos

- JDK 17 o superior.
- Apache Maven 3.8 o superior (incluido en Apache NetBeans, IntelliJ IDEA y Eclipse).
- MySQL Server 8.0 o superior con la base `gestorverde` creada con los scripts `01`, `02` y `05` de la carpeta `mysql/`.

## Configuración

`src/main/resources/gestorverde.properties` trae la conexión por defecto:

```
db.url=jdbc:mysql://localhost:3306/gestorverde?serverTimezone=America/Argentina/Buenos_Aires&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false&connectTimeout=5000
db.usuario=gv_app
db.clave=CambiarEstaClave#2026
```

Para cambiarla sin recompilar, copie el archivo a la carpeta desde donde ejecuta el programa y edítelo,
o pase los valores como propiedades del sistema: `java -Ddb.clave=... -jar ...`.

## Compilar, probar y ejecutar

```
mvn clean package        # compila, ejecuta las 20 pruebas JUnit y genera el JAR
java -jar target/gestorverde-prototipo-1.0-jar-with-dependencies.jar
```

Desde Apache NetBeans: *Archivo → Abrir proyecto* → seleccionar la carpeta `prototipo-java` → *Ejecutar* (F6).

## Usuarios de prueba (clave `Vivero2026!`)

| Usuario | Rol | Puede |
|---|---|---|
| `jperez` | Encargado de producción | Todo el menú |
| `msoria` | Operario | Consultar, mover y registrar mermas (no injertar) |
| `lferreyra` | Responsable técnico | Consultar y ver intentos rechazados |

## Estructura

```
src/main/java/gestorverde/
├── App.java                 punto de entrada y raíz de composición
├── presentacion/swing/      interfaz gráfica: VentanaPrincipal, paneles y diálogos
├── presentacion/            modo consola (MenuPrincipal, Consola, Formato)
├── aplicacion/              servicios de caso de uso, Sesion, Solicitudes
├── dominio/                 Lote (abstracta) y subclases, Ubicacion (Composite), EventoLote...
├── persistencia/            interfaces DAO y Transacciones
│   └── jdbc/                implementación JDBC para MySQL
├── estructuras/             ListaEnlazada, Pila y Cola genéricas
├── algoritmos/              Ordenamiento (quicksort) y Busqueda (binaria y secuencial)
└── excepciones/             jerarquía GestorVerdeException
src/test/java/gestorverde/   pruebas unitarias JUnit 5
```
