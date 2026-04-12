package com.lmora.cuentas.reportes.dto;

import com.lmora.cuentas.cuentas.model.TipoCuenta;
import java.math.BigDecimal;
import java.util.List;

public record ReporteCuentaDto(
        Long cuentaId,
        String numeroCuenta,
        TipoCuenta tipoCuenta,
        BigDecimal saldoInicial,
        BigDecimal saldoDisponible,
        BigDecimal totalCreditos,
        BigDecimal totalDebitos,
        List<ReporteMovimientoDto> movimientos
) {
}
