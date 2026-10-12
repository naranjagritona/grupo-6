<?php
declare(strict_types=1);

$modules = [
    'Inicio' => 'inicio.php',
    'Alumnos' => 'alumnos.php',
    'Cursos' => 'cursos.php',
    'Materias' => 'materias.php',
    'Calificaciones' => 'calificaciones.php',
    'Inscripciones' => 'inscripciones.php',
    'Docentes' => 'docentes.php',
];
$currentRoute = basename((string) ($_SERVER['SCRIPT_NAME'] ?? ''));
$e = 'moduleEscape';
?>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Academia Integral XXI - <?= $e($pageTitle) ?></title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet"/>
  <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
  <script>tailwind.config={theme:{extend:{fontFamily:{sans:['Inter','sans-serif']}}}};</script>
  <link rel="stylesheet" href="../assets/css/styles.css" />
</head>
<body class="app">
  <aside class="sidebar">
    <a class="brand brand-link" href="inicio.php">
      <span class="brand-mark" aria-hidden="true">AI</span>
      <span class="brand-text"><span>Academia</span><small>Integral XXI</small></span>
    </a>
    <nav class="nav" aria-label="Navegación principal">
      <?php foreach ($modules as $label => $href): ?>
        <a class="nav-link<?= $currentRoute === $href ? ' active' : '' ?>" href="<?= $e($href) ?>">
          <span><?= $e($label) ?></span>
        </a>
      <?php endforeach; ?>
    </nav>
  </aside>

  <div class="main-panel">
    <header class="topbar">
      <div class="topbar-tools">
        <div class="user">
          <div class="avatar" aria-hidden="true">AI</div>
          <div class="user-meta"><strong>Academia Integral XXI</strong><small>Administración</small></div>
        </div>
      </div>
    </header>

    <main class="content">
      <?php if ($data === null): ?>
        <div class="message error" role="alert">No se pudo conectar con la base de datos. Revisá la configuración de conexión.</div>
      <?php else: ?>
        <?php
        $config = $data['config'];
        $route = $data['route'];
        $fields = $config['fields'];
        $fieldValues = $data['formValues'];
        ?>
        <div class="page-head">
          <div>
            <h1><?= $e($config['title']) ?></h1>
            <p><?= $e($config['description']) ?></p>
          </div>
          <a class="primary-btn" href="<?= $e($route) ?>?nuevo=1">Nuevo registro</a>
        </div>

        <?php if ($data['flash'] !== ''): ?>
          <div class="message success" role="status"><?= $e($data['flash']) ?></div>
        <?php endif; ?>
        <?php if ($data['error'] !== null): ?>
          <div class="message error" role="alert"><?= $e($data['error']) ?></div>
        <?php endif; ?>

        <section class="dashboard-grid module-summary" aria-label="Resumen de <?= $e($config['title']) ?>">
          <?php foreach ($data['summaries'] as $summary): ?>
            <?php
            $value = is_numeric($summary['value']) ? (float) $summary['value'] : 0;
            $displayValue = str_contains($summary['label'], 'Recaudación')
                ? '$' . number_format($value, 2, ',', '.')
                : (str_contains($summary['label'], 'Promedio') ? number_format($value, 2, ',', '.') : number_format($value, 0, ',', '.'));
            ?>
            <article class="summary-card">
              <div class="summary-head"><span class="summary-title"><?= $e($summary['label']) ?></span></div>
              <div class="summary-amount"><?= $e($displayValue) ?></div>
            </article>
          <?php endforeach; ?>
        </section>

        <?php if ($data['showForm']): ?>
          <section class="panel-card module-form-card" id="registro-formulario">
            <div class="card-header">
              <h2><?= $data['recordId'] > 0 ? 'Editar registro' : 'Crear registro' ?></h2>
              <a class="filter-chip" href="<?= $e($route) ?>">Cerrar</a>
            </div>
            <?php if ($config['table'] === 'calificacion'): ?>
              <form method="get" action="<?= $e($route) ?>" class="related-search">
                <?php if ($data['recordId'] > 0): ?>
                  <input type="hidden" name="editar" value="<?= $e($data['recordId']) ?>" />
                <?php else: ?>
                  <input type="hidden" name="nuevo" value="1" />
                <?php endif; ?>
                <label for="related-search">Filtrar inscripciones por nombre, matrícula, curso o materia</label>
                <div class="filter-group">
                  <input id="related-search" type="search" name="relacion" value="<?= $e($data['relationSearch']) ?>" placeholder="Escribí al menos parte del nombre o matrícula" />
                  <button class="filter-chip active" type="submit">Buscar opciones</button>
                </div>
                <small>Se muestran hasta 100 resultados para mantener ágil la pantalla.</small>
              </form>
            <?php endif; ?>
            <form method="post" action="<?= $e($route) ?>" class="module-form">
              <input type="hidden" name="csrf_token" value="<?= $e($_SESSION['module_csrf']) ?>" />
              <input type="hidden" name="action" value="save" />
              <input type="hidden" name="id" value="<?= $e($data['recordId']) ?>" />
              <?php if ($config['table'] === 'calificacion'): ?>
                <input type="hidden" name="relacion" value="<?= $e($data['relationSearch']) ?>" />
              <?php endif; ?>
              <?php foreach ($fields as $name => $field): ?>
                <label class="module-field" for="field-<?= $e($name) ?>">
                  <span><?= $e($field['label']) ?><?= ($field['required'] ?? false) ? ' *' : '' ?></span>
                  <?php if ($field['type'] === 'select'): ?>
                    <select id="field-<?= $e($name) ?>" name="<?= $e($name) ?>" <?= ($field['required'] ?? false) ? 'required' : '' ?>>
                      <option value="">Seleccioná una opción</option>
                      <?php foreach ($data['options'][$name] as [$optionValue, $optionLabel]): ?>
                        <option value="<?= $e($optionValue) ?>" <?= (string) ($fieldValues[$name] ?? ($field['default'] ?? '')) === (string) $optionValue ? 'selected' : '' ?>>
                          <?= $e($optionLabel) ?>
                        </option>
                      <?php endforeach; ?>
                    </select>
                  <?php elseif ($field['type'] === 'textarea'): ?>
                    <textarea id="field-<?= $e($name) ?>" name="<?= $e($name) ?>" maxlength="<?= $e($field['maxLength'] ?? '') ?>" <?= ($field['required'] ?? false) ? 'required' : '' ?>><?= $e($fieldValues[$name] ?? '') ?></textarea>
                  <?php else: ?>
                    <input
                      id="field-<?= $e($name) ?>"
                      name="<?= $e($name) ?>"
                      type="<?= $e($field['type']) ?>"
                      value="<?= $field['type'] === 'password' ? '' : $e($fieldValues[$name] ?? ($field['default'] ?? '')) ?>"
                      <?= isset($field['min']) ? 'min="' . $e($field['min']) . '"' : '' ?>
                      <?= isset($field['max']) ? 'max="' . $e($field['max']) . '"' : '' ?>
                      <?= isset($field['step']) ? 'step="' . $e($field['step']) . '"' : '' ?>
                      <?= isset($field['maxLength']) ? 'maxlength="' . $e($field['maxLength']) . '"' : '' ?>
                      <?= ($field['required'] ?? false) ? 'required' : '' ?>
                    />
                  <?php endif; ?>
                </label>
              <?php endforeach; ?>
              <div class="module-form-actions">
                <button class="primary-btn" type="submit">Guardar</button>
                <a class="filter-chip" href="<?= $e($route) ?>">Cancelar</a>
              </div>
            </form>
            <?php if ($config['table'] === 'calificacion' && $data['recordId'] > 0): ?>
              <?php
              $historyQuery = $data['connection']->prepare(
                  'SELECT nota_anterior, nota_nueva, fecha_modificacion, motivo
                  FROM calificacion_historial
                  WHERE id_calificacion = :record_id
                  ORDER BY fecha_modificacion DESC'
              );
              $historyQuery->execute(['record_id' => $data['recordId']]);
              $gradeHistory = $historyQuery->fetchAll();
              ?>
              <h3 class="history-heading">Historial de correcciones</h3>
              <?php if ($gradeHistory === []): ?>
                <p class="empty-state">Esta nota todavía no tiene correcciones.</p>
              <?php else: ?>
                <div class="table-wrap"><table>
                  <thead><tr><th>Nota anterior</th><th>Nota nueva</th><th>Fecha</th><th>Motivo</th></tr></thead>
                  <tbody>
                    <?php foreach ($gradeHistory as $history): ?>
                      <tr>
                        <td><?= $e($history['nota_anterior']) ?></td>
                        <td><?= $e($history['nota_nueva']) ?></td>
                        <td><?= $e($history['fecha_modificacion']) ?></td>
                        <td><?= $e($history['motivo']) ?></td>
                      </tr>
                    <?php endforeach; ?>
                  </tbody>
                </table></div>
              <?php endif; ?>
            <?php endif; ?>
          </section>
        <?php endif; ?>

        <section class="table-card">
          <form class="list-filters module-filters" method="get" action="<?= $e($route) ?>">
            <label class="search module-search">
              <span class="visually-hidden">Buscar en <?= $e($config['title']) ?></span>
              <input type="search" name="q" value="<?= $e($data['search']) ?>" placeholder="Buscar en <?= $e($config['title']) ?>" />
            </label>
            <div class="filter-group">
              <?php if (isset($config['statusOptions'])): ?>
                <label class="visually-hidden" for="status-filter">Filtrar por estado</label>
                <select class="mini-select" id="status-filter" name="estado">
                  <option value="">Todos los estados</option>
                  <?php foreach ($config['statusOptions'] as [$optionValue, $optionLabel]): ?>
                    <option value="<?= $e($optionValue) ?>" <?= $data['status'] === $optionValue ? 'selected' : '' ?>><?= $e($optionLabel) ?></option>
                  <?php endforeach; ?>
                </select>
              <?php endif; ?>
              <button class="filter-chip active" type="submit">Buscar / filtrar</button>
              <a class="filter-chip" href="<?= $e($route) ?>">Limpiar</a>
            </div>
          </form>

          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <?php foreach ($config['columns'] as $key => $label): ?>
                    <th>
                      <?php if (isset($config['sortOptions'][$key])): ?>
                        <a class="sort-link" href="<?= $e($route . '?' . http_build_query([
                            'q' => $data['search'],
                            'estado' => $data['status'],
                            'orden' => $key,
                            'direccion' => $data['sortKey'] === $key && $data['direction'] === 'ASC' ? 'desc' : 'asc',
                        ])) ?>"><?= $e($label) ?><?= $data['sortKey'] === $key ? ($data['direction'] === 'ASC' ? ' ↑' : ' ↓') : '' ?></a>
                      <?php else: ?>
                        <?= $e($label) ?>
                      <?php endif; ?>
                    </th>
                  <?php endforeach; ?>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                <?php $rowCount = 0; ?>
                <?php while ($row = $data['listQuery']->fetch()): $rowCount++; ?>
                  <tr>
                    <?php foreach ($config['columns'] as $key => $label): ?>
                      <?php
                      $cellValue = $row[$key] ?? '';
                      if ($key === 'importe_cuota') {
                          $cellValue = '$' . number_format((float) $cellValue, 2, ',', '.');
                      } elseif (in_array($key, ['promedio', 'nota', 'nota_maxima'], true) && $cellValue !== '') {
                          $cellValue = number_format((float) $cellValue, 2, ',', '.');
                      } elseif ($key === 'fecha_inscripcion' && $cellValue !== '') {
                          $date = DateTimeImmutable::createFromFormat('!Y-m-d', (string) $cellValue);
                          $cellValue = $date ? $date->format('d/m/Y') : $cellValue;
                      }
                      $isStatus = $key === 'estado';
                      ?>
                      <td>
                        <?php if ($isStatus): ?>
                          <span class="status-badge <?= in_array($cellValue, ['Activo', 'Confirmada', 'Finalizado'], true) ? 'success' : (in_array($cellValue, ['Pendiente', 'Egresado'], true) ? 'warning' : 'neutral') ?>"><?= $e($cellValue) ?></span>
                        <?php else: ?>
                          <?= $e($cellValue === null || $cellValue === '' ? '—' : $cellValue) ?>
                        <?php endif; ?>
                      </td>
                    <?php endforeach; ?>
                    <td>
                      <div class="actions module-actions">
                        <a class="filter-chip" href="<?= $e($route) ?>?editar=<?= $e($row['id']) ?>" aria-label="Editar registro <?= $e($row['id']) ?>">Editar</a>
                        <?php if (isset($config['deactivateValue']) && ($row['estado'] ?? '') !== $config['deactivateValue']): ?>
                          <form method="post" action="<?= $e($route) ?>" onsubmit="return confirm('¿Confirmás cambiar el estado de este registro?');">
                            <input type="hidden" name="csrf_token" value="<?= $e($_SESSION['module_csrf']) ?>" />
                            <input type="hidden" name="action" value="deactivate" />
                            <input type="hidden" name="id" value="<?= $e($row['id']) ?>" />
                            <button class="icon-btn small delete" type="submit">Baja</button>
                          </form>
                        <?php endif; ?>
                        <?php if (isset($config['deleteGuardSql']) || $config['table'] === 'calificacion'): ?>
                          <form method="post" action="<?= $e($route) ?>" onsubmit="return confirm('Esta eliminación es permanente. ¿Continuar?');">
                            <input type="hidden" name="csrf_token" value="<?= $e($_SESSION['module_csrf']) ?>" />
                            <input type="hidden" name="action" value="delete" />
                            <input type="hidden" name="id" value="<?= $e($row['id']) ?>" />
                            <button class="icon-btn small delete" type="submit">Eliminar</button>
                          </form>
                        <?php endif; ?>
                      </div>
                    </td>
                  </tr>
                <?php endwhile; ?>
                <?php if ($rowCount === 0): ?>
                  <tr><td colspan="<?= count($config['columns']) + 1 ?>" class="empty-state">No hay registros que coincidan con la búsqueda.</td></tr>
                <?php endif; ?>
              </tbody>
            </table>
          </div>

          <?php
          $queryBase = ['q' => $data['search'], 'estado' => $data['status']];
          ?>
          <div class="pager">
            <p>Mostrando <b><?= $data['totalRows'] === 0 ? 0 : (($data['page'] - 1) * $data['perPage']) + 1 ?>–<?= min($data['page'] * $data['perPage'], $data['totalRows']) ?></b> de <b><?= number_format($data['totalRows'], 0, ',', '.') ?></b> registros</p>
            <div class="page-controls">
              <?php if ($data['page'] > 1): ?>
                <a class="page-btn" href="<?= $e($route . '?' . http_build_query($queryBase + ['pagina' => $data['page'] - 1])) ?>" aria-label="Página anterior">‹</a>
              <?php endif; ?>
              <span class="page-btn active" aria-current="page"><?= $data['page'] ?></span>
              <?php if ($data['page'] < $data['totalPages']): ?>
                <a class="page-btn" href="<?= $e($route . '?' . http_build_query($queryBase + ['pagina' => $data['page'] + 1])) ?>" aria-label="Página siguiente">›</a>
              <?php endif; ?>
            </div>
          </div>
        </section>
      <?php endif; ?>
    </main>
  </div>
</body>
</html>
