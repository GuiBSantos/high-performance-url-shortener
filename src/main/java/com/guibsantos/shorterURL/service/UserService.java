package com.guibsantos.shorterURL.service;

import com.guibsantos.shorterURL.controller.dto.request.RegisterRequest;
import com.guibsantos.shorterURL.controller.dto.response.UserResponse;
import com.guibsantos.shorterURL.entity.Role;
import com.guibsantos.shorterURL.entity.UserEntity;
import com.guibsantos.shorterURL.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailProducer emailProducer;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailProducer emailProducer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailProducer = emailProducer;
    }

    public UserResponse updateAvatar(String username, String avatarUrl) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        user.setAvatarUrl(avatarUrl);
        UserEntity savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getAvatarUrl()
        );
    }

    @Transactional
    public void registerUser(RegisterRequest request) {

        String cleanUsername = request.username().trim();
        String cleanEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByUsername(cleanUsername)) {
            throw new RuntimeException("Username já existe!");
        }

        if(userRepository.existsByEmail(cleanEmail)) {
            throw new RuntimeException("Email já está em uso!");
        }

        var user = UserEntity.builder()
                .username(cleanUsername)
                .email(cleanEmail)
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        emailProducer.sendEmailMessage(
                cleanEmail,
                "Bem-vindo ao Shorten!",
                cleanUsername,
                "WELCOME"
        );
    }
}