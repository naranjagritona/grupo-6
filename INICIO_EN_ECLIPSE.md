# Iniciar el proyecto en Eclipse

1. Seleccionar el proyecto y pulsar F5 para actualizar los archivos.
2. En Project > Clean, limpiar este proyecto y dejar que Eclipse lo compile.
3. Ejecutar src/main/Main.java como Java Application.

Las bibliotecas bcrypt-0.10.2.jar y bytes-1.5.0.jar están en lib y ya están agregadas al .classpath. Se mantiene la ruta del conector MySQL configurada en esta computadora.

Cuentas: admin / 123; alumnos y docentes / Colegio2026. Ejemplos: sofia.fernandez y ana.castillo.

El inicio de sesión busca el usuario por nombre y verifica la contraseña con bcrypt, igual que la web. Una falla de conexión se informa por separado. Las contraseñas nuevas se guardan con hash, y un hash existente no se vuelve a cifrar al editar.

La lectura de cursos usa la estructura actual de la base, sin columna division. La división se quitó del modelo y de las pantallas. Consultá MODULOS_JAVA.md para Cursos y Facturación.

Pruebas: tests/PruebaLogin.java. Con --base también verifica las cuentas y la carga inicial en Torga; solo realiza consultas de lectura.

Biblioteca: https://github.com/patrickfav/bcrypt (Apache 2.0).

