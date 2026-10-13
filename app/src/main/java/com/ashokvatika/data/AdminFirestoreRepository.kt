package com.ashokvatika.data

import android.content.ContentValues
import android.content.ContentResolver
import android.content.ContentProviderOperation
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
import com.ashokvatika.domain.model.CartProductItem
import com.ashokvatika.domain.model.DashboardDestination
import com.ashokvatika.domain.model.InventoryItem
import com.ashokvatika.domain.model.OpenCartRow
import com.ashokvatika.domain.model.SavedDeal
import com.ashokvatika.domain.model.AdminAccessRequest
import com.ashokvatika.domain.model.LiveCartPlant
import com.ashokvatika.domain.model.LiveCustomerSession
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val AccessRequestsCollection = "access_requests"
private const val InventoryCollection = "inventory_items"
private const val SavedDealsCollection = "saved_deals"
private const val LiveSessionsCollection = "customer_live_sessions"
private const val ProductQrPrefix = "ashokvatika://product/"
private const val BridgeLogTag = "LocalBridgeSync"

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
  private val cacheIoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val firestore by lazy { Firebase.firestore }
  private val realtimeDatabase by lazy { Firebase.database.reference.child(AccessRequestsCollection) }
  private val inventoryDatabase by lazy { Firebase.database.reference.child(InventoryCollection) }
  private val savedDealsDatabase by lazy { Firebase.database.reference.child(SavedDealsCollection) }
  private val liveSessionsDatabase by lazy { Firebase.database.reference.child(LiveSessionsCollection) }

  fun loadSavedDealsLocally(pendingOnly: Boolean = false): List<SavedDeal> {
    return LocalBridgeDatabaseHelper(context).use { helper ->
      helper.readableDatabase.query("saved_deals_local", arrayOf("payload"),
        if (pendingOnly) "pending = 1" else null, null, null, null, null).use { cursor ->
        buildList {
          while (cursor.moveToNext()) {
            val json = JSONObject(cursor.getString(0))
            val rows = json.getJSONArray("rows")
            add(SavedDeal(
              id = json.getString("id"), title = json.getString("title"),
              clientName = json.optString("clientName"), clientPhone = json.optString("clientPhone"),
              clientId = json.optString("clientId"), transactionCode = json.optString("transactionCode"),
              rows = (0 until rows.length()).mapNotNull { rows.getJSONObject(it).toPlainMap().toOpenCartRow() },
            ))
          }
        }
      }
    }
  }

  fun cacheSavedDeal(deal: SavedDeal, pending: Boolean = true) {
    LocalBridgeDatabaseHelper(context).use { helper ->
      val values = ContentValues().apply {
        put("id", deal.id)
        put("payload", JSONObject(deal.toRemoteSavedDealPayload(currentTimestamp())).toString())
        put("pending", if (pending) 1 else 0)
      }
      check(helper.writableDatabase.insertWithOnConflict("saved_deals_local", null, values,
        android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE) != -1L) { "Unable to save deal on this device" }
    }
  }

  fun observeInventory(
    onUpdate: (List<InventoryItem>) -> Unit,
    onError: (Throwable) -> Unit,
  ): LocalBridgeObservation {
    val realtimeItems = mutableMapOf<String, InventoryItem>()
    val firestoreItems = mutableMapOf<String, InventoryItem>()

    fun publishMergedItems() {
      val items = (realtimeItems.values + firestoreItems.values)
        .associateBy { it.id }
        .values
        .sortedWith(
          compareBy<InventoryItem> { it.category.title }
            .thenByDescending { it.id.inventorySequence() }
            .thenBy { it.name.lowercase(Locale.US) },
        )
      // An empty first snapshot can mean that Firebase is still loading or that this
      // install has no remote data yet. Keep the local cache until an actual item
      // arrives; explicit item deletion already removes it from the local bridge.
      if (items.isNotEmpty()) {
        val snapshotItems = items.toList()
        cacheIoScope.launch {
          runCatching {
            val cachedItems = loadBridgeInventory()
            val merged = (cachedItems + snapshotItems).associateBy { it.id }.values.toList()
            if (merged.toSet() != cachedItems.toSet()) {
              replaceInventoryBridgeSnapshot(merged)
            }
          }.onFailure { error ->
            Log.w(BridgeLogTag, "Unable to mirror inventory snapshot locally", error)
          }
        }
      }
      onUpdate(items)
    }

    val realtimeListener = object : ValueEventListener {
      override fun onDataChange(snapshot: DataSnapshot) {
        realtimeItems.clear()
        snapshot.children
          .mapNotNull { child -> child.toRealtimeInventoryItem() }
          .forEach { item -> realtimeItems[item.id] = item }
        publishMergedItems()
      }

      override fun onCancelled(error: DatabaseError) {
        onError(error.toException())
      }
    }
    inventoryDatabase.addValueEventListener(realtimeListener)

    val firestoreRegistration = firestore.collection(InventoryCollection)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          onError(error)
          return@addSnapshotListener
        }

        firestoreItems.clear()
        snapshot?.documents.orEmpty()
          .mapNotNull { document -> document.toInventoryItem() }
          .forEach { item -> firestoreItems[item.id] = item }
        publishMergedItems()
      }

    return LocalBridgeObservation {
      inventoryDatabase.removeEventListener(realtimeListener)
      firestoreRegistration.remove()
    }
  }

  fun observePendingAccessRequests(
    onUpdate: (List<AdminAccessRequest>) -> Unit,
    onError: (Throwable) -> Unit,
  ): LocalBridgeObservation {
    val realtimeRequests = mutableMapOf<String, AdminAccessRequest>()
    val firestoreRequests = mutableMapOf<String, AdminAccessRequest>()

    fun publishMergedRequests() {
      val requests = (realtimeRequests.values + firestoreRequests.values)
        .filter { it.status == "pending" }
        .associateBy { it.requestId }
        .values
        .sortedBy { it.createdAt }
      onUpdate(requests)
    }

    val listener = object : ValueEventListener {
      override fun onDataChange(snapshot: DataSnapshot) {
        realtimeRequests.clear()
        snapshot.children
          .mapNotNull { child -> child.toRealtimeAccessRequest() }
          .filter { it.status == "pending" }
          .forEach { request -> realtimeRequests[request.requestId] = request }
        publishMergedRequests()
      }

      override fun onCancelled(error: DatabaseError) {
        onError(error.toException())
      }
    }
    realtimeDatabase.addValueEventListener(listener)
    val firestoreRegistration = firestore.collection(AccessRequestsCollection)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          onError(error)
          return@addSnapshotListener
        }

        firestoreRequests.clear()
        snapshot?.documents.orEmpty()
          .mapNotNull { document -> document.toAdminAccessRequest() }
          .filter { it.status == "pending" }
          .forEach { request -> firestoreRequests[request.requestId] = request }
        publishMergedRequests()
      }

    return LocalBridgeObservation {
      realtimeDatabase.removeEventListener(listener)
      firestoreRegistration.remove()
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
          .filter { session -> session.isLoggedIn }
          .sortedWith(
            compareByDescending<LiveCustomerSession> { it.isLoggedIn }
              .thenByDescending { it.updatedAt.timestampSortKey() }
              .thenBy { it.customerName.lowercase(Locale.US) },
          )
        val snapshotSessions = sessions.toList()
        cacheIoScope.launch {
          runCatching {
            replaceLiveSessionsBridgeSnapshot(snapshotSessions)
          }.onFailure { error ->
            Log.w(BridgeLogTag, "Unable to mirror live customer sessions locally", error)
          }
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
  ): LocalBridgeObservation {
    val realtimeDeals = mutableMapOf<String, SavedDeal>()
    val firestoreDeals = mutableMapOf<String, SavedDeal>()

    fun publishMergedDeals() {
      val pending = loadSavedDealsLocally(pendingOnly = true)
      val pendingIds = pending.map { it.id }.toSet()
      val remote = (realtimeDeals.values + firestoreDeals.values).associateBy { it.id }.values
      remote.filterNot { it.id in pendingIds }.forEach { cacheSavedDeal(it, pending = false) }
      val deals = (loadSavedDealsLocally() + remote + pending)
        .associateBy { it.id }
        .values
        .sortedByDescending { it.id.substringAfterLast('-').toLongOrNull() ?: 0L }
      onUpdate(deals)
    }

    val realtimeListener = object : ValueEventListener {
      override fun onDataChange(snapshot: DataSnapshot) {
        realtimeDeals.clear()
        snapshot.children
          .mapNotNull { child -> child.toRealtimeSavedDeal() }
          .forEach { deal -> realtimeDeals[deal.id] = deal }
        publishMergedDeals()
      }

      override fun onCancelled(error: DatabaseError) {
        onError(error.toException())
      }
    }
    savedDealsDatabase.addValueEventListener(realtimeListener)

    val firestoreRegistration = firestore.collection(SavedDealsCollection)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          onError(error)
          return@addSnapshotListener
        }

        firestoreDeals.clear()
        snapshot?.documents.orEmpty()
          .mapNotNull { document -> document.toSavedDeal() }
          .forEach { deal -> firestoreDeals[deal.id] = deal }
        publishMergedDeals()
      }

    return LocalBridgeObservation {
      savedDealsDatabase.removeEventListener(realtimeListener)
      firestoreRegistration.remove()
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

    val inventoryPayload = item.toRemoteInventoryPayload(
      updatedAt = timestamp,
      stockCount = stockCount,
      qrCodeValue = qrCodeValue,
    )

    val realtimeUpdateResult = runCatching {
      inventoryDatabase.child(item.id).setValue(inventoryPayload).await()
    }.onFailure { error ->
      Log.w(BridgeLogTag, "Realtime inventory save failed for ${item.id}", error)
    }

    val firestoreUpdateResult = runCatching {
      firestore.collection(InventoryCollection)
      .document(item.id)
        .set(inventoryPayload)
        .await()
    }.onFailure { error ->
      Log.w(BridgeLogTag, "Firestore inventory save failed for ${item.id}", error)
    }

    if (realtimeUpdateResult.isFailure && firestoreUpdateResult.isFailure) {
      throw realtimeUpdateResult.exceptionOrNull()
        ?: firestoreUpdateResult.exceptionOrNull()
        ?: IllegalStateException("Unable to save inventory item ${item.id}")
    }
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
    cacheSavedDeal(deal)
    val payload = deal.toRemoteSavedDealPayload(updatedAt = currentTimestamp())

    val realtimeUpdateResult = runCatching {
      savedDealsDatabase.child(deal.id).setValue(payload).await()
    }.onFailure { error ->
      Log.w(BridgeLogTag, "Realtime saved deal save failed for ${deal.id}", error)
    }

    val firestoreUpdateResult = runCatching {
      firestore.collection(SavedDealsCollection)
        .document(deal.id)
        .set(payload)
        .await()
    }.onFailure { error ->
      Log.w(BridgeLogTag, "Firestore saved deal save failed for ${deal.id}", error)
    }

    if (realtimeUpdateResult.isFailure || firestoreUpdateResult.isFailure) {
      throw realtimeUpdateResult.exceptionOrNull()
        ?: firestoreUpdateResult.exceptionOrNull()
        ?: IllegalStateException("Unable to save deal ${deal.id}")
    }
    if (loadSavedDealsLocally().firstOrNull { it.id == deal.id } == deal) {
      cacheSavedDeal(deal, pending = false)
    }
  }

  suspend fun deleteInventoryItem(itemId: String) {
    context.contentResolver.delete(LocalBridgeContract.Inventory.itemUri(itemId), null, null)
    val realtimeDeleteResult = runCatching {
      inventoryDatabase.child(itemId).removeValue().await()
    }.onFailure { error ->
      Log.w(BridgeLogTag, "Realtime inventory delete failed for $itemId", error)
    }
    val firestoreDeleteResult = runCatching {
      firestore.collection(InventoryCollection)
        .document(itemId)
        .delete()
        .await()
    }.onFailure { error ->
      Log.w(BridgeLogTag, "Firestore inventory delete failed for $itemId", error)
    }
    if (realtimeDeleteResult.isFailure && firestoreDeleteResult.isFailure) {
      throw realtimeDeleteResult.exceptionOrNull()
        ?: firestoreDeleteResult.exceptionOrNull()
        ?: IllegalStateException("Unable to delete inventory item $itemId")
    }
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
      context.contentResolver.update(
        LocalBridgeContract.AccessRequests.itemUri(requestId),
        ContentValues().apply {
          put(LocalBridgeContract.AccessRequests.Status, status)
          put(LocalBridgeContract.AccessRequests.UpdatedAt, timestamp)
          put(LocalBridgeContract.AccessRequests.Note, note)
        },
        null,
        null,
      )
    }.onFailure { error ->
      Log.w(BridgeLogTag, "Local access request bridge update failed for $requestId", error)
    }

    val realtimeUpdateResult = runCatching {
      realtimeDatabase.child(requestId).updateChildren(
        mapOf(
          "status" to status,
          "updatedAt" to timestamp,
          "note" to note,
        ),
      ).await()
    }.onFailure { error ->
      Log.w(BridgeLogTag, "Realtime access request update failed for $requestId", error)
    }

    val firestoreUpdateResult = runCatching {
      firestore.collection(AccessRequestsCollection)
        .document(requestId)
        .set(
          mapOf(
            "status" to status,
            "updatedAt" to timestamp,
            "note" to note,
          ),
          SetOptions.merge(),
        )
        .await()
    }.onFailure { error ->
      Log.w(BridgeLogTag, "Firestore access request update failed for $requestId", error)
    }

    if (realtimeUpdateResult.isFailure && firestoreUpdateResult.isFailure) {
      throw firestoreUpdateResult.exceptionOrNull()
        ?: realtimeUpdateResult.exceptionOrNull()
        ?: IllegalStateException("Unable to update access request $requestId")
    }
  }

  suspend fun addProductToLiveCustomerCart(
    session: LiveCustomerSession,
    item: InventoryItem,
  ) {
    if (session.sessionId.isBlank() || item.id.isBlank()) return

    val timestamp = currentTimestamp()
    val cartItemRef = liveSessionsDatabase
      .child(session.sessionId)
      .child("cartItems")
      .child(item.id)
    val imageUri = item.mediaUris.firstOrNull() ?: item.imageUri
    cartItemRef.incrementCartItem(
      itemId = item.id,
      plantName = item.name,
      imageUri = imageUri,
    )

    liveSessionsDatabase.child(session.sessionId).updateChildren(
      mapOf(
        "cartUpdatedAt" to timestamp,
        "updatedAt" to timestamp,
      ),
    ).await()
  }

  private fun replaceInventoryBridgeSnapshot(items: List<InventoryItem>) {
    val resolver = context.contentResolver
    val operations = arrayListOf(
      ContentProviderOperation.newDelete(LocalBridgeContract.Inventory.ContentUri).build(),
    )
    items.forEach { item ->
      val updatedAt = currentTimestamp()
      val stockCount =
        ((item.batch.toIntOrNull() ?: 0) - (item.sale.toIntOrNull() ?: 0) - (item.rent.toIntOrNull() ?: 0)).coerceAtLeast(0)
      val qrCodeValue = "${ProductQrPrefix}${item.id}"
      val imageBase64 = resolvePreviewImageBase64(item, resolver)
      operations.add(
        ContentProviderOperation.newInsert(LocalBridgeContract.Inventory.itemUri(item.id)).withValues(
          item.toInventoryContentValues(
          updatedAt = updatedAt,
          stockCount = stockCount,
          imageBase64 = imageBase64,
          qrCodeValue = qrCodeValue,
          ),
        ).build(),
      )
    }
    resolver.applyBatch(LocalBridgeContract.Authority, operations)
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
      .filter { session -> session.isLoggedIn }
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

private fun DataSnapshot.toRealtimeInventoryItem(): InventoryItem? {
  val category = stringValue("category").toDashboardDestination() ?: return null
  val id = stringValue("id").orEmpty().ifBlank { key.orEmpty() }
  val name = stringValue("name").orEmpty()
  if (id.isBlank() || name.isBlank()) return null

  return InventoryItem(
    id = id,
    category = category,
    name = name,
    batch = stringValue("batch").orZero(),
    sale = stringValue("sale").orZero(),
    rent = stringValue("rent").orZero(),
    details = stringValue("details").orEmpty(),
    price = stringValue("price").orZero(),
    landingPrice = stringValue("landingPrice").orZero(),
    checkout = stringValue("checkout").orZero(),
    vendorName = stringValue("vendorName").orEmpty(),
    vendorPhone = stringValue("vendorPhone").orEmpty(),
    vendorEmail = stringValue("vendorEmail").orEmpty(),
    imageUri = stringValue("imageUri"),
    mediaUris = childrenList("mediaUris").filterIsInstance<String>(),
    status = stringValue("status").orEmpty().ifBlank { "complete" },
    templatePrimaryTitle = stringValue("templatePrimaryTitle").orEmpty(),
    templatePrimaryDescription = stringValue("templatePrimaryDescription").orEmpty(),
    templateSecondaryTitle = stringValue("templateSecondaryTitle").orEmpty(),
    templateSecondaryDescription = stringValue("templateSecondaryDescription").orEmpty(),
    templateContact = stringValue("templateContact").orEmpty(),
  )
}

private suspend fun DatabaseReference.incrementCartItem(
  itemId: String,
  plantName: String,
  imageUri: String?,
) {
  suspendCancellableCoroutine { continuation ->
    runTransaction(
      object : Transaction.Handler {
        override fun doTransaction(currentData: MutableData): Transaction.Result {
          val quantity = listOf("quantity", "count")
            .firstNotNullOfOrNull { key -> currentData.child(key).getValue(Long::class.java) }
            ?.toInt()
            ?: 0
          currentData.value = mapOf(
            "itemId" to itemId,
            "plantId" to itemId,
            "plantName" to plantName,
            "name" to plantName,
            "quantity" to (quantity + 1).coerceAtLeast(1),
            "imageUri" to imageUri,
          )
          return Transaction.success(currentData)
        }

        override fun onComplete(
          error: DatabaseError?,
          committed: Boolean,
          currentData: DataSnapshot?,
        ) {
          if (!continuation.isActive) return
          when {
            error != null -> continuation.resumeWithException(error.toException())
            !committed -> continuation.resumeWithException(
              IllegalStateException("Live cart update was not committed"),
            )
            else -> continuation.resume(Unit)
          }
        }
      },
    )
  }
}

private fun DataSnapshot.toRealtimeSavedDeal(): SavedDeal? {
  val dealId = stringValue("id").orEmpty().ifBlank { key.orEmpty() }
  if (dealId.isBlank()) return null

  val rows = child("rows").children
    .mapNotNull { rowSnapshot -> rowSnapshot.value.toOpenCartRow() }

  return SavedDeal(
    id = dealId,
    title = stringValue("title").orEmpty().ifBlank { "Saved deal" },
    clientName = stringValue("clientName").orEmpty(),
    clientPhone = stringValue("clientPhone").orEmpty(),
    clientId = stringValue("clientId").orEmpty(),
    transactionCode = stringValue("transactionCode").orEmpty(),
    rows = rows,
  )
}

private fun InventoryItem.toRemoteInventoryPayload(
  updatedAt: String,
  stockCount: Int,
  qrCodeValue: String,
): Map<String, Any?> {
  return mapOf(
    "id" to id,
    "category" to category.title,
    "name" to name,
    "batch" to batch.orZero(),
    "sale" to sale.orZero(),
    "rent" to rent.orZero(),
    "details" to details,
    "price" to price.orZero(),
    "landingPrice" to landingPrice.orZero(),
    "checkout" to checkout.orZero(),
    "vendorName" to vendorName,
    "vendorPhone" to vendorPhone,
    "vendorEmail" to vendorEmail,
    "imageUri" to imageUri,
    "mediaUris" to mediaUris,
    "status" to status.ifBlank { "complete" },
    "stockCount" to stockCount,
    "qrCodeValue" to qrCodeValue,
    "templatePrimaryTitle" to templatePrimaryTitle,
    "templatePrimaryDescription" to templatePrimaryDescription,
    "templateSecondaryTitle" to templateSecondaryTitle,
    "templateSecondaryDescription" to templateSecondaryDescription,
    "templateContact" to templateContact,
    "updatedAt" to updatedAt,
  )
}

private fun SavedDeal.toRemoteSavedDealPayload(updatedAt: String): Map<String, Any?> {
  return mapOf(
    "id" to id,
    "title" to title,
    "clientName" to clientName,
    "clientPhone" to clientPhone,
    "clientId" to clientId,
    "transactionCode" to transactionCode,
    "rows" to rows.map { row -> row.toRemotePayload() },
    "updatedAt" to updatedAt,
  )
}

private fun OpenCartRow.toRemotePayload(): Map<String, Any?> {
  return mapOf(
    "id" to id,
    "title" to title,
    "customAmount" to customAmount,
    "items" to items.map { item -> item.toRemotePayload() },
  )
}

private fun CartProductItem.toRemotePayload(): Map<String, Any?> {
  return mapOf(
    "id" to id,
    "name" to name,
    "shortName" to shortName,
    "mrp" to mrp,
    "landingPrice" to landingPrice,
    "quantity" to quantity,
    "imageUri" to imageUri,
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

private fun JSONObject.toPlainMap(): Map<String, Any?> = keys().asSequence().associateWith { key ->
  get(key).toPlainValue()
}

private fun Any?.toPlainValue(): Any? = when (this) {
  JSONObject.NULL -> null
  is JSONObject -> toPlainMap()
  is JSONArray -> (0 until length()).map { get(it).toPlainValue() }
  else -> this
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
    snapshot.value?.toString()?.takeIf { it.isNotBlank() && it != "null" }?.let { return it }
  }
  return null
}

private fun DataSnapshot.booleanValue(vararg keys: String): Boolean? {
  keys.forEach { key ->
    val snapshot = child(key)
    if (!snapshot.exists()) return@forEach
    parseRemoteBoolean(snapshot.value)?.let { return it }
  }
  return null
}

internal fun parseRemoteBoolean(value: Any?): Boolean? = when (value) {
  is Boolean -> value
  is Number -> value.toLong() != 0L
  is String -> when (value.trim().lowercase(Locale.US)) {
    "true", "1", "yes" -> true
    "false", "0", "no" -> false
    else -> null
  }
  else -> null
}

private fun DataSnapshot.intValue(vararg keys: String): Int {
  keys.forEach { key ->
    val snapshot = child(key)
    if (!snapshot.exists()) return@forEach
    (snapshot.value as? Number)?.let { return it.toInt() }
    snapshot.value?.toString()?.toIntOrNull()?.let { return it }
  }
  return 0
}

private fun DataSnapshot.childrenList(key: String): List<Any?> {
  val snapshot = child(key)
  if (!snapshot.exists()) return emptyList()
  return when (val value = snapshot.value) {
    is List<*> -> value
    is Map<*, *> -> value.values.toList()
    else -> emptyList()
  }
}

private fun Throwable.isIgnorableFirestoreError(): Boolean {
  val rawMessage = message.orEmpty()
  return rawMessage.contains("PERMISSION_DENIED", ignoreCase = true) ||
    rawMessage.contains("UNAVAILABLE", ignoreCase = true) ||
    rawMessage.contains("Failed to get document", ignoreCase = true) ||
    rawMessage.contains("network error", ignoreCase = true) ||
    rawMessage.contains("Missing or insufficient permissions", ignoreCase = true)
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
