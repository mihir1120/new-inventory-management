package com.ashokvatika.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenXmlInventoryReaderTest {
  @Test fun readsVendorTemplateAndEmbeddedImages() {
    val stream = requireNotNull(javaClass.classLoader?.getResourceAsStream("vendor_inventory_test.xlsx"))
    val sheets = stream.use(OpenXmlInventoryReader::read)

    assertEquals(listOf("Products", "Categories"), sheets.map { it.name })
    val products = sheets.first()
    assertEquals("Product Name", products.rows[0]?.get(0))
    assertEquals("Test Money Plant", products.rows[1]?.get(0))
    assertEquals("Plants", products.rows[1]?.get(1))
    assertEquals("12", products.rows[1]?.get(2))
    assertEquals("Test Terracotta Pot", products.rows[2]?.get(0))
    assertEquals("Supplies", products.rows[3]?.get(1))
    assertEquals(setOf(1, 2, 3), products.pictures.keys)
    assertTrue(products.pictures.values.all { it.isNotEmpty() })
    assertFalse(sheets.last().rows.isEmpty())
  }
}
