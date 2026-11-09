# Funcionamiento de Java

## Roles y permisos

Los identificadores coinciden con la base compartida y la web. Los permisos de las operaciones se vuelven a comprobar con el usuario de la sesión y su rol vigente en la base, incluyendo el estado del perfil.

| Rol | ID | Administración y cuentas | Otros accesos |
| --- | --- | --- | --- |
| Dirección | 1 | Todos los roles, gestión de cuentas y logs | Cursos, materias, calificaciones, facturación, solicitudes, condiciones y promociones |
| Secretaría | 2 | Alumnos, docentes y cooperadora; sin logs ni modificación de Dirección o Secretaría | Cursos y materias en consulta, inscripciones, solicitudes y promociones |
| Docente | 3 | Sin acceso | Cursos y materias asignados y sus calificaciones |
| Alumno | 4 | Sin acceso | Sus cursos, calificaciones y facturación |
| Cooperadora | 5 | Sin acceso | Gestión de facturación |

En inscripciones se conserva la separación original: registrar, modificar, cancelar y trasladar directamente corresponden a Secretaría. Dirección puede resolver solicitudes y ejecutar promociones; su control de Administración no le otorga esas operaciones exclusivas de Secretaría.

Secretaría tampoco puede convertir otra cuenta en Dirección, ni modificar una cuenta de Dirección asignándole primero otro rol. Se comprueban el rol anterior y el solicitado. Nadie puede cambiar su propio rol ni desactivar su propia sesión desde este formulario. Si otro operador cambia el rol mientras el formulario está abierto, hay que volver a cargar la cuenta.

## Registro y edición de cuentas

El selector muestra los roles permitidos. Alumno despliega los datos de su responsable. Matrícula y legajo se generan con el identificador de la cuenta; no se solicitan como entrada y se conservan al editar el mismo perfil. Los perfiles anteriores se conservan al cambiar de rol, junto con sus relaciones académicas.

El alta de usuario, el perfil, el responsable cuando corresponde y el aviso de correo comparten una transacción. Un fallo revierte todo. El bloqueo de altas se comparte con la web para comprobar nombres de usuario y correos sin duplicaciones concurrentes.

## Autocompletado de alumnos

En Inscripciones se puede escribir nombre, apellido, DNI o matrícula. La búsqueda ignora mayúsculas y tildes y filtra los alumnos activos cargados, sin consultar la base con cada letra. Es necesario seleccionar una sugerencia; un texto libre no se convierte en un identificador. Al editar una inscripción, alumno y curso permanecen fijos.

Las fechas escritas se interpretan de forma estricta: una fecha inexistente no se corrige automáticamente.

## Correo

Java guarda el aviso mediante `Notificacion_Correo_dao`, en la tabla existente `notificacion_correo`. Usa una clave de evento para identificar el aviso de una cuenta o inscripción. No crea tablas.

Se generan avisos para:

- Alta de una cuenta de alumno o docente.
- Inscripción del alumno y nueva inscripción de un traslado o promoción.
- Asignación o cambio efectivo de un docente en una materia del curso.

Primero se confirma la operación. Después se intenta el envío. Los mensajes de alta no incluyen contraseñas. Una operación revertida no genera un envío y un fallo de correo no elimina una cuenta o inscripción ya confirmada.

`SesionWeb_servicio` abre una sesión HTTPS con la cuenta autenticada y comparte la conexión con Facturación. El acceso `api_notificaciones.php` reutiliza el servicio SMTP de la web: Java no contiene claves SMTP. El servidor comprueba sesión activa, permisos y token del formulario, y recibe únicamente el identificador del aviso; no acepta un rol, destinatario ni mensaje como autoridad del cliente.

El servicio web reserva cada aviso antes del envío y conserva su estado e intentos. Los avisos ya enviados no se vuelven a mandar al repetir la solicitud. Administración incluye **Reintentar avisos** para los pendientes de la sesión. Si vence la sesión web, se debe volver a ingresar a Java antes de reintentar.

El 9 de noviembre de 2026 se envió un único mensaje de prueba al destinatario autorizado. El servicio devolvió `Enviado`; repetir la solicitud no incrementó los intentos. Esto confirma la aceptación por el servidor SMTP; la ubicación en Recibidos o Spam depende del proveedor del destinatario.

## Verificación

El proyecto y las pruebas compilan con Java 25 y las bibliotecas existentes. La compilación de comprobación se hizo fuera de `bin` para conservar los archivos de trabajo del usuario.

- `tests/ReglasJava.java`: permisos, motivos, estados, cupo, duplicados, conservación de notas y origen, reversión y selección real de sugerencias en Swing.
- `tests/CuentasJava.java`: altas por rol, rechazo de cambios prohibidos, duplicados, contraseña protegida y fallo del perfil que revierte la cuenta sin enviar correo.
- `tests/SolicitudesPromocionJava.java`: solicitud sin reservar cupo, resolución única, excepción de año y período, rechazo, dos solicitudes simultáneas por el último lugar, promoción y fallo del segundo alumno que revierte el lote completo.
- `tests/IntegracionJava.java`: autenticación y accesos reales de los cinco roles. El argumento opcional de destinatario ejecuta la prueba de correo autorizada con un evento idempotente.
- `tests/PersistenciaJava.java`: inserción técnica real revertida en MySQL y rechazo HTTPS de avisos ajenos de Secretaría o de un rol enviado por el cliente. No envía mensajes nuevos.

Las pruebas de flujos académicos y concurrencia usan DAO en memoria con transacciones y serialización. No se ejecutaron promociones, traslados ni altas de personas reales en la base publicada. La autenticación, la reversión técnica y la conexión SMTP se comprobaron con los servicios reales.

## Decisiones conservadas

No se agregó una restricción por fechas del período al registrar o trasladar. Tampoco una regla general de una única inscripción abierta por alumno y período, ni una operación de reversión académica. Traslados y promociones conservan la inscripción original y sus notas, y crean otra inscripción vinculada. Ampliar esas reglas requiere una decisión funcional explícita.
