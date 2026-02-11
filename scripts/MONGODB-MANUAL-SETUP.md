# MongoDB manual setup – what to create

Do this in **MongoDB Compass** (or mongosh) so the banking app can run.

---

## 1. Database: `banking_accounts`

Create database **banking_accounts** and these collections with at least one document each.

### Collection: `sequences`

Used by the app to generate IDs (1, 2, 3…). Create these 3 documents:

| _id (string) | seq (number) |
|--------------|--------------|
| `customers`  | `0`          |
| `accounts`   | `0`          |
| `bank_users` | `0`         |

**Example document (first one):**
```json
{
  "_id": "customers",
  "seq": 0
}
```
Then add the same for `"accounts"` and `"bank_users"`.

---

### Collection: `bank_users`

At least one admin user so you can log in. Add **1 document**:

| Field         | Type   | Value |
|---------------|--------|--------|
| `_id`         | long   | `1`   |
| `username`    | string | `"admin"` |
| `passwordHash`| string | (see below) |
| `customerId`  | null   | `null` |
| `role`        | string | `"ADMIN"` |

**passwordHash:** The app expects a **BCrypt** hash. Easiest: **don’t create this document by hand**. Start **account-service** once; it will create the admin user (admin / admin123) and the `bank_users` collection. Then you only need to create the **sequences** and optional customer/account below.

If you still want to create admin manually, use this hash for password **admin123** (BCrypt, strength 10):
```
$2a$10$8K1p/a0dL1LXMIgoEDFrwOfMQbL4sGxYXo/bvFhVvPm6VHqGvJQyO
```

**Example document:**
```json
{
  "_id": NumberLong(1),
  "username": "admin",
  "passwordHash": "$2a$10$8K1p/a0dL1LXMIgoEDFrwOfMQbL4sGxYXo/bvFhVvPm6VHqGvJQyO",
  "customerId": null,
  "role": "ADMIN"
}
```
(In Compass “Add data” → paste and adjust types: `_id` as Int64/long if needed.)

---

### Collection: `customers` (optional)

Only if you want a customer without using the app first. Add **1 document**:

| Field   | Type   | Value              |
|---------|--------|--------------------|
| `_id`   | long   | `1`                |
| `name`  | string | e.g. `"John Doe"`  |
| `email` | string | e.g. `"john@example.com"` |
| `phone` | string | e.g. `"+1 234 567 8900"` |

**Example:**
```json
{
  "_id": NumberLong(1),
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+1 234 567 8900"
}
```

If you add this, set **sequences** → document `_id: "customers"` → `seq: 1` so the next customer gets id 2.

---

### Collection: `accounts` (optional)

Only if you created a customer and want one account. Add **1 document**:

| Field          | Type    | Value |
|----------------|---------|--------|
| `_id`          | long    | `1`   |
| `accountNumber`| string  | e.g. `"ACC000000000001"` (unique) |
| `type`         | string  | `"CURRENT"` or `"SAVINGS"` |
| `balance`      | number  | e.g. `100` or `100.00` |
| `status`       | string  | `"ACTIVE"` |
| `currency`     | string  | `"USD"` |
| `customerId`   | long    | same as customer `_id` (e.g. `1`) |
| `createdAt`    | date    | e.g. `new Date()` or today |

**Example:**
```json
{
  "_id": NumberLong(1),
  "accountNumber": "ACC000000000001",
  "type": "CURRENT",
  "balance": 100,
  "status": "ACTIVE",
  "currency": "USD",
  "customerId": NumberLong(1),
  "createdAt": {"$date": "2025-01-01T00:00:00.000Z"}
}
```

If you add this, set **sequences** → `_id: "accounts"` → `seq: 1`.

---

## 2. Database: `banking_transactions`

- Create database **banking_transactions**.
- Create collection **transactions** (can be empty). The app will create and fill it when you do transfers/deposits/withdrawals.

No documents required upfront.

---

## 3. Database: `banking_notifications`

- Create database **banking_notifications**.
- Create collections **notifications** and **audit_logs** (can be empty).

No documents required upfront.

---

## Minimum you must create (summary)

| Database           | Collection   | What to create |
|--------------------|-------------|----------------|
| **banking_accounts** | **sequences** | 3 documents: `_id` = `"customers"`, `"accounts"`, `"bank_users"`, each with `seq: 0`. |
| **banking_accounts** | **bank_users** | Either: (1) **nothing** and start the app once so it creates admin, or (2) 1 document for admin (see above). |
| **banking_transactions** | **transactions** | Collection only (empty is fine). |
| **banking_notifications** | **notifications** | Collection only (empty is fine). |
| **banking_notifications** | **audit_logs**   | Collection only (empty is fine). |

**Easiest path:** Create database **banking_accounts**, then **sequences** with the 3 docs, then **bank_users** with the 1 admin doc (or leave bank_users empty and start the app once). Create **banking_transactions** and **banking_notifications** with their collections (empty). Then start the app.
