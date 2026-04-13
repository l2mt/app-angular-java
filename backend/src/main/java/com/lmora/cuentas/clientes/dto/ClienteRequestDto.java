package com.lmora.cuentas.clientes.dto;

import com.lmora.cuentas.clientes.model.Genero;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClienteRequestDto(
        @NotBlank
        @Size(max = 100)
        String nombre,

        @NotNull
        Genero genero,

        @NotNull
        @Min(0)
        @Max(120)
        Integer edad,

        @NotBlank
        @Size(max = 20)
        String identificacion,

        @Size(max = 200)
        String direccion,

        @Size(max = 20)
        String telefono,

        @NotBlank
        @Size(min = 4, max = 255)
        String contrasena,

        @NotNull
        Boolean estado
) {
}
