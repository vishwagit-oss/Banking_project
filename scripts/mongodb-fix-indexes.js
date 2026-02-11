// Fix wrong unique indexes (field name was stringified by mistake).
//
// FROM WINDOWS POWERSHELL (project root):
//   mongosh --file scripts/mongodb-fix-indexes.js
//
// OR in MongoDB Compass: open Mongosh tab at bottom, paste ONLY the code below
//   (do not paste "mongosh --file ..." inside mongosh).

db = db.getSiblingDB("banking_accounts");

// --- customers: drop wrong index, create correct unique on email ---
try {
  db.customers.dropIndex('{ "email": 1 }_1');
  print("Dropped wrong index on customers");
} catch (e) {
  print("Customers drop: " + e.message);
}
db.customers.createIndex({ email: 1 }, { unique: true });
print("Created index: customers.email (unique)");

// --- bank_users: drop wrong index, create correct unique on username ---
try {
  db.bank_users.dropIndex('{ "username": 1 }_1');
  print("Dropped wrong index on bank_users");
} catch (e) {
  print("bank_users drop: " + e.message);
}
db.bank_users.createIndex({ username: 1 }, { unique: true });
print("Created index: bank_users.username (unique)");

// --- accounts: drop wrong index, create correct unique on accountNumber ---
try {
  db.accounts.dropIndex('{ "accountNumber": 1 }_1');
  print("Dropped wrong index on accounts");
} catch (e) {
  print("accounts drop: " + e.message);
}
db.accounts.createIndex({ accountNumber: 1 }, { unique: true });
print("Created index: accounts.accountNumber (unique)");

print("\nVerify:");
db.customers.getIndexes().forEach(function(i) {
  print("  customers: " + i.name + " " + JSON.stringify(i.key) + (i.unique ? " UNIQUE" : ""));
});
db.bank_users.getIndexes().forEach(function(i) {
  print("  bank_users: " + i.name + " " + JSON.stringify(i.key) + (i.unique ? " UNIQUE" : ""));
});
db.accounts.getIndexes().forEach(function(i) {
  print("  accounts: " + i.name + " " + JSON.stringify(i.key) + (i.unique ? " UNIQUE" : ""));
});
print("\nDone. Unique keys are now on the real fields: email, username, accountNumber.");
