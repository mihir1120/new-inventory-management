package com.ashokvatika.data

import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ashokvatika.domain.model.DashboardDestination
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class InventorySpreadsheetImporterDeviceTest {
  @Test fun importsVendorWorkbookWithPicturesOnAndroid() {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val context = instrumentation.targetContext
    val input = File(context.filesDir, "vendor_inventory_device_test.xlsx")
    instrumentation.context.assets.open("vendor_inventory_test.xlsx").use { source ->
      input.outputStream().use { output -> source.copyTo(output) }
    }
    try {
      val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", input)
      val preview = InventorySpreadsheetImporter(context).read(uri, DashboardDestination.Plants)
      assertEquals(3, preview.items.size)
      assertEquals(0, preview.skippedRows)
      assertTrue(preview.warnings.isEmpty())
      assertEquals(
        listOf(DashboardDestination.Plants, DashboardDestination.Pots, DashboardDestination.Supplies),
        preview.items.map { it.category },
      )
      assertTrue(preview.items.all { it.status == "incomplete" })
      preview.items.forEach { item ->
        assertNotNull(item.imageUri)
        context.contentResolver.openInputStream(android.net.Uri.parse(item.imageUri)).use { image ->
          assertTrue(image != null && image.read() >= 0)
        }
      }
      preview.items.forEach { item ->
        item.imageUri?.let { imageUri ->
          val fileName = android.net.Uri.parse(imageUri).lastPathSegment?.substringAfterLast('/')
          if (fileName != null) File(context.filesDir, "inventory_import_images/$fileName").delete()
        }
      }
    } finally {
      input.delete()
    }
  }

  @Test fun importsLegacyXlsAndCsvOnAndroid() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val xls = File(context.filesDir, "vendor_legacy_test.xls")
    HSSFWorkbook().use { workbook ->
      val sheet = workbook.createSheet("Products")
      sheet.createRow(0).apply {
        createCell(0).setCellValue("Product Name")
        createCell(1).setCellValue("Category")
        createCell(2).setCellValue("Quantity")
      }
      sheet.createRow(1).apply {
        createCell(0).setCellValue("Legacy Test Pot")
        createCell(1).setCellValue("Pots")
        createCell(2).setCellValue(7.0)
      }
      xls.outputStream().use(workbook::write)
    }
    val csv = File(context.filesDir, "vendor_csv_test.csv")
    csv.writeText("Product Name,Category,Quantity\nCSV Test Supply,Supplies,9\n")
    try {
      val importer = InventorySpreadsheetImporter(context)
      val xlsUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", xls)
      val csvUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", csv)
      val xlsItem = importer.read(xlsUri, DashboardDestination.Plants).items.single()
      val csvItem = importer.read(csvUri, DashboardDestination.Plants).items.single()
      assertEquals(DashboardDestination.Pots, xlsItem.category)
      assertEquals("7", xlsItem.batch)
      assertEquals(DashboardDestination.Supplies, csvItem.category)
      assertEquals("9", csvItem.batch)
    } finally {
      xls.delete()
      csv.delete()
    }
  }
}
