# Customer Live Sync Contract

The admin app now reads live customer activity from the Firebase Realtime Database node:

`customer_live_sessions/{sessionId}`

Recommended payload shape:

```json
{
  "sessionId": "session-123",
  "customerId": "customer-42",
  "customerName": "Amit Sharma",
  "customerPhone": "9876543210",
  "isLoggedIn": true,
  "sessionStartedAt": "2026-05-27T19:30:00.000Z",
  "lastSeenAt": "2026-05-27T19:44:00.000Z",
  "cartUpdatedAt": "2026-05-27T19:43:10.000Z",
  "updatedAt": "2026-05-27T19:44:00.000Z",
  "cartItems": {
    "plant-1": {
      "itemId": "plant-1",
      "plantName": "Money Plant",
      "quantity": 2,
      "imageUri": "https://example.com/money-plant.jpg"
    },
    "plant-2": {
      "itemId": "plant-2",
      "plantName": "Snake Plant",
      "quantity": 1
    }
  }
}
```

Notes:

- `sessionId` should stay stable for one login session.
- `updatedAt` should be refreshed whenever login state or cart contents change.
- `cartItems` can be either a keyed object or an array; the admin app accepts both.
- For cart items, the admin app also accepts fallback keys like `name`, `title`, `productName`, `plantId`, and `id`.
