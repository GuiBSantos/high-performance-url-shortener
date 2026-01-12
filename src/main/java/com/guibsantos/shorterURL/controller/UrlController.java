package com.guibsantos.shorterURL.controller;

import com.guibsantos.shorterURL.controller.docs.UrlControllerDocs;
import com.guibsantos.shorterURL.controller.dto.request.ShortenUrlRequest;
import com.guibsantos.shorterURL.controller.dto.response.ShortenUrlResponse;
import com.guibsantos.shorterURL.controller.dto.response.UrlStatsResponse;
import com.guibsantos.shorterURL.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UrlController implements UrlControllerDocs {

    private final UrlService urlService;

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
        var response = urlService.shortenUrl(request, servletRequest);
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
}