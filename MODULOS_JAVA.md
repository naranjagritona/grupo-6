# Cursos y Facturación en Java

## Abrir en Eclipse

1. Seleccionar el proyecto y pulsar F5.
2. Usar Project > Clean y compilar el proyecto.
3. Ejecutar src/main/Main.java como Java Application, con Java 25.

Las dependencias ya están agregadas al .classpath. Se conserva el conector MySQL de esta computadora.

## Cursos

- Sin división en el formulario, el listado ni las consultas SQL.
- Barra de búsqueda por año, materias, docente y demás columnas.
- Selección de una o varias materias mediante casillas.
- Solo administradores crean, editan o cancelan; alumnos y docentes consultan sus cursos.
- No se pueden eliminar cursos, tampoco llamando directamente al DAO.
- El curso y sus materias se guardan en una transacción. Se impiden años duplicados dentro del mismo período, cupos menores que las inscripciones y cambios de la materia original cuando tiene evaluaciones.

## Facturación

- Pagar en Java con tarjeta o efectivo, sin abrir el navegador.
- Consultar cuotas e historial, ver el PDF dentro de Java y guardarlo como archivo.
- La web registra el pago y genera la factura mediante la misma lógica que su formulario. Las facturas creadas antes desde la web también se ven en Java.
- El efectivo queda Pendiente hasta que un administrador indique quién recibió el dinero y confirme su recepción. El nombre del alumno no se modifica.
- El PDF y el correo se habilitan después de confirmar; tarjeta conserva la simulación del proyecto escolar.
- Una cuota por alumno y mes actual, con período e importe controlados por el servidor.
- La configuración del precio general continúa en la web.
- El alumno no tiene acceso a Inscripciones y solo consulta sus propias cuotas. El docente no tiene acceso a Facturación.
- La sesión de facturación usa HTTPS, cookie HttpOnly y verificación CSRF. Las credenciales no se guardan en archivos; se descarta la contraseña de memoria una vez iniciada esa sesión.

Servidor: modulos/api_facturacion.php y clases/api/facturacionAplicacion.php. Ambos ya están publicados en Torga; no hace falta importar nuevas tablas ni reemplazar la configuración de correo.

## Pruebas

- tests/PruebaLogin.java: contraseñas y acceso; con --base consulta la base compartida.
- tests/PruebaCursos.java: guardado, edición, reversión ante fallos, cancelación y bloqueo del borrado. Usa tablas temporales y no modifica las tablas reales.
- tests/PruebaModulos.java: lectura desde Java de la facturación web, PDF, permisos y rechazo de duplicados. Recibe como argumento la carpeta donde guardar una imagen del PDF de prueba. No registra pagos ni envía correos.
- tests/PruebaPantallas.java: carga y búsqueda de cursos, cuotas e historial; guarda vistas de los paneles.
- En el proyecto PHP: tests/facturacion_aplicacion.php verifica pagos ficticios con SQLite y un correo reemplazado por una prueba local.

## Bibliotecas

- bcrypt y bytes: https://github.com/patrickfav/bcrypt
- Gson: https://github.com/google/gson
- Apache PDFBox, FontBox y PDFBox IO: https://pdfbox.apache.org/
- Apache Commons Logging: https://commons.apache.org/proper/commons-logging/

Los archivos de estas bibliotecas se descargaron de Maven Central. Sus licencias Apache 2.0 están incluidas en los JAR.
