package com.banking.transaction.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Returns JSON error messages so the UI (and proxy) can show the real reason
 * for transfer/deposit/withdraw failures (e.g. "Insufficient balance", "Invalid account").
 * Also handles failures from account-service (debit/credit) so the message is forwarded.
 */
@RestControllerAdvice
public class TransactionExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + (err.getDefaultMessage() != null ? err.getDefaultMessage() : "invalid"))
                .collect(Collectors.joining("; "));
        if (message.isBlank()) message = "Validation failed. Check from account, to account, and amount (min 0.01).";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Invalid request"));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Invalid state"));
    }

    /** When account-service returns an error (e.g. 500 Insufficient balance), return same status with clear message. */
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Map<String, String>> handleRestClientResponse(RestClientResponseException e) {
        String message = "Transaction failed";
        String body = e.getResponseBodyAsString();
        if (body != null && !body.isBlank()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> parsed = objectMapper.readValue(body, Map.class);
                Object msg = parsed.get("message");
                if (msg != null) message = msg.toString();
                else if (parsed.get("error") != null) message = parsed.get("error").toString();
            } catch (Exception ignored) {
                if (body.length() < 200) message = body;
            }
        }
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", message));
    }
}
