package com.guibsantos.shorterURL.controller.docs;

import com.guibsantos.shorterURL.controller.dto.request.*;
import com.guibsantos.shorterURL.controller.dto.response.LoginResponse;
import com.guibsantos.shorterURL.controller.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Autenticação", description = "Endpoints para registro, login, perfil e recuperação de senha")
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

    @Operation(summary = "Perfil do Usuário", description = "Retorna os dados do usuário logado (baseado no Token)")
    @SecurityRequirement(name = "bearer-key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados recuperados com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "403", description = "Token inválido ou não enviado")
    })
    ResponseEntity<UserResponse> getMyProfile();

    @Operation(summary = "Verificar disponibilidade de Usuário", description = "Retorna TRUE se o nome de usuário JÁ EXISTE (indisponível) e FALSE se estiver livre.")
    @ApiResponse(responseCode = "200", description = "Verificação realizada")
    ResponseEntity<Boolean> checkUsername(
            @Parameter(description = "O username para verificar", example = "jinx5")
            @PathVariable String username
    );

    @Operation(summary = "Verificar disponibilidade de E-mail", description = "Retorna TRUE se o e-mail JÁ EXISTE (indisponível) e FALSE se estiver livre.")
    @ApiResponse(responseCode = "200", description = "Verificação realizada")
    ResponseEntity<Boolean> checkEmail(
            @Parameter(description = "O e-mail para verificar", example = "teste@email.com")
            @RequestParam("value") String email
    );

    @Operation(
            summary = "Alterar senha do usuário (Logado)",
            description = "Permite que um usuário já autenticado altere sua senha. Requer a senha atual para validação."
    )
    @SecurityRequirement(name = "bearer-key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Token inválido ou não informado"),
            @ApiResponse(responseCode = "400", description = "Senha atual incorreta ou nova senha inválida"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> changePassword(
            @Parameter(description = "Objeto contendo a senha atual e a nova senha", required = true)
            @RequestBody ChangePasswordRequest request
    );


    @Operation(
            summary = "Solicitar código de recuperação (Esqueci a Senha)",
            description = "Envia um e-mail com um código de 6 dígitos para o usuário, caso o e-mail esteja cadastrado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se o e-mail existir, o código foi enviado"),
            @ApiResponse(responseCode = "404", description = "E-mail não encontrado no sistema"),
            @ApiResponse(responseCode = "500", description = "Erro ao enviar o e-mail")
    })
    ResponseEntity<Void> forgotPassword(
            @Parameter(description = "JSON contendo apenas o e-mail do usuário", required = true)
            @RequestBody ForgotPasswordRequest request
    );

    @Operation(
            summary = "Redefinir senha com código",
            description = "Valida o código recebido por e-mail e define a nova senha do usuário."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha redefinida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Código inválido, expirado ou dados incorretos"),
            @ApiResponse(responseCode = "404", description = "E-mail não encontrado")
    })
    ResponseEntity<Void> resetPassword(
            @Parameter(description = "JSON contendo e-mail, código recebido e nova senha", required = true)
            @RequestBody ResetPasswordRequest request
    );

    @Operation(
            summary = "Validar código de recuperação (Passo 2)",
            description = "Verifica se o código digitado está correto e não expirou, antes de permitir que o usuário digite a nova senha."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Código válido, pode prosseguir para a nova senha"),
            @ApiResponse(responseCode = "400", description = "Código incorreto ou expirado"),
            @ApiResponse(responseCode = "404", description = "E-mail não encontrado")
    })
    ResponseEntity<Void> validateCode(
            @Parameter(description = "JSON contendo e-mail e o código de 6 dígitos", required = true)
            @RequestBody ValidateCodeRequest request
    );

    @Operation(
            summary = "Atualizar Username",
            description = "Atualiza o nome de usuário da conta logada. Requer que o novo nome esteja disponível."
    )
    @SecurityRequirement(name = "bearer-key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Username atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Nome de usuário já existe ou inválido"),
            @ApiResponse(responseCode = "403", description = "Token inválido")
    })
    ResponseEntity<Void> updateUsername(
            @Parameter(description = "Novo username desejado", required = true)
            @RequestBody UpdateUsernameRequest request
    );

    @Operation(
            summary = "Excluir Conta",
            description = "Exclui permanentemente a conta do usuário e todos os dados relacionados (URLs, etc). Requer confirmação de senha."
    )
    @SecurityRequirement(name = "bearer-key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta excluída com sucesso"),
            @ApiResponse(responseCode = "400", description = "Senha incorreta"),
            @ApiResponse(responseCode = "403", description = "Token inválido")
    })
    ResponseEntity<Void> deleteAccount(
            @Parameter(description = "Senha atual para confirmação", required = true)
            @RequestBody DeleteAccountRequest request
    );
}