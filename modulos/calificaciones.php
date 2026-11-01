<?php
session_start();
require_once dirname(__DIR__) . '/clases/databases.php';
require_once dirname(__DIR__) . '/clases/dao/usuarioDAO.php';
require_once dirname(__DIR__) . '/clases/dao/calificacionDAO.php';

$conexion = databaseConnection();
$usuario = (new UsuarioDAO($conexion))->obtenerSesionActual();
$calificacionDAO = new CalificacionDAO($conexion);
$error = '';
$busquedaEntrada = $_GET['q'] ?? '';
$busqueda = is_string($busquedaEntrada) ? trim($busquedaEntrada) : '';
$leerFiltroId = static function (string $nombre): ?int {
    $valor = filter_var($_GET[$nombre] ?? null, FILTER_VALIDATE_INT);
    return $valor !== false && $valor > 0 ? $valor : null;
};
$cursoFiltro = $leerFiltroId('curso');
$materiaFiltro = $leerFiltroId('materia');
$evaluacionFiltro = $leerFiltroId('evaluacion');
$notaMinima = filter_var($usuario['isAdmin'] ? ($_GET['nota_minima'] ?? '6') : '6', FILTER_VALIDATE_FLOAT);
$notaMinima = $notaMinima === false ? 6.0 : max(0.0, min(10.0, $notaMinima));
$desaprobadasPermitidas = filter_var($usuario['isAdmin'] ? ($_GET['desaprobadas'] ?? '0') : '0', FILTER_VALIDATE_INT);
$desaprobadasPermitidas = $desaprobadasPermitidas === false ? 0 : max(0, min(20, $desaprobadasPermitidas));
$mensajeExito = $_SESSION['calificaciones_mensaje'] ?? '';
unset($_SESSION['calificaciones_mensaje']);
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
            } elseif ($action === 'save_inline') {
                $calificacionDAO->guardarCalificacion((int) ($_POST['id_calificacion'] ?? 0), $_POST, $usuario);
                $_SESSION['calificaciones_mensaje'] = 'La nota se guardó correctamente.';
            } elseif ($action === 'save_evaluation') {
                $calificacionDAO->guardarEvaluacion((int) ($_POST['id_curso'] ?? 0), $_POST, $usuario);
                $_SESSION['calificaciones_mensaje'] = 'La evaluación se agregó correctamente.';
            } elseif ($action === 'delete' && $usuario['isAdmin']) {
                $calificacionDAO->eliminarCalificacion((int) ($_POST['id_calificacion'] ?? 0));
            } else {
                http_response_code(403);
                throw new InvalidArgumentException('La operación solicitada no está permitida.');
            }
            $destino = [];
            foreach (['curso', 'materia', 'evaluacion'] as $filtro) {
                $valor = filter_var($_POST['filtro_' . $filtro] ?? $_GET[$filtro] ?? null, FILTER_VALIDATE_INT);
                if ($valor !== false && $valor > 0) $destino[$filtro] = $valor;
            }
            if ($action === 'save_evaluation' && !isset($destino['curso'])) {
                $cursoNuevo = filter_var($_POST['id_curso'] ?? null, FILTER_VALIDATE_INT);
                if ($cursoNuevo !== false && $cursoNuevo > 0) $destino['curso'] = $cursoNuevo;
            }
            $terminoEntrada = $_POST['filtro_q'] ?? $_GET['q'] ?? '';
            $termino = is_string($terminoEntrada) ? trim($terminoEntrada) : '';
            if ($termino !== '') $destino['q'] = $termino;
            header('Location: calificaciones.php' . ($destino === [] ? '' : '?' . http_build_query($destino)));
            exit;
        } catch (InvalidArgumentException $exception) {
            $error = $exception->getMessage();
        } catch (Throwable $exception) {
            error_log('Grade module operation failed: ' . $exception->getMessage());
            $error = 'No se pudo completar la operación.';
        }
    }
}
$filtrosBase = $calificacionDAO->opcionesFiltrosCalificacion($usuario);
if ($cursoFiltro !== null && !in_array($cursoFiltro, array_map('intval', array_column($filtrosBase['cursos'], 'id')), true)) {
    $cursoFiltro = null;
}
$filtrosCurso = $calificacionDAO->opcionesFiltrosCalificacion($usuario, $cursoFiltro);
if ($materiaFiltro !== null && !in_array($materiaFiltro, array_map('intval', array_column($filtrosCurso['materias'], 'id')), true)) {
    $materiaFiltro = null;
}
$filtros = $calificacionDAO->opcionesFiltrosCalificacion($usuario, $cursoFiltro, $materiaFiltro);
if ($evaluacionFiltro !== null && !in_array($evaluacionFiltro, array_map('intval', array_column($filtros['evaluaciones'], 'id')), true)) {
    $evaluacionFiltro = null;
}
$registros = $calificacionDAO->listarCalificacionesDeUsuario(
    $usuario, $busqueda, $cursoFiltro, $materiaFiltro, $evaluacionFiltro
);
$planilla = $cursoFiltro === null ? [] : $calificacionDAO->listarPlanillaDeCurso(
    $cursoFiltro, $usuario, $materiaFiltro, $evaluacionFiltro
);
$promociones = $calificacionDAO->listarPromocionPorUsuario($usuario, $notaMinima, $desaprobadasPermitidas);
$editar = isset($_GET['editar']) ? $calificacionDAO->obtenerCalificacionDeUsuario((int) $_GET['editar'], $usuario) : null;
if (isset($_GET['editar']) && $editar === null) {
    http_response_code(404);
    $error = 'La calificación no existe o no pertenece a tus cursos.';
}
$opciones = $editar !== null
    ? $calificacionDAO->opcionesDeCalificacion($usuario, (int) $editar['id_inscripcion'], (int) $editar['id_columna_notas'])
    : ($usuario['isAdmin'] ? $calificacionDAO->opcionesDeCalificacion($usuario) : ['inscripciones' => [], 'evaluaciones' => []]);
$mostrarFormulario = $editar !== null || ($usuario['isAdmin'] && isset($_GET['nuevo']));
$mostrarFormularioEvaluacion = isset($_GET['nueva_evaluacion']) && ($usuario['isAdmin'] || $usuario['isTeacher']);
?>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Academia Integral XXI - Calificaciones</title>
  <link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"><link rel="stylesheet" href="estilos/styles.css"><link rel="stylesheet" href="estilos/calificaciones_estilo.css?v=002">
</head>
<body class="app calificaciones-page">
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
      <div class="page-head"><div><h1>Calificaciones</h1><p>Consultá y completá las notas de tus cursos.</p></div><div class="grade-head-actions"><?php if ($usuario['isAdmin'] || $usuario['isTeacher']): ?><a class="filter-chip" href="calificaciones.php?nueva_evaluacion=1<?= $cursoFiltro ? '&curso=' . $cursoFiltro : '' ?>">Agregar tema / evaluación</a><?php endif; ?><?php if ($usuario['isAdmin']): ?><a class="primary-btn" href="calificaciones.php?nuevo=1">Nueva calificación</a><?php endif; ?></div></div>
      <?php if ($error !== ''): ?><div class="message error" role="alert"><?= htmlspecialchars($error, ENT_QUOTES, 'UTF-8') ?></div><?php endif; ?>
      <?php if ($mensajeExito !== ''): ?><div class="message success" role="status"><?= htmlspecialchars($mensajeExito, ENT_QUOTES, 'UTF-8') ?></div><?php endif; ?>
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
      <?php if ($mostrarFormularioEvaluacion): ?>
        <section class="panel-card module-form-card"><div class="card-header"><div><h2>Nueva evaluación / columna</h2><p>El título aparecerá como columna en la planilla.</p></div><a class="filter-chip" href="calificaciones.php<?= $cursoFiltro ? '?curso=' . $cursoFiltro : '' ?>">Cerrar</a></div>
          <form method="post" class="module-form"><input type="hidden" name="csrf_token" value="<?= htmlspecialchars($_SESSION['calificaciones_csrf'], ENT_QUOTES, 'UTF-8') ?>"><input type="hidden" name="action" value="save_evaluation">
            <?php foreach (['curso' => $cursoFiltro, 'materia' => $materiaFiltro, 'evaluacion' => $evaluacionFiltro] as $filtro => $valor): ?><?php if ($valor): ?><input type="hidden" name="filtro_<?= $filtro ?>" value="<?= (int) $valor ?>"><?php endif; ?><?php endforeach; ?><input type="hidden" name="filtro_q" value="<?= htmlspecialchars($busqueda, ENT_QUOTES, 'UTF-8') ?>">
            <label class="module-field"><span>Curso</span><select name="id_curso" required><option value="">Seleccioná un curso</option><?php foreach ($filtros['cursos'] as $curso): ?><option value="<?= (int) $curso['id'] ?>" <?= $cursoFiltro === (int) $curso['id'] ? 'selected' : '' ?>><?= htmlspecialchars($curso['etiqueta'], ENT_QUOTES, 'UTF-8') ?></option><?php endforeach; ?></select></label>
            <label class="module-field"><span>Título / tema</span><input name="titulo" maxlength="200" required placeholder="Ej. Trabajo práctico 1"></label>
            <label class="module-field"><span>Tipo</span><select name="tipo"><?php foreach (['Evaluación', 'Trabajo Práctico', 'Recuperatorio', 'Otro'] as $tipo): ?><option><?= htmlspecialchars($tipo, ENT_QUOTES, 'UTF-8') ?></option><?php endforeach; ?></select></label>
            <label class="module-field"><span>Nota máxima</span><input type="number" name="nota_maxima" min="0.01" step="0.01" value="10" required></label>
            <label class="module-field"><span>Nota de aprobación</span><input type="number" name="nota_aprobacion" min="0" step="0.01" value="6" required></label>
            <label class="module-field"><span>Fecha</span><input type="date" name="fecha_evaluacion" value="<?= date('Y-m-d') ?>" required></label>
            <div class="module-form-actions"><button class="primary-btn" type="submit">Agregar columna</button><a class="filter-chip" href="calificaciones.php<?= $cursoFiltro ? '?curso=' . $cursoFiltro : '' ?>">Cancelar</a></div>
          </form>
        </section>
      <?php endif; ?>
      <?php if ($usuario['isAdmin'] || $usuario['isStudent']): ?><section class="panel-card promotion-card"><div class="card-header"><div><h2>Promoción de año</h2><p>Estado calculado por alumno, año, división y período lectivo.</p></div></div>
        <?php if ($usuario['isAdmin']): ?><form method="get" class="promotion-rules">
          <?php foreach (['curso' => $cursoFiltro, 'materia' => $materiaFiltro, 'evaluacion' => $evaluacionFiltro] as $filtro => $valor): ?><?php if ($valor): ?><input type="hidden" name="<?= $filtro ?>" value="<?= (int) $valor ?>"><?php endif; ?><?php endforeach; ?><input type="hidden" name="q" value="<?= htmlspecialchars($busqueda, ENT_QUOTES, 'UTF-8') ?>">
          <label class="module-field"><span>Nota mínima (escala 0–10)</span><input type="number" name="nota_minima" min="0" max="10" step="0.1" value="<?= htmlspecialchars((string) $notaMinima, ENT_QUOTES, 'UTF-8') ?>"></label>
          <label class="module-field"><span>Materias desaprobadas permitidas</span><input type="number" name="desaprobadas" min="0" max="20" step="1" value="<?= $desaprobadasPermitidas ?>"></label>
          <button class="filter-chip active" type="submit">Aplicar regla</button>
        </form><?php endif; ?>
        <p class="promotion-caption">Las notas se normalizan según el máximo de cada evaluación. Solo se decide cuando terminaron los cursos del período; las notas faltantes dejan la materia incompleta. Regla actual: promedio mínimo <?= number_format($notaMinima, 1, ',', '.') ?> y hasta <?= $desaprobadasPermitidas ?> materias desaprobadas<?= $usuario['isAdmin'] ? ' (ajustable por administración)' : '' ?>.</p>
        <div class="table-wrap promotion-table-wrap"><table class="promotion-table"><thead><tr><th>Alumno</th><th>Año / división</th><th>Período</th><th>Materias aprobadas</th><th>Desaprobadas</th><th>Sin calificar</th><th>Decisión</th><th>Detalle</th></tr></thead><tbody>
          <?php foreach ($promociones as $fila): ?><tr><td><?= htmlspecialchars($fila['alumno'], ENT_QUOTES, 'UTF-8') ?></td><td><?= htmlspecialchars($fila['anio'] . '° ' . $fila['division'], ENT_QUOTES, 'UTF-8') ?></td><td><?= htmlspecialchars($fila['periodo'], ENT_QUOTES, 'UTF-8') ?></td><td><?= (int) $fila['aprobadas'] ?> / <?= (int) $fila['cantidad_materias'] ?></td><td><?= (int) $fila['desaprobadas'] ?></td><td><?= (int) $fila['incompletas'] ?></td><td><span class="status-badge <?= $fila['estado'] === 'Promociona' ? 'success' : (in_array($fila['estado'], ['En curso', 'Incompleto'], true) ? 'warning' : 'danger') ?>"><?= htmlspecialchars($fila['estado'], ENT_QUOTES, 'UTF-8') ?></span></td><td><details class="promotion-detail"><summary>Ver materias</summary><ul><?php foreach ($fila['materias_detalle'] as $materia): ?><li><?= htmlspecialchars($materia['nombre'], ENT_QUOTES, 'UTF-8') ?>: <?= $materia['promedio'] === null ? 'sin notas' : number_format($materia['promedio'], 2, ',', '.') . '/10' ?></li><?php endforeach; ?></ul></details></td></tr><?php endforeach; ?>
          <?php if ($promociones === []): ?><tr><td colspan="8" class="empty-state">Todavía no hay alumnos inscriptos en cursos con año y período definidos.</td></tr><?php endif; ?>
        </tbody></table></div>
      </section><?php endif; ?>
      <section class="panel-card grade-filter-card"><form method="get" class="grade-filters">
        <label class="module-field"><span>Curso</span><select name="curso"><option value="">Todos los cursos</option><?php foreach ($filtros['cursos'] as $opcion): ?><option value="<?= (int) $opcion['id'] ?>" <?= $cursoFiltro === (int) $opcion['id'] ? 'selected' : '' ?>><?= htmlspecialchars($opcion['etiqueta'], ENT_QUOTES, 'UTF-8') ?></option><?php endforeach; ?></select></label>
        <label class="module-field"><span>Materia</span><select name="materia"><option value="">Todas las materias</option><?php foreach ($filtros['materias'] as $opcion): ?><option value="<?= (int) $opcion['id'] ?>" <?= $materiaFiltro === (int) $opcion['id'] ? 'selected' : '' ?>><?= htmlspecialchars($opcion['etiqueta'], ENT_QUOTES, 'UTF-8') ?></option><?php endforeach; ?></select></label>
        <label class="module-field"><span>Evaluación</span><select name="evaluacion"><option value="">Todas las evaluaciones</option><?php foreach ($filtros['evaluaciones'] as $opcion): ?><option value="<?= (int) $opcion['id'] ?>" <?= $evaluacionFiltro === (int) $opcion['id'] ? 'selected' : '' ?>><?= htmlspecialchars($opcion['etiqueta'], ENT_QUOTES, 'UTF-8') ?></option><?php endforeach; ?></select></label>
        <label class="module-field grade-search"><span>Buscar</span><input type="search" name="q" placeholder="Alumno, materia o evaluación" value="<?= htmlspecialchars($busqueda, ENT_QUOTES, 'UTF-8') ?>"></label>
        <div class="grade-filter-actions"><button class="filter-chip active" type="submit">Filtrar</button><a class="filter-chip" href="calificaciones.php">Limpiar</a></div>
      </form></section>

      <?php
      $columnasPlanilla = [];
      $alumnosPlanilla = [];
      $notasPlanilla = [];
      foreach ($planilla as $celda) {
          $idInscripcion = (int) $celda['id_inscripcion'];
          $alumnosPlanilla[$idInscripcion] = ['alumno' => $celda['alumno'], 'matricula' => $celda['matricula']];
          if ($celda['id_columna_notas'] !== null) {
              $idColumna = (int) $celda['id_columna_notas'];
              $columnasPlanilla[$idColumna] = $celda;
              if ($celda['id_calificacion'] !== null) $notasPlanilla[$idInscripcion . ':' . $idColumna] = $celda;
          }
      }
      ?>
      <section class="table-card grade-matrix-card"><div class="grade-matrix-heading"><div><h2>Planilla de calificaciones</h2><p>Elegí un curso para completar las notas directamente en la tabla.</p></div><?php if ($cursoFiltro): ?><span class="grade-count"><?= count($alumnosPlanilla) ?> alumno<?= count($alumnosPlanilla) === 1 ? '' : 's' ?></span><?php endif; ?></div>
        <?php if ($cursoFiltro === null): ?><p class="empty-state">Seleccioná un curso en los filtros para abrir su planilla.</p>
        <?php elseif ($columnasPlanilla === []): ?><p class="empty-state">Este curso todavía no tiene evaluaciones. Usá “Agregar tema / evaluación” para crear la primera columna.</p>
        <?php else: ?><div class="grade-matrix-wrap"><table class="grade-matrix"><thead><tr><th>Alumno</th><th>Matrícula</th><?php foreach ($columnasPlanilla as $idColumna => $columna): ?><th><?= htmlspecialchars($columna['titulo'], ENT_QUOTES, 'UTF-8') ?><small><?= htmlspecialchars($columna['tipo'], ENT_QUOTES, 'UTF-8') ?> · máx. <?= htmlspecialchars((string) $columna['nota_maxima'], ENT_QUOTES, 'UTF-8') ?></small></th><?php endforeach; ?></tr></thead><tbody>
          <?php foreach ($alumnosPlanilla as $idInscripcion => $alumno): ?><tr><td><?= htmlspecialchars($alumno['alumno'], ENT_QUOTES, 'UTF-8') ?></td><td><?= htmlspecialchars((string) $alumno['matricula'], ENT_QUOTES, 'UTF-8') ?></td><?php foreach ($columnasPlanilla as $idColumna => $columna): ?><?php $celda = $notasPlanilla[$idInscripcion . ':' . $idColumna] ?? null; $editable = ($usuario['isAdmin'] || $usuario['isTeacher']) && $columna['estado_evaluacion'] === 'Abierta'; ?><td><?php if ($editable): ?><form method="post" class="grade-cell-form"><input type="hidden" name="csrf_token" value="<?= htmlspecialchars($_SESSION['calificaciones_csrf'], ENT_QUOTES, 'UTF-8') ?>"><input type="hidden" name="action" value="save_inline"><input type="hidden" name="id_calificacion" value="<?= (int) ($celda['id_calificacion'] ?? 0) ?>"><input type="hidden" name="id_inscripcion" value="<?= $idInscripcion ?>"><input type="hidden" name="id_columna_notas" value="<?= $idColumna ?>"><input type="hidden" name="observaciones" value="<?= htmlspecialchars((string) ($celda['observaciones'] ?? ''), ENT_QUOTES, 'UTF-8') ?>"><?php foreach (['curso' => $cursoFiltro, 'materia' => $materiaFiltro, 'evaluacion' => $evaluacionFiltro] as $filtro => $valor): ?><?php if ($valor): ?><input type="hidden" name="filtro_<?= $filtro ?>" value="<?= (int) $valor ?>"><?php endif; ?><?php endforeach; ?><input type="hidden" name="filtro_q" value="<?= htmlspecialchars($busqueda, ENT_QUOTES, 'UTF-8') ?>"><input class="grade-cell-input" type="number" name="nota" min="0" max="<?= htmlspecialchars((string) $columna['nota_maxima'], ENT_QUOTES, 'UTF-8') ?>" step="0.01" value="<?= htmlspecialchars((string) ($celda['nota'] ?? ''), ENT_QUOTES, 'UTF-8') ?>" aria-label="Nota de <?= htmlspecialchars($alumno['alumno'], ENT_QUOTES, 'UTF-8') ?> en <?= htmlspecialchars($columna['titulo'], ENT_QUOTES, 'UTF-8') ?>" required><?php if ($celda): ?><input class="grade-reason-input" type="text" name="motivo_correccion" maxlength="255" placeholder="Motivo si cambia" aria-label="Motivo de corrección"><?php endif; ?><button type="submit" class="grade-save-button">Guardar</button></form><?php else: ?><span class="grade-readonly"><?= $celda ? htmlspecialchars((string) $celda['nota'], ENT_QUOTES, 'UTF-8') : '—' ?></span><?php endif; ?></td><?php endforeach; ?></tr><?php endforeach; ?>
          <?php if ($alumnosPlanilla === []): ?><tr><td colspan="<?= count($columnasPlanilla) + 2 ?>" class="empty-state">No hay alumnos inscriptos en este curso.</td></tr><?php endif; ?>
        </tbody></table></div><?php endif; ?>
      </section>

      <section class="table-card"><form class="list-filters" method="get"><input type="hidden" name="curso" value="<?= (int) ($cursoFiltro ?? 0) ?>"><input type="hidden" name="materia" value="<?= (int) ($materiaFiltro ?? 0) ?>"><input type="hidden" name="evaluacion" value="<?= (int) ($evaluacionFiltro ?? 0) ?>"><label class="search"><input type="search" name="q" placeholder="Buscar calificaciones" value="<?= htmlspecialchars($busqueda, ENT_QUOTES, 'UTF-8') ?>"></label><button class="filter-chip active" type="submit">Buscar</button></form>
        <div class="table-wrap"><table><thead><tr><th>Alumno</th><th>Curso / materia</th><th>Evaluación</th><th>Nota</th><th>Máximo</th><th>Fecha</th><th>Correcciones</th><?php if ($usuario['isAdmin'] || $usuario['isTeacher']): ?><th>Acciones</th><?php endif; ?></tr></thead><tbody>
          <?php foreach ($registros as $fila): ?><tr><td><?= htmlspecialchars($fila['alumno'], ENT_QUOTES, 'UTF-8') ?></td><td><?= htmlspecialchars($fila['curso'], ENT_QUOTES, 'UTF-8') ?></td><td><?= htmlspecialchars($fila['evaluacion'], ENT_QUOTES, 'UTF-8') ?></td><td><?= htmlspecialchars((string) $fila['nota'], ENT_QUOTES, 'UTF-8') ?></td><td><?= htmlspecialchars((string) $fila['nota_maxima'], ENT_QUOTES, 'UTF-8') ?></td><td><?= htmlspecialchars((string) $fila['fecha_carga'], ENT_QUOTES, 'UTF-8') ?></td><td><?= (int) $fila['correcciones'] ?></td><?php if ($usuario['isAdmin'] || $usuario['isTeacher']): ?><td><div class="actions"><a class="filter-chip" href="calificaciones.php?editar=<?= (int) $fila['id_calificacion'] ?>">Editar</a><?php if ($usuario['isAdmin']): ?><form method="post" onsubmit="return confirm('¿Eliminar esta calificación?')"><input type="hidden" name="csrf_token" value="<?= htmlspecialchars($_SESSION['calificaciones_csrf'], ENT_QUOTES, 'UTF-8') ?>"><input type="hidden" name="action" value="delete"><input type="hidden" name="id_calificacion" value="<?= (int) $fila['id_calificacion'] ?>"><button class="icon-btn small delete" type="submit">Eliminar</button></form><?php endif; ?></div></td><?php endif; ?></tr><?php endforeach; ?>
          <?php if ($registros === []): ?><tr><td colspan="<?= ($usuario['isAdmin'] || $usuario['isTeacher']) ? 8 : 7 ?>" class="empty-state">No hay calificaciones para mostrar.</td></tr><?php endif; ?>
        </tbody></table></div>
      </section>
    </main>
  </div>
</body>
</html>
