# MongoDB seed script

Use this to create the banking databases and seed data **without starting the app**.

## Option 1: Run in MongoDB Compass

1. Connect to `mongodb://localhost:27017` in Compass.
2. Open the **Mongosh** tab at the bottom (or **>_ MongoDB Shell**).
3. Copy the contents of `mongodb-seed.js` and paste into the shell.
4. Press **Enter** to run.

You should see messages like:
`banking_accounts: sequences, bank_users (admin)...` and `Done.`

## Option 2: Run from terminal

**PowerShell (Windows):**
```powershell
cd C:\Users\vishw\banking-project
mongosh --file scripts/mongodb-seed.js
```

**Cmd.exe:**
```cmd
mongosh < scripts\mongodb-seed.js
```

**Bash / Linux / macOS:**
```bash
mongosh < scripts/mongodb-seed.js
```

## What it creates

| Database             | Collections / data |
|----------------------|--------------------|
| **banking_accounts** | `sequences`, `bank_users` (admin), `customers` (1 sample), `accounts` (1 sample) |
| **banking_transactions** | `transactions` (empty, ready for app) |
| **banking_notifications** | `notifications`, `audit_logs` (empty) |

- **Login:** username `admin`, password `admin123`
- **Sample customer:** John Doe (Customer ID: 2), one account `ACCSEED0000001` with 100 USD

If **admin login fails**, start **account-service** once; it will create the admin user with the correct password hash.
