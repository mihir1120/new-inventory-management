package com.ashokvatika.ui.admin

import com.ashokvatika.config.GalleryShareSheetVariant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GalleryShareSheetStateTest {
  @Test
  fun autoOpenVariantOpensSheetWhenFirstImageSelected() {
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
  fun manualOpenVariantKeepsSheetClosedAfterSelection() {
    val next = applyGallerySelectionToggle(
      currentSelection = emptyList(),
      wasSheetOpen = false,
      uriString = "image-1",
      variant = GalleryShareSheetVariant.ManualOpen,
    )

    assertEquals(listOf("image-1"), next.selectedImages)
    assertFalse(next.isSheetOpen)
  }

  @Test
  fun manualOpenVariantPreservesOpenSheetWhileSelectionChanges() {
    val next = applyGallerySelectionToggle(
      currentSelection = listOf("image-1"),
      wasSheetOpen = true,
      uriString = "image-2",
      variant = GalleryShareSheetVariant.ManualOpen,
    )

    assertEquals(listOf("image-1", "image-2"), next.selectedImages)
    assertTrue(next.isSheetOpen)
  }

  @Test
  fun removingLastImageClosesSheet() {
    val next = applyGallerySelectionRemoval(
      currentSelection = listOf("image-1"),
      uriString = "image-1",
    )

    assertTrue(next.selectedImages.isEmpty())
    assertFalse(next.isSheetOpen)
  }
}
