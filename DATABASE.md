# Database setup (MongoDB)

Each service uses its **own MongoDB database** on your machine. Data persists across restarts.

## Prerequisites

- **MongoDB** running locally (e.g. default port **27017**).

To install or start MongoDB:
- **Windows:** Install from [mongodb.com](https://www.mongodb.com/try/download/community) or use Docker: `docker run -d -p 27017:27017 mongo`
- **macOS:** `brew services start mongodb-community`
- **Linux:** `sudo systemctl start mongod`

## Databases

| Service                | Database name          | Collections |
|------------------------|------------------------|-------------|
| **account-service**    | `banking_accounts`     | customers, accounts, bank_users, sequences |
| **transaction-service**| `banking_transactions`| transactions |
| **notification-service** | `banking_notifications` | notifications, audit_logs |

- **account-service** keeps numeric IDs (1, 2, …) for customers and accounts via a `sequences` collection so the REST API and UI (e.g. “Customer ID”) stay the same.
- **transaction-service** and **notification-service** use MongoDB ObjectIds (string) for document IDs.

## Configuration

In each service’s `src/main/resources/application.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017
      database: banking_<service>   # e.g. banking_accounts
```

To use a different host/port or auth:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://username:password@localhost:27017/banking_accounts?authSource=admin
```

## Inspecting data

- **MongoDB Compass** (GUI): connect to `mongodb://localhost:27017` and open the databases above.
- **mongosh** (shell): `mongosh` then `use banking_accounts` and `db.customers.find()` etc.

## Summary

- No H2 or JDBC; all three services use **Spring Data MongoDB**.
- Start **MongoDB** before starting the services so they can connect.
