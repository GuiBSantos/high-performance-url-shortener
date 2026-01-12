package com.guibsantos.shorterURL.controller.dto.response;

public record UrlStatsResponse(
        String originalUrl,
        String shortCode,
        Long accessCount
) {}
