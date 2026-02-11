package com.banking.account.web;

import com.banking.account.config.InternalApiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Proxies transaction API calls from the UI to the transaction-service.
 * Sends internal API key so transaction-service accepts the call (bank-style service auth).
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionProxyController {

    private static final String INTERNAL_API_HEADER = "X-Internal-Api-Key";

    private final RestTemplate restTemplate;
    private final String transactionServiceUrl;
    private final String internalApiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TransactionProxyController(RestTemplate restTemplate,
                                     InternalApiProperties internalApi,
                                     @Value("${transaction-service.url:http://localhost:8082}") String transactionServiceUrl) {
        this.restTemplate = restTemplate;
        this.transactionServiceUrl = transactionServiceUrl.replaceAll("/$", "");
        this.internalApiKey = internalApi.getKey();
    }

    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.set(INTERNAL_API_HEADER, internalApiKey);
        return h;
    }

    /** Forward upstream error (4xx/5xx) so the UI can show e.g. "Insufficient balance". */
    private ResponseEntity<?> forwardError(RestClientResponseException e) {
        String body = e.getResponseBodyAsString();
        Map<String, Object> errorBody = null;
        if (body != null && !body.isBlank()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> parsed = objectMapper.readValue(body, Map.class);
                errorBody = parsed;
            } catch (Exception ignored) {
                errorBody = Map.of("message", body);
            }
        }
        if (errorBody == null) {
            errorBody = Map.of("message", e.getMessage() != null ? e.getMessage() : "Transaction service error");
        }
        return ResponseEntity.status(e.getStatusCode()).body(errorBody);
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody Map<String, Object> body) {
        String url = transactionServiceUrl + "/api/transactions/transfer";
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (RestClientResponseException e) {
            return forwardError(e);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("message", "Transfer service unavailable. Is transaction-service running on 8082? " + e.getMessage()));
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String reference) {
        String url = transactionServiceUrl + "/api/transactions/deposit"
                + "?accountNumber=" + URLEncoder.encode(accountNumber, StandardCharsets.UTF_8) + "&amount=" + amount
                + (reference != null && !reference.isBlank() ? "&reference=" + URLEncoder.encode(reference, StandardCharsets.UTF_8) : "");
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(authHeaders()), Map.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (RestClientResponseException e) {
            return forwardError(e);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("message", "Transaction service unavailable. " + e.getMessage()));
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String reference) {
        String url = transactionServiceUrl + "/api/transactions/withdraw"
                + "?accountNumber=" + URLEncoder.encode(accountNumber, StandardCharsets.UTF_8) + "&amount=" + amount
                + (reference != null && !reference.isBlank() ? "&reference=" + URLEncoder.encode(reference, StandardCharsets.UTF_8) : "");
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(authHeaders()), Map.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (RestClientResponseException e) {
            return forwardError(e);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("message", "Transaction service unavailable. " + e.getMessage()));
        }
    }

    @GetMapping("/history/{accountNumber}")
    public ResponseEntity<?> getHistory(@PathVariable String accountNumber) {
        String url = transactionServiceUrl + "/api/transactions/history/" + accountNumber;
        try {
            ResponseEntity<List<Map>> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(authHeaders()),
                    new ParameterizedTypeReference<List<Map>>() {});
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (RestClientResponseException e) {
            return forwardError(e);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("message", "Transaction service unavailable. Is transaction-service running on 8082?"));
        }
    }
}
