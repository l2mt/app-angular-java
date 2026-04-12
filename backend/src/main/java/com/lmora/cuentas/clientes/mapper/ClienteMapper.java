package com.lmora.cuentas.clientes.mapper;

import com.lmora.cuentas.clientes.dto.ClienteRequestDto;
import com.lmora.cuentas.clientes.dto.ClientePatchRequestDto;
import com.lmora.cuentas.clientes.dto.ClienteResponseDto;
import com.lmora.cuentas.clientes.model.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteRequestDto requestDto) {
        Cliente cliente = new Cliente();
        cliente.setNombre(requestDto.nombre());
        cliente.setGenero(requestDto.genero());
        cliente.setEdad(requestDto.edad());
        cliente.setIdentificacion(requestDto.identificacion());
        cliente.setDireccion(requestDto.direccion());
        cliente.setTelefono(requestDto.telefono());
        cliente.setContrasena(requestDto.contrasena());
        cliente.setEstado(requestDto.estado());
        return cliente;
    }

    public Cliente toEntity(ClientePatchRequestDto requestDto) {
        Cliente cliente = new Cliente();
        cliente.setNombre(requestDto.nombre());
        cliente.setGenero(requestDto.genero());
        cliente.setEdad(requestDto.edad());
        cliente.setIdentificacion(requestDto.identificacion());
        cliente.setDireccion(requestDto.direccion());
        cliente.setTelefono(requestDto.telefono());
        cliente.setContrasena(requestDto.contrasena());
        cliente.setEstado(requestDto.estado());
        return cliente;
    }

    public ClienteResponseDto toResponse(Cliente cliente) {
        return new ClienteResponseDto(
                cliente.getPersonaId(),
                cliente.getNombre(),
                cliente.getGenero(),
                cliente.getEdad(),
                cliente.getIdentificacion(),
                cliente.getDireccion(),
                cliente.getTelefono(),
                cliente.getEstado()
        );
    }
}
