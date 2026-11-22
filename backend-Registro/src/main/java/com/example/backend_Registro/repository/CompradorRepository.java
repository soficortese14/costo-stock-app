package com.example.backend_Registro.repository;

import com.example.backend_Registro.model.Comprador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompradorRepository extends JpaRepository<Comprador, Long> {
    boolean existsByEmail(String email);
}
