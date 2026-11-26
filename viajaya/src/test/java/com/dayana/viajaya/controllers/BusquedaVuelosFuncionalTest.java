package com.dayana.viajaya.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas funcionales PVY-9 / PVY-31: búsqueda de vuelos por origen, destino, fecha y pasajeros.
 * Recorren controlador → servicio → H2 con datos fijos de data.sql.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PVY-9 Buscar vuelos por destino y fechas")
class BusquedaVuelosFuncionalTest {

    private static final String FECHA_VALIDA = "2027-06-15";

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private JavaMailSender mailSender;

    @Nested
    @DisplayName("Búsqueda con parámetros válidos")
    class BusquedaValida {

        @Test
        @DisplayName("Con origen, destino, fecha y pasajeros válidos retorna las alternativas coincidentes")
        void buscaYRetornaResultados() {
            MvcTestResult resultado = mvc.get()
                    .uri("/vuelos/buscar?origen=Bogota&destino=Medellin&fechaSalida={fecha}&pasajeros=1", FECHA_VALIDA)
                    .exchange();

            assertThat(resultado).hasStatusOk();
            assertThat(resultado).bodyJson().extractingPath("$.length()").asNumber().isEqualTo(2);
            assertThat(resultado).bodyJson().extractingPath("$[0].origen").isEqualTo("Bogota");
            assertThat(resultado).bodyJson().extractingPath("$[0].destino").isEqualTo("Medellin");
            assertThat(resultado).bodyJson().extractingPath("$[0].aerolinea").isIn("Avianca", "LATAM");
            assertThat(resultado).bodyJson().extractingPath("$[1].aerolinea").isIn("Avianca", "LATAM");
        }

        @Test
        @DisplayName("Filtra por cupos: más pasajeros que cupos disponibles deja fuera esa oferta")
        void filtraPorCuposDisponibles() {
            MvcTestResult resultado = mvc.get()
                    .uri("/vuelos/buscar?origen=Bogota&destino=Cartagena&fechaSalida=2027-06-16&pasajeros=5")
                    .exchange();

            assertThat(resultado).hasStatusOk();
            assertThat(resultado).bodyJson().extractingPath("$.mensaje").asString()
                    .contains("No hay resultados");
        }
    }

    @Nested
    @DisplayName("Validaciones de campos (PVY-28)")
    class Validaciones {

        @Test
        @DisplayName("Sin origen responde 400")
        void origenObligatorio() {
            assertThat(mvc.get()
                    .uri("/vuelos/buscar?destino=Medellin&fechaSalida={fecha}&pasajeros=1", FECHA_VALIDA)
                    .exchange())
                    .hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Sin destino responde 400")
        void destinoObligatorio() {
            assertThat(mvc.get()
                    .uri("/vuelos/buscar?origen=Bogota&fechaSalida={fecha}&pasajeros=1", FECHA_VALIDA)
                    .exchange())
                    .hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Origen igual a destino responde 400")
        void origenYDestinoDebenSerDiferentes() {
            assertThat(mvc.get()
                    .uri("/vuelos/buscar?origen=Bogota&destino=Bogota&fechaSalida={fecha}&pasajeros=1", FECHA_VALIDA)
                    .exchange())
                    .hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Fecha anterior a hoy responde 400")
        void fechaNoPuedeSerPasada() {
            String fechaPasada = LocalDate.now().minusDays(1).toString();
            assertThat(mvc.get()
                    .uri("/vuelos/buscar?origen=Bogota&destino=Medellin&fechaSalida={fecha}&pasajeros=1", fechaPasada)
                    .exchange())
                    .hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Sin fecha de salida responde 400")
        void fechaObligatoria() {
            assertThat(mvc.get()
                    .uri("/vuelos/buscar?origen=Bogota&destino=Medellin&pasajeros=1")
                    .exchange())
                    .hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Pasajeros menor que 1 responde 400")
        void pasajerosDebenSerMayoresQueCero() {
            assertThat(mvc.get()
                    .uri("/vuelos/buscar?origen=Bogota&destino=Medellin&fechaSalida={fecha}&pasajeros=0", FECHA_VALIDA)
                    .exchange())
                    .hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Sin pasajeros responde 400")
        void pasajerosObligatorios() {
            assertThat(mvc.get()
                    .uri("/vuelos/buscar?origen=Bogota&destino=Medellin&fechaSalida={fecha}", FECHA_VALIDA)
                    .exchange())
                    .hasStatus(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("Sin resultados (PVY-30)")
    class SinResultados {

        @Test
        @DisplayName("Cuando no hay vuelos muestra mensaje claro para modificar criterios")
        void mensajeCuandoNoHayCoincidencias() {
            MvcTestResult resultado = mvc.get()
                    .uri("/vuelos/buscar?origen=Cali&destino=Pereira&fechaSalida=2027-12-01&pasajeros=1")
                    .exchange();

            assertThat(resultado).hasStatusOk();
            assertThat(resultado).bodyJson().extractingPath("$.mensaje").asString()
                    .contains("No hay resultados")
                    .contains("Modifique");
        }
    }
}
