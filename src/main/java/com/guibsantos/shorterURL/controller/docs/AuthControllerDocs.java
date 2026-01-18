package com.guibsantos.shorterURL.controller.docs;

import com.guibsantos.shorterURL.controller.dto.request.LoginRequest;
import com.guibsantos.shorterURL.controller.dto.request.RegisterRequest;
import com.guibsantos.shorterURL.controller.dto.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Autenticação", description = "Endpoints para registro e login de usuários")
public interface AuthControllerDocs {

    @Operation(
            summary = "Registrar novo usuário",
            description = "Cria uma nova conta de usuário no sistema com senha criptografada."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação (Username ou Email já existem)"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    ResponseEntity<String> register(
            @Parameter(description = "JSON contendo username, email e senha", required = true)
            @RequestBody RegisterRequest request
    );

    @Operation(summary = "Realizar Login", description = "Autentica o usuário e retorna um Token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Usuário ou senha inválidos")
    })
    ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request);
}
