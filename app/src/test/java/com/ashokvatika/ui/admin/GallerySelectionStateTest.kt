package com.ashokvatika.ui.admin

import com.ashokvatika.config.GalleryShareSheetVariant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GallerySelectionStateTest {
  @Test
  fun toggleAddsFirstImageAndOpensSheet() {
    val next = applyGallerySelectionToggle(
      currentSelection = emptyList(),
      wasSheetOpen = false,
      uriString = "image-1",
      variant = GalleryShareSheetVariant.AutoOpen,
    )

    assertEquals(listOf("image-1"), next.selectedImages)
    assertTrue(next.isSheetOpen)
  }

  @Test
  fun toggleRemovesLastImageAndClosesSheet() {
    val next = applyGallerySelectionToggle(
      currentSelection = listOf("image-1"),
      wasSheetOpen = true,
      uriString = "image-1",
      variant = GalleryShareSheetVariant.AutoOpen,
    )

    assertTrue(next.selectedImages.isEmpty())
    assertFalse(next.isSheetOpen)
  }

  @Test
  fun removeSelectionKeepsSheetOpenWhenItemsRemain() {
    val next = applyGallerySelectionRemoval(
      currentSelection = listOf("image-1", "image-2"),
      uriString = "image-1",
    )

    assertEquals(listOf("image-2"), next.selectedImages)
    assertTrue(next.isSheetOpen)
  }

  @Test
  fun removeSelectionClosesSheetWhenNothingRemains() {
    val next = applyGallerySelectionRemoval(
      currentSelection = listOf("image-1"),
      uriString = "image-1",
    )

    assertTrue(next.selectedImages.isEmpty())
    assertFalse(next.isSheetOpen)
  }
}
