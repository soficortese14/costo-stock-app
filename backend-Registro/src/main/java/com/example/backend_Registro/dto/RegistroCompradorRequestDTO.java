package com.example.backend_Registro.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroCompradorRequestDTO(
        @NotBlank(message = "El nombre completo es obligatorio")
        String nombre,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El formato del correo no es válido")
        String email,

        @NotBlank(message = "El teléfono de contacto es obligatorio")
        String numero,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        @AssertTrue(message = "Debe aceptar los términos y condiciones")
        boolean aceptoTerminos
) {}
