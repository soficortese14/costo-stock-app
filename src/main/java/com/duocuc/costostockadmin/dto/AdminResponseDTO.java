package com.duocuc.costostockadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminResponseDTO {
    private Long idAdmin;

    private String correo;

    private String contraseña;

    private String nombreAdmin;

    private String appaternoAdmin;

    private String apmaternoAdmin;

    private LocalDate fecha_nac;
}
