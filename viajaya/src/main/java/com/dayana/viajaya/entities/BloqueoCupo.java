
package com.dayana.viajaya.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bloqueos_cupos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BloqueoCupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "reserva_id", nullable = false)
    private Integer reservaId;

    @Column(name = "vuelo_id", nullable = false)
    private Integer vueloId;

    @Column(name = "cantidad_cupos", nullable = false)
    private Integer cantidadCupos;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDateTime fechaVencimiento;

    /** Valores canónicos: ACTIVO | VENCIDO */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;
}
