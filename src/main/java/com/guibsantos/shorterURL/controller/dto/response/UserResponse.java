package com.guibsantos.shorterURL.controller.dto.response;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String avatarUrl
) {}
