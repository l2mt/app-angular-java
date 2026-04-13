package com.lmora.cuentas.reportes.dto;

import com.lmora.cuentas.movimientos.model.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReporteMovimientoDto(
        Long movimientoId,
        LocalDateTime fecha,
        TipoMovimiento tipoMovimiento,
        BigDecimal valor,
        BigDecimal saldo
) {
}
