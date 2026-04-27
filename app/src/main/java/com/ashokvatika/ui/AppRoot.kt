package com.ashokvatika.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ashokvatika.ui.admin.AdminHomeScreen
import com.ashokvatika.ui.admin.CategoryScreen
import com.ashokvatika.ui.admin.CartProductItem
import com.ashokvatika.ui.admin.DashboardDestination
import com.ashokvatika.ui.admin.InventoryItem
import com.ashokvatika.ui.admin.InventoryListScreen
import com.ashokvatika.ui.admin.OpenCartRow
import com.ashokvatika.ui.admin.OpenCartScreen
import com.ashokvatika.ui.admin.OtcScreen
import com.ashokvatika.ui.admin.SavedDeal
import com.ashokvatika.ui.admin.SavedDealsScreen
import java.util.Locale

private enum class Route {
  Landing,
  AdminHome,
  Otc,
  OpenCart,
  SavedDeals,
  InventoryList,
  InventoryEditor,
}

@Composable
fun AppRoot() {
  var route by remember { mutableStateOf(Route.Landing) }
  var activeInventory by remember { mutableStateOf(DashboardDestination.Plants) }
  var editingItemId by remember { mutableStateOf<String?>(null) }
  val inventory = remember {
    mutableStateMapOf<DashboardDestination, List<InventoryItem>>()
  }
  val openCartRows = remember { mutableStateListOf<OpenCartRow>() }
  val savedDeals = remember { mutableStateListOf<SavedDeal>() }
  var cartRowCounter by remember { mutableIntStateOf(1) }
  var savedDealCounter by remember { mutableIntStateOf(1) }
  var clientName by remember { mutableStateOf("") }
  var clientPhone by remember { mutableStateOf("") }
  var clientId by remember { mutableStateOf("") }
  var transactionCode by remember { mutableStateOf(generateTransactionCode()) }
  val cartItemCount = openCartRows.size

  when (route) {
    Route.Landing -> LandingLoginScreen(
      onLoggedIn = { route = Route.AdminHome },
    )

    Route.AdminHome -> AdminHomeScreen(
      onTileClick = { destination ->
        if (destination == DashboardDestination.OTC) {
          route = Route.Otc
        } else {
          activeInventory = destination
          editingItemId = null
          route = Route.InventoryList
        }
      },
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
      onSaveDeal = {
        if (openCartRows.isNotEmpty()) {
          savedDeals.add(
            0,
            SavedDeal(
              id = "saved-deal-${System.currentTimeMillis()}",
              title = openCartRows.firstOrNull()?.title?.takeIf { it.isNotBlank() } ?: "Deal $savedDealCounter",
              clientName = clientName,
              clientPhone = clientPhone,
              clientId = clientId,
              transactionCode = transactionCode,
              rows = openCartRows.map { row -> row.copy(items = row.items.toList()) },
            ),
          )
          savedDealCounter += 1
          openCartRows.clear()
          clientName = ""
          clientPhone = ""
          clientId = ""
          transactionCode = generateTransactionCode()
          route = Route.SavedDeals
        }
      },
    )

    Route.SavedDeals -> SavedDealsScreen(
      deals = savedDeals,
      onBack = { route = Route.Otc },
      onOpenOtc = { route = Route.Otc },
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
          route = Route.InventoryList
        },
        onIncompleteBack = { item ->
          inventory[activeInventory] = inventory[activeInventory].orEmpty().upsert(item)
          editingItemId = item.id
          route = Route.InventoryList
        },
      )
    }
  }
}

private fun List<InventoryItem>.upsert(item: InventoryItem): List<InventoryItem> {
  val existingIndex = indexOfFirst { it.id == item.id }
  return if (existingIndex >= 0) {
    toMutableList().apply { set(existingIndex, item) }
  } else {
    this + item
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
      existing.copy(quantity = existing.quantity + addition.quantity)
    }
  }
  val existingIds = map { it.id }.toSet()
  val appended = additions.filter { it.id !in existingIds }
  return map { item -> merged.getValue(item.id) } + appended
}

private fun generateTransactionCode(): String {
  return "TX-${System.currentTimeMillis().toString().takeLast(6)}"
}
