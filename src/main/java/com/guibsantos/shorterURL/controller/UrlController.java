package com.guibsantos.shorterURL.controller;

import com.guibsantos.shorterURL.controller.docs.UrlControllerDocs;
import com.guibsantos.shorterURL.controller.dto.request.ShortenUrlRequest;
import com.guibsantos.shorterURL.controller.dto.response.ShortenUrlResponse;
import com.guibsantos.shorterURL.controller.dto.response.UrlStatsResponse;
import com.guibsantos.shorterURL.entity.UserEntity;
import com.guibsantos.shorterURL.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class UrlController implements UrlControllerDocs {

    private final UrlService urlService;

    @Override
    @GetMapping("/api/my-urls")
    public ResponseEntity<List<ShortenUrlResponse>> getUserUrls() {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserEntity)) {
            return ResponseEntity.status(403).build();
        }
        UserEntity user = (UserEntity) authentication.getPrincipal();

        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        var urls = urlService.getUserUrls(user);
        var response = urls.stream()
                .map(url -> new ShortenUrlResponse(
                        url.getOriginalUrl(),
                        url.getShortCode(),
                        baseUrl + "/" + url.getShortCode(),
                        url.getExpiresAt()
                ))
                .toList();
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/api/stats/{shortCode}")
    public ResponseEntity<UrlStatsResponse> getUrlStats(@PathVariable String shortCode) {
        var stats = urlService.getUrlStats(shortCode);
        return ResponseEntity.ok(stats);
    }

    @Override
    @PostMapping("/api/shorten")
    public ResponseEntity<ShortenUrlResponse> shortenUrl(@RequestBody @Valid ShortenUrlRequest request,
                                                         HttpServletRequest servletRequest) {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var user = (UserEntity) authentication.getPrincipal();

        var response = urlService.shortenUrl(request, servletRequest, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping("/{shortCode:[a-zA-Z0-9]{6}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        var url = urlService.getOriginalUrl(shortCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(url.getOriginalUrl()));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @Override
    @DeleteMapping("api/urls/{shortCode}")
    public ResponseEntity<Void> deleteUrl(@PathVariable String shortCode) {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var user = (UserEntity) authentication.getPrincipal();

        urlService.deleteUrl(shortCode, user);
        return ResponseEntity.noContent().build();
    }
}