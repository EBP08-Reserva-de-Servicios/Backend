package com.reservas.clientes.repository;

import com.reservas.clientes.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {
    boolean existsByCorreo(String correo);
    boolean existsByNumeroDocumento(String numeroDocumento);
    Optional<Cliente> findByCorreo(String correo);
}