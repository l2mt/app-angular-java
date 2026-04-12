package com.lmora.cuentas.clientes.service;

import com.lmora.cuentas.clientes.model.Cliente;
import java.util.List;

public interface ClienteService {

    List<Cliente> listarClientes();

    Cliente obtenerClientePorId(Long clienteId);

    Cliente crearCliente(Cliente cliente);

    Cliente actualizarCliente(Long clienteId, Cliente cliente);

    Cliente actualizarParcialCliente(Long clienteId, Cliente cliente);

    void eliminarCliente(Long clienteId);
}
