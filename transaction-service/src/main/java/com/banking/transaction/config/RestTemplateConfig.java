package com.banking.transaction.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class RestTemplateConfig {

    private static final String INTERNAL_API_HEADER = "X-Internal-Api-Key";

    @Bean
    public RestTemplate restTemplate(
            InternalApiProperties internalApi,
            @Value("${account-service.url:http://localhost:8081}") String accountServiceUrl) {
        RestTemplate rest = new RestTemplate(new SimpleClientHttpRequestFactory());
        rest.setInterceptors(List.of((ClientHttpRequestInterceptor) (request, body, execution) -> {
            if (request.getURI().toString().startsWith(accountServiceUrl)) {
                request.getHeaders().set(INTERNAL_API_HEADER, internalApi.getKey());
            }
            return execution.execute(request, body);
        }));
        return rest;
    }
}
