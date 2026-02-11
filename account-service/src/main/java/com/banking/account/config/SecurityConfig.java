package com.banking.account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final InternalApiKeyFilter internalApiKeyFilter;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, InternalApiKeyFilter internalApiKeyFilter,
                          RateLimitFilter rateLimitFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.internalApiKeyFilter = internalApiKeyFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/style.css", "/app.js", "/auth.js", "/favicon.ico").permitAll()
                        .requestMatchers("/ws/**", "/h2-console/**").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/activate", "/api/auth/register").permitAll()
                        // GET: user (JWT) or internal service; POST debit/credit: internal service only
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/accounts/number/*")).hasAnyRole("SERVICE", "ADMIN", "CUSTOMER")
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/accounts/number/*/debit")).hasRole("SERVICE")
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/accounts/number/*/credit")).hasRole("SERVICE")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .headers(h -> h
                        .frameOptions(f -> f.sameOrigin())
                        .xssProtection(x -> x.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .contentTypeOptions(c -> {})
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline';"))
                )
                // Add all custom filters before the standard one (no custom class as reference = no order lookup)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(internalApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
