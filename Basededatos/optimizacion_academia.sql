-- Mantenimiento puntual de Academia Integral XXI.
-- Antes de ejecutarlo: generar un respaldo completo y probar su restauración.
-- No elimina registros ni modifica columnas, claves o permisos.
-- Aplicar la compactación solo si la inspección previa justifica recuperar espacio.

USE `s23_academia_integral_xxi`;
SET SESSION lock_wait_timeout = 5;
SET SESSION innodb_lock_wait_timeout = 5;

-- Esta tabla tenía espacio libre interno en la inspección del 19/10/2026.
OPTIMIZE TABLE `calificacion`;

-- Actualizamos las estadísticas utilizadas por el planificador de consultas.
ANALYZE TABLE
    `alumno`, `calificacion`, `calificacion_historial`, `columna_notas`,
    `curso`, `docente`, `inscripcion`, `log_usuario`, `materia`,
    `periodo_lectivo`, `responsable`, `resultado_materia`, `rol`, `usuario`;
