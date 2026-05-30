package com.ashokvatika.data

import android.content.ContentValues
import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Base64
import com.ashokvatika.ui.admin.CartProductItem
import com.ashokvatika.ui.admin.DashboardDestination
import com.ashokvatika.ui.admin.InventoryItem
import com.ashokvatika.ui.admin.OpenCartRow
import com.ashokvatika.ui.admin.SavedDeal
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val AccessRequestsCollection = "access_requests"
private const val InventoryCollection = "inventory_items"
private const val SavedDealsCollection = "saved_deals"
private const val LiveSessionsCollection = "customer_live_sessions"
private const val ProductQrPrefix = "ashokvatika://product/"
private const val BridgeLogTag = "LocalBridgeSync"

data class AdminAccessRequest(
  val requestId: String,
  val customerId: String,
  val customerName: String,
  val customerPhone: String,
  val status: String,
  val createdAt: String,
  val updatedAt: String? = null,
  val note: String? = null,
)

data class LiveCartPlant(
  val itemId: String,
  val plantName: String,
  val quantity: Int,
  val imageUri: String? = null,
)

data class LiveCustomerSession(
  val sessionId: String,
  val customerId: String,
  val customerName: String,
  val customerPhone: String,
  val isLoggedIn: Boolean,
  val sessionStartedAt: String? = null,
  val lastSeenAt: String? = null,
  val cartUpdatedAt: String? = null,
  val updatedAt: String,
  val cartItems: List<LiveCartPlant> = emptyList(),
)

class LocalBridgeObservation(
  private val dispose: () -> Unit,
) {
  fun remove() {
    dispose()
  }
}

class AdminFirestoreRepository(
  private val context: Context,
) {
  private val firestore by lazy { Firebase.firestore }
  private val realtimeDatabase by lazy { Firebase.database.reference.child(AccessRequestsCollection) }
  private val liveSessionsDatabase by lazy { Firebase.database.reference.child(LiveSessionsCollection) }

  fun observeInventory(
    onUpdate: (List<InventoryItem>) -> Unit,
    onError: (Throwable) -> Unit,
  ): ListenerRegistration {
    return firestore.collection(InventoryCollection)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          onError(error)
          return@addSnapshotListener
        }

        val items = snapshot?.documents.orEmpty()
          .mapNotNull { document -> document.toInventoryItem() }
          .sortedWith(
            compareBy<InventoryItem> { it.category.title }
              .thenByDescending { it.id.inventorySequence() }
              .thenBy { it.name.lowercase(Locale.US) },
          )
        runCatching {
          replaceInventoryBridgeSnapshot(items)
        }.onFailure { error ->
          Log.w(BridgeLogTag, "Unable to mirror inventory snapshot locally", error)
        }
        onUpdate(items)
      }
  }

  fun observePendingAccessRequests(
    onUpdate: (List<AdminAccessRequest>) -> Unit,
    onError: (Throwable) -> Unit,
  ): LocalBridgeObservation {
    val listener = object : ValueEventListener {
      override fun onDataChange(snapshot: DataSnapshot) {
        val requests = snapshot.children
          .mapNotNull { child -> child.toRealtimeAccessRequest() }
          .sortedBy { it.createdAt }
          .filter { it.status == "pending" }
        onUpdate(requests)
      }

      override fun onCancelled(error: DatabaseError) {
        onError(error.toException())
      }
    }
    realtimeDatabase.addValueEventListener(listener)
    return LocalBridgeObservation {
      realtimeDatabase.removeEventListener(listener)
    }
  }

  fun observeLiveCustomerSessions(
    onUpdate: (List<LiveCustomerSession>) -> Unit,
    onError: (Throwable) -> Unit,
  ): LocalBridgeObservation {
    val listener = object : ValueEventListener {
      override fun onDataChange(snapshot: DataSnapshot) {
        val sessions = snapshot.children
          .mapNotNull { child -> child.toLiveCustomerSession() }
          .sortedWith(
            compareByDescending<LiveCustomerSession> { it.isLoggedIn }
              .thenByDescending { it.updatedAt.timestampSortKey() }
              .thenBy { it.customerName.lowercase(Locale.US) },
          )
        runCatching {
          replaceLiveSessionsBridgeSnapshot(sessions)
        }.onFailure { error ->
          Log.w(BridgeLogTag, "Unable to mirror live customer sessions locally", error)
        }
        onUpdate(sessions)
      }

      override fun onCancelled(error: DatabaseError) {
        onError(error.toException())
      }
    }
    liveSessionsDatabase.addValueEventListener(listener)
    return LocalBridgeObservation {
      liveSessionsDatabase.removeEventListener(listener)
    }
  }

  fun observeBridgePendingAccessRequests(
    onUpdate: (List<AdminAccessRequest>) -> Unit,
    onError: (Throwable) -> Unit,
  ): LocalBridgeObservation {
    val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
      override fun onChange(selfChange: Boolean) {
        runCatching {
          loadBridgePendingAccessRequests()
        }.onSuccess(onUpdate).onFailure(onError)
      }
    }

    return runCatching {
      context.contentResolver.registerContentObserver(
        LocalBridgeContract.AccessRequests.ContentUri,
        true,
        observer,
      )
      onUpdate(loadBridgePendingAccessRequests())
      LocalBridgeObservation {
        context.contentResolver.unregisterContentObserver(observer)
      }
    }.getOrElse { error ->
      onError(error)
      LocalBridgeObservation {}
    }
  }

  fun observeBridgeInventory(
    onUpdate: (List<InventoryItem>) -> Unit,
    onError: (Throwable) -> Unit,
  ): LocalBridgeObservation {
    val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
      override fun onChange(selfChange: Boolean) {
        runCatching {
          loadBridgeInventory()
        }.onSuccess(onUpdate).onFailure(onError)
      }
    }

    return runCatching {
      context.contentResolver.registerContentObserver(
        LocalBridgeContract.Inventory.ContentUri,
        true,
        observer,
      )
      onUpdate(loadBridgeInventory())
      LocalBridgeObservation {
        context.contentResolver.unregisterContentObserver(observer)
      }
    }.getOrElse { error ->
      onError(error)
      LocalBridgeObservation {}
    }
  }

  fun observeBridgeLiveCustomerSessions(
    onUpdate: (List<LiveCustomerSession>) -> Unit,
    onError: (Throwable) -> Unit,
  ): LocalBridgeObservation {
    val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
      override fun onChange(selfChange: Boolean) {
        runCatching {
          loadBridgeLiveCustomerSessions()
        }.onSuccess(onUpdate).onFailure(onError)
      }
    }

    return runCatching {
      context.contentResolver.registerContentObserver(
        LocalBridgeContract.LiveSessions.ContentUri,
        true,
        observer,
      )
      onUpdate(loadBridgeLiveCustomerSessions())
      LocalBridgeObservation {
        context.contentResolver.unregisterContentObserver(observer)
      }
    }.getOrElse { error ->
      onError(error)
      LocalBridgeObservation {}
    }
  }

  fun observeSavedDeals(
    onUpdate: (List<SavedDeal>) -> Unit,
    onError: (Throwable) -> Unit,
  ): ListenerRegistration {
    return firestore.collection(SavedDealsCollection)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          onError(error)
          return@addSnapshotListener
        }

        val deals = snapshot?.documents.orEmpty()
          .mapNotNull { document -> document.toSavedDeal() }
          .sortedByDescending { it.id.substringAfterLast('-').toLongOrNull() ?: 0L }
        onUpdate(deals)
      }
  }

  suspend fun upsertInventoryItem(item: InventoryItem) {
    val timestamp = currentTimestamp()
    val stockCount =
      ((item.batch.toIntOrNull() ?: 0) - (item.sale.toIntOrNull() ?: 0) - (item.rent.toIntOrNull() ?: 0)).coerceAtLeast(0)
    val qrCodeValue = "${ProductQrPrefix}${item.id}"
    val imageBase64 = resolvePreviewImageBase64(item, context.contentResolver)

    upsertInventoryBridgeItem(
      item = item,
      updatedAt = timestamp,
      stockCount = stockCount,
      imageBase64 = imageBase64,
      qrCodeValue = qrCodeValue,
    )

    firestore.collection(InventoryCollection)
      .document(item.id)
      .set(
        hashMapOf(
          "id" to item.id,
          "category" to item.category.name.lowercase(Locale.US),
          "name" to item.name,
          "details" to item.details,
          "price" to item.price.orZero(),
          "landingPrice" to item.landingPrice.orZero(),
          "vendorName" to item.vendorName,
          "vendorPhone" to item.vendorPhone,
          "vendorEmail" to item.vendorEmail,
          "batch" to item.batch.orZero(),
          "sale" to item.sale.orZero(),
          "rent" to item.rent.orZero(),
          "stockCount" to stockCount,
          "status" to item.status,
          "checkout" to item.checkout.orZero(),
          "imageUri" to item.imageUri,
          "mediaUris" to item.mediaUris,
          "qrCodeValue" to qrCodeValue,
          "templatePrimaryTitle" to item.templatePrimaryTitle,
          "templatePrimaryDescription" to item.templatePrimaryDescription,
          "templateSecondaryTitle" to item.templateSecondaryTitle,
          "templateSecondaryDescription" to item.templateSecondaryDescription,
          "templateContact" to item.templateContact,
          "updatedAt" to timestamp,
        ),
      )
      .await()
  }

  suspend fun cacheInventoryItem(item: InventoryItem) {
    val timestamp = currentTimestamp()
    val stockCount =
      ((item.batch.toIntOrNull() ?: 0) - (item.sale.toIntOrNull() ?: 0) - (item.rent.toIntOrNull() ?: 0)).coerceAtLeast(0)
    val qrCodeValue = "${ProductQrPrefix}${item.id}"
    val imageBase64 = resolvePreviewImageBase64(item, context.contentResolver)
    upsertInventoryBridgeItem(
      item = item,
      updatedAt = timestamp,
      stockCount = stockCount,
      imageBase64 = imageBase64,
      qrCodeValue = qrCodeValue,
    )
  }

  fun publishInventorySnapshotLocally(items: List<InventoryItem>) {
    replaceInventoryBridgeSnapshot(items)
  }

  suspend fun upsertSavedDeal(deal: SavedDeal) {
    firestore.collection(SavedDealsCollection)
      .document(deal.id)
      .set(
        hashMapOf(
          "id" to deal.id,
          "title" to deal.title,
          "clientName" to deal.clientName,
          "clientPhone" to deal.clientPhone,
          "clientId" to deal.clientId,
          "transactionCode" to deal.transactionCode,
          "rows" to deal.rows.map { row ->
            hashMapOf(
              "id" to row.id,
              "title" to row.title,
              "customAmount" to row.customAmount,
              "items" to row.items.map { item ->
                hashMapOf(
                  "id" to item.id,
                  "name" to item.name,
                  "shortName" to item.shortName,
                  "mrp" to item.mrp,
                  "landingPrice" to item.landingPrice,
                  "quantity" to item.quantity,
                  "imageUri" to item.imageUri,
                )
              },
            )
          },
          "updatedAt" to currentTimestamp(),
        ),
      )
      .await()
  }

  suspend fun deleteInventoryItem(itemId: String) {
    context.contentResolver.delete(LocalBridgeContract.Inventory.itemUri(itemId), null, null)
    firestore.collection(InventoryCollection)
      .document(itemId)
      .delete()
      .await()
  }

  suspend fun deleteInventoryItemLocally(itemId: String) {
    context.contentResolver.delete(LocalBridgeContract.Inventory.itemUri(itemId), null, null)
  }

  suspend fun updateAccessRequestStatus(
    requestId: String,
    approved: Boolean,
  ) {
    val timestamp = currentTimestamp()
    val status = if (approved) "approved" else "denied"
    val note = if (approved) "Approved by admin" else "Denied by admin"

    runCatching {
      realtimeDatabase.child(requestId).updateChildren(
        mapOf(
          "status" to status,
          "updatedAt" to timestamp,
          "note" to note,
        ),
      ).await()
    }.onFailure { error ->
      Log.w(BridgeLogTag, "Realtime access request update failed for $requestId", error)
      throw error
    }
  }

  private fun replaceInventoryBridgeSnapshot(items: List<InventoryItem>) {
    val resolver = context.contentResolver
    resolver.delete(LocalBridgeContract.Inventory.ContentUri, null, null)
    items.forEach { item ->
      val updatedAt = currentTimestamp()
      val stockCount =
        ((item.batch.toIntOrNull() ?: 0) - (item.sale.toIntOrNull() ?: 0) - (item.rent.toIntOrNull() ?: 0)).coerceAtLeast(0)
      val qrCodeValue = "${ProductQrPrefix}${item.id}"
      val imageBase64 = resolvePreviewImageBase64(item, resolver)
      resolver.insert(
        LocalBridgeContract.Inventory.itemUri(item.id),
        item.toInventoryContentValues(
          updatedAt = updatedAt,
          stockCount = stockCount,
          imageBase64 = imageBase64,
          qrCodeValue = qrCodeValue,
        ),
      )
    }
  }

  private fun replaceLiveSessionsBridgeSnapshot(sessions: List<LiveCustomerSession>) {
    val resolver = context.contentResolver
    resolver.delete(LocalBridgeContract.LiveSessions.ContentUri, null, null)
    sessions.forEach { session ->
      resolver.insert(
        LocalBridgeContract.LiveSessions.itemUri(session.sessionId),
        session.toLiveSessionContentValues(),
      )
    }
  }

  private fun upsertInventoryBridgeItem(
    item: InventoryItem,
    updatedAt: String,
    stockCount: Int,
    imageBase64: String?,
    qrCodeValue: String,
  ) {
    context.contentResolver.insert(
      LocalBridgeContract.Inventory.itemUri(item.id),
      item.toInventoryContentValues(
        updatedAt = updatedAt,
        stockCount = stockCount,
        imageBase64 = imageBase64,
        qrCodeValue = qrCodeValue,
      ),
    )
  }

  private fun loadBridgePendingAccessRequests(): List<AdminAccessRequest> {
    return context.contentResolver.query(
      LocalBridgeContract.AccessRequests.ContentUri,
      null,
      null,
      null,
      "${LocalBridgeContract.AccessRequests.CreatedAt} DESC",
    )?.use { cursor ->
      buildList {
        while (cursor.moveToNext()) {
          cursor.toBridgeAccessRequest()?.let(::add)
        }
      }
    }.orEmpty().filter { it.status == "pending" }
  }

  private fun loadBridgeInventory(): List<InventoryItem> {
    return context.contentResolver.query(
      LocalBridgeContract.Inventory.ContentUri,
      null,
      null,
      null,
      "${LocalBridgeContract.Inventory.UpdatedAt} DESC",
    )?.use { cursor ->
      buildList {
        while (cursor.moveToNext()) {
          cursor.toBridgeInventoryItem()?.let(::add)
        }
      }
    }.orEmpty()
      .sortedWith(
        compareBy<InventoryItem> { it.category.title }
          .thenByDescending { it.id.inventorySequence() }
          .thenBy { it.name.lowercase(Locale.US) },
      )
  }

  private fun loadBridgeLiveCustomerSessions(): List<LiveCustomerSession> {
    return context.contentResolver.query(
      LocalBridgeContract.LiveSessions.ContentUri,
      null,
      null,
      null,
      "${LocalBridgeContract.LiveSessions.UpdatedAt} DESC",
    )?.use { cursor ->
      buildList {
        while (cursor.moveToNext()) {
          cursor.toBridgeLiveCustomerSession()?.let(::add)
        }
      }
    }.orEmpty()
      .sortedWith(
        compareByDescending<LiveCustomerSession> { it.isLoggedIn }
          .thenByDescending { it.updatedAt.timestampSortKey() }
          .thenBy { it.customerName.lowercase(Locale.US) },
      )
  }
}

private fun InventoryItem.toInventoryContentValues(
  updatedAt: String,
  stockCount: Int,
  imageBase64: String?,
  qrCodeValue: String,
): ContentValues {
  return ContentValues().apply {
    put(LocalBridgeContract.Inventory.Id, id)
    put(LocalBridgeContract.Inventory.Category, category.name.lowercase(Locale.US))
    put(LocalBridgeContract.Inventory.Name, name)
    put(LocalBridgeContract.Inventory.Details, details)
    put(LocalBridgeContract.Inventory.Price, price.orZero())
    put(LocalBridgeContract.Inventory.LandingPrice, landingPrice.orZero())
    put(LocalBridgeContract.Inventory.VendorName, vendorName)
    put(LocalBridgeContract.Inventory.VendorPhone, vendorPhone)
    put(LocalBridgeContract.Inventory.VendorEmail, vendorEmail)
    put(LocalBridgeContract.Inventory.Batch, batch.orZero())
    put(LocalBridgeContract.Inventory.Sale, sale.orZero())
    put(LocalBridgeContract.Inventory.Rent, rent.orZero())
    put(LocalBridgeContract.Inventory.StockCount, stockCount)
    put(LocalBridgeContract.Inventory.Status, status)
    put(LocalBridgeContract.Inventory.Checkout, checkout.orZero())
    put(LocalBridgeContract.Inventory.ImageUri, imageUri)
    put(LocalBridgeContract.Inventory.MediaUrisJson, mediaUris.toStringListJson())
    put(LocalBridgeContract.Inventory.ImageBase64, imageBase64)
    put(LocalBridgeContract.Inventory.QrCodeValue, qrCodeValue)
    put(LocalBridgeContract.Inventory.TemplatePrimaryTitle, templatePrimaryTitle)
    put(LocalBridgeContract.Inventory.TemplatePrimaryDescription, templatePrimaryDescription)
    put(LocalBridgeContract.Inventory.TemplateSecondaryTitle, templateSecondaryTitle)
    put(LocalBridgeContract.Inventory.TemplateSecondaryDescription, templateSecondaryDescription)
    put(LocalBridgeContract.Inventory.TemplateContact, templateContact)
    put(LocalBridgeContract.Inventory.UpdatedAt, updatedAt)
  }
}

private fun AdminAccessRequest.toAccessRequestContentValues(): ContentValues {
  return ContentValues().apply {
    put(LocalBridgeContract.AccessRequests.RequestId, requestId)
    put(LocalBridgeContract.AccessRequests.CustomerId, customerId)
    put(LocalBridgeContract.AccessRequests.CustomerName, customerName)
    put(LocalBridgeContract.AccessRequests.CustomerPhone, customerPhone)
    put(LocalBridgeContract.AccessRequests.Status, status)
    put(LocalBridgeContract.AccessRequests.CreatedAt, createdAt)
    put(LocalBridgeContract.AccessRequests.UpdatedAt, updatedAt)
    put(LocalBridgeContract.AccessRequests.Note, note)
  }
}

private fun LiveCustomerSession.toLiveSessionContentValues(): ContentValues {
  return ContentValues().apply {
    put(LocalBridgeContract.LiveSessions.SessionId, sessionId)
    put(LocalBridgeContract.LiveSessions.CustomerId, customerId)
    put(LocalBridgeContract.LiveSessions.CustomerName, customerName.ifBlank { "Customer" })
    put(LocalBridgeContract.LiveSessions.CustomerPhone, customerPhone)
    put(LocalBridgeContract.LiveSessions.IsLoggedIn, if (isLoggedIn) 1 else 0)
    put(LocalBridgeContract.LiveSessions.SessionStartedAt, sessionStartedAt)
    put(LocalBridgeContract.LiveSessions.LastSeenAt, lastSeenAt)
    put(LocalBridgeContract.LiveSessions.CartUpdatedAt, cartUpdatedAt)
    put(LocalBridgeContract.LiveSessions.CartItemsJson, cartItems.toCartItemsJson())
    put(LocalBridgeContract.LiveSessions.CartItemCount, cartItems.sumOf { it.quantity.coerceAtLeast(0) })
    put(LocalBridgeContract.LiveSessions.CartPlantSummary, cartItems.toCartPlantSummary())
    put(LocalBridgeContract.LiveSessions.UpdatedAt, updatedAt)
  }
}

private fun Cursor.toBridgeAccessRequest(): AdminAccessRequest? {
  val requestId = getStringOrEmpty(LocalBridgeContract.AccessRequests.RequestId)
  if (requestId.isBlank()) return null

  return AdminAccessRequest(
    requestId = requestId,
    customerId = getStringOrEmpty(LocalBridgeContract.AccessRequests.CustomerId),
    customerName = getStringOrEmpty(LocalBridgeContract.AccessRequests.CustomerName).ifBlank { "Customer" },
    customerPhone = getStringOrEmpty(LocalBridgeContract.AccessRequests.CustomerPhone),
    status = getStringOrEmpty(LocalBridgeContract.AccessRequests.Status),
    createdAt = getStringOrEmpty(LocalBridgeContract.AccessRequests.CreatedAt),
    updatedAt = getOptionalString(LocalBridgeContract.AccessRequests.UpdatedAt),
    note = getOptionalString(LocalBridgeContract.AccessRequests.Note),
  )
}

private fun Cursor.toBridgeInventoryItem(): InventoryItem? {
  val category = getStringOrEmpty(LocalBridgeContract.Inventory.Category).toDashboardDestination() ?: return null
  val id = getStringOrEmpty(LocalBridgeContract.Inventory.Id)
  val name = getStringOrEmpty(LocalBridgeContract.Inventory.Name)
  if (id.isBlank() || name.isBlank()) return null

  val imageUri = getOptionalString(LocalBridgeContract.Inventory.ImageUri)
  val mediaUris = parseStringListJson(getOptionalString(LocalBridgeContract.Inventory.MediaUrisJson))
    .ifEmpty { listOfNotNull(imageUri) }

  return InventoryItem(
    id = id,
    category = category,
    name = name,
    batch = getStringOrEmpty(LocalBridgeContract.Inventory.Batch).orZero(),
    sale = getStringOrEmpty(LocalBridgeContract.Inventory.Sale).orZero(),
    rent = getStringOrEmpty(LocalBridgeContract.Inventory.Rent).orZero(),
    details = getStringOrEmpty(LocalBridgeContract.Inventory.Details),
    price = getStringOrEmpty(LocalBridgeContract.Inventory.Price).orZero(),
    landingPrice = getStringOrEmpty(LocalBridgeContract.Inventory.LandingPrice).orZero(),
    checkout = getStringOrEmpty(LocalBridgeContract.Inventory.Checkout).orZero(),
    vendorName = getStringOrEmpty(LocalBridgeContract.Inventory.VendorName),
    vendorPhone = getStringOrEmpty(LocalBridgeContract.Inventory.VendorPhone),
    vendorEmail = getStringOrEmpty(LocalBridgeContract.Inventory.VendorEmail),
    imageUri = imageUri,
    mediaUris = mediaUris,
    status = getStringOrEmpty(LocalBridgeContract.Inventory.Status).ifBlank { "complete" },
    templatePrimaryTitle = getStringOrEmpty(LocalBridgeContract.Inventory.TemplatePrimaryTitle),
    templatePrimaryDescription = getStringOrEmpty(LocalBridgeContract.Inventory.TemplatePrimaryDescription),
    templateSecondaryTitle = getStringOrEmpty(LocalBridgeContract.Inventory.TemplateSecondaryTitle),
    templateSecondaryDescription = getStringOrEmpty(LocalBridgeContract.Inventory.TemplateSecondaryDescription),
    templateContact = getStringOrEmpty(LocalBridgeContract.Inventory.TemplateContact),
  )
}

private fun Cursor.toBridgeLiveCustomerSession(): LiveCustomerSession? {
  val sessionId = getStringOrEmpty(LocalBridgeContract.LiveSessions.SessionId)
  if (sessionId.isBlank()) return null

  return LiveCustomerSession(
    sessionId = sessionId,
    customerId = getStringOrEmpty(LocalBridgeContract.LiveSessions.CustomerId),
    customerName = getStringOrEmpty(LocalBridgeContract.LiveSessions.CustomerName).ifBlank { "Customer" },
    customerPhone = getStringOrEmpty(LocalBridgeContract.LiveSessions.CustomerPhone),
    isLoggedIn = getBooleanOrFalse(LocalBridgeContract.LiveSessions.IsLoggedIn),
    sessionStartedAt = getOptionalString(LocalBridgeContract.LiveSessions.SessionStartedAt),
    lastSeenAt = getOptionalString(LocalBridgeContract.LiveSessions.LastSeenAt),
    cartUpdatedAt = getOptionalString(LocalBridgeContract.LiveSessions.CartUpdatedAt),
    updatedAt = getOptionalString(LocalBridgeContract.LiveSessions.UpdatedAt).takeIf { !it.isNullOrBlank() }
      ?: getOptionalString(LocalBridgeContract.LiveSessions.LastSeenAt).takeIf { !it.isNullOrBlank() }
      ?: currentTimestamp(),
    cartItems = parseLiveCartItemsJson(getOptionalString(LocalBridgeContract.LiveSessions.CartItemsJson)),
  )
}

private fun DataSnapshot.toRealtimeAccessRequest(): AdminAccessRequest? {
  val requestId = child("requestId").getValue(String::class.java).orEmpty().ifBlank { key.orEmpty() }
  if (requestId.isBlank()) return null

  return AdminAccessRequest(
    requestId = requestId,
    customerId = child("customerId").getValue(String::class.java).orEmpty(),
    customerName = child("customerName").getValue(String::class.java).orEmpty().ifBlank { "Customer" },
    customerPhone = child("customerPhone").getValue(String::class.java).orEmpty(),
    status = child("status").getValue(String::class.java).orEmpty(),
    createdAt = child("createdAt").getValue(String::class.java).orEmpty(),
    updatedAt = child("updatedAt").getValue(String::class.java),
    note = child("note").getValue(String::class.java),
  )
}

private fun DataSnapshot.toLiveCustomerSession(): LiveCustomerSession? {
  val sessionId = stringValue("sessionId").orEmpty().ifBlank { key.orEmpty() }
  if (sessionId.isBlank()) return null

  val cartNode = when {
    child("cartItems").exists() -> child("cartItems")
    child("cart").child("items").exists() -> child("cart").child("items")
    child("cart").exists() -> child("cart")
    else -> null
  }
  val cartItems = cartNode?.children?.mapNotNull { child -> child.toLiveCartPlant() }.orEmpty()
  val updatedAt = stringValue("updatedAt", "lastSeenAt", "cartUpdatedAt", "sessionStartedAt") ?: currentTimestamp()

  return LiveCustomerSession(
    sessionId = sessionId,
    customerId = stringValue("customerId", "userId").orEmpty(),
    customerName = stringValue("customerName", "name").orEmpty().ifBlank { "Customer" },
    customerPhone = stringValue("customerPhone", "phone").orEmpty(),
    isLoggedIn = booleanValue("isLoggedIn", "loggedIn") ?: false,
    sessionStartedAt = stringValue("sessionStartedAt", "loginAt", "loggedInAt"),
    lastSeenAt = stringValue("lastSeenAt", "sessionUpdatedAt", "heartbeatAt"),
    cartUpdatedAt = stringValue("cartUpdatedAt"),
    updatedAt = updatedAt,
    cartItems = cartItems,
  )
}

private fun DataSnapshot.toLiveCartPlant(): LiveCartPlant? {
  val itemId = stringValue("itemId", "plantId", "id").orEmpty().ifBlank { key.orEmpty() }
  val plantName = stringValue("plantName", "name", "title", "productName").orEmpty()
  if (itemId.isBlank() && plantName.isBlank()) return null

  return LiveCartPlant(
    itemId = itemId.ifBlank { plantName.lowercase(Locale.US).replace(" ", "-") },
    plantName = plantName.ifBlank { "Plant" },
    quantity = intValue("quantity", "count").coerceAtLeast(1),
    imageUri = stringValue("imageUri", "image"),
  )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toInventoryItem(): InventoryItem? {
  val category = getString("category").toDashboardDestination() ?: return null
  val id = getString("id").orEmpty().ifBlank { this.id }
  val name = getString("name").orEmpty()
  if (name.isBlank()) return null

  return InventoryItem(
    id = id,
    category = category,
    name = name,
    batch = getString("batch").orZero(),
    sale = getString("sale").orZero(),
    rent = getString("rent").orZero(),
    details = getString("details").orEmpty(),
    price = getString("price").orZero(),
    landingPrice = getString("landingPrice").orZero(),
    checkout = getString("checkout").orZero(),
    vendorName = getString("vendorName").orEmpty(),
    vendorPhone = getString("vendorPhone").orEmpty(),
    vendorEmail = getString("vendorEmail").orEmpty(),
    imageUri = getString("imageUri"),
    mediaUris = (get("mediaUris") as? List<*>)?.filterIsInstance<String>().orEmpty(),
    status = getString("status").orEmpty().ifBlank { "complete" },
    templatePrimaryTitle = getString("templatePrimaryTitle").orEmpty(),
    templatePrimaryDescription = getString("templatePrimaryDescription").orEmpty(),
    templateSecondaryTitle = getString("templateSecondaryTitle").orEmpty(),
    templateSecondaryDescription = getString("templateSecondaryDescription").orEmpty(),
    templateContact = getString("templateContact").orEmpty(),
  )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toAdminAccessRequest(): AdminAccessRequest? {
  val requestId = getString("requestId").orEmpty().ifBlank { id }
  if (requestId.isBlank()) return null

  return AdminAccessRequest(
    requestId = requestId,
    customerId = getString("customerId").orEmpty(),
    customerName = getString("customerName").orEmpty().ifBlank { "Customer" },
    customerPhone = getString("customerPhone").orEmpty(),
    status = getString("status").orEmpty(),
    createdAt = getString("createdAt").orEmpty(),
    updatedAt = getString("updatedAt"),
    note = getString("note"),
  )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toSavedDeal(): SavedDeal? {
  val dealId = getString("id").orEmpty().ifBlank { id }
  if (dealId.isBlank()) return null

  val rows = (get("rows") as? List<*>).orEmpty()
    .mapNotNull { rowEntry -> rowEntry.toOpenCartRow() }

  return SavedDeal(
    id = dealId,
    title = getString("title").orEmpty().ifBlank { "Saved deal" },
    clientName = getString("clientName").orEmpty(),
    clientPhone = getString("clientPhone").orEmpty(),
    clientId = getString("clientId").orEmpty(),
    transactionCode = getString("transactionCode").orEmpty(),
    rows = rows,
  )
}

private fun Any?.toOpenCartRow(): OpenCartRow? {
  val rowMap = this as? Map<*, *> ?: return null
  val rowId = rowMap["id"] as? String ?: return null
  val title = rowMap["title"] as? String ?: "Product"
  val customAmount = (rowMap["customAmount"] as? Number)?.toInt()
  val items = (rowMap["items"] as? List<*>).orEmpty()
    .mapNotNull { itemEntry -> itemEntry.toCartProductItem() }

  return OpenCartRow(
    id = rowId,
    title = title,
    items = items,
    customAmount = customAmount,
  )
}

private fun Any?.toCartProductItem(): CartProductItem? {
  val itemMap = this as? Map<*, *> ?: return null
  val itemId = itemMap["id"] as? String ?: return null
  val name = itemMap["name"] as? String ?: return null
  val shortName = itemMap["shortName"] as? String ?: name.take(2).uppercase(Locale.US)
  val mrp = (itemMap["mrp"] as? Number)?.toInt() ?: 0
  val landingPrice = (itemMap["landingPrice"] as? Number)?.toInt() ?: 0
  val quantity = (itemMap["quantity"] as? Number)?.toInt() ?: 0
  val imageUri = itemMap["imageUri"] as? String

  return CartProductItem(
    id = itemId,
    name = name,
    shortName = shortName,
    mrp = mrp,
    landingPrice = landingPrice,
    quantity = quantity,
    imageUri = imageUri,
  )
}

private fun String?.toDashboardDestination(): DashboardDestination? {
  return when (this?.trim()?.lowercase(Locale.US)) {
    "otc" -> DashboardDestination.OTC
    "plants" -> DashboardDestination.Plants
    "pots" -> DashboardDestination.Pots
    "supplies" -> DashboardDestination.Supplies
    "docs" -> DashboardDestination.Docs
    "live" -> DashboardDestination.Live
    "art" -> DashboardDestination.Art
    else -> null
  }
}

private fun Cursor.getStringOrEmpty(columnName: String): String {
  val index = getColumnIndex(columnName)
  return if (index >= 0 && !isNull(index)) getString(index).orEmpty() else ""
}

private fun Cursor.getOptionalString(columnName: String): String? {
  val index = getColumnIndex(columnName)
  return if (index >= 0 && !isNull(index)) getString(index) else null
}

private fun Cursor.getBooleanOrFalse(columnName: String): Boolean {
  val index = getColumnIndex(columnName)
  if (index < 0 || isNull(index)) return false
  return when (getType(index)) {
    Cursor.FIELD_TYPE_INTEGER -> getInt(index) != 0
    Cursor.FIELD_TYPE_STRING -> {
      val value = getString(index).orEmpty()
      value == "1" || value.equals("true", ignoreCase = true) || value.equals("yes", ignoreCase = true)
    }

    else -> false
  }
}

private fun String?.orZero(): String {
  return if (this.isNullOrBlank()) "0" else this
}

private fun String.inventorySequence(): Long {
  return substringAfterLast('-').toLongOrNull() ?: 0L
}

private fun String?.timestampSortKey(): Long {
  return parseBridgeTimestampMillis(this) ?: 0L
}

private fun currentTimestamp(): String {
  return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
  }.format(Date())
}

private fun parseBridgeTimestampMillis(value: String?): Long? {
  val raw = value?.trim().orEmpty()
  if (raw.isBlank()) return null

  raw.toLongOrNull()?.let { numeric ->
    return if (numeric > 1000000000000L) numeric else numeric * 1000L
  }

  val patterns = listOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSSX" to null,
    "yyyy-MM-dd'T'HH:mm:ssX" to null,
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" to TimeZone.getTimeZone("UTC"),
    "yyyy-MM-dd'T'HH:mm:ss'Z'" to TimeZone.getTimeZone("UTC"),
    "yyyy-MM-dd HH:mm:ss" to null,
    "dd-MM-yyyy HH:mm:ss" to null,
  )
  patterns.forEach { (pattern, timeZone) ->
    runCatching {
      SimpleDateFormat(pattern, Locale.US).apply {
        if (timeZone != null) {
          this.timeZone = timeZone
        }
      }.parse(raw)?.time
    }.getOrNull()?.let { return it }
  }
  return null
}

private fun List<LiveCartPlant>.toCartItemsJson(): String {
  return JSONArray().apply {
    forEach { item ->
      put(
        JSONObject().apply {
          put("itemId", item.itemId)
          put("plantName", item.plantName)
          put("quantity", item.quantity)
          put("imageUri", item.imageUri)
        },
      )
    }
  }.toString()
}

private fun List<String>.toStringListJson(): String {
  return JSONArray().apply {
    forEach { value ->
      put(value)
    }
  }.toString()
}

private fun List<LiveCartPlant>.toCartPlantSummary(): String {
  return joinToString(separator = ", ") { item ->
    "${item.plantName} x${item.quantity.coerceAtLeast(1)}"
  }
}

private fun parseLiveCartItemsJson(raw: String?): List<LiveCartPlant> {
  if (raw.isNullOrBlank()) return emptyList()
  return runCatching {
    val items = JSONArray(raw)
    buildList {
      for (index in 0 until items.length()) {
        val item = items.optJSONObject(index) ?: continue
        val itemId = item.optString("itemId").ifBlank { item.optString("id") }
        val plantName = item.optString("plantName").ifBlank { item.optString("name") }
        if (itemId.isBlank() && plantName.isBlank()) continue
        add(
          LiveCartPlant(
            itemId = itemId.ifBlank { plantName.lowercase(Locale.US).replace(" ", "-") },
            plantName = plantName.ifBlank { "Plant" },
            quantity = item.optInt("quantity", 1).coerceAtLeast(1),
            imageUri = item.optString("imageUri").takeIf { it.isNotBlank() },
          ),
        )
      }
    }
  }.getOrDefault(emptyList())
}

private fun parseStringListJson(raw: String?): List<String> {
  if (raw.isNullOrBlank()) return emptyList()
  return runCatching {
    val items = JSONArray(raw)
    buildList {
      for (index in 0 until items.length()) {
        items.optString(index).takeIf { it.isNotBlank() }?.let(::add)
      }
    }
  }.getOrDefault(emptyList())
}

private fun DataSnapshot.stringValue(vararg keys: String): String? {
  keys.forEach { key ->
    val snapshot = child(key)
    if (!snapshot.exists()) return@forEach
    snapshot.getValue(String::class.java)?.takeIf { it.isNotBlank() }?.let { return it }
    snapshot.value?.toString()?.takeIf { it.isNotBlank() && it != "null" }?.let { return it }
  }
  return null
}

private fun DataSnapshot.booleanValue(vararg keys: String): Boolean? {
  keys.forEach { key ->
    val snapshot = child(key)
    if (!snapshot.exists()) return@forEach
    snapshot.getValue(Boolean::class.java)?.let { return it }
    snapshot.getValue(Long::class.java)?.let { return it != 0L }
    snapshot.value?.toString()?.let { raw ->
      when {
        raw.equals("true", ignoreCase = true) || raw == "1" || raw.equals("yes", ignoreCase = true) -> return true
        raw.equals("false", ignoreCase = true) || raw == "0" || raw.equals("no", ignoreCase = true) -> return false
      }
    }
  }
  return null
}

private fun DataSnapshot.intValue(vararg keys: String): Int {
  keys.forEach { key ->
    val snapshot = child(key)
    if (!snapshot.exists()) return@forEach
    snapshot.getValue(Int::class.java)?.let { return it }
    snapshot.getValue(Long::class.java)?.toInt()?.let { return it }
    snapshot.value?.toString()?.toIntOrNull()?.let { return it }
  }
  return 0
}

private fun resolvePreviewImageBase64(
  item: InventoryItem,
  contentResolver: ContentResolver,
): String? {
  val candidateUri = item.mediaUris.firstOrNull() ?: item.imageUri ?: return null
  return encodeImageUriToBase64(contentResolver, candidateUri)
}

private fun encodeImageUriToBase64(
  contentResolver: ContentResolver,
  uriString: String,
): String? {
  return runCatching {
    val uri = Uri.parse(uriString)
    decodeSampledBitmap(contentResolver, uri, maxDimensionPx = 640)?.let { original ->
      val scaled = scaleBitmapIfNeeded(original)
      ByteArrayOutputStream().use { output ->
        scaled.compress(Bitmap.CompressFormat.JPEG, 72, output)
        if (scaled !== original) {
          scaled.recycle()
        }
        original.recycle()
        Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
      }
    }
  }.getOrNull()
}

private fun decodeSampledBitmap(
  contentResolver: ContentResolver,
  uri: Uri,
  maxDimensionPx: Int,
): Bitmap? {
  val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
  contentResolver.openInputStream(uri)?.use { stream ->
    BufferedInputStream(stream).use { buffered ->
      BitmapFactory.decodeStream(buffered, null, boundsOptions)
    }
  } ?: return null

  val decodeOptions = BitmapFactory.Options().apply {
    inSampleSize = calculateInSampleSize(
      sourceWidth = boundsOptions.outWidth,
      sourceHeight = boundsOptions.outHeight,
      maxDimensionPx = maxDimensionPx,
    )
    inPreferredConfig = Bitmap.Config.ARGB_8888
  }
  return contentResolver.openInputStream(uri)?.use { stream ->
    BufferedInputStream(stream).use { buffered ->
      BitmapFactory.decodeStream(buffered, null, decodeOptions)
    }
  }
}

private fun scaleBitmapIfNeeded(bitmap: Bitmap): Bitmap {
  val maxEdge = maxOf(bitmap.width, bitmap.height)
  if (maxEdge <= 360) return bitmap

  val scale = 360f / maxEdge.toFloat()
  return Bitmap.createScaledBitmap(
    bitmap,
    (bitmap.width * scale).toInt().coerceAtLeast(1),
    (bitmap.height * scale).toInt().coerceAtLeast(1),
    true,
  )
}

private fun calculateInSampleSize(
  sourceWidth: Int,
  sourceHeight: Int,
  maxDimensionPx: Int,
): Int {
  if (sourceWidth <= 0 || sourceHeight <= 0 || maxDimensionPx <= 0) return 1
  val largestEdge = maxOf(sourceWidth, sourceHeight)
  var sampleSize = 1
  while (largestEdge / sampleSize > maxDimensionPx) {
    sampleSize *= 2
  }
  return sampleSize.coerceAtLeast(1)
}
