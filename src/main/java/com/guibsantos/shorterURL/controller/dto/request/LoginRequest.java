package com.guibsantos.shorterURL.controller.dto.request;

public record LoginRequest(
        String email,
        String password
) {}
