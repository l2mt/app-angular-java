package com.lmora.cuentas.clientes.repository;

import com.lmora.cuentas.clientes.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByIdentificacion(String identificacion);

    @Query("""
            select case when count(cliente) > 0 then true else false end
            from Cliente cliente
            where cliente.identificacion = :identificacion
              and cliente.personaId <> :clienteId
            """)
    boolean existsOtroClienteConIdentificacion(
            @Param("identificacion") String identificacion,
            @Param("clienteId") Long clienteId
    );
}
