package com.lmora.cuentas.cuentas.dto;

import com.lmora.cuentas.cuentas.model.TipoCuenta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CuentaPatchRequestDto(
        @Positive
        Long clienteId,

        @Pattern(regexp = ".*\\S.*", message = "numeroCuenta no debe estar vacio")
        @Size(max = 30)
        String numeroCuenta,

        TipoCuenta tipoCuenta,

        @DecimalMin(value = "0.00")
        @Digits(integer = 14, fraction = 2)
        BigDecimal saldoInicial,

        Boolean estado
) {
}
