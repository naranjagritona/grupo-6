<?php
session_start();
require_once dirname(__DIR__) . '/clases/database.php';
require_once dirname(__DIR__) . '/clases/dao/inscripcionDAO.php';
require_once dirname(__DIR__) . '/clases/dao/usuarioDAO.php';
require_once dirname(__DIR__) . '/clases/php/inscripcion.php';

$conexion = databaseConnection();
$inscripcionDao = new InscripcionDAO($conexion);
$usuarioDao = new UsuarioDAO($conexion);
$usuario = $usuarioDao->obtenerSesionActual();
if (!$usuario['isAdmin']) {
    http_response_code(403);
    exit('No tenés permisos para gestionar inscripciones.');
}

$modoFormulario = ""; 
$inscripcionAEditar = null;

// Procesamiento de peticiones POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $accion = $_POST['accion'] ?? '';

    if ($accion === 'guardar_nuevo') {
        $idAlumno = (int)$_POST['id_alumno'];
        $idCurso = (int)$_POST['id_curso'];
        $fecha = $_POST['fecha'];
        $estado = $_POST['estado'];
        $importe = (float)$_POST['importe_cuota'];

        $nuevaInscripcion = new Inscripcion(0, $idAlumno, $idCurso, $fecha, EstadoInscripcion::from($estado), '', $importe, '', '');
        $inscripcionDao->insertar($nuevaInscripcion);
        header("Location: inscripciones.php");
        exit();
    }

    if ($accion === 'guardar_editar') {
        $idInscripcion = (int)$_POST['id_inscripcion'];
        $idAlumno = (int)$_POST['id_alumno'];
        $idCurso = (int)$_POST['id_curso'];
        $fecha = $_POST['fecha'];
        $estado = $_POST['estado'];
        $importe = (float)$_POST['importe_cuota'];

        $inscripcionModificada = new Inscripcion($idInscripcion, $idAlumno, $idCurso, $fecha, EstadoInscripcion::from($estado), '', $importe, '', '');
        $inscripcionDao->modificar($inscripcionModificada);
        header("Location: inscripciones.php");
        exit();
    }

    if ($accion === 'dar_baja') {
        $idInscripcion = (int)$_POST['id_inscripcion'];
        $motivoBaja = trim($_POST['motivo_baja']);
        if (!empty($motivoBaja)) {
            $inscripcionDao->darDeBaja($idInscripcion, $motivoBaja);
        }
        header("Location: inscripciones.php");
        exit();
    }
}

// Control de modales mediante GET
if (isset($_GET['nuevo'])) {
    $modoFormulario = 'nuevo';
}

if (isset($_GET['editar'])) {
    $modoFormulario = 'editar';
    $idEditar = (int)$_GET['editar'];
    $inscripcionAEditar = $inscripcionDao->obtenerPorId($idEditar);
}

$idBajaModal = isset($_GET['baja']) ? (int)$_GET['baja'] : 0;

// Filtros y búsquedas mediante el DAO de inscripciones
$terminoBusqueda = $_GET['q'] ?? '';
$filtroEstado = $_GET['estado'] ?? 'Todos';
$filtroCursoId = isset($_GET['curso']) ? (int)$_GET['curso'] : 0;

if (!empty($terminoBusqueda)) {
    $listaInscripciones = $inscripcionDao->buscarCampo($terminoBusqueda);
} elseif ($filtroEstado !== 'Todos' && !empty($filtroEstado)) {
    $listaInscripciones = $inscripcionDao->filtrarEstadoInscrip($filtroEstado);
} elseif ($filtroCursoId > 0) {
    $listaInscripciones = $inscripcionDao->filtrarCurso($filtroCursoId);
} else {
    $listaInscripciones = $inscripcionDao->listarDiezInscripcionesDetalladas();
}

// Obtención de listas para los selectores mediante el DAO
$listaAlumnos = $inscripcionDao->listarAlumnosActivos();
$listaCursosSelect = $inscripcionDao->listarCursosActivos();
?>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Academia Integral XXI - Inscripciones</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" />
  <link rel="stylesheet" href="estilos/styles.css" />
  <link rel="stylesheet" href="estilos/inscripciones_estilo.css" />
</head>
<body class="app">

<aside class="sidebar">
    <div class="brand"><img class="brand-mark" src="../img/logo.png" alt="Academia Integral XXI"><span class="brand-text"><span>Academia</span><small>Integral XXI</small></span></div>
    
    <nav class="nav" aria-label="Navegación principal">
      <a class="nav-link" href="cursos.php"><i class="bi bi-mortarboard"></i><span>Cursos</span></a>
      <a class="nav-link" href="calificaciones.php"><i class="bi bi-journal-check"></i><span>Calificaciones</span></a>
      <a class="nav-link" href="materias.php"><i class="bi bi-book"></i><span>Materias</span></a>
      <a class="nav-link active" href="inscripciones.php"><i class="bi bi-person-plus"></i><span>Inscripciones</span></a>
      <a class="nav-link" href="admin.php"><i class="bi bi-gear"></i><span>Administración</span></a>
    </nav>
  </aside>
  
  <div class="main-panel">
    <header class="topbar">
      <span></span>
      <div class="topbar-tools"><div class="user"><div class="avatar"><i class="bi bi-person"></i></div><div class="user-meta"><strong><?= htmlspecialchars($usuario['name'], ENT_QUOTES, 'UTF-8') ?></strong><small><?= htmlspecialchars($usuario['role'], ENT_QUOTES, 'UTF-8') ?></small></div></div><form method="post" action="logout.php"><input type="hidden" name="csrf_token" value="<?= htmlspecialchars($_SESSION['logout_csrf'] ??= bin2hex(random_bytes(32)), ENT_QUOTES, 'UTF-8') ?>"><button class="logout-button" type="submit"><i class="bi bi-box-arrow-right"></i><span>Cerrar sesión</span></button></form></div>
    </header>

    <main class="content">
      <div class="contenedor-inscripciones">
        <div class="cabecera-seccion">
          <div>
            <h1>Inscripciones</h1>
            <p>Gestioná las inscripciones de los alumnos a los diferentes cursos.</p>
          </div>
          <a href="inscripciones.php?nuevo=1" class="boton-nueva-inscripcion">Nueva inscripción</a>
        </div>

        <?php if ($modoFormulario === 'nuevo'): ?>
          <div class="modal-overlay">
            <div class="modal-ventana">
              <h2>Nueva inscripción</h2>
              <form method="POST" action="inscripciones.php">
                <input type="hidden" name="accion" value="guardar_nuevo">
                <div class="formulario-grid">
                  <div class="campo-formulario">
                    <label for="id_alumno">Alumno:</label>
                    <select id="id_alumno" name="id_alumno" required>
                      <option value="">Seleccione un alumno...</option>
                      <?php foreach ($listaAlumnos as $alum): ?>
                        <option value="<?= $alum['id_alumno'] ?>"><?= htmlspecialchars($alum['nombre_completo']) ?></option>
                      <?php endforeach; ?>
                    </select>
                  </div>
                  <div class="campo-formulario">
                    <label for="id_curso">Curso / Materia:</label>
                    <select id="id_curso" name="id_curso" required>
                      <option value="">Seleccione un curso...</option>
                      <?php foreach ($listaCursosSelect as $curs): ?>
                        <option value="<?= $curs['id_curso'] ?>"><?= htmlspecialchars($curs['curso_nombre']) ?></option>
                      <?php endforeach; ?>
                    </select>
                  </div>
                  <div class="campo-formulario">
                    <label for="fecha">Fecha:</label>
                    <input type="date" id="fecha" name="fecha" value="<?= date('Y-m-d') ?>" required>
                  </div>
                  <div class="campo-formulario">
                    <label for="estado">Estado:</label>
                    <select id="estado" name="estado" required>
                      <option value="Pendiente">Pendiente</option>
                      <option value="Confirmada">Confirmada</option>
                    </select>
                  </div>
                  <div class="campo-formulario">
                    <label for="importe_cuota">Importe Cuota ($):</label>
                    <input type="number" step="0.01" id="importe_cuota" name="importe_cuota" value="55000.00" required>
                  </div>
                </div>
                <div class="acciones-formulario">
                  <a href="inscripciones.php" class="boton-buscar" style="background:#6c757d; text-decoration:none;">Cancelar</a>
                  <button type="submit" class="boton-buscar">Guardar</button>
                </div>
              </form>
            </div>
          </div>
        <?php endif; ?>

        <?php if ($modoFormulario === 'editar' && $inscripcionAEditar): ?>
          <div class="modal-overlay">
            <div class="modal-ventana">
              <h2>Editar inscripción</h2>
              <form method="POST" action="inscripciones.php">
                <input type="hidden" name="accion" value="guardar_editar">
                <input type="hidden" name="id_inscripcion" value="<?= $inscripcionAEditar['id_inscripcion'] ?>">
                <div class="formulario-grid">
                  <div class="campo-formulario">
                    <label for="id_alumno">Alumno:</label>
                    <select id="id_alumno" name="id_alumno" required>
                      <?php foreach ($listaAlumnos as $alum): ?>
                        <option value="<?= $alum['id_alumno'] ?>" <?= $inscripcionAEditar['id_alumno'] == $alum['id_alumno'] ? 'selected' : '' ?>>
                          <?= htmlspecialchars($alum['nombre_completo']) ?>
                        </option>
                      <?php endforeach; ?>
                    </select>
                  </div>
                  <div class="campo-formulario">
                    <label for="id_curso">Curso / Materia:</label>
                    <select id="id_curso" name="id_curso" required>
                      <?php foreach ($listaCursosSelect as $curs): ?>
                        <option value="<?= $curs['id_curso'] ?>" <?= $inscripcionAEditar['id_curso'] == $curs['id_curso'] ? 'selected' : '' ?>>
                          <?= htmlspecialchars($curs['curso_nombre']) ?>
                        </option>
                      <?php endforeach; ?>
                    </select>
                  </div>
                  <div class="campo-formulario">
                    <label for="fecha">Fecha:</label>
                    <input type="date" id="fecha" name="fecha" value="<?= htmlspecialchars($inscripcionAEditar['fecha_inscripcion']) ?>" required>
                  </div>
                  <div class="campo-formulario">
                    <label for="estado">Estado:</label>
                    <select id="estado" name="estado" required>
                      <option value="Pendiente" <?= $inscripcionAEditar['estado'] === 'Pendiente' ? 'selected' : '' ?>>Pendiente</option>
                      <option value="Confirmada" <?= $inscripcionAEditar['estado'] === 'Confirmada' ? 'selected' : '' ?>>Confirmada</option>
                      <option value="Finalizada" <?= $inscripcionAEditar['estado'] === 'Finalizada' ? 'selected' : '' ?>>Finalizada</option>
                      <option value="Cancelada" <?= $inscripcionAEditar['estado'] === 'Cancelada' ? 'selected' : '' ?>>Cancelada</option>
                    </select>
                  </div>
                  <div class="campo-formulario">
                    <label for="importe_cuota">Importe Cuota ($):</label>
                    <input type="number" step="0.01" id="importe_cuota" name="importe_cuota" value="<?= htmlspecialchars((string)$inscripcionAEditar['importe_cuota']) ?>" required>
                  </div>
                </div>
                <div class="acciones-formulario">
                  <a href="inscripciones.php" class="boton-buscar" style="background:#6c757d; text-decoration:none;">Cancelar</a>
                  <button type="submit" class="boton-buscar">Actualizar</button>
                </div>
              </form>
            </div>
          </div>
        <?php endif; ?>

        <?php if ($idBajaModal > 0): ?>
          <div class="modal-overlay">
            <div class="modal-ventana" style="border-top: 5px solid #dc3545;">
              <h2>Dar de baja inscripción (#<?= $idBajaModal ?>)</h2>
              <p style="font-size:13px; color:#666;">Ingrese el motivo. El estado cambiará a finalizado.</p>
              <form method="POST" action="inscripciones.php">
                <input type="hidden" name="accion" value="dar_baja">
                <input type="hidden" name="id_inscripcion" value="<?= $idBajaModal ?>">
                <div class="formulario-grid">
                  <div class="campo-formulario">
                    <label for="motivo_baja">Motivo de la baja:</label>
                    <textarea id="motivo_baja" name="motivo_baja" rows="3" placeholder="Escriba el motivo..." required></textarea>
                  </div>
                </div>
                <div class="acciones-formulario">
                  <a href="inscripciones.php" class="boton-buscar" style="background:#6c757d; text-decoration:none;">Cancelar</a>
                  <button type="submit" class="boton-buscar" style="background-color: #dc3545;">Confirmar baja</button>
                </div>
              </form>
            </div>
          </div>
        <?php endif; ?>

        <form method="GET" action="inscripciones.php" class="panel-filtros">
          <div class="grupo-filtro">
            <label for="anio_lectivo">Año lectivo</label>
            <select id="anio_lectivo" name="anio_lectivo">
              <option value="2025" selected>2025</option>
              <option value="2026">2026</option>
            </select>
          </div>

          <div class="grupo-filtro">
            <label for="curso">Curso</label>
            <select id="curso" name="curso">
              <option value="0">Todos</option>
              <?php foreach ($listaCursosSelect as $cursoItem): ?>
                <option value="<?= $cursoItem['id_curso'] ?>" <?= $filtroCursoId === (int)$cursoItem['id_curso'] ? 'selected' : '' ?>>
                  <?= htmlspecialchars($cursoItem['curso_nombre']) ?>
                </option>
              <?php endforeach; ?>
            </select>
          </div>

          <div class="grupo-filtro">
            <label for="estado">Estado</label>
            <select id="estado" name="estado">
              <option value="Todos" <?= $filtroEstado === 'Todos' ? 'selected' : '' ?>>Todos</option>
              <option value="Confirmada" <?= $filtroEstado === 'Confirmada' ? 'selected' : '' ?>>Confirmada</option>
              <option value="Pendiente" <?= $filtroEstado === 'Pendiente' ? 'selected' : '' ?>>Pendiente</option>
              <option value="Cancelada" <?= $filtroEstado === 'Cancelada' ? 'selected' : '' ?>>Cancelada</option>
              <option value="Finalizada" <?= $filtroEstado === 'Finalizada' ? 'selected' : '' ?>>Finalizada</option>
            </select>
          </div>

          <button type="submit" class="boton-buscar">Filtrar</button>
        </form>

        <div class="buscador-tabla-container">
          <form method="GET" action="inscripciones.php">
            <input type="text" name="q" value="<?= htmlspecialchars($terminoBusqueda) ?>" placeholder="Buscar por alumno, DNI o materia..." />
            <button type="submit" class="boton-buscar">Buscar</button>
          </form>
        </div>

        <div class="tabla-tarjeta">
          <div class="contenedor-tabla">
            <table class="tabla-inscripciones">
              <thead>
                <tr>
                  <th>Fecha</th>
                  <th>Alumno</th>
                  <th>Curso</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                <?php if (empty($listaInscripciones)): ?>
                  <tr>
                    <td colspan="5" style="text-align: center; padding: 20px; color: #666;">No se encontraron inscripciones.</td>
                  </tr>
                <?php else: ?>
                  <?php foreach ($listaInscripciones as $inscripcion): ?>
                    <?php
                    $fechaFormateada = !empty($inscripcion['fecha_inscripcion']) ? date('d/m/Y', strtotime($inscripcion['fecha_inscripcion'])) : '—';
                    $estadoMin = strtolower($inscripcion['estado']);
                    $estadoClase = $estadoMin === 'confirmada' ? 'confirmada' : ($estadoMin === 'pendiente' ? 'pendiente' : 'finalizada');
                    ?>
                    <tr>
                      <td><?= htmlspecialchars($fechaFormateada) ?></td>
                      <td><?= htmlspecialchars($inscripcion['nombre_alumno'] ?? 'Alumno') ?></td>
                      <td><?= htmlspecialchars($inscripcion['nombre_curso'] ?? 'Curso') ?></td>
                      <td><span class="etiqueta-estado <?= $estadoClase ?>"><?= htmlspecialchars($inscripcion['estado']) ?></span></td>
                      <td>
                        <div class="acciones-tabla">
                          <a href="inscripciones.php?editar=<?= $inscripcion['id_inscripcion'] ?>" class="boton-accion editar" title="Editar">
                            <i class="bi bi-pencil"></i>
                          </a>
                          <a href="inscripciones.php?baja=<?= $inscripcion['id_inscripcion'] ?>" class="boton-accion eliminar" title="Dar de baja">
                            <i class="bi bi-trash"></i>
                          </a>
                        </div>
                      </td>
                    </tr>
                  <?php endforeach; ?>
                <?php endif; ?>
              </tbody>
            </table>
          </div>

          <div class="pie-tabla">
            <p>Mostrando registros de inscripciones</p>
          </div>
        </div>
      </div>
    </main>
  </div>
</body>
</html>