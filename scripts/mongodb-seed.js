// Run this in MongoDB Compass (Mongosh tab) or in terminal: mongosh < scripts/mongodb-seed.js
// Or in Compass: open the "Mongosh" tab at the bottom, paste and run.

// ========== 1. ACCOUNT SERVICE: banking_accounts ==========
db = db.getSiblingDB("banking_accounts");

// Sequences (so app gets IDs 1, 2, 3...)
db.sequences.insertOne({ _id: "customers", seq: NumberLong(0) });
db.sequences.insertOne({ _id: "accounts", seq: NumberLong(0) });
db.sequences.insertOne({ _id: "bank_users", seq: NumberLong(0) });

// Default admin user (username: admin, password: admin123)
// Hash is BCrypt for "admin123" - if login fails, start the app once so it creates admin.
db.bank_users.insertOne({
  _id: NumberLong(1),
  username: "admin",
  passwordHash: "$2a$10$8K1p/a0dL1LXMIgoEDFrwOfMQbL4sGxYXo/bvFhVvPm6VHqGvJQyO",
  customerId: null,
  role: "ADMIN"
});

// Bump sequence so next admin/customer gets id 2
db.sequences.updateOne({ _id: "bank_users" }, { $set: { seq: NumberLong(1) } });

// Optional: sample customer (id 2)
db.customers.insertOne({
  _id: NumberLong(2),
  name: "John Doe",
  email: "john@example.com",
  phone: "+1 234 567 8900"
});
db.sequences.updateOne({ _id: "customers" }, { $set: { seq: NumberLong(2) } });

// Optional: sample account for John (id 1)
db.accounts.insertOne({
  _id: NumberLong(1),
  accountNumber: "ACCSEED0000001",
  type: "CURRENT",
  balance: NumberDecimal("100.00"),
  status: "ACTIVE",
  currency: "USD",
  customerId: NumberLong(2),
  createdAt: new Date()
});
db.sequences.updateOne({ _id: "accounts" }, { $set: { seq: NumberLong(1) } });

print("banking_accounts: sequences, bank_users (admin), customers, accounts created.");

// ========== 2. TRANSACTION SERVICE: banking_transactions ==========
db = db.getSiblingDB("banking_transactions");
// Collections created when app runs; optional empty doc to create collection
db.transactions.insertOne({ _placeholder: true });
db.transactions.deleteOne({ _placeholder: true });
print("banking_transactions: transactions collection ready.");

// ========== 3. NOTIFICATION SERVICE: banking_notifications ==========
db = db.getSiblingDB("banking_notifications");
db.notifications.insertOne({ _placeholder: true });
db.notifications.deleteOne({ _placeholder: true });
db.audit_logs.insertOne({ _placeholder: true });
db.audit_logs.deleteOne({ _placeholder: true });
print("banking_notifications: notifications, audit_logs ready.");
print("Done. Start the app and log in as admin / admin123");
print("If admin login fails, start account-service once so it creates the admin user.");
