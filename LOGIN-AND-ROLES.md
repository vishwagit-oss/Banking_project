# Login & roles (Scotiabank-style)

## Flow (like Scotiabank)

1. **Sign In** – Enter username and password. Used by customers who have already activated, and by admins.
2. **Activate now** – For first-time users: enter **Customer ID** (from the bank), choose a **username** and **password**. After that they can sign in with that username/password.

## Default admin (for testing)

- **Username:** `admin`  
- **Password:** `admin123`  

Created automatically on first run. Use this to sign in as Admin and create customers/accounts.

## Customer flow

1. **Admin** creates a **Customer** (name, email, phone) in the portal → system returns **Customer ID**.
2. **Admin** creates an **Account** for that customer → system returns **Account number**.
3. The **customer** goes to the portal → **Activate now** → enters their **Customer ID**, chooses username and password → **Activate**.
4. Next time they **Sign In** with that username and password → they see only **their accounts** (and can transfer, deposit, withdraw, history).

## Roles

- **CUSTOMER** – Can see only their own accounts (by `customerId` in JWT). Can use Transfer, Deposit, Withdraw, History for their accounts.
- **ADMIN** – Can create customers, create accounts, list all customers, find any customer’s accounts, lookup any account, and use Transfer/History.

## Security

- **JWT** is stored in `localStorage` and sent as `Authorization: Bearer <token>` on API calls.
- **Account service** protects `/api/customers` and `/api/accounts`: customers can only access their own data; admin can access all.
- **Debit/Credit** endpoints are open (no JWT) so the **Transaction Service** can call them.
