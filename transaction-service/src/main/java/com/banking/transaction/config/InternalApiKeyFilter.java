package com.banking.transaction.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.core.Ordered;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter implements Ordered {

    private static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 100;

    @Override
    public int getOrder() {
        return ORDER;
    }

    private static final String HEADER = "X-Internal-Api-Key";
    private static final String ROLE_SERVICE = "ROLE_SERVICE";

    private final InternalApiProperties internalApi;

    public InternalApiKeyFilter(InternalApiProperties internalApi) {
        this.internalApi = internalApi;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String key = request.getHeader(HEADER);
        if (key != null && !key.isBlank() && key.equals(internalApi.getKey())) {
            var auth = new UsernamePasswordAuthenticationToken(
                    "INTERNAL_SERVICE",
                    null,
                    List.of(new SimpleGrantedAuthority(ROLE_SERVICE))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}
