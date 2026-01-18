package com.guibsantos.shorterURL.repository;

import com.guibsantos.shorterURL.entity.UrlEntity;
import com.guibsantos.shorterURL.entity.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<UrlEntity, Long> {

    Optional<UrlEntity> findByShortCode(String shortCode);

    @Transactional
    @Modifying
    int deleteByExpiresAtBefore(LocalDateTime dateTime);

    @Transactional
    @Modifying
    void deleteByShortCode(String shortCode);

    List<UrlEntity> findByUser(UserEntity user);

    Optional<UrlEntity> findByShortCodeAndUser(String shortCode, UserEntity user);
}
