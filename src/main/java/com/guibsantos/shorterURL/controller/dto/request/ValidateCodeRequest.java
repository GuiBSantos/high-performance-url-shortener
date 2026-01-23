package com.guibsantos.shorterURL.controller.dto.request;

public record ValidateCodeRequest(
        String email,
        String code
) {}
