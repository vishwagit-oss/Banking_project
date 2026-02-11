# Understanding These HTTP Errors

## 403 (Forbidden)

**Meaning:** The server understood the request but **refuses to allow it** (you don’t have permission).

**In this app:**
- You’re **not logged in**, or your **JWT expired** → sign in again.
- You’re logged in as a **customer** but the page is calling an **admin-only** URL (e.g. list/create customers) → 403.
- **Fix:** Sign in as **Admin** (username: `admin`, password: `admin123`) when using the Customers / Accounts tabs. If you already are, sign out and sign in again to get a fresh token.

---

## 400 (Bad Request)

**Meaning:** The server understood the request but says the **data is invalid** or not allowed.

**In this app (Create Customer):**
- You’re seeing **400** on `POST /api/customers` when you click **Create Customer**.
- The backend returns 400 with a message like: **"Customer with email already exists: your@email.com"**.
- **So:** The server is **intentionally rejecting** the create because that email is already in the database (or the app thinks it is).
- **Fix:** Use an email that isn’t used yet, or check the DB (and the “Create customer rejected” log line) to see which customer already has that email.

---

## WebSocket to ws://127.0.0.1:5500//ws failed

**Meaning:** A script (often from **Live Server** in VS Code) is trying to open a WebSocket to port **5500** for live reload.

**In this app:**
- This is **not from the banking app**. The app runs on **8081** and doesn’t use that WebSocket.
- **Fix:** Ignore it, or:
  - Don’t open the app via Live Server (5500). Use **http://localhost:8081** after starting account-service.
  - Or turn off / disable the Live Server extension when testing the banking app.

---

## Quick checklist

| What you see              | Likely cause                    | What to do                                      |
|---------------------------|----------------------------------|-------------------------------------------------|
| 403 on page load / API    | Not admin or token missing/expired | Sign in as admin; sign out and sign in again   |
| 400 on Create Customer    | Email already exists            | Use a different email or check DB / server logs |
| WebSocket 5500 failed     | Live Server / reload script     | Ignore, or run app from 8081 only              |
