package com.lmora.cuentas.movimientos.repository;

import com.lmora.cuentas.movimientos.model.Movimiento;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    @Query("""
            select movimiento
            from Movimiento movimiento
            where movimiento.cuenta.cuentaId = :cuentaId
            order by movimiento.fecha asc, movimiento.movimientoId asc
            """)
    List<Movimiento> findMovimientosDeCuenta(@Param("cuentaId") Long cuentaId);

    @Query("""
            select movimiento
            from Movimiento movimiento
            where movimiento.cuenta.cliente.personaId = :clienteId
              and movimiento.fecha <= :fechaHasta
            order by movimiento.cuenta.cuentaId asc, movimiento.fecha asc, movimiento.movimientoId asc
            """)
    List<Movimiento> findMovimientosParaReporte(
            @Param("clienteId") Long clienteId,
            @Param("fechaHasta") LocalDateTime fechaHasta
    );

    @Query("""
            select case when count(movimiento) > 0 then true else false end
            from Movimiento movimiento
            where movimiento.cuenta.cuentaId = :cuentaId
            """)
    boolean existsMovimientosDeCuenta(@Param("cuentaId") Long cuentaId);
}
