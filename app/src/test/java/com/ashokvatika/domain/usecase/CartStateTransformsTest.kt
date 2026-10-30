package com.ashokvatika.domain.usecase

import com.ashokvatika.domain.model.CartProductItem
import org.junit.Assert.assertEquals
import org.junit.Test

class CartStateTransformsTest {
  @Test
  fun duplicateNewItemsMergeIntoOneRow() {
    val plant = CartProductItem("plant-1", "Rose", "R", 100, 70, 1)

    val merged = emptyList<CartProductItem>().mergeCartItems(listOf(plant, plant.copy(quantity = 2)))

    assertEquals(1, merged.size)
    assertEquals(3, merged.single().quantity)
  }

  @Test
  fun existingItemsKeepTheirOrderWhenNewItemsAreAppended() {
    val first = CartProductItem("first", "Rose", "R", 100, 70, 1)
    val second = CartProductItem("second", "Tulip", "T", 80, 50, 1)

    val merged = listOf(first).mergeCartItems(listOf(second, first.copy(quantity = 2)))

    assertEquals(listOf("first", "second"), merged.map { it.id })
    assertEquals(3, merged.first().quantity)
  }
}
