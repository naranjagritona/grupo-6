<?php
session_start();
require_once dirname(__DIR__) . '/clases/databases.php';
require_once dirname(__DIR__) . '/clases/dao/usuarioDAO.php';
require_once dirname(__DIR__) . '/clases/dao/materiaDAO.php';

$conexion = databaseConnection();
$usuario = (new UsuarioDAO($conexion))->obtenerSesionActual();
$materiaDAO = new MateriaDAO($conexion);
$busquedaEntrada = $_GET['q'] ?? '';
$busqueda = is_string($busquedaEntrada) ? trim($busquedaEntrada) : '';
$error = '';
if (!isset($_SESSION['materias_csrf'])) {
    $_SESSION['materias_csrf'] = bin2hex(random_bytes(32));
}
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $token = is_string($_POST['csrf_token'] ?? null) ? $_POST['csrf_token'] : '';
    if (!hash_equals($_SESSION['materias_csrf'], $token)) {
        $error = 'La sesión venció. Recargá la página e intentá nuevamente.';
    } elseif (!$usuario['isAdmin']) {
        http_response_code(403);
        exit('No tenés permisos para modificar materias.');
    } else {
        try {
            if (($_POST['action'] ?? '') === 'save') {
                $materiaDAO->guardarMateria((int) ($_POST['id_materia'] ?? 0), (string) ($_POST['nombre_materia'] ?? ''));
            } elseif (($_POST['action'] ?? '') === 'delete') {
                $materiaDAO->eliminarMateria((int) ($_POST['id_materia'] ?? 0));
            } else {
                throw new InvalidArgumentException('La operación solicitada no es válida.');
            }
            header('Location: materias.php');
            exit;
        } catch (InvalidArgumentException $exception) {
            $error = $exception->getMessage();
        } catch (Throwable $exception) {
            error_log('Subject module operation failed: ' . $exception->getMessage());
            $error = 'No se pudo completar la operación.';
        }
    }
}
$registros = $materiaDAO->listarMateriasDeUsuario($usuario, $busqueda);
$editar = isset($_GET['editar']) ? $materiaDAO->obtenerMateria((int) $_GET['editar']) : null;
$totalCursos = array_sum(array_map(static fn(array $fila): int => (int) $fila['cursos'], $registros));
$materiasConCurso = count(array_filter($registros, static fn(array $fila): bool => (int) $fila['cursos'] > 0));
?>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Academia Integral XXI - Materias</title>
  <link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"><link rel="stylesheet" href="estilos/styles.css"><link rel="stylesheet" href="estilos/materias_estilo.css?v=002">
</head>
<body class="app materias-page">
  <aside class="sidebar">
    <div class="brand"><img class="brand-mark" src="../img/logo.png" alt="Academia Integral XXI"><span class="brand-text"><span>Academia</span><small>Integral XXI</small></span></div>
    <nav class="nav" aria-label="Navegación principal">
      <a class="nav-link" href="cursos.php"><i class="bi bi-mortarboard"></i><span>Cursos</span></a>
      <a class="nav-link" href="calificaciones.php"><i class="bi bi-journal-check"></i><span>Calificaciones</span></a>
      <?php if($usuario['isAdmin']): ?><a class="nav-link" href="inscripciones.php"><i class="bi bi-person-plus"></i><span>Inscripciones</span></a><?php endif; ?>
      <a class="nav-link active" href="materias.php"><i class="bi bi-book"></i><span>Materias</span></a>
      <?php if($usuario['isAdmin']): ?><a class="nav-link" href="admin.php"><i class="bi bi-gear"></i><span>Administración</span></a><?php endif; ?>
    </nav>
  </aside>
  <div class="main-panel">
    <header class="topbar"><span></span><div class="topbar-tools"><div class="user"><div class="avatar"><i class="bi bi-person"></i></div><div class="user-meta"><strong><?= htmlspecialchars($usuario['name'],ENT_QUOTES,'UTF-8') ?></strong><small><?= htmlspecialchars($usuario['role'],ENT_QUOTES,'UTF-8') ?></small></div></div><form method="post" action="logout.php"><input type="hidden" name="csrf_token" value="<?= htmlspecialchars($_SESSION['logout_csrf'] ??= bin2hex(random_bytes(32)),ENT_QUOTES,'UTF-8') ?>"><button class="logout-button" type="submit"><i class="bi bi-box-arrow-right"></i><span>Cerrar sesión</span></button></form></div></header>
    <main class="content"><div class="contenedor-materias">
      <div class="page-head"><div><h1>Materias</h1><p>Administrá el catálogo y consultá en qué cursos se dicta cada materia.</p></div><?php if($usuario['isAdmin']): ?><a class="primary-btn boton-nueva-materia" href="materias.php?nuevo=1"><i class="bi bi-plus-lg" aria-hidden="true"></i> Nueva materia</a><?php endif; ?></div>
      <?php if($error!==''): ?><div class="message error" role="alert"><?= htmlspecialchars($error,ENT_QUOTES,'UTF-8') ?></div><?php endif; ?>
      <div class="resumen-materias"><article class="resumen-materia"><span class="resumen-icono"><i class="bi bi-book" aria-hidden="true"></i></span><div><small>Materias visibles</small><strong><?= count($registros) ?></strong></div></article><article class="resumen-materia"><span class="resumen-icono azul"><i class="bi bi-mortarboard" aria-hidden="true"></i></span><div><small>Con cursos asociados</small><strong><?= $materiasConCurso ?></strong></div></article><article class="resumen-materia"><span class="resumen-icono verde"><i class="bi bi-collection" aria-hidden="true"></i></span><div><small>Cursos relacionados</small><strong><?= $totalCursos ?></strong></div></article></div>
      <?php if($usuario['isAdmin'] && (isset($_GET['nuevo']) || $editar!==null)): ?>
        <section class="panel-card module-form-card"><div class="card-header"><h2><?= $editar?'Editar materia':'Crear materia' ?></h2><a class="filter-chip" href="materias.php">Cerrar</a></div>
          <form method="post" class="module-form"><input type="hidden" name="csrf_token" value="<?= htmlspecialchars($_SESSION['materias_csrf'],ENT_QUOTES,'UTF-8') ?>"><input type="hidden" name="action" value="save"><input type="hidden" name="id_materia" value="<?= (int)($editar['id_materia']??0) ?>"><label class="module-field"><span>Nombre de materia</span><input name="nombre_materia" maxlength="200" required value="<?= htmlspecialchars($editar['nombre_materia']??'',ENT_QUOTES,'UTF-8') ?>"></label><div class="module-form-actions"><button class="primary-btn" type="submit">Guardar</button><a class="filter-chip" href="materias.php">Cancelar</a></div></form>
        </section>
      <?php endif; ?>
      <section class="table-card"><div class="materias-tabla-cabecera"><div><h2>Catálogo de materias</h2><p>Las materias con cursos asociados se mantienen vinculadas al historial académico.</p></div><span class="contador-registros"><?= count($registros) ?> registro<?= count($registros) === 1 ? '' : 's' ?></span></div><form class="list-filters" method="get"><label class="search"><input type="search" name="q" placeholder="Buscar por nombre de materia" value="<?= htmlspecialchars($busqueda,ENT_QUOTES,'UTF-8') ?>"></label><button class="filter-chip active" type="submit">Buscar</button><?php if($busqueda!==''): ?><a class="filter-chip" href="materias.php">Limpiar</a><?php endif; ?></form>
        <div class="table-wrap"><table class="tabla-materias"><thead><tr><th>Materia</th><th>Cursos relacionados</th><th>Uso</th><?php if($usuario['isAdmin']): ?><th>Acciones</th><?php endif; ?></tr></thead><tbody>
          <?php foreach($registros as $fila): ?><tr><td><div class="nombre-materia"><span class="materia-avatar"><i class="bi bi-book-half" aria-hidden="true"></i></span><strong><?= htmlspecialchars($fila['materia'],ENT_QUOTES,'UTF-8') ?></strong></div></td><td><span class="cantidad-cursos"><i class="bi bi-collection" aria-hidden="true"></i><?= (int)$fila['cursos'] ?> curso<?= (int)$fila['cursos'] === 1 ? '' : 's' ?></span></td><td><span class="etiqueta-uso <?= (int)$fila['cursos'] > 0 ? 'en-uso' : 'sin-uso' ?>"><?= (int)$fila['cursos'] > 0 ? 'En uso' : 'Sin cursos' ?></span></td><?php if($usuario['isAdmin']): ?><td><div class="actions"><a class="filter-chip" href="materias.php?editar=<?= (int)$fila['id_materia'] ?>">Editar</a><form method="post" onsubmit="return confirm('¿Eliminar esta materia?')"><input type="hidden" name="csrf_token" value="<?= htmlspecialchars($_SESSION['materias_csrf'],ENT_QUOTES,'UTF-8') ?>"><input type="hidden" name="action" value="delete"><input type="hidden" name="id_materia" value="<?= (int)$fila['id_materia'] ?>"><button class="icon-btn small delete" type="submit" <?= (int)$fila['cursos'] > 0 ? 'disabled title="No se puede eliminar: tiene cursos asociados"' : '' ?>>Eliminar</button></form></div></td><?php endif; ?></tr><?php endforeach; ?>
          <?php if($registros===[]): ?><tr><td colspan="<?= $usuario['isAdmin']?3:2 ?>" class="empty-state">No hay materias para mostrar.</td></tr><?php endif; ?>
        </tbody></table></div>
      </section>
    </div></main>
  </div>
</body>
</html>
