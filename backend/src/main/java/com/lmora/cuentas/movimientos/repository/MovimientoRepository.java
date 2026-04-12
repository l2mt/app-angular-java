package com.lmora.cuentas.movimientos.repository;

import com.lmora.cuentas.movimientos.model.Movimiento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByCuentaCuentaIdOrderByFechaAscMovimientoIdAsc(Long cuentaId);
}
