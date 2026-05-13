package com.ashokvatika.ui.admin
import android.content.Context
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
  val headerBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.letterhead_header)
  val footerBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.letterhead_footer)
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
  canvas: Canvas,
  deal: SavedDeal,
  issueDate: String,
  invoiceNumber: String,
  lineItems: List<InvoiceLineItem>,
  totalAmount: Int,
) {
  val orange = Color.parseColor("#F47C2C")
  val green = Color.parseColor("#39D267")
  val pink = Color.parseColor("#D889DE")
  val grey = Color.parseColor("#8A8A8A")
  val lineGrey = Color.parseColor("#D9D9D9")
  val black = Color.parseColor("#2C2C2C")
  val blue = Color.parseColor("#38A9F5")

  val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = orange
    textSize = 34f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
  }
  val nurseryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = green
    textSize = 21f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
  }
  val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = pink
    textSize = 10f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
  }
  val contactPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = grey
    textSize = 9f
    textAlign = Paint.Align.RIGHT
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
  }
  val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = blue
    textSize = 24f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
  }
  val metaLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
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
  val totalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = grey
    textSize = 16f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
  }
  val totalValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = grey
    textSize = 18f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
  }
  val rulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = lineGrey
    strokeWidth = 1f
  }

  canvas.drawText("Ashokvatika", 28f, 92f, logoPaint)
  canvas.drawText("Ashokvatika Plants Nursery", 332f, 36f, nurseryPaint)
  canvas.drawText("A SELECTION OF EXOTIC URBAN PLANTS & SUPPLIES", 565f, 54f, subHeaderPaint)
  canvas.drawText("Vallabhbhai Patel Road / Mahakali", 565f, 74f, contactPaint)
  canvas.drawText("Andheri - East / Mumbai", 565f, 92f, contactPaint)
  canvas.drawText("+91 78085 80855 / +91 981981 7780", 565f, 110f, contactPaint)
  canvas.drawText("ashokvatikanursery@gmail.com", 565f, 126f, contactPaint)

  canvas.drawLine(24f, 146f, 568f, 146f, rulePaint)

  canvas.drawText("INVOICE", 28f, 190f, sectionPaint)
  canvas.drawText("#  AVN", 28f, 238f, sectionPaint)

  canvas.drawText("CLIENT /", 176f, 180f, metaLabelPaint)
  canvas.drawText("ADDRESS /", 176f, 205f, metaLabelPaint)
  canvas.drawText("DATE /", 176f, 230f, metaLabelPaint)
  canvas.drawText("POS /", 176f, 255f, metaLabelPaint)

  canvas.drawText(deal.clientName.ifBlank { "Walk-in customer" }, 264f, 180f, bodyPaint)
  canvas.drawText("-", 264f, 205f, bodyPaint)
  canvas.drawText(invoiceNumber, 92f, 238f, bodyPaint)
  canvas.drawText(issueDate, 264f, 230f, bodyPaint)
  canvas.drawText(deal.transactionCode.ifBlank { "-" }, 264f, 255f, bodyPaint)

  val tableTop = 300f
  val descX = 28f
  val qtyX = 378f
  val rateX = 442f
  val totalX = 520f

  canvas.drawText("DESCRIPTION", descX, tableTop, metaLabelPaint)
  canvas.drawText("QNT", qtyX, tableTop, metaLabelPaint)
  canvas.drawText("@", rateX, tableTop, metaLabelPaint)
  canvas.drawText("TOTAL", totalX, tableTop, metaLabelPaint)

  var y = tableTop + 30f
  lineItems.forEach { item ->
    val wrappedLines = wrapText(item.description, bodyPaint, 300f)
    wrappedLines.forEachIndexed { index, line ->
      canvas.drawText(line, descX, y + index * 14f, bodyPaint)
    }
    canvas.drawText(item.quantity.toString(), qtyX, y, bodyPaint)
    canvas.drawText(formatInvoiceAmount(item.unitPrice), rateX, y, bodyPaint)
    canvas.drawText(formatInvoiceAmount(item.total), totalX, y, bodyPaint)
    y += max(28f, wrappedLines.size * 16f)
  }

  val grandTotalY = max(y + 18f, 510f)
  canvas.drawText("GRAND TOTAL : Rs.", 360f, grandTotalY, totalPaint)
  canvas.drawText(formatInvoiceAmount(totalAmount), 500f, grandTotalY, totalValuePaint)

  val wordsY = grandTotalY + 46f
  canvas.drawText("IN", 28f, wordsY, bodyBoldPaint)
  canvas.drawText("WORDS/", 28f, wordsY + 20f, bodyBoldPaint)
  canvas.drawText(numberToWords(totalAmount), 92f, wordsY + 4f, bodyPaint)

  val footerLineY = wordsY + 42f
  canvas.drawLine(24f, footerLineY, 568f, footerLineY, rulePaint)
  canvas.drawText("We at Ashokvatika, sincerely appreciate your business.", 28f, footerLineY + 44f, bodyPaint)
  canvas.drawText("THANKS & REGARDS,", 28f, footerLineY + 68f, bodyBoldPaint)
  canvas.drawText("VISHAKHA SHINDE,", 28f, footerLineY + 116f, bodyPaint)
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

