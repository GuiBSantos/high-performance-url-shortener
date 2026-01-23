package com.guibsantos.shorterURL.controller.dto.request;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword
) {
}
