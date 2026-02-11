# Banking Microservices

Java banking project showcasing **REST APIs**, **SOAP Web Services**, and **microservices** with Spring Boot, Spring Data JPA, and Spring Security.

## Architecture

| Service               | Port | Description                    |
|-----------------------|------|--------------------------------|
| **account-service**  | 8081 | Customers, accounts, balance; REST + SOAP |
| **transaction-service** | 8082 | Transfers, deposit, withdraw; REST + SOAP |
| **notification-service** | 8083 | Audit log, notifications; REST only |

- **Account Service** exposes SOAP: `ValidateAccount`, `GetAccountDetails`.
- **Transaction Service** exposes SOAP: `InquireTransfer`, `ExecuteTransfer`.
- **Transaction Service** calls **Account Service** (REST) for debit/credit.

## Prerequisites

- **Java 17+**
- **Maven 3.8+**

## How to Run

You can run **without installing Maven** by using the included Maven Wrapper (`mvnw.cmd`).  
From the **banking-project** folder:

**1. Start Account Service** (must be first):

```powershell
cd C:\Users\vishw\banking-project
.\mvnw.cmd spring-boot:run -pl account-service
```

Or double‑click **run-account-service.cmd**.

**2. Start Transaction Service** (in another terminal):

```powershell
cd C:\Users\vishw\banking-project
.\mvnw.cmd spring-boot:run -pl transaction-service
```

Or run **run-transaction-service.cmd**.

**3. Start Notification Service** (optional):

```powershell
.\mvnw.cmd spring-boot:run -pl notification-service
```

Or **run-notification-service.cmd**.

**If you have Maven installed**, you can instead run from each module:

```powershell
cd account-service
mvn spring-boot:run
```

## Web frontend

With **Account Service** running, open in your browser:

**http://localhost:8081/**

Use the tabs to:
- **Customers** – create and list customers
- **Accounts** – create accounts (select customer) and lookup by account number
- **Transfer** – transfer between accounts, or deposit/withdraw
- **History** – view transaction history for an account

**Transaction Service** must also be running (port 8082) for Transfer, Deposit, Withdraw, and History to work.

---

## REST API Examples

### Account Service (port 8081)

- **Create customer:**  
  `POST http://localhost:8081/api/customers`  
  Body: `{"name":"John Doe","email":"john@example.com","phone":"+1234567890"}`

- **Create account:**  
  `POST http://localhost:8081/api/accounts`  
  Body: `{"customerId":1,"type":"SAVINGS","initialBalance":1000,"currency":"USD"}`

- **Get account by number:**  
  `GET http://localhost:8081/api/accounts/number/{accountNumber}`

### Transaction Service (port 8082)

- **Transfer:**  
  `POST http://localhost:8082/api/transactions/transfer`  
  Body: `{"fromAccountNumber":"ACC...","toAccountNumber":"ACC...","amount":50,"reference":"Payment"}`

- **Deposit:**  
  `POST http://localhost:8082/api/transactions/deposit?accountNumber=ACC...&amount=100`

- **Withdraw:**  
  `POST http://localhost:8082/api/transactions/withdraw?accountNumber=ACC...&amount=25`

- **History:**  
  `GET http://localhost:8082/api/transactions/history/{accountNumber}`

### Notification Service (port 8083)

- **Record audit:**  
  `POST http://localhost:8083/api/audit`  
  Body: `{"eventType":"TRANSFER","entityType":"Transaction","accountNumber":"ACC...","details":"..."}`

- **Get audit by account:**  
  `GET http://localhost:8083/api/audit/account/{accountNumber}`

## SOAP Endpoints

### Account Service

- **WSDL:** `http://localhost:8081/ws/accounts.wsdl`
- **ValidateAccountRequest** / **ValidateAccountResponse**
- **GetAccountDetailsRequest** / **GetAccountDetailsResponse**

Use a SOAP client (e.g. SoapUI, Postman) or `curl` with XML payload against the `/ws` path.

### Transaction Service

- **WSDL:** `http://localhost:8082/ws/transactions.wsdl`
- **InquireTransferRequest** / **InquireTransferResponse**
- **ExecuteTransferRequest** / **ExecuteTransferResponse**

## Tech Stack

- Java 17
- Spring Boot 3.2
- Spring Web (REST)
- Spring Data JPA
- Spring Security
- Spring Web Services (SOAP)
- H2 (in-memory DB)
- Lombok, Maven

## Project Structure

```
banking-project/
├── pom.xml                 # Parent POM
├── account-service/        # Accounts, customers, SOAP validate/get
├── transaction-service/   # Transfers, deposit, withdraw, SOAP transfer
└── notification-service/  # Audit log, notifications
```
