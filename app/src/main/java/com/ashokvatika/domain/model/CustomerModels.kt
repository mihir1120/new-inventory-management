package com.ashokvatika.domain.model

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

data class LiveCartPlant(
  val itemId: String,
  val plantName: String,
  val quantity: Int,
  val imageUri: String? = null,
)

data class LiveCustomerSession(
  val sessionId: String,
  val customerId: String,
  val customerName: String,
  val customerPhone: String,
  val isLoggedIn: Boolean,
  val sessionStartedAt: String? = null,
  val lastSeenAt: String? = null,
  val cartUpdatedAt: String? = null,
  val updatedAt: String,
  val cartItems: List<LiveCartPlant> = emptyList(),
)
