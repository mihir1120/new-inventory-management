package com.ashokvatika.ui.admin
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.ashokvatika.app.R
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

private const val InvoicePageWidth = 596
private const val InvoicePageHeight = 842
private const val LetterheadPageWidth = 596
private const val LetterheadPageHeight = 842

private data class InvoiceLineItem(
  val description: String,
  val quantity: Int,
  val unitPrice: Int,
  val total: Int,
)

internal fun buildInvoicePdf(
  context: Context,
  deal: SavedDeal,
): ByteArray {
  val lineItems = deal.toInvoiceLineItems()
  val totalAmount = lineItems.sumOf { it.total }
  val issueDate = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())
  val invoiceNumber = buildInvoiceNumber(deal)

  val document = PdfDocument()
  val pageInfo = PdfDocument.PageInfo.Builder(InvoicePageWidth, InvoicePageHeight, 1).create()
  val page = document.startPage(pageInfo)
  val canvas = page.canvas
  canvas.drawColor(Color.WHITE)

  drawInvoice(
    context = context,
    canvas = canvas,
    deal = deal,
    issueDate = issueDate,
    invoiceNumber = invoiceNumber,
    lineItems = lineItems,
    totalAmount = totalAmount,
  )

  document.finishPage(page)
  return ByteArrayOutputStream().use { output ->
    document.writeTo(output)
    document.close()
    output.toByteArray()
  }
}

internal fun buildLetterheadPdf(
  context: Context,
  bodyText: String,
): ByteArray {
  val document = PdfDocument()
  val resources = context.applicationContext.resources
  val headerBitmap = PdfBitmapCache.letterheadHeader(resources)
  val footerBitmap = PdfBitmapCache.letterheadFooter(resources)
  val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#2C2C2C")
    textSize = 14f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
  }
  val bodyLeft = 52f
  val bodyRight = LetterheadPageWidth - 52f
  val bodyTop = 144f
  val bodyBottom = 718f
  val lineHeight = 22f
  val rawLines = wrapParagraphsForLetterhead(
    text = bodyText.ifBlank { " " },
    paint = bodyPaint,
    maxWidth = bodyRight - bodyLeft,
  )
  val linesPerPage = ((bodyBottom - bodyTop) / lineHeight).toInt().coerceAtLeast(1)
  val pages = rawLines.chunked(linesPerPage).ifEmpty { listOf(listOf("")) }

  pages.forEachIndexed { index, lines ->
    val pageInfo = PdfDocument.PageInfo.Builder(LetterheadPageWidth, LetterheadPageHeight, index + 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas
    canvas.drawColor(Color.WHITE)

    if (headerBitmap != null) {
      canvas.drawBitmap(
        headerBitmap,
        null,
        RectF(92f, 18f, 504f, 112f),
        null,
      )
    }
    if (footerBitmap != null) {
      canvas.drawBitmap(
        footerBitmap,
        null,
        RectF(86f, 752f, 510f, 812f),
        null,
      )
    }

    var y = bodyTop
    lines.forEach { line ->
      canvas.drawText(line, bodyLeft, y, bodyPaint)
      y += lineHeight
    }

    document.finishPage(page)
  }

  return ByteArrayOutputStream().use { output ->
    document.writeTo(output)
    document.close()
    output.toByteArray()
  }
}

private fun drawInvoice(
  context: Context,
  canvas: Canvas,
  deal: SavedDeal,
  issueDate: String,
  invoiceNumber: String,
  lineItems: List<InvoiceLineItem>,
  totalAmount: Int,
) {
  val pageLeft = 24f
  val pageRight = InvoicePageWidth - 24f
  val tableLeft = pageLeft
  val tableRight = pageRight
  val descriptionRight = 380f
  val quantityRight = 438f
  val unitPriceRight = 500f
  val orange = Color.parseColor("#F47C2C")
  val green = Color.parseColor("#33C86B")
  val pink = Color.parseColor("#C98CCB")
  val grey = Color.parseColor("#8A8A8A")
  val lightGrey = Color.parseColor("#D8D8D8")
  val black = Color.parseColor("#2C2C2C")
  val blue = Color.parseColor("#38A9F5")
  val logoBitmap = PdfBitmapCache.invoiceLogo(context.applicationContext.resources)

  val rulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = lightGrey
    strokeWidth = 1f
  }
  val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = orange
    textSize = 30f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
  }
  val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = green
    textSize = 18f
    textAlign = Paint.Align.RIGHT
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
  }
  val subBrandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = pink
    textSize = 8.8f
    textAlign = Paint.Align.RIGHT
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
  }
  val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = grey
    textSize = 8.8f
    textAlign = Paint.Align.RIGHT
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
  }
  val invoiceTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = blue
    textSize = 26f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
  }
  val invoiceIdPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = blue
    textSize = 24f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
  }
  val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = black
    textSize = 10.8f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
  }
  val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = grey
    textSize = 11f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
  }
  val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = black
    textSize = 11f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
  }
  val bodyBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = black
    textSize = 11f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
  }
  val amountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = black
    textSize = 11f
    textAlign = Paint.Align.RIGHT
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
  }
  val centeredAmountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = black
    textSize = 11f
    textAlign = Paint.Align.CENTER
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
  }
  val totalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = grey
    textSize = 16f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
  }
  val totalValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = grey
    textSize = 16f
    textAlign = Paint.Align.RIGHT
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
  }
  if (logoBitmap != null) {
    canvas.drawBitmap(logoBitmap, null, RectF(18f, 44f, 188f, 106f), null)
  } else {
    canvas.drawText("Ashokvatika", 18f, 92f, logoPaint)
  }

  canvas.drawText("Ashokvatika Plants Nursery", pageRight, 48f, brandPaint)
  canvas.drawText("A SELECTION OF EXOTIC URBAN PLANTS & SUPPLIES", pageRight, 67f, subBrandPaint)
  canvas.drawText("Vallabhbhai Patel Road / Mahakali", pageRight, 87f, headerPaint)
  canvas.drawText("Andheri - East / Mumbai", pageRight, 106f, headerPaint)
  canvas.drawText("+91 78085 80855 / +91 981981 7780", pageRight, 125f, headerPaint)
  canvas.drawText("ashokvatianursery@gmail.com", pageRight, 144f, headerPaint)

  canvas.drawLine(pageLeft, 170f, pageRight, 170f, rulePaint)

  canvas.drawText("INVOICE", pageLeft, 222f, invoiceTitlePaint)
  canvas.drawText("#", pageLeft, 308f, invoiceIdPaint)
  canvas.drawText("AV", 58f, 308f, invoiceIdPaint)
  canvas.drawText(invoiceNumber, 126f, 306f, bodyPaint)

  val labelX = 232f
  val valueX = 292f
  canvas.drawText("CLIENT /", labelX, 212f, labelPaint)
  canvas.drawText("ADDRESS /", labelX, 246f, labelPaint)
  canvas.drawText("DATE /", labelX, 280f, labelPaint)
  canvas.drawText("POS /", labelX, 314f, labelPaint)
  canvas.drawText(deal.clientName.ifBlank { "Walk-in customer" }, valueX, 212f, bodyPaint)
  canvas.drawText("-", valueX, 246f, bodyPaint)
  canvas.drawText(issueDate, valueX, 280f, bodyPaint)
  canvas.drawText(deal.transactionCode.ifBlank { "-" }, valueX, 314f, bodyPaint)

  val tableTop = 360f
  val tableHeaderBottom = 404f
  var y = tableHeaderBottom + 30f
  val rowHeights = lineItems.map { item ->
    val wrappedLines = wrapText(item.description, bodyPaint, descriptionRight - tableLeft - 18f)
    wrappedLines to max(34f, wrappedLines.size * 18f)
  }
  val rowsHeight = rowHeights.sumOf { it.second.toInt() }.toFloat()
  val tableBottom = max(tableHeaderBottom + 140f, tableHeaderBottom + rowsHeight + 24f)

  canvas.drawLine(tableLeft, tableTop, tableRight, tableTop, rulePaint)
  canvas.drawLine(tableLeft, tableHeaderBottom, tableRight, tableHeaderBottom, rulePaint)
  canvas.drawLine(tableLeft, tableBottom, tableRight, tableBottom, rulePaint)
  canvas.drawLine(tableLeft, tableTop, tableLeft, tableBottom, rulePaint)
  canvas.drawLine(descriptionRight, tableTop, descriptionRight, tableBottom, rulePaint)
  canvas.drawLine(quantityRight, tableTop, quantityRight, tableBottom, rulePaint)
  canvas.drawLine(unitPriceRight, tableTop, unitPriceRight, tableBottom, rulePaint)
  canvas.drawLine(tableRight, tableTop, tableRight, tableBottom, rulePaint)

  canvas.drawText("DESCRIPTION", tableLeft + 8f, 388f, tableHeaderPaint)
  canvas.drawText("QNT", 392f, 388f, tableHeaderPaint)
  canvas.drawText("@", 466f, 388f, tableHeaderPaint)
  canvas.drawText("TOTAL", 524f, 388f, tableHeaderPaint)

  rowHeights.forEachIndexed { index, (wrappedLines, rowHeight) ->
    wrappedLines.forEachIndexed { lineIndex, line ->
      canvas.drawText(line, tableLeft + 8f, y + lineIndex * 14f, bodyPaint)
    }
    canvas.drawText(lineItems[index].quantity.toString(), 408f, y, centeredAmountPaint)
    canvas.drawText(formatInvoiceAmount(lineItems[index].unitPrice), 474f, y, amountPaint)
    canvas.drawText(formatInvoiceAmount(lineItems[index].total), 548f, y, amountPaint)
    y += rowHeight
    canvas.drawLine(tableLeft, y - 12f, tableRight, y - 12f, rulePaint)
  }

  val totalY = tableBottom + 50f
  canvas.drawText("GRAND TOTAL : ₹", 356f, totalY, totalLabelPaint)
  canvas.drawText(formatInvoiceAmount(totalAmount), 548f, totalY, totalValuePaint)

  val wordsY = totalY + 68f
  canvas.drawText("IN WORDS/", pageLeft, wordsY, bodyBoldPaint)
  canvas.drawText(numberToWords(totalAmount), 170f, wordsY, bodyPaint)

  val footerLineY = wordsY + 34f
  canvas.drawLine(pageLeft, footerLineY, pageRight, footerLineY, rulePaint)
  canvas.drawText("We at Ashokvatika, sincerely appreciate your business.", pageLeft, footerLineY + 44f, bodyPaint)
  canvas.drawText("THANKS & REGARDS,", pageLeft, footerLineY + 68f, bodyPaint)
  canvas.drawText("VISHAKHA SHINDE,", pageLeft, footerLineY + 126f, bodyPaint)
}

private fun SavedDeal.toInvoiceLineItems(): List<InvoiceLineItem> {
  val lineItems = mutableListOf<InvoiceLineItem>()
  rows.forEach { row ->
    row.items.forEach { item ->
      lineItems += InvoiceLineItem(
        description = item.name,
        quantity = item.quantity,
        unitPrice = item.mrp,
        total = item.mrp * item.quantity,
      )
    }
    val adjustment = row.invoiceDisplayAmount() - row.invoiceBaseAmount()
    if (adjustment != 0) {
      lineItems += InvoiceLineItem(
        description = "${row.title.ifBlank { "Deal" }} adjustment",
        quantity = 1,
        unitPrice = adjustment,
        total = adjustment,
      )
    }
  }
  return lineItems.ifEmpty {
    listOf(
      InvoiceLineItem(
        description = title.ifBlank { "Closed deal" },
        quantity = 1,
        unitPrice = rows.sumOf { it.invoiceDisplayAmount() },
        total = rows.sumOf { it.invoiceDisplayAmount() },
      ),
    )
  }
}

private fun OpenCartRow.invoiceBaseAmount(): Int {
  return items.sumOf { it.mrp * it.quantity }
}

private fun OpenCartRow.invoiceDisplayAmount(): Int {
  return customAmount ?: invoiceBaseAmount()
}

private fun buildInvoiceNumber(deal: SavedDeal): String {
  val trailingDigits = deal.id.takeLastWhile(Char::isDigit)
  val source = trailingDigits.takeLast(6).ifBlank { deal.transactionCode.takeLast(6) }
  return source.ifBlank { "000001" }
}

private fun wrapParagraphsForLetterhead(
  text: String,
  paint: Paint,
  maxWidth: Float,
): List<String> {
  if (text.isBlank()) return listOf("")
  val lines = mutableListOf<String>()
  text.replace("\r\n", "\n").split('\n').forEachIndexed { index, paragraph ->
    if (paragraph.isBlank()) {
      lines += ""
    } else {
      lines += wrapText(paragraph, paint, maxWidth)
    }
    if (index != text.replace("\r\n", "\n").split('\n').lastIndex && paragraph.isNotBlank()) {
      // Keep explicit line breaks between paragraphs without adding excess spacing.
      lines += ""
    }
  }
  return lines
}

private fun wrapText(
  text: String,
  paint: Paint,
  maxWidth: Float,
): List<String> {
  if (text.isBlank()) return listOf("-")
  val result = mutableListOf<String>()
  var remaining = text.trim()
  while (remaining.isNotEmpty()) {
    val count = paint.breakText(remaining, true, maxWidth, null)
    if (count <= 0) break
    var line = remaining.take(count)
    if (count < remaining.length) {
      val breakIndex = line.lastIndexOf(' ')
      if (breakIndex > 8) {
        line = line.substring(0, breakIndex)
      }
    }
    result += line.trim()
    remaining = remaining.removePrefix(line).trimStart()
  }
  return result.ifEmpty { listOf(text) }
}

private fun formatInvoiceAmount(value: Int): String {
  return NumberFormat.getIntegerInstance(Locale.US).format(value)
}

private fun numberToWords(value: Int): String {
  if (value == 0) return "Zero only"
  if (value < 0) return "Minus ${numberToWords(-value).removeSuffix(" only")} only"

  val units = arrayOf(
    "", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
    "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen",
  )
  val tens = arrayOf("", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety")

  fun belowThousand(number: Int): String {
    val hundred = number / 100
    val remainder = number % 100
    val parts = mutableListOf<String>()
    if (hundred > 0) parts += "${units[hundred]} hundred"
    if (remainder > 0) {
      parts += if (remainder < 20) {
        units[remainder]
      } else {
        listOf(tens[remainder / 10], units[remainder % 10]).filter { it.isNotBlank() }.joinToString(" ")
      }
    }
    return parts.joinToString(" ")
  }

  val scales = listOf(
    1_000_000_000 to "billion",
    1_000_000 to "million",
    1_000 to "thousand",
  )

  var remaining = value
  val parts = mutableListOf<String>()
  scales.forEach { (scaleValue, label) ->
    if (remaining >= scaleValue) {
      parts += "${belowThousand(remaining / scaleValue)} $label"
      remaining %= scaleValue
    }
  }
  if (remaining > 0) parts += belowThousand(remaining)

  return parts.joinToString(" ").replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
  } + " only"
}

private object PdfBitmapCache {
  @Volatile
  private var cachedHeader: Bitmap? = null

  @Volatile
  private var cachedFooter: Bitmap? = null

  @Volatile
  private var cachedLogo: Bitmap? = null

  fun letterheadHeader(resources: Resources): Bitmap? {
    return cachedHeader ?: synchronized(this) {
      cachedHeader ?: BitmapFactory.decodeResource(resources, R.drawable.letterhead_header).also {
        cachedHeader = it
      }
    }
  }

  fun letterheadFooter(resources: Resources): Bitmap? {
    return cachedFooter ?: synchronized(this) {
      cachedFooter ?: BitmapFactory.decodeResource(resources, R.drawable.letterhead_footer).also {
        cachedFooter = it
      }
    }
  }

  fun invoiceLogo(resources: Resources): Bitmap? {
    return cachedLogo ?: synchronized(this) {
      cachedLogo ?: BitmapFactory.decodeResource(resources, R.drawable.av_logo_exact).also {
        cachedLogo = it
      }
    }
  }
}

