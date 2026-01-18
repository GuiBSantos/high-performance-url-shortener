package com.guibsantos.shorterURL.controller.dto.response;

import java.time.LocalDateTime;

public record ShortenUrlResponse(String url, String shortCode, String shortUrl, LocalDateTime expiresAt) {
}
