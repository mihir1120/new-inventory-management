package com.ashokvatika.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
internal fun FullImagePreview(
  image: ImageBitmap,
  onDismiss: () -> Unit,
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.96f))
        .clickable(onClick = onDismiss),
      contentAlignment = Alignment.Center,
    ) {
      Image(
        bitmap = image,
        contentDescription = "Full product image. Tap anywhere to close",
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize().padding(12.dp),
      )
      Text(
        text = "Tap anywhere to close",
        color = Color.White,
        fontSize = 14.sp,
        modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp),
      )
    }
  }
}
