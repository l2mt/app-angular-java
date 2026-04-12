package com.lmora.cuentas.movimientos.service;

import com.lmora.cuentas.cuentas.model.Cuenta;
import com.lmora.cuentas.cuentas.repository.CuentaRepository;
import com.lmora.cuentas.movimientos.exception.CupoDiarioExcedidoException;
import com.lmora.cuentas.movimientos.exception.MovimientoInvalidoException;
import com.lmora.cuentas.movimientos.exception.SaldoNoDisponibleException;
import com.lmora.cuentas.movimientos.model.Movimiento;
import com.lmora.cuentas.movimientos.model.TipoMovimiento;
import com.lmora.cuentas.movimientos.repository.MovimientoRepository;
import com.lmora.cuentas.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {

    private static final BigDecimal CUPO_DIARIO_RETIRO = new BigDecimal("1000.00");
    private static final BigDecimal CERO = BigDecimal.ZERO;

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Movimiento> listarMovimientos() {
        return movimientoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Movimiento obtenerMovimientoPorId(Long movimientoId) {
        return buscarMovimientoPorId(movimientoId);
    }

    @Override
    @Transactional
    public Movimiento crearMovimiento(Long cuentaId, Movimiento movimiento) {
        Cuenta cuenta = buscarCuentaPorId(cuentaId);
        movimiento.setCuenta(cuenta);
        if (movimiento.getFecha() == null) {
            movimiento.setFecha(LocalDateTime.now());
        }

        List<Movimiento> movimientos = new ArrayList<>(movimientoRepository
                .findMovimientosDeCuenta(cuentaId));
        movimientos.add(movimiento);

        recalcularYValidarMovimientos(cuenta, movimientos);
        movimientoRepository.saveAll(movimientos);
        return movimiento;
    }

    @Override
    @Transactional
    public Movimiento actualizarMovimiento(Long movimientoId, Long cuentaId, Movimiento movimiento) {
        Movimiento movimientoExistente = buscarMovimientoPorId(movimientoId);
        validarCuentaInmutable(cuentaId, movimientoExistente);

        movimientoExistente.setFecha(movimiento.getFecha() != null ? movimiento.getFecha() : movimientoExistente.getFecha());
        movimientoExistente.setTipoMovimiento(movimiento.getTipoMovimiento());
        movimientoExistente.setValor(movimiento.getValor());

        List<Movimiento> movimientos = new ArrayList<>(movimientoRepository
                .findMovimientosDeCuenta(movimientoExistente.getCuenta().getCuentaId()));

        recalcularYValidarMovimientos(movimientoExistente.getCuenta(), movimientos);
        movimientoRepository.saveAll(movimientos);
        return movimientoExistente;
    }

    @Override
    @Transactional
    public Movimiento actualizarParcialMovimiento(Long movimientoId, Long cuentaId, Movimiento movimiento) {
        Movimiento movimientoExistente = buscarMovimientoPorId(movimientoId);
        validarCuentaInmutable(cuentaId, movimientoExistente);

        if (movimiento.getFecha() != null) {
            movimientoExistente.setFecha(movimiento.getFecha());
        }

        if (movimiento.getTipoMovimiento() != null) {
            movimientoExistente.setTipoMovimiento(movimiento.getTipoMovimiento());
        }

        if (movimiento.getValor() != null) {
            movimientoExistente.setValor(movimiento.getValor());
        }

        List<Movimiento> movimientos = new ArrayList<>(movimientoRepository
                .findMovimientosDeCuenta(movimientoExistente.getCuenta().getCuentaId()));

        recalcularYValidarMovimientos(movimientoExistente.getCuenta(), movimientos);
        movimientoRepository.saveAll(movimientos);
        return movimientoExistente;
    }

    @Override
    @Transactional
    public void eliminarMovimiento(Long movimientoId) {
        Movimiento movimiento = buscarMovimientoPorId(movimientoId);
        Cuenta cuenta = movimiento.getCuenta();

        List<Movimiento> movimientos = new ArrayList<>(movimientoRepository
                .findMovimientosDeCuenta(cuenta.getCuentaId()));
        movimientos.removeIf(item -> item.getMovimientoId().equals(movimientoId));

        recalcularYValidarMovimientos(cuenta, movimientos);
        movimientoRepository.delete(movimiento);
        movimientoRepository.saveAll(movimientos);
    }

    private Movimiento buscarMovimientoPorId(Long movimientoId) {
        return movimientoRepository.findById(movimientoId)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento", "id", movimientoId));
    }

    private Cuenta buscarCuentaPorId(Long cuentaId) {
        return cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta", "id", cuentaId));
    }

    private void validarCuentaInmutable(Long cuentaId, Movimiento movimientoExistente) {
        if (cuentaId != null && !cuentaId.equals(movimientoExistente.getCuenta().getCuentaId())) {
            throw new MovimientoInvalidoException("No se permite cambiar la cuenta del movimiento");
        }
    }

    private void recalcularYValidarMovimientos(Cuenta cuenta, List<Movimiento> movimientos) {
        movimientos.sort(Comparator
                .comparing(Movimiento::getFecha)
                .thenComparing(movimiento -> movimiento.getMovimientoId() == null
                        ? Long.MAX_VALUE
                        : movimiento.getMovimientoId()));

        BigDecimal saldoAcumulado = cuenta.getSaldoInicial();
        Map<LocalDate, BigDecimal> debitosPorDia = new HashMap<>();

        for (Movimiento movimiento : movimientos) {
            validarMovimiento(movimiento);

            if (movimiento.getTipoMovimiento() == TipoMovimiento.DEBITO) {
                LocalDate fecha = movimiento.getFecha().toLocalDate();
                BigDecimal totalDebitos = debitosPorDia.getOrDefault(fecha, CERO).add(movimiento.getValor().abs());
                if (totalDebitos.compareTo(CUPO_DIARIO_RETIRO) > 0) {
                    throw new CupoDiarioExcedidoException();
                }
                debitosPorDia.put(fecha, totalDebitos);
            }

            saldoAcumulado = saldoAcumulado.add(movimiento.getValor());
            if (saldoAcumulado.compareTo(CERO) < 0) {
                throw new SaldoNoDisponibleException();
            }

            movimiento.setSaldo(saldoAcumulado);
        }
    }

    private void validarMovimiento(Movimiento movimiento) {
        if (movimiento.getFecha() == null) {
            throw new MovimientoInvalidoException("La fecha del movimiento es obligatoria");
        }

        if (movimiento.getTipoMovimiento() == null) {
            throw new MovimientoInvalidoException("El tipo de movimiento es obligatorio");
        }

        if (movimiento.getValor() == null || movimiento.getValor().compareTo(CERO) == 0) {
            throw new MovimientoInvalidoException("El valor del movimiento debe ser distinto de cero");
        }

        if (movimiento.getTipoMovimiento() == TipoMovimiento.CREDITO && movimiento.getValor().compareTo(CERO) <= 0) {
            throw new MovimientoInvalidoException("El valor de un credito debe ser positivo");
        }

        if (movimiento.getTipoMovimiento() == TipoMovimiento.DEBITO && movimiento.getValor().compareTo(CERO) >= 0) {
            throw new MovimientoInvalidoException("El valor de un debito debe ser negativo");
        }
    }
}
