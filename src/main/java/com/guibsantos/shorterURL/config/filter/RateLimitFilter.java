package com.guibsantos.shorterURL.config.filter;

import com.guibsantos.shorterURL.service.RateLimitService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {

    private final RateLimitService rateLimitService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)  request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        if(path.startsWith("/swagger") || path.startsWith("v3/api-docs") || path.startsWith("/css")) {
            chain.doFilter(request, response);
            return;
        }

        String ip = getClientIP(httpRequest);

        boolean isAuthRoute = path.startsWith("/auth") || path.contains("/login") || path.contains("/register");

        Bucket bucket = rateLimitService.resolveBucket(ip, isAuthRoute);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            httpResponse.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            chain.doFilter(request, response);
        } else {

            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");

            long waitForRefil = probe.getNanosToWaitForRefill() / 1_000_000_000;

            String jsonError = String.format(
                    "{\"error\": \"Muitas tentativas. Aguarde %d segundos. \"}",
                    waitForRefil
            );

            httpResponse.getWriter().write(jsonError);
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
