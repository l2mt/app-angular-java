package com.lmora.cuentas.clientes.dto;

import com.lmora.cuentas.clientes.model.Genero;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClientePatchRequestDto(
        @Pattern(regexp = ".*\\S.*", message = "nombre no debe estar vacio")
        @Size(max = 100)
        String nombre,

        Genero genero,

        @Min(0)
        @Max(120)
        Integer edad,

        @Pattern(regexp = ".*\\S.*", message = "identificacion no debe estar vacia")
        @Size(max = 20)
        String identificacion,

        @Pattern(regexp = ".*\\S.*", message = "direccion no debe estar vacia")
        @Size(max = 200)
        String direccion,

        @Pattern(regexp = ".*\\S.*", message = "telefono no debe estar vacio")
        @Size(max = 20)
        String telefono,

        @Pattern(regexp = ".*\\S.*", message = "contrasena no debe estar vacia")
        @Size(min = 4, max = 255)
        String contrasena,

        Boolean estado
) {
}
