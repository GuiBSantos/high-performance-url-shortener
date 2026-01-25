package com.guibsantos.shorterURL.service;

import com.guibsantos.shorterURL.controller.dto.request.ChangePasswordRequest;
import com.guibsantos.shorterURL.entity.UserEntity;
import com.guibsantos.shorterURL.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService implements UserDetailsService {

    private final EmailProducer emailProducer;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailProducer emailProducer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailProducer = emailProducer;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email não encontrado!"));
    }

    public void changePassword(ChangePasswordRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário logado não encontrado no banco."));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new RuntimeException("A senha atual está incorreta.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public void forgotPassword(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("E-mail não encontrado."));

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

    public void resetPassword(String email, String code, String newPassword) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("E-mail não encontrado."));

        if (user.getRecoveryCode() == null || !user.getRecoveryCode().equals(code)) {
            throw new RuntimeException("Código inválido.");
        }

        if (user.getRecoveryCodeExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Código expirado. Solicite um novo.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setRecoveryCode(null);
        user.setRecoveryCodeExpiration(null);
        userRepository.save(user);
    }

    public void validateRecoveryCode(String email, String code) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("E-mail não encontrado."));

        if (user.getRecoveryCode() == null || !user.getRecoveryCode().equals(code)) {
            throw new RuntimeException("Código inválido.");
        }

        if (user.getRecoveryCodeExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Código expirado. Solicite um novo.");
        }
    }

    public void updateUsername(String newUsername) {
        String clearUsername = newUsername.trim();

        if (userRepository.existsByUsername(clearUsername)) {
            throw new RuntimeException("Este nome de usuário já está em uso.");
        }

        var auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        user.setUsername(clearUsername);
        userRepository.save(user);
    }

    public void deleteAccount(String password) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Senha incorreta. Não foi possível excluir a conta.");
        }

        userRepository.delete(user);
    }
}