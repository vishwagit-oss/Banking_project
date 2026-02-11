# Banking Microservices

A full-stack **banking-style application** built with Java microservices: **REST APIs**, **SOAP Web Services**, JWT authentication, and a single-page web UI. Each customer can have at most one **Savings** and one **Current** account; transfers, deposits, and withdrawals are supported with audit logging and security measures inspired by real banks.

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [How to Run](#how-to-run)
- [Web UI & Login](#web-ui--login)
- [REST API](#rest-api)
- [SOAP Web Services](#soap-web-services)
- [Security](#security)
- [Database](#database)
- [Business Rules](#business-rules)
- [Documentation](#documentation)
- [Pushing to GitHub](#pushing-to-github)

---

## Features

- **Three microservices**: Account (customers, accounts, auth), Transaction (transfers, deposit, withdraw), Notification (audit log, notifications).
- **REST + SOAP**: REST for the UI and service-to-service calls; SOAP for legacy/integration (validate account, get account details, inquire/execute transfer).
- **JWT authentication**: Login and “Activate now” (first-time customer setup). Roles: **Admin** (full access) and **Customer** (own accounts only).
- **Bank-style security**: BCrypt passwords, rate limiting on login/activate, account lockout after failed attempts, internal API key for service-to-service, security headers, audit logging.
- **One account per type per customer**: Each customer can have at most one **SAVINGS** and one **CURRENT** account.
- **Single-page web app**: Served by account-service; proxy to transaction-service so the browser uses one origin.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│  Browser (http://localhost:8081)                                         │
│  • Login / Activate  • My accounts  • Transfer / Deposit / Withdraw       │
│  • JWT in Authorization header                                           │
└───────────────────────────────┬─────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  account-service (8081)                                                  │
│  • REST: /api/auth, /api/customers, /api/accounts, /api/transactions/*   │
│  • SOAP: /ws  (ValidateAccount, GetAccountDetails)                        │
│  • Serves: index.html, app.js, auth.js, style.css                        │
│  • Proxies UI → transaction-service (with X-Internal-Api-Key)            │
│  • Calls notification-service for audit (login, debit/credit)             │
└───────┬─────────────────────────────────────────┬──────────────────────┘
        │                                           │
        │ X-Internal-Api-Key                        │ X-Internal-Api-Key
        ▼                                           ▼
┌───────────────────────┐               ┌─────────────────────────────────┐
│ transaction-service   │               │ notification-service (8083)      │
│ (8082)                │               │ • REST: /api/audit, /api/notif  │
│ • REST: transfer,     │               │ • Stores: audit_logs, notifs    │
│   deposit, withdraw,  │               └─────────────────────────────────┘
│   history             │
│ • SOAP: InquireTransfer│
│   ExecuteTransfer     │
│ • Calls account-service│
│   (validate, debit,   │
│   credit)             │
└───────────┬───────────┘
            │
            ▼
┌───────────────────────┐
│ account-service       │
│ (debit/credit/validate)│
└───────────────────────┘

All services use MongoDB (separate DBs).
```

| Service               | Port | Description |
|-----------------------|------|-------------|
| **account-service**   | 8081 | Customers, accounts, auth (JWT), web UI, SOAP validate/get, proxy to transaction-service, audit client to notification-service. |
| **transaction-service** | 8082 | Transfers, deposit, withdraw, history; SOAP transfer; calls account-service for validate/debit/credit. |
| **notification-service** | 8083 | Audit log and notifications; called by account-service (and optionally others) with internal API key. |

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.2 |
| **REST** | Spring Web (Jackson JSON) |
| **SOAP** | Spring Web Services, JAXB, WSDL/XSD |
| **Data** | Spring Data MongoDB (no JPA/H2) |
| **Security** | Spring Security, JWT (jjwt), BCrypt, custom filters (rate limit, internal API key) |
| **Build** | Maven 3.x (wrapper: `mvnw.cmd`) |
| **Frontend** | Vanilla JS, HTML, CSS (no framework); served as static from account-service |
| **Database** | MongoDB (one DB per service) |

---

## Project Structure

```
banking-project/
├── pom.xml                          # Parent POM (Spring Boot 3.2, Java 17)
├── mvnw.cmd                         # Maven Wrapper (Windows)
├── .mvn/wrapper/                    # Maven Wrapper config
├── README.md                        # This file
├── SECURITY.md                     # Security practices & env vars
├── DATABASE.md                     # MongoDB setup
├── LOGIN-AND-ROLES.md              # Login, activate, admin/customer
├── HOW-TO-OPEN-WEBSITE.md
├── TROUBLESHOOTING-HTTP.md
├── run-account-service.ps1        # Start account-service
├── run-transaction-service.ps1
├── run-notification-service.ps1
├── scripts/                        # MongoDB seed, indexes, docs
│   ├── mongodb-seed.js
│   ├── mongodb-fix-indexes.js
│   └── *.md
│
├── account-service/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/banking/account/
│       │   ├── AccountServiceApplication.java
│       │   ├── client/             # AuditClient → notification-service
│       │   ├── config/             # Security, JWT, CORS, Mongo, REST/SOAP
│       │   ├── dto/                # Request/response DTOs
│       │   ├── entity/             # Account, Customer, BankUser
│       │   ├── repository/         # Mongo repositories
│       │   ├── service/            # AccountService, AuthService, CustomerService
│       │   ├── soap/               # SOAP endpoint & payloads
│       │   └── web/                # REST controllers, exception handler
│       └── resources/
│           ├── application.yml
│           ├── account.xsd
│           └── static/             # index.html, app.js, auth.js, style.css
│
├── transaction-service/
│   ├── pom.xml
│   └── src/main/java/com/banking/transaction/
│       ├── TransactionServiceApplication.java
│       ├── client/                 # AccountServiceClient (validate, debit, credit)
│       ├── config/
│       ├── dto/
│       ├── entity/                 # Transaction
│       ├── repository/
│       ├── service/                # TransactionService
│       ├── soap/
│       └── web/
│
└── notification-service/
    ├── pom.xml
    └── src/main/java/com/banking/notification/
        ├── NotificationServiceApplication.java
        ├── config/
        ├── entity/                 # AuditLog, Notification
        ├── repository/
        ├── service/                # AuditService, NotificationService
        └── web/
```

---

## Prerequisites

- **Java 17** (e.g. [Eclipse Adoptium](https://adoptium.net/))
- **Maven 3.8+** (optional; project includes Maven Wrapper)
- **MongoDB** running locally on **port 27017**

Install/start MongoDB:

- **Windows:** [MongoDB Community](https://www.mongodb.com/try/download/community) or `docker run -d -p 27017:27017 mongo`
- **macOS:** `brew services start mongodb-community`
- **Linux:** `sudo systemctl start mongod`

---

## How to Run

From the project root (`banking-project`).

**1. Start MongoDB** (if not already running).

**2. Start Account Service** (must be first; serves UI and auth):

```powershell
.\run-account-service.ps1
```

Or:

```powershell
.\mvnw.cmd spring-boot:run -pl account-service
```

**3. Start Transaction Service** (new terminal):

```powershell
.\run-transaction-service.ps1
```

**4. (Optional) Start Notification Service** (for audit log):

```powershell
.\run-notification-service.ps1
```

**5. Open the app:** [http://localhost:8081/](http://localhost:8081/)

Transfer, deposit, withdraw, and history require **both** account-service and transaction-service to be running.

---

## Web UI & Login

- **Sign In**: Username + password (for already activated customers and admin).
- **Activate now**: First-time setup: Customer ID (from bank) + choose username and password.

**Default admin (testing):**

- Username: `admin`  
- Password: `admin123`  

Created on first run. Use Admin to create customers and accounts.

**Roles:**

- **Admin**: Create customers/accounts, list customers, find any customer’s accounts, lookup any account, transfer/deposit/history.
- **Customer**: See only own accounts; transfer, deposit, withdraw, history for own accounts.

See [LOGIN-AND-ROLES.md](LOGIN-AND-ROLES.md) for the full flow.

---

## REST API

Base URLs: account `http://localhost:8081`, transaction `http://localhost:8082`, notification `http://localhost:8083`.  
User endpoints require `Authorization: Bearer <JWT>` except login/activate.  
Transaction and notification service APIs require `X-Internal-Api-Key` (used by account-service proxy and internal calls).

### Account Service (8081)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/login` | Login (body: `username`, `password`) |
| POST | `/api/auth/activate` | First-time activate (body: `customerId`, `username`, `password`) |
| GET  | `/api/auth/me` | Current user (JWT required) |
| GET  | `/api/customers` | List customers (Admin) |
| POST | `/api/customers` | Create customer (Admin) |
| GET  | `/api/customers/{id}/accounts` | Accounts for customer (Admin or own customerId) |
| GET  | `/api/accounts/{id}` | Account by ID (JWT, ownership check for Customer) |
| GET  | `/api/accounts/number/{number}` | Account by number (JWT or internal service) |
| POST | `/api/accounts` | Create account (Admin; body: `customerId`, `type`, `initialBalance`, `currency`) |
| POST | `/api/transactions/transfer` | Proxy to transaction-service |
| POST | `/api/transactions/deposit` | Proxy |
| POST | `/api/transactions/withdraw` | Proxy |
| GET  | `/api/transactions/history/{accountNumber}` | Proxy |

Internal (service-to-service, `X-Internal-Api-Key`):

- GET `/api/accounts/number/{number}` → 200 if account exists and ACTIVE (no body).
- POST `/api/accounts/number/{number}/debit?amount=...`
- POST `/api/accounts/number/{number}/credit?amount=...`

### Transaction Service (8082)

All under `/api/transactions`; require internal API key when called directly (UI goes via account-service proxy).

- POST `/transfer` — body: `fromAccountNumber`, `toAccountNumber`, `amount`, `reference`
- POST `/deposit?accountNumber=...&amount=...&reference=...`
- POST `/withdraw?accountNumber=...&amount=...&reference=...`
- GET `/history/{accountNumber}`
- GET `/statement/{accountNumber}?from=...&to=...`

### Notification Service (8083)

- POST `/api/audit` — body: `eventType`, `entityType`, `entityId`, `accountNumber`, `details`
- GET `/api/audit/account/{accountNumber}`

---

## SOAP Web Services

- **Account Service**  
  - WSDL: `http://localhost:8081/ws/accounts.wsdl`  
  - Operations: ValidateAccount, GetAccountDetails  

- **Transaction Service**  
  - WSDL: `http://localhost:8082/ws/transactions.wsdl`  
  - Operations: InquireTransfer, ExecuteTransfer  

Use SoapUI, Postman, or any SOAP client against the `/ws` path.

---

## Security

- **Passwords**: BCrypt only; no plaintext.
- **Auth**: JWT (HMAC-SHA) for browser; stateless.
- **Service-to-service**: Shared secret in header `X-Internal-Api-Key` (same value in all three services).
- **Rate limiting**: Login and activate limited (e.g. 5 attempts per IP per 60 seconds).
- **Lockout**: After 5 failed logins, account locked for 15 minutes.
- **Headers**: X-XSS-Protection, X-Content-Type-Options, Content-Security-Policy (account-service).
- **Audit**: Login success/fail, activation, debit/credit sent to notification-service.

See [SECURITY.md](SECURITY.md) for environment variables (e.g. `JWT_SECRET`, `INTERNAL_API_KEY`, `MONGO_URI`) and production checklist.

---

## Database

Each service uses its **own MongoDB database**:

| Service                | Database              | Main collections        |
|------------------------|-----------------------|--------------------------|
| account-service        | `banking_accounts`    | customers, accounts, bank_users, sequences |
| transaction-service    | `banking_transactions`| transactions             |
| notification-service   | `banking_notifications`| audit_logs, notifications |

See [DATABASE.md](DATABASE.md) and `scripts/` for setup, seed, and indexes.

---

## Business Rules

- **One account per type per customer**: A customer can have at most one **SAVINGS** and one **CURRENT** account. Creating a second account of the same type returns an error.
- **Ownership**: Customers see and operate only on their own accounts; Admin can manage all.

---

## Documentation

| File | Description |
|------|-------------|
| [README.md](README.md) | This file – overview, stack, run, API |
| [SECURITY.md](SECURITY.md) | Security features and production env vars |
| [DATABASE.md](DATABASE.md) | MongoDB setup and config |
| [LOGIN-AND-ROLES.md](LOGIN-AND-ROLES.md) | Login, activate, Admin vs Customer |
| [HOW-TO-OPEN-WEBSITE.md](HOW-TO-OPEN-WEBSITE.md) | Opening the UI |
| [TROUBLESHOOTING-HTTP.md](TROUBLESHOOTING-HTTP.md) | Common issues |
| `scripts/` | MongoDB seed script and docs |

---

## Pushing to GitHub

If you see **Permission denied (publickey)** when using SSH, use **HTTPS** and a **Personal Access Token** instead:

```powershell
git remote set-url origin https://github.com/vishwagit-oss/Banking-project.git
git push -u origin main
```

When prompted for password, use a [GitHub Personal Access Token](https://github.com/settings/tokens) (with `repo` scope), not your GitHub account password.

---

## License

This project is for learning and portfolio use. Adjust licensing as needed for your repository.
