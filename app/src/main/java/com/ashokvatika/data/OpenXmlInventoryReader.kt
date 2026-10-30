package com.ashokvatika.data

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

/** Android-safe reader for the small subset of OOXML needed by vendor inventory sheets. */
internal object OpenXmlInventoryReader {
  private const val RelationshipNamespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"
  private const val MaxPartBytes = 20 * 1024 * 1024
  private const val MaxWorkbookBytes = 80 * 1024 * 1024

  fun read(stream: InputStream): List<SpreadsheetSheet> {
    val parts = unzip(stream)
    val workbook = xml(parts["xl/workbook.xml"] ?: error("This file is not an Excel workbook."))
    val workbookRels = relationships(parts, "xl/workbook.xml")
    val strings = parts["xl/sharedStrings.xml"]?.let { sharedStrings(xml(it)) }.orEmpty()
    return descendants(workbook.documentElement, "sheet").mapNotNull { sheetNode ->
      val name = sheetNode.getAttribute("name")
      val relationshipId = sheetNode.relationshipId("id")
      val path = workbookRels[relationshipId] ?: return@mapNotNull null
      val sheetBytes = parts[path] ?: return@mapNotNull null
      val document = xml(sheetBytes)
      SpreadsheetSheet(
        name,
        rows(document, strings),
        pictures(parts, path, document) + inCellPictures(parts, document),
      )
    }.also { require(it.isNotEmpty()) { "No readable worksheets were found." } }
  }

  private fun unzip(stream: InputStream): Map<String, ByteArray> {
    val parts = mutableMapOf<String, ByteArray>()
    var total = 0
    ZipInputStream(stream.buffered()).use { zip ->
      while (true) {
        val entry = zip.nextEntry ?: break
        if (entry.isDirectory) continue
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
          val count = zip.read(buffer)
          if (count < 0) break
          output.write(buffer, 0, count)
          require(output.size() <= MaxPartBytes) { "An Excel file part is too large." }
          total += count
          require(total <= MaxWorkbookBytes) { "The Excel workbook is too large." }
        }
        parts[entry.name.trimStart('/')] = output.toByteArray()
        zip.closeEntry()
      }
    }
    return parts
  }

  private fun xml(data: ByteArray): Document {
    val factory = DocumentBuilderFactory.newInstance().apply {
      isNamespaceAware = true
      isExpandEntityReferences = false
      runCatching { setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) }
      runCatching { setFeature("http://xml.org/sax/features/external-general-entities", false) }
      runCatching { setFeature("http://xml.org/sax/features/external-parameter-entities", false) }
    }
    return factory.newDocumentBuilder().parse(ByteArrayInputStream(data))
  }

  private fun relationships(parts: Map<String, ByteArray>, source: String): Map<String, String> {
    val directory = source.substringBeforeLast('/', "")
    val fileName = source.substringAfterLast('/')
    val relsPath = "$directory/_rels/$fileName.rels"
    val document = parts[relsPath]?.let(::xml) ?: return emptyMap()
    return descendants(document.documentElement, "Relationship").associate { relation ->
      relation.getAttribute("Id") to resolve(source, relation.getAttribute("Target"))
    }
  }

  private fun resolve(source: String, target: String): String {
    if (target.startsWith('/')) return target.trimStart('/')
    val segments = (source.substringBeforeLast('/') + "/" + target).split('/')
    val path = mutableListOf<String>()
    for (segment in segments) when (segment) {
      "", "." -> Unit
      ".." -> if (path.isNotEmpty()) path.removeAt(path.lastIndex)
      else -> path += segment
    }
    return path.joinToString("/")
  }

  private fun sharedStrings(document: Document): List<String> = descendants(document.documentElement, "si")
    .map { item -> descendants(item, "t").joinToString("") { it.textContent.orEmpty() } }

  private fun rows(document: Document, strings: List<String>): Map<Int, Map<Int, String>> {
    val result = mutableMapOf<Int, Map<Int, String>>()
    descendants(document.documentElement, "row").forEachIndexed { fallbackRow, row ->
      val rowIndex = row.getAttribute("r").toIntOrNull()?.minus(1) ?: fallbackRow
      val cells = mutableMapOf<Int, String>()
      descendants(row, "c").forEachIndexed { fallbackColumn, cell ->
        val column = columnIndex(cell.getAttribute("r")) ?: fallbackColumn
        val raw = descendants(cell, "v").firstOrNull()?.textContent.orEmpty()
        val value = when (cell.getAttribute("t")) {
          "s" -> strings.getOrNull(raw.toIntOrNull() ?: -1).orEmpty()
          "inlineStr" -> descendants(cell, "t").joinToString("") { it.textContent.orEmpty() }
          else -> raw
        }
        cells[column] = value
      }
      result[rowIndex] = cells
    }
    return result
  }

  private fun columnIndex(reference: String): Int? {
    val letters = reference.takeWhile(Char::isLetter).uppercase()
    if (letters.isEmpty()) return null
    return letters.fold(0) { index, char -> index * 26 + (char - 'A' + 1) } - 1
  }

  private fun pictures(parts: Map<String, ByteArray>, sheetPath: String, document: Document): Map<Int, ByteArray> {
    val drawingId = descendants(document.documentElement, "drawing").firstOrNull()?.relationshipId("id")
      ?: return emptyMap()
    val drawingPath = relationships(parts, sheetPath)[drawingId] ?: return emptyMap()
    val drawing = parts[drawingPath]?.let(::xml) ?: return emptyMap()
    val imageRels = relationships(parts, drawingPath)
    val result = mutableMapOf<Int, ByteArray>()
    val anchors = descendants(drawing.documentElement, "oneCellAnchor") +
      descendants(drawing.documentElement, "twoCellAnchor")
    for (anchor in anchors) {
      val row = descendants(anchor, "from").firstOrNull()?.let { from ->
        descendants(from, "row").firstOrNull()?.textContent?.toIntOrNull()
      } ?: continue
      val imageId = descendants(anchor, "blip").firstOrNull()?.relationshipId("embed") ?: continue
      val imagePath = imageRels[imageId] ?: continue
      parts[imagePath]?.let { result[row] = it }
    }
    return result
  }

  /** Reads modern Excel "Place in Cell" images, stored as rich values rather than drawings. */
  private fun inCellPictures(parts: Map<String, ByteArray>, sheet: Document): Map<Int, ByteArray> {
    val metadata = parts["xl/metadata.xml"]?.let(::xml) ?: return emptyMap()
    val richValues = parts["xl/richData/rdrichvalue.xml"]?.let(::xml) ?: return emptyMap()
    val richValueRels = parts["xl/richData/richValueRel.xml"]?.let(::xml) ?: return emptyMap()
    val rels = relationships(parts, "xl/richData/richValueRel.xml")

    val futureMetadata = descendants(metadata.documentElement, "futureMetadata")
      .firstOrNull { it.getAttribute("name") == "XLRICHVALUE" } ?: return emptyMap()
    val richValueIndexes = descendants(futureMetadata, "rvb")
      .mapNotNull { it.getAttribute("i").toIntOrNull() }
    val metadataValueIndexes = descendants(metadata.documentElement, "valueMetadata")
      .flatMap { descendants(it, "bk") }
      .mapNotNull { block -> descendants(block, "rc").firstOrNull()?.getAttribute("v")?.toIntOrNull() }
    val metadataToRichValue = metadataValueIndexes.mapIndexedNotNull { metadataIndex, valueIndex ->
      richValueIndexes.getOrNull(valueIndex)?.let { metadataIndex + 1 to it }
    }.toMap()
    val imageIndexes = descendants(richValues.documentElement, "rv").map { value ->
      descendants(value, "v").firstOrNull()?.textContent?.toIntOrNull()
    }
    val imageTargets = descendants(richValueRels.documentElement, "rel").mapNotNull { relation ->
      val id = relation.relationshipId("id")
      rels[id]
    }

    val result = mutableMapOf<Int, ByteArray>()
    for (cell in descendants(sheet.documentElement, "c")) {
      val metadataIndex = cell.getAttribute("vm").toIntOrNull() ?: continue
      val row = cell.getAttribute("r").dropWhile(Char::isLetter).toIntOrNull()?.minus(1) ?: continue
      val richValueIndex = metadataToRichValue[metadataIndex] ?: continue
      val imageIndex = imageIndexes.getOrNull(richValueIndex) ?: continue
      val imagePath = imageTargets.getOrNull(imageIndex) ?: continue
      parts[imagePath]?.let { result[row] = it }
    }
    return result
  }

  private fun Element.relationshipId(name: String): String =
    getAttributeNS(RelationshipNamespace, name).ifBlank { getAttribute("r:$name") }

  private fun descendants(node: Node, localName: String): List<Element> {
    val result = mutableListOf<Element>()
    val nodes = (node as? Element)?.getElementsByTagNameNS("*", localName) ?: return result
    for (index in 0 until nodes.length) (nodes.item(index) as? Element)?.let(result::add)
    return result
  }
}
