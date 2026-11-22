package com.example.backend_Registro.repository;

import com.example.backend_Registro.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    boolean existsByRut(String rut);
    boolean existsByEmail(String email);
}
