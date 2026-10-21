<?php
session_start();
require_once dirname(__DIR__) . '/clases/database.php';
require_once dirname(__DIR__) . '/clases/dao/usuarioDAO.php';
require_once dirname(__DIR__) . '/clases/dao/calificacionDAO.php';

$conexion = databaseConnection();
$usuario = (new UsuarioDAO($conexion))->obtenerSesionActual();
$calificacionDAO = new CalificacionDAO($conexion);
$error = '';
$busqueda = trim((string) ($_GET['q'] ?? ''));
if (!isset($_SESSION['calificaciones_csrf'])) {
    $_SESSION['calificaciones_csrf'] = bin2hex(random_bytes(32));
}
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $token = is_string($_POST['csrf_token'] ?? null) ? $_POST['csrf_token'] : '';
    if (!hash_equals($_SESSION['calificaciones_csrf'], $token)) {
        $error = 'La sesión venció. Recargá la página e intentá nuevamente.';
    } elseif (!$usuario['isAdmin'] && !$usuario['isTeacher']) {
        http_response_code(403);
        exit('No tenés permisos para modificar calificaciones.');
    } else {
        try {
            $action = $_POST['action'] ?? '';
            if ($action === 'save') {
                $calificacionDAO->guardarCalificacion((int) ($_POST['id_calificacion'] ?? 0), $_POST, $usuario);
            } elseif ($action === 'delete' && $usuario['isAdmin']) {
                $calificacionDAO->eliminarCalificacion((int) ($_POST['id_calificacion'] ?? 0));
            } else {
                http_response_code(403);
                throw new InvalidArgumentException('La operación solicitada no está permitida.');
            }
            header('Location: calificaciones.php');
            exit;
        } catch (InvalidArgumentException $exception) {
            $error = $exception->getMessage();
        } catch (Throwable $exception) {
            error_log('Grade module operation failed: ' . $exception->getMessage());
            $error = 'No se pudo completar la operación.';
        }
    }
}
$registros = $calificacionDAO->listarCalificacionesDeUsuario($usuario, $busqueda);
$editar = isset($_GET['editar']) ? $calificacionDAO->obtenerCalificacionDeUsuario((int) $_GET['editar'], $usuario) : null;
if (isset($_GET['editar']) && $editar === null) {
    http_response_code(404);
    $error = 'La calificación no existe o no pertenece a tus cursos.';
}
$opciones = $editar !== null
    ? $calificacionDAO->opcionesDeCalificacion($usuario, (int) $editar['id_inscripcion'], (int) $editar['id_columna_notas'])
    : ($usuario['isAdmin'] ? $calificacionDAO->opcionesDeCalificacion($usuario) : ['inscripciones' => [], 'evaluaciones' => []]);
$mostrarFormulario = $editar !== null || ($usuario['isAdmin'] && isset($_GET['nuevo']));
?>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Academia Integral XXI - Calificaciones</title>
  <link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"><link rel="stylesheet" href="estilos/styles.css">
</head>
<body class="app">
  <aside class="sidebar">
    <div class="brand"><img class="brand-mark" src="../img/logo.png" alt="Academia Integral XXI"><span class="brand-text"><span>Academia</span><small>Integral XXI</small></span></div>
    <nav class="nav" aria-label="Navegación principal">
      <a class="nav-link" href="cursos.php"><i class="bi bi-mortarboard"></i><span>Cursos</span></a>
      <a class="nav-link active" href="calificaciones.php"><i class="bi bi-journal-check"></i><span>Calificaciones</span></a>
      <?php if ($usuario['isAdmin']): ?><a class="nav-link" href="inscripciones.php"><i class="bi bi-person-plus"></i><span>Inscripciones</span></a><?php endif; ?>
      <a class="nav-link" href="materias.php"><i class="bi bi-book"></i><span>Materias</span></a>
      <?php if ($usuario['isAdmin']): ?><a class="nav-link" href="admin.php"><i class="bi bi-gear"></i><span>Administración</span></a><?php endif; ?>
    </nav>
  </aside>
  <div class="main-panel">
    <header class="topbar"><span></span><div class="topbar-tools"><div class="user"><div class="avatar"><i class="bi bi-person"></i></div><div class="user-meta"><strong><?= htmlspecialchars($usuario['name'], ENT_QUOTES, 'UTF-8') ?></strong><small><?= htmlspecialchars($usuario['role'], ENT_QUOTES, 'UTF-8') ?></small></div></div><form method="post" action="logout.php"><input type="hidden" name="csrf_token" value="<?= htmlspecialchars($_SESSION['logout_csrf'] ??= bin2hex(random_bytes(32)), ENT_QUOTES, 'UTF-8') ?>"><button class="logout-button" type="submit"><i class="bi bi-box-arrow-right"></i><span>Cerrar sesión</span></button></form></div></header>
    <main class="content">
      <div class="page-head"><div><h1>Calificaciones</h1><p>Consultá el desempeño académico de tus cursos.</p></div><?php if ($usuario['isAdmin']): ?><a class="primary-btn" href="calificaciones.php?nuevo=1">Nueva calificación</a><?php endif; ?></div>
      <?php if ($error !== ''): ?><div class="message error" role="alert"><?= htmlspecialchars($error, ENT_QUOTES, 'UTF-8') ?></div><?php endif; ?>
      <?php if ($mostrarFormulario && ($usuario['isAdmin'] || $usuario['isTeacher'])): ?>
        <?php $values = $editar ?? ['id_calificacion' => 0, 'id_inscripcion' => '', 'id_columna_notas' => '', 'nota' => '', 'observaciones' => '']; ?>
        <section class="panel-card module-form-card"><div class="card-header"><h2><?= $editar ? 'Editar calificación' : 'Nueva calificación' ?></h2><a class="filter-chip" href="calificaciones.php">Cerrar</a></div>
          <form method="post" class="module-form">
            <input type="hidden" name="csrf_token" value="<?= htmlspecialchars($_SESSION['calificaciones_csrf'], ENT_QUOTES, 'UTF-8') ?>"><input type="hidden" name="action" value="save"><input type="hidden" name="id_calificacion" value="<?= (int) $values['id_calificacion'] ?>">
            <?php foreach (['id_inscripcion' => ['Alumno / curso', $opciones['inscripciones']], 'id_columna_notas' => ['Evaluación', $opciones['evaluaciones']]] as $field => [$label, $items]): ?>
              <label class="module-field"><span><?= $label ?></span><select name="<?= $field ?>" required <?= $usuario['isTeacher'] ? 'disabled' : '' ?>><option value="">Seleccioná</option><?php foreach ($items as $item): ?><option value="<?= (int) $item['id'] ?>" <?= (string) $values[$field] === (string) $item['id'] ? 'selected' : '' ?>><?= htmlspecialchars($item['etiqueta'], ENT_QUOTES, 'UTF-8') ?></option><?php endforeach; ?></select></label>
              <?php if ($usuario['isTeacher']): ?><input type="hidden" name="<?= $field ?>" value="<?= (int) $values[$field] ?>"><?php endif; ?>
            <?php endforeach; ?>
            <label class="module-field"><span>Nota</span><input type="number" name="nota" min="0" step="0.01" required value="<?= htmlspecialchars((string) $values['nota'], ENT_QUOTES, 'UTF-8') ?>"></label>
            <label class="module-field"><span>Observaciones</span><textarea name="observaciones" maxlength="255"><?= htmlspecialchars((string) $values['observaciones'], ENT_QUOTES, 'UTF-8') ?></textarea></label>
            <?php if ($editar): ?><label class="module-field"><span>Motivo de corrección (si cambia la nota)</span><textarea name="motivo_correccion" maxlength="255"></textarea></label><?php endif; ?>
            <div class="module-form-actions"><button class="primary-btn" type="submit">Guardar</button><a class="filter-chip" href="calificaciones.php">Cancelar</a></div>
          </form>
        </section>
      <?php endif; ?>
      <section class="table-card"><form class="list-filters" method="get"><label class="search"><input type="search" name="q" placeholder="Buscar calificaciones" value="<?= htmlspecialchars($busqueda, ENT_QUOTES, 'UTF-8') ?>"></label><button class="filter-chip active">Buscar</button></form>
        <div class="table-wrap"><table><thead><tr><th>Alumno</th><th>Curso / materia</th><th>Evaluación</th><th>Nota</th><th>Máximo</th><th>Fecha</th><th>Correcciones</th><?php if ($usuario['isAdmin'] || $usuario['isTeacher']): ?><th>Acciones</th><?php endif; ?></tr></thead><tbody>
          <?php foreach ($registros as $fila): ?><tr><td><?= htmlspecialchars($fila['alumno'], ENT_QUOTES, 'UTF-8') ?></td><td><?= htmlspecialchars($fila['curso'], ENT_QUOTES, 'UTF-8') ?></td><td><?= htmlspecialchars($fila['evaluacion'], ENT_QUOTES, 'UTF-8') ?></td><td><?= htmlspecialchars((string) $fila['nota'], ENT_QUOTES, 'UTF-8') ?></td><td><?= htmlspecialchars((string) $fila['nota_maxima'], ENT_QUOTES, 'UTF-8') ?></td><td><?= htmlspecialchars((string) $fila['fecha_carga'], ENT_QUOTES, 'UTF-8') ?></td><td><?= (int) $fila['correcciones'] ?></td><?php if ($usuario['isAdmin'] || $usuario['isTeacher']): ?><td><div class="actions"><a class="filter-chip" href="calificaciones.php?editar=<?= (int) $fila['id_calificacion'] ?>">Editar</a><?php if ($usuario['isAdmin']): ?><form method="post" onsubmit="return confirm('¿Eliminar esta calificación?')"><input type="hidden" name="csrf_token" value="<?= htmlspecialchars($_SESSION['calificaciones_csrf'], ENT_QUOTES, 'UTF-8') ?>"><input type="hidden" name="action" value="delete"><input type="hidden" name="id_calificacion" value="<?= (int) $fila['id_calificacion'] ?>"><button class="icon-btn small delete" type="submit">Eliminar</button></form><?php endif; ?></div></td><?php endif; ?></tr><?php endforeach; ?>
          <?php if ($registros === []): ?><tr><td colspan="<?= ($usuario['isAdmin'] || $usuario['isTeacher']) ? 8 : 7 ?>" class="empty-state">No hay calificaciones para mostrar.</td></tr><?php endif; ?>
        </tbody></table></div>
      </section>
    </main>
  </div>
</body>
</html>
