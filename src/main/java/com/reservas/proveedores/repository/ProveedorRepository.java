package com.reservas.proveedores.repository;

import com.reservas.proveedores.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    // ---- Duplicados del registro (HU-16). El NIT usa existsById(nit), que ya trae JpaRepository ----
    boolean existsByCorreo(String correo);
    boolean existsByTelefono(String telefono);
    boolean existsByRazonSocial(String razonSocial);
    boolean existsByLogoHash(String logoHash);       // logo "no registrado previamente"

    // ---- Login (CuentaAutenticable): el correo es único ----
    Optional<Proveedor> findByCorreo(String correo);
}