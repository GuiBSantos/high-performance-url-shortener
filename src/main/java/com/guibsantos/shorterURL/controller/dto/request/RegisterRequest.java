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

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String password
) {}
