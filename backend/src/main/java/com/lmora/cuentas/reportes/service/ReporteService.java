package com.lmora.cuentas.reportes.service;

import com.lmora.cuentas.reportes.dto.ReporteEstadoCuentaResponseDto;
import java.time.LocalDate;

public interface ReporteService {

    ReporteEstadoCuentaResponseDto generarEstadoCuenta(Long clienteId, LocalDate fechaDesde, LocalDate fechaHasta);
}
