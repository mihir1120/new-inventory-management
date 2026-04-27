package com.ashokvatika.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle

@Composable
fun NumericKeypad(
  modifier: Modifier = Modifier,
  onDigit: (Int) -> Unit,
  onDecimal: (() -> Unit)? = null,
  onSignToggle: (() -> Unit)? = null,
  onBackspace: () -> Unit,
  onConfirm: (() -> Unit)?,
) {
  val bg = Color(0xFFF2F3F4)
  val keyBg = Color.White.copy(alpha = 0.92f)
  val ink = Color(0xFF141414)
  val accent = Color(0xFFF17C32)

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(bg)
      .padding(horizontal = 18.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    KeyRow(
      keys = listOf(1, 2, 3),
      keyBg = keyBg,
      ink = ink,
      onDigit = onDigit,
    )
    KeyRow(
      keys = listOf(4, 5, 6),
      keyBg = keyBg,
      ink = ink,
      onDigit = onDigit,
    )
    KeyRow(
      keys = listOf(7, 8, 9),
      keyBg = keyBg,
      ink = ink,
      onDigit = onDigit,
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (onSignToggle != null) {
        KeyButton(
          label = "+/-",
          modifier = Modifier.weight(1f),
          bg = keyBg,
          ink = ink,
          onClick = onSignToggle,
        )
      }

      KeyButton(
        label = "0",
        modifier = Modifier.weight(1f),
        bg = keyBg,
        ink = ink,
        onClick = { onDigit(0) },
      )

      if (onDecimal != null) {
        KeyButton(
          label = ".",
          modifier = Modifier.weight(1f),
          bg = keyBg,
          ink = ink,
          onClick = onDecimal,
        )
      } else {
        Spacer(modifier = Modifier.weight(1f))
      }

      IconKeyButton(
        modifier = Modifier.weight(1f),
        bg = keyBg,
        onClick = onBackspace,
      ) {
        Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace", tint = ink)
      }

      if (onConfirm != null) {
        IconKeyButton(
          modifier = Modifier.weight(1f),
          bg = keyBg,
          onClick = onConfirm,
        ) {
          Icon(Icons.Filled.CheckCircle, contentDescription = "Confirm", tint = accent)
        }
      }
    }
  }
}

@Composable
private fun KeyRow(
  keys: List<Int>,
  keyBg: Color,
  ink: Color,
  onDigit: (Int) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    keys.forEach { n ->
      KeyButton(
        label = n.toString(),
        modifier = Modifier.weight(1f),
        bg = keyBg,
        ink = ink,
        onClick = { onDigit(n) },
      )
    }
  }
}

@Composable
private fun KeyButton(
  label: String,
  modifier: Modifier = Modifier,
  bg: Color,
  ink: Color,
  onClick: () -> Unit,
) {
  Box(
    modifier = modifier
      .height(62.dp)
      .background(bg, RoundedCornerShape(16.dp))
      .padding(6.dp),
    contentAlignment = Alignment.Center,
  ) {
    IconButton(onClick = onClick, modifier = Modifier.size(52.dp)) {
      Text(
        text = label,
        style = TextStyle(
          color = ink,
          fontSize = 22.sp,
          fontWeight = FontWeight.SemiBold,
        ),
      )
    }
  }
}

@Composable
private fun IconKeyButton(
  modifier: Modifier = Modifier,
  bg: Color,
  onClick: () -> Unit,
  content: @Composable () -> Unit,
) {
  Box(
    modifier = modifier
      .height(62.dp)
      .background(bg, RoundedCornerShape(16.dp))
      .padding(6.dp),
    contentAlignment = Alignment.Center,
  ) {
    IconButton(onClick = onClick, modifier = Modifier.size(52.dp)) {
      content()
    }
  }
}
