package com.ashokvatika.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashokvatika.data.AdminAccessRequest
import com.ashokvatika.data.AdminAuthStore
import com.ashokvatika.data.AdminFirestoreRepository
import com.ashokvatika.data.LiveCustomerSession
import com.ashokvatika.notifications.AdminApprovalNotifier
import com.ashokvatika.ui.admin.AdminHomeScreen
import com.ashokvatika.ui.admin.ArtHomeScreen
import com.ashokvatika.ui.admin.ArtBrochureScreen
import com.ashokvatika.ui.admin.ArtCatPlusScreen
import com.ashokvatika.ui.admin.ArtCatFourScreen
import com.ashokvatika.ui.admin.ArtCatTwoScreen
import com.ashokvatika.ui.admin.ArtProPlusScreen
import com.ashokvatika.ui.admin.ArtProfileScreen
import com.ashokvatika.ui.admin.AddressInfoScreen
import com.ashokvatika.ui.admin.CategoryScreen
import com.ashokvatika.ui.admin.CartProductItem
import com.ashokvatika.ui.admin.DashboardDestination
import com.ashokvatika.ui.admin.DocsHomeScreen
import com.ashokvatika.ui.admin.InventoryItem
import com.ashokvatika.ui.admin.InventoryListScreen
import com.ashokvatika.ui.admin.LetterheadScreen
import com.ashokvatika.ui.admin.LiveHomeScreen
import com.ashokvatika.ui.admin.LiveInventoryCatalog
import com.ashokvatika.ui.admin.OpenCartRow
import com.ashokvatika.ui.admin.OpenCartScreen
import com.ashokvatika.ui.admin.OtcScreen
import com.ashokvatika.ui.admin.SavedDeal
import com.ashokvatika.ui.admin.SavedDealsScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class Route {
  Landing,
  AdminHome,
  DocsHome,
  Live,
  ArtHome,
  ArtBrochure,
  ArtCatTwo,
  ArtCatFour,
  ArtCatPlus,
  ArtProPlus,
  ArtProfile,
  AddressInfo,
  Letterhead,
  Otc,
  OpenCart,
  SavedDeals,
  InventoryList,
  InventoryEditor,
}

@Composable
fun AppRoot() {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val auth = remember { FirebaseAuth.getInstance() }
  val authStore = remember(context) { AdminAuthStore(context.applicationContext) }
  val firestoreRepository = remember(context) { AdminFirestoreRepository(context.applicationContext) }
  val isProvisioned = authStore.isProvisioned()
  val hasPersistedSession = authStore.hasActiveSession()
  var route by rememberSaveable(isProvisioned, hasPersistedSession) {
    mutableStateOf(
      if (isProvisioned && hasPersistedSession) Route.AdminHome else Route.Landing,
    )
  }
  var artBackRoute by remember { mutableStateOf(Route.AdminHome) }
  var activeInventory by remember { mutableStateOf(DashboardDestination.Plants) }
  var editingItemId by remember { mutableStateOf<String?>(null) }
  val inventory = remember {
    mutableStateMapOf<DashboardDestination, List<InventoryItem>>()
  }
  val pendingAccessRequests = remember { mutableStateListOf<AdminAccessRequest>() }
  var adminSyncMessage by remember { mutableStateOf<String?>(null) }
  val liveCustomerSessions = remember { mutableStateListOf<LiveCustomerSession>() }
  var liveSyncMessage by remember { mutableStateOf<String?>(null) }
  val openCartRows = remember { mutableStateListOf<OpenCartRow>() }
  val savedDeals = remember { mutableStateListOf<SavedDeal>() }
  var cartRowCounter by remember { mutableIntStateOf(1) }
  var savedDealCounter by remember { mutableIntStateOf(1) }
  var clientName by remember { mutableStateOf("") }
  var clientPhone by remember { mutableStateOf("") }
  var clientId by remember { mutableStateOf("") }
  var transactionCode by remember { mutableStateOf(generateTransactionCode()) }
  var lastNotifiedRequestId by rememberSaveable { mutableStateOf<String?>(null) }
  val hasFirebaseSession = auth.currentUser != null
  val cartItemCount = openCartRows.size
  LiveInventoryCatalog.inventoryByDestination = inventory.mapValues { entry -> entry.value.toList() }
  val currentInventorySnapshot = inventory.values.flatten()

  LaunchedEffect(isProvisioned) {
    if (!isProvisioned && hasPersistedSession) {
      authStore.setActiveSession(false)
    }
  }

  LaunchedEffect(hasFirebaseSession) {
    if (hasFirebaseSession) {
      authStore.setActiveSession(true)
    }
  }

  LaunchedEffect(pendingAccessRequests.firstOrNull()?.requestId) {
    val incomingRequest = pendingAccessRequests.firstOrNull() ?: return@LaunchedEffect
    if (incomingRequest.requestId == lastNotifiedRequestId) return@LaunchedEffect

    AdminApprovalNotifier.showIncomingApproval(
      context = context,
      requestId = incomingRequest.requestId,
      customerName = incomingRequest.customerName,
      customerPhone = incomingRequest.customerPhone,
    )
    lastNotifiedRequestId = incomingRequest.requestId
  }

  LaunchedEffect(currentInventorySnapshot) {
    runCatching {
      firestoreRepository.publishInventorySnapshotLocally(currentInventorySnapshot)
    }.onFailure { error ->
      adminSyncMessage = "Inventory bridge sync error: ${error.message ?: "Unable to publish local inventory."}"
    }
  }

  if (route != Route.Landing) {
    DisposableEffect(firestoreRepository) {
      val inventoryBridgeRegistration = firestoreRepository.observeBridgeInventory(
        onUpdate = { cachedItems ->
          val grouped = cachedItems.groupBy { item -> item.category }
          inventory.clear()
          grouped.forEach { (destination, items) ->
            inventory[destination] = items
          }
        },
        onError = { error ->
          adminSyncMessage = "Inventory load error: ${error.message ?: "Unable to read saved inventory."}"
        },
      )
      val inventoryRegistration = firestoreRepository.observeInventory(
        onUpdate = {
          adminSyncMessage = null
        },
        onError = { error ->
          adminSyncMessage = "Inventory sync error: ${error.message ?: "Unable to connect to Firebase."}"
        },
      )
      val requestsRegistration = firestoreRepository.observePendingAccessRequests(
        onUpdate = { requests ->
          adminSyncMessage = null
          pendingAccessRequests.clear()
          pendingAccessRequests.addAll(requests)
        },
        onError = { error ->
          adminSyncMessage = "Approval sync error: ${error.message ?: "Unable to connect to Firebase."}"
        },
      )
      val savedDealsRegistration = firestoreRepository.observeSavedDeals(
        onUpdate = { deals ->
          adminSyncMessage = null
          savedDeals.clear()
          savedDeals.addAll(deals)
        },
        onError = { error ->
          adminSyncMessage = "OTC sync error: ${error.message ?: "Unable to connect to Firebase."}"
        },
      )
      val liveBridgeRegistration = firestoreRepository.observeBridgeLiveCustomerSessions(
        onUpdate = { sessions ->
          liveSyncMessage = null
          liveCustomerSessions.clear()
          liveCustomerSessions.addAll(sessions)
        },
        onError = { error ->
          liveSyncMessage = "Live sync error: ${error.message ?: "Unable to load customer sessions."}"
        },
      )
      val liveRemoteRegistration = firestoreRepository.observeLiveCustomerSessions(
        onUpdate = { sessions ->
          liveSyncMessage = null
          liveCustomerSessions.clear()
          liveCustomerSessions.addAll(sessions)
        },
        onError = { error ->
          liveSyncMessage = "Live sync error: ${error.message ?: "Unable to connect to Firebase."}"
        },
      )

      onDispose {
        inventoryBridgeRegistration.remove()
        inventoryRegistration.remove()
        requestsRegistration.remove()
        savedDealsRegistration.remove()
        liveRemoteRegistration.remove()
        liveBridgeRegistration.remove()
      }
    }
  }

  val handleRequestDecision: (String, Boolean) -> Unit = { requestId, approved ->
    scope.launch {
      firestoreRepository.updateAccessRequestStatus(requestId, approved = approved)
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    when (route) {
      Route.Landing -> LandingLoginScreen(
        onLoggedIn = {
          authStore.setActiveSession(true)
          route = Route.AdminHome
        },
      )

      Route.AdminHome -> AdminHomeScreen(
        pendingAccessRequests = pendingAccessRequests,
        syncMessage = adminSyncMessage,
        onApproveRequest = { requestId -> handleRequestDecision(requestId, true) },
        onDenyRequest = { requestId -> handleRequestDecision(requestId, false) },
        onTileClick = { destination ->
          when (destination) {
            DashboardDestination.OTC -> {
              route = Route.Otc
            }
            DashboardDestination.Docs -> route = Route.DocsHome
            DashboardDestination.Live -> route = Route.Live
            DashboardDestination.Art -> {
              artBackRoute = Route.AdminHome
              route = Route.ArtHome
            }
            else -> {
              activeInventory = destination
              editingItemId = null
              route = Route.InventoryList
            }
          }
        },
      )

      Route.Live -> LiveHomeScreen(
        onBack = { route = Route.AdminHome },
        liveSessions = liveCustomerSessions,
        syncMessage = liveSyncMessage,
      )

      Route.DocsHome -> DocsHomeScreen(
        onBack = { route = Route.AdminHome },
        onOpenAddress = { route = Route.AddressInfo },
        onOpenLetterhead = { route = Route.Letterhead },
        onOpenArt = {
          artBackRoute = Route.DocsHome
          route = Route.ArtHome
        },
      )

      Route.AddressInfo -> AddressInfoScreen(
        onBack = { route = Route.DocsHome },
      )

      Route.Letterhead -> LetterheadScreen(
        onBack = { route = Route.DocsHome },
      )

      Route.ArtHome -> ArtHomeScreen(
        onBack = { route = artBackRoute },
        onOpenBrochure = { route = Route.ArtBrochure },
        onOpenCatTwo = { route = Route.ArtCatTwo },
        onOpenCatFour = { route = Route.ArtCatFour },
        onOpenCatPlus = { route = Route.ArtCatPlus },
        onOpenProPlus = { route = Route.ArtProPlus },
        onOpenProfile = { route = Route.ArtProfile },
      )

      Route.ArtBrochure -> ArtBrochureScreen(
        onBack = { route = Route.ArtHome },
      )

      Route.ArtCatTwo -> ArtCatTwoScreen(
        onBack = { route = Route.ArtHome },
      )

      Route.ArtCatFour -> ArtCatFourScreen(
        onBack = { route = Route.ArtHome },
      )

      Route.ArtCatPlus -> ArtCatPlusScreen(
        onBack = { route = Route.ArtHome },
      )

      Route.ArtProPlus -> ArtProPlusScreen(
        onBack = { route = Route.ArtHome },
      )

      Route.ArtProfile -> ArtProfileScreen(
        onBack = { route = Route.ArtHome },
      )

      Route.Otc -> OtcScreen(
        onBack = { route = Route.AdminHome },
        cartCount = cartItemCount,
        onOpenCart = {
          if (openCartRows.isNotEmpty()) {
            route = Route.OpenCart
          }
        },
        onOpenSavedDeals = { route = Route.SavedDeals },
        onAddToCart = { items ->
          if (items.isEmpty()) return@OtcScreen
          openCartRows.add(
            OpenCartRow(
              id = "cart-row-${System.currentTimeMillis()}",
              title = "Product $cartRowCounter",
              items = items,
            ),
          )
          cartRowCounter += 1
        },
      )

      Route.OpenCart -> OpenCartScreen(
        rows = openCartRows,
        clientName = clientName,
        clientPhone = clientPhone,
        clientId = clientId,
        transactionCode = transactionCode,
        nextSavedDealOrdinal = savedDealCounter,
        onClientNameChange = { clientName = it },
        onClientPhoneChange = { clientPhone = it.filter(Char::isDigit) },
        onClientIdChange = { clientId = it },
        onTransactionCodeChange = { transactionCode = it.uppercase(Locale.US) },
        onBack = { route = Route.Otc },
        onOpenSavedDeals = { route = Route.SavedDeals },
        onAddItemsToRow = { rowId, items ->
          if (items.isEmpty()) return@OpenCartScreen
          val index = openCartRows.indexOfFirst { it.id == rowId }
          if (index >= 0) {
            val row = openCartRows[index]
            openCartRows[index] = row.copy(
              items = row.items.mergeCartItems(items),
              customAmount = null,
            )
          }
        },
        onUpdateRowAmount = { rowId, amount ->
          val index = openCartRows.indexOfFirst { it.id == rowId }
          if (index >= 0) {
            openCartRows[index] = openCartRows[index].copy(customAmount = amount)
          }
        },
        onUpdateRowTitle = { rowId, title ->
          val index = openCartRows.indexOfFirst { it.id == rowId }
          if (index >= 0) {
            openCartRows[index] = openCartRows[index].copy(title = title)
          }
        },
        onUpdateItemQuantity = { rowId, itemId, quantity ->
          val index = openCartRows.indexOfFirst { it.id == rowId }
          if (index >= 0) {
            val row = openCartRows[index]
            val updatedItems = row.items.mapNotNull { item ->
              when {
                item.id != itemId -> item
                quantity <= 0 -> null
                else -> item.copy(quantity = quantity)
              }
            }
            if (updatedItems.isEmpty()) {
              openCartRows.removeAt(index)
            } else {
              openCartRows[index] = row.copy(items = updatedItems, customAmount = null)
            }
          }
        },
        onDeleteItem = { rowId, itemId ->
          val index = openCartRows.indexOfFirst { it.id == rowId }
          if (index >= 0) {
            val row = openCartRows[index]
            val updatedItems = row.items.filterNot { it.id == itemId }
            if (updatedItems.isEmpty()) {
              openCartRows.removeAt(index)
            } else {
              openCartRows[index] = row.copy(items = updatedItems, customAmount = null)
            }
          }
        },
        onDeleteRow = { rowId ->
          openCartRows.removeAll { it.id == rowId }
        },
        onClearCart = {
          openCartRows.clear()
          clientName = ""
          clientPhone = ""
          clientId = ""
          transactionCode = generateTransactionCode()
        },
        onSaveDeal = { deal ->
          if (deal.rows.isNotEmpty()) {
            savedDeals.add(0, deal)
            if (hasFirebaseSession) {
              scope.launch {
                runCatching {
                  firestoreRepository.upsertSavedDeal(deal)
                }.onSuccess {
                  adminSyncMessage = null
                }.onFailure { error ->
                  adminSyncMessage = "OTC save failed: ${error.message ?: "Deal could not be stored in Firebase."}"
                }
              }
            }
            savedDealCounter += 1
            openCartRows.clear()
            clientName = ""
            clientPhone = ""
            clientId = ""
            transactionCode = generateTransactionCode()
          }
        },
      )

      Route.SavedDeals -> SavedDealsScreen(
        deals = savedDeals,
        onBack = { route = Route.Otc },
        onClearCart = {
          openCartRows.clear()
          clientName = ""
          clientPhone = ""
          clientId = ""
          transactionCode = generateTransactionCode()
        },
        onUpdateDealInfo = { dealId, name, phone, savedClientId, savedTransactionCode ->
          val index = savedDeals.indexOfFirst { it.id == dealId }
          if (index >= 0) {
            savedDeals[index] = savedDeals[index].copy(
              clientName = name,
              clientPhone = phone.filter(Char::isDigit),
              clientId = savedClientId,
              transactionCode = savedTransactionCode.uppercase(Locale.US),
            )
          }
        },
      )

      Route.InventoryList -> {
        val items = inventory[activeInventory].orEmpty()
        InventoryListScreen(
          title = activeInventory,
          items = items,
          onBack = { route = Route.AdminHome },
          onAddItem = {
            editingItemId = null
            route = Route.InventoryEditor
          },
          onItemTap = { item ->
            editingItemId = item.id
            route = Route.InventoryEditor
          },
        onDeleteItem = { item ->
          inventory[activeInventory] = inventory[activeInventory].orEmpty().filterNot { it.id == item.id }
          if (editingItemId == item.id) editingItemId = null
          scope.launch {
            runCatching {
              firestoreRepository.deleteInventoryItem(item.id)
            }.onSuccess {
              adminSyncMessage = null
            }.onFailure { error ->
              adminSyncMessage = "Delete failed: ${error.message ?: "Item could not be removed from Firebase."}"
            }
          }
        },
      )
      }

      Route.InventoryEditor -> {
        val currentItem = inventory[activeInventory].orEmpty().firstOrNull { it.id == editingItemId }
        CategoryScreen(
          title = activeInventory,
          initialItem = currentItem,
          onConfirmBack = { item ->
            inventory[activeInventory] = inventory[activeInventory].orEmpty().upsert(item)
            editingItemId = item.id
            adminSyncMessage = null
            route = Route.InventoryList
            scope.launch {
              runCatching {
                firestoreRepository.upsertInventoryItem(item)
              }.onSuccess {
                adminSyncMessage = null
              }.onFailure { error ->
                adminSyncMessage = "Save failed: ${error.message ?: "Item could not be stored in Firebase."}"
              }
            }
          },
          onIncompleteBack = { item ->
            inventory[activeInventory] = inventory[activeInventory].orEmpty().upsert(item)
            editingItemId = item.id
            adminSyncMessage = null
            route = Route.InventoryList
            scope.launch {
              runCatching {
                firestoreRepository.upsertInventoryItem(item)
              }.onSuccess {
                adminSyncMessage = null
              }.onFailure { error ->
                adminSyncMessage = "Save failed: ${error.message ?: "Item could not be stored in Firebase."}"
              }
            }
          },
        )
      }
    }

    if (route != Route.Landing) {
      IncomingCallApprovalPopup(
        request = pendingAccessRequests.firstOrNull(),
        pendingCount = pendingAccessRequests.size,
        onApproveRequest = { requestId -> handleRequestDecision(requestId, true) },
        onDenyRequest = { requestId -> handleRequestDecision(requestId, false) },
      )
    }
  }
}

@Composable
private fun IncomingCallApprovalPopup(
  request: AdminAccessRequest?,
  pendingCount: Int,
  onApproveRequest: (String) -> Unit,
  onDenyRequest: (String) -> Unit,
) {
  val activeRequest = request ?: return

  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.TopEnd,
  ) {
    Card(
      modifier = Modifier
        .padding(top = 24.dp, end = 20.dp, start = 20.dp),
      colors = CardDefaults.cardColors(
        containerColor = Color.White,
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = if (pendingCount > 1) "Incoming customer call ($pendingCount waiting)" else "Incoming customer call",
          color = Color(0xFF2E7D32),
          fontSize = 18.sp,
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text = activeRequest.customerName,
          color = Color(0xFF141414),
          fontSize = 22.sp,
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text = activeRequest.customerPhone.ifBlank { "Phone not available" },
          color = Color(0xFF5F6368),
          fontSize = 15.sp,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Button(
            onClick = { onDenyRequest(activeRequest.requestId) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFFD32F2F),
              contentColor = Color.White,
            ),
          ) {
            Text(text = "Deny")
          }
          Button(
            onClick = { onApproveRequest(activeRequest.requestId) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF2E7D32),
              contentColor = Color.White,
            ),
          ) {
            Text(text = "Accept")
          }
        }
      }
    }
  }
}

private fun List<InventoryItem>.upsert(item: InventoryItem): List<InventoryItem> {
  val existingIndex = indexOfFirst { it.id == item.id }
  return if (existingIndex >= 0) {
    toMutableList().apply { set(existingIndex, item) }
  } else {
    listOf(item) + this
  }
}

private fun List<CartProductItem>.mergeCartItems(additions: List<CartProductItem>): List<CartProductItem> {
  if (additions.isEmpty()) return this
  val merged = associateBy { it.id }.toMutableMap()
  additions.forEach { addition ->
    val existing = merged[addition.id]
    merged[addition.id] = if (existing == null) {
      addition
    } else {
      existing.copy(
        quantity = existing.quantity + addition.quantity,
        imageUri = existing.imageUri ?: addition.imageUri,
      )
    }
  }
  val existingIds = map { it.id }.toSet()
  val appended = additions.filter { it.id !in existingIds }
  return map { item -> merged.getValue(item.id) } + appended
}

private fun generateTransactionCode(): String {
  val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
  val randomSuffix = buildString {
    repeat(6) {
      append(TransactionCodeAlphabet[SecureRandomHolder.random.nextInt(TransactionCodeAlphabet.length)])
    }
  }
  return "TX-$timestamp-$randomSuffix"
}

private const val TransactionCodeAlphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

private object SecureRandomHolder {
  val random = SecureRandom()
}
