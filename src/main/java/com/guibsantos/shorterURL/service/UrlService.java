package com.guibsantos.shorterURL.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guibsantos.shorterURL.controller.dto.request.ShortenUrlRequest;
import com.guibsantos.shorterURL.controller.dto.response.ShortenUrlResponse;
import com.guibsantos.shorterURL.controller.dto.response.UrlStatsResponse;
import com.guibsantos.shorterURL.controller.exception.UrlNotFoundException;
import com.guibsantos.shorterURL.entity.Role;
import com.guibsantos.shorterURL.entity.UrlEntity;
import com.guibsantos.shorterURL.entity.UserEntity;
import com.guibsantos.shorterURL.repository.UrlRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UrlRepository urlRepository;
    private final ObjectMapper objectMapper;

    private static final int CACHE_TTL_MINUTES = 10;

    @Transactional
    public ShortenUrlResponse shortenUrl(ShortenUrlRequest request, HttpServletRequest servletRequest, UserEntity user) {
        String shortCode;

        do {
            shortCode = generateRandomCode();
        } while (urlRepository.findByShortCode(shortCode).isPresent());

        var entity = UrlEntity.builder()
                .originalUrl(request.url())
                .shortCode(shortCode)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .user(user)
                .build();

        urlRepository.save(entity);

        String cacheKey = "url:" + shortCode;
        redisTemplate.opsForValue().set(cacheKey, entity, CACHE_TTL_MINUTES, TimeUnit.MINUTES);

        var redirectUrl = servletRequest.getRequestURL().toString().replace("/api/shorten", "/" + shortCode);

        return new ShortenUrlResponse(entity.getOriginalUrl(), entity.getShortCode(), redirectUrl, entity.getExpiresAt());
    }

    public UrlEntity getOriginalUrl(String shortCode) {
        String cacheKey = "url:" + shortCode;
        UrlEntity urlEntity = null;

        Object cachedObject = redisTemplate.opsForValue().get(cacheKey);

        if (cachedObject != null) {
            try {

                urlEntity = objectMapper.convertValue(cachedObject, UrlEntity.class);
                log.info("Cache Hit! Recuperado do Redis: {}", shortCode);
            } catch (Exception e) {
                log.error("Erro ao converter cache: {}", e.getMessage());
            }
        }

        if (urlEntity == null) {
            log.warn("Cache MISS: Codigo {} nao encontrado no Redis. Buscando no banco...", shortCode);
            urlEntity = urlRepository.findByShortCode(shortCode)
                    .orElseThrow(() -> new UrlNotFoundException("Url não encontrada!"));
        }

        if(urlEntity.getExpiresAt() != null && urlEntity.getExpiresAt().isBefore(LocalDateTime.now())) {

            redisTemplate.delete(cacheKey);

            urlRepository.delete(urlEntity);
            log.warn("URL eexpirada tentada acessar: {}", shortCode);
            throw new UrlNotFoundException("Esta URL expirou e não está mais disponível.");
        }

        if (urlEntity.getAccessCount() == null) {
            urlEntity.setAccessCount(0L);
        }
        urlEntity.setAccessCount(urlEntity.getAccessCount() + 1);

        log.info("Atualizando contador para: {}", shortCode);
        redisTemplate.opsForValue().set(cacheKey, urlEntity, CACHE_TTL_MINUTES, TimeUnit.MINUTES);

        urlRepository.save(urlEntity);

        return urlEntity;
    }

    public UrlStatsResponse getUrlStats(String shortCode) {
        String cacheKey = "url:" + shortCode;
        UrlEntity entity = null;

        Object cachedObject = redisTemplate.opsForValue().get(cacheKey);

        if (cachedObject != null) {
            try {
                entity = objectMapper.convertValue(cachedObject, UrlEntity.class);
            } catch (Exception e) {
                log.error("Erro ao ler stats do cache", e);
            }
        }

        if (entity == null) {
            entity = urlRepository.findByShortCode(shortCode)
                    .orElseThrow(() -> new UrlNotFoundException("Url not found"));
        }

        return new UrlStatsResponse(entity.getOriginalUrl(), entity.getShortCode(), entity.getAccessCount());
    }

    @Transactional
    public void deleteUrl(String shortCode, UserEntity currentUser) {

        var url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Url não encontrada"));

        boolean isOwner =
                url.getUser() != null &&
                        url.getUser().getId().equals(currentUser.getId());

        boolean isAdmin =
                currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Você não tem permissão para deletar essa URL!");
        }

        urlRepository.delete(url);

        redisTemplate.delete("url:" + shortCode);
    }

    private String generateRandomCode() {
        return RandomStringUtils.randomAlphanumeric(6);
    }
}