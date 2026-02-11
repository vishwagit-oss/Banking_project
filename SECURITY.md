# Security – Banking-style practices

This document describes the security measures implemented (aligned with real-bank practices) and how to configure them for development and production.

## What is implemented

### 1. **Secrets from environment**
- **JWT secret**: Set `JWT_SECRET` (min 32 characters / 256 bits). Never commit production secrets.
- **Internal API key**: Set `INTERNAL_API_KEY` – same value for account-service, transaction-service, and notification-service for service-to-service calls.
- **MongoDB**: Set `MONGO_URI` in production with TLS and authentication (e.g. `mongodb+srv://...` or `mongodb://...?tls=true`).

### 2. **HTTPS (encryption in transit)**
- **Development**: Services use HTTP (e.g. `http://localhost:8081`).
- **Production**: Enable TLS on the reverse proxy (e.g. nginx, cloud load balancer) or in Spring Boot:
  ```yaml
  server:
    ssl:
      key-store: classpath:keystore.p12
      key-store-password: ${SSL_KEYSTORE_PASSWORD}
      key-store-type: PKCS12
  ```
  Set `TRANSACTION_SERVICE_URL`, `ACCOUNT_SERVICE_URL`, `NOTIFICATION_SERVICE_URL` to `https://...` where applicable.

### 3. **Password storage**
- Passwords are hashed with **BCrypt** (no plaintext). Used for login and activation.

### 4. **Authentication and authorization**
- **User-facing**: JWT (HMAC-SHA) for browser clients. Token in `Authorization: Bearer <token>`.
- **Service-to-service**: Internal APIs (debit/credit/validate, transaction API, audit API) require header **`X-Internal-Api-Key`** with the shared secret. Only the account-service proxy and transaction-service call each other with this key; the browser never sees it.

### 5. **Rate limiting**
- **Login and activation**: Limited to 5 attempts per IP per 60 seconds (configurable via `app.rate-limit.auth.max-attempts` and `app.rate-limit.auth.window-seconds`). Reduces brute-force risk.

### 6. **Account lockout**
- After **5 failed login attempts**, the account is locked for **15 minutes** (`lockedUntil`). Prevents sustained guessing.

### 7. **Security headers**
- **X-XSS-Protection**: Enabled with block mode.
- **X-Content-Type-Options**: nosniff.
- **Content-Security-Policy**: Restricts scripts and styles to same origin where applied (account-service).

### 8. **Audit logging**
- Events sent to notification-service (with internal API key):
  - **LOGIN_SUCCESS** / **LOGIN_FAILED**
  - **ACTIVATION_SUCCESS**
  - **DEBIT** / **CREDIT** (account number and amount in details)
- Failures to write audit do not block the main operation.

### 9. **Access control**
- Customers see only their own accounts. Admins can create accounts/customers. Debit/credit endpoints are **internal only** (require `ROLE_SERVICE` via API key).

---

## Production checklist

- [ ] Set **JWT_SECRET** (min 32 chars) and do not use the default.
- [ ] Set **INTERNAL_API_KEY** to a strong shared secret; same value on all three services.
- [ ] Use **HTTPS** for all user and service-to-service traffic (proxy or Spring SSL).
- [ ] Set **MONGO_URI** with TLS and auth; restrict network access to DB.
- [ ] Restrict **CORS** to your real front-end origins (not `*`).
- [ ] Run services in a private network; only the gateway/load balancer should be public.
- [ ] Keep dependencies updated (e.g. `mvn versions:display-dependency-updates`).
- [ ] Consider **HSTS** when using HTTPS (e.g. in reverse proxy).
- [ ] Do not log passwords or tokens; ensure audit details do not expose secrets.

---

## Environment variables summary

| Variable | Used by | Purpose |
|----------|---------|---------|
| `JWT_SECRET` | account-service | Signing JWTs (min 32 chars) |
| `JWT_EXPIRATION_MS` | account-service | Token lifetime (default 24h) |
| `INTERNAL_API_KEY` | all three services | Service-to-service auth |
| `MONGO_URI` | all three services | MongoDB connection (use TLS in prod) |
| `TRANSACTION_SERVICE_URL` | account-service | URL of transaction-service (https in prod) |
| `NOTIFICATION_SERVICE_URL` | account-service | URL of notification-service |
| `ACCOUNT_SERVICE_URL` | transaction-service | URL of account-service |
| `SSL_KEYSTORE_PASSWORD` | optional | If using server.ssl in Spring Boot |

---

## Defaults (development only)

- JWT secret and internal API key have **defaults** in `application.yml` so the app runs locally without env vars. **Do not use these defaults in production.**
