package com.lmora.cuentas.reportes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReporteEstadoCuentaResponseDto(
        ReporteClienteDto cliente,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        BigDecimal totalCreditos,
        BigDecimal totalDebitos,
        List<ReporteCuentaDto> cuentas,
        String pdfBase64
) {
}
