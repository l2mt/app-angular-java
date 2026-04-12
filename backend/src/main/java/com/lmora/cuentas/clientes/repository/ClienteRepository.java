package com.lmora.cuentas.clientes.repository;

import com.lmora.cuentas.clientes.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByIdentificacion(String identificacion);

    boolean existsByIdentificacionAndPersonaIdNot(String identificacion, Long personaId);
}
