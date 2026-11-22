package com.example.backend_Registro.service;

import com.example.backend_Registro.dto.EmailValidationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class EmailValidationService {

    private static final Logger log = LoggerFactory.getLogger(EmailValidationService.class);
    private final RestClient restClient;

    @Value("${external.email-api.key}")
    private String apiKey;

    public EmailValidationService(RestClient emailValidationRestClient) {
        this.restClient = emailValidationRestClient;
    }

    public void validarCorreoExterno(String email) {
        if (apiKey == null || apiKey.equals("TU_API_KEY_AQUI") || apiKey.isBlank()) {
            log.warn("API Key no configurada. Omitiendo validación externa para: {}", email);
            return;
        }

        try {
            EmailValidationDTO validacion = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("api_key", apiKey)
                            .queryParam("email", email)
                            .build())
                    .retrieve()
                    .body(EmailValidationDTO.class);

            if (validacion != null) {
                boolean noEntregable = "UNDELIVERABLE".equalsIgnoreCase(validacion.deliverability());
                boolean esTemporal = validacion.isDisposableEmail() != null && validacion.isDisposableEmail().value();

                if (noEntregable || esTemporal) {
                    throw new IllegalArgumentException("El correo electrónico no existe o es una cuenta temporal.");
                }
            }
        } catch (ResourceAccessException e) {
            log.warn("Timeout o error de red con API de correos (Fail-Open): {}", e.getMessage());
        } catch (RestClientResponseException e) {
            log.warn("Error HTTP {} desde API de correos (Fail-Open).", e.getStatusCode());
        }
    }
}
