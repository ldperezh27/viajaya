-- =============================================================================
-- ViajaYa — script completo de base de datos (equipo)
-- MySQL 8 · Docker bd_viajaya · puerto 3310 · root / admin123
-- Ejecutar en MySQL Workbench o cliente mysql sobre una BD vacía / nueva.
-- spring.jpa.hibernate.ddl-auto=none  →  este script crea el esquema.
--
-- Incluye: usuarios (+ password_hash), vuelos, reservas (PVY-20),
--          pasajeros, bloqueos_cupos.
-- Si la BD ya existía sin estas columnas, usa en su lugar:
--   database/pvy18_autenticacion.sql y database/pvy20_iniciar_reserva.sql
-- =============================================================================

CREATE DATABASE IF NOT EXISTS viajaya;
USE viajaya;

-- -----------------------------------------------------------------------------
-- Tablas
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    password_hash VARCHAR(255) NULL
);

CREATE TABLE IF NOT EXISTS vuelos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    aerolinea VARCHAR(100) NOT NULL,
    origen VARCHAR(100) NOT NULL,
    destino VARCHAR(100) NOT NULL,
    fecha_salida DATE NOT NULL,
    hora_salida TIME NOT NULL,
    fecha_llegada DATE NULL,
    hora_llegada TIME NULL,
    duracion_minutos INT NULL,
    precio DECIMAL(12, 2) NOT NULL,
    escalas INT NOT NULL DEFAULT 0,
    equipaje VARCHAR(255) NULL,
    cupos_disponibles INT NOT NULL DEFAULT 0,
    cupos_totales INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS reservas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha_reserva DATE NOT NULL,
    destino VARCHAR(100) NOT NULL,
    valor DECIMAL(12,2) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    usuario_id INT NOT NULL,
    vuelo_id INT NULL,
    cantidad_pasajeros INT NULL,
    correo_contacto VARCHAR(150) NULL,
    telefono_contacto VARCHAR(20) NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (vuelo_id) REFERENCES vuelos(id)
);

CREATE TABLE IF NOT EXISTS pasajeros (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reserva_id INT NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    tipo_documento VARCHAR(30) NOT NULL,
    numero_documento VARCHAR(30) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    FOREIGN KEY (reserva_id) REFERENCES reservas(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bloqueos_cupos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reserva_id INT NOT NULL UNIQUE,
    vuelo_id INT NOT NULL,
    cantidad_cupos INT NOT NULL,
    fecha_inicio DATETIME NOT NULL,
    fecha_vencimiento DATETIME NOT NULL,
    estado VARCHAR(20) NOT NULL,
    FOREIGN KEY (reserva_id) REFERENCES reservas(id),
    FOREIGN KEY (vuelo_id) REFERENCES vuelos(id)
);

-- -----------------------------------------------------------------------------
-- Datos de prueba
-- Clave de los usuarios semilla: ViajaYa123
-- Fechas de vuelos: ajústalas a >= hoy si /api/vuelos/buscar no las muestra.
-- -----------------------------------------------------------------------------

INSERT INTO usuarios (nombre, apellido, correo, telefono, password_hash)
VALUES
('Laura', 'Gomez', 'laura@gmail.com', '3001234567', '$2b$10$iwGdLJbt3RKAxNQwyS.IIep3UtHTJZznOGO796jR3zRgp0sOB.BMu'),
('Carlos', 'Ramirez', 'carlos@gmail.com', '3107654321', '$2b$10$iwGdLJbt3RKAxNQwyS.IIep3UtHTJZznOGO796jR3zRgp0sOB.BMu');

INSERT INTO vuelos (
    aerolinea, origen, destino, fecha_salida, hora_salida,
    fecha_llegada, hora_llegada, duracion_minutos, precio, escalas, equipaje, cupos_disponibles, cupos_totales
) VALUES
('Avianca', 'Bogota', 'Medellin', '2027-06-15', '08:00:00', '2027-06-15', '09:05:00', 65, 189000.00, 0, '1 pieza de mano 10kg', 12, 12),
('LATAM', 'Bogota', 'Medellin', '2027-06-15', '14:30:00', '2027-06-15', '15:40:00', 70, 215000.00, 0, 'Cabina + bodega 23kg', 8, 8),
('Wingo', 'Bogota', 'Cartagena', '2027-06-16', '06:15:00', '2027-06-16', '07:45:00', 90, 159000.00, 0, '1 pieza de mano 10kg', 20, 20),
('Avianca', 'Medellin', 'Bogota', '2027-06-17', '10:00:00', '2027-06-17', '11:10:00', 70, 195000.00, 0, '1 pieza de mano 10kg', 15, 15),
('LATAM', 'Bogota', 'Cali', '2027-06-18', '16:00:00', '2027-06-18', '17:10:00', 70, 175000.00, 1, 'Escala en Pereira; cabina 10kg', 5, 5);

-- Estados de reserva alineados con el código: PENDIENTE | CONFIRMADA | EXPIRADA
INSERT INTO reservas (fecha_reserva, destino, valor, estado, usuario_id, vuelo_id, cantidad_pasajeros, correo_contacto, telefono_contacto)
VALUES
('2026-11-18', 'Cartagena', 850000, 'CONFIRMADA', 1, 3, 1, 'laura@gmail.com', '3001234567'),
('2026-11-19', 'Medellin', 450000, 'PENDIENTE', 1, 1, 1, 'laura@gmail.com', '3001234567'),
('2026-11-20', 'Santa Marta', 1200000, 'CONFIRMADA', 2, NULL, NULL, 'carlos@gmail.com', '3107654321');

-- -----------------------------------------------------------------------------
-- Verificación
-- -----------------------------------------------------------------------------

SHOW TABLES;

SELECT COUNT(*) AS total_usuarios FROM usuarios;
SELECT COUNT(*) AS total_reservas FROM reservas;
SELECT COUNT(*) AS total_vuelos FROM vuelos;

SELECT u.nombre, u.apellido, r.destino, r.fecha_reserva, r.valor, r.estado
FROM usuarios u
INNER JOIN reservas r ON u.id = r.usuario_id;

SELECT id, aerolinea, origen, destino, fecha_salida, precio, cupos_disponibles
FROM vuelos;
