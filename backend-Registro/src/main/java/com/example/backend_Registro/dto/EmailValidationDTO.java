package com.example.backend_Registro.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EmailValidationDTO(
        String email,
        String deliverability,
        @JsonProperty("is_disposable_email") DisposableInfo isDisposableEmail
) {
    public record DisposableInfo(boolean value) {}
}
