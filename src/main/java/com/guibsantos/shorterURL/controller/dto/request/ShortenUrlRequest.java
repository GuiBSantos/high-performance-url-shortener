package com.guibsantos.shorterURL.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ShortenUrlRequest(
        @NotBlank(message = "A URL não pode estar vazia.")
        String url,

        Integer maxClicks,

        Long expirationTimeInMinutes
) {
}