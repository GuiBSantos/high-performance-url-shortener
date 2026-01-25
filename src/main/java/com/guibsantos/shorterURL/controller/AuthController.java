package com.guibsantos.shorterURL.controller;

import com.guibsantos.shorterURL.controller.docs.AuthControllerDocs;
import com.guibsantos.shorterURL.controller.dto.request.*;
import com.guibsantos.shorterURL.controller.dto.response.LoginResponse;
import com.guibsantos.shorterURL.controller.dto.response.UserResponse;
import com.guibsantos.shorterURL.entity.UserEntity;
import com.guibsantos.shorterURL.repository.UserRepository;
import com.guibsantos.shorterURL.service.AuthService;
import com.guibsantos.shorterURL.service.TokenService;
import com.guibsantos.shorterURL.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final UserService userService;
    private final AuthService authService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @Override
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        userService.registerUser(request);
        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(request.email(), request.password());
            var auth = authenticationManager.authenticate(usernamePassword);

            var user = (UserEntity) auth.getPrincipal();
            var token = tokenService.generateToken(user);

            return ResponseEntity.ok(new LoginResponse(token));

        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            throw e;
        }
    }

    @Override
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserEntity) {
            UserEntity user = (UserEntity) authentication.getPrincipal();

            return ResponseEntity.ok(new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getAvatarUrl()
            ));
        }
        return ResponseEntity.status(403).build();
    }

    @Override
    @GetMapping("/check-username/{username}")
    public ResponseEntity<Boolean> checkUsername(@PathVariable String username) {
        boolean exists = userRepository.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }

    @Override
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam("value") String email) {
        boolean exists = userRepository.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    @Override
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.email(), request.code(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/validate-code")
    public ResponseEntity<Void> validateCode(@RequestBody ValidateCodeRequest request) {
        authService.validateRecoveryCode(request.email(), request.code());
        return ResponseEntity.ok().build();
    }

    @Override
    @PatchMapping("/update-username")
    public ResponseEntity<Void> updateUsername(@RequestBody @Valid UpdateUsernameRequest request) {
        authService.updateUsername(request.newUsername());
        return ResponseEntity.ok().build();
    }

    @Override
    @DeleteMapping("/delete-account")
    public ResponseEntity<Void> deleteAccount(@RequestBody @Valid DeleteAccountRequest request) {
        authService.deleteAccount(request.password());
        return ResponseEntity.ok().build();
    }

}