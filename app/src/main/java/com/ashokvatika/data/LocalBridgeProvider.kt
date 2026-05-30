package com.ashokvatika.data

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri

class LocalBridgeProvider : ContentProvider() {
  private lateinit var databaseHelper: LocalBridgeDatabaseHelper

  override fun onCreate(): Boolean {
    val appContext = context?.applicationContext ?: return false
    databaseHelper = LocalBridgeDatabaseHelper(appContext)
    return true
  }

  override fun query(
    uri: Uri,
    projection: Array<out String>?,
    selection: String?,
    selectionArgs: Array<out String>?,
    sortOrder: String?,
  ): Cursor? {
    val database = databaseHelper.readableDatabase
    val (table, resolvedSelection, resolvedArgs, notifyUri, defaultSortOrder) = resolveQuery(uri, selection, selectionArgs)
    return database.query(
      table,
      projection,
      resolvedSelection,
      resolvedArgs,
      null,
      null,
      sortOrder ?: defaultSortOrder,
    ).apply {
      setNotificationUri(context?.contentResolver, notifyUri)
    }
  }

  override fun getType(uri: Uri): String {
    return when (uriMatcher.match(uri)) {
      InventoryDir -> "vnd.android.cursor.dir/${LocalBridgeContract.Authority}.${LocalBridgeContract.Inventory.Table}"
      InventoryItem -> "vnd.android.cursor.item/${LocalBridgeContract.Authority}.${LocalBridgeContract.Inventory.Table}"
      AccessDir -> "vnd.android.cursor.dir/${LocalBridgeContract.Authority}.${LocalBridgeContract.AccessRequests.Table}"
      AccessItem -> "vnd.android.cursor.item/${LocalBridgeContract.Authority}.${LocalBridgeContract.AccessRequests.Table}"
      LiveSessionsDir -> "vnd.android.cursor.dir/${LocalBridgeContract.Authority}.${LocalBridgeContract.LiveSessions.Table}"
      LiveSessionsItem -> "vnd.android.cursor.item/${LocalBridgeContract.Authority}.${LocalBridgeContract.LiveSessions.Table}"
      else -> throw IllegalArgumentException("Unsupported uri: $uri")
    }
  }

  override fun insert(
    uri: Uri,
    values: ContentValues?,
  ): Uri {
    val database = databaseHelper.writableDatabase
    val (table, itemKey, keyColumn, directoryUri) = resolveInsertTarget(uri)
    val safeValues = ContentValues(values ?: ContentValues())
    val itemId = safeValues.getAsString(keyColumn)
      ?: itemKey
      ?: throw IllegalArgumentException("Missing primary key for $uri")
    safeValues.put(keyColumn, itemId)

    database.insertWithOnConflict(table, null, safeValues, SQLiteDatabase.CONFLICT_REPLACE)
    val itemUri = directoryUri.buildUpon().appendPath(itemId).build()
    notifyChange(directoryUri, itemUri)
    return itemUri
  }

  override fun update(
    uri: Uri,
    values: ContentValues?,
    selection: String?,
    selectionArgs: Array<out String>?,
  ): Int {
    val database = databaseHelper.writableDatabase
    val (table, keyColumn, itemId, directoryUri) = resolveUpdateTarget(uri)
    val affectedRows = if (itemId == null) {
      database.update(table, values, selection, selectionArgs)
    } else {
      database.update(table, values, "$keyColumn = ?", arrayOf(itemId))
    }
    if (affectedRows > 0) {
      val itemUri = itemId?.let { directoryUri.buildUpon().appendPath(it).build() }
      notifyChange(directoryUri, itemUri)
    }
    return affectedRows
  }

  override fun delete(
    uri: Uri,
    selection: String?,
    selectionArgs: Array<out String>?,
  ): Int {
    val database = databaseHelper.writableDatabase
    val (table, keyColumn, itemId, directoryUri) = resolveUpdateTarget(uri)
    val affectedRows = if (itemId == null) {
      database.delete(table, selection, selectionArgs)
    } else {
      database.delete(table, "$keyColumn = ?", arrayOf(itemId))
    }
    if (affectedRows > 0) {
      val itemUri = itemId?.let { directoryUri.buildUpon().appendPath(it).build() }
      notifyChange(directoryUri, itemUri)
    }
    return affectedRows
  }

  private fun resolveQuery(
    uri: Uri,
    selection: String?,
    selectionArgs: Array<out String>?,
  ): QueryTarget {
    return when (uriMatcher.match(uri)) {
      InventoryDir -> QueryTarget(
        table = LocalBridgeContract.Inventory.Table,
        selection = selection,
        selectionArgs = selectionArgs,
        notifyUri = LocalBridgeContract.Inventory.ContentUri,
        defaultSortOrder = "${LocalBridgeContract.Inventory.UpdatedAt} DESC",
      )

      InventoryItem -> QueryTarget(
        table = LocalBridgeContract.Inventory.Table,
        selection = "${LocalBridgeContract.Inventory.Id} = ?",
        selectionArgs = arrayOf(requireLastPathSegment(uri)),
        notifyUri = uri,
        defaultSortOrder = null,
      )

      AccessDir -> QueryTarget(
        table = LocalBridgeContract.AccessRequests.Table,
        selection = selection,
        selectionArgs = selectionArgs,
        notifyUri = LocalBridgeContract.AccessRequests.ContentUri,
        defaultSortOrder = "${LocalBridgeContract.AccessRequests.CreatedAt} DESC",
      )

      AccessItem -> QueryTarget(
        table = LocalBridgeContract.AccessRequests.Table,
        selection = "${LocalBridgeContract.AccessRequests.RequestId} = ?",
        selectionArgs = arrayOf(requireLastPathSegment(uri)),
        notifyUri = uri,
        defaultSortOrder = null,
      )

      LiveSessionsDir -> QueryTarget(
        table = LocalBridgeContract.LiveSessions.Table,
        selection = selection,
        selectionArgs = selectionArgs,
        notifyUri = LocalBridgeContract.LiveSessions.ContentUri,
        defaultSortOrder = "${LocalBridgeContract.LiveSessions.UpdatedAt} DESC",
      )

      LiveSessionsItem -> QueryTarget(
        table = LocalBridgeContract.LiveSessions.Table,
        selection = "${LocalBridgeContract.LiveSessions.SessionId} = ?",
        selectionArgs = arrayOf(requireLastPathSegment(uri)),
        notifyUri = uri,
        defaultSortOrder = null,
      )

      else -> throw IllegalArgumentException("Unsupported uri: $uri")
    }
  }

  private fun resolveInsertTarget(uri: Uri): InsertTarget {
    return when (uriMatcher.match(uri)) {
      InventoryDir -> InsertTarget(
        table = LocalBridgeContract.Inventory.Table,
        itemKey = null,
        keyColumn = LocalBridgeContract.Inventory.Id,
        directoryUri = LocalBridgeContract.Inventory.ContentUri,
      )

      InventoryItem -> InsertTarget(
        table = LocalBridgeContract.Inventory.Table,
        itemKey = requireLastPathSegment(uri),
        keyColumn = LocalBridgeContract.Inventory.Id,
        directoryUri = LocalBridgeContract.Inventory.ContentUri,
      )

      AccessDir -> InsertTarget(
        table = LocalBridgeContract.AccessRequests.Table,
        itemKey = null,
        keyColumn = LocalBridgeContract.AccessRequests.RequestId,
        directoryUri = LocalBridgeContract.AccessRequests.ContentUri,
      )

      AccessItem -> InsertTarget(
        table = LocalBridgeContract.AccessRequests.Table,
        itemKey = requireLastPathSegment(uri),
        keyColumn = LocalBridgeContract.AccessRequests.RequestId,
        directoryUri = LocalBridgeContract.AccessRequests.ContentUri,
      )

      LiveSessionsDir -> InsertTarget(
        table = LocalBridgeContract.LiveSessions.Table,
        itemKey = null,
        keyColumn = LocalBridgeContract.LiveSessions.SessionId,
        directoryUri = LocalBridgeContract.LiveSessions.ContentUri,
      )

      LiveSessionsItem -> InsertTarget(
        table = LocalBridgeContract.LiveSessions.Table,
        itemKey = requireLastPathSegment(uri),
        keyColumn = LocalBridgeContract.LiveSessions.SessionId,
        directoryUri = LocalBridgeContract.LiveSessions.ContentUri,
      )

      else -> throw IllegalArgumentException("Unsupported uri: $uri")
    }
  }

  private fun resolveUpdateTarget(uri: Uri): UpdateTarget {
    return when (uriMatcher.match(uri)) {
      InventoryDir -> UpdateTarget(
        table = LocalBridgeContract.Inventory.Table,
        keyColumn = LocalBridgeContract.Inventory.Id,
        itemId = null,
        directoryUri = LocalBridgeContract.Inventory.ContentUri,
      )

      InventoryItem -> UpdateTarget(
        table = LocalBridgeContract.Inventory.Table,
        keyColumn = LocalBridgeContract.Inventory.Id,
        itemId = requireLastPathSegment(uri),
        directoryUri = LocalBridgeContract.Inventory.ContentUri,
      )

      AccessDir -> UpdateTarget(
        table = LocalBridgeContract.AccessRequests.Table,
        keyColumn = LocalBridgeContract.AccessRequests.RequestId,
        itemId = null,
        directoryUri = LocalBridgeContract.AccessRequests.ContentUri,
      )

      AccessItem -> UpdateTarget(
        table = LocalBridgeContract.AccessRequests.Table,
        keyColumn = LocalBridgeContract.AccessRequests.RequestId,
        itemId = requireLastPathSegment(uri),
        directoryUri = LocalBridgeContract.AccessRequests.ContentUri,
      )

      LiveSessionsDir -> UpdateTarget(
        table = LocalBridgeContract.LiveSessions.Table,
        keyColumn = LocalBridgeContract.LiveSessions.SessionId,
        itemId = null,
        directoryUri = LocalBridgeContract.LiveSessions.ContentUri,
      )

      LiveSessionsItem -> UpdateTarget(
        table = LocalBridgeContract.LiveSessions.Table,
        keyColumn = LocalBridgeContract.LiveSessions.SessionId,
        itemId = requireLastPathSegment(uri),
        directoryUri = LocalBridgeContract.LiveSessions.ContentUri,
      )

      else -> throw IllegalArgumentException("Unsupported uri: $uri")
    }
  }

  private fun requireLastPathSegment(uri: Uri): String {
    return uri.lastPathSegment ?: throw IllegalArgumentException("Missing id for $uri")
  }

  private fun notifyChange(
    directoryUri: Uri,
    itemUri: Uri?,
  ) {
    val resolver = context?.contentResolver ?: return
    resolver.notifyChange(directoryUri, null)
    itemUri?.let { resolver.notifyChange(it, null) }
  }

  private data class QueryTarget(
    val table: String,
    val selection: String?,
    val selectionArgs: Array<out String>?,
    val notifyUri: Uri,
    val defaultSortOrder: String?,
  )

  private data class InsertTarget(
    val table: String,
    val itemKey: String?,
    val keyColumn: String,
    val directoryUri: Uri,
  )

  private data class UpdateTarget(
    val table: String,
    val keyColumn: String,
    val itemId: String?,
    val directoryUri: Uri,
  )

  private companion object {
    const val InventoryDir = 100
    const val InventoryItem = 101
    const val AccessDir = 200
    const val AccessItem = 201
    const val LiveSessionsDir = 300
    const val LiveSessionsItem = 301

    val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
      addURI(LocalBridgeContract.Authority, LocalBridgeContract.Inventory.Table, InventoryDir)
      addURI(LocalBridgeContract.Authority, "${LocalBridgeContract.Inventory.Table}/*", InventoryItem)
      addURI(LocalBridgeContract.Authority, LocalBridgeContract.AccessRequests.Table, AccessDir)
      addURI(LocalBridgeContract.Authority, "${LocalBridgeContract.AccessRequests.Table}/*", AccessItem)
      addURI(LocalBridgeContract.Authority, LocalBridgeContract.LiveSessions.Table, LiveSessionsDir)
      addURI(LocalBridgeContract.Authority, "${LocalBridgeContract.LiveSessions.Table}/*", LiveSessionsItem)
    }
  }
}
