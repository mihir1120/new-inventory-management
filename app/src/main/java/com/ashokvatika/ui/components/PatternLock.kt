package com.ashokvatika.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun PatternLock(
  modifier: Modifier = Modifier,
  dotSize: Dp = 16.dp,
  gap: Dp = 38.dp,
  dotBase: Color = Color(0xFF31905D),
  resetKey: Int = 0,
  enabled: Boolean,
  onPatternChanged: (List<Int>) -> Unit,
  onPatternFinalized: (List<Int>) -> Unit,
) {
  val centers = remember { mutableStateMapOf<Int, Offset>() }
  var dragging by remember { mutableStateOf(false) }
  var pointer by remember { mutableStateOf<Offset?>(null) }
  var pattern by remember { mutableStateOf(emptyList<Int>()) }
  var containerCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

  val density = LocalDensity.current
  val radiusPx = with(density) { (dotSize * 1.25f).toPx() }

  LaunchedEffect(resetKey) {
    pattern = emptyList()
    dragging = false
    pointer = null
    onPatternChanged(emptyList())
  }

  fun updatePattern(next: List<Int>) {
    pattern = next
    onPatternChanged(next)
  }

  fun hitTest(pos: Offset): Int? {
    if (centers.size != 9) return null
    var bestIndex: Int? = null
    var bestDist = Float.MAX_VALUE
    for ((idx, c) in centers) {
      val d = (pos - c).getDistance()
      if (d < radiusPx && d < bestDist) {
        bestDist = d
        bestIndex = idx
      }
    }
    return bestIndex
  }

  Box(
    modifier = modifier
      .wrapContentSize(Alignment.Center)
      .onGloballyPositioned { containerCoords = it }
      .pointerInput(enabled) {
        if (!enabled) return@pointerInput
        detectDragGestures(
          onDragStart = { start ->
            dragging = true
            pointer = start
            hitTest(start)?.let { idx ->
              if (!pattern.contains(idx)) updatePattern(pattern + idx)
            }
          },
          onDrag = { change, _ ->
            pointer = change.position
            hitTest(change.position)?.let { idx ->
              if (!pattern.contains(idx)) updatePattern(pattern + idx)
            }
          },
          onDragEnd = {
            dragging = false
            pointer = null
            onPatternFinalized(pattern)
          },
          onDragCancel = {
            dragging = false
            pointer = null
            onPatternFinalized(pattern)
          },
        )
      },
    contentAlignment = Alignment.Center,
  ) {
    Canvas(
      modifier = Modifier
        .width((dotSize * 3) + (gap * 2))
        .aspectRatio(1f),
    ) {
      val stroke = Stroke(width = 6f, cap = StrokeCap.Round)
      val points = pattern.mapNotNull { centers[it] }
      for (i in 0 until points.lastIndex) {
        drawLine(
          color = dotBase.copy(alpha = 0.55f),
          start = points[i],
          end = points[i + 1],
          strokeWidth = stroke.width,
          cap = stroke.cap,
        )
      }
      if (dragging && points.isNotEmpty() && pointer != null) {
        drawLine(
          color = dotBase.copy(alpha = 0.42f),
          start = points.last(),
          end = pointer!!,
          strokeWidth = stroke.width,
          cap = stroke.cap,
        )
      }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      repeat(3) { row ->
        Row(verticalAlignment = Alignment.CenterVertically) {
          repeat(3) { col ->
            val idx = row * 3 + col
            val selected = pattern.contains(idx)
            Dot(
              index = idx,
              dotSize = dotSize,
              gap = gap,
              base = dotBase,
              selected = selected,
              enabled = enabled,
              containerCoords = containerCoords,
              onCentered = { centers[idx] = it },
            )
          }
        }
      }
    }
  }
}

@Composable
fun PatternLockPreview(
  modifier: Modifier = Modifier,
  dotSize: Dp = 16.dp,
  gap: Dp = 38.dp,
  dotBase: Color = Color(0xFF31905D),
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    repeat(3) { row ->
      Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { col ->
          val idx = row * 3 + col
          AnimatedDot(
            index = idx,
            dotSize = dotSize,
            gap = gap,
            base = dotBase,
          )
        }
      }
    }
  }
}

@Composable
private fun AnimatedDot(
  index: Int,
  dotSize: Dp,
  gap: Dp,
  base: Color,
) {
  var visible by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    delay(220L + (index * 45L))
    visible = true
  }

  Dot(
    index = index,
    dotSize = dotSize,
    gap = gap,
    base = base,
    selected = false,
    enabled = false,
    alphaOverride = if (visible) 1f else 0f,
    scaleOverride = if (visible) 1f else 0.86f,
    translateYOverride = if (visible) 0f else 12f,
    onCentered = {},
  )
}

@Composable
private fun Dot(
  index: Int,
  dotSize: Dp,
  gap: Dp,
  base: Color,
  selected: Boolean,
  enabled: Boolean,
  alphaOverride: Float? = null,
  scaleOverride: Float? = null,
  translateYOverride: Float? = null,
  containerCoords: LayoutCoordinates? = null,
  onCentered: (Offset) -> Unit,
) {
  val bg = if (selected) base else base
  val alpha = alphaOverride ?: 1f
  val scale = scaleOverride ?: 1f
  val ty = translateYOverride ?: 0f

  Canvas(
    modifier = Modifier
      .size(dotSize)
      .onGloballyPositioned { coords ->
        val c = Offset(coords.size.width / 2f, coords.size.height / 2f)
        val rel = if (containerCoords != null) containerCoords.localPositionOf(coords, c) else c
        onCentered(rel)
      },
  ) {
    withTransform({
      translate(top = ty)
      scale(scale, scale)
    }) {
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(Color(0xFF48B071), bg, Color(0xFF24734A)),
          center = center,
          radius = size.minDimension * 0.8f,
        ),
        radius = size.minDimension / 2f,
        center = center,
        alpha = alpha,
      )
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(Color.White.copy(alpha = 0.92f), Color.White.copy(alpha = 0.52f), Color.Transparent),
          center = Offset(center.x - (size.minDimension * 0.16f), center.y - (size.minDimension * 0.18f)),
          radius = size.minDimension * 0.7f,
        ),
        radius = size.minDimension / 2f,
        center = center,
        alpha = alpha,
      )
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(Color(0x38000000), Color.Transparent),
          center = Offset(center.x + (size.minDimension * 0.15f), center.y + (size.minDimension * 0.18f)),
          radius = size.minDimension * 0.9f,
        ),
        radius = size.minDimension / 2f,
        center = center,
        alpha = alpha,
      )
    }
  }
}
