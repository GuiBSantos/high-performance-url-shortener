package com.guibsantos.shorterURL.job;

import com.guibsantos.shorterURL.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupJob {

    private final UrlRepository urlRepository;

    @Scheduled(cron = "0 * * * * *")
    public void cleanup() {
        log.info("Iniciando varredura de URLs expiradas...");

        int deletedCount = urlRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        if(deletedCount > 0) {
            log.info("{} URLs foram deletadas do banco", deletedCount);
        } else {
            log.info("Nenhuma URL expirada encontrada.");
        }
    }
}
