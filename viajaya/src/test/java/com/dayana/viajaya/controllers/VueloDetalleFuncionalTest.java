package com.dayana.viajaya.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas del detalle de vuelo (PVY-14 / PVY-107).
 * Cubren GET /vuelos/{id}: 200 con datos del itinerario y 404 si no existe.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PVY-14 Consultar detalle de un vuelo")
class VueloDetalleFuncionalTest {

    @Autowired
    private MockMvcTester mvc;

    @Nested
    @DisplayName("GET /vuelos/{id}")
    class DetallePorId {

        @Test
        @DisplayName("Un vuelo existente responde 200 con itinerario, tarifas, escalas y equipaje")
        void vueloExistenteRetornaDetalleCompleto() {
            MvcTestResult resultado = mvc.get().uri("/vuelos/1").exchange();

            assertThat(resultado).hasStatusOk();
            assertThat(resultado).bodyJson().extractingPath("$.id").isEqualTo(1);
            assertThat(resultado).bodyJson().extractingPath("$.aerolinea").isEqualTo("Avianca");
            assertThat(resultado).bodyJson().extractingPath("$.origen").isEqualTo("Bogotá");
            assertThat(resultado).bodyJson().extractingPath("$.destino").isEqualTo("Cartagena");
            assertThat(resultado).bodyJson().extractingPath("$.fechaSalida").isEqualTo("2026-12-15");
            assertThat(resultado).bodyJson().extractingPath("$.horaSalida").asString().startsWith("08:30");
            assertThat(resultado).bodyJson().extractingPath("$.duracionMinutos").isEqualTo(90);
            assertThat(resultado).bodyJson().extractingPath("$.precio").isEqualTo(350000.0);
            assertThat(resultado).bodyJson().extractingPath("$.escalas").isEqualTo(0);
            assertThat(resultado).bodyJson().extractingPath("$.equipaje").isEqualTo("1 pieza de mano + 23kg bodega");
            assertThat(resultado).bodyJson().extractingPath("$.cuposDisponibles").isEqualTo(12);
            assertThat(resultado).bodyJson().extractingPath("$.cuposTotales").isEqualTo(180);
        }

        @Test
        @DisplayName("Un id inexistente responde 404")
        void vueloInexistenteRetorna404() {
            assertThat(mvc.get().uri("/vuelos/99999"))
                    .hasStatus(HttpStatus.NOT_FOUND);
        }
    }
}
