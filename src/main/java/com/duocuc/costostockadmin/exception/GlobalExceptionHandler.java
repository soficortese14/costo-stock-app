package com.duocuc.costostockadmin.exception;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;


//Global ExceptionHandler es siempre el mismo.

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleValidationErrors(
            MethodArgumentNotValidException ex){
        // LinkedHashMap mantiene el orden de inserción.
        Map<String, String> errores = new LinkedHashMap<>();
        // getFieldErrors() devuelve uno por cada campo inválido.
        // getField()          → nombre del campo del DTO ("titulo", "precio"...)
        // getDefaultMessage() → el texto del message= en la anotación
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(),error.getDefaultMessage()));

        // 400 Bad Request: el cliente envió datos inválidos.
        return ResponseEntity.badRequest().body(errores);
    }

    // ── ERROR DE NEGOCIO (categoría no encontrada, etc.) ──
    // Se dispara cuando el Service lanza RuntimeException,
    // por ejemplo: "Categoría no encontrada con id: 99"
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String,String>> handleRuntimeException(
            RuntimeException ex){
        Map<String, String> error = new LinkedHashMap<>();
        error.put("error", ex.getMessage());
        // 400 Bad Request: el cliente envió un dato que no existe
        // (un categoriaId inválido es un error del cliente, no del servidor).
        // Usamos 400 y no 500 porque el servidor funcionó correctamente;
        // fue el dato enviado el que causó el problema.
        return ResponseEntity.badRequest().body(error);
    }
}
