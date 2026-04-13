package com.lmora.cuentas.movimientos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lmora.cuentas.clientes.model.Cliente;
import com.lmora.cuentas.cuentas.model.Cuenta;
import com.lmora.cuentas.cuentas.model.TipoCuenta;
import com.lmora.cuentas.cuentas.repository.CuentaRepository;
import com.lmora.cuentas.movimientos.exception.CupoDiarioExcedidoException;
import com.lmora.cuentas.movimientos.exception.MovimientoInvalidoException;
import com.lmora.cuentas.movimientos.exception.SaldoNoDisponibleException;
import com.lmora.cuentas.movimientos.model.Movimiento;
import com.lmora.cuentas.movimientos.model.TipoMovimiento;
import com.lmora.cuentas.movimientos.repository.MovimientoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceImplTest {

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @InjectMocks
    private MovimientoServiceImpl movimientoService;

    @Test
    void crearMovimiento_cuandoEsCredito_calculaSaldoCorrectamente() {
        Cuenta cuenta = crearCuenta();
        Movimiento movimiento = crearMovimiento(cuenta, null, TipoMovimiento.CREDITO, new BigDecimal("100.00"));

        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.findByCuentaCuentaIdOrderByFechaAscMovimientoIdAsc(1L)).thenReturn(List.of());
        when(movimientoRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Movimiento movimientoGuardado = movimientoService.crearMovimiento(1L, movimiento);

        assertEquals(new BigDecimal("2100.00"), movimientoGuardado.getSaldo());
        verify(movimientoRepository).saveAll(any());
    }

    @Test
    void crearMovimiento_cuandoEsDebito_calculaSaldoCorrectamente() {
        Cuenta cuenta = crearCuenta();
        Movimiento movimiento = crearMovimiento(cuenta, null, TipoMovimiento.DEBITO, new BigDecimal("-100.00"));

        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.findByCuentaCuentaIdOrderByFechaAscMovimientoIdAsc(1L)).thenReturn(List.of());
        when(movimientoRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Movimiento movimientoGuardado = movimientoService.crearMovimiento(1L, movimiento);

        assertEquals(new BigDecimal("1900.00"), movimientoGuardado.getSaldo());
    }

    @Test
    void crearMovimiento_cuandoSignoNoCoincideConTipo_lanzaExcepcion() {
        Cuenta cuenta = crearCuenta();
        Movimiento movimiento = crearMovimiento(cuenta, null, TipoMovimiento.CREDITO, new BigDecimal("-100.00"));

        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.findByCuentaCuentaIdOrderByFechaAscMovimientoIdAsc(1L)).thenReturn(List.of());

        assertThrows(MovimientoInvalidoException.class, () -> movimientoService.crearMovimiento(1L, movimiento));

        verify(movimientoRepository, never()).saveAll(any());
    }

    @Test
    void crearMovimiento_cuandoSaldoNoEsSuficiente_lanzaExcepcion() {
        Cuenta cuenta = crearCuenta(new BigDecimal("500.00"));
        Movimiento movimiento = crearMovimiento(cuenta, null, TipoMovimiento.DEBITO, new BigDecimal("-600.00"));

        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.findByCuentaCuentaIdOrderByFechaAscMovimientoIdAsc(1L)).thenReturn(List.of());

        assertThrows(SaldoNoDisponibleException.class, () -> movimientoService.crearMovimiento(1L, movimiento));

        verify(movimientoRepository, never()).saveAll(any());
    }

    @Test
    void crearMovimiento_cuandoCupoDiarioEsExcedido_lanzaExcepcion() {
        Cuenta cuenta = crearCuenta();
        Movimiento existente = crearMovimiento(cuenta, 10L, TipoMovimiento.DEBITO, new BigDecimal("-600.00"));
        Movimiento nuevo = crearMovimiento(cuenta, null, TipoMovimiento.DEBITO, new BigDecimal("-500.00"));

        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.findByCuentaCuentaIdOrderByFechaAscMovimientoIdAsc(1L)).thenReturn(List.of(existente));

        assertThrows(CupoDiarioExcedidoException.class, () -> movimientoService.crearMovimiento(1L, nuevo));

        verify(movimientoRepository, never()).saveAll(any());
    }

    @Test
    void eliminarMovimiento_cuandoExiste_recalculaSaldosPosteriores() {
        Cuenta cuenta = crearCuenta();
        Movimiento primero = crearMovimiento(cuenta, 1L, TipoMovimiento.CREDITO, new BigDecimal("100.00"));
        primero.setSaldo(new BigDecimal("2100.00"));
        Movimiento segundo = crearMovimiento(cuenta, 2L, TipoMovimiento.DEBITO, new BigDecimal("-50.00"));
        segundo.setSaldo(new BigDecimal("2050.00"));

        when(movimientoRepository.findById(1L)).thenReturn(Optional.of(primero));
        when(movimientoRepository.findByCuentaCuentaIdOrderByFechaAscMovimientoIdAsc(1L)).thenReturn(List.of(primero, segundo));
        when(movimientoRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        movimientoService.eliminarMovimiento(1L);

        assertEquals(new BigDecimal("1950.00"), segundo.getSaldo());
        verify(movimientoRepository).delete(primero);
        verify(movimientoRepository).saveAll(any());
    }

    private Cuenta crearCuenta() {
        return crearCuenta(new BigDecimal("2000.00"));
    }

    private Cuenta crearCuenta(BigDecimal saldoInicial) {
        Cliente cliente = new Cliente();
        cliente.setPersonaId(1L);

        Cuenta cuenta = new Cuenta();
        cuenta.setCuentaId(1L);
        cuenta.setCliente(cliente);
        cuenta.setNumeroCuenta("478758");
        cuenta.setTipoCuenta(TipoCuenta.AHORROS);
        cuenta.setSaldoInicial(saldoInicial);
        cuenta.setEstado(true);
        return cuenta;
    }

    private Movimiento crearMovimiento(Cuenta cuenta, Long movimientoId, TipoMovimiento tipoMovimiento, BigDecimal valor) {
        Movimiento movimiento = new Movimiento();
        movimiento.setMovimientoId(movimientoId);
        movimiento.setFecha(LocalDateTime.of(2026, 4, 12, 10, 0));
        movimiento.setTipoMovimiento(tipoMovimiento);
        movimiento.setValor(valor);
        movimiento.setCuenta(cuenta);
        return movimiento;
    }
}
