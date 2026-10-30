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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import android.widget.Toast
import com.ashokvatika.domain.model.AdminAccessRequest
import com.ashokvatika.data.AdminAuthStore
import com.ashokvatika.data.AdminFirestoreRepository
import com.ashokvatika.data.LocalBridgeObservation
import com.ashokvatika.domain.model.LiveCartPlant
import com.ashokvatika.domain.model.LiveCustomerSession
import com.ashokvatika.domain.model.CartProductItem
import com.ashokvatika.domain.model.DashboardDestination
import com.ashokvatika.domain.model.InventoryItem
import com.ashokvatika.domain.model.OpenCartRow
import com.ashokvatika.domain.model.SavedDeal
import com.ashokvatika.domain.usecase.InvoiceNumberStart
import com.ashokvatika.domain.usecase.generateTransactionCode
import com.ashokvatika.domain.usecase.mergeCartItems
import com.ashokvatika.domain.usecase.openCartRowId
import com.ashokvatika.domain.usecase.toCartProductItem
import com.ashokvatika.domain.usecase.upsert
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
import com.ashokvatika.ui.admin.DocsHomeScreen
import com.ashokvatika.ui.admin.InventoryListScreen
import com.ashokvatika.ui.admin.InvoiceCreateScreen
import com.ashokvatika.ui.admin.LetterheadScreen
import com.ashokvatika.ui.admin.LiveHomeScreen
import com.ashokvatika.ui.admin.LiveInventoryCatalog
import com.ashokvatika.ui.admin.OpenCartScreen
import com.ashokvatika.ui.admin.OtcScreen
import com.ashokvatika.ui.admin.SavedDealsScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
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
  InvoiceCreate,
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
  authStore.clearObsoleteFirebaseSessionOnce()
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
  val bridgePendingAccessRequests = remember { mutableStateListOf<AdminAccessRequest>() }
  val remotePendingAccessRequests = remember { mutableStateListOf<AdminAccessRequest>() }
  var adminSyncMessage by remember { mutableStateOf<String?>(null) }
  var inventorySaveWarning by remember { mutableStateOf<String?>(null) }
  val liveCustomerSessions = remember { mutableStateListOf<LiveCustomerSession>() }
  val bridgeLiveCustomerSessions = remember { mutableStateListOf<LiveCustomerSession>() }
  val remoteLiveCustomerSessions = remember { mutableStateListOf<LiveCustomerSession>() }
  var liveSyncMessage by remember { mutableStateOf<String?>(null) }
  val openCartRows = remember { mutableStateListOf<OpenCartRow>() }
  val savedDeals = remember { mutableStateListOf<SavedDeal>() }
  LaunchedEffect(firestoreRepository) {
    val localDeals = withContext(Dispatchers.IO) { firestoreRepository.loadSavedDealsLocally() }
    savedDeals.clear()
    savedDeals.addAll(localDeals)
  }
  var cartRowCounter by remember { mutableIntStateOf(1) }
  var savedDealCounter by remember { mutableIntStateOf(nextInvoiceCounter(savedDeals)) }
  var clientName by remember { mutableStateOf("") }
  var clientPhone by remember { mutableStateOf("") }
  var clientId by remember { mutableStateOf("") }
  var transactionCode by remember { mutableStateOf(generateTransactionCode(savedDealCounter)) }
  var syncedCartSessionId by remember { mutableStateOf<String?>(null) }
  var lastNotifiedRequestId by rememberSaveable { mutableStateOf<String?>(null) }
  var hasFirebaseSession by remember(auth) { mutableStateOf(false) }
  var showFirebaseSignInDialog by rememberSaveable { mutableStateOf(false) }
  var firebaseEmail by rememberSaveable { mutableStateOf("") }
  var firebasePassword by rememberSaveable { mutableStateOf("") }
  var firebaseSignInError by remember { mutableStateOf<String?>(null) }
  var firebaseSignInBusy by remember { mutableStateOf(false) }

  DisposableEffect(auth) {
    val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
      val user = firebaseAuth.currentUser
      if (user == null) {
        hasFirebaseSession = false
      } else {
        scope.launch {
          val isAuthorizedAdmin = runCatching {
            com.google.firebase.database.FirebaseDatabase.getInstance()
              .reference
              .child("adminUsers")
              .child(user.uid)
              .get()
              .await()
              .getValue(Boolean::class.java) == true
          }.getOrElse { error ->
            adminSyncMessage = "Firebase admin access check failed: ${error.message ?: "Unable to verify this account."}"
            false
          }
          hasFirebaseSession = isAuthorizedAdmin
          if (!isAuthorizedAdmin && adminSyncMessage.isNullOrBlank()) {
            adminSyncMessage = "Firebase account is signed in but is not enabled for admin access yet."
          } else if (isAuthorizedAdmin) {
            adminSyncMessage = null
          }
        }
      }
    }
    auth.addAuthStateListener(authStateListener)
    onDispose { auth.removeAuthStateListener(authStateListener) }
  }
  val inventoryStateSnapshot = inventory.toMap()
  val inventoryByDestination = remember(inventoryStateSnapshot) {
    inventoryStateSnapshot.mapValues { (_, items) -> items.toList() }
  }
  val currentInventorySnapshot = remember(inventoryStateSnapshot) {
    inventoryStateSnapshot.values.flatten()
  }
  LiveInventoryCatalog.inventoryByDestination = inventoryByDestination
  val liveSessionsSnapshot = liveCustomerSessions.toList()
  val activeLiveSessionsForCart = liveCustomerSessions.filter { session -> session.isLoggedIn }
  val cartItemCount = openCartRows.size + activeLiveSessionsForCart.count { session ->
    openCartRows.none { row -> row.id == session.openCartRowId() }
  }

  fun refreshPendingAccessRequests() {
    val merged = (bridgePendingAccessRequests + remotePendingAccessRequests)
      .associateBy { request -> request.requestId }
      .values
      .sortedBy { request -> request.createdAt }
    pendingAccessRequests.clear()
    pendingAccessRequests.addAll(merged)
  }

  fun refreshLiveCustomerSessions() {
    val merged = (bridgeLiveCustomerSessions + remoteLiveCustomerSessions)
      .associateBy { session -> session.sessionId }
      .values
      .sortedWith(
        compareByDescending<LiveCustomerSession> { session -> session.isLoggedIn }
          .thenByDescending { session -> session.updatedAt }
          .thenBy { session -> session.customerName.lowercase(Locale.US) },
      )
    liveCustomerSessions.clear()
    liveCustomerSessions.addAll(merged)
  }

  fun syncLiveSessionsToOpenCart() {
    val activeSession = liveCustomerSessions
      .filter { session -> session.isLoggedIn }
      .maxByOrNull { session -> session.updatedAt }

    val liveRowIds = liveCustomerSessions.map { session -> session.openCartRowId() }.toSet()
    val activeRowId = activeSession?.openCartRowId()
    openCartRows.removeAll { row -> row.id in liveRowIds && row.id != activeRowId }

    if (activeSession == null) {
      if (syncedCartSessionId != null && openCartRows.none { row -> row.id == syncedCartSessionId?.let { "live-session-$it" } }) {
        syncedCartSessionId = null
      }
      return
    }

    if (syncedCartSessionId != activeSession.sessionId) {
      clientName = activeSession.customerName.ifBlank { clientName }
      clientPhone = activeSession.customerPhone.filter(Char::isDigit).ifBlank { clientPhone }
      clientId = activeSession.customerId.ifBlank { clientId }
      syncedCartSessionId = activeSession.sessionId
    } else {
      if (clientName.isBlank()) clientName = activeSession.customerName
      if (clientPhone.isBlank()) clientPhone = activeSession.customerPhone.filter(Char::isDigit)
      if (clientId.isBlank()) clientId = activeSession.customerId
    }

    val syncedItems = activeSession.cartItems.map { cartItem ->
      cartItem.toCartProductItem(currentInventorySnapshot)
    }
    val syncedRow = OpenCartRow(
      id = activeSession.openCartRowId(),
      title = activeSession.customerName.ifBlank { "Live customer" },
      items = syncedItems,
    )
    val existingIndex = openCartRows.indexOfFirst { row -> row.id == syncedRow.id }
    if (existingIndex >= 0 && syncedItems.isEmpty()) {
      openCartRows.removeAt(existingIndex)
    } else if (existingIndex >= 0) {
      openCartRows[existingIndex] = syncedRow.copy(customAmount = openCartRows[existingIndex].customAmount)
    } else if (syncedItems.isNotEmpty()) {
      openCartRows.add(0, syncedRow)
    }
  }

  LaunchedEffect(isProvisioned) {
    if (!isProvisioned && hasPersistedSession) {
      authStore.setActiveSession(false)
    }
  }

  LaunchedEffect(hasFirebaseSession) {
    if (hasFirebaseSession) {
      authStore.setActiveSession(true)
      withContext(Dispatchers.IO) { firestoreRepository.loadSavedDealsLocally(pendingOnly = true) }.forEach { deal ->
        runCatching { firestoreRepository.upsertSavedDeal(deal) }.onFailure {
          adminSyncMessage = "Saved deal is on this device; cloud sync failed: ${it.message}"
        }
      }
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

  LaunchedEffect(liveSessionsSnapshot, currentInventorySnapshot) {
    syncLiveSessionsToOpenCart()
  }

  if (route != Route.Landing) {
    DisposableEffect(firestoreRepository, hasFirebaseSession) {
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
      val requestsBridgeRegistration = firestoreRepository.observeBridgePendingAccessRequests(
        onUpdate = { requests ->
          bridgePendingAccessRequests.clear()
          bridgePendingAccessRequests.addAll(requests)
          refreshPendingAccessRequests()
        },
        onError = { error ->
          adminSyncMessage = "Approval bridge error: ${error.message ?: "Unable to read local customer requests."}"
        },
      )
      val remoteRegistrations = mutableListOf<LocalBridgeObservation>()
      if (hasFirebaseSession) {
        remoteRegistrations.add(firestoreRepository.observeInventory(
          onUpdate = {
            adminSyncMessage = null
          },
          onError = { error ->
            adminSyncMessage = "Inventory sync error: ${error.message ?: "Unable to connect to Firebase."}"
          },
        ))
        remoteRegistrations.add(firestoreRepository.observePendingAccessRequests(
          onUpdate = { requests ->
            adminSyncMessage = null
            remotePendingAccessRequests.clear()
            remotePendingAccessRequests.addAll(requests)
            refreshPendingAccessRequests()
          },
          onError = { error ->
            adminSyncMessage = "Approval sync error: ${error.message ?: "Firebase access failed."}"
          },
        ))
        remoteRegistrations.add(firestoreRepository.observeSavedDeals(
          onUpdate = { deals ->
            adminSyncMessage = null
            savedDeals.clear()
            savedDeals.addAll(deals)
            val nextCounter = nextInvoiceCounter(deals)
            if (nextCounter > savedDealCounter) {
              savedDealCounter = nextCounter
              if (openCartRows.isEmpty()) {
                transactionCode = generateTransactionCode(nextCounter)
              }
            }
          },
          onError = { error ->
            adminSyncMessage = "OTC sync error: ${error.message ?: "Unable to connect to Firebase."}"
          },
        ))
      } else {
        remotePendingAccessRequests.clear()
        remoteLiveCustomerSessions.clear()
        refreshPendingAccessRequests()
        refreshLiveCustomerSessions()
        adminSyncMessage = null
      }
      val liveBridgeRegistration = firestoreRepository.observeBridgeLiveCustomerSessions(
        onUpdate = { sessions ->
          liveSyncMessage = null
          bridgeLiveCustomerSessions.clear()
          bridgeLiveCustomerSessions.addAll(sessions)
          refreshLiveCustomerSessions()
        },
        onError = { error ->
          liveSyncMessage = "Live sync error: ${error.message ?: "Unable to load customer sessions."}"
        },
      )
      if (hasFirebaseSession) {
        remoteRegistrations.add(firestoreRepository.observeLiveCustomerSessions(
          onUpdate = { sessions ->
            liveSyncMessage = null
            remoteLiveCustomerSessions.clear()
            remoteLiveCustomerSessions.addAll(sessions)
            refreshLiveCustomerSessions()
          },
          onError = { error ->
            liveSyncMessage = "Live sync error: ${error.message ?: "Firebase access failed."}"
          },
        ))
      }

      onDispose {
        inventoryBridgeRegistration.remove()
        requestsBridgeRegistration.remove()
        remoteRegistrations.forEach { registration -> registration.remove() }
        liveBridgeRegistration.remove()
      }
    }
  }

  val handleRequestDecision: (String, Boolean) -> Unit = { requestId, approved ->
    scope.launch {
      runCatching {
        firestoreRepository.updateAccessRequestStatus(requestId, approved = approved)
      }.onFailure { error ->
        adminSyncMessage = "Approval could not be saved: ${error.message}"
      }
    }
  }

  fun handleFirebaseAction() {
    if (auth.currentUser != null) {
      auth.signOut()
      adminSyncMessage = "Firebase disconnected. Local admin data remains available on this device."
    } else {
      firebaseSignInError = null
      showFirebaseSignInDialog = true
    }
  }

  fun submitFirebaseSignIn() {
    if (firebaseSignInBusy) return
    val email = firebaseEmail.trim()
    if (email.isBlank() || firebasePassword.isBlank()) {
      firebaseSignInError = "Enter the admin email and password."
      return
    }
    firebaseSignInBusy = true
    firebaseSignInError = null
    scope.launch {
      runCatching { auth.signInWithEmailAndPassword(email, firebasePassword).await() }
        .onSuccess {
          showFirebaseSignInDialog = false
          firebasePassword = ""
        }
        .onFailure { error ->
          firebaseSignInError = error.localizedMessage ?: "Firebase sign-in failed. Check the email and password."
        }
      firebaseSignInBusy = false
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
        syncMessage = inventorySaveWarning ?: adminSyncMessage,
        firebaseConnected = hasFirebaseSession,
        onFirebaseAction = ::handleFirebaseAction,
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
        inventoryItems = currentInventorySnapshot,
        syncMessage = liveSyncMessage,
        onAddProductToCart = { session, item ->
          scope.launch {
            runCatching {
              firestoreRepository.addProductToLiveCustomerCart(session, item)
            }.onSuccess {
              liveSyncMessage = "${item.name} added to ${session.customerName.ifBlank { "customer" }} cart."
            }.onFailure { error ->
              liveSyncMessage = "Cart update error: ${error.message ?: "Unable to add product."}"
            }
          }
        },
      )

      Route.DocsHome -> DocsHomeScreen(
        onBack = { route = Route.AdminHome },
        onOpenAddress = { route = Route.AddressInfo },
        onOpenInvoice = { route = Route.InvoiceCreate },
        onOpenLetterhead = { route = Route.Letterhead },
        onOpenArt = {
          artBackRoute = Route.DocsHome
          route = Route.ArtHome
        },
      )

      Route.AddressInfo -> AddressInfoScreen(
        onBack = { route = Route.DocsHome },
      )

      Route.InvoiceCreate -> InvoiceCreateScreen(
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
          if (openCartRows.isNotEmpty() || activeLiveSessionsForCart.isNotEmpty()) {
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
        liveSessions = liveCustomerSessions,
        inventoryItems = currentInventorySnapshot,
        liveSyncMessage = liveSyncMessage,
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
          transactionCode = generateTransactionCode(savedDealCounter)
        },
        onAddProductToLiveCart = { session, item ->
          scope.launch {
            runCatching {
              firestoreRepository.addProductToLiveCustomerCart(session, item)
            }.onSuccess {
              liveSyncMessage = "${item.name} added to ${session.customerName.ifBlank { "customer" }} cart."
            }.onFailure { error ->
              liveSyncMessage = "Cart update error: ${error.message ?: "Unable to add product."}"
            }
          }
        },
        onSaveDeal = { deal ->
          if (deal.rows.isNotEmpty() && runCatching { firestoreRepository.cacheSavedDeal(deal) }
              .onFailure { adminSyncMessage = "Deal could not be saved on this device: ${it.message}" }.isSuccess) {
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
            if (!hasFirebaseSession) adminSyncMessage = "Deal saved on this device. Firebase sign-in is required for cloud sync."
            savedDealCounter += 1
            openCartRows.clear()
            clientName = ""
            clientPhone = ""
            clientId = ""
            transactionCode = generateTransactionCode(savedDealCounter)
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
          transactionCode = generateTransactionCode(savedDealCounter)
        },
        onUpdateDealInfo = { dealId, name, phone, savedClientId, savedTransactionCode ->
          val index = savedDeals.indexOfFirst { it.id == dealId }
          if (index >= 0) {
            val updatedDeal = savedDeals[index].copy(
              clientName = name,
              clientPhone = phone.filter(Char::isDigit),
              clientId = savedClientId,
              transactionCode = savedTransactionCode.uppercase(Locale.US),
            )
            runCatching { firestoreRepository.cacheSavedDeal(updatedDeal) }.onSuccess {
              savedDeals[index] = updatedDeal
              if (hasFirebaseSession) {
                scope.launch {
                  runCatching { firestoreRepository.upsertSavedDeal(updatedDeal) }.onFailure {
                    adminSyncMessage = "Deal saved on this device; cloud update failed: ${it.message}"
                  }
                }
              } else {
                adminSyncMessage = "Deal saved on this device. Firebase sign-in is required for cloud sync."
              }
            }.onFailure { adminSyncMessage = "Deal update could not be saved: ${it.message}" }
          }
        },
      )

      Route.InventoryList -> {
        val items = inventory[activeInventory].orEmpty()
        InventoryListScreen(
          title = activeInventory,
          items = items,
          onBack = { route = Route.AdminHome },
          onHome = { route = Route.AdminHome },
          onAddItem = {
            editingItemId = null
            route = Route.InventoryEditor
          },
          onImportItems = { importedItems ->
            importedItems.groupBy { it.category }.forEach { (category, newItems) ->
              inventory[category] = newItems.fold(inventory[category].orEmpty()) { current, item ->
                current.upsert(item)
              }
            }
            scope.launch {
              var failed = 0
              var cloudWarnings = 0
              var firstWarning: String? = null
              importedItems.forEach { item ->
                runCatching { firestoreRepository.upsertInventoryItem(item) }
                  .onSuccess { outcome ->
                    if (outcome.warning != null) {
                      cloudWarnings++
                      if (firstWarning == null) firstWarning = outcome.warning
                    }
                  }
                  .onFailure { failed++ }
              }
              val message = when {
                failed > 0 -> "${importedItems.size - failed} products saved; $failed could not be saved."
                cloudWarnings > 0 -> "${importedItems.size} products saved on this device; $cloudWarnings have a Firebase sync warning."
                else -> "${importedItems.size} products imported."
              }
              inventorySaveWarning = firstWarning ?: if (failed > 0) message else null
              Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
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
              inventorySaveWarning = null
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
          onHome = { route = Route.AdminHome },
          onConfirmBack = { item ->
            inventory[activeInventory] = inventory[activeInventory].orEmpty().upsert(item)
            editingItemId = item.id
            adminSyncMessage = null
            route = Route.InventoryList
            scope.launch {
              runCatching {
                firestoreRepository.upsertInventoryItem(item)
              }.onSuccess { outcome ->
                inventorySaveWarning = outcome.warning
              }.onFailure { error ->
                inventorySaveWarning = null
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
              }.onSuccess { outcome ->
                inventorySaveWarning = outcome.warning
              }.onFailure { error ->
                inventorySaveWarning = null
                adminSyncMessage = "Save failed: ${error.message ?: "Item could not be stored in Firebase."}"
              }
            }
          },
          onExitWithoutChanges = { route = Route.InventoryList },
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

  if (showFirebaseSignInDialog) {
    AlertDialog(
      onDismissRequest = { if (!firebaseSignInBusy) showFirebaseSignInDialog = false },
      title = { Text("Connect admin to Firebase") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Sign in with the admin account created in Firebase Authentication.")
          OutlinedTextField(
            value = firebaseEmail,
            onValueChange = { firebaseEmail = it; firebaseSignInError = null },
            label = { Text("Admin email") },
            singleLine = true,
            enabled = !firebaseSignInBusy,
          )
          OutlinedTextField(
            value = firebasePassword,
            onValueChange = { firebasePassword = it; firebaseSignInError = null },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            enabled = !firebaseSignInBusy,
          )
          firebaseSignInError?.let { error ->
            Text(text = error, color = Color(0xFFB00020))
          }
        }
      },
      confirmButton = {
        Button(onClick = ::submitFirebaseSignIn, enabled = !firebaseSignInBusy) {
          Text(if (firebaseSignInBusy) "Signing in…" else "Sign in")
        }
      },
      dismissButton = {
        TextButton(onClick = { if (!firebaseSignInBusy) showFirebaseSignInDialog = false }) {
          Text("Cancel")
        }
      },
    )
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



private fun nextInvoiceCounter(deals: List<SavedDeal>): Int {
  val lastInvoiceNumber = deals.maxOfOrNull { deal ->
    deal.transactionCode.substringAfterLast('-').toIntOrNull()
      ?: deal.id.takeLastWhile(Char::isDigit).toIntOrNull()
      ?: 0
  } ?: 0
  return maxOf(InvoiceNumberStart, lastInvoiceNumber + 1)
}
