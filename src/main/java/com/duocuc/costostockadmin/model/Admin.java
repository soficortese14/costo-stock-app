package com.duocuc.costostockadmin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "admin")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAdmin;

    @Column(nullable = false)
    private String correo;

    @Column(nullable = false)
    private String contraseña;

    @Column(nullable = false)
    private String nombreAdmin;

    @Column(nullable = false)
    private String appaternoAdmin;

    @Column(nullable = false)
    private String apmaternoAdmin;

    @Column(nullable = false)
    private LocalDate fecha_nac;
}