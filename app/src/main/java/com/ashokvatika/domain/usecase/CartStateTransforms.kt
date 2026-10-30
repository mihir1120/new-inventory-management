package com.ashokvatika.domain.usecase

import com.ashokvatika.domain.model.CartProductItem
import com.ashokvatika.domain.model.InventoryItem
import com.ashokvatika.domain.model.LiveCartPlant
import com.ashokvatika.domain.model.LiveCustomerSession
import java.util.Locale

fun List<InventoryItem>.upsert(item: InventoryItem): List<InventoryItem> {
  val existingIndex = indexOfFirst { it.id == item.id }
  return if (existingIndex >= 0) {
    toMutableList().apply { set(existingIndex, item) }
  } else {
    listOf(item) + this
  }
}

fun List<CartProductItem>.mergeCartItems(additions: List<CartProductItem>): List<CartProductItem> {
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
  val appendedIds = additions.map { it.id }.distinct().filter { it !in existingIds }
  return map { item -> merged.getValue(item.id) } + appendedIds.map(merged::getValue)
}

fun LiveCustomerSession.openCartRowId(): String {
  return "live-session-$sessionId"
}

fun LiveCartPlant.toCartProductItem(inventoryItems: List<InventoryItem>): CartProductItem {
  val matchedInventoryItem = inventoryItems.firstOrNull { item -> item.id == itemId }
    ?: inventoryItems.firstOrNull { item -> item.name.equals(plantName, ignoreCase = true) }
  val displayName = plantName.ifBlank { matchedInventoryItem?.name.orEmpty() }.ifBlank { "Plant" }

  return CartProductItem(
    id = itemId.ifBlank { displayName.lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), "-").trim('-') },
    name = displayName,
    shortName = displayName
      .split(Regex("\\s+"))
      .filter { it.isNotBlank() }
      .take(2)
      .joinToString("") { it.take(1).uppercase(Locale.US) }
      .ifBlank { displayName.take(2).uppercase(Locale.US) },
    mrp = matchedInventoryItem?.price?.filter(Char::isDigit)?.toIntOrNull() ?: 0,
    landingPrice = matchedInventoryItem?.landingPrice?.filter(Char::isDigit)?.toIntOrNull() ?: 0,
    quantity = quantity.coerceAtLeast(1),
    imageUri = imageUri ?: matchedInventoryItem?.mediaUris?.firstOrNull() ?: matchedInventoryItem?.imageUri,
  )
}

const val InvoiceNumberStart = 26

fun generateTransactionCode(invoiceNumber: Int = InvoiceNumberStart): String {
  return "INV-${invoiceNumber.coerceAtLeast(InvoiceNumberStart)}"
}
