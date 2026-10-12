'use strict';

const numberFormat = new Intl.NumberFormat('es-AR');
const gradeFormat = new Intl.NumberFormat('es-AR', {
  minimumFractionDigits: 1,
  maximumFractionDigits: 2
});
const statusClasses = {
  Confirmada: 'success',
  Pendiente: 'warning',
  Finalizada: 'neutral',
  Cancelada: 'danger'
};
const statusDotClasses = {
  Confirmada: 'green',
  Pendiente: 'orange',
  Cancelada: 'red'
};

function setText(id, value) {
  document.getElementById(id).textContent = value;
}

function renderMonthlyChart(months) {
  const chart = document.getElementById('monthly-chart');
  const maxCount = Math.max(1, ...months.map((month) => month.total));
  const monthFormatter = new Intl.DateTimeFormat('es-AR', { month: 'short' });

  chart.replaceChildren();
  months.forEach((month) => {
    const column = document.createElement('div');
    column.className = 'bar-col';

    const bars = document.createElement('div');
    bars.className = 'bar-pair';
    [
      { value: month.total, className: 'bar', label: 'Total' },
      { value: month.confirmed, className: 'bar alt', label: 'Confirmadas' }
    ].forEach((item) => {
      const bar = document.createElement('div');
      bar.className = item.className;
      bar.style.height = `${(item.value / maxCount) * 100}%`;
      bar.title = `${item.label}: ${numberFormat.format(item.value)}`;
      bars.append(bar);
    });

    const label = document.createElement('span');
    label.className = 'bar-label';
    label.textContent = monthFormatter.format(new Date(`${month.month}-01T12:00:00`));

    column.append(bars, label);
    chart.append(column);
  });
}

function renderRecentEnrollments(enrollments) {
  const list = document.getElementById('recent-enrollments');
  list.replaceChildren();

  if (enrollments.length === 0) {
    const emptyMessage = document.createElement('p');
    emptyMessage.className = 'empty-state';
    emptyMessage.textContent = 'Todavía no hay inscripciones registradas.';
    list.append(emptyMessage);
    return;
  }

  enrollments.forEach((enrollment) => {
    const item = document.createElement('div');
    item.className = 'activity-item';

    const info = document.createElement('div');
    info.className = 'activity-info';

    const dot = document.createElement('span');
    dot.className = `dot ${statusDotClasses[enrollment.status] || ''}`.trim();

    const details = document.createElement('div');
    const name = document.createElement('div');
    name.className = 'activity-name';
    name.textContent = enrollment.student;
    const date = document.createElement('div');
    date.className = 'activity-meta';
    date.textContent = new Intl.DateTimeFormat('es-AR').format(
      new Date(`${enrollment.date}T12:00:00`)
    );

    const status = document.createElement('span');
    status.className = `status-badge ${statusClasses[enrollment.status] || 'neutral'}`;
    status.textContent = enrollment.status;

    details.append(name, date);
    info.append(dot, details);
    item.append(info, status);
    list.append(item);
  });
}

function renderEnrollmentStatuses(statuses) {
  const list = document.getElementById('enrollment-status');
  list.replaceChildren();

  statuses.forEach((status) => {
    const item = document.createElement('div');
    item.className = 'task-item';

    const info = document.createElement('div');
    info.className = 'task-info';
    const dot = document.createElement('span');
    dot.className = `dot ${statusDotClasses[status.status] || ''}`.trim();

    const label = document.createElement('div');
    label.className = 'task-name';
    label.textContent = status.status;

    const count = document.createElement('span');
    count.className = 'status-badge neutral';
    count.textContent = numberFormat.format(status.count);

    info.append(dot, label);
    item.append(info, count);
    list.append(item);
  });
}

function loadDashboard() {
  const errorMessage = document.getElementById('dashboard-error');

  try {
    const data = window.dashboardData;
    if (!data) {
      throw new Error('PHP no pudo cargar los datos desde la base.');
    }

    setText('active-students', numberFormat.format(data.stats.activeStudents));
    setText('confirmed-enrollments', numberFormat.format(data.stats.confirmedEnrollments));
    setText('pending-enrollments', numberFormat.format(data.stats.pendingEnrollments));
    setText('active-teachers', numberFormat.format(data.stats.activeTeachers));
    setText('active-courses', numberFormat.format(data.stats.activeCourses));
    setText('total-enrollments', numberFormat.format(data.stats.totalEnrollments));

    setText(
      'active-period',
      data.academic.periodName || 'Sin período activo'
    );
    setText(
      'average-grade',
      data.academic.average === null ? '—' : gradeFormat.format(data.academic.average)
    );
    setText('approved-results', numberFormat.format(data.academic.approved));
    setText('in-progress-results', numberFormat.format(data.academic.inProgress));

    renderMonthlyChart(data.monthlyEnrollments);
    renderRecentEnrollments(data.recentEnrollments);
    renderEnrollmentStatuses(data.enrollmentStatuses);
  } catch (error) {
    console.error('No se pudo cargar el panel:', error);
    errorMessage.hidden = false;
  }
}

loadDashboard();
