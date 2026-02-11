# How to open the Banking website

Follow these steps **in order**.

---

## Step 1: Start Account Service

Open **PowerShell** and run:

```powershell
cd C:\Users\vishw\banking-project
.\run-account-service.ps1
```

Wait until you see a line like:

```
Started AccountServiceApplication in ... seconds
```

**Leave this window open.** Do not close it.

---

## Step 2: Open the website in your browser

1. Open **Google Chrome**, **Edge**, or **Firefox**.
2. Click the address bar and type exactly:

   ```
   http://localhost:8081
   ```

3. Press **Enter**.

You should see the **Banking Portal** page with tabs: Customers, Accounts, Transfer, History.

---

## If you still don’t see the website

- Try this URL instead: **http://localhost:8081/index.html**
- Make sure the Account Service window is still running (you see "Started AccountServiceApplication").
- Make sure you typed **8081** (not 8080 or 8082).
- If you see "Cannot connect" or "Refused", start Step 1 again and wait until the app is fully started.

---

## Optional: Start Transaction Service (for Transfer and History)

To use **Transfer**, **Deposit**, **Withdraw**, and **History**, open a **second** PowerShell window and run:

```powershell
cd C:\Users\vishw\banking-project
.\run-transaction-service.ps1
```

Wait until it says "Started TransactionServiceApplication". Then the Transfer and History tabs will work.
