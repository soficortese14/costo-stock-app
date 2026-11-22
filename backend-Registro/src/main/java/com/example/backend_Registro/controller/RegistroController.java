package com.example.backend_Registro.controller;

import com.example.backend_Registro.dto.RegistroCompradorRequestDTO;
import com.example.backend_Registro.dto.RegistroProveedorRequestDTO;
import com.example.backend_Registro.model.Comprador;
import com.example.backend_Registro.model.Proveedor;
import com.example.backend_Registro.service.RegistroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/registro")
@CrossOrigin(origins = "*")
public class RegistroController {

    private final RegistroService registroService;

    public RegistroController(RegistroService registroService) {
        this.registroService = registroService;
    }

    @PostMapping("/comprador")
    public ResponseEntity<Comprador> registrarComprador(@Valid @RequestBody RegistroCompradorRequestDTO request) {
        Comprador creado = registroService.registrarComprador(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PostMapping("/proveedor")
    public ResponseEntity<Proveedor> registrarProveedor(@Valid @RequestBody RegistroProveedorRequestDTO request) {
        Proveedor creado = registroService.registrarProveedor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/compradores")
    public ResponseEntity<List<Comprador>> listarCompradores() {
        return ResponseEntity.ok(registroService.listarCompradores());
    }

    @GetMapping("/proveedores")
    public ResponseEntity<List<Proveedor>> listarProveedores() {
        return ResponseEntity.ok(registroService.listarProveedores());
    }
}
