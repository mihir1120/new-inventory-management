package com.ashokvatika.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.ashokvatika.R

val BrandOrange = Color(0xFFF17C32)
val BrandGreen = Color(0xFF7CD84F)
val BrandInk = Color(0xFF141414)

enum class BrandButtonIcon {
  OTC,
  Plants,
  Pots,
  Supplies,
}

@Composable
fun AshokvatikaLogo(
  modifier: Modifier = Modifier,
  textSize: TextUnit,
  markSize: Dp,
) {
  val logoHeight = (markSize + (textSize.value * 0.72f).dp) * 1.5f
  Image(
    painter = painterResource(R.drawable.av_logo_exact),
    contentDescription = "Ashokvatika logo",
    contentScale = ContentScale.Fit,
    modifier = modifier.height(logoHeight),
  )
}

@Composable
fun BrandButtonGlyph(
  kind: BrandButtonIcon,
  modifier: Modifier = Modifier,
  primary: Color = Color.White,
  secondary: Color = Color.White.copy(alpha = 0.8f),
) {
  val iconRes = when (kind) {
    BrandButtonIcon.OTC -> R.drawable.icon_otc
    BrandButtonIcon.Plants -> R.drawable.icon_plants
    BrandButtonIcon.Pots -> R.drawable.icon_pots
    BrandButtonIcon.Supplies -> R.drawable.icon_supplies
  }
  Image(
    painter = painterResource(iconRes),
    contentDescription = null,
    contentScale = ContentScale.Fit,
    modifier = modifier,
  )
}
