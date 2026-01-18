package com.guibsantos.shorterURL.controller;

import com.guibsantos.shorterURL.controller.docs.AuthControllerDocs;
import com.guibsantos.shorterURL.controller.dto.request.LoginRequest;
import com.guibsantos.shorterURL.controller.dto.request.RegisterRequest;
import com.guibsantos.shorterURL.controller.dto.response.LoginResponse;
import com.guibsantos.shorterURL.entity.UserEntity;
import com.guibsantos.shorterURL.service.TokenService;
import com.guibsantos.shorterURL.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final UserService userService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    @Override
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        userService.registerUser(request);
        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        var usernamePassword = new UsernamePasswordAuthenticationToken(request.username(), request.password());
        var auth = authenticationManager.authenticate(usernamePassword);

        var user = (UserEntity) auth.getPrincipal();

        var token = tokenService.generateToken(user);

        return ResponseEntity.ok(new LoginResponse(token));
    }
}
