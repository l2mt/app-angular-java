package com.lmora.cuentas.movimientos.controller;

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
import com.lmora.cuentas.cuentas.model.Cuenta;
import com.lmora.cuentas.cuentas.model.TipoCuenta;
import com.lmora.cuentas.movimientos.dto.MovimientoPatchRequestDto;
import com.lmora.cuentas.movimientos.dto.MovimientoRequestDto;
import com.lmora.cuentas.movimientos.dto.MovimientoResponseDto;
import com.lmora.cuentas.movimientos.exception.CupoDiarioExcedidoException;
import com.lmora.cuentas.movimientos.exception.SaldoNoDisponibleException;
import com.lmora.cuentas.movimientos.mapper.MovimientoMapper;
import com.lmora.cuentas.movimientos.model.Movimiento;
import com.lmora.cuentas.movimientos.model.TipoMovimiento;
import com.lmora.cuentas.movimientos.service.MovimientoService;
import com.lmora.cuentas.shared.exception.GlobalExceptionHandler;
import com.lmora.cuentas.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(MovimientoController.class)
@Import(GlobalExceptionHandler.class)
class MovimientoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MovimientoService movimientoService;

    @MockitoBean
    private MovimientoMapper movimientoMapper;

    @Test
    void crearMovimiento_cuandoRequestEsValido_retorna201() throws Exception {
        MovimientoRequestDto requestDto = crearMovimientoRequestDto(TipoMovimiento.CREDITO, new BigDecimal("100.00"));
        Movimiento movimiento = crearMovimiento(1L, TipoMovimiento.CREDITO, new BigDecimal("100.00"), new BigDecimal("2100.00"));
        MovimientoResponseDto response = crearMovimientoResponseDto(1L, TipoMovimiento.CREDITO, new BigDecimal("100.00"), new BigDecimal("2100.00"));

        given(movimientoMapper.toEntity(any(MovimientoRequestDto.class))).willReturn(movimiento);
        given(movimientoService.crearMovimiento(1L, movimiento)).willReturn(movimiento);
        given(movimientoMapper.toResponse(movimiento)).willReturn(response);

        mockMvc.perform(post("/movimientos")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/movimientos/1")))
                .andExpect(jsonPath("$.movimientoId").value(1))
                .andExpect(jsonPath("$.cuentaId").value(1))
                .andExpect(jsonPath("$.saldo").value(2100.00));
    }

    @Test
    void obtenerMovimientoPorId_cuandoNoExiste_retorna404() throws Exception {
        given(movimientoService.obtenerMovimientoPorId(99L))
                .willThrow(new ResourceNotFoundException("Movimiento", "id", 99L));

        mockMvc.perform(get("/movimientos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Movimiento no encontrado con id 99"))
                .andExpect(jsonPath("$.path").value("/movimientos/99"));
    }

    @Test
    void crearMovimiento_cuandoRequestEsInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/movimientos")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.cuentaId").exists())
                .andExpect(jsonPath("$.validationErrors.tipoMovimiento").exists())
                .andExpect(jsonPath("$.validationErrors.valor").exists());
    }

    @Test
    void crearMovimiento_cuandoSaldoNoDisponible_retorna409() throws Exception {
        MovimientoRequestDto requestDto = crearMovimientoRequestDto(TipoMovimiento.DEBITO, new BigDecimal("-2500.00"));
        Movimiento movimiento = crearMovimiento(null, TipoMovimiento.DEBITO, new BigDecimal("-2500.00"), null);

        given(movimientoMapper.toEntity(any(MovimientoRequestDto.class))).willReturn(movimiento);
        given(movimientoService.crearMovimiento(1L, movimiento)).willThrow(new SaldoNoDisponibleException());

        mockMvc.perform(post("/movimientos")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Saldo no disponible"))
                .andExpect(jsonPath("$.path").value("/movimientos"));
    }

    @Test
    void crearMovimiento_cuandoCupoDiarioExcedido_retorna409() throws Exception {
        MovimientoRequestDto requestDto = crearMovimientoRequestDto(TipoMovimiento.DEBITO, new BigDecimal("-500.00"));
        Movimiento movimiento = crearMovimiento(null, TipoMovimiento.DEBITO, new BigDecimal("-500.00"), null);

        given(movimientoMapper.toEntity(any(MovimientoRequestDto.class))).willReturn(movimiento);
        given(movimientoService.crearMovimiento(1L, movimiento)).willThrow(new CupoDiarioExcedidoException());

        mockMvc.perform(post("/movimientos")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Cupo diario excedido"))
                .andExpect(jsonPath("$.path").value("/movimientos"));
    }

    @Test
    void actualizarParcialMovimiento_cuandoRequestEsValido_retorna200() throws Exception {
        MovimientoPatchRequestDto requestDto = new MovimientoPatchRequestDto(
                null,
                null,
                TipoMovimiento.DEBITO,
                new BigDecimal("-50.00")
        );
        Movimiento movimientoPatch = new Movimiento();
        movimientoPatch.setTipoMovimiento(TipoMovimiento.DEBITO);
        movimientoPatch.setValor(new BigDecimal("-50.00"));

        Movimiento movimientoActualizado = crearMovimiento(1L, TipoMovimiento.DEBITO, new BigDecimal("-50.00"), new BigDecimal("1950.00"));
        MovimientoResponseDto response = crearMovimientoResponseDto(1L, TipoMovimiento.DEBITO, new BigDecimal("-50.00"), new BigDecimal("1950.00"));

        given(movimientoMapper.toEntity(any(MovimientoPatchRequestDto.class))).willReturn(movimientoPatch);
        given(movimientoService.actualizarParcialMovimiento(1L, null, movimientoPatch)).willReturn(movimientoActualizado);
        given(movimientoMapper.toResponse(movimientoActualizado)).willReturn(response);

        mockMvc.perform(patch("/movimientos/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movimientoId").value(1))
                .andExpect(jsonPath("$.tipoMovimiento").value("DEBITO"))
                .andExpect(jsonPath("$.saldo").value(1950.00));
    }

    @Test
    void eliminarMovimiento_cuandoExiste_retorna204() throws Exception {
        doNothing().when(movimientoService).eliminarMovimiento(1L);

        mockMvc.perform(delete("/movimientos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void listarMovimientos_cuandoExistenMovimientos_retorna200() throws Exception {
        Movimiento movimiento = crearMovimiento(1L, TipoMovimiento.CREDITO, new BigDecimal("100.00"), new BigDecimal("2100.00"));
        MovimientoResponseDto response = crearMovimientoResponseDto(1L, TipoMovimiento.CREDITO, new BigDecimal("100.00"), new BigDecimal("2100.00"));

        given(movimientoService.listarMovimientos()).willReturn(List.of(movimiento));
        given(movimientoMapper.toResponse(movimiento)).willReturn(response);

        mockMvc.perform(get("/movimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].movimientoId").value(1))
                .andExpect(jsonPath("$[0].saldo").value(2100.00));
    }

    private MovimientoRequestDto crearMovimientoRequestDto(TipoMovimiento tipoMovimiento, BigDecimal valor) {
        return new MovimientoRequestDto(
                1L,
                LocalDateTime.of(2026, 4, 12, 10, 0),
                tipoMovimiento,
                valor
        );
    }

    private MovimientoResponseDto crearMovimientoResponseDto(
            Long movimientoId,
            TipoMovimiento tipoMovimiento,
            BigDecimal valor,
            BigDecimal saldo
    ) {
        return new MovimientoResponseDto(
                movimientoId,
                1L,
                LocalDateTime.of(2026, 4, 12, 10, 0),
                tipoMovimiento,
                valor,
                saldo
        );
    }

    private Movimiento crearMovimiento(
            Long movimientoId,
            TipoMovimiento tipoMovimiento,
            BigDecimal valor,
            BigDecimal saldo
    ) {
        Cliente cliente = new Cliente();
        cliente.setPersonaId(1L);

        Cuenta cuenta = new Cuenta();
        cuenta.setCuentaId(1L);
        cuenta.setCliente(cliente);
        cuenta.setTipoCuenta(TipoCuenta.AHORROS);
        cuenta.setSaldoInicial(new BigDecimal("2000.00"));

        Movimiento movimiento = new Movimiento();
        movimiento.setMovimientoId(movimientoId);
        movimiento.setCuenta(cuenta);
        movimiento.setFecha(LocalDateTime.of(2026, 4, 12, 10, 0));
        movimiento.setTipoMovimiento(tipoMovimiento);
        movimiento.setValor(valor);
        movimiento.setSaldo(saldo);
        return movimiento;
    }
}
