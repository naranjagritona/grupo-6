<?php
session_start();
require_once dirname(__DIR__) . '/clases/database.php';
require_once dirname(__DIR__) . '/clases/dao/usuarioDAO.php';
require_once dirname(__DIR__) . '/clases/dao/alumnoDAO.php';
require_once dirname(__DIR__) . '/clases/dao/docenteDAO.php';
require_once dirname(__DIR__) . '/clases/dao/log_usuarioDAO.php';
require_once dirname(__DIR__) . '/clases/dao/cursoDAO.php';
require_once dirname(__DIR__) . '/clases/dao/inscripcionDAO.php';

require_once dirname(__DIR__) . '/clases/php/usuario.php';
require_once dirname(__DIR__) . '/clases/php/alumno.php';
require_once dirname(__DIR__) . '/clases/php/docente.php';
require_once dirname(__DIR__) . '/clases/php/responsable.php';
require_once dirname(__DIR__) . '/clases/php/log_usuario.php';
require_once dirname(__DIR__) . '/clases/php/curso.php';
require_once dirname(__DIR__) . '/clases/php/inscripcion.php';

$conexion = databaseConnection();
$usuarioDao = new UsuarioDAO($conexion);
$alumnoDao = new AlumnoDAO($conexion);
$docenteDao = new DocenteDAO($conexion);
$logDao = new LogUsuarioDAO($conexion);
$cursoDao = new CursoDAO($conexion);
$inscripcionDao = new InscripcionDAO($conexion);
$usuarioActual = $usuarioDao->obtenerSesionActual();
if (!$usuarioActual['isAdmin']) {
    http_response_code(403);
    exit('No tenés permisos para acceder a este módulo.');
}

function registrarAccionAdministrativa(LogUsuarioDAO $logDao, string $accion, string $modulo, string $detalle): void
{
    try {
        $logDao->insertar(new LogUsuario(
            0,
            (int) $_SESSION['id_usuario'],
            $accion,
            $modulo,
            '',
            $detalle,
            date('Y-m-d H:i:s')
        ));
    } catch (Throwable $exception) {
        error_log('Admin audit log failed: ' . $exception->getMessage());
    }
}

$mensaje = "";
$error = "";
if (!isset($_SESSION['admin_csrf'])) {
    $_SESSION['admin_csrf'] = bin2hex(random_bytes(32));
}

// Procesamiento de formularios POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $accion = $_POST['accion'] ?? '';
    $token = is_string($_POST['csrf_token'] ?? null) ? $_POST['csrf_token'] : '';
    if (!hash_equals($_SESSION['admin_csrf'], $token)) {
        $error = 'La sesión del formulario venció. Recargá la página e intentá nuevamente.';
    } else {
        try {
            if (in_array($accion, ['crear_alumno', 'actualizar_alumno'], true)) {
                $datos = [
                    'nombre_usuario' => trim($_POST['nombre_usuario'] ?? ''),
                    'contrasena' => $_POST['contraseña'] ?? '',
                    'nombre' => trim($_POST['alumno_nombre'] ?? ''),
                    'apellido' => trim($_POST['alumno_apellido'] ?? ''),
                    'dni' => trim($_POST['alumno_dni'] ?? ''),
                    'resp_nombre' => trim($_POST['resp_nombre'] ?? ''),
                    'resp_apellido' => trim($_POST['resp_apellido'] ?? ''),
                    'resp_dni' => trim($_POST['resp_dni'] ?? ''),
                    'resp_relacion' => trim($_POST['resp_relacion'] ?? ''),
                ];
                if ($accion === 'crear_alumno') {
                    $alumnoDao->crearAdmin($datos);
                    registrarAccionAdministrativa($logDao, 'CREAR', 'alumno', 'Alta de alumno: ' . $datos['nombre'] . ' ' . $datos['apellido']);
                    $mensaje = 'Alumno registrado. La matrícula se asignó automáticamente.';
                } else {
                    $alumnoDao->actualizarAdmin((int) ($_POST['id_alumno'] ?? 0), $datos);
                    registrarAccionAdministrativa($logDao, 'MODIFICAR', 'alumno', 'Actualización de alumno: ' . $datos['nombre'] . ' ' . $datos['apellido']);
                    $mensaje = 'Los datos del alumno se actualizaron correctamente.';
                }
            } elseif (in_array($accion, ['crear_docente', 'actualizar_docente'], true)) {
                $datos = [
                    'nombre_usuario' => trim($_POST['doc_nombre_usuario'] ?? ''),
                    'contrasena' => $_POST['doc_contraseña'] ?? '',
                    'nombre' => trim($_POST['doc_nombre'] ?? ''),
                    'apellido' => trim($_POST['doc_apellido'] ?? ''),
                    'dni' => trim($_POST['doc_dni'] ?? ''),
                ];
                if ($accion === 'crear_docente') {
                    $docenteDao->crearAdmin($datos);
                    registrarAccionAdministrativa($logDao, 'CREAR', 'docente', 'Alta de docente: ' . $datos['nombre'] . ' ' . $datos['apellido']);
                    $mensaje = 'Docente registrado. El legajo se asignó automáticamente.';
                } else {
                    $docenteDao->actualizarAdmin((int) ($_POST['id_docente'] ?? 0), $datos);
                    registrarAccionAdministrativa($logDao, 'MODIFICAR', 'docente', 'Actualización de docente: ' . $datos['nombre'] . ' ' . $datos['apellido']);
                    $mensaje = 'Los datos del docente se actualizaron correctamente.';
                }
            } elseif ($accion === 'dar_baja_alumno' || $accion === 'dar_baja_docente') {
                $tipo = $accion === 'dar_baja_alumno' ? 'alumno' : 'docente';
                $id = (int) ($_POST['id'] ?? 0);
                if ($tipo === 'alumno') {
                    $alumnoDao->darDeBajaAdmin($id);
                } else {
                    $docenteDao->darDeBajaAdmin($id);
                }
                registrarAccionAdministrativa($logDao, 'BAJA', $tipo, 'Baja administrativa del registro #' . $id);
                $mensaje = 'La cuenta seleccionada quedó inactiva.';
            } else {
                throw new InvalidArgumentException('La operación solicitada no es válida.');
            }
        } catch (InvalidArgumentException $exception) {
            $error = $exception->getMessage();
        } catch (PDOException $exception) {
            error_log('Admin database operation failed: ' . $exception->getMessage());
            $error = 'No se pudo completar la operación. Verificá que los datos no estén duplicados e intentá nuevamente.';
        } catch (Throwable $exception) {
            error_log('Admin operation failed: ' . $exception->getMessage());
            $error = 'No se pudo completar la operación. Intentá nuevamente.';
        }
    }
}

// Estadísticas para las tarjetas generales usando el DAO
$totalAlumnos = count($alumnoDao->listarTodos());
$totalDocentes = count($docenteDao->listarTodos());
$totalCursos = count($cursoDao->listarTodos());
$totalInscripciones = count($inscripcionDao->listarTodos());
$totalUsuarios = count($usuarioDao->listarTodos());
$listaLogs = $logDao->listarLogConUsuarios(25);
$alumnosGestion = $alumnoDao->listarAdmin();
$docentesGestion = $docenteDao->listarAdmin();
$alumnoEditar = null;
$docenteEditar = null;
if (isset($_GET['editar_alumno'])) {
    foreach ($alumnosGestion as $registro) {
        if ((int) $registro['id_alumno'] === (int) $_GET['editar_alumno']) {
            $alumnoEditar = $registro;
            break;
        }
    }
}
if (isset($_GET['editar_docente'])) {
    foreach ($docentesGestion as $registro) {
        if ((int) $registro['id_docente'] === (int) $_GET['editar_docente']) {
            $docenteEditar = $registro;
            break;
        }
    }
}
?>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Academia Integral XXI - Administración</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
  <link rel="stylesheet" href="estilos/styles.css">
  <link rel="stylesheet" href="estilos/admin_estilo.css">
</head>
<body class="app">
  <aside class="sidebar">
    <div class="brand"><img class="brand-mark" src="../img/logo.png" alt="Academia Integral XXI"><span class="brand-text"><span>Academia</span><small>Integral XXI</small></span></div>
    <nav class="nav" aria-label="Navegación principal">
      <a class="nav-link" href="cursos.php"><i class="bi bi-mortarboard" aria-hidden="true"></i><span>Cursos</span></a>
      <a class="nav-link" href="calificaciones.php"><i class="bi bi-journal-check" aria-hidden="true"></i><span>Calificaciones</span></a>
      <a class="nav-link" href="inscripciones.php"><i class="bi bi-person-plus" aria-hidden="true"></i><span>Inscripciones</span></a>
      <a class="nav-link" href="materias.php"><i class="bi bi-book" aria-hidden="true"></i><span>Materias</span></a>
      <a class="nav-link active" href="admin.php"><i class="bi bi-gear" aria-hidden="true"></i><span>Administración</span></a>
    </nav>
  </aside>
  <div class="main-panel">
    <header class="topbar"><span></span><div class="topbar-tools">
      <div class="user"><div class="avatar" aria-hidden="true"><i class="bi bi-person"></i></div><div class="user-meta"><strong><?= htmlspecialchars($usuarioActual['name'], ENT_QUOTES, 'UTF-8') ?></strong><small><?= htmlspecialchars($usuarioActual['role'], ENT_QUOTES, 'UTF-8') ?></small></div></div>
      <form method="post" action="logout.php"><input type="hidden" name="csrf_token" value="<?= htmlspecialchars($_SESSION['logout_csrf'] ??= bin2hex(random_bytes(32)), ENT_QUOTES, 'UTF-8') ?>"><button class="logout-button" type="submit"><i class="bi bi-box-arrow-right" aria-hidden="true"></i><span>Cerrar sesión</span></button></form>
    </div></header>
    <main class="content">
  <!-- Panel Principal -->
      <div class="page-head">
        <div>
          <h1>Gestión de Administración</h1>
          <p>Control centralizado de altas de usuarios (alumnos y docentes) y registro de auditoría (logs).</p>
        </div>
      </div>

      <?php if (!empty($mensaje)): ?>
        <div class="mensaje-alerta exito"><?= htmlspecialchars($mensaje) ?></div>
      <?php endif; ?>
      <?php if (!empty($error)): ?>
        <div class="mensaje-alerta error"><?= htmlspecialchars($error) ?></div>
      <?php endif; ?>

      <!-- Tarjetas de Información General de Módulos y Usuarios -->
      <section class="admin-cards-grid">
        <div class="admin-card">
          <div class="card-title">Alumnos Totales</div>
          <div class="card-number"><?= $totalAlumnos ?></div>
        </div>
        <div class="admin-card">
          <div class="card-title">Docentes Totales</div>
          <div class="card-number"><?= $totalDocentes ?></div>
        </div>
        <div class="admin-card">
          <div class="card-title">Cursos Activos</div>
          <div class="card-number"><?= $totalCursos ?></div>
        </div>
        <div class="admin-card">
          <div class="card-title">Inscripciones</div>
          <div class="card-number"><?= $totalInscripciones ?></div>
        </div>
        <div class="admin-card">
          <div class="card-title">Cuentas de Usuarios</div>
          <div class="card-number"><?= $totalUsuarios ?></div>
        </div>
        <div class="admin-card">
          <div class="card-title">Registros en Log</div>
          <div class="card-number"><?= count($listaLogs) ?></div>
        </div>
      </section>

      <!-- Formularios de Creación de Usuarios (Desplegables) -->
      <?php if ($alumnoEditar !== null): ?>
        <section class="admin-panel-card">
          <h2>Editar alumno</h2>
          <form method="POST" action="admin.php?editar_alumno=<?= (int) $alumnoEditar['id_alumno'] ?>" class="admin-form">
            <input type="hidden" name="csrf_token" value="<?= htmlspecialchars($_SESSION['admin_csrf'], ENT_QUOTES, 'UTF-8') ?>">
            <input type="hidden" name="accion" value="actualizar_alumno">
            <input type="hidden" name="id_alumno" value="<?= (int) $alumnoEditar['id_alumno'] ?>">
            <div class="form-row">
              <div class="form-group"><label>Usuario</label><input name="nombre_usuario" required value="<?= htmlspecialchars($alumnoEditar['nombre_usuario'], ENT_QUOTES, 'UTF-8') ?>"></div>
              <div class="form-group"><label>Nueva contraseña (opcional)</label><input type="password" name="contraseña" minlength="8"></div>
              <div class="form-group"><label>Nombre</label><input name="alumno_nombre" required value="<?= htmlspecialchars($alumnoEditar['nombre'], ENT_QUOTES, 'UTF-8') ?>"></div>
              <div class="form-group"><label>Apellido</label><input name="alumno_apellido" required value="<?= htmlspecialchars($alumnoEditar['apellido'], ENT_QUOTES, 'UTF-8') ?>"></div>
              <div class="form-group"><label>DNI</label><input name="alumno_dni" required value="<?= htmlspecialchars($alumnoEditar['dni'], ENT_QUOTES, 'UTF-8') ?>"></div>
              <div class="form-group"><label>Matrícula asignada</label><input readonly value="<?= htmlspecialchars($alumnoEditar['matricula'], ENT_QUOTES, 'UTF-8') ?>"></div>
              <div class="form-group"><label>Nombre del responsable</label><input name="resp_nombre" required value="<?= htmlspecialchars($alumnoEditar['responsable_nombre'], ENT_QUOTES, 'UTF-8') ?>"></div>
              <div class="form-group"><label>Apellido del responsable</label><input name="resp_apellido" required value="<?= htmlspecialchars($alumnoEditar['responsable_apellido'], ENT_QUOTES, 'UTF-8') ?>"></div>
              <div class="form-group"><label>DNI del responsable</label><input name="resp_dni" required value="<?= htmlspecialchars($alumnoEditar['responsable_dni'], ENT_QUOTES, 'UTF-8') ?>"></div>
              <div class="form-group"><label>Relación</label><input name="resp_relacion" required value="<?= htmlspecialchars($alumnoEditar['tipo_relacion'], ENT_QUOTES, 'UTF-8') ?>"></div>
            </div>
            <button class="btn-admin primary" type="submit">Guardar cambios</button>
            <a class="btn-admin" href="admin.php">Cancelar</a>
          </form>
        </section>
      <?php endif; ?>
      <?php if ($docenteEditar !== null): ?>
        <section class="admin-panel-card">
          <h2>Editar docente</h2>
          <form method="POST" action="admin.php?editar_docente=<?= (int) $docenteEditar['id_docente'] ?>" class="admin-form">
            <input type="hidden" name="csrf_token" value="<?= htmlspecialchars($_SESSION['admin_csrf'], ENT_QUOTES, 'UTF-8') ?>">
            <input type="hidden" name="accion" value="actualizar_docente">
            <input type="hidden" name="id_docente" value="<?= (int) $docenteEditar['id_docente'] ?>">
            <div class="form-row">
              <div class="form-group"><label>Usuario</label><input name="doc_nombre_usuario" required value="<?= htmlspecialchars($docenteEditar['nombre_usuario'], ENT_QUOTES, 'UTF-8') ?>"></div>
              <div class="form-group"><label>Nueva contraseña (opcional)</label><input type="password" name="doc_contraseña" minlength="8"></div>
              <div class="form-group"><label>Nombre</label><input name="doc_nombre" required value="<?= htmlspecialchars($docenteEditar['nombre'], ENT_QUOTES, 'UTF-8') ?>"></div>
              <div class="form-group"><label>Apellido</label><input name="doc_apellido" required value="<?= htmlspecialchars($docenteEditar['apellido'], ENT_QUOTES, 'UTF-8') ?>"></div>
              <div class="form-group"><label>DNI</label><input name="doc_dni" required value="<?= htmlspecialchars($docenteEditar['dni'], ENT_QUOTES, 'UTF-8') ?>"></div>
              <div class="form-group"><label>Legajo asignado</label><input readonly value="<?= htmlspecialchars($docenteEditar['legajo'], ENT_QUOTES, 'UTF-8') ?>"></div>
            </div>
            <button class="btn-admin primary" type="submit">Guardar cambios</button>
            <a class="btn-admin" href="admin.php">Cancelar</a>
          </form>
        </section>
      <?php endif; ?>
      <div class="admin-forms-grid">
        
        <!-- Formulario Alta Alumno -->
        <div id="card-alumno" class="admin-panel-card formulario-colapsado">
          <button type="button" class="btn-toggle-formulario" onclick="toggleFormulario('alumno')">
            <span>➕ Registrar Nuevo Alumno y Responsable</span>
            <span class="icono-flecha">▼</span>
          </button>
          
          <p class="panel-desc">Ingresa los datos del alumno, su responsable y define sus credenciales de acceso.</p>
          <form method="POST" action="admin.php" class="admin-form">
            <input type="hidden" name="csrf_token" value="<?= htmlspecialchars($_SESSION['admin_csrf'], ENT_QUOTES, 'UTF-8') ?>">
            <input type="hidden" name="accion" value="crear_alumno">
            
            <h3>Credenciales de Acceso</h3>
            <div class="form-row">
              <div class="form-group">
                <label>Nombre de Usuario:</label>
                <input type="text" name="nombre_usuario" placeholder="Ej. alumno.perez" required maxlength="200">
              </div>
              <div class="form-group">
                <label>Contraseña:</label>
                <input type="password" name="contraseña" placeholder="Mínimo 8 caracteres" required minlength="8" maxlength="200">
              </div>
            </div>

            <h3>Datos del Alumno</h3>
            <div class="form-row">
              <div class="form-group">
                <label>Nombre:</label>
                <input type="text" name="alumno_nombre" required maxlength="255">
              </div>
              <div class="form-group">
                <label>Apellido:</label>
                <input type="text" name="alumno_apellido" required maxlength="255">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>DNI:</label>
                <input type="text" name="alumno_dni" required maxlength="20">
              </div>
              <div class="form-group">
                <label>Matrícula:</label>
                <input type="text" value="Se asigna automáticamente" readonly>
              </div>
            </div>

            <h3>Datos del Responsable</h3>
            <div class="form-row">
              <div class="form-group">
                <label>Nombre Responsable:</label>
                <input type="text" name="resp_nombre" required maxlength="255">
              </div>
              <div class="form-group">
                <label>Apellido Responsable:</label>
                <input type="text" name="resp_apellido" required maxlength="255">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>DNI Responsable:</label>
                <input type="text" name="resp_dni" required maxlength="20">
              </div>
              <div class="form-group">
                <label>Tipo de Relación:</label>
                <input type="text" name="resp_relacion" placeholder="Padre, Madre, Tutor" required maxlength="100">
              </div>
            </div>

            <button type="submit" class="btn-admin primary">Registrar Alumno</button>
          </form>
        </div>

        <!-- Formulario Alta Docente -->
        <div id="card-docente" class="admin-panel-card formulario-colapsado">
          <button type="button" class="btn-toggle-formulario" onclick="toggleFormulario('docente')">
            <span>➕ Registrar Nuevo Docente</span>
            <span class="icono-flecha">▼</span>
          </button>

          <p class="panel-desc">Registra un nuevo docente asignando sus datos institucionales y de acceso.</p>
          <form method="POST" action="admin.php" class="admin-form">
            <input type="hidden" name="csrf_token" value="<?= htmlspecialchars($_SESSION['admin_csrf'], ENT_QUOTES, 'UTF-8') ?>">
            <input type="hidden" name="accion" value="crear_docente">
            
            <h3>Credenciales de Acceso</h3>
            <div class="form-row">
              <div class="form-group">
                <label>Nombre de Usuario:</label>
                <input type="text" name="doc_nombre_usuario" placeholder="Ej. docente.gomez" required maxlength="200">
              </div>
              <div class="form-group">
                <label>Contraseña:</label>
                <input type="password" name="doc_contraseña" placeholder="Mínimo 8 caracteres" required minlength="8" maxlength="200">
              </div>
            </div>

            <h3>Datos del Docente</h3>
            <div class="form-row">
              <div class="form-group">
                <label>Nombre:</label>
                <input type="text" name="doc_nombre" required maxlength="200">
              </div>
              <div class="form-group">
                <label>Apellido:</label>
                <input type="text" name="doc_apellido" required maxlength="200">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>DNI:</label>
                <input type="text" name="doc_dni" required maxlength="20">
              </div>
              <div class="form-group"><label>Legajo</label><input type="text" value="Se asigna automáticamente" readonly></div>
            </div>

            <button type="submit" class="btn-admin primary" style="margin-top: 2rem;">Registrar Docente</button>
          </form>
        </div>

      </div>

      <section class="admin-panel-card" style="margin-top: 2rem;">
        <h2>Alumnos</h2>
        <div class="tabla-contenedor">
          <table class="admin-table">
            <thead><tr><th>Alumno</th><th>Usuario</th><th>DNI</th><th>Matrícula</th><th>Estado</th><th>Acciones</th></tr></thead>
            <tbody>
              <?php foreach ($alumnosGestion as $alumno): ?>
                <tr>
                  <td><?= htmlspecialchars($alumno['apellido'] . ', ' . $alumno['nombre'], ENT_QUOTES, 'UTF-8') ?></td>
                  <td><?= htmlspecialchars($alumno['nombre_usuario'], ENT_QUOTES, 'UTF-8') ?></td>
                  <td><?= htmlspecialchars($alumno['dni'], ENT_QUOTES, 'UTF-8') ?></td>
                  <td><?= htmlspecialchars($alumno['matricula'], ENT_QUOTES, 'UTF-8') ?></td>
                  <td><?= htmlspecialchars($alumno['estado'], ENT_QUOTES, 'UTF-8') ?></td>
                  <td>
                    <a class="btn-admin" href="admin.php?editar_alumno=<?= (int) $alumno['id_alumno'] ?>">Editar</a>
                    <?php if (strcasecmp((string) $alumno['estado'], 'Inactivo') !== 0): ?>
                      <form method="POST" action="admin.php" style="display:inline" onsubmit="return confirm('¿Dar de baja a este alumno?');">
                        <input type="hidden" name="csrf_token" value="<?= htmlspecialchars($_SESSION['admin_csrf'], ENT_QUOTES, 'UTF-8') ?>">
                        <input type="hidden" name="accion" value="dar_baja_alumno">
                        <input type="hidden" name="id" value="<?= (int) $alumno['id_alumno'] ?>">
                        <button class="btn-admin" type="submit">Dar de baja</button>
                      </form>
                    <?php endif; ?>
                  </td>
                </tr>
              <?php endforeach; ?>
              <?php if ($alumnosGestion === []): ?><tr><td colspan="6" class="empty-state">No hay alumnos registrados.</td></tr><?php endif; ?>
            </tbody>
          </table>
        </div>
      </section>

      <section class="admin-panel-card" style="margin-top: 2rem;">
        <h2>Docentes</h2>
        <div class="tabla-contenedor">
          <table class="admin-table">
            <thead><tr><th>Docente</th><th>Usuario</th><th>DNI</th><th>Legajo</th><th>Estado</th><th>Acciones</th></tr></thead>
            <tbody>
              <?php foreach ($docentesGestion as $docente): ?>
                <tr>
                  <td><?= htmlspecialchars($docente['apellido'] . ', ' . $docente['nombre'], ENT_QUOTES, 'UTF-8') ?></td>
                  <td><?= htmlspecialchars($docente['nombre_usuario'], ENT_QUOTES, 'UTF-8') ?></td>
                  <td><?= htmlspecialchars($docente['dni'], ENT_QUOTES, 'UTF-8') ?></td>
                  <td><?= htmlspecialchars($docente['legajo'], ENT_QUOTES, 'UTF-8') ?></td>
                  <td><?= htmlspecialchars($docente['estado'], ENT_QUOTES, 'UTF-8') ?></td>
                  <td>
                    <a class="btn-admin" href="admin.php?editar_docente=<?= (int) $docente['id_docente'] ?>">Editar</a>
                    <?php if (strcasecmp((string) $docente['estado'], 'Inactivo') !== 0): ?>
                      <form method="POST" action="admin.php" style="display:inline" onsubmit="return confirm('¿Dar de baja a este docente?');">
                        <input type="hidden" name="csrf_token" value="<?= htmlspecialchars($_SESSION['admin_csrf'], ENT_QUOTES, 'UTF-8') ?>">
                        <input type="hidden" name="accion" value="dar_baja_docente">
                        <input type="hidden" name="id" value="<?= (int) $docente['id_docente'] ?>">
                        <button class="btn-admin" type="submit">Dar de baja</button>
                      </form>
                    <?php endif; ?>
                  </td>
                </tr>
              <?php endforeach; ?>
              <?php if ($docentesGestion === []): ?><tr><td colspan="6" class="empty-state">No hay docentes registrados.</td></tr><?php endif; ?>
            </tbody>
          </table>
        </div>
      </section>

      <!-- Tabla de Log de Actividades de Usuarios -->
      <section class="admin-panel-card" style="margin-top: 2rem;">
        <h2>Registro de Actividades (Log de Usuarios)</h2>
        <p class="panel-desc">Auditoría en tiempo real de acciones, modificaciones de calificaciones, periodos y accesos.</p>
        
        <div class="tabla-contenedor">
          <table class="admin-table">
            <thead>
              <tr>
                <th>ID Log</th>
                <th>Usuario</th>
                <th>Rol</th>
                <th>Acción</th>
                <th>Campo / Módulo</th>
                <th>Detalle / Valor Nuevo</th>
                <th>Fecha y Hora</th>
              </tr>
            </thead>
            <tbody>
              <?php if (empty($listaLogs)): ?>
                <tr>
                  <td colspan="7" class="empty-state">No hay registros de actividad recientes.</td>
                </tr>
              <?php else: ?>
                <?php foreach ($listaLogs as $log): ?>
                  <tr>
                    <td>#<?= $log['id_log_usuario'] ?></td>
                    <td><strong><?= htmlspecialchars($log['nombre_usuario'] ?? 'Sistema / ID ' . $log['id_usuario']) ?></strong></td>
                    <td><span class="badge-rol"><?= htmlspecialchars($log['rol_nombre'] ?? 'Usuario') ?></span></td>
                    <td><span class="badge-accion <?= strtolower($log['accion']) ?>"><?= htmlspecialchars($log['accion']) ?></span></td>
                    <td><?= htmlspecialchars($log['campo_modificado']) ?></td>
                    <td><?= htmlspecialchars($log['valor_nuevo']) ?></td>
                    <td><?= htmlspecialchars($log['fecha_hora']) ?></td>
                  </tr>
                <?php endforeach; ?>
              <?php endif; ?>
            </tbody>
          </table>
        </div>
      </section>

  <script>
    function toggleFormulario(tipo) {
      const card = document.getElementById('card-' + tipo);
      card.classList.toggle('formulario-colapsado');
    }
    
    <?php if (!empty($error) || !empty($mensaje)): ?>
      <?php if (isset($_POST['accion']) && $_POST['accion'] === 'crear_alumno'): ?>
        document.getElementById('card-alumno').classList.remove('formulario-colapsado');
      <?php elseif (isset($_POST['accion']) && $_POST['accion'] === 'crear_docente'): ?>
        document.getElementById('card-docente').classList.remove('formulario-colapsado');
      <?php endif; ?>
    <?php endif; ?>
  </script>
    </main>
  </div>
</body>
</html>