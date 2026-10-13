package com.ashokvatika.data

import android.net.Uri
import com.ashokvatika.app.BuildConfig

object LocalBridgeContract {
  val Authority = BuildConfig.LOCAL_BRIDGE_AUTHORITY
  private val BaseUri: Uri = Uri.parse("content://$Authority")

  object Inventory {
    const val Table = "inventory_items"
    const val Id = "id"
    const val Category = "category"
    const val Name = "name"
    const val Details = "details"
    const val Price = "price"
    const val LandingPrice = "landingPrice"
    const val VendorName = "vendorName"
    const val VendorPhone = "vendorPhone"
    const val VendorEmail = "vendorEmail"
    const val Batch = "batch"
    const val Sale = "sale"
    const val Rent = "rent"
    const val StockCount = "stockCount"
    const val Status = "status"
    const val Checkout = "checkout"
    const val ImageUri = "imageUri"
    const val MediaUrisJson = "mediaUrisJson"
    const val ImageBase64 = "imageBase64"
    const val QrCodeValue = "qrCodeValue"
    const val TemplatePrimaryTitle = "templatePrimaryTitle"
    const val TemplatePrimaryDescription = "templatePrimaryDescription"
    const val TemplateSecondaryTitle = "templateSecondaryTitle"
    const val TemplateSecondaryDescription = "templateSecondaryDescription"
    const val TemplateContact = "templateContact"
    const val UpdatedAt = "updatedAt"

    val ContentUri: Uri = BaseUri.buildUpon().appendPath(Table).build()

    fun itemUri(itemId: String): Uri = ContentUri.buildUpon().appendPath(itemId).build()
  }

  object AccessRequests {
    const val Table = "access_requests"
    const val RequestId = "requestId"
    const val CustomerId = "customerId"
    const val CustomerName = "customerName"
    const val CustomerPhone = "customerPhone"
    const val Status = "status"
    const val ClientType = "clientType"
    const val CreatedAt = "createdAt"
    const val UpdatedAt = "updatedAt"
    const val Note = "note"

    val ContentUri: Uri = BaseUri.buildUpon().appendPath(Table).build()

    fun itemUri(requestId: String): Uri = ContentUri.buildUpon().appendPath(requestId).build()
  }

  object LiveSessions {
    const val Table = "customer_live_sessions"
    const val SessionId = "sessionId"
    const val CustomerId = "customerId"
    const val CustomerName = "customerName"
    const val CustomerPhone = "customerPhone"
    const val IsLoggedIn = "isLoggedIn"
    const val SessionStartedAt = "sessionStartedAt"
    const val LastSeenAt = "lastSeenAt"
    const val CartUpdatedAt = "cartUpdatedAt"
    const val CartItemsJson = "cartItemsJson"
    const val CartItemCount = "cartItemCount"
    const val CartPlantSummary = "cartPlantSummary"
    const val UpdatedAt = "updatedAt"

    val ContentUri: Uri = BaseUri.buildUpon().appendPath(Table).build()

    fun itemUri(sessionId: String): Uri = ContentUri.buildUpon().appendPath(sessionId).build()
  }
}
