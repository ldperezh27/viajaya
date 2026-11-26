package com.dayana.viajaya.controllers;

import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas funcionales de la historia PVY-22 (Consultar confirmación de reserva).
 * Recorren cada endpoint de punta a punta: controlador, servicios y base de datos H2 con datos fijos.
 * El servidor de correo se reemplaza por un mock, así que ninguna prueba envía correos reales.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PVY-22 Consultar confirmación de reserva")
class ConfirmacionReservaFuncionalTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private JavaMailSender mailSender;

    @BeforeEach
    void configurarCorreo() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
    }

    @Nested
    @DisplayName("Resumen de la confirmación (PVY-87)")
    class ResumenConfirmacion {

        @Test
        @DisplayName("El titular ve código, servicio, fecha, titular y estado de su reserva")
        void titularVeConfirmacionCompleta() {
            MvcTestResult resultado = mvc.get().uri("/reservas/1/confirmacion?usuarioId=1").exchange();

            assertThat(resultado).hasStatusOk();
            assertThat(resultado).bodyJson().extractingPath("$.codigo").isEqualTo("VJY-000001");
            assertThat(resultado).bodyJson().extractingPath("$.servicio").isEqualTo("Reserva de viaje");
            assertThat(resultado).bodyJson().extractingPath("$.destino").isEqualTo("Cartagena");
            assertThat(resultado).bodyJson().extractingPath("$.fechaReserva").isEqualTo("2026-11-18");
            assertThat(resultado).bodyJson().extractingPath("$.estado").isEqualTo("CONFIRMADA");
            assertThat(resultado).bodyJson().extractingPath("$.titular.nombre").isEqualTo("Laura");
            assertThat(resultado).bodyJson().extractingPath("$.titular.apellido").isEqualTo("Gomez");
        }

        @Test
        @DisplayName("Una reserva de otro usuario responde 404 sin revelar datos")
        void reservaAjenaNoRevelaDatos() {
            assertThat(mvc.get().uri("/reservas/3/confirmacion?usuarioId=1"))
                    .hasStatus(HttpStatus.NOT_FOUND)
                    .body().isEmpty();
        }

        @Test
        @DisplayName("Una reserva que no existe responde 404")
        void reservaInexistente() {
            assertThat(mvc.get().uri("/reservas/999/confirmacion?usuarioId=1"))
                    .hasStatus(HttpStatus.NOT_FOUND)
                    .body().isEmpty();
        }

        @Test
        @DisplayName("Sin indicar el usuario responde 400")
        void sinUsuario() {
            assertThat(mvc.get().uri("/reservas/1/confirmacion"))
                    .hasStatus(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("Comprobante en PDF (PVY-88)")
    class ComprobantePdf {

        @Test
        @DisplayName("El titular descarga un PDF con los datos de su reserva")
        void titularDescargaPdf() throws IOException {
            MvcTestResult resultado = mvc.get().uri("/reservas/1/comprobante?usuarioId=1").exchange();

            assertThat(resultado)
                    .hasStatusOk()
                    .hasContentType(MediaType.APPLICATION_PDF)
                    .hasHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comprobante-reserva-1.pdf\"");

            String texto = extraerTextoPdf(resultado.getResponse().getContentAsByteArray());
            assertThat(texto).contains(
                    "VJY-000001", "CONFIRMADA", "Laura Gomez", "laura@example.com",
                    "Reserva de viaje", "Cartagena", "18/11/2026", "850.000,00");
        }

        @Test
        @DisplayName("No genera el PDF de una reserva de otro usuario")
        void noGeneraPdfReservaAjena() {
            assertThat(mvc.get().uri("/reservas/3/comprobante?usuarioId=1"))
                    .hasStatus(HttpStatus.NOT_FOUND)
                    .body().isEmpty();
        }
    }

    @Nested
    @DisplayName("Envío del comprobante por correo (PVY-89)")
    class EnvioCorreo {

        @Test
        @DisplayName("Envía el PDF al correo registrado del titular")
        void enviaComprobanteAlTitular() throws Exception {
            assertThat(mvc.post().uri("/reservas/1/comprobante/correo?usuarioId=1"))
                    .hasStatusOk()
                    .bodyJson().extractingPath("$.mensaje")
                    .isEqualTo("El comprobante fue enviado al correo laura@example.com");

            ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(captor.capture());
            MimeMessage correo = captor.getValue();

            assertThat(correo.getAllRecipients()).extracting(Object::toString).containsExactly("laura@example.com");
            assertThat(correo.getSubject()).contains("VJY-000001");

            Part adjunto = buscarAdjunto(correo, "comprobante-reserva-1.pdf");
            assertThat(adjunto).as("PDF adjunto en el correo").isNotNull();
            assertThat(adjunto.getDataHandler().getContentType()).isEqualTo("application/pdf");
            assertThat(extraerTextoPdf(adjunto.getInputStream().readAllBytes())).contains("VJY-000001");
        }

        @Test
        @DisplayName("No envía correo si la reserva pertenece a otro usuario")
        void noEnviaReservaAjena() {
            assertThat(mvc.post().uri("/reservas/3/comprobante/correo?usuarioId=1"))
                    .hasStatus(HttpStatus.NOT_FOUND);

            verify(mailSender, never()).send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("Si el servidor de correo falla responde 503")
        void servidorCorreoNoDisponible() {
            doThrow(new MailSendException("Servidor SMTP no disponible"))
                    .when(mailSender).send(any(MimeMessage.class));

            assertThat(mvc.post().uri("/reservas/1/comprobante/correo?usuarioId=1"))
                    .hasStatus(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private String extraerTextoPdf(byte[] pdf) throws IOException {
        try (PdfReader lector = new PdfReader(pdf)) {
            assertThat(lector.getNumberOfPages()).isEqualTo(1);
            return new PdfTextExtractor(lector).getTextFromPage(1);
        }
    }

    private Part buscarAdjunto(Part parte, String nombreArchivo) throws Exception {
        if (nombreArchivo.equals(parte.getFileName())) {
            return parte;
        }
        if (parte.getContent() instanceof Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                Part encontrado = buscarAdjunto(multipart.getBodyPart(i), nombreArchivo);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }
}
