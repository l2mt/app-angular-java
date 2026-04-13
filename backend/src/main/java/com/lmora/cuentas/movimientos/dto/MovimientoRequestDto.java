package com.lmora.cuentas.movimientos.dto;

import com.lmora.cuentas.movimientos.model.TipoMovimiento;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoRequestDto(
        @NotNull
        @Positive
        Long cuentaId,

        LocalDateTime fecha,

        @NotNull
        TipoMovimiento tipoMovimiento,

        @NotNull
        @Digits(integer = 14, fraction = 2)
        BigDecimal valor
) {
}
