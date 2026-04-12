package com.lmora.cuentas.cuentas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lmora.cuentas.clientes.model.Cliente;
import com.lmora.cuentas.clientes.repository.ClienteRepository;
import com.lmora.cuentas.cuentas.exception.CuentaNumeroDuplicadoException;
import com.lmora.cuentas.cuentas.model.Cuenta;
import com.lmora.cuentas.cuentas.model.TipoCuenta;
import com.lmora.cuentas.cuentas.repository.CuentaRepository;
import com.lmora.cuentas.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CuentaServiceImplTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private CuentaServiceImpl cuentaService;

    @Test
    void crearCuenta_cuandoNumeroCuentaExiste_lanzaExcepcion() {
        Cuenta cuenta = crearCuenta();

        when(cuentaRepository.existsByNumeroCuenta("478758")).thenReturn(true);

        assertThrows(CuentaNumeroDuplicadoException.class, () -> cuentaService.crearCuenta(1L, cuenta));

        verify(clienteRepository, never()).findById(any());
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    void crearCuenta_cuandoClienteNoExiste_lanzaExcepcion() {
        Cuenta cuenta = crearCuenta();

        when(cuentaRepository.existsByNumeroCuenta("478758")).thenReturn(false);
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cuentaService.crearCuenta(99L, cuenta)
        );

        assertEquals("Cliente no encontrado con id 99", exception.getMessage());
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    void crearCuenta_cuandoDatosSonValidos_guardaCuentaConCliente() {
        Cuenta cuenta = crearCuenta();
        Cliente cliente = crearCliente(1L);

        when(cuentaRepository.existsByNumeroCuenta("478758")).thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cuenta cuentaGuardada = cuentaService.crearCuenta(1L, cuenta);

        assertEquals(cliente, cuentaGuardada.getCliente());
        assertEquals("478758", cuentaGuardada.getNumeroCuenta());
        verify(cuentaRepository).save(cuenta);
    }

    @Test
    void obtenerCuentaPorId_cuandoNoExiste_lanzaExcepcion() {
        when(cuentaRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cuentaService.obtenerCuentaPorId(99L)
        );

        assertEquals("Cuenta no encontrado con id 99", exception.getMessage());
    }

    @Test
    void actualizarParcialCuenta_cuandoNoLlegaNumeroNiCliente_actualizaSoloCamposParciales() {
        Cliente cliente = crearCliente(1L);
        Cuenta cuentaExistente = crearCuenta();
        cuentaExistente.setCuentaId(1L);
        cuentaExistente.setCliente(cliente);

        Cuenta patch = new Cuenta();
        patch.setTipoCuenta(TipoCuenta.CORRIENTE);
        patch.setSaldoInicial(new BigDecimal("3000.00"));
        patch.setEstado(false);

        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaExistente));
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cuenta cuentaActualizada = cuentaService.actualizarParcialCuenta(1L, null, patch);

        assertEquals("478758", cuentaActualizada.getNumeroCuenta());
        assertEquals(cliente, cuentaActualizada.getCliente());
        assertEquals(TipoCuenta.CORRIENTE, cuentaActualizada.getTipoCuenta());
        assertEquals(new BigDecimal("3000.00"), cuentaActualizada.getSaldoInicial());
        assertEquals(false, cuentaActualizada.getEstado());
        verify(cuentaRepository, never()).existsByNumeroCuentaAndCuentaIdNot(any(), any());
        verify(clienteRepository, never()).findById(any());
    }

    private Cuenta crearCuenta() {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta("478758");
        cuenta.setTipoCuenta(TipoCuenta.AHORROS);
        cuenta.setSaldoInicial(new BigDecimal("2000.00"));
        cuenta.setEstado(true);
        return cuenta;
    }

    private Cliente crearCliente(Long clienteId) {
        Cliente cliente = new Cliente();
        cliente.setPersonaId(clienteId);
        cliente.setNombre("Jose Lema");
        return cliente;
    }
}
