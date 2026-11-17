package com.duocuc.costostockadmin.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class AdminRequestDTO {

    @NotBlank(message = "El correo no puede estar vacío")
    private String correo;

    @NotBlank(message = "La contraseña no puede estar vacía")
    private String contraseña;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombreAdmin;

    @NotBlank(message = "El apellido paterno no puede estar vacío")
    private String appaternoAdmin;

    @NotBlank(message = "El apellido materno no puede estar vacío")
    private String apmaternoAdmin;

    @NotNull(message = "La fecha no puede estar vacía.")
    private LocalDate fecha_nac;
}