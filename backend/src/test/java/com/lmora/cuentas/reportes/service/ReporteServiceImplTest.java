package com.lmora.cuentas.reportes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.lmora.cuentas.clientes.model.Cliente;
import com.lmora.cuentas.clientes.model.Genero;
import com.lmora.cuentas.clientes.repository.ClienteRepository;
import com.lmora.cuentas.cuentas.model.Cuenta;
import com.lmora.cuentas.cuentas.model.TipoCuenta;
import com.lmora.cuentas.cuentas.repository.CuentaRepository;
import com.lmora.cuentas.movimientos.model.Movimiento;
import com.lmora.cuentas.movimientos.model.TipoMovimiento;
import com.lmora.cuentas.movimientos.repository.MovimientoRepository;
import com.lmora.cuentas.reportes.dto.ReporteEstadoCuentaResponseDto;
import com.lmora.cuentas.reportes.exception.ReporteInvalidoException;
import com.lmora.cuentas.reportes.pdf.EstadoCuentaPdfGenerator;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReporteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    private ReporteServiceImpl reporteService;

    @BeforeEach
    void setUp() {
        reporteService = new ReporteServiceImpl(
                clienteRepository,
                cuentaRepository,
                movimientoRepository,
                new EstadoCuentaPdfGenerator()
        );
    }

    @Test
    void generarEstadoCuenta_cuandoDatosSonValidos_retornaJsonConPdfBase64() {
        LocalDate fechaDesde = LocalDate.of(2026, 4, 5);
        LocalDate fechaHasta = LocalDate.of(2026, 4, 30);

        Cliente cliente = crearCliente();
        Cuenta cuentaAhorros = crearCuenta(1L, "478758", new BigDecimal("1000.00"));
        Cuenta cuentaCorriente = crearCuenta(2L, "225487", new BigDecimal("500.00"));

        Movimiento creditoFueraDeRango = crearMovimiento(
                10L,
                cuentaAhorros,
                LocalDate.of(2026, 4, 2),
                TipoMovimiento.CREDITO,
                new BigDecimal("100.00"),
                new BigDecimal("1100.00")
        );
        Movimiento debitoEnRango = crearMovimiento(
                11L,
                cuentaAhorros,
                LocalDate.of(2026, 4, 10),
                TipoMovimiento.DEBITO,
                new BigDecimal("-50.00"),
                new BigDecimal("1050.00")
        );
        Movimiento creditoPrevio = crearMovimiento(
                12L,
                cuentaCorriente,
                LocalDate.of(2026, 3, 31),
                TipoMovimiento.CREDITO,
                new BigDecimal("100.00"),
                new BigDecimal("600.00")
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.findCuentasDeCliente(1L))
                .thenReturn(List.of(cuentaCorriente, cuentaAhorros));
        when(movimientoRepository.findMovimientosParaReporte(
                1L,
                fechaHasta.atTime(LocalTime.MAX)
        )).thenReturn(List.of(creditoFueraDeRango, debitoEnRango, creditoPrevio));

        ReporteEstadoCuentaResponseDto reporte = reporteService.generarEstadoCuenta(1L, fechaDesde, fechaHasta);

        assertEquals(1L, reporte.cliente().clienteId());
        assertEquals(new BigDecimal("0"), reporte.totalCreditos());
        assertEquals(new BigDecimal("50.00"), reporte.totalDebitos());
        assertEquals(2, reporte.cuentas().size());

        assertEquals("225487", reporte.cuentas().get(0).numeroCuenta());
        assertEquals(new BigDecimal("600.00"), reporte.cuentas().get(0).saldoDisponible());
        assertEquals(new BigDecimal("0"), reporte.cuentas().get(0).totalCreditos());
        assertEquals(new BigDecimal("0"), reporte.cuentas().get(0).totalDebitos());
        assertEquals(0, reporte.cuentas().get(0).movimientos().size());

        assertEquals("478758", reporte.cuentas().get(1).numeroCuenta());
        assertEquals(new BigDecimal("1050.00"), reporte.cuentas().get(1).saldoDisponible());
        assertEquals(new BigDecimal("0"), reporte.cuentas().get(1).totalCreditos());
        assertEquals(new BigDecimal("50.00"), reporte.cuentas().get(1).totalDebitos());
        assertEquals(1, reporte.cuentas().get(1).movimientos().size());

        byte[] pdf = Base64.getDecoder().decode(reporte.pdfBase64());
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    void generarEstadoCuenta_cuandoRangoEsInvalido_lanzaExcepcion() {
        ReporteInvalidoException exception = assertThrows(
                ReporteInvalidoException.class,
                () -> reporteService.generarEstadoCuenta(
                        1L,
                        LocalDate.of(2026, 4, 30),
                        LocalDate.of(2026, 4, 1)
                )
        );

        assertEquals("La fecha desde no puede ser mayor que la fecha hasta", exception.getMessage());
        verifyNoInteractions(clienteRepository, cuentaRepository, movimientoRepository);
    }

    private Cliente crearCliente() {
        Cliente cliente = new Cliente();
        cliente.setPersonaId(1L);
        cliente.setNombre("Jose Lema");
        cliente.setGenero(Genero.MASCULINO);
        cliente.setEdad(30);
        cliente.setIdentificacion("1234567890");
        cliente.setDireccion("Otavalo sn y principal");
        cliente.setTelefono("0987654321");
        cliente.setContrasena("1234");
        cliente.setEstado(true);
        return cliente;
    }

    private Cuenta crearCuenta(Long cuentaId, String numeroCuenta, BigDecimal saldoInicial) {
        Cuenta cuenta = new Cuenta();
        cuenta.setCuentaId(cuentaId);
        cuenta.setNumeroCuenta(numeroCuenta);
        cuenta.setTipoCuenta(TipoCuenta.AHORROS);
        cuenta.setSaldoInicial(saldoInicial);
        cuenta.setEstado(true);
        cuenta.setCliente(crearCliente());
        return cuenta;
    }

    private Movimiento crearMovimiento(
            Long movimientoId,
            Cuenta cuenta,
            LocalDate fecha,
            TipoMovimiento tipoMovimiento,
            BigDecimal valor,
            BigDecimal saldo
    ) {
        Movimiento movimiento = new Movimiento();
        movimiento.setMovimientoId(movimientoId);
        movimiento.setCuenta(cuenta);
        movimiento.setFecha(fecha.atTime(10, 0));
        movimiento.setTipoMovimiento(tipoMovimiento);
        movimiento.setValor(valor);
        movimiento.setSaldo(saldo);
        return movimiento;
    }
}
