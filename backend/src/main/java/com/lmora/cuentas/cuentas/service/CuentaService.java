package com.lmora.cuentas.cuentas.service;

import com.lmora.cuentas.cuentas.model.Cuenta;
import java.util.List;

public interface CuentaService {

    List<Cuenta> listarCuentas();

    Cuenta obtenerCuentaPorId(Long cuentaId);

    Cuenta crearCuenta(Long clienteId, Cuenta cuenta);

    Cuenta actualizarCuenta(Long cuentaId, Long clienteId, Cuenta cuenta);

    Cuenta actualizarParcialCuenta(Long cuentaId, Long clienteId, Cuenta cuenta);

    void eliminarCuenta(Long cuentaId);
}
