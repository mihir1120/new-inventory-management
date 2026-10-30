package com.ashokvatika.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.ashokvatika.domain.model.DashboardDestination
import com.ashokvatika.domain.model.InventoryItem
import org.apache.poi.hssf.usermodel.HSSFPicture
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.DataFormatter
import java.io.File
import java.io.InputStream
import java.util.Locale
import java.util.UUID

data class InventoryImportPreview(
  val items: List<InventoryItem>,
  val skippedRows: Int,
  val warnings: List<String>,
)

internal data class SpreadsheetSheet(
  val name: String,
  val rows: Map<Int, Map<Int, String>>,
  val pictures: Map<Int, ByteArray> = emptyMap(),
)

/** Reads vendor workbooks without changing inventory until the admin confirms the preview. */
class InventorySpreadsheetImporter(private val context: Context) {
  fun read(uri: Uri, defaultCategory: DashboardDestination): InventoryImportPreview {
    val displayName = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
      ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }.orEmpty()
    val mimeType = context.contentResolver.getType(uri).orEmpty()
    val sheets = context.contentResolver.openInputStream(uri)?.use { stream ->
      when {
        displayName.endsWith(".csv", true) || mimeType.contains("csv", true) ||
          mimeType.contains("comma-separated", true) -> listOf(readCsv(stream))
        displayName.endsWith(".xls", true) -> readXls(stream)
        else -> OpenXmlInventoryReader.read(stream)
      }
    } ?: throw IllegalArgumentException("The selected file could not be opened.")

    val items = mutableListOf<InventoryItem>()
    val warnings = mutableListOf<String>()
    var skipped = 0
    for (sheet in sheets) {
      if (sheet.name.equals("Categories", ignoreCase = true)) continue
      val header = sheet.rows.toSortedMap().entries.firstOrNull { (_, cells) ->
        cells.values.any { it.isNotBlank() }
      } ?: continue
      val headers = header.value.entries.associate { (index, value) -> normalize(value) to index }
      val nameColumn = column(headers, "product name", "item name", "plant name", "pot name", "supply name", "name", "product")
      if (nameColumn == null) {
        warnings += "${sheet.name}: no product name column; sheet skipped."
        continue
      }
      for ((rowIndex, row) in sheet.rows.toSortedMap()) {
        if (rowIndex <= header.key) continue
        fun value(vararg names: String): String = column(headers, *names)?.let { row[it]?.trim() }.orEmpty()
        val name = row[nameColumn]?.trim().orEmpty()
        if (name.isBlank()) {
          if (row.values.any { it.isNotBlank() }) skipped++
          continue
        }
        val categoryText = value("category", "type", "product category")
        val category = parseCategory(categoryText, sheet.name, defaultCategory)
        if (category == null) {
          warnings += "${sheet.name} row ${rowIndex + 1}: unknown category '$categoryText'; row skipped."
          skipped++
          continue
        }
        val price = number(value("price", "selling price", "mrp", "final price"), "0")
        val photo = sheet.pictures[rowIndex]?.let { savePicture(it, category) }
        val imageReference = value("photo", "image", "image url", "photo url", "image uri")
        val imageUri = photo ?: imageReference.takeIf { it.startsWith("content://") }
        if (photo == null && imageReference.isNotBlank() && imageUri == null) {
          warnings += "${sheet.name} row ${rowIndex + 1}: photo reference could not be imported; embed the image in the sheet."
        }
        items += InventoryItem(
          id = "vendor-upload-${category.name.lowercase(Locale.US)}-${UUID.randomUUID()}-${System.currentTimeMillis() + items.size}",
          category = category,
          name = name,
          batch = number(value("batch", "quantity", "qty", "stock", "stock count"), "0"),
          sale = number(value("sale", "sold"), "0"),
          rent = number(value("rent", "rented"), "0"),
          details = value("details", "description", "notes"),
          price = price,
          landingPrice = number(value("landing price", "cost price", "purchase price", "vendor price"), "0"),
          checkout = number(value("checkout", "checkout price"), ""),
          vendorName = value("vendor name", "vendor", "supplier name", "supplier"),
          vendorPhone = value("vendor phone", "vendor mobile", "supplier phone", "phone").filter(Char::isDigit),
          vendorEmail = value("vendor email", "supplier email", "email"),
          imageUri = imageUri,
          mediaUris = listOfNotNull(imageUri),
          status = if (price.toIntOrNull()?.let { it > 0 } == true) "complete" else "incomplete",
          templatePrimaryTitle = name,
        )
      }
    }
    return InventoryImportPreview(items, skipped, warnings)
  }

  private fun readXls(stream: InputStream): List<SpreadsheetSheet> = HSSFWorkbook(stream).use { workbook ->
    val formatter = DataFormatter(Locale.US)
    (0 until workbook.numberOfSheets).map { sheetIndex ->
      val sheet = workbook.getSheetAt(sheetIndex)
      val rows = (sheet.firstRowNum..sheet.lastRowNum).mapNotNull { rowIndex ->
        sheet.getRow(rowIndex)?.let { row ->
          rowIndex to (0 until row.lastCellNum.coerceAtLeast(0)).associateWith { column ->
            formatter.formatCellValue(row.getCell(column))
          }
        }
      }.toMap()
      val pictures = sheet.drawingPatriarch?.children.orEmpty().filterIsInstance<HSSFPicture>()
        .associate { it.clientAnchor.row1 to it.pictureData.data }
      SpreadsheetSheet(sheet.sheetName, rows, pictures)
    }
  }

  private fun readCsv(stream: InputStream): SpreadsheetSheet {
    val source = stream.bufferedReader().readText().removePrefix("\uFEFF")
    val rows = mutableMapOf<Int, Map<Int, String>>()
    val cells = mutableMapOf<Int, String>()
    val value = StringBuilder()
    var row = 0
    var column = 0
    var quoted = false
    var index = 0
    fun endCell() {
      cells[column++] = value.toString()
      value.clear()
    }
    fun endRow() {
      endCell()
      rows[row++] = cells.toMap()
      cells.clear()
      column = 0
    }
    while (index < source.length) {
      val char = source[index]
      when {
        char == '"' && quoted && source.getOrNull(index + 1) == '"' -> {
          value.append('"')
          index++
        }
        char == '"' -> quoted = !quoted
        char == ',' && !quoted -> endCell()
        (char == '\n' || char == '\r') && !quoted -> {
          if (char == '\r' && source.getOrNull(index + 1) == '\n') index++
          endRow()
        }
        else -> value.append(char)
      }
      index++
    }
    if (quoted) throw IllegalArgumentException("CSV has an unclosed quoted field.")
    if (value.isNotEmpty() || cells.isNotEmpty()) endRow()
    return SpreadsheetSheet("Products", rows)
  }

  private fun savePicture(data: ByteArray, category: DashboardDestination): String {
    require(data.size <= 15 * 1024 * 1024) { "An embedded photo exceeds 15 MB." }
    val directory = File(context.filesDir, "inventory_import_images").apply { mkdirs() }
    val file = File(directory, "${category.name.lowercase(Locale.US)}-${UUID.randomUUID()}.jpg")
    file.outputStream().use { it.write(data) }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file).toString()
  }

  private fun parseCategory(value: String, sheetName: String, fallback: DashboardDestination): DashboardDestination? {
    val label = (value.ifBlank { sheetName }).lowercase(Locale.US)
    return when {
      "plant" in label -> DashboardDestination.Plants
      "pot" in label -> DashboardDestination.Pots
      "suppl" in label || "supply" in label -> DashboardDestination.Supplies
      value.isBlank() -> fallback
      else -> null
    }
  }

  private fun number(value: String, fallback: String): String =
    value.replace(",", "").replace("₹", "").trim().toDoubleOrNull()
      ?.takeIf { it >= 0 && it <= Int.MAX_VALUE }
      ?.toInt()?.toString() ?: fallback

  private fun column(headers: Map<String, Int>, vararg labels: String): Int? =
    labels.firstNotNullOfOrNull { headers[normalize(it)] }

  private fun normalize(value: String): String = value.lowercase(Locale.US)
    .replace(Regex("[^a-z0-9]+"), " ").trim()
}
