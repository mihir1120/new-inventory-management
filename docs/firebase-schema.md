# Firebase Schema

This app uses Firebase for the admin test database. The current production UI is wired to `inventory_items`, `saved_deals`, `access_requests`, and `customer_live_sessions`.

## `inventory_items`

Stores products shown in Plants, Pots, Supplies, and OTC catalogue flows.

Fields:

- `id`: string
- `category`: string
- `name`: string
- `details`: string
- `price`: string
- `landingPrice`: string
- `vendorName`: string
- `vendorPhone`: string
- `vendorEmail`: string
- `batch`: string
- `sale`: string
- `rent`: string
- `stockCount`: number
- `status`: string
- `checkout`: string
- `imageUri`: string or null
- `mediaUris`: string array
- `qrCodeValue`: string
- `templatePrimaryTitle`: string
- `templatePrimaryDescription`: string
- `templateSecondaryTitle`: string
- `templateSecondaryDescription`: string
- `templateContact`: string
- `updatedAt`: ISO-8601 UTC string

Stock formula:

```text
stockCount = batch - sale - rent
```

## `saved_deals`

Stores saved OTC cart/deal records.

Fields:

- `id`: string
- `title`: string
- `clientName`: string
- `clientPhone`: string
- `clientId`: string
- `transactionCode`: string
- `rows`: array
- `updatedAt`: ISO-8601 UTC string

Each row:

- `id`: string
- `title`: string
- `customAmount`: number or null
- `items`: array

Each item:

- `id`: string
- `name`: string
- `shortName`: string
- `mrp`: number
- `landingPrice`: number
- `quantity`: number
- `imageUri`: string or null

## `access_requests`

Realtime Database path used for customer/admin approval requests.

Fields:

- `requestId`: string
- `customerId`: string
- `customerName`: string
- `customerPhone`: string
- `status`: string
- `createdAt`: ISO-8601 UTC string
- `updatedAt`: ISO-8601 UTC string
- `note`: string

Allowed statuses:

- `pending`
- `approved`
- `denied`

## `customer_live_sessions`

Realtime Database path used for live customer sessions and carts.

Fields:

- `sessionId`: string
- `customerId`: string
- `customerName`: string
- `customerPhone`: string
- `isLoggedIn`: boolean
- `sessionStartedAt`: ISO-8601 UTC string
- `lastSeenAt`: ISO-8601 UTC string
- `cartUpdatedAt`: ISO-8601 UTC string
- `updatedAt`: ISO-8601 UTC string
- `cartItems`: object keyed by product id

Each cart item:

- `itemId`: string
- `plantName`: string
- `quantity`: number
- `imageUri`: string or null

## Optional Future Collections

These files have been scaffolded for future direct Firestore modules, but the current UI does not depend on them yet:

- `admins`
- `categories`
- `products`
- `customers`
- `orders`
- `payments`
- `deliveries`
- `inventory_logs`
