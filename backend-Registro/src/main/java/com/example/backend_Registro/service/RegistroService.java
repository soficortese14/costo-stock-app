package com.example.backend_Registro.service;

import com.example.backend_Registro.dto.RegistroCompradorRequestDTO;
import com.example.backend_Registro.dto.RegistroProveedorRequestDTO;
import com.example.backend_Registro.model.Comprador;
import com.example.backend_Registro.model.Proveedor;
import com.example.backend_Registro.repository.CompradorRepository;
import com.example.backend_Registro.repository.ProveedorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegistroService {

    private final CompradorRepository compradorRepository;
    private final ProveedorRepository proveedorRepository;
    private final EmailValidationService emailValidationService;

    public RegistroService(CompradorRepository compradorRepository,
                           ProveedorRepository proveedorRepository,
                           EmailValidationService emailValidationService) {
        this.compradorRepository = compradorRepository;
        this.proveedorRepository = proveedorRepository;
        this.emailValidationService = emailValidationService;
    }

    public Comprador registrarComprador(RegistroCompradorRequestDTO dto) {
        String emailLimpio = dto.email().trim().toLowerCase();

        if (compradorRepository.existsByEmail(emailLimpio)) {
            throw new IllegalArgumentException("Este correo electrónico ya está registrado.");
        }

        emailValidationService.validarCorreoExterno(emailLimpio);

        Comprador comprador = Comprador.builder()
                .nombre(dto.nombre().trim())
                .email(emailLimpio)
                .numero(dto.numero().trim())
                .password(dto.password())
                .aceptoTerminos(dto.aceptoTerminos())
                .build();

        return compradorRepository.save(comprador);
    }

    public Proveedor registrarProveedor(RegistroProveedorRequestDTO dto) {
        String rutLimpio = dto.rut().trim();
        String emailLimpio = dto.email().trim().toLowerCase();

        if (proveedorRepository.existsByRut(rutLimpio)) {
            throw new IllegalArgumentException("El RUT ya se encuentra registrado.");
        }
        if (proveedorRepository.existsByEmail(emailLimpio)) {
            throw new IllegalArgumentException("El correo de empresa ya está registrado.");
        }

        emailValidationService.validarCorreoExterno(emailLimpio);

        Proveedor proveedor = Proveedor.builder()
                .rut(rutLimpio)
                .nombre(dto.nombre().trim())
                .email(emailLimpio)
                .password(dto.password())
                .razonSocial(dto.razonSocial().trim())
                .giro(dto.giro().trim())
                .comuna(dto.comuna().trim())
                .telefono(dto.telefono().trim())
                .build();

        return proveedorRepository.save(proveedor);
    }

    public List<Comprador> listarCompradores() {
        return compradorRepository.findAll();
    }

    public List<Proveedor> listarProveedores() {
        return proveedorRepository.findAll();
    }
}
