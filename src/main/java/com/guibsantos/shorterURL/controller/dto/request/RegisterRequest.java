package com.guibsantos.shorterURL.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "O username não pode estar vazio")
        @Size(min = 3, max = 20, message = "O username deve ter entre 3 e 20 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9._]+$", message = "Username deve conter apenas letras, números, ponto ou underline")
        String username,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        @Size(max = 100, message = "O e-mail é muito longo")
        String email,

        @NotBlank
        @Size(min = 8, max = 64, message = "A senha deve ter entre 8 e 64 caracteres")
        String password
) {}
