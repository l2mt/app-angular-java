package com.lmora.cuentas.clientes.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "cliente_id", referencedColumnName = "persona_id")
public class Cliente extends Persona {

    @NotBlank
    @Size(min = 4, max = 255)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;

    @NotNull
    @Column(name = "estado", nullable = false)
    private Boolean estado = Boolean.TRUE;
}
