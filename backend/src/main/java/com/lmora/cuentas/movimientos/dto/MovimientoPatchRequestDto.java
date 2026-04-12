package com.lmora.cuentas.movimientos.dto;

import com.lmora.cuentas.movimientos.model.TipoMovimiento;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoPatchRequestDto(
        @Positive
        Long cuentaId,

        LocalDateTime fecha,

        TipoMovimiento tipoMovimiento,

        @Digits(integer = 14, fraction = 2)
        BigDecimal valor
) {
}
