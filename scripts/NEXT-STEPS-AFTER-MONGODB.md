# What to do after MongoDB is set up

You’ve created the databases and collections. Follow these steps to run the app.

---

## 1. Start MongoDB (if not already running)

- Make sure MongoDB is running on **localhost:27017**.
- In Compass, you should be able to connect to `mongodb://localhost:27017`.

---

## 2. Start the banking services

Start these **in any order** (or run them in separate terminals).

### Account service (required – UI + login + customers + accounts)

```powershell
cd C:\Users\vishw\banking-project
.\run-account-service.ps1
```

Or from the project root:

```powershell
mvn spring-boot:run -pl account-service
```

Wait until you see something like: **Started AccountServiceApplication** or **Tomcat started on port(s): 8081**.

---

### Transaction service (required – transfers, deposits, withdrawals)

```powershell
.\run-transaction-service.ps1
```

Or:

```powershell
mvn spring-boot:run -pl transaction-service
```

Wait until it starts on **port 8082**.

---

### Notification service (optional)

```powershell
.\run-notification-service.ps1
```

Or:

```powershell
mvn spring-boot:run -pl notification-service
```

Runs on **port 8083** if you use it.

---

## 3. Open the app in the browser

Go to:

**http://localhost:8081**

- You should see the **Banking Portal** login page.

---

## 4. Log in

- If you created the **admin** user in MongoDB: use **username:** `admin`, **password:** `admin123`.
- If **bank_users** was left empty: the account-service creates the admin on first start. Use **admin** / **admin123**.

---

## 5. Use the app (everything is stored in MongoDB)

- **Admin:** Customers tab → Create Customer (saved in `customers`). Accounts tab → Create Account (saved in `accounts`). Transfer / Deposit / Withdraw (saved in `transactions`).
- **Activate:** New users use “Activate now” (Customer ID + username + password). That creates a row in `bank_users` (password stored as hash) and they can log in.
- **Customer:** Log in → view accounts, transfer, deposit, withdraw, history. All of that reads/writes MongoDB.

You can confirm in **Compass**: open `banking_accounts` → `customers`, `accounts`, `bank_users` and `banking_transactions` → `transactions` and you’ll see new documents as you use the app.

---

## Quick checklist

| Step | Action |
|------|--------|
| 1 | MongoDB running on localhost:27017 |
| 2 | Start **account-service** (port 8081) |
| 3 | Start **transaction-service** (port 8082) |
| 4 | Open **http://localhost:8081** in the browser |
| 5 | Log in as **admin** / **admin123** (or activate a customer and log in as them) |

After that, everything you do in the app (logins, customers, accounts, transfers) is stored in your MongoDB databases.
