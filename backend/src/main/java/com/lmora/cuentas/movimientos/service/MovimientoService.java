package com.lmora.cuentas.movimientos.service;

import com.lmora.cuentas.movimientos.model.Movimiento;
import java.util.List;

public interface MovimientoService {

    List<Movimiento> listarMovimientos();

    Movimiento obtenerMovimientoPorId(Long movimientoId);

    Movimiento crearMovimiento(Long cuentaId, Movimiento movimiento);

    Movimiento actualizarMovimiento(Long movimientoId, Long cuentaId, Movimiento movimiento);

    Movimiento actualizarParcialMovimiento(Long movimientoId, Long cuentaId, Movimiento movimiento);

    void eliminarMovimiento(Long movimientoId);
}
