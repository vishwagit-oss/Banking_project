package com.banking.transaction.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
public class AccountServiceClient {

    private final RestTemplate restTemplate;
    private final String accountServiceUrl;

    public AccountServiceClient(RestTemplate restTemplate,
                                @Value("${account-service.url:http://localhost:8081}") String accountServiceUrl) {
        this.restTemplate = restTemplate;
        this.accountServiceUrl = accountServiceUrl;
    }

    public boolean validateAccount(String accountNumber) {
        try {
            ResponseEntity<?> r = restTemplate.getForEntity(
                    accountServiceUrl + "/api/accounts/number/" + accountNumber, Object.class);
            return r.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    public void debit(String accountNumber, BigDecimal amount) {
        String url = accountServiceUrl + "/api/accounts/number/" + accountNumber + "/debit?amount=" + amount;
        restTemplate.postForObject(url, null, Void.class);
    }

    public void credit(String accountNumber, BigDecimal amount) {
        String url = accountServiceUrl + "/api/accounts/number/" + accountNumber + "/credit?amount=" + amount;
        restTemplate.postForObject(url, null, Void.class);
    }
}
