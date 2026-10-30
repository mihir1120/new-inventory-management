package com.ashokvatika.domain.model

data class InventoryItem(
  val id: String,
  val category: DashboardDestination,
  val name: String,
  val batch: String,
  val sale: String,
  val rent: String,
  val details: String,
  val price: String,
  val landingPrice: String,
  val checkout: String,
  val vendorName: String = "",
  val vendorPhone: String = "",
  val vendorEmail: String = "",
  val imageUri: String?,
  val mediaUris: List<String> = emptyList(),
  val status: String = "complete",
  val templatePrimaryTitle: String = "",
  val templatePrimaryDescription: String = "",
  val templateSecondaryTitle: String = "",
  val templateSecondaryDescription: String = "",
  val templateContact: String = "",
  val updatedAt: String = "",
) {
  // Imported IDs remain stable across local storage, Firebase sync, and later edits.
  val isVendorSpreadsheetUpload: Boolean
    get() = id.startsWith("vendor-upload-") ||
      LegacySpreadsheetId.matches(id)

  private companion object {
    // Imports created before the explicit prefix used category + UUID + timestamp.
    val LegacySpreadsheetId = Regex(
      "^[a-z_]+-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}-[0-9]+$",
    )
  }
}

data class CartProductItem(
  val id: String,
  val name: String,
  val shortName: String,
  val mrp: Int,
  val landingPrice: Int,
  val quantity: Int,
  val imageUri: String? = null,
)

data class OpenCartRow(
  val id: String,
  val title: String,
  val items: List<CartProductItem>,
  val customAmount: Int? = null,
)

data class SavedDeal(
  val id: String,
  val title: String,
  val clientName: String,
  val clientPhone: String,
  val clientId: String,
  val transactionCode: String,
  val rows: List<OpenCartRow>,
)
