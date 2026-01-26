package com.guibsantos.shorterURL.controller.docs;

import com.guibsantos.shorterURL.controller.dto.request.ShortenUrlRequest;
import com.guibsantos.shorterURL.controller.dto.response.ShortenUrlResponse;
import com.guibsantos.shorterURL.controller.dto.response.UrlStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "URL Shortener", description = "Endpoints para gerenciar e encurtar URLs")
public interface UrlControllerDocs {

    @Operation(summary = "Listar Minhas URLs", description = "Retorna o histórico de todas as URLs criadas pelo usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ShortenUrlResponse.class))),
            @ApiResponse(responseCode = "403", description = "Não autorizado (Token inválido ou ausente)")
    })
    @SecurityRequirement(name = "bearer-key")
    ResponseEntity<List<ShortenUrlResponse>> getUserUrls();

    @Operation(summary = "Obter estatísticas da URL", description = "Retorna o número total de acessos (cliques) de uma URL encurtada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso"),
            @ApiResponse(responseCode = "404", description = "URL não encontrada")
    })
    ResponseEntity<UrlStatsResponse> getUrlStats(
            @Parameter(description = "Código curto", required = true)
            @PathVariable String shortCode
    );

    @Operation(summary = "Encurtar URL", description = "Recebe uma URL original e retorna um código encurtado de 6 caracteres.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "URL encurtada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ShortenUrlResponse.class))),
            @ApiResponse(responseCode = "400", description = "URL inválida ou payload mal formatado", content = @Content)
    })
    @SecurityRequirement(name = "bearer-key")
    ResponseEntity<ShortenUrlResponse> shortenUrl(
            @RequestBody ShortenUrlRequest request,
            @Parameter(hidden = true) HttpServletRequest servletRequest
    );

    @Operation(summary = "Redirecionar", description = "Recebe um código encurtado e redireciona para a URL original.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirecionamento encontrado e executado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Código não encontrado ou expirado", content = @Content)
    })
    ResponseEntity<Void> redirect(
            @Parameter(description = "Código curto", required = true)
            @PathVariable String shortCode
    );

    @Operation(
            summary = "Deletar URL Encurtada",
            description = "Remove o link do banco de dados e libera o código para uso futuro."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sucesso: Conteúdo deletado"),
            @ApiResponse(responseCode = "404", description = "Erro: URL não encontrada")
    })
    @SecurityRequirement(name = "bearer-key")
    ResponseEntity<Void> deleteUrl(
            @Parameter(description = "O código curto da URL (ex: a1b2c3)", required = true)
            @PathVariable String shortCode
    );
}