package com.lmora.cuentas.cuentas.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lmora.cuentas.clientes.model.Cliente;
import com.lmora.cuentas.cuentas.dto.CuentaPatchRequestDto;
import com.lmora.cuentas.cuentas.dto.CuentaRequestDto;
import com.lmora.cuentas.cuentas.dto.CuentaResponseDto;
import com.lmora.cuentas.cuentas.exception.CuentaNumeroDuplicadoException;
import com.lmora.cuentas.cuentas.mapper.CuentaMapper;
import com.lmora.cuentas.cuentas.model.Cuenta;
import com.lmora.cuentas.cuentas.model.TipoCuenta;
import com.lmora.cuentas.cuentas.service.CuentaService;
import com.lmora.cuentas.shared.exception.GlobalExceptionHandler;
import com.lmora.cuentas.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(CuentaController.class)
@Import(GlobalExceptionHandler.class)
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CuentaService cuentaService;

    @MockitoBean
    private CuentaMapper cuentaMapper;

    @Test
    void listarCuentas_cuandoExistenCuentas_retorna200() throws Exception {
        Cuenta cuenta = crearCuenta(1L, 1L);
        CuentaResponseDto response = crearCuentaResponseDto(1L, 1L);

        given(cuentaService.listarCuentas()).willReturn(List.of(cuenta));
        given(cuentaMapper.toResponse(cuenta)).willReturn(response);

        mockMvc.perform(get("/cuentas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cuentaId").value(1))
                .andExpect(jsonPath("$[0].numeroCuenta").value("478758"));
    }

    @Test
    void obtenerCuentaPorId_cuandoExiste_retorna200() throws Exception {
        Cuenta cuenta = crearCuenta(1L, 1L);
        CuentaResponseDto response = crearCuentaResponseDto(1L, 1L);

        given(cuentaService.obtenerCuentaPorId(1L)).willReturn(cuenta);
        given(cuentaMapper.toResponse(cuenta)).willReturn(response);

        mockMvc.perform(get("/cuentas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuentaId").value(1))
                .andExpect(jsonPath("$.clienteId").value(1))
                .andExpect(jsonPath("$.tipoCuenta").value("AHORROS"));
    }

    @Test
    void obtenerCuentaPorId_cuandoNoExiste_retorna404() throws Exception {
        given(cuentaService.obtenerCuentaPorId(99L))
                .willThrow(new ResourceNotFoundException("Cuenta", "id", 99L));

        mockMvc.perform(get("/cuentas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Cuenta no encontrado con id 99"))
                .andExpect(jsonPath("$.path").value("/cuentas/99"));
    }

    @Test
    void crearCuenta_cuandoRequestEsValido_retorna201() throws Exception {
        CuentaRequestDto requestDto = crearCuentaRequestDto();
        Cuenta cuenta = crearCuenta(1L, 1L);
        CuentaResponseDto response = crearCuentaResponseDto(1L, 1L);

        given(cuentaMapper.toEntity(any(CuentaRequestDto.class))).willReturn(cuenta);
        given(cuentaService.crearCuenta(1L, cuenta)).willReturn(cuenta);
        given(cuentaMapper.toResponse(cuenta)).willReturn(response);

        mockMvc.perform(post("/cuentas")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/cuentas/1")))
                .andExpect(jsonPath("$.cuentaId").value(1))
                .andExpect(jsonPath("$.clienteId").value(1))
                .andExpect(jsonPath("$.numeroCuenta").value("478758"));
    }

    @Test
    void crearCuenta_cuandoRequestEsInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/cuentas")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("La solicitud contiene datos invalidos"))
                .andExpect(jsonPath("$.validationErrors.clienteId").exists())
                .andExpect(jsonPath("$.validationErrors.numeroCuenta").exists())
                .andExpect(jsonPath("$.validationErrors.saldoInicial").exists());
    }

    @Test
    void crearCuenta_cuandoNumeroCuentaEstaDuplicado_retorna409() throws Exception {
        CuentaRequestDto requestDto = crearCuentaRequestDto();
        Cuenta cuenta = crearCuenta(null, null);

        given(cuentaMapper.toEntity(any(CuentaRequestDto.class))).willReturn(cuenta);
        given(cuentaService.crearCuenta(1L, cuenta))
                .willThrow(new CuentaNumeroDuplicadoException("478758"));

        mockMvc.perform(post("/cuentas")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Ya existe una cuenta con el numero 478758"))
                .andExpect(jsonPath("$.path").value("/cuentas"));
    }

    @Test
    void actualizarParcialCuenta_cuandoRequestEsValido_retorna200() throws Exception {
        CuentaPatchRequestDto requestDto = new CuentaPatchRequestDto(
                null,
                "999001",
                null,
                new BigDecimal("3000.00"),
                false
        );

        Cuenta cuentaPatch = new Cuenta();
        cuentaPatch.setNumeroCuenta("999001");
        cuentaPatch.setSaldoInicial(new BigDecimal("3000.00"));
        cuentaPatch.setEstado(false);

        Cuenta cuentaActualizada = crearCuenta(1L, 1L);
        cuentaActualizada.setNumeroCuenta("999001");
        cuentaActualizada.setSaldoInicial(new BigDecimal("3000.00"));
        cuentaActualizada.setEstado(false);

        CuentaResponseDto response = new CuentaResponseDto(
                1L,
                1L,
                "999001",
                TipoCuenta.AHORROS,
                new BigDecimal("3000.00"),
                false
        );

        given(cuentaMapper.toEntity(any(CuentaPatchRequestDto.class))).willReturn(cuentaPatch);
        given(cuentaService.actualizarParcialCuenta(1L, null, cuentaPatch)).willReturn(cuentaActualizada);
        given(cuentaMapper.toResponse(cuentaActualizada)).willReturn(response);

        mockMvc.perform(patch("/cuentas/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuentaId").value(1))
                .andExpect(jsonPath("$.numeroCuenta").value("999001"))
                .andExpect(jsonPath("$.estado").value(false));
    }

    @Test
    void eliminarCuenta_cuandoExiste_retorna204() throws Exception {
        doNothing().when(cuentaService).eliminarCuenta(1L);

        mockMvc.perform(delete("/cuentas/1"))
                .andExpect(status().isNoContent());
    }

    private CuentaRequestDto crearCuentaRequestDto() {
        return new CuentaRequestDto(
                1L,
                "478758",
                TipoCuenta.AHORROS,
                new BigDecimal("2000.00"),
                true
        );
    }

    private CuentaResponseDto crearCuentaResponseDto(Long cuentaId, Long clienteId) {
        return new CuentaResponseDto(
                cuentaId,
                clienteId,
                "478758",
                TipoCuenta.AHORROS,
                new BigDecimal("2000.00"),
                true
        );
    }

    private Cuenta crearCuenta(Long cuentaId, Long clienteId) {
        Cuenta cuenta = new Cuenta();
        cuenta.setCuentaId(cuentaId);
        cuenta.setNumeroCuenta("478758");
        cuenta.setTipoCuenta(TipoCuenta.AHORROS);
        cuenta.setSaldoInicial(new BigDecimal("2000.00"));
        cuenta.setEstado(true);

        if (clienteId != null) {
            Cliente cliente = new Cliente();
            cliente.setPersonaId(clienteId);
            cuenta.setCliente(cliente);
        }

        return cuenta;
    }
}
