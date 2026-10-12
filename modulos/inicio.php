<?php
declare(strict_types=1);

require_once dirname(__DIR__) . '/clases/database.php';
require_once dirname(__DIR__) . '/clases/dashboard.php';

$dashboardData = null;

try {
    $dashboardData = getDashboardData(databaseConnection());
} catch (PDOException $exception) {
    error_log('Dashboard page database error: ' . $exception->getMessage());
}
?>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Academia Integral XXI - Inicio</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet"/>
  <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
  <script>tailwind.config={theme:{extend:{fontFamily:{sans:['Inter','sans-serif']}}}};</script>
  <link rel="stylesheet" href="../assets/css/styles.css" />
</head>
<body class="app">
  <aside class="sidebar">
    <div class="brand"><div class="brand-mark"><svg viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg" fill="none"><path d="M24 4L7 9V22C7 32.5 14.2 42.3 24 45C33.8 42.3 41 32.5 41 22V9L24 4Z" fill="rgba(255,255,255,0.08)" stroke="white" stroke-width="2.5" stroke-linejoin="round"/><path d="M16 28C19 26 22 26 24 28C26 26 29 26 32 28V19C29 17 26 17 24 19C22 17 19 17 16 19V28Z" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><text x="24" y="24" text-anchor="middle" font-size="9" font-weight="700" fill="white" font-family="Inter, sans-serif" letter-spacing="1">AI</text></svg></div><div class="brand-text"><span>Academia</span><small>Integral XXI</small></div></div>
    <nav class="nav">
      <a class="nav-link active" href="inicio.php"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/></svg><span>Inicio</span></a>
      <a class="nav-link" href="cursos.php"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 14l9-5-9-5-9 5 9 5z"/><path d="M12 14l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998 12.078 12.078 0 01.665-6.479L12 14z"/><path d="M12 14v7"/></svg><span>Cursos</span></a>
      <a class="nav-link" href="calificaciones.php"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"/></svg><span>Calificaciones</span></a>
      <a class="nav-link" href="inscripciones.php"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"/></svg><span>Inscripciones</span></a>
      <a class="nav-link" href="alumnos.php"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/></svg><span>Alumnos</span></a>
      <a class="nav-link" href="docentes.php"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg><span>Docentes</span></a>
    </nav>
    <div class="nav-footer"><a class="nav-link" href="#"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg><span>Configuración</span></a></div>
  </aside>

  <div class="main-panel">
    <header class="topbar">
      <div class="search"><input type="text" placeholder="Buscar" /><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.35-4.35"/></svg></div>
      <div class="topbar-tools">
        <button class="notif" type="button" aria-label="Notificaciones"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/></svg><span class="notif-dot"></span></button>
        <div class="user"><div class="avatar"><svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path fill-rule="evenodd" clip-rule="evenodd" d="M12 2a5 5 0 100 10 5 5 0 000-10zm0 12c-5.33 0-8 2.67-8 6a1 1 0 001 1h14a1 1 0 001-1c0-3.33-2.67-6-8-6z"/></svg></div><div class="user-meta"><strong>Academia Integral XXI</strong><small>Panel institucional</small></div></div>
      </div>
    </header>

    <main class="content">
      <div class="page-head"><div><h1>Inicio</h1><p>Resumen general de la academia y el estado de cada área.</p></div></div>

      <div class="dashboard-grid">
        <div class="summary-card">
          <div class="summary-head"><span class="summary-title">Alumnos activos</span><div class="summary-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/></svg></div></div>
          <div class="summary-amount" id="active-students">—</div>
          <div class="summary-meta">Según el estado de matrícula</div>
        </div>

        <div class="summary-card">
          <div class="summary-head"><span class="summary-title">Inscripciones confirmadas</span><div class="summary-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2"/></svg></div></div>
          <div class="summary-amount" id="confirmed-enrollments">—</div>
          <div class="summary-meta">Pendientes: <strong id="pending-enrollments">—</strong></div>
        </div>

        <div class="summary-card">
          <div class="summary-head"><span class="summary-title">Docentes activos</span><div class="summary-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg></div></div>
          <div class="summary-amount" id="active-teachers">—</div>
          <div class="summary-meta">Con estado activo</div>
        </div>

        <div class="summary-card">
          <div class="summary-head"><span class="summary-title">Cursos activos</span><div class="summary-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 14l9-5-9-5-9 5 9 5z"/><path d="M12 14l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998 12.078 12.078 0 01.665-6.479L12 14z"/></svg></div></div>
          <div class="summary-amount" id="active-courses">—</div>
          <div class="summary-meta">En períodos lectivos</div>
        </div>
      </div>

      <div class="panel-grid">
        <div class="panel-card">
          <div class="card-header"><h2>Inscripciones por mes</h2><span class="pill neutral">Últimos 6 meses</span></div>
          <div class="chart" id="monthly-chart" aria-live="polite" aria-label="Inscripciones totales y confirmadas por mes"></div>
          <div class="legend"><span><i style="background:#2563eb"></i> Inscripciones</span><span><i style="background:#06b6d4"></i> Confirmadas</span></div>
        </div>

        <div class="panel-card">
          <div class="card-header"><h2>Últimas inscripciones</h2><span class="pill info">Recientes</span></div>
          <div class="activity-list" id="recent-enrollments" aria-live="polite"></div>
        </div>
      </div>

      <div class="panel-grid">
        <div class="panel-card">
          <div class="card-header"><h2>Resumen académico</h2><span class="pill neutral" id="active-period">Período lectivo</span></div>
          <div class="kpi-row"><div><div class="kpi-value" id="average-grade">—</div><div class="kpi-label">Promedio general (sobre 10)</div></div></div>
          <div class="kpi-row" style="margin-top:1rem"><div><div class="kpi-value" id="approved-results">—</div><div class="kpi-label">Materias aprobadas</div></div></div>
          <div class="kpi-row" style="margin-top:1rem"><div><div class="kpi-value" id="in-progress-results">—</div><div class="kpi-label">Materias en curso</div></div></div>
        </div>

        <div class="panel-card">
          <div class="card-header"><h2>Estado de inscripciones</h2><span class="pill info">Total: <span id="total-enrollments">—</span></span></div>
          <div class="task-list" id="enrollment-status" aria-live="polite"></div>
        </div>
      </div>
    </main>
    <p class="dashboard-error" id="dashboard-error" role="alert" <?= $dashboardData === null ? '' : 'hidden' ?>>No se pudieron cargar los datos del panel.</p>
  </div>
  <script>
    window.dashboardData = <?= json_encode(
        $dashboardData,
        JSON_UNESCAPED_UNICODE
            | JSON_HEX_TAG
            | JSON_HEX_AMP
            | JSON_HEX_APOS
            | JSON_HEX_QUOT
            | JSON_THROW_ON_ERROR
    ) ?>;
  </script>
  <script src="../assets/js/dashboard.js" defer></script>
</body>
</html>
