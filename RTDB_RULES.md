# Realtime Database Rules

The customer-facing screens (`HomeFragment`, `ItemsPurchaseHistory`, `ItemPurchaseCash`, and `AdminCheckListActivity`) query `buyitems` server-side with `orderByChild("customerrfid").equalTo(rfid)` to avoid pulling the entire node into memory.

For this query to use a server-side index (instead of falling back to a client-side scan with a warning), add `.indexOn` to your RTDB rules:

```json
{
  "rules": {
    "customers": {
      ".read": true,
      ".write": true
    },
    "buyitems": {
      ".read": true,
      ".write": true,
      ".indexOn": ["customerrfid", "customerpayment"]
    }
  }
}
```

Paste these in **Firebase Console → Realtime Database → Rules → Publish**.

Without the index, Firebase will log:

```
W/RepoOperation: Using an unspecified index. Your data will be downloaded
and filtered on the client. Consider adding ".indexOn": "customerrfid" at
/buyitems to your security rules for better performance.
```

The app keeps working, but every customer screen will download every row of `buyitems`.
