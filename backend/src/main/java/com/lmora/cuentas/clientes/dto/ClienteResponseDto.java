package com.lmora.cuentas.clientes.dto;

import com.lmora.cuentas.clientes.model.Genero;

public record ClienteResponseDto(
        Long clienteId,
        String nombre,
        Genero genero,
        Integer edad,
        String identificacion,
        String direccion,
        String telefono,
        Boolean estado
) {
}
