-- Datos fijos para las pruebas. Correos @example.com para no apuntar nunca a buzones reales.
-- password_hash = BCrypt de ViajaYa123 (mismo que database/viajaya_completo.sql)
INSERT INTO usuarios (id, nombre, apellido, correo, telefono, password_hash) VALUES
(1, 'Laura', 'Gomez', 'laura@example.com', '3001234567', '$2b$10$iwGdLJbt3RKAxNQwyS.IIep3UtHTJZznOGO796jR3zRgp0sOB.BMu'),
(2, 'Carlos', 'Ramirez', 'carlos@example.com', '3107654321', '$2b$10$iwGdLJbt3RKAxNQwyS.IIep3UtHTJZznOGO796jR3zRgp0sOB.BMu');

INSERT INTO vuelos (
    id, aerolinea, origen, destino, fecha_salida, hora_salida, fecha_llegada, hora_llegada,
    duracion_minutos, precio, escalas, equipaje, cupos_disponibles, cupos_totales
) VALUES
(1, 'Avianca', 'Bogotá', 'Cartagena', '2026-12-15', '08:30:00', '2026-12-15', '10:00:00',
 90, 350000.00, 0, '1 pieza de mano + 23kg bodega', 12, 180),
(2, 'LATAM', 'Bogotá', 'Medellín', '2026-12-20', '14:00:00', '2026-12-20', '15:10:00',
 70, 220000.00, 1, '1 pieza de mano', 8, 160);

INSERT INTO reservas (id, fecha_reserva, destino, valor, estado, usuario_id) VALUES
(1, '2026-11-18', 'Cartagena', 850000.00, 'Confirmada', 1),
(2, '2026-11-19', 'Medellin', 450000.00, 'Pendiente', 1),
(3, '2026-11-20', 'Santa Marta', 1200000.00, 'Confirmada', 2);
