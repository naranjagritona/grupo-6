-- Ejecutar una sola vez sobre la estructura anterior.
-- La cuota general se conserva en configuracion_cuota y los pagos mantienen su importe.
ALTER TABLE inscripcion DROP COLUMN importe_cuota;

-- Ordenamos los identificadores existentes y dejamos que la base genere los siguientes.
SET @academia_historial = 0;
UPDATE calificacion_historial
SET id_calificacion_historial = (@academia_historial := @academia_historial + 1)
ORDER BY fecha_modificacion, id_calificacion;
ALTER TABLE calificacion_historial
MODIFY id_calificacion_historial INT NOT NULL AUTO_INCREMENT,
ADD PRIMARY KEY (id_calificacion_historial);

-- Guardamos los datos anteriores y posteriores del movimiento sin recortar el detalle.
ALTER TABLE log_usuario
MODIFY valor_anterior TEXT NOT NULL,
MODIFY valor_nuevo TEXT NOT NULL;
