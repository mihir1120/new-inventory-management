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
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val AccessRequestsCollection = "access_requests"
private const val InventoryCollection = "inventory_items"
private const val SavedDealsCollection = "saved_deals"
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

    firestore.collection(InventoryCollection)
      .document(item.id)
      .set(
        hashMapOf(
          "id" to item.id,
          "category" to item.category.name.lowercase(Locale.US),
          "name" to item.name,
          "details" to item.details,
          "price" to item.price.orZero(),
          "landingPrice" to item.checkout.orZero(),
          "batch" to item.batch.orZero(),
          "sale" to item.sale.orZero(),
          "rent" to item.rent.orZero(),
          "stockCount" to stockCount,
          "status" to item.status,
          "checkout" to item.checkout.orZero(),
          "imageUri" to item.imageUri,
          "mediaUris" to item.mediaUris,
          "imageBase64" to imageBase64,
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
    upsertInventoryBridgeItem(
      item = item,
      updatedAt = timestamp,
      stockCount = stockCount,
      imageBase64 = imageBase64,
      qrCodeValue = qrCodeValue,
    )
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
    firestore.collection(InventoryCollection)
      .document(itemId)
      .delete()
      .await()
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
    put(LocalBridgeContract.Inventory.LandingPrice, checkout.orZero())
    put(LocalBridgeContract.Inventory.Batch, batch.orZero())
    put(LocalBridgeContract.Inventory.Sale, sale.orZero())
    put(LocalBridgeContract.Inventory.Rent, rent.orZero())
    put(LocalBridgeContract.Inventory.StockCount, stockCount)
    put(LocalBridgeContract.Inventory.Status, status)
    put(LocalBridgeContract.Inventory.Checkout, checkout.orZero())
    put(LocalBridgeContract.Inventory.ImageUri, imageUri)
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
    checkout = getString("checkout").orZero(),
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
    "plants" -> DashboardDestination.Plants
    "pots" -> DashboardDestination.Pots
    "supplies" -> DashboardDestination.Supplies
    "docs" -> DashboardDestination.Docs
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

private fun String?.orZero(): String {
  return if (this.isNullOrBlank()) "0" else this
}

private fun String.inventorySequence(): Long {
  return substringAfterLast('-').toLongOrNull() ?: 0L
}

private fun currentTimestamp(): String {
  return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
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
    contentResolver.openInputStream(uri)?.use { stream ->
      val original = BitmapFactory.decodeStream(stream) ?: return null
      val scaled = scaleBitmapIfNeeded(original)
      ByteArrayOutputStream().use { output ->
        scaled.compress(Bitmap.CompressFormat.JPEG, 78, output)
        Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
      }
    }
  }.getOrNull()
}

private fun scaleBitmapIfNeeded(bitmap: Bitmap): Bitmap {
  val maxEdge = maxOf(bitmap.width, bitmap.height)
  if (maxEdge <= 960) return bitmap

  val scale = 960f / maxEdge.toFloat()
  return Bitmap.createScaledBitmap(
    bitmap,
    (bitmap.width * scale).toInt().coerceAtLeast(1),
    (bitmap.height * scale).toInt().coerceAtLeast(1),
    true,
  )
}
