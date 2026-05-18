package com.ashokvatika.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LocalBridgeDatabaseHelper(
  context: Context,
) : SQLiteOpenHelper(context, DatabaseName, null, DatabaseVersion) {
  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(createInventoryTableSql())
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

  override fun onOpen(db: SQLiteDatabase) {
    super.onOpen(db)
    ensureAccessRequestTable(db)
    ensureInventoryColumns(db)
  }

  override fun onUpgrade(
    db: SQLiteDatabase,
    oldVersion: Int,
    newVersion: Int,
  ) {
    if (oldVersion < 2) {
      ensureInventoryColumns(db)
    }
    if (oldVersion < 3) {
      ensureAccessRequestTable(db)
    }
  }

  private fun ensureAccessRequestTable(db: SQLiteDatabase) {
    db.execSQL(
      """
      CREATE TABLE IF NOT EXISTS ${LocalBridgeContract.AccessRequests.Table} (
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

  private fun ensureInventoryColumns(db: SQLiteDatabase) {
    addColumnIfMissing(
      db,
      LocalBridgeContract.Inventory.Table,
      LocalBridgeContract.Inventory.VendorName,
    )
    addColumnIfMissing(
      db,
      LocalBridgeContract.Inventory.Table,
      LocalBridgeContract.Inventory.VendorPhone,
    )
    addColumnIfMissing(
      db,
      LocalBridgeContract.Inventory.Table,
      LocalBridgeContract.Inventory.VendorEmail,
    )
    addColumnIfMissing(
      db,
      LocalBridgeContract.Inventory.Table,
      LocalBridgeContract.Inventory.TemplatePrimaryTitle,
    )
    addColumnIfMissing(
      db,
      LocalBridgeContract.Inventory.Table,
      LocalBridgeContract.Inventory.TemplatePrimaryDescription,
    )
    addColumnIfMissing(
      db,
      LocalBridgeContract.Inventory.Table,
      LocalBridgeContract.Inventory.TemplateSecondaryTitle,
    )
    addColumnIfMissing(
      db,
      LocalBridgeContract.Inventory.Table,
      LocalBridgeContract.Inventory.TemplateSecondaryDescription,
    )
    addColumnIfMissing(
      db,
      LocalBridgeContract.Inventory.Table,
      LocalBridgeContract.Inventory.TemplateContact,
    )
  }

  private fun addColumnIfMissing(
    db: SQLiteDatabase,
    tableName: String,
    columnName: String,
  ) {
    db.rawQuery("PRAGMA table_info($tableName)", null).use { cursor ->
      val nameColumnIndex = cursor.getColumnIndex("name")
      while (cursor.moveToNext()) {
        if (cursor.getString(nameColumnIndex) == columnName) {
          return
        }
      }
    }

    db.execSQL("ALTER TABLE $tableName ADD COLUMN $columnName TEXT")
  }

  private fun createInventoryTableSql(): String {
    return """
      CREATE TABLE ${LocalBridgeContract.Inventory.Table} (
        ${LocalBridgeContract.Inventory.Id} TEXT PRIMARY KEY,
        ${LocalBridgeContract.Inventory.Category} TEXT NOT NULL,
        ${LocalBridgeContract.Inventory.Name} TEXT NOT NULL,
        ${LocalBridgeContract.Inventory.Details} TEXT,
        ${LocalBridgeContract.Inventory.Price} TEXT,
        ${LocalBridgeContract.Inventory.LandingPrice} TEXT,
        ${LocalBridgeContract.Inventory.VendorName} TEXT,
        ${LocalBridgeContract.Inventory.VendorPhone} TEXT,
        ${LocalBridgeContract.Inventory.VendorEmail} TEXT,
        ${LocalBridgeContract.Inventory.Batch} TEXT,
        ${LocalBridgeContract.Inventory.Sale} TEXT,
        ${LocalBridgeContract.Inventory.Rent} TEXT,
        ${LocalBridgeContract.Inventory.StockCount} INTEGER,
        ${LocalBridgeContract.Inventory.Status} TEXT,
        ${LocalBridgeContract.Inventory.Checkout} TEXT,
        ${LocalBridgeContract.Inventory.ImageUri} TEXT,
        ${LocalBridgeContract.Inventory.ImageBase64} TEXT,
        ${LocalBridgeContract.Inventory.QrCodeValue} TEXT,
        ${LocalBridgeContract.Inventory.TemplatePrimaryTitle} TEXT,
        ${LocalBridgeContract.Inventory.TemplatePrimaryDescription} TEXT,
        ${LocalBridgeContract.Inventory.TemplateSecondaryTitle} TEXT,
        ${LocalBridgeContract.Inventory.TemplateSecondaryDescription} TEXT,
        ${LocalBridgeContract.Inventory.TemplateContact} TEXT,
        ${LocalBridgeContract.Inventory.UpdatedAt} TEXT
      )
    """.trimIndent()
  }

  private companion object {
    const val DatabaseName = "ashokvatika_local_bridge.db"
    const val DatabaseVersion = 3
  }
}
