<?php
session_start();
require_once dirname(__DIR__) . '/clases/database.php';
require_once dirname(__DIR__) . '/clases/dao/usuarioDAO.php';
require_once dirname(__DIR__) . '/clases/dao/cursoDAO.php';

$conexion = databaseConnection();
$usuario = (new UsuarioDAO($conexion))->obtenerSesionActual();
$cursoDAO = new CursoDAO($conexion);
$busqueda = trim((string) ($_GET['q'] ?? ''));
$estadoFiltro = trim((string) ($_GET['estado'] ?? ''));
$error = '';
$mensajeExito = $_SESSION['cursos_mensaje'] ?? '';
unset($_SESSION['cursos_mensaje']);
if (!isset($_SESSION['cursos_csrf'])) {
    $_SESSION['cursos_csrf'] = bin2hex(random_bytes(32));
}
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $token = is_string($_POST['csrf_token'] ?? null) ? $_POST['csrf_token'] : '';
    if (!hash_equals($_SESSION['cursos_csrf'], $token)) {
        $error = 'La sesión venció. Recargá la página e intentá nuevamente.';
    } elseif (!$usuario['isAdmin']) {
        http_response_code(403);
        exit('No tenés permisos para modificar cursos.');
    } else {
        try {
            if (($_POST['action'] ?? '') === 'save') {
                $esEdicion = (int) ($_POST['id_curso'] ?? 0) > 0;
                $cursoDAO->guardarCurso((int) ($_POST['id_curso'] ?? 0), $_POST);
                $_SESSION['cursos_mensaje'] = $esEdicion ? 'El curso se actualizó correctamente.' : 'El curso se creó correctamente.';
            } elseif (($_POST['action'] ?? '') === 'cancel') {
                $cursoDAO->cancelarCurso((int) ($_POST['id_curso'] ?? 0));
                $_SESSION['cursos_mensaje'] = 'El curso se canceló correctamente.';
            } else {
                throw new InvalidArgumentException('La operación solicitada no es válida.');
            }
            header('Location: cursos.php');
            exit;
        } catch (InvalidArgumentException $exception) {
            $error = $exception->getMessage();
        } catch (Throwable $exception) {
            error_log('Course module operation failed: ' . $exception->getMessage());
            $error = 'No se pudo completar la operación.';
        }
    }
}
$editar = isset($_GET['editar']) ? $cursoDAO->obtenerCursoConUsuario((int) $_GET['editar'], $usuario) : null;
$opciones = $usuario['isAdmin'] ? $cursoDAO->opcionesDeCurso($editar ? (int) $editar['id_curso'] : null) : [];
$registros = $cursoDAO->listarCursosConUsuario($usuario, $busqueda, $estadoFiltro);
if (isset($_GET['editar']) && $usuario['isAdmin'] && $editar === null) {
    $error = 'El curso solicitado no existe.';
}
?>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Academia Integral XXI - Cursos</title>
  <link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"><link rel="stylesheet" href="estilos/styles.css"><link rel="stylesheet" href="estilos/cursos_estilo.css?v=006">
</head>
<body class="app">
  <aside class="sidebar">
    <div class="brand"><img class="brand-mark" src="../img/logo.png" alt="Academia Integral XXI"><span class="brand-text"><span>Academia</span><small>Integral XXI</small></span></div>
    <nav class="nav" aria-label="Navegación principal">
      <a class="nav-link active" href="cursos.php"><i class="bi bi-mortarboard"></i><span>Cursos</span></a>
      <a class="nav-link" href="calificaciones.php"><i class="bi bi-journal-check"></i><span>Calificaciones</span></a>
      <?php if ($usuario['isAdmin']): ?><a class="nav-link" href="inscripciones.php"><i class="bi bi-person-plus"></i><span>Inscripciones</span></a><?php endif; ?>
      <a class="nav-link" href="materias.php"><i class="bi bi-book"></i><span>Materias</span></a>
      <?php if ($usuario['isAdmin']): ?><a class="nav-link" href="admin.php"><i class="bi bi-gear"></i><span>Administración</span></a><?php endif; ?>
    </nav>
  </aside>
  <div class="main-panel">
    <header class="topbar"><span></span><div class="topbar-tools"><div class="user"><div class="avatar"><i class="bi bi-person"></i></div><div class="user-meta"><strong><?= htmlspecialchars($usuario['name'], ENT_QUOTES, 'UTF-8') ?></strong><small><?= htmlspecialchars($usuario['role'], ENT_QUOTES, 'UTF-8') ?></small></div></div><form method="post" action="logout.php"><input type="hidden" name="csrf_token" value="<?= htmlspecialchars($_SESSION['logout_csrf'] ??= bin2hex(random_bytes(32)), ENT_QUOTES, 'UTF-8') ?>"><button class="logout-button" type="submit"><i class="bi bi-box-arrow-right"></i><span>Cerrar sesión</span></button></form></div></header>
    <main class="content"><div class="contenedor-cursos">
      <div class="cabecera-seccion"><div><h1>Cursos</h1><p>Consultá los cursos vinculados a tu cuenta.</p></div><?php if ($usuario['isAdmin']): ?><a class="boton-nuevo-curso" href="cursos.php?nuevo=1">Nuevo curso</a><?php endif; ?></div>
      <?php if ($mensajeExito !== ''): ?><div class="message success aviso-cursos" role="status"><?= htmlspecialchars($mensajeExito, ENT_QUOTES, 'UTF-8') ?></div><?php endif; ?>
      <?php if ($error !== ''): ?><div class="message error aviso-cursos" role="alert"><?= htmlspecialchars($error, ENT_QUOTES, 'UTF-8') ?></div><?php endif; ?>
      <?php if ($usuario['isAdmin'] && (isset($_GET['nuevo']) || $editar !== null)): ?>
        <?php $valores = $editar ?? ['id_curso'=>0,'id_periodo'=>'','id_materia'=>'','id_docente'=>'','año'=>'','division'=>'','modalidad'=>'Presencial','turno'=>'mañana','cupo_maximo'=>30,'fecha_inicio'=>'','fecha_fin'=>'','estado'=>'Activo']; ?>
        <div class="modal-overlay"><section class="modal-ventana"><div class="modal-cabecera"><h2><?= $editar ? 'Editar curso' : 'Crear curso' ?></h2><a class="filter-chip" href="cursos.php">Cerrar</a></div>
          <form method="post" class="formulario-grid"><input type="hidden" name="csrf_token" value="<?= htmlspecialchars($_SESSION['cursos_csrf'], ENT_QUOTES, 'UTF-8') ?>"><input type="hidden" name="action" value="save"><input type="hidden" name="id_curso" value="<?= (int) $valores['id_curso'] ?>">
            <?php foreach (['id_periodo'=>['Período',$opciones['periodos']],'id_materia'=>['Materia',$opciones['materias']],'id_docente'=>['Docente',$opciones['docentes']]] as $field=>[$label,$items]): ?><label class="campo-formulario"><span><?= $label ?></span><select name="<?= $field ?>" required><option value="">Seleccioná</option><?php foreach($items as $item): ?><option value="<?= (int)$item['id'] ?>" <?= (string)$valores[$field]===(string)$item['id']?'selected':'' ?>><?= htmlspecialchars($item['etiqueta'],ENT_QUOTES,'UTF-8') ?></option><?php endforeach; ?></select></label><?php endforeach; ?>
            <label class="campo-formulario"><span>Año / nivel</span><input name="año" maxlength="200" required value="<?= htmlspecialchars((string)$valores['año'],ENT_QUOTES,'UTF-8') ?>"></label>
            <label class="campo-formulario"><span>División</span><input name="division" maxlength="200" required value="<?= htmlspecialchars((string)$valores['division'],ENT_QUOTES,'UTF-8') ?>"></label>
            <label class="campo-formulario"><span>Modalidad</span><select name="modalidad"><?php foreach(['Presencial','Virtual','Híbrido'] as $item): ?><option <?= $valores['modalidad']===$item?'selected':'' ?>><?= $item ?></option><?php endforeach; ?></select></label>
            <label class="campo-formulario"><span>Turno</span><select name="turno"><?php foreach(['mañana','tarde','vespertino'] as $item): ?><option value="<?= $item ?>" <?= $valores['turno']===$item?'selected':'' ?>><?= ucfirst($item) ?></option><?php endforeach; ?></select></label>
            <label class="campo-formulario"><span>Cupo máximo</span><input type="number" name="cupo_maximo" min="1" max="1000" required value="<?= (int)$valores['cupo_maximo'] ?>"></label>
            <label class="campo-formulario"><span>Fecha de inicio</span><input type="date" name="fecha_inicio" required value="<?= htmlspecialchars((string)$valores['fecha_inicio'],ENT_QUOTES,'UTF-8') ?>"></label>
            <label class="campo-formulario"><span>Fecha de fin</span><input type="date" name="fecha_fin" required value="<?= htmlspecialchars((string)$valores['fecha_fin'],ENT_QUOTES,'UTF-8') ?>"></label>
            <label class="campo-formulario"><span>Estado</span><select name="estado"><?php foreach(['Activo','Finalizado','Cancelado'] as $item): ?><option <?= $valores['estado']===$item?'selected':'' ?>><?= $item ?></option><?php endforeach; ?></select></label>
            <div class="acciones-formulario"><button class="primary-btn" type="submit">Guardar</button><a class="filter-chip" href="cursos.php">Cancelar</a></div>
          </form>
        </div></section></div>
      <?php endif; ?>
      <div class="barra-herramientas-cursos">
        <div class="panel-filtros">
          <form method="get" class="filtros-cursos">
            <input type="hidden" name="q" value="<?= htmlspecialchars($busqueda,ENT_QUOTES,'UTF-8') ?>">
            <div class="grupo-filtro"><label for="filtro-estado">Estado</label><select id="filtro-estado" name="estado"><option value="">Todos los estados</option><?php foreach(['Activo','Finalizado','Cancelado'] as $item): ?><option value="<?= $item ?>" <?= $estadoFiltro===$item?'selected':'' ?>><?= $item ?></option><?php endforeach; ?></select></div>
            <button class="boton-buscar" type="submit">Filtrar</button>
          </form>
        </div>
        <div class="buscador-tabla-container">
          <form method="get"><input type="hidden" name="estado" value="<?= htmlspecialchars($estadoFiltro,ENT_QUOTES,'UTF-8') ?>"><input type="search" name="q" placeholder="Buscar cursos..." aria-label="Buscar cursos" value="<?= htmlspecialchars($busqueda,ENT_QUOTES,'UTF-8') ?>"><button class="boton-buscar" type="submit">Buscar</button></form>
        </div>
      </div>
      <section class="tabla-tarjeta"><div class="contenedor-tabla" tabindex="0" aria-label="Tabla de cursos con desplazamiento"><table class="tabla-cursos"><thead><tr><th>Curso / materia</th><th>Período</th><th>Docente</th><th>Turno</th><th>Modalidad</th><th>Cupo</th><th>Estado</th><?php if($usuario['isAdmin']): ?><th>Acciones</th><?php endif; ?></tr></thead><tbody>
        <?php foreach($registros as $fila): ?><tr><td><?= htmlspecialchars($fila['curso'],ENT_QUOTES,'UTF-8') ?></td><td><?= htmlspecialchars($fila['periodo'],ENT_QUOTES,'UTF-8') ?></td><td><?= htmlspecialchars($fila['docente'],ENT_QUOTES,'UTF-8') ?></td><td><?= htmlspecialchars($fila['turno'],ENT_QUOTES,'UTF-8') ?></td><td><?= htmlspecialchars($fila['modalidad'],ENT_QUOTES,'UTF-8') ?></td><td><?= (int)$fila['cupo_maximo'] ?></td><td><span class="etiqueta-estado <?= strcasecmp((string)$fila['estado'], 'Activo') === 0 ? 'activo' : 'inactivo' ?>"><?= htmlspecialchars($fila['estado'],ENT_QUOTES,'UTF-8') ?></span></td><?php if($usuario['isAdmin']): ?><td><div class="acciones-tabla"><a class="boton-accion editar" title="Editar curso" aria-label="Editar curso" href="cursos.php?editar=<?= (int)$fila['id_curso'] ?>"><i class="bi bi-pencil" aria-hidden="true"></i></a><?php if($fila['estado']!=='Cancelado'): ?><form method="post" onsubmit="return confirm('¿Cancelar este curso?')"><input type="hidden" name="csrf_token" value="<?= htmlspecialchars($_SESSION['cursos_csrf'],ENT_QUOTES,'UTF-8') ?>"><input type="hidden" name="action" value="cancel"><input type="hidden" name="id_curso" value="<?= (int)$fila['id_curso'] ?>"><button class="boton-accion eliminar" type="submit" title="Cancelar curso" aria-label="Cancelar curso"><i class="bi bi-trash" aria-hidden="true"></i></button></form><?php endif; ?></div></td><?php endif; ?></tr><?php endforeach; ?>
        <?php if($registros===[]): ?><tr><td colspan="<?= $usuario['isAdmin']?8:7 ?>" class="empty-state">No hay cursos para mostrar.</td></tr><?php endif; ?>
        </tbody></table></div><div class="pie-tabla">Mostrando registros de cursos</div>
      </section>
    </div></main>
  </div>
</body>
</html>
