package com.guibsantos.shorterURL.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record ShortenUrlRequest(
        @NotBlank(message = "A URL não pode estar vazia.")
        @URL(message = "URL inválida.")
        String url,

        Integer maxClicks,

        Long expirationTimeInMinutes
) {
}