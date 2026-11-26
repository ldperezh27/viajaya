-- Datos fijos para las pruebas. Correos @example.com para no apuntar nunca a buzones reales.
-- password_hash = BCrypt de ViajaYa123 (mismo que database/viajaya_completo.sql)
INSERT INTO usuarios (id, nombre, apellido, correo, telefono, password_hash) VALUES
(1, 'Laura', 'Gomez', 'laura@example.com', '3001234567', '$2b$10$iwGdLJbt3RKAxNQwyS.IIep3UtHTJZznOGO796jR3zRgp0sOB.BMu'),
(2, 'Carlos', 'Ramirez', 'carlos@example.com', '3107654321', '$2b$10$iwGdLJbt3RKAxNQwyS.IIep3UtHTJZznOGO796jR3zRgp0sOB.BMu');

-- Fechas futuras fijas para que PVY-9 /buscar no falle por “fecha anterior a hoy”.
INSERT INTO vuelos (
    id, aerolinea, origen, destino, fecha_salida, hora_salida,
    fecha_llegada, hora_llegada, duracion_minutos, precio, escalas, equipaje,
    cupos_disponibles, cupos_totales
) VALUES
(1, 'Avianca', 'Bogota', 'Medellin', '2027-06-15', '08:00:00',
 '2027-06-15', '09:05:00', 65, 189000.00, 0, '1 pieza de mano 10kg', 12, 12),
(2, 'LATAM', 'Bogota', 'Medellin', '2027-06-15', '14:30:00',
 '2027-06-15', '15:40:00', 70, 215000.00, 0, 'Cabina + bodega 23kg', 8, 8),
(3, 'Wingo', 'Bogota', 'Cartagena', '2027-06-16', '06:15:00',
 '2027-06-16', '07:45:00', 90, 159000.00, 0, '1 pieza de mano 10kg', 2, 20),
(4, 'Avianca', 'Medellin', 'Bogota', '2027-06-17', '10:00:00',
 '2027-06-17', '11:10:00', 70, 195000.00, 0, '1 pieza de mano 10kg', 15, 15);

-- Estados de reserva alineados con el código: PENDIENTE | CONFIRMADA | EXPIRADA
INSERT INTO reservas (id, fecha_reserva, destino, valor, estado, usuario_id) VALUES
(1, '2026-11-18', 'Cartagena', 850000.00, 'CONFIRMADA', 1),
(2, '2026-11-19', 'Medellin', 450000.00, 'PENDIENTE', 1),
(3, '2026-11-20', 'Santa Marta', 1200000.00, 'CONFIRMADA', 2);
