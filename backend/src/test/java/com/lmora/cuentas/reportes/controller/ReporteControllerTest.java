package com.lmora.cuentas.reportes.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lmora.cuentas.reportes.dto.ReporteClienteDto;
import com.lmora.cuentas.reportes.dto.ReporteCuentaDto;
import com.lmora.cuentas.reportes.dto.ReporteEstadoCuentaResponseDto;
import com.lmora.cuentas.reportes.dto.ReporteMovimientoDto;
import com.lmora.cuentas.reportes.exception.ReporteInvalidoException;
import com.lmora.cuentas.reportes.service.ReporteService;
import com.lmora.cuentas.shared.exception.GlobalExceptionHandler;
import com.lmora.cuentas.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReporteController.class)
@Import(GlobalExceptionHandler.class)
class ReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReporteService reporteService;

    @Test
    void generarEstadoCuenta_cuandoRequestEsValido_retorna200() throws Exception {
        given(reporteService.generarEstadoCuenta(
                1L,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        )).willReturn(crearReporte());

        mockMvc.perform(get("/reportes")
                        .param("clienteId", "1")
                        .param("fechaDesde", "2026-04-01")
                        .param("fechaHasta", "2026-04-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente.clienteId").value(1))
                .andExpect(jsonPath("$.cuentas[0].numeroCuenta").value("478758"))
                .andExpect(jsonPath("$.cuentas[0].movimientos[0].tipoMovimiento").value("CREDITO"))
                .andExpect(jsonPath("$.totalCreditos").value(100.00))
                .andExpect(jsonPath("$.pdfBase64").value("JVBERi0x"));
    }

    @Test
    void generarEstadoCuenta_cuandoRangoEsInvalido_retorna400() throws Exception {
        given(reporteService.generarEstadoCuenta(
                1L,
                LocalDate.of(2026, 4, 30),
                LocalDate.of(2026, 4, 1)
        )).willThrow(new ReporteInvalidoException("La fecha desde no puede ser mayor que la fecha hasta"));

        mockMvc.perform(get("/reportes")
                        .param("clienteId", "1")
                        .param("fechaDesde", "2026-04-30")
                        .param("fechaHasta", "2026-04-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("La fecha desde no puede ser mayor que la fecha hasta"))
                .andExpect(jsonPath("$.path").value("/reportes"));
    }

    @Test
    void generarEstadoCuenta_cuandoClienteNoExiste_retorna404() throws Exception {
        given(reporteService.generarEstadoCuenta(
                99L,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        )).willThrow(new ResourceNotFoundException("Cliente", "id", 99L));

        mockMvc.perform(get("/reportes")
                        .param("clienteId", "99")
                        .param("fechaDesde", "2026-04-01")
                        .param("fechaHasta", "2026-04-30"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Cliente no encontrado con id 99"))
                .andExpect(jsonPath("$.path").value("/reportes"));
    }

    private ReporteEstadoCuentaResponseDto crearReporte() {
        return new ReporteEstadoCuentaResponseDto(
                new ReporteClienteDto(1L, "Jose Lema", "1234567890"),
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                List.of(new ReporteCuentaDto(
                        1L,
                        "478758",
                        com.lmora.cuentas.cuentas.model.TipoCuenta.AHORROS,
                        new BigDecimal("2000.00"),
                        new BigDecimal("2050.00"),
                        new BigDecimal("100.00"),
                        new BigDecimal("50.00"),
                        List.of(new ReporteMovimientoDto(
                                1L,
                                LocalDateTime.of(2026, 4, 12, 10, 0),
                                com.lmora.cuentas.movimientos.model.TipoMovimiento.CREDITO,
                                new BigDecimal("100.00"),
                                new BigDecimal("2100.00")
                        ))
                )),
                "JVBERi0x"
        );
    }
}
