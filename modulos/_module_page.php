<?php
declare(strict_types=1);

// Definimos los módulos del sistema con sus respectivos iconos SVG
$modules = [
    'inicio.php' => ['label' => 'Inicio', 'svg' => '<path d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/>'],
    'cursos.php' => ['label' => 'Cursos', 'svg' => '<path d="M12 14l9-5-9-5-9 5 9 5z"/><path d="M12 14l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998 12.078 12.078 0 01.665-6.479L12 14z"/><path d="M12 14v7"/>'],
    'calificaciones.php' => ['label' => 'Calificaciones', 'svg' => '<path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"/>'],
    'inscripciones.php' => ['label' => 'Inscripciones', 'svg' => '<path d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"/>'],
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
  <!-- Ruta corregida a la carpeta local de estilos -->
  <link rel="stylesheet" href="estilos/styles.css" />
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
      <?php foreach ($modules as $href => $info): ?>
        <a class="nav-link<?= $currentRoute === $href ? ' active' : '' ?>" href="<?= $e($href) ?>">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><?= $info['svg'] ?></svg>
          <span><?= $e($info['label']) ?></span>
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
