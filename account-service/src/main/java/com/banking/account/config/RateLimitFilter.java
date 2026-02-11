package com.banking.account.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bank-style rate limiting: limits login/activate attempts per IP to reduce brute-force risk.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter implements Ordered {

    private static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 100;

    @Override
    public int getOrder() {
        return ORDER;
    }

    private final int maxAttempts;
    private final long windowSeconds;
    private final ConcurrentHashMap<String, Window> store = new ConcurrentHashMap<>();

    public RateLimitFilter(
            @Value("${app.rate-limit.auth.max-attempts:5}") int maxAttempts,
            @Value("${app.rate-limit.auth.window-seconds:60}") long windowSeconds) {
        this.maxAttempts = maxAttempts;
        this.windowSeconds = windowSeconds;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.equals("/api/auth/login") && !path.equals("/api/auth/activate");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String key = clientKey(request);
        Window w = store.compute(key, (k, old) -> {
            long now = System.currentTimeMillis();
            if (old == null || now - old.start > windowSeconds * 1000) {
                return new Window(now, new AtomicInteger(0));
            }
            return old;
        });
        if (w.count.incrementAndGet() > maxAttempts) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Too many attempts. Try again later.\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String clientKey(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return "auth:" + xff.split(",")[0].trim();
        }
        return "auth:" + request.getRemoteAddr();
    }

    private static class Window {
        final long start;
        final AtomicInteger count;
        Window(long start, AtomicInteger count) {
            this.start = start;
            this.count = count;
        }
    }
}
