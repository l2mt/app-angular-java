package com.lmora.cuentas.cuentas.repository;

import com.lmora.cuentas.cuentas.model.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    boolean existsByNumeroCuenta(String numeroCuenta);

    boolean existsByNumeroCuentaAndCuentaIdNot(String numeroCuenta, Long cuentaId);

    boolean existsByClientePersonaId(Long clienteId);
}
