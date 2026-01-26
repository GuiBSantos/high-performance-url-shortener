package com.guibsantos.shorterURL.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.guibsantos.shorterURL.controller.dto.request.ChangePasswordRequest;
import com.guibsantos.shorterURL.controller.dto.response.GoogleLoginResponse;
import com.guibsantos.shorterURL.entity.Role;
import com.guibsantos.shorterURL.entity.UserEntity;
import com.guibsantos.shorterURL.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Random;

@Service
public class AuthService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailProducer emailProducer;
    private final TokenService tokenService;

    @Value("${google.client.id}")
    private String googleClientId;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailProducer emailProducer,
                       TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailProducer = emailProducer;
        this.tokenService = tokenService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email não encontrado!"));
    }

    @Transactional
    public GoogleLoginResponse loginWithGoogle(String token) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(token);

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                String email = payload.getEmail();
                String rawName = (String) payload.get("name");
                String baseUsername = rawName != null ? rawName : email.split("@")[0];
                String pictureUrl = (String) payload.get("picture");

                UserEntity user = userRepository.findByEmail(email).orElse(null);

                if (user != null) {
                    log.info("Login via Google efetuado para: {}", email);
                } else {
                    log.info("Registrando novo usuário via Google: {}", email);

                    user = new UserEntity();
                    user.setEmail(email);
                    String finalUsername = generateUniqueUsername(baseUsername);
                    user.setUsername(finalUsername);
                    user.setPassword("");
                    user.setRole(Role.USER);
                    user.setAvatarUrl(pictureUrl);

                    userRepository.save(user);
                }

                String jwtToken = tokenService.generateToken(user);
                return new GoogleLoginResponse(jwtToken);

            } else {
                throw new IllegalArgumentException("Token do Google inválido ou expirado.");
            }
        } catch (Exception e) {
            log.error("Erro na autenticação com Google", e);
            throw new RuntimeException("Erro na autenticação com Google: " + e.getMessage());
        }
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        UserEntity user = getAuthenticatedUser();

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("A senha atual está incorreta.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void forgotPassword(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("E-mail não encontrado."));

        String code = String.format("%06d", new Random().nextInt(999999));

        user.setRecoveryCode(code);
        user.setRecoveryCodeExpiration(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        String subject = "Recuperação de Senha - ShorterURL";
        String body = "<h3>Olá, " + user.getUsername() + "!</h3>" +
                "<p>Seu código de recuperação é:</p>" +
                "<h1>" + code + "</h1>" +
                "<p>Válido por 10 minutos.</p>";

        emailProducer.sendEmailMessage(email, subject, body);
    }

    @Transactional
    public void validateRecoveryCode(String email, String code) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("E-mail não encontrado."));

        validateCode(user, code);
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("E-mail não encontrado."));

        validateCode(user, code);

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setRecoveryCode(null);
        user.setRecoveryCodeExpiration(null);
        userRepository.save(user);
    }

    @Transactional
    public void updateUsername(String newUsername) {
        String clearUsername = newUsername.trim();

        if (userRepository.existsByUsername(clearUsername)) {
            throw new IllegalArgumentException("Este nome de usuário já está em uso.");
        }

        UserEntity user = getAuthenticatedUser();
        user.setUsername(clearUsername);
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(String password) {
        UserEntity user = getAuthenticatedUser();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Senha incorreta. Não foi possível excluir a conta.");
        }

        userRepository.delete(user);
    }

    private UserEntity getAuthenticatedUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuário logado não encontrado no banco."));
    }

    private void validateCode(UserEntity user, String code) {
        if (user.getRecoveryCode() == null || !user.getRecoveryCode().equals(code)) {
            throw new IllegalArgumentException("Código inválido.");
        }

        if (user.getRecoveryCodeExpiration().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Código expirado. Solicite um novo.");
        }
    }

    private String generateUniqueUsername(String baseName) {
        String cleanName = baseName.replaceAll("\\s+", "");

        if (!userRepository.existsByUsername(cleanName)) {
            return cleanName;
        }

        String newName = cleanName;
        Random random = new Random();

        while (userRepository.existsByUsername(newName)) {
            newName = cleanName + random.nextInt(1000, 9999);
        }

        return newName;
    }
}