package com.lmora.cuentas.reportes.service;

import com.lmora.cuentas.clientes.model.Cliente;
import com.lmora.cuentas.clientes.repository.ClienteRepository;
import com.lmora.cuentas.cuentas.model.Cuenta;
import com.lmora.cuentas.cuentas.repository.CuentaRepository;
import com.lmora.cuentas.movimientos.model.Movimiento;
import com.lmora.cuentas.movimientos.model.TipoMovimiento;
import com.lmora.cuentas.movimientos.repository.MovimientoRepository;
import com.lmora.cuentas.reportes.dto.ReporteClienteDto;
import com.lmora.cuentas.reportes.dto.ReporteCuentaDto;
import com.lmora.cuentas.reportes.dto.ReporteEstadoCuentaResponseDto;
import com.lmora.cuentas.reportes.dto.ReporteMovimientoDto;
import com.lmora.cuentas.reportes.exception.ReporteInvalidoException;
import com.lmora.cuentas.reportes.pdf.EstadoCuentaPdfGenerator;
import com.lmora.cuentas.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private static final BigDecimal CERO = BigDecimal.ZERO;

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final EstadoCuentaPdfGenerator estadoCuentaPdfGenerator;

    @Override
    @Transactional(readOnly = true)
    public ReporteEstadoCuentaResponseDto generarEstadoCuenta(Long clienteId, LocalDate fechaDesde, LocalDate fechaHasta) {
        validarParametros(clienteId, fechaDesde, fechaHasta);

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));

        List<Cuenta> cuentas = cuentaRepository.findCuentasDeCliente(clienteId);
        LocalDateTime inicio = fechaDesde.atStartOfDay();
        LocalDateTime fin = fechaHasta.atTime(LocalTime.MAX);

        Map<Long, List<Movimiento>> movimientosPorCuenta = movimientoRepository
                .findMovimientosParaReporte(
                        clienteId,
                        fin
                )
                .stream()
                .collect(Collectors.groupingBy(
                        movimiento -> movimiento.getCuenta().getCuentaId(),
                        Collectors.toList()
                ));

        List<ReporteCuentaDto> cuentasReporte = cuentas.stream()
                .map(cuenta -> construirCuentaReporte(cuenta, movimientosPorCuenta.get(cuenta.getCuentaId()), inicio, fin))
                .toList();

        BigDecimal totalCreditos = cuentasReporte.stream()
                .map(ReporteCuentaDto::totalCreditos)
                .reduce(CERO, BigDecimal::add);

        BigDecimal totalDebitos = cuentasReporte.stream()
                .map(ReporteCuentaDto::totalDebitos)
                .reduce(CERO, BigDecimal::add);

        ReporteEstadoCuentaResponseDto reporteBase = new ReporteEstadoCuentaResponseDto(
                new ReporteClienteDto(cliente.getPersonaId(), cliente.getNombre(), cliente.getIdentificacion()),
                fechaDesde,
                fechaHasta,
                totalCreditos,
                totalDebitos,
                cuentasReporte,
                null
        );

        String pdfBase64 = Base64.getEncoder().encodeToString(estadoCuentaPdfGenerator.generate(reporteBase));

        return new ReporteEstadoCuentaResponseDto(
                reporteBase.cliente(),
                reporteBase.fechaDesde(),
                reporteBase.fechaHasta(),
                reporteBase.totalCreditos(),
                reporteBase.totalDebitos(),
                reporteBase.cuentas(),
                pdfBase64
        );
    }

    private ReporteCuentaDto construirCuentaReporte(
            Cuenta cuenta,
            List<Movimiento> movimientosHastaFecha,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta
    ) {
        List<Movimiento> movimientosCuenta = movimientosHastaFecha == null ? List.of() : movimientosHastaFecha;

        List<ReporteMovimientoDto> movimientosEnRango = movimientosCuenta.stream()
                .filter(movimiento -> !movimiento.getFecha().isBefore(fechaDesde))
                .filter(movimiento -> !movimiento.getFecha().isAfter(fechaHasta))
                .map(this::toReporteMovimiento)
                .toList();

        BigDecimal totalCreditos = movimientosEnRango.stream()
                .filter(movimiento -> movimiento.tipoMovimiento() == TipoMovimiento.CREDITO)
                .map(ReporteMovimientoDto::valor)
                .reduce(CERO, BigDecimal::add);

        BigDecimal totalDebitos = movimientosEnRango.stream()
                .filter(movimiento -> movimiento.tipoMovimiento() == TipoMovimiento.DEBITO)
                .map(ReporteMovimientoDto::valor)
                .map(BigDecimal::abs)
                .reduce(CERO, BigDecimal::add);

        BigDecimal saldoDisponible = movimientosCuenta.isEmpty()
                ? cuenta.getSaldoInicial()
                : movimientosCuenta.get(movimientosCuenta.size() - 1).getSaldo();

        return new ReporteCuentaDto(
                cuenta.getCuentaId(),
                cuenta.getNumeroCuenta(),
                cuenta.getTipoCuenta(),
                cuenta.getSaldoInicial(),
                saldoDisponible,
                totalCreditos,
                totalDebitos,
                movimientosEnRango
        );
    }

    private ReporteMovimientoDto toReporteMovimiento(Movimiento movimiento) {
        return new ReporteMovimientoDto(
                movimiento.getMovimientoId(),
                movimiento.getFecha(),
                movimiento.getTipoMovimiento(),
                movimiento.getValor(),
                movimiento.getSaldo()
        );
    }

    private void validarParametros(Long clienteId, LocalDate fechaDesde, LocalDate fechaHasta) {
        if (clienteId == null || clienteId <= 0) {
            throw new ReporteInvalidoException("El cliente del reporte es obligatorio");
        }

        if (fechaDesde == null || fechaHasta == null) {
            throw new ReporteInvalidoException("El rango de fechas del reporte es obligatorio");
        }

        if (fechaDesde.isAfter(fechaHasta)) {
            throw new ReporteInvalidoException("La fecha desde no puede ser mayor que la fecha hasta");
        }
    }
}
