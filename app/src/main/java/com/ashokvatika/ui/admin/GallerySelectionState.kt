package com.ashokvatika.ui.admin

import com.ashokvatika.config.GalleryShareSheetVariant

data class GallerySelectionState(
  val selectedImages: List<String>,
  val isSheetOpen: Boolean,
)

fun applyGallerySelectionToggle(
  currentSelection: List<String>,
  wasSheetOpen: Boolean,
  uriString: String,
  variant: GalleryShareSheetVariant,
): GallerySelectionState {
  val nextSelection = if (uriString in currentSelection) {
    currentSelection - uriString
  } else {
    currentSelection + uriString
  }

  val nextSheetOpen = when {
    nextSelection.isEmpty() -> false
    variant == GalleryShareSheetVariant.AutoOpen -> true
    else -> wasSheetOpen
  }

  return GallerySelectionState(
    selectedImages = nextSelection,
    isSheetOpen = nextSheetOpen,
  )
}

fun applyGallerySelectionRemoval(
  currentSelection: List<String>,
  uriString: String,
): GallerySelectionState {
  val nextSelection = currentSelection - uriString
  return GallerySelectionState(
    selectedImages = nextSelection,
    isSheetOpen = nextSelection.isNotEmpty(),
  )
}
