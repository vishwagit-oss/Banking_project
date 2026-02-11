# Banking Portal

A full-stack banking-style app built with **Java microservices**: REST APIs, JWT auth, and a single-page web UI. Customers can have at most one **Savings** and one **Current** account; the app supports transfers, deposits, withdrawals, and audit-style logging.

---

## What This Project Is

- **Three microservices**: **account-service** (customers, accounts, auth, web UI), **transaction-service** (transfers, deposit, withdraw, history), **notification-service** (audit log).
- **Web UI** is served by account-service; it talks to transaction-service via the account-service proxy so the browser uses one origin.
- **Roles**: **Admin** (manage customers and accounts) and **Customer** (view own accounts, transfer, deposit, withdraw).
- **Default admin**: username `admin`, password `admin123` (for local/testing).

---

## Tech Stack

| Layer        | Technology |
|-------------|------------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.x |
| **API** | Spring Web (REST, JSON) |
| **Data** | Spring Data MongoDB |
| **Security** | Spring Security, JWT (jjwt), BCrypt, rate limiting, internal API key for service-to-service |
| **Build** | Maven (wrapper: `mvnw.cmd`) |
| **Frontend** | Vanilla JS, HTML, CSS (static files served by account-service) |
| **Database** | MongoDB (one DB per service) |

---

## Run Locally

### Prerequisites

- **Java 17** (e.g. [Eclipse Temurin](https://adoptium.net/))
- **MongoDB** on **port 27017**

  - **Windows:** [MongoDB Community](https://www.mongodb.com/try/download/community) or:  
    `docker run -d -p 27017:27017 mongo`
  - **macOS:** `brew services start mongodb-community`
  - **Linux:** `sudo systemctl start mongod`

### Start the services

From the project root:

1. **Account service** (UI + auth; run first):

   ```powershell
   .\run-account-service.ps1
   ```

   Or: `.\mvnw.cmd spring-boot:run -pl account-service`

2. **Transaction service** (new terminal):

   ```powershell
   .\run-transaction-service.ps1
   ```

3. **Notification service** (optional; for audit log):

   ```powershell
   .\run-notification-service.ps1
   ```

4. Open in browser: **http://localhost:8081/**

Transfer, deposit, withdraw, and history need **account-service** and **transaction-service** running. Notification-service is optional for full audit logging.

---

## Host on Railway

You can deploy the **account-service** (and optionally the others) using [Railway](https://railway.app/).

### Deploy account-service (Docker)

The repo includes a **Dockerfile** that builds and runs **account-service** only.

1. Push this repo to **GitHub** (or connect your repo to Railway).
2. In **Railway**, create a new project and choose **Deploy from GitHub repo**; select this repo.
3. Railway will detect the **Dockerfile** and build the image, then run the container.
4. Set **environment variables** in Railway (Variables tab):
   - `PORT` – Railway sets this; the app reads it for `server.port`.
   - `MONGO_URI` – Your MongoDB connection string (e.g. [MongoDB Atlas](https://www.mongodb.com/cloud/atlas) free tier).
   - `JWT_SECRET` – A long random string (e.g. 32+ chars) for signing JWTs.
   - `INTERNAL_API_KEY` – Shared key for service-to-service calls (if you add transaction/notification later).

5. After deploy, Railway gives you a public URL (e.g. `https://your-app.up.railway.app`). Open it to use the Banking Portal.

### Full setup (all three services)

To run **transaction-service** and **notification-service** on Railway as well:

- Deploy each as a separate service (each with its own Dockerfile or build config, or use the same repo and set different start commands / build targets).
- Give each service its own **MONGO_URI** (or same cluster, different databases).
- Set **TRANSACTION_SERVICE_URL** and **NOTIFICATION_SERVICE_URL** in account-service to the public URLs of those services.
- Use the same **INTERNAL_API_KEY** on all services.

The included Dockerfile is set up for **account-service** only; it’s the one that serves the UI and is the main entry point for users.
