package com.guibsantos.shorterURL.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUsernameRequest(
        @NotBlank @Size(min=3, max=20) String newUsername
) {}
