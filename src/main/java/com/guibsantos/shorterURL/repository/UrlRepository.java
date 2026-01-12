package com.guibsantos.shorterURL.repository;

import com.guibsantos.shorterURL.entity.UrlEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<UrlEntity, Long> {
    Optional<UrlEntity> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    @Transactional
    @Modifying
    int deleteByExpiresAtBefore(LocalDateTime dateTime);
}
