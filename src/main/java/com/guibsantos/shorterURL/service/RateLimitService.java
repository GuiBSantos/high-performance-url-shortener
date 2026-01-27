package com.guibsantos.shorterURL.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String ip, boolean isLoginEndpoint) {
        String key = ip + (isLoginEndpoint ? "_LOGIN" : "_GENERAL");

        return cache.computeIfAbsent(key, k -> newBucket(isLoginEndpoint));
    }

    private Bucket newBucket(boolean isLoginEndpoint) {
        Bandwidth limit;

        if(isLoginEndpoint) {
            limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        } else {
            limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
        }

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
