package com.guibsantos.shorterURL.controller.dto.request;

public record ResetPasswordRequest(String email, String code, String newPassword) {}
