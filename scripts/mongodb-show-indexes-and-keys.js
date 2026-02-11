// Run in mongosh: mongosh < scripts/mongodb-show-indexes-and-keys.js
// Or in MongoDB Compass: open Mongosh tab, paste and run.

// ========== 1. BANKING_ACCOUNTS ==========
db = db.getSiblingDB("banking_accounts");

print("\n========== DATABASE: banking_accounts ==========\n");

["customers", "accounts", "bank_users", "sequences"].forEach(function(collName) {
  var coll = db.getCollection(collName);
  var count = coll.countDocuments();
  print("--- Collection: " + collName + " (documents: " + count + ") ---");
  print("Indexes:");
  coll.getIndexes().forEach(function(idx) {
    var unique = idx.unique ? " UNIQUE" : "";
    print("  " + JSON.stringify(idx.key) + "  name: " + idx.name + unique);
  });
  if (count > 0 && count <= 5) {
    print("Documents:");
    coll.find().forEach(function(doc) { print("  " + JSON.stringify(doc)); });
  } else if (count > 5) {
    print("Sample (first 2):");
    coll.find().limit(2).forEach(function(doc) { print("  " + JSON.stringify(doc)); });
  }
  print("");
});

// ========== 2. BANKING_TRANSACTIONS ==========
db = db.getSiblingDB("banking_transactions");
print("========== DATABASE: banking_transactions ==========\n");
db.getCollectionNames().forEach(function(collName) {
  var coll = db.getCollection(collName);
  print("--- " + collName + " (count: " + coll.countDocuments() + ") ---");
  coll.getIndexes().forEach(function(idx) {
    var unique = idx.unique ? " UNIQUE" : "";
    print("  " + JSON.stringify(idx.key) + "  name: " + idx.name + unique);
  });
  print("");
});

// ========== 3. BANKING_NOTIFICATIONS ==========
db = db.getSiblingDB("banking_notifications");
print("========== DATABASE: banking_notifications ==========\n");
db.getCollectionNames().forEach(function(collName) {
  var coll = db.getCollection(collName);
  print("--- " + collName + " (count: " + coll.countDocuments() + ") ---");
  coll.getIndexes().forEach(function(idx) {
    var unique = idx.unique ? " UNIQUE" : "";
    print("  " + JSON.stringify(idx.key) + "  name: " + idx.name + unique);
  });
  print("");
});

print("Done. Unique indexes enforce no duplicate values for the indexed field(s).");
