package com.ashokvatika.data

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.ashokvatika.ui.admin.CartProductItem
import com.ashokvatika.ui.admin.DashboardDestination
import com.ashokvatika.ui.admin.InventoryItem
import com.ashokvatika.ui.admin.OpenCartRow
import com.ashokvatika.ui.admin.SavedDeal
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

class AdminFirestoreRepository(
  private val context: Context,
) {
  private val firestore by lazy { Firebase.firestore }

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
        onUpdate(items)
      }
  }

  fun observePendingAccessRequests(
    onUpdate: (List<AdminAccessRequest>) -> Unit,
    onError: (Throwable) -> Unit,
  ): ListenerRegistration {
    return firestore.collection(AccessRequestsCollection)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          onError(error)
          return@addSnapshotListener
        }

        val requests = snapshot?.documents.orEmpty()
          .mapNotNull { document -> document.toAdminAccessRequest() }
          .sortedBy { it.createdAt }
          .filter { it.status == "pending" }
        onUpdate(requests)
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
  }

  suspend fun updateAccessRequestStatus(
    requestId: String,
    approved: Boolean,
  ) {
    firestore.collection(AccessRequestsCollection)
      .document(requestId)
      .update(
        mapOf(
          "status" to if (approved) "approved" else "denied",
          "updatedAt" to currentTimestamp(),
          "note" to if (approved) "Approved by admin" else "Denied by admin",
        ),
      )
      .await()
  }
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
    else -> null
  }
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
