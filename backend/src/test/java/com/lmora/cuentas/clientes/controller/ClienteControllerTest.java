package com.lmora.cuentas.clientes.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lmora.cuentas.clientes.dto.ClientePatchRequestDto;
import com.lmora.cuentas.clientes.dto.ClienteRequestDto;
import com.lmora.cuentas.clientes.dto.ClienteResponseDto;
import com.lmora.cuentas.clientes.exception.ClienteIdentificacionDuplicadaException;
import com.lmora.cuentas.clientes.mapper.ClienteMapper;
import com.lmora.cuentas.clientes.model.Cliente;
import com.lmora.cuentas.clientes.model.Genero;
import com.lmora.cuentas.clientes.service.ClienteService;
import com.lmora.cuentas.shared.exception.GlobalExceptionHandler;
import com.lmora.cuentas.shared.exception.ResourceNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(ClienteController.class)
@Import(GlobalExceptionHandler.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClienteService clienteService;

    @MockitoBean
    private ClienteMapper clienteMapper;

    @Test
    void listarClientes_cuandoExistenClientes_retorna200() throws Exception {
        Cliente cliente = crearCliente(1L);
        ClienteResponseDto response = crearClienteResponseDto(1L);

        given(clienteService.listarClientes()).willReturn(List.of(cliente));
        given(clienteMapper.toResponse(cliente)).willReturn(response);

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clienteId").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Jose Lema"));
    }

    @Test
    void obtenerClientePorId_cuandoExiste_retorna200() throws Exception {
        Cliente cliente = crearCliente(1L);
        ClienteResponseDto response = crearClienteResponseDto(1L);

        given(clienteService.obtenerClientePorId(1L)).willReturn(cliente);
        given(clienteMapper.toResponse(cliente)).willReturn(response);

        mockMvc.perform(get("/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(1))
                .andExpect(jsonPath("$.identificacion").value("1234567890"));
    }

    @Test
    void obtenerClientePorId_cuandoNoExiste_retorna404() throws Exception {
        given(clienteService.obtenerClientePorId(99L))
                .willThrow(new ResourceNotFoundException("Cliente", "id", 99L));

        mockMvc.perform(get("/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Cliente no encontrado con id 99"))
                .andExpect(jsonPath("$.path").value("/clientes/99"));
    }

    @Test
    void crearCliente_cuandoRequestEsValido_retorna201() throws Exception {
        ClienteRequestDto requestDto = crearClienteRequestDto();
        Cliente cliente = crearCliente(1L);
        ClienteResponseDto response = crearClienteResponseDto(1L);

        given(clienteMapper.toEntity(any(ClienteRequestDto.class))).willReturn(cliente);
        given(clienteService.crearCliente(cliente)).willReturn(cliente);
        given(clienteMapper.toResponse(cliente)).willReturn(response);

        mockMvc.perform(post("/clientes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/clientes/1")))
                .andExpect(jsonPath("$.clienteId").value(1))
                .andExpect(jsonPath("$.nombre").value("Jose Lema"))
                .andExpect(jsonPath("$.telefono").value("0987654321"));
    }

    @Test
    void crearCliente_cuandoRequestEsInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("La solicitud contiene datos invalidos"))
                .andExpect(jsonPath("$.validationErrors.nombre").exists())
                .andExpect(jsonPath("$.validationErrors.identificacion").exists())
                .andExpect(jsonPath("$.validationErrors.contrasena").exists());
    }

    @Test
    void crearCliente_cuandoIdentificacionEstaDuplicada_retorna409() throws Exception {
        ClienteRequestDto requestDto = crearClienteRequestDto();
        Cliente cliente = crearCliente(null);

        given(clienteMapper.toEntity(any(ClienteRequestDto.class))).willReturn(cliente);
        given(clienteService.crearCliente(cliente))
                .willThrow(new ClienteIdentificacionDuplicadaException("1234567890"));

        mockMvc.perform(post("/clientes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Ya existe un cliente con la identificacion 1234567890"))
                .andExpect(jsonPath("$.path").value("/clientes"));
    }

    @Test
    void actualizarParcialCliente_cuandoRequestEsValido_retorna200() throws Exception {
        ClientePatchRequestDto requestDto = new ClientePatchRequestDto(
                null,
                null,
                null,
                null,
                "Quito Norte",
                "0970001111",
                null,
                false
        );
        Cliente clientePatch = new Cliente();
        clientePatch.setDireccion("Quito Norte");
        clientePatch.setTelefono("0970001111");
        clientePatch.setEstado(false);

        Cliente clienteActualizado = crearCliente(1L);
        clienteActualizado.setDireccion("Quito Norte");
        clienteActualizado.setTelefono("0970001111");
        clienteActualizado.setEstado(false);

        ClienteResponseDto response = new ClienteResponseDto(
                1L,
                "Jose Lema",
                Genero.MASCULINO,
                30,
                "1234567890",
                "Quito Norte",
                "0970001111",
                false
        );

        given(clienteMapper.toEntity(any(ClientePatchRequestDto.class))).willReturn(clientePatch);
        given(clienteService.actualizarParcialCliente(1L, clientePatch)).willReturn(clienteActualizado);
        given(clienteMapper.toResponse(clienteActualizado)).willReturn(response);

        mockMvc.perform(patch("/clientes/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(1))
                .andExpect(jsonPath("$.direccion").value("Quito Norte"))
                .andExpect(jsonPath("$.telefono").value("0970001111"))
                .andExpect(jsonPath("$.estado").value(false));
    }

    @Test
    void eliminarCliente_cuandoExiste_retorna204() throws Exception {
        doNothing().when(clienteService).eliminarCliente(1L);

        mockMvc.perform(delete("/clientes/1"))
                .andExpect(status().isNoContent());
    }

    private ClienteRequestDto crearClienteRequestDto() {
        return new ClienteRequestDto(
                "Jose Lema",
                Genero.MASCULINO,
                30,
                "1234567890",
                "Otavalo sn y principal",
                "0987654321",
                "1234",
                true
        );
    }

    private ClienteResponseDto crearClienteResponseDto(Long clienteId) {
        return new ClienteResponseDto(
                clienteId,
                "Jose Lema",
                Genero.MASCULINO,
                30,
                "1234567890",
                "Otavalo sn y principal",
                "0987654321",
                true
        );
    }

    private Cliente crearCliente(Long clienteId) {
        Cliente cliente = new Cliente();
        cliente.setPersonaId(clienteId);
        cliente.setNombre("Jose Lema");
        cliente.setGenero(Genero.MASCULINO);
        cliente.setEdad(30);
        cliente.setIdentificacion("1234567890");
        cliente.setDireccion("Otavalo sn y principal");
        cliente.setTelefono("0987654321");
        cliente.setContrasena("1234");
        cliente.setEstado(true);
        return cliente;
    }
}
