package com.lmora.cuentas.movimientos.dto;

import com.lmora.cuentas.movimientos.model.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoResponseDto(
        Long movimientoId,
        Long cuentaId,
        LocalDateTime fecha,
        TipoMovimiento tipoMovimiento,
        BigDecimal valor,
        BigDecimal saldo
) {
}
