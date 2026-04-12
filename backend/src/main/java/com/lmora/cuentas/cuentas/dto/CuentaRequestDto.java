package com.lmora.cuentas.cuentas.dto;

import com.lmora.cuentas.cuentas.model.TipoCuenta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CuentaRequestDto(
        @NotNull
        @Positive
        Long clienteId,

        @NotBlank
        @Size(max = 30)
        String numeroCuenta,

        @NotNull
        TipoCuenta tipoCuenta,

        @NotNull
        @DecimalMin(value = "0.00")
        @Digits(integer = 14, fraction = 2)
        BigDecimal saldoInicial,

        @NotNull
        Boolean estado
) {
}
