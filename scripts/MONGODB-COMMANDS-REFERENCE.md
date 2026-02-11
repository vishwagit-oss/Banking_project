# MongoDB commands – database, indexes, unique keys

## Run the full script (indexes + unique keys for all DBs)

```bash
mongosh --file scripts/mongodb-show-indexes-and-keys.js
```

Or in MongoDB Compass: open the **Mongosh** tab at the bottom, paste the contents of `mongodb-show-indexes-and-keys.js`, and run.

---

## One-off commands (paste in mongosh)

### Switch database
```javascript
use banking_accounts
```

### List all collections in current database
```javascript
db.getCollectionNames()
```

### List all indexes (and see which are UNIQUE) for a collection
```javascript
db.customers.getIndexes()
```
Shows each index and its `key`; if `unique: true`, that index enforces uniqueness.

### See only index names and keys (compact)
```javascript
db.customers.getIndexes().forEach(function(i) { print(i.name + ": " + JSON.stringify(i.key) + (i.unique ? " UNIQUE" : "")); })
```

### Count documents in a collection
```javascript
db.customers.countDocuments()
```

### Find documents with a specific field value (e.g. email)
```javascript
db.customers.find({ email: "vishwagohil2124@gmail.com" })
```

### List all customers (id, name, email only)
```javascript
db.customers.find({}, { _id: 1, name: 1, email: 1 })
```

### Drop an index (if you ever need to remove unique constraint)
```javascript
db.customers.dropIndex("email_1")
```
(Use the exact name from `getIndexes()`.)

### Create a unique index manually
```javascript
db.customers.createIndex({ email: 1 }, { unique: true })
```

### Current database name
```javascript
db.getName()
```

### All three banking databases – indexes summary
```javascript
["banking_accounts","banking_transactions","banking_notifications"].forEach(function(dbName) {
  var d = db.getSiblingDB(dbName);
  print("\n=== " + dbName + " ===");
  d.getCollectionNames().forEach(function(c) {
    var coll = d.getCollection(c);
    coll.getIndexes().forEach(function(i) { print("  " + c + ": " + i.name + " " + JSON.stringify(i.key) + (i.unique ? " UNIQUE" : "")); });
  });
});
```

---

## What “unique key” means

- **`_id`** – Every collection has a unique index on `_id` by default (no two documents can have the same `_id`).
- **`email` on customers** – The app defines a unique index on `email` in the `customers` collection, so no two customers can have the same email.
- Any index with **`unique: true`** in `getIndexes()` enforces no duplicate values for that key (or key combination).
