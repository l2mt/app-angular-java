package com.lmora.cuentas.cuentas.repository;

import com.lmora.cuentas.cuentas.model.Cuenta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    boolean existsByNumeroCuenta(String numeroCuenta);

    @Query("""
            select case when count(cuenta) > 0 then true else false end
            from Cuenta cuenta
            where cuenta.numeroCuenta = :numeroCuenta
              and cuenta.cuentaId <> :cuentaId
            """)
    boolean existsOtraCuentaConNumero(
            @Param("numeroCuenta") String numeroCuenta,
            @Param("cuentaId") Long cuentaId
    );

    @Query("""
            select case when count(cuenta) > 0 then true else false end
            from Cuenta cuenta
            where cuenta.cliente.personaId = :clienteId
            """)
    boolean existsCuentasDeCliente(@Param("clienteId") Long clienteId);

    @Query("""
            select cuenta
            from Cuenta cuenta
            where cuenta.cliente.personaId = :clienteId
            order by cuenta.numeroCuenta asc
            """)
    List<Cuenta> findCuentasDeCliente(@Param("clienteId") Long clienteId);
}
