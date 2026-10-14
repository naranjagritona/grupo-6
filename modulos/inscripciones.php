<?php
session_start();
require_once dirname(__DIR__) . '/clases/database.php';
require_once dirname(__DIR__) . '/clases/dao/inscripcionDAO.php';
require_once dirname(__DIR__) . '/clases/dao/usuarioDAO.php';
require_once dirname(__DIR__) . '/clases/php/inscripcion.php';

$conexion = databaseConnection();
$inscripcionDao = new InscripcionDAO($conexion);
$usuarioDao = new UsuarioDAO($conexion);

// Obtener el nombre real del usuario a través del UsuarioDAO
$idUsuario = $_SESSION['id_usuario'] ?? null;
$usuarioSesion = $_SESSION['usuario'] ?? 'Usuario';
$nombreRealUsuario = $usuarioDao->obtenerNombreReal($idUsuario, $usuarioSesion);

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
  <link rel="stylesheet" href="estilos/styles.css" />
  <link rel="stylesheet" href="estilos/inscripciones_estilo.css" />
</head>
<body class="app">

<aside class="sidebar">
    <div class="brand">
      <div class="brand-mark">
        <svg viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg" fill="none">
          <path d="M24 4L7 9V22C7 32.5 14.2 42.3 24 45C33.8 42.3 41 32.5 41 22V9L24 4Z" fill="rgba(255,255,255,0.08)" stroke="white" stroke-width="2.5" stroke-linejoin="round"/>
          <path d="M16 28C19 26 22 26 24 28C26 26 29 26 32 28V19C29 17 26 17 24 19C22 17 19 17 16 19V28Z" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <text x="24" y="24" text-anchor="middle" font-size="9" font-weight="700" fill="white" font-family="Inter, sans-serif" letter-spacing="1">AI</text>
        </svg>
      </div>
      <div class="brand-text"><span>Academia</span><small>Integral XXI</small></div>
    </div>
    
    <nav class="nav" aria-label="Navegación principal">
      <a class="nav-link" href="inicio.php">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/></svg>
        <span>Inicio</span>
      </a>
      <a class="nav-link" href="cursos.php">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 14l9-5-9-5-9 5 9 5z"/><path d="M12 14l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998 12.078 12.078 0 01.665-6.479L12 14z"/><path d="M12 14v7"/></svg>
        <span>Cursos</span>
      </a>
      <a class="nav-link" href="calificaciones.php">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"/></svg>
        <span>Calificaciones</span>
      </a>
      <a class="nav-link active" href="inscripciones.php">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"/></svg>
        <span>Inscripciones</span>
      </a>
    </nav>
    
    <div class="nav-footer">
      <a class="nav-link" href="#">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
        <span>Configuración</span>
      </a>
    </div>
  </aside>
  
  <div class="main-panel">
    <header class="topbar">
      <div></div>
      <div class="topbar-tools">
        <div class="user">
          <div class="user-meta">
            <strong><?= htmlspecialchars($nombreRealUsuario) ?></strong>
          </div>
        </div>
      </div>
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
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg>
                          </a>
                          <a href="inscripciones.php?baja=<?= $inscripcion['id_inscripcion'] ?>" class="boton-accion eliminar" title="Dar de baja">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>
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