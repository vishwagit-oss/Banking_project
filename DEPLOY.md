# How to Deploy This Banking App

Your app is a **Java Spring Boot** backend. Use **Render** (works with the Dockerfile in this repo) and **MongoDB Atlas** (free cloud database).

---

## Step 1: Create a free MongoDB database (MongoDB Atlas)

1. Go to **[mongodb.com/atlas](https://www.mongodb.com/atlas)** and sign up / log in.
2. Create a **free cluster** (e.g. M0).
3. Click **Connect** → **Drivers** → copy the connection string. It looks like:
   ```text
   mongodb+srv://USERNAME:PASSWORD@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority
   ```
4. Replace `<password>` with your database user password (or create a DB user in Atlas first).
5. For this app, you can use one cluster and one database name: `banking_accounts`. Example:
   ```text
   mongodb+srv://USERNAME:PASSWORD@cluster0.xxxxx.mongodb.net/banking_accounts?retryWrites=true&w=majority
   ```
   Save this as your **MONGO_URI** (you’ll paste it into Render).

---

## Step 2: Deploy on Render

1. Go to **[render.com](https://render.com)** and sign up / log in (GitHub is easiest).
2. Click **New +** → **Web Service**.
3. Connect your GitHub account if needed, then select the repo: **vishwagit-oss/Banking_project**.
4. Use these settings:

   | Field | Value |
   |-------|--------|
   | **Name** | `account-service` (or any name) |
   | **Region** | Oregon (US West) or nearest to you |
   | **Branch** | `main` |
   | **Root Directory** | Leave **empty** (so Render uses the Dockerfile at repo root) |
   | **Runtime** | **Docker** (important: choose Docker, not Node) |
   | **Instance Type** | **Free** to try (or **Starter $7/mo** for always-on) |

5. **Build & Start:** Leave **Build Command** and **Start Command** empty when using Docker. Render will use the Dockerfile.

6. **Environment Variables** – click **Add Environment Variable** and add:

   | Key | Value |
   |----|--------|
   | `MONGO_URI` | Your MongoDB Atlas connection string from Step 1 |
   | `JWT_SECRET` | A long random string (e.g. 32+ characters), e.g. use a password generator |
   | `INTERNAL_API_KEY` | Any secret string (e.g. another random 32-char string) |

7. Click **Create Web Service**. Render will clone the repo, build the Docker image, and start the app (can take 5–10 minutes the first time).

8. When the deploy is **Live**, open the URL Render shows (e.g. `https://account-service-xxxx.onrender.com`). That is your deployed app.

---

## Step 3: Use the deployed app

- Open the Render URL in your browser. You should see the login page.
- **Default admin:** username `admin`, password `admin123` (if the app created the default user on first run).
- Transfers/deposits/withdrawals that call the transaction-service will only work fully if you also deploy **transaction-service** and **notification-service** (see below). For login, customers, and accounts, account-service + MongoDB is enough.

---

## Optional: Deploy transaction-service and notification-service

To have full banking (transfers, history, audit), deploy the other two services the same way:

1. **New Web Service** again, same repo **Banking_project**.
2. You’ll need **two more Dockerfiles** (one for transaction-service, one for notification-service), or one Dockerfile that accepts a build arg. For a quick path, you can add two more Dockerfiles in the repo (e.g. `Dockerfile.transaction`, `Dockerfile.notification`) and in Render set **Dockerfile Path** to that file for each service.
3. For each service, set **MONGO_URI** (same Atlas cluster, different database names: `banking_transactions`, `banking_notifications`) and **INTERNAL_API_KEY** (same value as account-service).
4. For **transaction-service**, set **ACCOUNT_SERVICE_URL** to your account-service Render URL (e.g. `https://account-service-xxxx.onrender.com`).
5. For **account-service**, set **TRANSACTION_SERVICE_URL** and **NOTIFICATION_SERVICE_URL** to the new Render URLs once those services are live.

(If you want, we can add `Dockerfile.transaction` and `Dockerfile.notification` to the repo so you only need to create two more Web Services and set env vars.)

---

## Summary

| What | Where |
|------|--------|
| **Database** | MongoDB Atlas (free tier) → connection string = `MONGO_URI` |
| **Hosting** | Render → Web Service, **Docker** runtime, **no** Root Directory |
| **Env vars** | `MONGO_URI`, `JWT_SECRET`, `INTERNAL_API_KEY` |
| **URL** | Use the URL Render gives you after deploy (e.g. `https://account-service-xxxx.onrender.com`) |

Do **not** use Vercel/Netlify/Cloudflare Pages for this repo; they don’t run Java. Use **Render** (or Railway / Fly.io) with **Docker**.

---

## Troubleshooting: "Connection refused" / "Timed out" to MongoDB

If the container logs show:

- `MongoSocketOpenException: Exception opening socket`
- `Connection refused` to `localhost:27017`
- `DataAccessResourceFailureException: Timed out after 30000 ms`

**Cause:** The app is using the default `mongodb://localhost:27017`. Inside the container there is no MongoDB; `localhost` is the container itself.

**Fix:**

1. Create a MongoDB (e.g. [MongoDB Atlas](https://www.mongodb.com/atlas) free cluster).
2. Get the connection string (e.g. `mongodb+srv://user:pass@cluster0.xxxxx.mongodb.net/banking_accounts?retryWrites=true&w=majority`).
3. In your hosting dashboard (Render, Railway, etc.), add an **environment variable**:
   - **Key:** `MONGO_URI`
   - **Value:** your full Atlas connection string (with real password, no `<password>` placeholder).
4. Redeploy the service so the new env var is picked up.

After that, the app will connect to Atlas instead of localhost and should start successfully.
