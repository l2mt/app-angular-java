package com.lmora.cuentas.reportes.dto;

public record ReporteClienteDto(
        Long clienteId,
        String nombre,
        String identificacion
) {
}
