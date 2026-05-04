package com.ashokvatika.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LocalBridgeDatabaseHelper(
  context: Context,
) : SQLiteOpenHelper(context, DatabaseName, null, DatabaseVersion) {
  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(
      """
      CREATE TABLE ${LocalBridgeContract.Inventory.Table} (
        ${LocalBridgeContract.Inventory.Id} TEXT PRIMARY KEY,
        ${LocalBridgeContract.Inventory.Category} TEXT NOT NULL,
        ${LocalBridgeContract.Inventory.Name} TEXT NOT NULL,
        ${LocalBridgeContract.Inventory.Details} TEXT,
        ${LocalBridgeContract.Inventory.Price} TEXT,
        ${LocalBridgeContract.Inventory.LandingPrice} TEXT,
        ${LocalBridgeContract.Inventory.Batch} TEXT,
        ${LocalBridgeContract.Inventory.Sale} TEXT,
        ${LocalBridgeContract.Inventory.Rent} TEXT,
        ${LocalBridgeContract.Inventory.StockCount} INTEGER,
        ${LocalBridgeContract.Inventory.Status} TEXT,
        ${LocalBridgeContract.Inventory.Checkout} TEXT,
        ${LocalBridgeContract.Inventory.ImageUri} TEXT,
        ${LocalBridgeContract.Inventory.ImageBase64} TEXT,
        ${LocalBridgeContract.Inventory.QrCodeValue} TEXT,
        ${LocalBridgeContract.Inventory.UpdatedAt} TEXT
      )
      """.trimIndent(),
    )
    db.execSQL(
      """
      CREATE TABLE ${LocalBridgeContract.AccessRequests.Table} (
        ${LocalBridgeContract.AccessRequests.RequestId} TEXT PRIMARY KEY,
        ${LocalBridgeContract.AccessRequests.CustomerId} TEXT,
        ${LocalBridgeContract.AccessRequests.CustomerName} TEXT NOT NULL,
        ${LocalBridgeContract.AccessRequests.CustomerPhone} TEXT,
        ${LocalBridgeContract.AccessRequests.Status} TEXT NOT NULL,
        ${LocalBridgeContract.AccessRequests.ClientType} TEXT,
        ${LocalBridgeContract.AccessRequests.CreatedAt} TEXT,
        ${LocalBridgeContract.AccessRequests.UpdatedAt} TEXT,
        ${LocalBridgeContract.AccessRequests.Note} TEXT
      )
      """.trimIndent(),
    )
  }

  override fun onUpgrade(
    db: SQLiteDatabase,
    oldVersion: Int,
    newVersion: Int,
  ) {
    db.execSQL("DROP TABLE IF EXISTS ${LocalBridgeContract.Inventory.Table}")
    db.execSQL("DROP TABLE IF EXISTS ${LocalBridgeContract.AccessRequests.Table}")
    onCreate(db)
  }

  private companion object {
    const val DatabaseName = "ashokvatika_local_bridge.db"
    const val DatabaseVersion = 1
  }
}
