package com.lmora.cuentas.cuentas.dto;

import com.lmora.cuentas.cuentas.model.TipoCuenta;
import java.math.BigDecimal;

public record CuentaResponseDto(
        Long cuentaId,
        Long clienteId,
        String numeroCuenta,
        TipoCuenta tipoCuenta,
        BigDecimal saldoInicial,
        Boolean estado
) {
}
