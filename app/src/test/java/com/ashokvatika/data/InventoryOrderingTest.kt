package com.ashokvatika.data

import com.ashokvatika.domain.model.DashboardDestination
import com.ashokvatika.domain.model.InventoryItem
import org.junit.Assert.assertEquals
import org.junit.Test

class InventoryOrderingTest {
  @Test fun importedUuidItemSortsAboveOlderManualItems() {
    // Cache mirroring can refresh an old item's updatedAt; creation order must remain stable.
    val old = item("plants-1750000000000", "Old plant", "2026-10-28T01:35:00.000Z")
    val imported = item("plants-832c3f10-191a-4aa3-a3b1-178b09d20eb5", "Imported plant", "2026-10-28T01:20:00.000Z")
    val newest = item("plants-fef1e6e5-2ab2-4f7b-99dc-1298c3aedf70-1793151000000", "New plant", "2026-10-28T01:30:00.000Z")

    assertEquals(listOf(newest, imported, old), listOf(old, imported, newest).sortedByMostRecentInventory())
  }

  private fun item(id: String, name: String, updatedAt: String) = InventoryItem(
    id = id,
    category = DashboardDestination.Plants,
    name = name,
    batch = "1",
    sale = "0",
    rent = "0",
    details = "",
    price = "0",
    landingPrice = "0",
    checkout = "0",
    imageUri = null,
    updatedAt = updatedAt,
  )
}
