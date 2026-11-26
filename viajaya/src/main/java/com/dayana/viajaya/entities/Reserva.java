
package com.dayana.viajaya.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "reservas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "fecha_reserva", nullable = false)
    private LocalDate fechaReserva;

    @Column(name = "destino", nullable = false, length = 100)
    private String destino;

    @Column(name = "valor", nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    /** Valores canónicos: PENDIENTE | CONFIRMADA | EXPIRADA */
    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(name = "vuelo_id")
    private Integer vueloId;

    @Column(name = "cantidad_pasajeros")
    private Integer cantidadPasajeros;

    @Column(name = "correo_contacto", length = 150)
    private String correoContacto;

    @Column(name = "telefono_contacto", length = 20)
    private String telefonoContacto;
}
