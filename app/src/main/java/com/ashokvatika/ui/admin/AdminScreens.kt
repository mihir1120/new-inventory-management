package com.ashokvatika.ui.admin

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AddToDrive
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.net.toUri
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import com.ashokvatika.app.R
import com.ashokvatika.config.AppExperimentManager
import com.ashokvatika.config.GalleryShareSheetVariant
import com.ashokvatika.data.AdminAccessRequest
import com.ashokvatika.ui.components.AshokvatikaLogo
import com.ashokvatika.ui.components.BrandButtonGlyph
import com.ashokvatika.ui.components.BrandButtonIcon
import com.ashokvatika.ui.components.BrandGreen
import com.ashokvatika.ui.components.BrandOrange
import com.ashokvatika.ui.components.NumericKeypad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Bg = Color(0xFFF6F2EB)
private val Accent = BrandOrange
private val Ink = Color(0xFF141414)
private val AccentGreen = BrandGreen
private val TileShadow = Color(0x2C9EA4AA)
private val SoftGreyShadow = Color(0x24969CA6)
private val Panel = Color(0xFFFFFCF7)
private val PanelTint = Color(0xFFF7EFE4)
private val PanelBorder = Color(0xFFE8DDCF)
private val CardShape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
private val UploadPill = Color(0x26FFFFFF)
private val OtcHeaderActionSize = 54.dp
private val OtcHeaderIconSize = 24.dp
private val OtcCartIconSize = 31.2.dp
private val OtcHeaderHorizontalPadding = 20.dp
private val OtcPageVerticalPadding = 16.dp
private val OtcHeaderActionSpacing = 12.dp
private val OtcHeaderLogoMaxWidth = 360.dp
private val OtcHeaderTopSpace = 8.dp
private val OtcHeaderFrameHeight = 186.dp
private val OtcHeaderLogoWidth = 320.dp
private val OtcHeaderLogoHeight = 122.4.dp
private val OtcPhoneHeaderHorizontalPadding = 12.dp
private val OtcPhoneHeaderTopPadding = 6.dp
private val OtcPhoneHeaderActionTopPadding = 4.dp
private val OtcPhoneHeaderActionSize = 38.dp
private val OtcPhoneHeaderIconSize = 16.dp
private val OtcPhoneHeaderSpacing = 8.dp
private val OtcPhoneHeaderLogoWidth = 184.dp
private val OtcPhoneHeaderLogoHeight = 60.dp
private val OtcPhoneHeaderLogoTextSize = 16.sp
private val OtcPhoneHeaderLogoMarkSize = 29.33.dp
private val OtcPhoneContentTopGap = 1.33.dp
private val OtcPhoneStickyBlockTopOffset = 18.dp
private val OtcHeaderTopOffset = OtcPageVerticalPadding + OtcHeaderTopSpace
private val OtcMainContentTopPadding = OtcHeaderTopOffset + OtcHeaderFrameHeight + 10.dp
private val OtcOpenCartContentTopPadding = OtcHeaderTopOffset + OtcHeaderFrameHeight + 12.dp
private val OtcClosedContentTopPadding = OtcHeaderTopOffset + OtcHeaderFrameHeight + 14.dp
private val LockedHeaderLogoTopOffset = (-24).dp
private val LockedHeaderActionSize = 42.dp
private val LockedHeaderIconSize = 24.dp
private val LockedHeaderActionSpacing = 10.dp

private fun isPhoneScreen(maxWidth: Dp): Boolean = maxWidth < 600.dp

private fun isCompactPhoneScreen(maxWidth: Dp, maxHeight: Dp): Boolean =
  isPhoneScreen(maxWidth) && (maxWidth < 430.dp || maxHeight < 900.dp)

enum class DashboardDestination(
  val title: String,
) {
  OTC("OTC"),
  Plants("Plants"),
  Supplies("Supplies"),
  Pots("Pots"),
  Docs("Docs"),
  Art("Art"),
}

private data class DashboardTileItem(
  val destination: DashboardDestination,
  val icon: BrandButtonIcon? = null,
  val delayMillis: Int,
)

private data class DocsMenuItem(
  val title: String,
  val delayMillis: Int,
  val baseColor: Color = Accent,
)

private data class MenuScreenLayoutSpec(
  val contentWidth: Dp,
  val tileGap: Dp,
  val tileWidth: Dp,
  val topSpacer: Dp,
  val topPadding: Dp,
  val brandTextSize: TextUnit,
  val brandMarkSize: Dp,
  val logoWidthFraction: Float,
  val footerWidthFraction: Float,
  val footerHeight: Dp,
  val bottomSpacer: Dp,
)

private fun menuScreenLayoutSpec(
  maxWidth: Dp,
  maxHeight: Dp,
): MenuScreenLayoutSpec {
  val phoneScreen = isPhoneScreen(maxWidth)
  val compactScreen = isCompactPhoneScreen(maxWidth, maxHeight)
  val tabletScreen = maxWidth >= 700.dp
  val contentWidth = when {
    maxWidth >= 1000.dp -> 900.dp
    tabletScreen -> 820.dp
    else -> minOf(maxWidth, 720.dp)
  }
  val tileGap = when {
    maxWidth >= 1000.dp -> 30.dp
    tabletScreen -> 24.dp
    compactScreen -> 10.dp
    else -> 12.dp
  }
  val tileWidth = ((contentWidth - tileGap) / 2f).coerceAtMost(
    when {
      maxWidth >= 1000.dp -> 278.dp
      tabletScreen -> 250.dp
      compactScreen -> 188.dp
      else -> 218.dp
    },
  )
  val topSpacer = when {
    maxWidth >= 1000.dp -> 68.dp
    tabletScreen -> 60.dp
    compactScreen -> 20.dp
    maxHeight > 900.dp -> 52.dp
    maxHeight > 700.dp -> 36.dp
    else -> 24.dp
  }
  val topPadding = when {
    maxWidth >= 1000.dp -> 54.dp
    tabletScreen -> 46.dp
    compactScreen -> 12.dp
    maxHeight > 900.dp -> 42.dp
    maxHeight > 700.dp -> 30.dp
    else -> 18.dp
  }
  val brandTextSize = when {
    maxWidth >= 1000.dp -> 86.sp
    tabletScreen -> 75.sp
    compactScreen -> 42.sp
    phoneScreen -> 48.sp
    else -> 57.sp
  }
  val brandMarkSize = when {
    maxWidth >= 1000.dp -> 190.dp
    tabletScreen -> 168.dp
    compactScreen -> 92.dp
    phoneScreen -> 104.dp
    else -> 126.dp
  }
  val logoWidthFraction = when {
    maxWidth >= 1000.dp -> 0.48f
    tabletScreen -> 0.54f
    compactScreen -> 0.72f
    phoneScreen -> 0.64f
    else -> 0.6f
  }
  val footerWidthFraction = when {
    maxWidth >= 1000.dp -> 0.3f
    tabletScreen -> 0.34f
    compactScreen -> 0.44f
    phoneScreen -> 0.4f
    else -> 0.38f
  }
  val footerHeight = when {
    maxWidth >= 1000.dp -> 128.dp
    tabletScreen -> 116.dp
    compactScreen -> 72.dp
    else -> 96.dp
  }
  val bottomSpacer = if (compactScreen) 20.dp else 28.dp

  return MenuScreenLayoutSpec(
    contentWidth = contentWidth,
    tileGap = tileGap,
    tileWidth = tileWidth,
    topSpacer = topSpacer,
    topPadding = topPadding,
    brandTextSize = brandTextSize,
    brandMarkSize = brandMarkSize,
    logoWidthFraction = logoWidthFraction,
    footerWidthFraction = footerWidthFraction,
    footerHeight = footerHeight,
    bottomSpacer = bottomSpacer,
  )
}

private data class MenuHeaderSpec(
  val contentWidth: Dp,
  val horizontalPadding: Dp,
  val headerTopPadding: Dp,
  val actionButtonSize: Dp,
  val actionIconSize: Dp,
  val actionSpacing: Dp,
  val actionTopPadding: Dp,
)

@Composable
private fun rememberMenuHeaderSpec(
  maxWidth: Dp,
  maxHeight: Dp,
): MenuHeaderSpec {
  val compactScreen = isCompactPhoneScreen(maxWidth, maxHeight)
  val phoneScreen = isPhoneScreen(maxWidth)
  val statusBarTopPaddingPx = ViewCompat.getRootWindowInsets(LocalView.current)
    ?.getInsets(WindowInsetsCompat.Type.statusBars())
    ?.top ?: 0
  val statusBarTopPadding = with(LocalDensity.current) { statusBarTopPaddingPx.toDp() }
  val contentWidth = if (phoneScreen) maxWidth else minOf(maxWidth - if (compactScreen) 20.dp else 32.dp, 920.dp)
  val horizontalPadding = if (phoneScreen) OtcPhoneHeaderHorizontalPadding else if (compactScreen) 10.dp else 16.dp
  val headerTopPadding = if (phoneScreen) statusBarTopPadding + OtcPhoneHeaderTopPadding else OtcHeaderTopOffset
  val actionButtonSize = if (phoneScreen) OtcPhoneHeaderActionSize else LockedHeaderActionSize
  val actionIconSize = if (phoneScreen) OtcPhoneHeaderIconSize else LockedHeaderIconSize
  val actionSpacing = if (phoneScreen) OtcPhoneHeaderSpacing else LockedHeaderActionSpacing
  val actionTopPadding = if (phoneScreen) OtcPhoneHeaderActionTopPadding else 0.dp
  return MenuHeaderSpec(
    contentWidth = contentWidth,
    horizontalPadding = horizontalPadding,
    headerTopPadding = headerTopPadding,
    actionButtonSize = actionButtonSize,
    actionIconSize = actionIconSize,
    actionSpacing = actionSpacing,
    actionTopPadding = actionTopPadding,
  )
}

@Composable
private fun MenuHubHeader(
  modifier: Modifier = Modifier,
  onBack: () -> Unit,
  actionButtonSize: Dp,
  actionIconSize: Dp,
  actionSpacing: Dp,
  actionTopPadding: Dp,
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(actionButtonSize * 3f + actionSpacing * 2f),
  ) {
    Column(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(top = actionTopPadding),
      horizontalAlignment = Alignment.End,
      verticalArrangement = Arrangement.spacedBy(actionSpacing),
    ) {
      OtcHeaderActionSlot(actionButtonSize) {
        CircleIconAction(
          size = actionButtonSize,
          borderColor = Accent,
          borderWidth = 2.dp,
          icon = {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Accent,
              modifier = Modifier.size(actionIconSize),
            )
          },
          onClick = onBack,
        )
      }
      OtcHeaderActionSlot(actionButtonSize)
      OtcHeaderActionSlot(actionButtonSize)
    }
  }
}

data class InventoryItem(
  val id: String,
  val category: DashboardDestination,
  val name: String,
  val batch: String,
  val sale: String,
  val rent: String,
  val details: String,
  val price: String,
  val checkout: String,
  val imageUri: String?,
  val mediaUris: List<String> = emptyList(),
  val status: String = "complete",
  val templatePrimaryTitle: String = "",
  val templatePrimaryDescription: String = "",
  val templateSecondaryTitle: String = "",
  val templateSecondaryDescription: String = "",
  val templateContact: String = "",
)

data class CartProductItem(
  val id: String,
  val name: String,
  val shortName: String,
  val mrp: Int,
  val landingPrice: Int,
  val quantity: Int,
  val imageUri: String? = null,
)

data class OpenCartRow(
  val id: String,
  val title: String,
  val items: List<CartProductItem>,
  val customAmount: Int? = null,
)

data class SavedDeal(
  val id: String,
  val title: String,
  val clientName: String,
  val clientPhone: String,
  val clientId: String,
  val transactionCode: String,
  val rows: List<OpenCartRow>,
)

private data class PendingInvoiceExport(
  val fileName: String,
  val pdfBytes: ByteArray,
)

private enum class BuildProductCategory(
  val label: String,
) {
  Pots("Pots"),
  Supplies("Supplies"),
}

private data class BuildProductItem(
  val id: String,
  val category: BuildProductCategory,
  val name: String,
  val stock: Int,
  val price: Int,
  val shortName: String,
)

private data class BuildProductSelection(
  val item: BuildProductItem,
  val quantity: Int,
) {
  val totalPrice: Int
    get() = item.price * quantity
}

private data class GalleryImageItem(
  val id: String,
  val uriString: String,
)

private data class SharePlatformAction(
  val id: String,
  val label: String,
  val icon: androidx.compose.ui.graphics.vector.ImageVector,
  val packageName: String? = null,
)

private enum class MediaPopupTab {
  GALLERY,
  BROUCHER,
  BROUCHER2,
}

@Composable
fun AdminHomeScreen(
  pendingAccessRequests: List<AdminAccessRequest>,
  syncMessage: String?,
  onApproveRequest: (String) -> Unit,
  onDenyRequest: (String) -> Unit,
  onTileClick: (DashboardDestination) -> Unit,
) {
  val dmSansThin = rememberDashboardFont()
  var animateTiles by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    delay(120)
    animateTiles = true
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Bg)
      .navigationBarsPadding()
      .padding(horizontal = 16.dp, vertical = 16.dp),
  ) {
    val phoneScreen = isPhoneScreen(maxWidth)
    val compactScreen = isCompactPhoneScreen(maxWidth, maxHeight)
    val layout = menuScreenLayoutSpec(maxWidth, maxHeight)
    val dashboardContentOffset = if (compactScreen) 4.dp else 10.dp
    val dashboardLogoWidth = layout.contentWidth * layout.logoWidthFraction
    val dashboardLogoHeight = (dashboardLogoWidth / 4.92f).coerceAtLeast(50.dp)
    val dashboardTopSpacer = 14.dp
    val dashboardTileGap = when {
      compactScreen -> 6.dp
      phoneScreen -> 8.dp
      else -> layout.tileGap
    }
    val tileRows = listOf(
      listOf(
        DashboardTileItem(DashboardDestination.OTC, BrandButtonIcon.OTC, 40),
        DashboardTileItem(DashboardDestination.Plants, BrandButtonIcon.Plants, 160),
      ),
      listOf(
        DashboardTileItem(DashboardDestination.Supplies, BrandButtonIcon.Supplies, 280),
        DashboardTileItem(DashboardDestination.Pots, BrandButtonIcon.Pots, 400),
      ),
      listOf(
        DashboardTileItem(DashboardDestination.Docs, BrandButtonIcon.Docs, 520),
        DashboardTileItem(DashboardDestination.Art, BrandButtonIcon.Art, 640),
      ),
    )

    Column(
      modifier = Modifier
        .width(layout.contentWidth)
        .align(Alignment.Center)
        .offset(y = dashboardContentOffset),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      AshokvatikaLogo(
        modifier = Modifier.fillMaxWidth(layout.logoWidthFraction),
        textSize = layout.brandTextSize,
        markSize = layout.brandMarkSize,
        fixedHeight = dashboardLogoHeight,
      )

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "Choose a workspace to manage products, OTC deals, documents, and art assets.",
        color = Ink.copy(alpha = 0.68f),
        textAlign = TextAlign.Center,
        style = TextStyle(
          fontFamily = dmSansThin,
          fontWeight = FontWeight.Normal,
          fontSize = if (compactScreen) 13.sp else 16.sp,
          lineHeight = if (compactScreen) 18.sp else 22.sp,
        ),
      )

      if (!syncMessage.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(if (compactScreen) 10.dp else 14.dp))
        Text(
          text = syncMessage,
          color = Accent,
          textAlign = TextAlign.Center,
          style = TextStyle(
            fontFamily = dmSansThin,
            fontWeight = FontWeight.Normal,
            fontSize = if (compactScreen) 13.sp else 16.sp,
            lineHeight = if (compactScreen) 17.sp else 20.sp,
          ),
        )
      }

      if (pendingAccessRequests.isNotEmpty()) {
        Spacer(modifier = Modifier.height(18.dp))
        PendingAccessApprovalCard(
          requests = pendingAccessRequests,
          fontFamily = dmSansThin,
          onApproveRequest = onApproveRequest,
          onDenyRequest = onDenyRequest,
        )
      }

      Spacer(modifier = Modifier.height(dashboardTopSpacer))

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dashboardTileGap),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        tileRows.forEach { row ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dashboardTileGap, Alignment.CenterHorizontally),
          ) {
            row.forEach { tile ->
              DashboardTile(
                title = tile.destination.title,
                fontFamily = dmSansThin,
                tileWidth = layout.tileWidth,
                icon = tile.icon,
                delayMillis = tile.delayMillis,
                animateTiles = animateTiles,
                onClick = { onTileClick(tile.destination) },
              )
            }
          }
        }
      }
    }

    Image(
      painter = painterResource(R.drawable.logo_powered_by),
      contentDescription = "Powered by Brand and Stock",
      contentScale = ContentScale.Fit,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = if (compactScreen) 8.dp else 12.dp)
        .fillMaxWidth(if (compactScreen) 0.34f else layout.footerWidthFraction)
        .height(if (compactScreen) 60.dp else layout.footerHeight),
    )
  }
}

@Composable
fun DocsHomeScreen(
  onBack: () -> Unit,
  onOpenAddress: () -> Unit,
  onOpenLetterhead: () -> Unit,
  onOpenArt: () -> Unit,
) {
  val dmSansThin = rememberDashboardFont()
  var animateTiles by remember { mutableStateOf(false) }
  val tileRows = listOf(
    listOf(
      DocsMenuItem("Address", 40),
      DocsMenuItem("Invoice", 160),
    ),
    listOf(
      DocsMenuItem("Letterhead", 280),
      DocsMenuItem("Client", 400),
    ),
    listOf(
      DocsMenuItem("QR", 520),
      DocsMenuItem("Art", 640),
    ),
  )

  BackHandler(onBack = onBack)

  LaunchedEffect(Unit) {
    delay(120)
    animateTiles = true
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Bg)
      .navigationBarsPadding()
      .padding(horizontal = 16.dp, vertical = 16.dp),
  ) {
    val layout = menuScreenLayoutSpec(maxWidth, maxHeight)
    val header = rememberMenuHeaderSpec(maxWidth, maxHeight)
    val compactScreen = isCompactPhoneScreen(maxWidth, maxHeight)
    val docsContentOffset = if (compactScreen) 4.dp else 10.dp
    val docsLogoWidth = layout.contentWidth * layout.logoWidthFraction
    val docsLogoHeight = (docsLogoWidth / 4.92f).coerceAtLeast(50.dp)
    val logoToTilesGap = when {
      maxWidth >= 1000.dp -> 18.dp
      maxWidth >= 700.dp -> 16.dp
      compactScreen -> 14.dp
      else -> 14.dp
    }
    val docsTileGap = when {
      compactScreen -> 6.dp
      isPhoneScreen(maxWidth) -> 8.dp
      else -> layout.tileGap
    }

    Column(
      modifier = Modifier
        .width(layout.contentWidth)
        .align(Alignment.Center)
        .offset(y = docsContentOffset),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      AshokvatikaLogo(
        modifier = Modifier.fillMaxWidth(layout.logoWidthFraction),
        textSize = layout.brandTextSize,
        markSize = layout.brandMarkSize,
        fixedHeight = docsLogoHeight,
      )

      Spacer(modifier = Modifier.height(logoToTilesGap))

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(docsTileGap),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        tileRows.forEach { row ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(docsTileGap, Alignment.CenterHorizontally),
          ) {
            row.forEach { item ->
              DashboardTile(
                title = item.title,
                fontFamily = dmSansThin,
                tileWidth = layout.tileWidth,
                icon = null,
                delayMillis = item.delayMillis,
                animateTiles = animateTiles,
                baseColor = item.baseColor,
                onClick = {
                  when (item.title) {
                    "Address" -> onOpenAddress()
                    "Letterhead" -> onOpenLetterhead()
                    "Art" -> onOpenArt()
                  }
                },
              )
            }
          }
        }
      }
    }

    Image(
      painter = painterResource(R.drawable.logo_powered_by),
      contentDescription = "Powered by Brand and Stock",
      contentScale = ContentScale.Fit,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = if (compactScreen) 8.dp else 12.dp)
        .fillMaxWidth(if (compactScreen) 0.34f else layout.footerWidthFraction)
        .height(if (compactScreen) 60.dp else layout.footerHeight),
    )

    MenuHubHeader(
      modifier = Modifier
        .width(header.contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = header.horizontalPadding, top = header.headerTopPadding, end = header.horizontalPadding),
      onBack = onBack,
      actionButtonSize = header.actionButtonSize,
      actionIconSize = header.actionIconSize,
      actionSpacing = header.actionSpacing,
      actionTopPadding = header.actionTopPadding,
    )
  }
}

@Composable
fun ArtHomeScreen(
  onBack: () -> Unit,
  onOpenBrochure: () -> Unit,
  onOpenCatTwo: () -> Unit,
  onOpenCatFour: () -> Unit,
  onOpenCatPlus: () -> Unit,
  onOpenProPlus: () -> Unit,
  onOpenProfile: () -> Unit,
) {
  val dmSansThin = rememberDashboardFont()
  var animateTiles by remember { mutableStateOf(false) }
  val tileRows = listOf(
    listOf(
      DocsMenuItem("Brochure", 40),
      DocsMenuItem("Profile", 160),
    ),
    listOf(
      DocsMenuItem("Pro+", 280),
      DocsMenuItem("Cat/2", 400),
    ),
    listOf(
      DocsMenuItem("Cat/4", 520),
      DocsMenuItem("Cat+", 640, baseColor = Color(0xFFF57A2C)),
    ),
  )

  BackHandler(onBack = onBack)

  LaunchedEffect(Unit) {
    delay(120)
    animateTiles = true
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Bg)
      .navigationBarsPadding()
      .padding(horizontal = 16.dp, vertical = 16.dp),
  ) {
    val layout = menuScreenLayoutSpec(maxWidth, maxHeight)
    val header = rememberMenuHeaderSpec(maxWidth, maxHeight)
    val compactScreen = isCompactPhoneScreen(maxWidth, maxHeight)
    val phoneScreen = isPhoneScreen(maxWidth)
    val artContentOffset = if (compactScreen) 4.dp else 10.dp
    val artLogoWidth = layout.contentWidth * layout.logoWidthFraction
    val artLogoHeight = (artLogoWidth / 4.92f).coerceAtLeast(50.dp)
    val logoToTilesGap = when {
      maxWidth >= 1000.dp -> 18.dp
      maxWidth >= 700.dp -> 16.dp
      compactScreen -> 14.dp
      else -> 14.dp
    }
    val artTileGap = when {
      compactScreen -> 6.dp
      phoneScreen -> 8.dp
      else -> layout.tileGap
    }

    Column(
      modifier = Modifier
        .width(layout.contentWidth)
        .align(Alignment.Center)
        .offset(y = artContentOffset),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      AshokvatikaLogo(
        modifier = Modifier.fillMaxWidth(layout.logoWidthFraction),
        textSize = layout.brandTextSize,
        markSize = layout.brandMarkSize,
        fixedHeight = artLogoHeight,
      )

      Spacer(modifier = Modifier.height(logoToTilesGap))

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(artTileGap),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        tileRows.forEach { row ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(artTileGap, Alignment.CenterHorizontally),
          ) {
            row.forEach { item ->
              DashboardTile(
                title = item.title,
                fontFamily = dmSansThin,
                tileWidth = layout.tileWidth,
                icon = null,
                delayMillis = item.delayMillis,
                animateTiles = animateTiles,
                baseColor = item.baseColor,
                onClick = {
                  when (item.title) {
                    "Brochure" -> onOpenBrochure()
                    "Cat/2" -> onOpenCatTwo()
                    "Cat/4" -> onOpenCatFour()
                    "Cat+" -> onOpenCatPlus()
                    "Pro+" -> onOpenProPlus()
                    "Profile" -> onOpenProfile()
                  }
                },
              )
            }
          }
        }
      }
    }

    Image(
      painter = painterResource(R.drawable.logo_powered_by),
      contentDescription = "Powered by Brand and Stock",
      contentScale = ContentScale.Fit,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = if (compactScreen) 8.dp else 12.dp)
        .fillMaxWidth(if (compactScreen) 0.34f else layout.footerWidthFraction)
        .height(if (compactScreen) 60.dp else layout.footerHeight),
    )

    MenuHubHeader(
      modifier = Modifier
        .width(header.contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = header.horizontalPadding, top = header.headerTopPadding, end = header.horizontalPadding),
      onBack = onBack,
      actionButtonSize = header.actionButtonSize,
      actionIconSize = header.actionIconSize,
      actionSpacing = header.actionSpacing,
      actionTopPadding = header.actionTopPadding,
    )
  }
}

@Composable
fun AddressInfoScreen(
  onBack: () -> Unit,
) {
  val context = LocalContext.current
  val fontFamily = rememberDashboardFont()
  BackHandler(onBack = onBack)

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Bg)
      .navigationBarsPadding()
      .padding(horizontal = 16.dp, vertical = 16.dp),
  ) {
    val phoneScreen = isPhoneScreen(maxWidth)
    val compactScreen = isCompactPhoneScreen(maxWidth, maxHeight)
    val header = rememberMenuHeaderSpec(maxWidth, maxHeight)
    val layout = menuScreenLayoutSpec(maxWidth, maxHeight)
    val contentWidth = when {
      maxWidth >= 1000.dp -> 720.dp
      maxWidth >= 700.dp -> 640.dp
      else -> minOf(maxWidth, 540.dp)
    }
    val mapIconSize = when {
      maxWidth >= 1000.dp -> 90.dp
      maxWidth >= 700.dp -> 78.dp
      compactScreen -> 52.dp
      else -> 64.dp
    }
    val mapLinkSize = when {
      maxWidth >= 1000.dp -> 22.sp
      maxWidth >= 700.dp -> 19.sp
      compactScreen -> 12.sp
      else -> 14.sp
    }
    val titleSize = when {
      maxWidth >= 1000.dp -> 52.sp
      maxWidth >= 700.dp -> 44.sp
      compactScreen -> 26.sp
      else -> 34.sp
    }
    val detailSize = when {
      maxWidth >= 1000.dp -> 30.sp
      maxWidth >= 700.dp -> 25.sp
      compactScreen -> 16.sp
      else -> 21.sp
    }
    val contactSize = when {
      maxWidth >= 1000.dp -> 28.sp
      maxWidth >= 700.dp -> 23.sp
      compactScreen -> 15.sp
      else -> 20.sp
    }
    val socialSize = when {
      maxWidth >= 1000.dp -> 82.dp
      maxWidth >= 700.dp -> 72.dp
      compactScreen -> 48.dp
      else -> 58.dp
    }
    val socialSpacing = when {
      maxWidth >= 700.dp -> 22.dp
      compactScreen -> 12.dp
      else -> 16.dp
    }
    val mapUrl = "https://maps.app.goo.gl/hrvhQ97MA8PT8DhTA"
    val emailAddress = "ashokvatika@gmail.com"

    Column(
      modifier = Modifier
        .width(contentWidth)
        .align(Alignment.TopCenter)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Spacer(modifier = Modifier.height(layout.topPadding))

      AshokvatikaLogo(
        modifier = Modifier.fillMaxWidth(layout.logoWidthFraction.coerceAtMost(0.82f)),
        textSize = layout.brandTextSize,
        markSize = layout.brandMarkSize,
      )

      Spacer(modifier = Modifier.height(if (compactScreen) 34.dp else 44.dp))

      Surface(
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.clickable {
          launchExternalUri(context, mapUrl)
        },
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(if (compactScreen) 8.dp else 10.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = "Map location",
            tint = Color(0xFFE11D15),
            modifier = Modifier.size(mapIconSize),
          )
          Text(
            text = mapUrl,
            color = Ink.copy(alpha = 0.36f),
            textAlign = TextAlign.Center,
            style = TextStyle(
              fontFamily = fontFamily,
              fontWeight = FontWeight.SemiBold,
              fontSize = mapLinkSize,
            ),
          )
        }
      }

      Spacer(modifier = Modifier.height(if (compactScreen) 30.dp else 40.dp))

      Text(
        text = "Ashokvatika Nursery",
        color = Ink,
        textAlign = TextAlign.Center,
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = titleSize,
          lineHeight = titleSize * 1.08f,
        ),
      )

      Spacer(modifier = Modifier.height(if (compactScreen) 14.dp else 20.dp))

      Text(
        text = "Vallabhbhbhai Patel Road,\nMahakali, Andheri - East,\nMumbai 400 093",
        color = Ink.copy(alpha = 0.56f),
        textAlign = TextAlign.Center,
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = detailSize,
          lineHeight = detailSize * 1.04f,
        ),
      )

      Spacer(modifier = Modifier.height(if (compactScreen) 20.dp else 28.dp))

      AddressInfoAction(
        icon = Icons.Filled.Call,
        text = "+91 78085 80855\n+91 981981 7780",
        fontFamily = fontFamily,
        textSize = contactSize,
        onClick = { launchExternalUri(context, "tel:+917808580855") },
      )

      Spacer(modifier = Modifier.height(if (compactScreen) 18.dp else 24.dp))

      AddressInfoAction(
        icon = Icons.Filled.Mail,
        text = emailAddress,
        fontFamily = fontFamily,
        textSize = contactSize,
        onClick = { launchExternalUri(context, "mailto:$emailAddress") },
      )

      Spacer(modifier = Modifier.height(if (compactScreen) 34.dp else 48.dp))

      Row(
        horizontalArrangement = Arrangement.spacedBy(socialSpacing),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        AddressSocialBadge(
          label = "IG",
          fontFamily = fontFamily,
          background = Color(0xFFE95950),
          size = socialSize,
          onClick = {},
        )
        AddressSocialBadge(
          label = "WA",
          fontFamily = fontFamily,
          background = Color(0xFF4CD964),
          size = socialSize,
          onClick = { launchExternalUri(context, "https://wa.me/919819817780") },
        )
        AddressSocialBadge(
          label = "f",
          fontFamily = fontFamily,
          background = Color(0xFF1877F2),
          size = socialSize,
          onClick = {},
        )
        AddressSocialBadge(
          label = "YT",
          background = Color(0xFFFF0000),
          size = socialSize,
          onClick = {},
        )
      }

      Spacer(modifier = Modifier.height(if (phoneScreen) 32.dp else 42.dp))
    }

    MenuHubHeader(
      modifier = Modifier
        .width(header.contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = header.horizontalPadding, top = header.headerTopPadding, end = header.horizontalPadding),
      onBack = onBack,
      actionButtonSize = header.actionButtonSize,
      actionIconSize = header.actionIconSize,
      actionSpacing = header.actionSpacing,
      actionTopPadding = header.actionTopPadding,
    )
  }
}

@Composable
fun LetterheadScreen(
  onBack: () -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val fontFamily = rememberDashboardFont()
  var letterBody by rememberSaveable { mutableStateOf("") }
  var isReadyToExport by rememberSaveable { mutableStateOf(false) }
  var pendingExport by remember { mutableStateOf<PendingInvoiceExport?>(null) }
  val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
    val export = pendingExport
    pendingExport = null
    if (export == null) {
      return@rememberLauncherForActivityResult
    }
    scope.launch {
      val saved = if (uri == null) {
        false
      } else {
        withContext(Dispatchers.IO) {
          runCatching {
            context.contentResolver.openOutputStream(uri, "rwt")?.use { stream ->
              stream.write(export.pdfBytes)
              stream.flush()
            } != null
          }.getOrDefault(false)
        }
      }
      Toast.makeText(
        context,
        when {
          uri == null -> "Letterhead export was skipped."
          saved -> "Letterhead PDF saved successfully."
          else -> "Letterhead PDF save failed."
        },
        Toast.LENGTH_SHORT,
      ).show()
    }
  }

  BackHandler(onBack = onBack)

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Bg)
      .navigationBarsPadding()
      .padding(horizontal = 12.dp, vertical = 8.dp),
  ) {
    val header = rememberMenuHeaderSpec(maxWidth, maxHeight)
    val phoneScreen = isPhoneScreen(maxWidth)
    val compactScreen = isCompactPhoneScreen(maxWidth, maxHeight)
    val contentTopPadding = header.headerTopPadding + if (phoneScreen) 20.dp else 26.dp
    val bodyFontSize = when {
      maxWidth >= 1000.dp -> 21.sp
      maxWidth >= 700.dp -> 18.sp
      compactScreen -> 13.sp
      else -> 15.sp
    }
    val letterheadHeaderWidthFraction = when {
      maxWidth >= 1000.dp -> 0.38f
      maxWidth >= 700.dp -> 0.46f
      compactScreen -> 0.62f
      else -> 0.56f
    }
    val letterheadFooterWidthFraction = when {
      maxWidth >= 1000.dp -> 0.42f
      maxWidth >= 700.dp -> 0.52f
      compactScreen -> 0.8f
      else -> 0.74f
    }
    val editorTopGap = when {
      maxWidth >= 1000.dp -> 26.dp
      maxWidth >= 700.dp -> 22.dp
      compactScreen -> 18.dp
      else -> 20.dp
    }
    val editorBottomGap = when {
      maxWidth >= 1000.dp -> 28.dp
      maxWidth >= 700.dp -> 24.dp
      compactScreen -> 16.dp
      else -> 18.dp
    }
    val editorHorizontalPadding = when {
      maxWidth >= 1000.dp -> 44.dp
      maxWidth >= 700.dp -> 36.dp
      compactScreen -> 14.dp
      else -> 18.dp
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(top = contentTopPadding),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        painter = painterResource(R.drawable.letterhead_header),
        contentDescription = "Letterhead header",
        contentScale = ContentScale.FillWidth,
        modifier = Modifier
          .fillMaxWidth(letterheadHeaderWidthFraction),
      )

      Spacer(modifier = Modifier.height(editorTopGap))

      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(horizontal = editorHorizontalPadding),
      ) {
        BasicTextField(
          value = letterBody,
          onValueChange = {
            letterBody = it
            isReadyToExport = false
          },
          modifier = Modifier.fillMaxSize(),
          textStyle = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = bodyFontSize,
            lineHeight = bodyFontSize * 1.45f,
            color = Ink,
          ),
          cursorBrush = SolidColor(Accent),
          keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(
            onDone = {
              isReadyToExport = letterBody.isNotBlank()
            },
          ),
          decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxSize()) {
              if (letterBody.isBlank()) {
                Text(
                  text = "Type the letter here...",
                  color = Ink.copy(alpha = 0.18f),
                  style = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = bodyFontSize,
                    lineHeight = bodyFontSize * 1.45f,
                  ),
                )
              }
              innerTextField()
            }
          },
        )
      }

      Spacer(modifier = Modifier.height(editorBottomGap))

      Image(
        painter = painterResource(R.drawable.letterhead_footer),
        contentDescription = "Letterhead footer",
        contentScale = ContentScale.FillWidth,
        modifier = Modifier
          .fillMaxWidth(letterheadFooterWidthFraction)
          .padding(bottom = if (compactScreen) 6.dp else 10.dp),
      )
    }

    Box(
      modifier = Modifier
        .width(header.contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = header.horizontalPadding, top = header.headerTopPadding, end = header.horizontalPadding)
        .fillMaxWidth()
        .height(header.actionButtonSize * 3f + header.actionSpacing * 2f),
    ) {
      Column(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(top = header.actionTopPadding),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(header.actionSpacing),
      ) {
        OtcHeaderActionSlot(header.actionButtonSize) {
          CircleIconAction(
            size = header.actionButtonSize,
            borderColor = Accent,
            borderWidth = 2.dp,
            icon = {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Accent,
                modifier = Modifier.size(header.actionIconSize),
              )
            },
            onClick = onBack,
          )
        }
        OtcHeaderActionSlot(header.actionButtonSize) {
          if (letterBody.isNotBlank()) {
            CircleIconAction(
              size = header.actionButtonSize,
              borderColor = Accent,
              borderWidth = 2.dp,
              icon = {
                Icon(
                  imageVector = Icons.Filled.Check,
                  contentDescription = "Done typing",
                  tint = Accent,
                  modifier = Modifier.size(header.actionIconSize),
                )
              },
              onClick = {
                isReadyToExport = true
              },
            )
          }
        }
        OtcHeaderActionSlot(header.actionButtonSize) {
          if (isReadyToExport && letterBody.isNotBlank()) {
            CircleIconAction(
              size = header.actionButtonSize,
              borderColor = Accent,
              borderWidth = 2.dp,
              icon = {
                Text(
                  text = "PDF",
                  color = Accent,
                  style = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compactScreen) 10.sp else 12.sp,
                  ),
                )
              },
              onClick = {
                val pdfBytes = runCatching {
                  buildLetterheadPdf(context, letterBody)
                }.getOrNull()
                if (pdfBytes == null) {
                  Toast.makeText(context, "Letterhead PDF generation failed.", Toast.LENGTH_SHORT).show()
                } else {
                  val export = PendingInvoiceExport(
                    fileName = buildLetterheadFileName(),
                    pdfBytes = pdfBytes,
                  )
                  pendingExport = export
                  exportLauncher.launch(export.fileName)
                }
              },
            )
          }
        }
      }
    }
  }
}

private fun buildLetterheadFileName(): String {
  return "ASHOKVATIKA_letterhead_${System.currentTimeMillis()}.pdf"
}

@Composable
fun ArtBrochureScreen(
  onBack: () -> Unit,
) {
  val context = LocalContext.current
  val fontFamily = rememberDashboardFont()
  var productName by rememberSaveable { mutableStateOf("Product Title") }
  var description by rememberSaveable {
    mutableStateOf(
      "Product Description, Product Description, Product Description,\n" +
        "Product Description, Product Description, Product Description,\n" +
        "Product Description, Product Description,\n" +
        "Product Description, Product Description,",
    )
  }
  var mediaUris by rememberSaveable { mutableStateOf(emptyList<String>()) }
  var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

  val brochureDriveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
    if (uri != null) {
      try {
        context.contentResolver.takePersistableUriPermission(
          uri,
          android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
      } catch (_: SecurityException) {
      }
      mediaUris = listOf(uri.toString())
    }
  }
  val brochureCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { didSave ->
    if (didSave) {
      val capturedUri = pendingCameraUri?.toString()
      if (capturedUri != null) {
        mediaUris = listOf(capturedUri)
      }
    } else {
      pendingCameraUri = null
    }
  }

  BackHandler(onBack = onBack)

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Bg)
      .navigationBarsPadding()
      .padding(horizontal = 16.dp, vertical = 16.dp),
  ) {
    val compactScreen = isCompactPhoneScreen(maxWidth, maxHeight)
    val header = rememberMenuHeaderSpec(maxWidth, maxHeight)
    val topContentPadding = if (isPhoneScreen(maxWidth)) maxHeight * 0.12f else 20.dp

    Column(
      modifier = Modifier
        .width(header.contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = header.horizontalPadding, top = topContentPadding, end = header.horizontalPadding, bottom = 12.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      BrochureScreen(
        mediaUris = mediaUris,
        productName = productName,
        onProductNameChange = { productName = it },
        description = description,
        onDescriptionChange = { description = it },
        contactText = "",
        onAddImageFromDrive = { brochureDriveLauncher.launch(arrayOf("image/*")) },
        onAddImageFromCamera = {
          val outputUri = createCameraImageUri(context)
          pendingCameraUri = outputUri
          brochureCameraLauncher.launch(outputUri)
        },
        fontFamily = fontFamily,
        compactScreen = compactScreen,
        standalonePage = true,
      )
    }

    MenuHubHeader(
      modifier = Modifier
        .width(header.contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = header.horizontalPadding, top = header.headerTopPadding, end = header.horizontalPadding),
      onBack = onBack,
      actionButtonSize = header.actionButtonSize,
      actionIconSize = header.actionIconSize,
      actionSpacing = header.actionSpacing,
      actionTopPadding = header.actionTopPadding,
    )
  }
}

@Composable
fun ArtProfileScreen(
  onBack: () -> Unit,
) {
  val context = LocalContext.current
  val fontFamily = rememberDashboardFont()
  var productName by rememberSaveable { mutableStateOf("Product Title") }
  var description by rememberSaveable {
    mutableStateOf(
      "Product Description, Product Description,\n" +
        "Product Description, Product Description,\n" +
        "Product Description, Product Description,\n" +
        "Product Description, Product Description,",
    )
  }
  var mediaUris by rememberSaveable { mutableStateOf(emptyList<String>()) }

  val profileDriveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
    if (uri != null) {
      try {
        context.contentResolver.takePersistableUriPermission(
          uri,
          android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
      } catch (_: SecurityException) {
      }
      mediaUris = listOf(uri.toString())
    }
  }

  BackHandler(onBack = onBack)

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Bg)
      .navigationBarsPadding(),
  ) {
    val configuration = LocalConfiguration.current
    val physicalTablet = configuration.smallestScreenWidthDp >= 600
    val phoneScreen = !physicalTablet && isPhoneScreen(maxWidth)
    val compactScreen = isCompactPhoneScreen(maxWidth, maxHeight)
    val statusBarTopPaddingPx = ViewCompat.getRootWindowInsets(LocalView.current)
      ?.getInsets(WindowInsetsCompat.Type.statusBars())
      ?.top ?: 0
    val statusBarTopPadding = with(LocalDensity.current) { statusBarTopPaddingPx.toDp() }
    val contentWidth = if (phoneScreen) maxWidth else minOf(maxWidth - if (compactScreen) 20.dp else 32.dp, 920.dp)
    val horizontalPadding = if (phoneScreen) OtcPhoneHeaderHorizontalPadding else if (compactScreen) 10.dp else 16.dp
    val headerTopPadding = if (phoneScreen) statusBarTopPadding + OtcPhoneHeaderTopPadding else OtcHeaderTopOffset
    val headerActionSize = if (phoneScreen) OtcPhoneHeaderActionSize else LockedHeaderActionSize
    val headerIconSize = if (phoneScreen) OtcPhoneHeaderIconSize else LockedHeaderIconSize
    val headerSpacing = if (phoneScreen) OtcPhoneHeaderSpacing else LockedHeaderActionSpacing
    val topBarTextSize = when {
      maxWidth > 700.dp -> 34.sp
      compactScreen -> 22.sp
      phoneScreen -> OtcPhoneHeaderLogoTextSize
      else -> 28.sp
    }
    val topBarMarkSize = when {
      maxWidth > 700.dp -> 76.dp
      compactScreen -> 46.dp
      phoneScreen -> OtcPhoneHeaderLogoMarkSize
      else -> 60.dp
    }
    val topContentPadding = if (phoneScreen) headerTopPadding + OtcPhoneHeaderLogoHeight + 10.dp else 76.dp

    Column(
      modifier = Modifier
        .width(contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = horizontalPadding, top = topContentPadding, end = horizontalPadding, bottom = 12.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      BrochureTwoScreen(
        mediaUris = mediaUris,
        productName = productName,
        onProductNameChange = { productName = it },
        description = description,
        onDescriptionChange = { description = it },
        contactText = "",
        onAddImage = { _ -> profileDriveLauncher.launch(arrayOf("image/*")) },
        fontFamily = fontFamily,
        standalonePage = true,
        showBottomGrid = false,
      )
    }

    OtcActionOnlyHeader(
      modifier = Modifier
        .width(contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = horizontalPadding, top = headerTopPadding, end = horizontalPadding),
      onBack = onBack,
      actionButtonSize = headerActionSize,
      actionIconSize = headerIconSize,
      actionSpacing = headerSpacing,
      containerHeight = if (phoneScreen) OtcHeaderFrameHeight else Dp.Unspecified,
      logoWidth = if (phoneScreen) OtcPhoneHeaderLogoWidth else 260.dp,
      logoHeight = if (phoneScreen) OtcPhoneHeaderLogoHeight else OtcHeaderLogoHeight,
      logoTextSize = topBarTextSize,
      logoMarkSize = topBarMarkSize,
      actionTopPadding = if (phoneScreen) OtcPhoneHeaderActionTopPadding else 0.dp,
    )
  }
}

@Composable
fun ArtProPlusScreen(
  onBack: () -> Unit,
) {
  val context = LocalContext.current
  val fontFamily = rememberDashboardFont()
  var productName by rememberSaveable { mutableStateOf("Product Title") }
  var description by rememberSaveable {
    mutableStateOf(
      "Product Description, Product Description,\n" +
        "Product Description, Product Description,\n" +
        "Product Description, Product Description,\n" +
        "Product Description, Product Description,",
    )
  }
  var mediaUris by rememberSaveable { mutableStateOf(emptyList<String>()) }
  var pendingImageSlot by rememberSaveable { mutableIntStateOf(0) }
  var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

  val proPlusDriveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
    if (uri != null) {
      try {
        context.contentResolver.takePersistableUriPermission(
          uri,
          android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
      } catch (_: SecurityException) {
      }
      val uriString = uri.toString()
      val imageIndexes = mediaUris.mapIndexedNotNull { index, existingUri ->
        index.takeIf { !isVideoUri(context, existingUri) }
      }
      mediaUris = if (pendingImageSlot < imageIndexes.size) {
        mediaUris.toMutableList().apply {
          this[imageIndexes[pendingImageSlot]] = uriString
        }
      } else {
        mediaUris + uriString
      }
    }
  }
  val proPlusCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { didSave ->
    if (didSave) {
      val capturedUri = pendingCameraUri?.toString()
      if (capturedUri != null) {
        val imageIndexes = mediaUris.mapIndexedNotNull { index, existingUri ->
          index.takeIf { !isVideoUri(context, existingUri) }
        }
        mediaUris = if (pendingImageSlot < imageIndexes.size) {
          mediaUris.toMutableList().apply {
            this[imageIndexes[pendingImageSlot]] = capturedUri
          }
        } else {
          mediaUris + capturedUri
        }
      }
    } else {
      pendingCameraUri = null
    }
  }

  BackHandler(onBack = onBack)

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Bg)
      .navigationBarsPadding(),
  ) {
    val configuration = LocalConfiguration.current
    val physicalTablet = configuration.smallestScreenWidthDp >= 600
    val phoneScreen = !physicalTablet && isPhoneScreen(maxWidth)
    val compactScreen = isCompactPhoneScreen(maxWidth, maxHeight)
    val statusBarTopPaddingPx = ViewCompat.getRootWindowInsets(LocalView.current)
      ?.getInsets(WindowInsetsCompat.Type.statusBars())
      ?.top ?: 0
    val statusBarTopPadding = with(LocalDensity.current) { statusBarTopPaddingPx.toDp() }
    val contentWidth = if (phoneScreen) maxWidth else minOf(maxWidth - if (compactScreen) 20.dp else 32.dp, 920.dp)
    val horizontalPadding = if (phoneScreen) OtcPhoneHeaderHorizontalPadding else if (compactScreen) 10.dp else 16.dp
    val headerTopPadding = if (phoneScreen) statusBarTopPadding + OtcPhoneHeaderTopPadding else OtcHeaderTopOffset
    val headerActionSize = if (phoneScreen) OtcPhoneHeaderActionSize else LockedHeaderActionSize
    val headerIconSize = if (phoneScreen) OtcPhoneHeaderIconSize else LockedHeaderIconSize
    val headerSpacing = if (phoneScreen) OtcPhoneHeaderSpacing else LockedHeaderActionSpacing
    val topBarTextSize = when {
      maxWidth > 700.dp -> 34.sp
      compactScreen -> 22.sp
      phoneScreen -> OtcPhoneHeaderLogoTextSize
      else -> 28.sp
    }
    val topBarMarkSize = when {
      maxWidth > 700.dp -> 76.dp
      compactScreen -> 46.dp
      phoneScreen -> OtcPhoneHeaderLogoMarkSize
      else -> 60.dp
    }
    val topContentPadding = if (phoneScreen) headerTopPadding + OtcPhoneHeaderLogoHeight + 10.dp else 76.dp

    Column(
      modifier = Modifier
        .width(contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = horizontalPadding, top = topContentPadding, end = horizontalPadding, bottom = 12.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      BrochureTwoScreen(
        mediaUris = mediaUris,
        productName = productName,
        onProductNameChange = { productName = it },
        description = description,
        onDescriptionChange = { description = it },
        contactText = "",
        onAddImage = { slotIndex ->
          pendingImageSlot = slotIndex
          proPlusDriveLauncher.launch(arrayOf("image/*"))
        },
        onAddImageFromCamera = { slotIndex ->
          pendingImageSlot = slotIndex
          val outputUri = createCameraImageUri(context)
          pendingCameraUri = outputUri
          proPlusCameraLauncher.launch(outputUri)
        },
        fontFamily = fontFamily,
        standalonePage = true,
        showBottomGrid = true,
        showLogo = false,
        useContainerBackground = false,
        useUniformImageSizes = true,
      )
    }

    OtcActionOnlyHeader(
      modifier = Modifier
        .width(contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = horizontalPadding, top = headerTopPadding, end = horizontalPadding),
      onBack = onBack,
      actionButtonSize = headerActionSize,
      actionIconSize = headerIconSize,
      actionSpacing = headerSpacing,
      containerHeight = if (phoneScreen) OtcHeaderFrameHeight else Dp.Unspecified,
      logoWidth = if (phoneScreen) OtcPhoneHeaderLogoWidth else 260.dp,
      logoHeight = if (phoneScreen) OtcPhoneHeaderLogoHeight else OtcHeaderLogoHeight,
      logoTextSize = topBarTextSize,
      logoMarkSize = topBarMarkSize,
      actionTopPadding = if (phoneScreen) OtcPhoneHeaderActionTopPadding else 0.dp,
    )
  }
}

@Composable
fun ArtCatTwoScreen(
  onBack: () -> Unit,
) {
  val context = LocalContext.current
  val fontFamily = rememberDashboardFont()
  var firstSku by rememberSaveable { mutableStateOf("SKU#") }
  var secondSku by rememberSaveable { mutableStateOf("SKU#") }
  var firstTitle by rememberSaveable { mutableStateOf("Product Title") }
  var secondTitle by rememberSaveable { mutableStateOf("Product Title") }
  var firstDescription by rememberSaveable {
    mutableStateOf(
      "Product Description, Product Description,\n" +
        "Product Description, Product Description,\n" +
        "Product Description, Product Description,\n" +
        "Product Description, Product Description,",
    )
  }
  var secondDescription by rememberSaveable {
    mutableStateOf(
      "Product Description, Product Description,\n" +
        "Product Description, Product Description,\n" +
        "Product Description, Product Description,\n" +
        "Product Description, Product Description,",
    )
  }
  var mediaUris by rememberSaveable { mutableStateOf(emptyList<String>()) }
  var pendingImageSlot by rememberSaveable { mutableIntStateOf(0) }
  var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

  val catTwoDriveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
    if (uri != null) {
      try {
        context.contentResolver.takePersistableUriPermission(
          uri,
          android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
      } catch (_: SecurityException) {
      }
      val uriString = uri.toString()
      val imageIndexes = mediaUris.mapIndexedNotNull { index, existingUri ->
        index.takeIf { !isVideoUri(context, existingUri) }
      }
      mediaUris = if (pendingImageSlot < imageIndexes.size) {
        mediaUris.toMutableList().apply {
          this[imageIndexes[pendingImageSlot]] = uriString
        }
      } else {
        mediaUris + uriString
      }
    }
  }
  val catTwoCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { didSave ->
    if (didSave) {
      val capturedUri = pendingCameraUri?.toString()
      if (capturedUri != null) {
        val imageIndexes = mediaUris.mapIndexedNotNull { index, existingUri ->
          index.takeIf { !isVideoUri(context, existingUri) }
        }
        mediaUris = if (pendingImageSlot < imageIndexes.size) {
          mediaUris.toMutableList().apply {
            this[imageIndexes[pendingImageSlot]] = capturedUri
          }
        } else {
          mediaUris + capturedUri
        }
      }
    } else {
      pendingCameraUri = null
    }
  }

  BackHandler(onBack = onBack)

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Bg)
      .navigationBarsPadding(),
  ) {
    val configuration = LocalConfiguration.current
    val physicalTablet = configuration.smallestScreenWidthDp >= 600
    val phoneScreen = !physicalTablet && isPhoneScreen(maxWidth)
    val compactScreen = isCompactPhoneScreen(maxWidth, maxHeight)
    val statusBarTopPaddingPx = ViewCompat.getRootWindowInsets(LocalView.current)
      ?.getInsets(WindowInsetsCompat.Type.statusBars())
      ?.top ?: 0
    val statusBarTopPadding = with(LocalDensity.current) { statusBarTopPaddingPx.toDp() }
    val contentWidth = if (phoneScreen) maxWidth else minOf(maxWidth - if (compactScreen) 20.dp else 32.dp, 920.dp)
    val horizontalPadding = if (phoneScreen) OtcPhoneHeaderHorizontalPadding else if (compactScreen) 10.dp else 16.dp
    val headerTopPadding = if (phoneScreen) statusBarTopPadding + OtcPhoneHeaderTopPadding else OtcHeaderTopOffset
    val headerActionSize = if (phoneScreen) OtcPhoneHeaderActionSize else LockedHeaderActionSize
    val headerIconSize = if (phoneScreen) OtcPhoneHeaderIconSize else LockedHeaderIconSize
    val headerSpacing = if (phoneScreen) OtcPhoneHeaderSpacing else LockedHeaderActionSpacing
    val topBarTextSize = when {
      maxWidth > 700.dp -> 34.sp
      compactScreen -> 22.sp
      phoneScreen -> OtcPhoneHeaderLogoTextSize
      else -> 28.sp
    }
    val topBarMarkSize = when {
      maxWidth > 700.dp -> 76.dp
      compactScreen -> 46.dp
      phoneScreen -> OtcPhoneHeaderLogoMarkSize
      else -> 60.dp
    }
    val topContentPadding = if (phoneScreen) headerTopPadding + OtcPhoneHeaderLogoHeight + 10.dp else 76.dp

    Column(
      modifier = Modifier
        .width(contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = horizontalPadding, top = topContentPadding, end = horizontalPadding, bottom = 12.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      CatTwoScreen(
        mediaUris = mediaUris,
        firstSku = firstSku,
        onFirstSkuChange = { firstSku = it },
        firstTitle = firstTitle,
        onFirstTitleChange = { firstTitle = it },
        firstDescription = firstDescription,
        onFirstDescriptionChange = { firstDescription = it },
        secondSku = secondSku,
        onSecondSkuChange = { secondSku = it },
        secondTitle = secondTitle,
        onSecondTitleChange = { secondTitle = it },
        secondDescription = secondDescription,
        onSecondDescriptionChange = { secondDescription = it },
        contactText = "",
        onAddImage = { slotIndex ->
          pendingImageSlot = slotIndex
          catTwoDriveLauncher.launch(arrayOf("image/*"))
        },
        onAddImageFromCamera = { slotIndex ->
          pendingImageSlot = slotIndex
          val outputUri = createCameraImageUri(context)
          pendingCameraUri = outputUri
          catTwoCameraLauncher.launch(outputUri)
        },
        fontFamily = fontFamily,
        showLogo = false,
      )
    }

    OtcActionOnlyHeader(
      modifier = Modifier
        .width(contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = horizontalPadding, top = headerTopPadding, end = horizontalPadding),
      onBack = onBack,
      actionButtonSize = headerActionSize,
      actionIconSize = headerIconSize,
      actionSpacing = headerSpacing,
      containerHeight = if (phoneScreen) OtcHeaderFrameHeight else Dp.Unspecified,
      logoWidth = if (phoneScreen) OtcPhoneHeaderLogoWidth else 260.dp,
      logoHeight = if (phoneScreen) OtcPhoneHeaderLogoHeight else OtcHeaderLogoHeight,
      logoTextSize = topBarTextSize,
      logoMarkSize = topBarMarkSize,
      actionTopPadding = if (phoneScreen) OtcPhoneHeaderActionTopPadding else 0.dp,
    )
  }
}

@Composable
fun ArtCatFourScreen(
  onBack: () -> Unit,
) {
  val context = LocalContext.current
  val fontFamily = rememberDashboardFont()
  var firstSku by rememberSaveable { mutableStateOf("SKU#") }
  var secondSku by rememberSaveable { mutableStateOf("SKU#") }
  var thirdSku by rememberSaveable { mutableStateOf("SKU#") }
  var fourthSku by rememberSaveable { mutableStateOf("SKU#") }
  var firstTitle by rememberSaveable { mutableStateOf("Product Title") }
  var secondTitle by rememberSaveable { mutableStateOf("Product Title") }
  var thirdTitle by rememberSaveable { mutableStateOf("Product Title") }
  var fourthTitle by rememberSaveable { mutableStateOf("Product Title") }
  var firstDescription by rememberSaveable { mutableStateOf(defaultCatalogDescription()) }
  var secondDescription by rememberSaveable { mutableStateOf(defaultCatalogDescription()) }
  var thirdDescription by rememberSaveable { mutableStateOf(defaultCatalogDescription()) }
  var fourthDescription by rememberSaveable { mutableStateOf(defaultCatalogDescription()) }
  var mediaUris by rememberSaveable { mutableStateOf(emptyList<String>()) }
  var pendingImageSlot by rememberSaveable { mutableIntStateOf(0) }

  val catFourDriveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
    if (uri != null) {
      try {
        context.contentResolver.takePersistableUriPermission(
          uri,
          android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
      } catch (_: SecurityException) {
      }
      val uriString = uri.toString()
      val imageIndexes = mediaUris.mapIndexedNotNull { index, existingUri ->
        index.takeIf { !isVideoUri(context, existingUri) }
      }
      mediaUris = if (pendingImageSlot < imageIndexes.size) {
        mediaUris.toMutableList().apply {
          this[imageIndexes[pendingImageSlot]] = uriString
        }
      } else {
        mediaUris + uriString
      }
    }
  }

  BackHandler(onBack = onBack)

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Bg)
      .navigationBarsPadding(),
  ) {
    val configuration = LocalConfiguration.current
    val physicalTablet = configuration.smallestScreenWidthDp >= 600
    val phoneScreen = !physicalTablet && isPhoneScreen(maxWidth)
    val compactScreen = isCompactPhoneScreen(maxWidth, maxHeight)
    val statusBarTopPaddingPx = ViewCompat.getRootWindowInsets(LocalView.current)
      ?.getInsets(WindowInsetsCompat.Type.statusBars())
      ?.top ?: 0
    val statusBarTopPadding = with(LocalDensity.current) { statusBarTopPaddingPx.toDp() }
    val contentWidth = if (phoneScreen) maxWidth else minOf(maxWidth - if (compactScreen) 20.dp else 32.dp, 920.dp)
    val horizontalPadding = if (phoneScreen) OtcPhoneHeaderHorizontalPadding else if (compactScreen) 10.dp else 16.dp
    val headerTopPadding = if (phoneScreen) statusBarTopPadding + OtcPhoneHeaderTopPadding else OtcHeaderTopOffset
    val headerActionSize = if (phoneScreen) OtcPhoneHeaderActionSize else LockedHeaderActionSize
    val headerIconSize = if (phoneScreen) OtcPhoneHeaderIconSize else LockedHeaderIconSize
    val headerSpacing = if (phoneScreen) OtcPhoneHeaderSpacing else LockedHeaderActionSpacing
    val topBarTextSize = when {
      maxWidth > 700.dp -> 34.sp
      compactScreen -> 22.sp
      phoneScreen -> OtcPhoneHeaderLogoTextSize
      else -> 28.sp
    }
    val topBarMarkSize = when {
      maxWidth > 700.dp -> 76.dp
      compactScreen -> 46.dp
      phoneScreen -> OtcPhoneHeaderLogoMarkSize
      else -> 60.dp
    }
    val topContentPadding = if (phoneScreen) headerTopPadding + OtcPhoneHeaderLogoHeight + 10.dp else 76.dp

    Column(
      modifier = Modifier
        .width(contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = horizontalPadding, top = topContentPadding, end = horizontalPadding, bottom = 12.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      CatCatalogScreen(
        mediaUris = mediaUris,
        items = listOf(
          CatCatalogItemState(firstSku, { firstSku = it }, firstTitle, { firstTitle = it }, firstDescription, { firstDescription = it }, useCameraPlaceholder = true),
          CatCatalogItemState(secondSku, { secondSku = it }, secondTitle, { secondTitle = it }, secondDescription, { secondDescription = it }, useCameraPlaceholder = true),
          CatCatalogItemState(thirdSku, { thirdSku = it }, thirdTitle, { thirdTitle = it }, thirdDescription, { thirdDescription = it }, useCameraPlaceholder = true),
          CatCatalogItemState(fourthSku, { fourthSku = it }, fourthTitle, { fourthTitle = it }, fourthDescription, { fourthDescription = it }, useCameraPlaceholder = true),
        ),
        contactText = "",
        onAddImage = { slotIndex ->
          pendingImageSlot = slotIndex
          catFourDriveLauncher.launch(arrayOf("image/*"))
        },
        fontFamily = fontFamily,
        showLogo = false,
      )
    }

    OtcActionOnlyHeader(
      modifier = Modifier
        .width(contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = horizontalPadding, top = headerTopPadding, end = horizontalPadding),
      onBack = onBack,
      actionButtonSize = headerActionSize,
      actionIconSize = headerIconSize,
      actionSpacing = headerSpacing,
      containerHeight = if (phoneScreen) OtcHeaderFrameHeight else Dp.Unspecified,
      logoWidth = if (phoneScreen) OtcPhoneHeaderLogoWidth else 260.dp,
      logoHeight = if (phoneScreen) OtcPhoneHeaderLogoHeight else OtcHeaderLogoHeight,
      logoTextSize = topBarTextSize,
      logoMarkSize = topBarMarkSize,
      actionTopPadding = if (phoneScreen) OtcPhoneHeaderActionTopPadding else 0.dp,
    )
  }
}

@Composable
fun ArtCatPlusScreen(
  onBack: () -> Unit,
) {
  val context = LocalContext.current
  val fontFamily = rememberDashboardFont()
  var firstSku by rememberSaveable { mutableStateOf("SKU#") }
  var secondSku by rememberSaveable { mutableStateOf("SKU#") }
  var thirdSku by rememberSaveable { mutableStateOf("SKU#") }
  var fourthSku by rememberSaveable { mutableStateOf("SKU#") }
  var firstTitle by rememberSaveable { mutableStateOf("Product Title") }
  var secondTitle by rememberSaveable { mutableStateOf("Product Title") }
  var thirdTitle by rememberSaveable { mutableStateOf("Product Title") }
  var fourthTitle by rememberSaveable { mutableStateOf("Product Title") }
  var firstDescription by rememberSaveable { mutableStateOf(defaultCatalogDescription()) }
  var secondDescription by rememberSaveable { mutableStateOf(defaultCatalogDescription()) }
  var thirdDescription by rememberSaveable { mutableStateOf(defaultCatalogDescription()) }
  var fourthDescription by rememberSaveable { mutableStateOf(defaultCatalogDescription()) }
  var mediaUris by rememberSaveable { mutableStateOf(emptyList<String>()) }
  var pendingImageSlot by rememberSaveable { mutableIntStateOf(0) }

  val catPlusDriveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
    if (uri != null) {
      try {
        context.contentResolver.takePersistableUriPermission(
          uri,
          android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
      } catch (_: SecurityException) {
      }
      val uriString = uri.toString()
      val imageIndexes = mediaUris.mapIndexedNotNull { index, existingUri ->
        index.takeIf { !isVideoUri(context, existingUri) }
      }
      mediaUris = if (pendingImageSlot < imageIndexes.size) {
        mediaUris.toMutableList().apply {
          this[imageIndexes[pendingImageSlot]] = uriString
        }
      } else {
        mediaUris + uriString
      }
    }
  }

  BackHandler(onBack = onBack)

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Bg)
      .navigationBarsPadding(),
  ) {
    val configuration = LocalConfiguration.current
    val physicalTablet = configuration.smallestScreenWidthDp >= 600
    val phoneScreen = !physicalTablet && isPhoneScreen(maxWidth)
    val compactScreen = isCompactPhoneScreen(maxWidth, maxHeight)
    val statusBarTopPaddingPx = ViewCompat.getRootWindowInsets(LocalView.current)
      ?.getInsets(WindowInsetsCompat.Type.statusBars())
      ?.top ?: 0
    val statusBarTopPadding = with(LocalDensity.current) { statusBarTopPaddingPx.toDp() }
    val contentWidth = if (phoneScreen) maxWidth else minOf(maxWidth - if (compactScreen) 20.dp else 32.dp, 920.dp)
    val horizontalPadding = if (phoneScreen) OtcPhoneHeaderHorizontalPadding else if (compactScreen) 10.dp else 16.dp
    val headerTopPadding = if (phoneScreen) statusBarTopPadding + OtcPhoneHeaderTopPadding else OtcHeaderTopOffset
    val headerActionSize = if (phoneScreen) OtcPhoneHeaderActionSize else LockedHeaderActionSize
    val headerIconSize = if (phoneScreen) OtcPhoneHeaderIconSize else LockedHeaderIconSize
    val headerSpacing = if (phoneScreen) OtcPhoneHeaderSpacing else LockedHeaderActionSpacing
    val topBarTextSize = when {
      maxWidth > 700.dp -> 34.sp
      compactScreen -> 22.sp
      phoneScreen -> OtcPhoneHeaderLogoTextSize
      else -> 28.sp
    }
    val topBarMarkSize = when {
      maxWidth > 700.dp -> 76.dp
      compactScreen -> 46.dp
      phoneScreen -> OtcPhoneHeaderLogoMarkSize
      else -> 60.dp
    }
    val topContentPadding = if (phoneScreen) headerTopPadding + OtcPhoneHeaderLogoHeight + 10.dp else 76.dp

    Column(
      modifier = Modifier
        .width(contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = horizontalPadding, top = topContentPadding, end = horizontalPadding, bottom = 12.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      CatPlusCatalogScreen(
        mediaUris = mediaUris,
        items = listOf(
          CatCatalogItemState(firstSku, { firstSku = it }, firstTitle, { firstTitle = it }, firstDescription, { firstDescription = it }),
          CatCatalogItemState(secondSku, { secondSku = it }, secondTitle, { secondTitle = it }, secondDescription, { secondDescription = it }),
          CatCatalogItemState(thirdSku, { thirdSku = it }, thirdTitle, { thirdTitle = it }, thirdDescription, { thirdDescription = it }),
          CatCatalogItemState(fourthSku, { fourthSku = it }, fourthTitle, { fourthTitle = it }, fourthDescription, { fourthDescription = it }),
        ),
        contactText = "",
        onAddImage = { slotIndex ->
          pendingImageSlot = slotIndex
          catPlusDriveLauncher.launch(arrayOf("image/*"))
        },
        fontFamily = fontFamily,
      )
    }

    OtcActionOnlyHeader(
      modifier = Modifier
        .width(contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = horizontalPadding, top = headerTopPadding, end = horizontalPadding),
      onBack = onBack,
      actionButtonSize = headerActionSize,
      actionIconSize = headerIconSize,
      actionSpacing = headerSpacing,
      containerHeight = if (phoneScreen) OtcHeaderFrameHeight else Dp.Unspecified,
      logoWidth = if (phoneScreen) OtcPhoneHeaderLogoWidth else 260.dp,
      logoHeight = if (phoneScreen) OtcPhoneHeaderLogoHeight else OtcHeaderLogoHeight,
      logoTextSize = topBarTextSize,
      logoMarkSize = topBarMarkSize,
      actionTopPadding = if (phoneScreen) OtcPhoneHeaderActionTopPadding else 0.dp,
    )
  }
}

@Composable
private fun CatPlusCatalogScreen(
  mediaUris: List<String>,
  items: List<CatCatalogItemState>,
  contactText: String,
  onAddImage: (Int) -> Unit,
  fontFamily: FontFamily,
) {
  val context = LocalContext.current
  val images = remember(mediaUris) { mediaUris.filterNot { isVideoUri(context, it) } }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
      val tabletLayout = maxWidth >= 760.dp
      val layoutWidth = if (tabletLayout) minOf(maxWidth * 0.9f, 820.dp) else maxWidth

      Column(
        modifier = Modifier
          .width(layoutWidth)
          .align(Alignment.TopCenter),
        verticalArrangement = Arrangement.spacedBy(if (tabletLayout) 14.dp else 12.dp),
      ) {
        CatTwoHeader(
          contactText = contactText,
          fontFamily = fontFamily,
          tabletLayout = tabletLayout,
          showLogo = false,
        )
        items.forEachIndexed { index, item ->
          CatPlusItemRow(
            sku = item.sku,
            onSkuChange = item.onSkuChange,
            title = item.title,
            onTitleChange = item.onTitleChange,
            description = item.description,
            onDescriptionChange = item.onDescriptionChange,
            uriString = images.getOrNull(index),
            onAddImage = { onAddImage(index) },
            fontFamily = fontFamily,
            tabletLayout = tabletLayout,
          )
        }
      }
    }
  }
}

@Composable
private fun CatPlusItemRow(
  sku: String,
  onSkuChange: (String) -> Unit,
  title: String,
  onTitleChange: (String) -> Unit,
  description: String,
  onDescriptionChange: (String) -> Unit,
  uriString: String?,
  onAddImage: () -> Unit,
  fontFamily: FontFamily,
  tabletLayout: Boolean,
) {
  val imageHeight = if (tabletLayout) 220.dp else 146.dp
  val skuSize = if (tabletLayout) 12.sp else 9.sp
  val titleSize = if (tabletLayout) 20.sp else 14.sp
  val titleLineHeight = if (tabletLayout) 24.sp else 17.sp
  val descriptionSize = if (tabletLayout) 10.5.sp else 7.5.sp
  val descriptionLineHeight = if (tabletLayout) 14.sp else 10.sp

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(if (tabletLayout) 8.dp else 6.dp),
  ) {
    TemplateEditableText(
      value = sku,
      onValueChange = onSkuChange,
      placeholder = "SKU#",
      fontFamily = fontFamily,
      textAlign = TextAlign.Start,
      fontSize = skuSize,
      color = Ink.copy(alpha = 0.72f),
      lineHeight = skuSize * 1.2f,
      fontWeight = FontWeight.SemiBold,
      clearOnFirstFocusValue = "SKU#",
      widthFraction = 0.18f,
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(if (tabletLayout) 12.dp else 10.dp),
      verticalAlignment = Alignment.Top,
    ) {
      BrochureImageContainer(
        uriString = uriString,
        onAddImageFromDrive = onAddImage,
        onAddImageFromCamera = null,
        modifier = Modifier.weight(0.62f),
        height = imageHeight,
        placeholderIcon = Icons.Filled.PhotoCamera,
        placeholderIconDescription = "Camera",
        showActionButtons = false,
        openPickerOnContainerTap = true,
      )
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(if (tabletLayout) 10.dp else 7.dp),
      ) {
        TemplateEditableText(
          value = title,
          onValueChange = onTitleChange,
          placeholder = "Product Title",
          fontFamily = fontFamily,
          textAlign = TextAlign.Start,
          fontSize = titleSize,
          color = Color(0xFF727272),
          lineHeight = titleLineHeight,
          fontWeight = FontWeight.SemiBold,
          clearOnFirstFocusValue = "Product Title",
        )
        TemplateEditableText(
          value = description,
          onValueChange = onDescriptionChange,
          placeholder = "Product Description, Product Description, Product Description, Product\nDescription, Product Description, Product Description, Product\nDescription, Product Description, Product Description,",
          fontFamily = fontFamily,
          textAlign = TextAlign.Start,
          fontSize = descriptionSize,
          color = Ink.copy(alpha = 0.62f),
          lineHeight = descriptionLineHeight,
          minLines = 4,
          fontWeight = FontWeight.Medium,
          clearPlaceholderOnFocus = true,
          clearOnFirstFocusValue = defaultCatalogDescription(),
        )
        Row(
          horizontalArrangement = Arrangement.spacedBy(if (tabletLayout) 14.dp else 10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          BrochureCircleAction(
            onClick = {},
            icon = Icons.Filled.WbSunny,
            tint = Ink.copy(alpha = 0.38f),
          )
          BrochureCircleAction(
            onClick = {},
            icon = Icons.Filled.WaterDrop,
            tint = Ink.copy(alpha = 0.38f),
          )
        }
      }
    }
  }
}

@Composable
private fun AddressInfoAction(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  text: String,
  fontFamily: FontFamily,
  textSize: TextUnit,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier.clickable(onClick = onClick),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = Accent,
      modifier = Modifier.size((textSize.value * 1.2f).dp),
    )
    Text(
      text = text,
      color = Ink.copy(alpha = 0.56f),
      textAlign = TextAlign.Center,
      style = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = textSize,
        lineHeight = textSize * 1.02f,
      ),
    )
  }
}

@Composable
private fun AddressSocialBadge(
  label: String,
  fontFamily: FontFamily = FontFamily.Default,
  background: Color,
  size: Dp,
  onClick: () -> Unit,
) {
  Surface(
    modifier = Modifier.size(size),
    shape = CircleShape,
    color = background,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
    onClick = onClick,
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(
        text = label,
        color = Color.White,
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.ExtraBold,
          fontSize = (size.value * 0.34f).sp,
        ),
      )
    }
  }
}

private fun launchExternalUri(
  context: android.content.Context,
  uri: String,
) {
  runCatching {
    context.startActivity(
      android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)).apply {
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
      },
    )
  }
}

@Composable
private fun PendingAccessApprovalCard(
  requests: List<AdminAccessRequest>,
  fontFamily: FontFamily,
  onApproveRequest: (String) -> Unit,
  onDenyRequest: (String) -> Unit,
) {
  val activeRequest = requests.firstOrNull() ?: return

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = CardShape,
    color = Color.White,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = if (requests.size > 1) "Pending customer approvals (${requests.size})" else "Pending customer approval",
        color = AccentGreen,
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Normal,
          fontSize = 22.sp,
        ),
      )
      Text(
        text = activeRequest.customerName,
        color = Ink,
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Normal,
          fontSize = 26.sp,
        ),
      )
      Text(
        text = activeRequest.customerPhone.ifBlank { "Phone not available" },
        color = Accent,
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Normal,
          fontSize = 18.sp,
        ),
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        ActionButtonCard(
          title = "Approve",
          color = AccentGreen,
          fontFamily = fontFamily,
          onClick = { onApproveRequest(activeRequest.requestId) },
          modifier = Modifier.weight(1f),
        )
        ActionButtonCard(
          title = "Deny",
          color = Accent,
          fontFamily = fontFamily,
          onClick = { onDenyRequest(activeRequest.requestId) },
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
fun CategoryScreen(
  title: DashboardDestination,
  initialItem: InventoryItem?,
  onConfirmBack: (InventoryItem) -> Unit,
  onIncompleteBack: (InventoryItem) -> Unit,
) {
  val dmSansThin = rememberDashboardFont()
  val seed = remember(title, initialItem) { initialItem?.toProductDetail() ?: detailFor(title) }
  val context = LocalContext.current
  val initialItemId = initialItem?.id
  val initialMediaUris = remember(seed.mediaUris, seed.imageUri) {
    seed.mediaUris.ifEmpty { listOfNotNull(seed.imageUri) }
  }

  var productName by rememberSaveable(title.name, initialItemId) { mutableStateOf(seed.name) }
  var batchText by rememberSaveable(title.name, initialItemId) { mutableStateOf(seed.batch) }
  var saleText by rememberSaveable(title.name, initialItemId) { mutableStateOf(seed.sale) }
  var rentText by rememberSaveable(title.name, initialItemId) { mutableStateOf(seed.rent) }
  var detailsText by rememberSaveable(title.name, initialItemId) { mutableStateOf(seed.details) }
  var priceText by rememberSaveable(title.name, initialItemId) { mutableStateOf(seed.price) }
  var checkoutText by rememberSaveable(title.name, initialItemId) { mutableStateOf(seed.checkout) }
  var showDetailsPopup by rememberSaveable(title.name, initialItemId) { mutableStateOf(false) }
  var showDetailsCalendar by rememberSaveable(title.name, initialItemId) { mutableStateOf(false) }
  var vendorNameText by rememberSaveable(title.name, initialItemId) { mutableStateOf("") }
  var vendorPhoneText by rememberSaveable(title.name, initialItemId) { mutableStateOf("") }
  var vendorEmailText by rememberSaveable(title.name, initialItemId) { mutableStateOf("") }
  var landingPriceText by rememberSaveable(title.name, initialItemId) { mutableStateOf("") }
  var othersText by rememberSaveable(title.name, initialItemId) { mutableStateOf("") }
  var markupText by rememberSaveable(title.name, initialItemId) { mutableStateOf("") }
  var selectedDetailsDate by rememberSaveable(title.name, initialItemId) { mutableStateOf(seed.details) }
  var mediaUris by rememberSaveable(title.name, initialItemId) { mutableStateOf(initialMediaUris) }
  var mediaPrimaryTitleText by rememberSaveable(title.name, initialItemId) { mutableStateOf(seed.templatePrimaryTitle) }
  var mediaPrimaryDescriptionText by rememberSaveable(title.name, initialItemId) { mutableStateOf(seed.templatePrimaryDescription) }
  var mediaSecondaryTitleText by rememberSaveable(title.name, initialItemId) { mutableStateOf(seed.templateSecondaryTitle) }
  var mediaSecondaryDescriptionText by rememberSaveable(title.name, initialItemId) { mutableStateOf(seed.templateSecondaryDescription) }
  var mediaContactText by rememberSaveable(title.name, initialItemId) { mutableStateOf(seed.templateContact) }
  var showUploadOptions by rememberSaveable(title.name, initialItemId) { mutableStateOf(initialMediaUris.isEmpty()) }
  var showBackConfirm by remember { mutableStateOf(false) }
  var showPricePopup by rememberSaveable(title.name, initialItemId) { mutableStateOf(false) }
  var showCheckoutPopup by rememberSaveable(title.name, initialItemId) { mutableStateOf(false) }
  var showMediaPopup by rememberSaveable(title.name, initialItemId) { mutableStateOf(false) }
  var showPriceValidation by rememberSaveable(title.name, initialItemId) { mutableStateOf(false) }
  var checkoutQuantityText by rememberSaveable(title.name, initialItemId) { mutableStateOf("1") }
  var checkoutDiscountText by rememberSaveable(title.name, initialItemId) { mutableStateOf("0") }
  var checkoutDiscountPercentText by rememberSaveable(title.name, initialItemId) { mutableStateOf("0") }
  var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
  val buildProductCatalog = remember { buildProductCatalog() }
  val selectedBuildProducts = remember { mutableStateMapOf<String, Int>() }
  var selectedBuildCategory by remember { mutableStateOf<BuildProductCategory?>(null) }
  var showBuildProductPicker by remember { mutableStateOf(false) }
  var showBuildSelectionsPopup by remember { mutableStateOf(false) }
  var activeBuildProductItem by remember { mutableStateOf<BuildProductItem?>(null) }
  var buildProductQuantityDraft by remember { mutableStateOf("1") }
  var pendingDeleteSelectionId by remember { mutableStateOf<String?>(null) }
  var cameraCapturePendingConfirmation by remember { mutableStateOf(false) }
  var pendingBrochureImageSlot by remember { mutableIntStateOf(0) }
  val supportsMediaGallery = title != DashboardDestination.OTC

  val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
    if (uris.isNotEmpty()) {
      mediaUris = (mediaUris + uris.map(Uri::toString)).distinct()
      showUploadOptions = false
      cameraCapturePendingConfirmation = false
    }
  }
  val driveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
    if (uris.isNotEmpty()) {
      uris.forEach { uri ->
        try {
          context.contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
          )
        } catch (_: SecurityException) {
        }
      }
      mediaUris = (mediaUris + uris.map(Uri::toString)).distinct()
      showUploadOptions = false
      cameraCapturePendingConfirmation = false
    }
  }
  val mediaPopupDriveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
    if (uris.isNotEmpty()) {
      uris.forEach { uri ->
        try {
          context.contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
          )
        } catch (_: SecurityException) {
        }
      }
      mediaUris = (mediaUris + uris.map(Uri::toString)).distinct()
    }
  }
  val brochureImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
    if (uri != null) {
      try {
        context.contentResolver.takePersistableUriPermission(
          uri,
          android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
      } catch (_: SecurityException) {
      }
      val uriString = uri.toString()
      val imageIndexes = mediaUris.mapIndexedNotNull { index, existingUri ->
        index.takeIf { !isVideoUri(context, existingUri) }
      }
      mediaUris = if (pendingBrochureImageSlot < imageIndexes.size) {
        mediaUris.toMutableList().apply {
          this[imageIndexes[pendingBrochureImageSlot]] = uriString
        }
      } else {
        mediaUris + uriString
      }
    }
  }
  val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { didSave ->
    if (didSave) {
      val capturedUri = pendingCameraUri?.toString()
      if (capturedUri != null) {
        mediaUris = (mediaUris + capturedUri).distinct()
      }
      showUploadOptions = true
      cameraCapturePendingConfirmation = true
    } else {
      pendingCameraUri = null
      cameraCapturePendingConfirmation = false
    }
  }
  val selectedMediaUri = mediaUris.firstOrNull()
  val selectedImageBitmap by rememberDecodedImage(
    uriString = selectedMediaUri?.takeUnless { isVideoUri(context, it) },
    maxDimensionPx = 1400,
  )

  val batch = batchText.toIntOrNull() ?: 0
  val sale = saleText.toIntOrNull() ?: 0
  val rent = rentText.toIntOrNull() ?: 0
  val availableStock = (batch - sale - rent).coerceAtLeast(0)
  val landingUnitPrice = landingPriceText.toIntOrNull() ?: 0
  val selectedBuildItems = remember(selectedBuildProducts.toMap()) {
    buildProductCatalog.filter { selectedBuildProducts.containsKey(it.id) }.map { item ->
      BuildProductSelection(
        item = item,
        quantity = selectedBuildProducts[item.id] ?: 0,
      )
    }
  }
  val buildProductText = selectedBuildCategory?.label.orEmpty()
  val linkedBuildProductValue = selectedBuildItems.sumOf { it.totalPrice }
  val valueAdditionText = if (linkedBuildProductValue == 0) "" else linkedBuildProductValue.toString()
  val valueAddition = linkedBuildProductValue
  val othersValue = othersText.toIntOrNull() ?: 0
  val markup = markupText.toIntOrNull() ?: 0
  val beforeMarkup = landingUnitPrice + valueAddition + othersValue
  val unitPrice = beforeMarkup + markup
  val quantityValue = checkoutQuantityText.toIntOrNull() ?: 1
  val discountPerUnit = checkoutDiscountText.toIntOrNull() ?: 0
  val discountPercent = checkoutDiscountPercentText.toIntOrNull() ?: 0
  val discountedUnitPrice = (unitPrice - discountPerUnit).coerceAtLeast(0)
  val priceTotal = discountedUnitPrice * quantityValue
  val mrpTotal = unitPrice * quantityValue
  val discountTotal = (mrpTotal - priceTotal).coerceAtLeast(0)
  val checkout = priceTotal
  val checkoutDisplay = if (checkoutText.isNotBlank()) checkoutText else if (checkout > 0) formatAmount(checkout) else ""
  val headline = "${availableStock}/${formatAmount(unitPrice)}"
  val confirmDetails = {
    detailsText = selectedDetailsDate
    showDetailsCalendar = false
    showDetailsPopup = false
  }
  val saveItem = {
    onConfirmBack(
      buildInventoryItem(
        initialItem = initialItem,
        title = title,
        productName = productName,
        seed = seed,
        batchText = batchText,
        saleText = saleText,
        rentText = rentText,
        detailsText = detailsText,
        selectedDetailsDate = selectedDetailsDate,
        unitPrice = unitPrice,
        checkoutDisplay = checkoutDisplay,
        selectedMediaUris = mediaUris,
        templatePrimaryTitle = mediaPrimaryTitleText,
        templatePrimaryDescription = mediaPrimaryDescriptionText,
        templateSecondaryTitle = mediaSecondaryTitleText,
        templateSecondaryDescription = mediaSecondaryDescriptionText,
        templateContact = mediaContactText,
        status = "complete",
      ),
    )
  }
  val saveIncompleteItem = {
    onIncompleteBack(
      buildInventoryItem(
        initialItem = initialItem,
        title = title,
        productName = productName,
        seed = seed,
        batchText = batchText,
        saleText = saleText,
        rentText = rentText,
        detailsText = detailsText,
        selectedDetailsDate = selectedDetailsDate,
        unitPrice = unitPrice,
        checkoutDisplay = checkoutDisplay,
        selectedMediaUris = mediaUris,
        templatePrimaryTitle = mediaPrimaryTitleText,
        templatePrimaryDescription = mediaPrimaryDescriptionText,
        templateSecondaryTitle = mediaSecondaryTitleText,
        templateSecondaryDescription = mediaSecondaryDescriptionText,
        templateContact = mediaContactText,
        status = "incomplete",
      ),
    )
  }

  BackHandler { showBackConfirm = true }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Bg)
      .navigationBarsPadding(),
  ) {
    val configuration = LocalConfiguration.current
    val physicalTablet = configuration.smallestScreenWidthDp >= 600
    val phoneScreen = !physicalTablet && isPhoneScreen(maxWidth)
    val compactScreen = isCompactPhoneScreen(maxWidth, maxHeight)
    val statusBarTopPaddingPx = ViewCompat.getRootWindowInsets(LocalView.current)
      ?.getInsets(WindowInsetsCompat.Type.statusBars())
      ?.top ?: 0
    val statusBarTopPadding = with(LocalDensity.current) { statusBarTopPaddingPx.toDp() }
    val contentWidth = if (phoneScreen) maxWidth else minOf(maxWidth - if (compactScreen) 20.dp else 32.dp, 784.dp)
    val horizontalPadding = if (phoneScreen) OtcPhoneHeaderHorizontalPadding else if (compactScreen) 10.dp else 16.dp
    val headerTopPadding = if (phoneScreen) statusBarTopPadding + OtcPhoneHeaderTopPadding else OtcHeaderTopOffset
    val topContentPadding = if (phoneScreen) maxHeight * 0.15f else 8.dp
    val headerActionSize = if (phoneScreen) OtcPhoneHeaderActionSize else LockedHeaderActionSize
    val headerIconSize = if (phoneScreen) OtcPhoneHeaderIconSize else LockedHeaderIconSize
    val headerSpacing = if (phoneScreen) OtcPhoneHeaderSpacing else LockedHeaderActionSpacing
    val circleSize = when {
      maxWidth > 1000.dp -> minOf(672.dp, maxHeight * 0.54f)
      maxWidth > 760.dp -> minOf(594.dp, maxHeight * 0.52f)
      compactScreen -> minOf(contentWidth * 0.82f, maxHeight * 0.33f, 320.dp)
      else -> minOf(contentWidth * 0.96f, maxHeight * 0.48f, 456.dp)
    }
    val statCardHeight = when {
      compactScreen -> 84.dp
      maxHeight > 760.dp -> 124.dp
      maxHeight > 680.dp -> 112.dp
      else -> 100.dp
    }
    val statValueSize = when {
      compactScreen -> 33.sp
      maxHeight > 760.dp -> 51.sp
      maxHeight > 680.dp -> 46.5.sp
      else -> 42.sp
    }
    val statValueLineHeight = when {
      compactScreen -> 36.sp
      maxHeight > 760.dp -> 57.sp
      maxHeight > 680.dp -> 52.5.sp
      else -> 48.sp
    }
    val statGap = if (compactScreen) 6.dp else 8.dp
    val topSpacing = when {
      compactScreen -> 4.dp
      maxHeight > 780.dp -> 12.dp
      else -> 8.dp
    }
    val topBarTextSize = when {
      maxWidth > 700.dp -> 34.sp
      compactScreen -> 22.sp
      phoneScreen -> OtcPhoneHeaderLogoTextSize
      else -> 28.sp
    }
    val topBarMarkSize = when {
      maxWidth > 700.dp -> 76.dp
      compactScreen -> 46.dp
      phoneScreen -> OtcPhoneHeaderLogoMarkSize
      else -> 60.dp
    }
    val editorTitleSize = when {
      compactScreen -> 24.sp
      phoneScreen -> 28.sp
      else -> 36.sp
    }
    val stockLabelSize = if (phoneScreen) 12.sp else 14.sp
    val headlineSize = when {
      compactScreen -> 42.sp
      phoneScreen -> 45.sp
      else -> 54.sp
    }

    Column(
      modifier = Modifier
        .width(contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = horizontalPadding, top = topContentPadding, end = horizontalPadding, bottom = 8.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Spacer(modifier = Modifier.height(topSpacing))

      Box(
        modifier = Modifier
          .size(circleSize)
          .shadow(
            elevation = 18.dp,
            shape = CircleShape,
            ambientColor = SoftGreyShadow,
            spotColor = SoftGreyShadow,
          )
          .background(Accent, CircleShape)
          .clip(CircleShape)
          .clickable {
            if (selectedMediaUri != null) {
              cameraCapturePendingConfirmation = false
              showUploadOptions = !showUploadOptions
            }
          },
        contentAlignment = Alignment.Center,
      ) {
        val productImage = selectedImageBitmap
        if (productImage != null) {
          Image(
            bitmap = productImage,
            contentDescription = "Selected product image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .fillMaxSize()
              .graphicsLayer {
                scaleX = 1.04f
                scaleY = 1.04f
              }
              .background(Color.White.copy(alpha = 0.14f), CircleShape),
          )
        } else if (selectedMediaUri != null && isVideoUri(context, selectedMediaUri)) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = "Video media",
                tint = Color.White,
                modifier = Modifier.size(42.dp),
              )
              Text(
                text = "Video cover",
                color = Color.White,
                style = TextStyle(
                  fontFamily = dmSansThin,
                  fontWeight = FontWeight.Normal,
                  fontSize = 16.sp,
                ),
              )
            }
          }
        }

        if (showUploadOptions || selectedMediaUri == null) {
          val mobileUploadIconSize = if (compactScreen) 24.dp else 26.dp
          val mobileUploadOptionSize = if (compactScreen) 52.dp else 56.dp
          val uploadRowWidthFraction = if (phoneScreen) 0.74f else 0.82f
          val uploadRowHorizontalPadding = if (phoneScreen) 10.dp else if (compactScreen) 16.dp else 24.dp
          val uploadRowVerticalPadding = if (phoneScreen) 10.dp else if (compactScreen) 16.dp else 24.dp
          val uploadRowSpacing = if (phoneScreen) 10.dp else if (compactScreen) 12.dp else 18.dp
          Row(
            modifier = Modifier
              .fillMaxWidth(uploadRowWidthFraction)
              .padding(
                horizontal = uploadRowHorizontalPadding,
                vertical = uploadRowVerticalPadding,
              ),
            horizontalArrangement = Arrangement.spacedBy(uploadRowSpacing, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            UploadIconOption(
              icon = {
                Icon(
                  imageVector = if (cameraCapturePendingConfirmation) Icons.Filled.Check else Icons.Filled.PhotoCamera,
                  contentDescription = if (cameraCapturePendingConfirmation) "Confirm image" else "Camera",
                  tint = Color.White,
                  modifier = Modifier.size(mobileUploadIconSize),
                )
              },
              size = mobileUploadOptionSize,
              onClick = {
                if (cameraCapturePendingConfirmation) {
                  cameraCapturePendingConfirmation = false
                  showUploadOptions = false
                } else {
                  val outputUri = createCameraImageUri(context)
                  pendingCameraUri = outputUri
                  cameraLauncher.launch(outputUri)
                }
              },
            )
            UploadIconOption(
              icon = {
                Icon(
                  imageVector = Icons.Filled.GridView,
                  contentDescription = "Gallery",
                  tint = Color.White,
                  modifier = Modifier.size(mobileUploadIconSize),
                )
              },
              size = mobileUploadOptionSize,
              onClick = { galleryLauncher.launch("*/*") },
            )
            UploadIconOption(
              icon = {
                Icon(
                  imageVector = Icons.Filled.AddToDrive,
                  contentDescription = "Google Drive",
                  tint = Color.White,
                  modifier = Modifier.size(mobileUploadIconSize),
                )
              },
              size = mobileUploadOptionSize,
              onClick = { driveLauncher.launch(arrayOf("image/*", "video/*")) },
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      InlineEditor(
        value = productName,
        onValueChange = { productName = it },
        fontFamily = dmSansThin,
        fontSize = editorTitleSize,
        textAlign = TextAlign.Center,
        widthFraction = 0.82f,
        clearOnFirstTap = productName == "Product Name",
      )

      Spacer(modifier = Modifier.height(2.dp))

      Text(
        text = "in stock",
        color = Ink.copy(alpha = 0.42f),
        style = TextStyle(
          fontFamily = dmSansThin,
          fontWeight = FontWeight.Normal,
          fontSize = stockLabelSize,
        ),
      )

      Spacer(modifier = Modifier.height(2.dp))

      Text(
        text = headline,
        color = Ink,
        style = TextStyle(
          fontFamily = dmSansThin,
          fontWeight = FontWeight.Normal,
          fontSize = headlineSize,
          lineHeight = if (compactScreen) 31.sp else 40.sp,
        ),
      )

      Spacer(modifier = Modifier.height(if (compactScreen) 4.dp else 6.dp))

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(statGap),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(statGap),
        ) {
          EditableStatCard(
            label = "batch",
            value = batchText,
            onValueChange = { batchText = it.filter(Char::isDigit) },
            modifier = Modifier.weight(1f),
            fontFamily = dmSansThin,
            cardHeight = statCardHeight,
            valueFontSize = statValueSize,
            valueLineHeight = statValueLineHeight,
            numeric = true,
          )
          EditableStatCard(
            label = "sale",
            value = saleText,
            onValueChange = { saleText = it.filter(Char::isDigit) },
            modifier = Modifier.weight(1f),
            fontFamily = dmSansThin,
            cardHeight = statCardHeight,
            valueFontSize = statValueSize,
            valueLineHeight = statValueLineHeight,
            numeric = true,
          )
          EditableStatCard(
            label = "rent",
            value = rentText,
            onValueChange = { rentText = it.filter(Char::isDigit) },
            modifier = Modifier.weight(1f),
            fontFamily = dmSansThin,
            cardHeight = statCardHeight,
            valueFontSize = statValueSize,
            valueLineHeight = statValueLineHeight,
            numeric = true,
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(statGap),
        ) {
          ActionStatCard(
            label = "details",
            value = detailsText,
            onClick = { showDetailsPopup = true },
            modifier = Modifier.weight(1f),
            fontFamily = dmSansThin,
            cardHeight = statCardHeight,
            valueFontSize = statValueSize,
            valueLineHeight = statValueLineHeight,
          )
          ActionStatCard(
            label = "price",
            value = if (unitPrice > 0) "\u20B9 ${formatAmount(unitPrice)}" else "",
            onClick = { showPricePopup = true },
            modifier = Modifier.weight(1f),
            fontFamily = dmSansThin,
            cardHeight = statCardHeight,
            valueFontSize = statValueSize,
            valueLineHeight = statValueLineHeight,
          )
          ActionStatCard(
            label = if (supportsMediaGallery) "media" else "checkout",
            value = if (supportsMediaGallery) {
              if (mediaUris.isEmpty()) "Open" else "${mediaUris.size}"
            } else {
              if (checkoutDisplay.isBlank()) "" else "\u20B9 $checkoutDisplay"
            },
            onClick = {
              if (supportsMediaGallery) {
                showMediaPopup = true
              } else {
                showCheckoutPopup = true
              }
            },
            modifier = Modifier.weight(1f),
            fontFamily = dmSansThin,
            cardHeight = statCardHeight,
            valueFontSize = statValueSize,
            valueLineHeight = statValueLineHeight,
          )
        }
      }

      Spacer(modifier = Modifier.height(if (compactScreen) 18.dp else 24.dp))
    }

    if (showDetailsPopup) {
      DetailsEntryPopup(
        vendorName = vendorNameText,
        onVendorNameChange = { vendorNameText = it },
        phone = vendorPhoneText,
        onPhoneChange = { vendorPhoneText = it.filter(Char::isDigit) },
        email = vendorEmailText,
        onEmailChange = { vendorEmailText = it },
        landingPrice = landingPriceText,
        onLandingPriceChange = { landingPriceText = it.filter(Char::isDigit) },
        selectedDate = selectedDetailsDate,
        onDateSelected = { selectedDetailsDate = it },
        showCalendar = showDetailsCalendar,
        onCalendarClick = { showDetailsCalendar = !showDetailsCalendar },
        onClose = {
          showDetailsCalendar = false
          showDetailsPopup = false
        },
        onConfirm = confirmDetails,
        fontFamily = dmSansThin,
        modifier = Modifier
          .width(contentWidth)
          .align(Alignment.Center)
          .padding(horizontal = 24.dp),
      )
    }

    if (showPricePopup) {
      PriceBreakdownPopup(
        landingPrice = landingPriceText,
        onLandingPriceChange = { landingPriceText = it.filter(Char::isDigit) },
        buildProduct = buildProductText,
        onBuildProductClick = {
          showPricePopup = false
          showBuildProductPicker = true
        },
        valueAddition = valueAdditionText,
        others = othersText,
        onOthersChange = { othersText = it.filter(Char::isDigit) },
        markup = markupText,
        onMarkupChange = {
          markupText = it.filter(Char::isDigit)
          showPriceValidation = false
        },
        selectedBuildItems = selectedBuildItems,
        onEditSelections = {
          showPricePopup = false
          showBuildSelectionsPopup = true
        },
        beforeMarkup = beforeMarkup,
        finalPrice = unitPrice,
        showMarkupValidation = showPriceValidation,
        onClose = { showPricePopup = false },
        onConfirm = {
          if (markupText.isBlank()) {
            showPriceValidation = true
          } else {
            priceText = unitPrice.toString()
            showPriceValidation = false
            showPricePopup = false
          }
        },
        fontFamily = dmSansThin,
        modifier = Modifier
          .width(contentWidth)
          .align(Alignment.Center)
          .padding(horizontal = 24.dp),
      )
    }

    if (showBuildProductPicker) {
      BuildProductPickerPopup(
        category = selectedBuildCategory,
        items = buildProductCatalog,
        onCategorySelected = { selectedBuildCategory = it },
        onItemSelected = { item ->
          activeBuildProductItem = item
          buildProductQuantityDraft = (selectedBuildProducts[item.id] ?: 1).toString()
          showBuildProductPicker = false
        },
        onClose = {
          showBuildProductPicker = false
          showPricePopup = true
        },
        fontFamily = dmSansThin,
        modifier = Modifier
          .width(contentWidth)
          .align(Alignment.Center)
          .padding(horizontal = 24.dp),
      )
    }

    activeBuildProductItem?.let { activeItem ->
      BuildProductQuantityPopup(
        item = activeItem,
        quantity = buildProductQuantityDraft,
        onQuantityChange = { buildProductQuantityDraft = it.filter(Char::isDigit).ifBlank { "1" } },
        onDecrease = {
          val next = ((buildProductQuantityDraft.toIntOrNull() ?: 1) - 1).coerceAtLeast(1)
          buildProductQuantityDraft = next.toString()
        },
        onIncrease = {
          val next = ((buildProductQuantityDraft.toIntOrNull() ?: 1) + 1).coerceAtMost(99)
          buildProductQuantityDraft = next.toString()
        },
        onConfirm = {
          selectedBuildProducts[activeItem.id] = (buildProductQuantityDraft.toIntOrNull() ?: 1).coerceAtLeast(1)
          selectedBuildCategory = activeItem.category
          activeBuildProductItem = null
          showPricePopup = true
        },
        onClose = {
          activeBuildProductItem = null
          showPricePopup = true
        },
        fontFamily = dmSansThin,
        modifier = Modifier
          .width(contentWidth)
          .align(Alignment.Center)
          .padding(horizontal = 24.dp),
      )
    }

    if (showBuildSelectionsPopup) {
      BuildProductSelectionsPopup(
        selections = selectedBuildItems,
        onDeleteRequest = { pendingDeleteSelectionId = it.item.id },
        onClose = {
          showBuildSelectionsPopup = false
          showPricePopup = true
        },
        fontFamily = dmSansThin,
        modifier = Modifier
          .width(contentWidth)
          .align(Alignment.Center)
          .padding(horizontal = 24.dp),
      )
    }

    pendingDeleteSelectionId?.let { selectionId ->
      val pendingSelection = selectedBuildItems.firstOrNull { it.item.id == selectionId }
      if (pendingSelection != null) {
        DeleteBuildSelectionDialog(
          itemName = pendingSelection.item.name,
          onConfirm = {
            selectedBuildProducts.remove(selectionId)
            pendingDeleteSelectionId = null
            if (selectedBuildProducts.isEmpty()) {
              selectedBuildCategory = null
            }
          },
          onDismiss = { pendingDeleteSelectionId = null },
          fontFamily = dmSansThin,
          modifier = Modifier
            .width(contentWidth)
            .align(Alignment.Center)
            .padding(horizontal = 24.dp),
        )
      } else {
        pendingDeleteSelectionId = null
      }
    }

    if (showCheckoutPopup) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.34f)),
      )

      CheckoutSummaryPopup(
        quantity = checkoutQuantityText,
        onQuantityChange = { checkoutQuantityText = it.filter(Char::isDigit) },
        discount = checkoutDiscountText,
        onDiscountChange = {
          val numeric = it.filter(Char::isDigit)
          checkoutDiscountText = numeric
          val nextDiscount = numeric.toIntOrNull() ?: 0
          checkoutDiscountPercentText = if (unitPrice > 0) ((nextDiscount * 100f) / unitPrice).toInt().toString() else "0"
        },
        discountPercent = checkoutDiscountPercentText,
        onDiscountPercentChange = {
          val numeric = it.filter(Char::isDigit)
          checkoutDiscountPercentText = numeric
          val nextPercent = numeric.toIntOrNull() ?: 0
          checkoutDiscountText = ((unitPrice * nextPercent) / 100f).toInt().toString()
        },
        finalPrice = unitPrice,
        beforeMarkup = beforeMarkup,
        landingPrice = landingUnitPrice,
        quantityValue = quantityValue,
        discountedUnitPrice = discountedUnitPrice,
        discountTotal = discountTotal,
        priceTotal = priceTotal,
        profitLoss = priceTotal - (beforeMarkup * quantityValue),
        onClose = {
          checkoutText = formatAmount(priceTotal)
          showCheckoutPopup = false
        },
        fontFamily = dmSansThin,
        modifier = Modifier
          .width(contentWidth)
          .align(Alignment.Center)
          .padding(horizontal = 24.dp),
      )
    }

    if (showMediaPopup) {
      MediaGalleryPopup(
        mediaUris = mediaUris,
        productName = productName,
        primaryTitle = mediaPrimaryTitleText,
        onPrimaryTitleChange = { mediaPrimaryTitleText = it },
        primaryDescription = mediaPrimaryDescriptionText,
        onPrimaryDescriptionChange = { mediaPrimaryDescriptionText = it },
        secondaryTitle = mediaSecondaryTitleText,
        onSecondaryTitleChange = { mediaSecondaryTitleText = it },
        secondaryDescription = mediaSecondaryDescriptionText,
        onSecondaryDescriptionChange = { mediaSecondaryDescriptionText = it },
        contactText = mediaContactText,
        onContactTextChange = { mediaContactText = it },
        fontFamily = dmSansThin,
        onAddFromDrive = { mediaPopupDriveLauncher.launch(arrayOf("image/*")) },
        onAddBrochureImage = { slotIndex ->
          pendingBrochureImageSlot = slotIndex
          brochureImageLauncher.launch(arrayOf("image/*"))
        },
        onClose = { showMediaPopup = false },
      )
    }

    if (showBackConfirm) {
      ConfirmChangesDialog(
        onDismiss = {
          showBackConfirm = false
          saveIncompleteItem()
        },
        onConfirm = {
          showBackConfirm = false
          saveItem()
        },
        fontFamily = dmSansThin,
        modifier = Modifier
          .width(contentWidth)
          .align(Alignment.Center)
          .padding(horizontal = 24.dp),
      )
    }

    OtcActionOnlyHeader(
      modifier = Modifier
        .width(contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = horizontalPadding, top = headerTopPadding, end = horizontalPadding),
      onBack = { showBackConfirm = true },
      actionButtonSize = headerActionSize,
      actionIconSize = headerIconSize,
      actionSpacing = headerSpacing,
      containerHeight = if (phoneScreen) OtcHeaderFrameHeight else Dp.Unspecified,
      logoWidth = if (phoneScreen) OtcPhoneHeaderLogoWidth else 260.dp,
      logoHeight = if (phoneScreen) OtcPhoneHeaderLogoHeight else OtcHeaderLogoHeight,
      logoTextSize = topBarTextSize,
      logoMarkSize = topBarMarkSize,
      actionTopPadding = if (phoneScreen) OtcPhoneHeaderActionTopPadding else 0.dp,
    )

  }
}

@Composable
fun InventoryListScreen(
  title: DashboardDestination,
  items: List<InventoryItem>,
  onBack: () -> Unit,
  onAddItem: () -> Unit,
  onItemTap: (InventoryItem) -> Unit,
  onDeleteItem: (InventoryItem) -> Unit,
) {
  val dmSansThin = rememberDashboardFont()
  var searchQuery by rememberSaveable(title.name) { mutableStateOf("") }
  var pendingDeleteItem by remember { mutableStateOf<InventoryItem?>(null) }
  val filteredItems = remember(items, searchQuery) {
    if (searchQuery.isBlank()) {
      items
    } else {
      items.filter { item -> item.name.contains(searchQuery.trim(), ignoreCase = true) }
    }
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Bg)
      .navigationBarsPadding(),
  ) {
    val phoneScreen = isPhoneScreen(maxWidth)
    val compactScreen = isCompactPhoneScreen(maxWidth, maxHeight)
    val statusBarTopPaddingPx = ViewCompat.getRootWindowInsets(LocalView.current)
      ?.getInsets(WindowInsetsCompat.Type.statusBars())
      ?.top ?: 0
    val statusBarTopPadding = with(LocalDensity.current) { statusBarTopPaddingPx.toDp() }
    val contentWidth = if (phoneScreen) maxWidth else minOf(maxWidth - if (compactScreen) 20.dp else 32.dp, 920.dp)
    val horizontalPadding = if (phoneScreen) OtcPhoneHeaderHorizontalPadding else if (compactScreen) 10.dp else 16.dp
    val headerTopPadding = if (phoneScreen) statusBarTopPadding + OtcPhoneHeaderTopPadding else OtcHeaderTopOffset
    val topContentPadding = if (phoneScreen) maxHeight * 0.20f else 12.dp
    val headerActionSize = if (phoneScreen) OtcPhoneHeaderActionSize else LockedHeaderActionSize
    val headerIconSize = if (phoneScreen) OtcPhoneHeaderIconSize else LockedHeaderIconSize
    val headerSpacing = if (phoneScreen) OtcPhoneHeaderSpacing else LockedHeaderActionSpacing
    val brandTextSize = when {
      maxWidth > 700.dp -> 46.sp
      compactScreen -> 28.sp
      phoneScreen -> OtcPhoneHeaderLogoTextSize
      else -> 36.sp
    }
    val brandMarkSize = when {
      maxWidth > 700.dp -> 102.dp
      compactScreen -> 58.dp
      phoneScreen -> OtcPhoneHeaderLogoMarkSize
      else -> 78.dp
    }
    LazyColumn(
      modifier = Modifier
        .width(contentWidth)
        .fillMaxSize()
        .align(Alignment.TopCenter)
        .padding(start = horizontalPadding, top = topContentPadding, end = horizontalPadding, bottom = 12.dp),
      verticalArrangement = Arrangement.spacedBy(if (compactScreen) 10.dp else 14.dp),
      horizontalAlignment = Alignment.Start,
    ) {
      item {
        if (compactScreen) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            SearchInventoryField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              fontFamily = dmSansThin,
              modifier = Modifier.fillMaxWidth(),
              placeholder = "Search inventory",
            )

            AddInventoryButton(
              fontFamily = dmSansThin,
              onClick = onAddItem,
              modifier = Modifier.fillMaxWidth(),
              label = "Add item",
            )
          }
        } else {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            SearchInventoryField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              fontFamily = dmSansThin,
              modifier = Modifier.weight(1f),
              placeholder = "Search inventory",
            )

            AddInventoryButton(
              fontFamily = dmSansThin,
              onClick = onAddItem,
              modifier = Modifier.weight(1f),
              label = "Add item",
            )
          }
        }
      }

      if (filteredItems.isEmpty()) {
        item {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            color = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
          ) {
            Text(
              text = "No items added yet. Start by adding a product to build this inventory list.",
              modifier = Modifier.padding(horizontal = 20.dp, vertical = 32.dp),
              textAlign = TextAlign.Center,
              color = Ink.copy(alpha = 0.62f),
              style = TextStyle(
                fontFamily = dmSansThin,
                fontWeight = FontWeight.Normal,
                fontSize = if (compactScreen) 16.sp else 20.sp,
                lineHeight = if (compactScreen) 20.sp else 24.sp,
              ),
            )
          }
        }
      } else {
        items(filteredItems, key = { it.id }) { item ->
          InventoryItemCard(
            item = item,
            fontFamily = dmSansThin,
            onClick = { onItemTap(item) },
            onRequestDelete = { pendingDeleteItem = it },
          )
        }
      }
    }

    val deleteItem = pendingDeleteItem
    if (deleteItem != null) {
      ConfirmDeleteItemDialog(
        itemName = deleteItem.name,
        onConfirm = {
          onDeleteItem(deleteItem)
          pendingDeleteItem = null
        },
        onDismiss = { pendingDeleteItem = null },
      )
    }

    OtcActionOnlyHeader(
      modifier = Modifier
        .width(contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = horizontalPadding, top = headerTopPadding, end = horizontalPadding),
      onBack = onBack,
      actionButtonSize = headerActionSize,
      actionIconSize = headerIconSize,
      actionSpacing = headerSpacing,
      containerHeight = if (phoneScreen) OtcHeaderFrameHeight else Dp.Unspecified,
      logoWidth = if (phoneScreen) OtcPhoneHeaderLogoWidth else 260.dp,
      logoHeight = if (phoneScreen) OtcPhoneHeaderLogoHeight else OtcHeaderLogoHeight,
      logoTextSize = brandTextSize,
      logoMarkSize = brandMarkSize,
      actionTopPadding = if (phoneScreen) OtcPhoneHeaderActionTopPadding else 0.dp,
    )
  }
}

@Composable
private fun InventoryListHeader(
  textSize: androidx.compose.ui.unit.TextUnit,
  markSize: Dp,
  title: DashboardDestination,
  itemCount: Int,
  onBack: () -> Unit,
  phoneScreen: Boolean = false,
) {
  val fontFamily = rememberDashboardFont()

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.Start,
  ) {
    TopBrandBar(
      textSize = textSize,
      markSize = markSize,
      onBack = onBack,
      alignStart = true,
      logoMaxWidth = if (phoneScreen) OtcPhoneHeaderLogoWidth else 260.dp,
      actionButtonSize = if (phoneScreen) OtcPhoneHeaderActionSize else LockedHeaderActionSize,
      actionIconSize = if (phoneScreen) OtcPhoneHeaderIconSize else LockedHeaderIconSize,
      actionSpacing = if (phoneScreen) OtcPhoneHeaderSpacing else LockedHeaderActionSpacing,
      logoTopOffset = LockedHeaderLogoTopOffset,
      actionTopPadding = if (phoneScreen) OtcPhoneHeaderActionTopPadding else 0.dp,
      containerHeight = if (phoneScreen) OtcHeaderFrameHeight else Dp.Unspecified,
    )

  }
}

@Composable
private fun TopBrandBar(
  textSize: androidx.compose.ui.unit.TextUnit,
  markSize: Dp,
  onBack: () -> Unit,
  alignStart: Boolean = false,
  logoMaxWidth: Dp = OtcHeaderLogoMaxWidth,
  actionButtonSize: Dp = 42.dp,
  actionIconSize: Dp = 24.dp,
  actionSpacing: Dp = 10.dp,
  logoTopOffset: Dp = 0.dp,
  actionTopPadding: Dp = 0.dp,
  actionBorderColor: Color = Color.Transparent,
  actionBorderWidth: Dp = 0.dp,
  middleAction: (@Composable () -> Unit)? = null,
  bottomAction: (@Composable () -> Unit)? = null,
  containerHeight: Dp = Dp.Unspecified,
) {
  val actionColumnHeight = (actionButtonSize * 3f) + (actionSpacing * 2f)
  val containerModifier = Modifier
    .fillMaxWidth()
    .then(
      if (containerHeight != Dp.Unspecified) {
        Modifier.height(containerHeight)
      } else {
        Modifier.heightIn(min = actionColumnHeight)
      },
    )
  if (alignStart) {
    Box(
      modifier = containerModifier,
    ) {
      AshokvatikaLogo(
        modifier = Modifier
          .align(Alignment.TopStart)
          .widthIn(max = logoMaxWidth)
          .offset(y = logoTopOffset),
        textSize = textSize,
        markSize = markSize,
      )

      Column(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(top = actionTopPadding),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(actionSpacing),
      ) {
        OtcHeaderActionSlot(actionButtonSize) {
          CircleIconAction(
            size = actionButtonSize,
            borderColor = actionBorderColor,
            borderWidth = actionBorderWidth,
            icon = {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Accent,
                modifier = Modifier.size(actionIconSize),
              )
            },
            onClick = onBack,
          )
        }
        OtcHeaderActionSlot(actionButtonSize, middleAction)
        OtcHeaderActionSlot(actionButtonSize, bottomAction)
      }
    }
  } else {
    Box(
      modifier = containerModifier,
      contentAlignment = Alignment.Center,
    ) {
      AshokvatikaLogo(
        modifier = Modifier.fillMaxWidth(0.7f),
        textSize = textSize,
        markSize = markSize,
      )

      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(top = 8.dp),
      ) {
        Column(
          horizontalAlignment = Alignment.End,
          verticalArrangement = Arrangement.spacedBy(actionSpacing),
        ) {
          OtcHeaderActionSlot(actionButtonSize) {
            CircleIconAction(
              size = actionButtonSize,
              borderColor = actionBorderColor,
              borderWidth = actionBorderWidth,
              icon = {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Back",
                  tint = Accent,
                  modifier = Modifier.size(actionIconSize),
                )
              },
              onClick = onBack,
            )
          }
          OtcHeaderActionSlot(actionButtonSize, middleAction)
          OtcHeaderActionSlot(actionButtonSize, bottomAction)
        }
      }
    }
  }
}

@Composable
private fun OtcHeaderActionSlot(
  size: Dp,
  content: (@Composable () -> Unit)? = null,
) {
  Box(
    modifier = Modifier.size(size),
    contentAlignment = Alignment.Center,
  ) {
    content?.invoke()
  }
}

@Composable
private fun OtcActionOnlyHeader(
  modifier: Modifier = Modifier,
  onBack: () -> Unit,
  middleAction: (@Composable () -> Unit)? = null,
  bottomAction: (@Composable () -> Unit)? = null,
  actionButtonSize: Dp = OtcHeaderActionSize,
  actionIconSize: Dp = OtcHeaderIconSize,
  actionSpacing: Dp = OtcHeaderActionSpacing,
  containerHeight: Dp = OtcHeaderFrameHeight,
  logoWidth: Dp = OtcHeaderLogoWidth,
  logoHeight: Dp = OtcHeaderLogoHeight,
  logoTextSize: androidx.compose.ui.unit.TextUnit = 30.sp,
  logoMarkSize: Dp = 60.dp,
  logoTopOffset: Dp = LockedHeaderLogoTopOffset,
  actionTopPadding: Dp = 0.dp,
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(containerHeight),
  ) {
    AshokvatikaLogo(
      modifier = Modifier
        .align(Alignment.TopStart)
        .widthIn(max = logoWidth)
        .offset(y = logoTopOffset),
      textSize = logoTextSize,
      markSize = logoMarkSize,
      fixedHeight = logoHeight,
    )

    Column(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(top = actionTopPadding),
      horizontalAlignment = Alignment.End,
      verticalArrangement = Arrangement.spacedBy(actionSpacing),
    ) {
      OtcHeaderActionSlot(actionButtonSize) {
        CircleIconAction(
          size = actionButtonSize,
          borderColor = Accent,
          borderWidth = 2.dp,
          icon = {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Accent,
              modifier = Modifier.size(actionIconSize),
            )
          },
          onClick = onBack,
        )
      }
      OtcHeaderActionSlot(actionButtonSize, middleAction)
      OtcHeaderActionSlot(actionButtonSize, bottomAction)
    }
  }
}

@Composable
private fun ClosedDealsShortcutIcon(size: Dp = OtcHeaderIconSize) {
  Image(
    painter = painterResource(R.drawable.icon_closed_list),
    contentDescription = null,
    modifier = Modifier.size(size),
  )
}

@Composable
private fun ClosedPageOtcShortcutIcon() {
  Image(
    painter = painterResource(R.drawable.icon_closed_header_list),
    contentDescription = null,
    modifier = Modifier.size(OtcHeaderIconSize),
  )
}

@Composable
private fun AddInventoryButton(
  fontFamily: FontFamily,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  label: String = "Add items",
) {
  BoxWithConstraints(modifier = modifier) {
    val compactButton = maxWidth < 220.dp
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(
          elevation = 8.dp,
          shape = CardShape,
          ambientColor = SoftGreyShadow,
          spotColor = SoftGreyShadow,
        )
        .clickable(onClick = onClick),
      shape = CardShape,
      color = AccentGreen,
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = if (compactButton) 16.dp else 22.dp, vertical = if (compactButton) 14.dp else 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        if (label.isNotBlank()) {
          Text(
            text = label,
            color = Color.White,
            style = TextStyle(
              fontFamily = fontFamily,
              fontWeight = FontWeight.SemiBold,
              fontSize = if (compactButton) 18.sp else 24.sp,
            ),
          )
        } else {
          Spacer(modifier = Modifier.width(1.dp))
        }
        Icon(
          imageVector = Icons.Filled.Add,
          contentDescription = "Add item",
          tint = Color.White,
          modifier = Modifier.size(if (compactButton) 24.dp else 28.dp),
        )
      }
    }
  }
}

@Composable
private fun SearchInventoryField(
  value: String,
  onValueChange: (String) -> Unit,
  fontFamily: FontFamily,
  modifier: Modifier = Modifier,
  placeholder: String = "Search items",
) {
  BoxWithConstraints(modifier = modifier) {
    val compactField = maxWidth < 240.dp
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(
          elevation = 8.dp,
          shape = CardShape,
          ambientColor = SoftGreyShadow,
          spotColor = SoftGreyShadow,
        ),
      shape = CardShape,
      color = Panel,
      border = androidx.compose.foundation.BorderStroke(1.dp, PanelBorder),
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = if (compactField) 16.dp else 22.dp, vertical = if (compactField) 12.dp else 15.dp),
        horizontalArrangement = Arrangement.spacedBy(if (compactField) 10.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.Filled.Search,
          contentDescription = "Search items",
          tint = Accent,
          modifier = Modifier.size(if (compactField) 22.dp else 26.dp),
        )

        BasicTextField(
          value = value,
          onValueChange = onValueChange,
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          cursorBrush = SolidColor(Accent),
          textStyle = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = if (compactField) 18.sp else 24.sp,
            color = Ink,
          ),
          decorationBox = { innerTextField ->
            if (value.isBlank() && placeholder.isNotBlank()) {
              Text(
                text = placeholder,
                color = Ink.copy(alpha = 0.42f),
                style = TextStyle(
                  fontFamily = fontFamily,
                  fontWeight = FontWeight.Normal,
                  fontSize = if (compactField) 18.sp else 24.sp,
                ),
              )
            }
            innerTextField()
          },
        )
      }
    }
  }
}

@Composable
private fun InventoryItemCard(
  item: InventoryItem,
  fontFamily: FontFamily,
  onClick: () -> Unit,
  onRequestDelete: (InventoryItem) -> Unit,
) {
  val imageBitmap by rememberDecodedImage(item.imageUri, maxDimensionPx = 720)
  val batchValue = item.batch.toIntOrNull() ?: 0
  val saleValue = item.sale.toIntOrNull() ?: 0
  val rentValue = item.rent.toIntOrNull() ?: 0
  val availableStock = (batchValue - saleValue - rentValue).coerceAtLeast(0)
  val inStock = availableStock > 0
  val swipeOffset = remember { Animatable(0f) }
  val scope = rememberCoroutineScope()
  var dragConsumed by remember(item.id) { mutableStateOf(false) }

  BoxWithConstraints(
    modifier = Modifier.fillMaxWidth(),
  ) {
    val compactCard = maxWidth < 380.dp
    val imageHeight = if (compactCard) 112.dp else 146.dp
    val rowPadding = if (compactCard) 12.dp else 18.dp
    val spacing = if (compactCard) 12.dp else 18.dp
    val nameSize = if (compactCard) 20.sp else 26.sp
    val statusSize = if (compactCard) 16.sp else 20.sp
    val stockSize = if (compactCard) 28.sp else 38.sp

    Surface(
      modifier = Modifier.matchParentSize(),
      shape = CardShape,
      color = Color(0xFFF1E4D6),
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
    ) {}

    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .offset { IntOffset(swipeOffset.value.toInt(), 0) }
      .shadow(
          elevation = 14.dp,
          shape = CardShape,
          ambientColor = SoftGreyShadow,
          spotColor = SoftGreyShadow,
        )
      .pointerInput(item.id) {
          detectHorizontalDragGestures(
            onDragStart = {
              dragConsumed = false
            },
            onHorizontalDrag = { _, dragAmount ->
              if (kotlin.math.abs(dragAmount) > 2f) {
                dragConsumed = true
              }
              val updated = (swipeOffset.value + dragAmount).coerceIn(0f, 260f)
              scope.launch { swipeOffset.snapTo(updated) }
            },
            onDragEnd = {
              scope.launch {
                if (swipeOffset.value > 132f) {
                  onRequestDelete(item)
                }
                swipeOffset.animateTo(0f, animationSpec = tween(durationMillis = 220))
              }
            },
            onDragCancel = {
              scope.launch {
                swipeOffset.animateTo(0f, animationSpec = tween(durationMillis = 220))
              }
              dragConsumed = false
            },
          )
        }
        .clickable {
          if (!dragConsumed) {
            onClick()
          }
          dragConsumed = false
        },
      shape = CardShape,
      color = Color.White,
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = rowPadding, vertical = if (compactCard) 12.dp else 16.dp),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.Top,
      ) {
        Box(
          modifier = Modifier
            .height(imageHeight)
            .aspectRatio(0.68f)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
            .background(Color(0xFFF3F3F3), androidx.compose.foundation.shape.RoundedCornerShape(24.dp)),
          contentAlignment = Alignment.Center,
        ) {
          val previewBitmap = imageBitmap
          if (previewBitmap != null) {
            Image(
              bitmap = previewBitmap,
              contentDescription = item.name,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize(),
            )
          } else {
            Text(
              text = item.name.take(2).uppercase(Locale.US),
              color = Accent,
              style = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
              ),
            )
          }
        }

        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = item.name,
            color = Ink,
            style = TextStyle(
              fontFamily = fontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = nameSize,
              lineHeight = if (compactCard) 22.sp else 28.sp,
            ),
          )
          Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(if (compactCard) 8.dp else 10.dp),
          ) {
            Text(
              text = if (inStock) "In-stock" else "Out of stock",
              color = if (inStock) AccentGreen else Color(0xFFD85656),
              style = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = statusSize,
              ),
            )
            Text(
              text = availableStock.toString(),
              color = Ink.copy(alpha = 0.88f),
              style = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Thin,
                fontSize = stockSize,
                lineHeight = if (compactCard) 30.sp else 38.sp,
              ),
            )
          }
          Row(
            horizontalArrangement = Arrangement.spacedBy(if (compactCard) 6.dp else 10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            InventoryMetricPill("Batch", batchValue.toString(), fontFamily, compactCard)
            InventoryMetricPill("Sale", saleValue.toString(), fontFamily, compactCard)
            InventoryMetricPill("Rent", rentValue.toString(), fontFamily, compactCard)
          }
          if (item.status == "incomplete") {
            Text(
              text = "incomplete",
              color = Color(0xFFC04A44),
              style = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
              ),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun InventoryMetricPill(
  label: String,
  value: String,
  fontFamily: FontFamily,
  compact: Boolean = false,
) {
  Surface(
    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    color = Accent,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = if (compact) 10.dp else 14.dp, vertical = if (compact) 6.dp else 8.dp),
      horizontalAlignment = Alignment.Start,
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(
        text = label,
        color = Color.White,
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Normal,
          fontSize = if (compact) 10.sp else 12.sp,
        ),
      )
      Text(
        text = value,
        color = Color.White,
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Normal,
          fontSize = if (compact) 13.sp else 16.sp,
          lineHeight = if (compact) 14.sp else 16.sp,
        ),
      )
    }
  }
}

@Composable
private fun ConfirmDeleteItemDialog(
  itemName: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  val fontFamily = rememberDashboardFont()

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier.fillMaxWidth(0.9f),
      shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
      color = Accent,
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 26.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Text(
          text = "Confirm to delete item :",
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Center,
          color = Color.White.copy(alpha = 0.98f),
          style = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Thin,
            fontSize = 24.sp,
            lineHeight = 26.sp,
          ),
        )
        Text(
          text = itemName,
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Center,
          color = Color.White,
          style = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 30.sp,
            lineHeight = 32.sp,
          ),
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          TextButton(onClick = onConfirm) {
            Text(
              text = "Yes",
              color = Color.White,
              style = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
              ),
            )
          }
          TextButton(onClick = onDismiss) {
            Text(
              text = "No",
              color = Color.White,
              style = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
              ),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ConfirmChangesDialog(
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
  fontFamily: FontFamily,
  modifier: Modifier = Modifier,
) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Surface(
      modifier = modifier,
      shape = CardShape,
      color = Accent,
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
      ) {
        Text(
          text = "Confirm to save item :",
          color = Color.White,
          textAlign = TextAlign.Center,
          style = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Thin,
            fontSize = 28.sp,
          ),
          modifier = Modifier.fillMaxWidth(),
        )
        Text(
          text = "Yes saves this product\nNo sends it back as incomplete",
          color = Color.White,
          textAlign = TextAlign.Center,
          style = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            lineHeight = 28.sp,
          ),
          modifier = Modifier.fillMaxWidth(),
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          TextButton(onClick = onConfirm) { Text("Yes", color = Color.White) }
          TextButton(onClick = onDismiss) { Text("No", color = Color.White) }
        }
      }
    }
  }
}

@Composable
fun OtcScreen(
  onBack: () -> Unit,
  cartCount: Int,
  onOpenCart: () -> Unit,
  onOpenSavedDeals: () -> Unit,
  onAddToCart: (List<CartProductItem>) -> Unit,
) {
  val dmSansThin = rememberDashboardFont()
  val catalog = otcCatalog()
  var activeItemId by rememberSaveable { mutableStateOf<String?>(null) }
  var draftQuantity by rememberSaveable { mutableStateOf("1") }
  val quantities = remember { mutableStateMapOf<String, Int>() }
  val selectedIds = quantities.keys.toSet()
  val selectedItems = catalog.filter { it.id in selectedIds }
  val selectedCartItems = selectedItems.mapNotNull { item ->
    val quantity = quantities[item.id] ?: return@mapNotNull null
    CartProductItem(
      id = item.id,
      name = item.name,
      shortName = item.shortName,
      mrp = item.price.toIntOrNull() ?: 0,
      landingPrice = item.landingPrice.toIntOrNull() ?: 0,
      quantity = quantity,
      imageUri = item.imageUri,
    )
  }
  val activeItem = catalog.firstOrNull { it.id == activeItemId }
  val totalAmount = catalog.sumOf { item ->
    val quantity = quantities[item.id] ?: 0
    val price = item.price.filter(Char::isDigit).toIntOrNull() ?: 0
    quantity * price
  }
  val landingTotal = catalog.sumOf { item ->
    val quantity = quantities[item.id] ?: 0
    val price = item.landingPrice.filter(Char::isDigit).toIntOrNull() ?: 0
    quantity * price
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Bg)
      .navigationBarsPadding(),
  ) {
    val phoneScreen = isPhoneScreen(maxWidth)
    val compactScreen = isCompactPhoneScreen(maxWidth, maxHeight)
    val statusBarTopPaddingPx = ViewCompat.getRootWindowInsets(LocalView.current)
      ?.getInsets(WindowInsetsCompat.Type.statusBars())
      ?.top ?: 0
    val statusBarTopPadding = with(LocalDensity.current) { statusBarTopPaddingPx.toDp() }
    val contentWidth = if (phoneScreen) maxWidth else minOf(maxWidth - 28.dp, 980.dp)
    val horizontalPadding = when {
      phoneScreen -> OtcPhoneHeaderHorizontalPadding
      maxWidth > 700.dp -> OtcHeaderHorizontalPadding
      compactScreen -> 8.dp
      else -> 12.dp
    }
    val headerActionSize = when {
      phoneScreen -> OtcPhoneHeaderActionSize
      compactScreen -> 42.dp
      else -> OtcHeaderActionSize
    }
    val headerIconSize = if (phoneScreen) OtcPhoneHeaderIconSize else OtcHeaderIconSize
    val headerSpacing = if (phoneScreen) OtcPhoneHeaderSpacing else if (compactScreen) 12.dp else OtcHeaderActionSpacing
    val headerContainerHeight = when {
      phoneScreen -> OtcHeaderFrameHeight
      compactScreen -> 122.dp
      else -> OtcHeaderFrameHeight
    }
    val headerLogoWidth = when {
      phoneScreen -> OtcPhoneHeaderLogoWidth
      compactScreen -> 220.dp
      else -> OtcHeaderLogoWidth
    }
    val headerLogoHeight = when {
      phoneScreen -> OtcPhoneHeaderLogoHeight
      compactScreen -> 82.dp
      else -> OtcHeaderLogoHeight
    }
    val headerLogoTextSize = when {
      phoneScreen -> OtcPhoneHeaderLogoTextSize
      compactScreen -> 22.sp
      else -> 30.sp
    }
    val headerLogoMarkSize = if (phoneScreen) OtcPhoneHeaderLogoMarkSize else 60.dp
    val headerTopPadding = if (phoneScreen) statusBarTopPadding + OtcPhoneHeaderTopPadding else OtcHeaderTopOffset
    val contentTopPadding = if (phoneScreen) {
      // Keep phone content closer to the header than the original layout, but leave
      // enough breathing room so the price block and floating add button do not crowd
      // the action rail.
      headerTopPadding + headerLogoHeight + OtcPhoneContentTopGap + OtcPhoneStickyBlockTopOffset
    } else {
      headerTopPadding + headerContainerHeight + 10.dp
    }
    val amountSize = when {
      maxWidth > 1000.dp -> 60.sp
      maxWidth > 700.dp -> 48.sp
      compactScreen -> 28.sp
      phoneScreen -> 21.33.sp
      else -> 40.sp
    }
    val mrpLabelSize = when {
      maxWidth > 1000.dp -> 13.sp
      maxWidth > 700.dp -> 11.sp
      compactScreen -> 8.sp
      phoneScreen -> 6.sp
      else -> 9.sp
    }
    val mobileRowContentWidth = contentWidth - (horizontalPadding * 2f)
    val thumbnailSize = minOf((contentWidth - 16.dp) / 5f, if (maxWidth > 700.dp) 52.dp else if (compactScreen) 34.dp else 34.dp)
    val carouselCardWidth = when {
      maxWidth > 1200.dp -> 194.dp
      maxWidth > 900.dp -> 176.dp
      maxWidth > 700.dp -> 156.dp
      phoneScreen -> ((mobileRowContentWidth - 8.dp) / 3f)
      compactScreen -> 136.dp
      else -> minOf(contentWidth * 0.39f, 140.dp)
    }
    val carouselCardHeight = when {
      maxHeight > 900.dp -> 210.dp
      maxHeight > 760.dp -> 176.dp
      phoneScreen -> (((mobileRowContentWidth - 8.dp) / 3f) * 1.36f)
      compactScreen -> 136.dp
      else -> 152.dp
    }
    val carouselPhotoSize = when {
      maxHeight > 900.dp -> 100.dp
      maxHeight > 760.dp -> 84.dp
      phoneScreen -> ((((mobileRowContentWidth - 8.dp) / 3f) * 1.2f) * 0.6f)
      compactScreen -> 50.dp
      else -> 66.dp
    }
    val sectionGap = when {
      maxHeight > 900.dp -> 8.dp
      maxHeight > 720.dp -> 6.dp
      phoneScreen -> 2.dp
      compactScreen -> 4.dp
      else -> 4.dp
    }
    val titleAssetSize = when {
      maxHeight > 760.dp -> 30.dp
      phoneScreen -> 12.dp
      compactScreen -> 16.dp
      else -> 24.dp
    }
    val columnLogoSize = when {
      maxHeight > 760.dp -> 34.dp
      phoneScreen -> 16.dp
      compactScreen -> 18.dp
      else -> 28.dp
    }
    val visibleSelectionSlots = if (maxWidth > 700.dp) 6 else 5
    val selectionRowSpacing = if (phoneScreen) 8.dp else 8.dp
    val staticPlusButtonSize = if (phoneScreen) 38.dp else headerActionSize
    val staticPlusButtonFootprint = if (phoneScreen) 80.dp else staticPlusButtonSize
    val selectionRowEndInset = if (phoneScreen) staticPlusButtonFootprint + selectionRowSpacing else 0.dp
    val sectionRowSpacing = if (phoneScreen) 6.dp else 12.dp
    val cardHorizontalPadding = if (phoneScreen) 2.dp else 8.dp
    val cardVerticalPadding = if (phoneScreen) 2.dp else 8.dp
    val cardNameSize = if (phoneScreen) 12.sp else 13.sp
    val cardNameLineHeight = if (phoneScreen) 13.sp else 15.sp
    val cardPriceSize = if (phoneScreen) 20.sp else 24.sp
    val cardPriceLineHeight = if (phoneScreen) 22.sp else 26.sp
    val columnGap = if (phoneScreen) 4.dp else 12.dp
    val availableColumnsWidth = contentWidth - (horizontalPadding * 2f)
    val productColumnWidth = (availableColumnsWidth - (columnGap * 2f)) / 3f
    val productCardWidth = productColumnWidth
    val productCardHeight = when {
      phoneScreen -> productCardWidth * 1.56f
      compactScreen -> 152.dp
      else -> maxOf(carouselCardHeight, productCardWidth * 1.42f)
    }
    val productPhotoSize = when {
      phoneScreen -> minOf(productCardWidth * 0.82f, productCardHeight * 0.5f)
      compactScreen -> minOf(productCardWidth * 0.66f, 54.dp)
      else -> minOf(productCardWidth * 0.6f, carouselPhotoSize)
    }
    val plantItems = catalog.filter { it.category == OtcCarouselCategory.Plant }
    val potItems = catalog.filter { it.category == OtcCarouselCategory.Pots }
    val supplyItems = catalog.filter { it.category == OtcCarouselCategory.Supplies }
    var stickyBlockHeightPx by remember { mutableIntStateOf(0) }
    val stickyBlockHeight = with(LocalDensity.current) { stickyBlockHeightPx.toDp() }
    val productColumnsTopPadding = if (phoneScreen) {
      (contentTopPadding + stickyBlockHeight - staticPlusButtonFootprint - 8.dp)
        .coerceAtLeast(contentTopPadding)
    } else {
      contentTopPadding + stickyBlockHeight + sectionGap
    }

    Box(
      modifier = Modifier
        .width(contentWidth)
        .fillMaxHeight()
        .align(Alignment.TopCenter),
    ) {
      if (phoneScreen) {
        Row(
          modifier = Modifier
              .fillMaxSize()
              .padding(
                start = horizontalPadding,
                top = productColumnsTopPadding,
                end = horizontalPadding,
                bottom = OtcPageVerticalPadding,
              ),
          horizontalArrangement = Arrangement.spacedBy(columnGap),
          verticalAlignment = Alignment.Top,
        ) {
          OtcProductColumn(
            modifier = Modifier.weight(1f),
            items = plantItems,
            selectedIds = selectedIds,
            onItemTap = { item ->
              activeItemId = item.id
              draftQuantity = (quantities[item.id] ?: 1).toString()
            },
            fontFamily = dmSansThin,
            cardWidth = productCardWidth,
            cardHeight = productCardHeight,
            photoSize = productPhotoSize,
            horizontalPadding = cardHorizontalPadding,
            verticalPadding = cardVerticalPadding,
            nameFontSize = cardNameSize,
            nameLineHeight = cardNameLineHeight,
            priceFontSize = cardPriceSize,
            priceLineHeight = cardPriceLineHeight,
            rowSpacing = sectionRowSpacing,
          )
          OtcProductColumn(
            modifier = Modifier.weight(1f),
            items = potItems,
            selectedIds = selectedIds,
            onItemTap = { item ->
              activeItemId = item.id
              draftQuantity = (quantities[item.id] ?: 1).toString()
            },
            fontFamily = dmSansThin,
            cardWidth = productCardWidth,
            cardHeight = productCardHeight,
            photoSize = productPhotoSize,
            horizontalPadding = cardHorizontalPadding,
            verticalPadding = cardVerticalPadding,
            nameFontSize = cardNameSize,
            nameLineHeight = cardNameLineHeight,
            priceFontSize = cardPriceSize,
            priceLineHeight = cardPriceLineHeight,
            rowSpacing = sectionRowSpacing,
          )
          OtcProductColumn(
            modifier = Modifier.weight(1f),
            items = supplyItems,
            selectedIds = selectedIds,
            onItemTap = { item ->
              activeItemId = item.id
              draftQuantity = (quantities[item.id] ?: 1).toString()
            },
            fontFamily = dmSansThin,
            cardWidth = productCardWidth,
            cardHeight = productCardHeight,
            photoSize = productPhotoSize,
            horizontalPadding = cardHorizontalPadding,
            verticalPadding = cardVerticalPadding,
            nameFontSize = cardNameSize,
            nameLineHeight = cardNameLineHeight,
            priceFontSize = cardPriceSize,
            priceLineHeight = cardPriceLineHeight,
            rowSpacing = sectionRowSpacing,
          )
        }

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f)
            .background(Bg)
            .onSizeChanged { stickyBlockHeightPx = it.height }
              .padding(
                start = horizontalPadding,
                top = contentTopPadding,
                end = horizontalPadding,
                bottom = 0.dp,
              ),
          horizontalAlignment = Alignment.Start,
        ) {
          MrpEditor(
            totalAmount = totalAmount,
            landingTotal = landingTotal,
            fontFamily = dmSansThin,
            labelSize = mrpLabelSize,
            amountSize = amountSize,
            actionButtonSize = headerActionSize,
            centerActionWithAmount = true,
            topAlignAction = true,
            showAction = false,
            onAddToCart = {
              if (selectedCartItems.isNotEmpty()) {
                onAddToCart(selectedCartItems)
                quantities.clear()
                activeItemId = null
                draftQuantity = "1"
              }
            },
            addEnabled = selectedCartItems.isNotEmpty(),
            leftAligned = true,
          )

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = if (phoneScreen) 12.dp else 8.dp),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (phoneScreen) 4.dp else 0.dp)
                .padding(end = selectionRowEndInset)
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(selectionRowSpacing, Alignment.Start),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              repeat(maxOf(visibleSelectionSlots, selectedItems.size)) { index ->
                OtcSelectionThumbnail(
                  item = selectedItems.getOrNull(index),
                  size = thumbnailSize,
                  fontFamily = dmSansThin,
                  onDelete = { item -> quantities.remove(item.id) },
                )
              }
            }

            Image(
              painter = painterResource(R.drawable.plus_button),
              contentDescription = "Add to Cart",
              modifier = Modifier
                .align(Alignment.CenterEnd)
                .zIndex(1f)
                .size(staticPlusButtonSize)
                .alpha(if (selectedCartItems.isNotEmpty()) 1f else 0.4f)
                .clickable(
                  enabled = selectedCartItems.isNotEmpty(),
                  onClick = {
                    onAddToCart(selectedCartItems)
                    quantities.clear()
                    activeItemId = null
                    draftQuantity = "1"
                  },
                ),
            )
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = if (phoneScreen) 10.dp else 2.dp, bottom = if (phoneScreen) 6.dp else 0.dp),
            horizontalArrangement = Arrangement.spacedBy(columnGap),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            OtcColumnLogo(
              modifier = Modifier.weight(1f),
              imageRes = R.drawable.otc_title_plants,
              imageSize = columnLogoSize,
            )
            OtcColumnLogo(
              modifier = Modifier.weight(1f),
              imageRes = R.drawable.otc_title_pots,
              imageSize = columnLogoSize,
            )
            OtcColumnLogo(
              modifier = Modifier.weight(1f),
              imageRes = R.drawable.otc_title_supplies,
              imageSize = columnLogoSize,
            )
          }
        }

        if (activeItem != null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .align(Alignment.TopCenter)
              .padding(top = contentTopPadding + stickyBlockHeight + 12.dp)
              .zIndex(2f),
          ) {
            OtcQuantityPopup(
              item = activeItem,
              quantity = draftQuantity,
              onQuantityChange = { draftQuantity = it.filter(Char::isDigit) },
              onDecrease = {
                val next = ((draftQuantity.toIntOrNull() ?: 1) - 1).coerceAtLeast(1)
                draftQuantity = next.toString()
              },
              onIncrease = {
                val next = ((draftQuantity.toIntOrNull() ?: 1) + 1).coerceAtMost(99)
                draftQuantity = next.toString()
              },
              onConfirm = {
                quantities[activeItem.id] = (draftQuantity.toIntOrNull() ?: 1).coerceAtLeast(1)
                activeItemId = null
              },
              onClose = { activeItemId = null },
              fontFamily = dmSansThin,
              imageSize = carouselPhotoSize * 2f,
            )
          }
        }
      } else {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(
              start = horizontalPadding,
              top = contentTopPadding,
              end = horizontalPadding,
              bottom = OtcPageVerticalPadding,
            ),
          verticalArrangement = Arrangement.spacedBy(sectionGap),
        ) {
          MrpEditor(
            totalAmount = totalAmount,
            landingTotal = landingTotal,
            fontFamily = dmSansThin,
            labelSize = mrpLabelSize,
            amountSize = amountSize,
            actionButtonSize = headerActionSize,
            centerActionWithAmount = false,
            topAlignAction = false,
            showAction = true,
            onAddToCart = {
              if (selectedCartItems.isNotEmpty()) {
                onAddToCart(selectedCartItems)
                quantities.clear()
                activeItemId = null
                draftQuantity = "1"
              }
            },
            addEnabled = selectedCartItems.isNotEmpty(),
            leftAligned = true,
          )

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(selectionRowSpacing, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            repeat(maxOf(visibleSelectionSlots, selectedItems.size)) { index ->
              OtcSelectionThumbnail(
                item = selectedItems.getOrNull(index),
                size = thumbnailSize,
                fontFamily = dmSansThin,
                onDelete = { item -> quantities.remove(item.id) },
              )
            }
          }

          if (activeItem != null) {
            OtcQuantityPopup(
              item = activeItem,
              quantity = draftQuantity,
              onQuantityChange = { draftQuantity = it.filter(Char::isDigit) },
              onDecrease = {
                val next = ((draftQuantity.toIntOrNull() ?: 1) - 1).coerceAtLeast(1)
                draftQuantity = next.toString()
              },
              onIncrease = {
                val next = ((draftQuantity.toIntOrNull() ?: 1) + 1).coerceAtMost(99)
                draftQuantity = next.toString()
              },
              onConfirm = {
                quantities[activeItem.id] = (draftQuantity.toIntOrNull() ?: 1).coerceAtLeast(1)
                activeItemId = null
              },
              onClose = { activeItemId = null },
              fontFamily = dmSansThin,
              imageSize = carouselPhotoSize * 2f,
            )
          }

          OtcCarouselSection(
            title = "Plant",
            titleImageRes = R.drawable.otc_title_plants,
            titleImageSize = titleAssetSize,
            items = plantItems,
            selectedIds = selectedIds,
            onItemTap = { item ->
              activeItemId = item.id
              draftQuantity = (quantities[item.id] ?: 1).toString()
            },
            fontFamily = dmSansThin,
            cardWidth = carouselCardWidth,
            cardHeight = carouselCardHeight,
            photoSize = carouselPhotoSize,
            rowSpacing = sectionRowSpacing,
            cardHorizontalPadding = cardHorizontalPadding,
            cardVerticalPadding = cardVerticalPadding,
            nameFontSize = cardNameSize,
            nameLineHeight = cardNameLineHeight,
            priceFontSize = cardPriceSize,
            priceLineHeight = cardPriceLineHeight,
            stackCards = false,
            mobileCardLayout = false,
          )
          OtcCarouselSection(
            title = "Pots",
            titleImageRes = R.drawable.otc_title_pots,
            titleImageSize = titleAssetSize,
            items = potItems,
            selectedIds = selectedIds,
            onItemTap = { item ->
              activeItemId = item.id
              draftQuantity = (quantities[item.id] ?: 1).toString()
            },
            fontFamily = dmSansThin,
            cardWidth = carouselCardWidth,
            cardHeight = carouselCardHeight,
            photoSize = carouselPhotoSize,
            rowSpacing = sectionRowSpacing,
            cardHorizontalPadding = cardHorizontalPadding,
            cardVerticalPadding = cardVerticalPadding,
            nameFontSize = cardNameSize,
            nameLineHeight = cardNameLineHeight,
            priceFontSize = cardPriceSize,
            priceLineHeight = cardPriceLineHeight,
            stackCards = false,
            mobileCardLayout = false,
          )
          OtcCarouselSection(
            title = "Supplies",
            titleImageRes = R.drawable.otc_title_supplies,
            titleImageSize = titleAssetSize,
            items = supplyItems,
            selectedIds = selectedIds,
            onItemTap = { item ->
              activeItemId = item.id
              draftQuantity = (quantities[item.id] ?: 1).toString()
            },
            fontFamily = dmSansThin,
            cardWidth = carouselCardWidth,
            cardHeight = carouselCardHeight,
            photoSize = carouselPhotoSize,
            rowSpacing = sectionRowSpacing,
            cardHorizontalPadding = cardHorizontalPadding,
            cardVerticalPadding = cardVerticalPadding,
            nameFontSize = cardNameSize,
            nameLineHeight = cardNameLineHeight,
            priceFontSize = cardPriceSize,
            priceLineHeight = cardPriceLineHeight,
            stackCards = false,
            mobileCardLayout = false,
          )
        }
      }
    }

    OtcActionOnlyHeader(
      modifier = Modifier
        .width(contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = horizontalPadding, top = headerTopPadding, end = horizontalPadding),
      onBack = onBack,
      actionButtonSize = headerActionSize,
      actionIconSize = headerIconSize,
      actionSpacing = headerSpacing,
      containerHeight = headerContainerHeight,
      logoWidth = headerLogoWidth,
      logoHeight = headerLogoHeight,
      logoTextSize = headerLogoTextSize,
      logoMarkSize = headerLogoMarkSize,
      actionTopPadding = if (phoneScreen) OtcPhoneHeaderActionTopPadding else 0.dp,
      middleAction = {
        CircleIconAction(
          size = headerActionSize,
          borderColor = Accent,
          borderWidth = 2.dp,
          icon = {
            if (cartCount > 0) {
              Text(
                text = cartCount.toString(),
                color = Accent,
                style = TextStyle(
                  fontFamily = dmSansThin,
                  fontWeight = FontWeight.Normal,
                  fontSize = 13.33.sp,
                ),
              )
            } else {
              Box(
                modifier = Modifier.size(headerIconSize),
                contentAlignment = Alignment.Center,
              ) {
                Image(
                  painter = painterResource(R.drawable.wheelbarrow),
                  contentDescription = "Open cart",
                  modifier = Modifier.size(headerIconSize),
                )
              }
            }
          },
          onClick = onOpenCart,
        )
      },
      bottomAction = {
        CircleIconAction(
          size = headerActionSize,
          borderColor = Accent,
          borderWidth = 2.dp,
          icon = {
            ClosedDealsShortcutIcon(size = headerIconSize)
          },
          onClick = onOpenSavedDeals,
        )
      },
    )

  }
}

@Composable
private fun OtcColumnLogo(
  modifier: Modifier = Modifier,
  imageRes: Int,
  imageSize: Dp,
) {
  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    Image(
      painter = painterResource(imageRes),
      contentDescription = null,
      contentScale = ContentScale.Fit,
      modifier = Modifier.size(imageSize),
    )
  }
}

@Composable
private fun OtcProductColumn(
  modifier: Modifier = Modifier,
  items: List<OtcCarouselItem>,
  selectedIds: Set<String>,
  onItemTap: (OtcCarouselItem) -> Unit,
  fontFamily: FontFamily,
  cardWidth: Dp,
  cardHeight: Dp,
  photoSize: Dp,
  horizontalPadding: Dp,
  verticalPadding: Dp,
  nameFontSize: androidx.compose.ui.unit.TextUnit,
  nameLineHeight: androidx.compose.ui.unit.TextUnit,
  priceFontSize: androidx.compose.ui.unit.TextUnit,
  priceLineHeight: androidx.compose.ui.unit.TextUnit,
  rowSpacing: Dp,
) {
  val listState = rememberLazyListState()
  LazyColumn(
    modifier = modifier.fillMaxHeight(),
    state = listState,
    verticalArrangement = Arrangement.spacedBy(rowSpacing),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    items(items, key = { it.id }) { item ->
      OtcCarouselCard(
        item = item,
        selected = item.id in selectedIds,
        onTap = { onItemTap(item) },
        fontFamily = fontFamily,
        cardWidth = cardWidth,
        cardHeight = cardHeight,
        photoSize = photoSize,
        horizontalPadding = horizontalPadding,
        verticalPadding = verticalPadding,
        nameFontSize = nameFontSize,
        nameLineHeight = nameLineHeight,
        priceFontSize = priceFontSize,
        priceLineHeight = priceLineHeight,
        mobileCardLayout = true,
      )
    }
  }
}

@Composable
private fun MrpEditor(
  totalAmount: Int,
  landingTotal: Int,
  fontFamily: FontFamily,
  labelSize: androidx.compose.ui.unit.TextUnit,
  amountSize: androidx.compose.ui.unit.TextUnit,
  onAddToCart: () -> Unit,
  addEnabled: Boolean,
  actionButtonSize: Dp = OtcHeaderActionSize,
  centerActionWithAmount: Boolean = false,
  leftAligned: Boolean = false,
  topAlignAction: Boolean = false,
  showAction: Boolean = true,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(14.dp),
    verticalAlignment = when {
      topAlignAction -> Alignment.Top
      centerActionWithAmount -> Alignment.CenterVertically
      else -> Alignment.Bottom
    },
  ) {
    Column(
      modifier = Modifier.weight(1f),
      horizontalAlignment = if (leftAligned) Alignment.Start else Alignment.CenterHorizontally,
    ) {
      Text(
        text = "₹ ${formatAmount(totalAmount)}",
        color = Ink,
        textAlign = if (leftAligned) TextAlign.Start else TextAlign.Center,
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Thin,
          fontSize = amountSize,
          lineHeight = amountSize,
        ),
      )
      Text(
        text = "₹ ${formatAmount(landingTotal)}",
        color = Color(0xFFB8B8B8),
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Normal,
          fontSize = labelSize * 1.45f,
          lineHeight = labelSize * 1.7f,
        ),
      )
    }

    if (showAction) {
      Image(
        painter = painterResource(R.drawable.plus_button),
        contentDescription = "Add to Cart",
        modifier = Modifier
          .size(actionButtonSize)
          .alpha(if (addEnabled) 1f else 0.4f)
          .clickable(enabled = addEnabled, onClick = onAddToCart),
      )
    }
  }
}

@Composable
private fun OtcQuantityPopup(
  item: OtcCarouselItem,
  quantity: String,
  onQuantityChange: (String) -> Unit,
  onDecrease: () -> Unit,
  onIncrease: () -> Unit,
  onConfirm: () -> Unit,
  onClose: () -> Unit,
  fontFamily: FontFamily,
  imageSize: Dp,
) {
  val imageBitmap by rememberDecodedImage(item.imageUri)
  var clearedInitialQuantity by remember(item.id) { mutableStateOf(false) }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(
        elevation = 10.dp,
        shape = CardShape,
        ambientColor = SoftGreyShadow,
        spotColor = SoftGreyShadow,
      ),
  ) {
    val previewSize = minOf(maxWidth * 0.52f, imageSize * 1.32f, 240.dp)
    Surface(
      shape = CardShape,
      color = Panel,
      border = androidx.compose.foundation.BorderStroke(1.dp, PanelBorder),
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(18.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Box(
              modifier = Modifier
                .size(previewSize)
                .background(Accent, CircleShape)
                .clip(CircleShape),
              contentAlignment = Alignment.Center,
            ) {
              if (imageBitmap != null) {
                Image(
                  bitmap = imageBitmap!!,
                  contentDescription = item.name,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize(),
                )
              } else {
                Text(
                  text = item.shortName,
                  color = Color.White,
                  textAlign = TextAlign.Center,
                  style = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Thin,
                    fontSize = minOf(previewSize.value * 0.22f, 28f).sp,
                    lineHeight = minOf(previewSize.value * 0.25f, 32f).sp,
                  ),
                )
              }
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              QuantityButton(
                text = "−",
                onClick = onDecrease,
                fontFamily = fontFamily,
              )

              Box(
                modifier = Modifier
                  .width(116.dp)
                  .height(58.dp)
                  .background(Color(0xFFF7F7F7), CardShape)
                  .border(1.dp, Accent.copy(alpha = 0.24f), CardShape),
                contentAlignment = Alignment.Center,
              ) {
                BasicTextField(
                  value = quantity,
                  onValueChange = { updated ->
                    onQuantityChange(updated.filter(Char::isDigit).take(4))
                  },
                  singleLine = true,
                  textStyle = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Thin,
                    fontSize = 30.sp,
                    lineHeight = 32.sp,
                    color = Ink,
                    textAlign = TextAlign.Center,
                  ),
                  keyboardOptions = KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                    imeAction = ImeAction.Done,
                  ),
                  keyboardActions = KeyboardActions(
                    onDone = { onConfirm() },
                  ),
                  cursorBrush = SolidColor(Ink),
                  modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                      if (focusState.isFocused && !clearedInitialQuantity && quantity == "1") {
                        clearedInitialQuantity = true
                        onQuantityChange("")
                      }
                    },
                  decorationBox = { innerTextField ->
                    Box(
                      modifier = Modifier.fillMaxWidth(),
                      contentAlignment = Alignment.Center,
                    ) {
                      innerTextField()
                    }
                  },
                )
              }

              QuantityButton(
                text = "+",
                onClick = onIncrease,
                fontFamily = fontFamily,
              )
            }

            Text(
              text = item.name,
              color = Ink,
              textAlign = TextAlign.Center,
              maxLines = 2,
              style = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp,
                lineHeight = 27.sp,
              ),
            )

            Text(
              text = "\u20B9 ${item.price}",
              color = Ink,
              textAlign = TextAlign.Center,
              style = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Thin,
                fontSize = 34.sp,
                lineHeight = 38.sp,
              ),
            )
          }

          Column(
            modifier = Modifier
              .fillMaxHeight()
              .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            CircleIconAction(
              size = 46.dp,
              icon = {
                Icon(
                  imageVector = Icons.Filled.Close,
                  contentDescription = "Close",
                  tint = Accent,
                )
              },
              onClick = onClose,
            )
            CircleIconAction(
              size = 46.dp,
              icon = {
                Icon(
                  imageVector = Icons.Filled.Check,
                  contentDescription = "Confirm quantity",
                  tint = Color(0xFF31A853),
                )
              },
              onClick = onConfirm,
            )
          }
        }
      }
    }

  }
}

@Composable
private fun QuantityButton(
  text: String,
  onClick: () -> Unit,
  fontFamily: FontFamily,
) {
  Box(
    modifier = Modifier
      .size(34.dp)
      .background(Accent.copy(alpha = 0.12f), CircleShape)
      .border(1.dp, Accent.copy(alpha = 0.38f), CircleShape)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = text,
      color = Accent,
      textAlign = TextAlign.Center,
      style = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 24.sp,
      ),
    )
  }
}

@Composable
private fun OtcSelectionThumbnail(
  item: OtcCarouselItem?,
  size: Dp,
  fontFamily: FontFamily,
  onDelete: (OtcCarouselItem) -> Unit,
) {
  val imageBitmap by rememberDecodedImage(item?.imageUri)
  Box(
    modifier = Modifier
      .size(size)
      .pointerInput(item?.id) {
        detectTapGestures(
          onLongPress = {
            if (item != null) {
              onDelete(item)
            }
          },
        )
      }
      .shadow(
        elevation = if (item == null) 0.dp else 8.dp,
        shape = CircleShape,
        ambientColor = SoftGreyShadow,
        spotColor = SoftGreyShadow,
      )
      .background(if (item == null) Color.White else Accent, CircleShape)
      .border(1.dp, Accent.copy(alpha = if (item == null) 0.24f else 0f), CircleShape)
      .clip(CircleShape),
    contentAlignment = Alignment.Center,
  ) {
    if (item != null) {
      if (imageBitmap != null) {
        Image(
          bitmap = imageBitmap!!,
          contentDescription = item.name,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
      } else {
        Text(
          text = item.shortName,
          color = Color.White,
          textAlign = TextAlign.Center,
          style = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Thin,
            fontSize = minOf(size.value * 0.22f, 13f).sp,
            lineHeight = minOf(size.value * 0.24f, 14f).sp,
          ),
        )
      }
    }
  }
}

@Composable
private fun OtcCarouselSection(
  modifier: Modifier = Modifier,
  title: String,
  titleImageRes: Int? = null,
  titleImageSize: Dp = 24.dp,
  items: List<OtcCarouselItem>,
  selectedIds: Set<String>,
  onItemTap: (OtcCarouselItem) -> Unit,
  fontFamily: FontFamily,
  cardWidth: Dp,
  cardHeight: Dp,
  photoSize: Dp,
  titleGap: Dp = 6.dp,
  rowSpacing: Dp = 12.dp,
  cardHorizontalPadding: Dp = 8.dp,
  cardVerticalPadding: Dp = 8.dp,
  nameFontSize: androidx.compose.ui.unit.TextUnit = 13.sp,
  nameLineHeight: androidx.compose.ui.unit.TextUnit = 15.sp,
  priceFontSize: androidx.compose.ui.unit.TextUnit = 24.sp,
  priceLineHeight: androidx.compose.ui.unit.TextUnit = 26.sp,
  lockToVisibleRow: Boolean = false,
  stackCards: Boolean = true,
  mobileCardLayout: Boolean = true,
) {
  val visibleItems = items
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(titleGap),
  ) {
    if (titleImageRes != null) {
      Image(
        painter = painterResource(titleImageRes),
        contentDescription = title,
        contentScale = ContentScale.Fit,
        modifier = Modifier.size(titleImageSize),
      )
    } else {
      Text(
        text = title,
        modifier = Modifier.fillMaxWidth(),
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Normal,
          fontSize = 18.sp,
          lineHeight = 20.sp,
          color = Ink,
        ),
      )
    }

    if (stackCards) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(rowSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        visibleItems.forEach { item ->
          OtcCarouselCard(
            item = item,
            selected = item.id in selectedIds,
            onTap = { onItemTap(item) },
            fontFamily = fontFamily,
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            photoSize = photoSize,
            horizontalPadding = cardHorizontalPadding,
            verticalPadding = cardVerticalPadding,
            nameFontSize = nameFontSize,
            nameLineHeight = nameLineHeight,
            priceFontSize = priceFontSize,
            priceLineHeight = priceLineHeight,
            mobileCardLayout = mobileCardLayout,
          )
        }
      }
    } else {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(rowSpacing, Alignment.Start),
        verticalAlignment = Alignment.Top,
      ) {
        visibleItems.forEach { item ->
          OtcCarouselCard(
            item = item,
            selected = item.id in selectedIds,
            onTap = { onItemTap(item) },
            fontFamily = fontFamily,
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            photoSize = photoSize,
            horizontalPadding = cardHorizontalPadding,
            verticalPadding = cardVerticalPadding,
            nameFontSize = nameFontSize,
            nameLineHeight = nameLineHeight,
            priceFontSize = priceFontSize,
            priceLineHeight = priceLineHeight,
            mobileCardLayout = mobileCardLayout,
          )
        }
      }
    }
  }
}

@Composable
private fun OtcCarouselCard(
  item: OtcCarouselItem,
  selected: Boolean,
  onTap: () -> Unit,
  fontFamily: FontFamily,
  cardWidth: Dp,
  cardHeight: Dp,
  photoSize: Dp,
  horizontalPadding: Dp = 8.dp,
  verticalPadding: Dp = 8.dp,
  nameFontSize: androidx.compose.ui.unit.TextUnit = 13.sp,
  nameLineHeight: androidx.compose.ui.unit.TextUnit = 15.sp,
  priceFontSize: androidx.compose.ui.unit.TextUnit = 24.sp,
  priceLineHeight: androidx.compose.ui.unit.TextUnit = 26.sp,
  mobileCardLayout: Boolean = true,
) {
  val imageBitmap by rememberDecodedImage(item.imageUri)
  Surface(
    modifier = Modifier
      .width(cardWidth)
      .height(cardHeight)
      .clickable(onClick = onTap)
      .border(
        width = if (selected) 2.dp else 1.dp,
        color = if (selected) Accent else Accent.copy(alpha = 0.18f),
        shape = CardShape,
      ),
    shape = CardShape,
    color = Color.White,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    if (mobileCardLayout) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
      ) {
        Spacer(modifier = Modifier.height(8.dp))

        Box(
          modifier = Modifier
            .size(photoSize)
            .background(Accent.copy(alpha = if (selected) 1f else 0.82f), CircleShape)
            .clip(CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          if (imageBitmap != null) {
            Image(
              bitmap = imageBitmap!!,
              contentDescription = item.name,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize(),
            )
          } else {
            Text(
              text = item.shortName,
              color = Color.White,
              textAlign = TextAlign.Center,
              style = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Thin,
                fontSize = minOf(photoSize.value * 0.22f, 14f).sp,
                lineHeight = minOf(photoSize.value * 0.24f, 16f).sp,
              ),
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f, fill = true)
            .padding(bottom = 8.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Bottom,
        ) {
          Text(
            text = item.name,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 2,
            style = TextStyle(
              fontFamily = fontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = nameFontSize,
              lineHeight = nameLineHeight,
              color = Ink,
            ),
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "\u20B9 ${item.price}",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = TextStyle(
              fontFamily = fontFamily,
              fontWeight = FontWeight.Thin,
              fontSize = priceFontSize,
              lineHeight = priceLineHeight,
              color = Ink,
            ),
          )
        }
      }
    } else {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        Box(
          modifier = Modifier
            .size(photoSize)
            .background(Accent.copy(alpha = if (selected) 1f else 0.82f), CircleShape)
            .clip(CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          if (imageBitmap != null) {
            Image(
              bitmap = imageBitmap!!,
              contentDescription = item.name,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize(),
            )
          } else {
            Text(
              text = item.shortName,
              color = Color.White,
              textAlign = TextAlign.Center,
              style = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Thin,
                fontSize = minOf(photoSize.value * 0.22f, 14f).sp,
                lineHeight = minOf(photoSize.value * 0.24f, 16f).sp,
              ),
            )
          }
        }

        Text(
          text = item.name,
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Center,
          maxLines = 1,
          style = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = nameFontSize,
            lineHeight = nameLineHeight,
            color = Ink,
          ),
        )

        Text(
          text = "\u20B9 ${item.price}",
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Center,
          style = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Thin,
            fontSize = priceFontSize,
            lineHeight = priceLineHeight,
            color = Ink,
          ),
        )
      }
    }
  }
}

@Composable
fun OpenCartScreen(
  rows: List<OpenCartRow>,
  clientName: String,
  clientPhone: String,
  clientId: String,
  transactionCode: String,
  nextSavedDealOrdinal: Int,
  onClientNameChange: (String) -> Unit,
  onClientPhoneChange: (String) -> Unit,
  onClientIdChange: (String) -> Unit,
  onTransactionCodeChange: (String) -> Unit,
  onBack: () -> Unit,
  onOpenSavedDeals: () -> Unit,
  onAddItemsToRow: (String, List<CartProductItem>) -> Unit,
  onUpdateRowAmount: (String, Int) -> Unit,
  onUpdateRowTitle: (String, String) -> Unit,
  onUpdateItemQuantity: (String, String, Int) -> Unit,
  onDeleteItem: (String, String) -> Unit,
  onDeleteRow: (String) -> Unit,
  onClearCart: () -> Unit,
  onSaveDeal: (SavedDeal) -> Unit,
) {
  val fontFamily = rememberDashboardFont()
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var activeEditor by remember { mutableStateOf<Pair<String, CartProductItem>?>(null) }
  var rowItemPickerId by remember { mutableStateOf<String?>(null) }
  var rowCheckoutId by remember { mutableStateOf<String?>(null) }
  var pendingDeleteRowId by remember { mutableStateOf<String?>(null) }
  var showClearCartConfirm by remember { mutableStateOf(false) }
  var showPaymentGateway by remember { mutableStateOf(false) }
  var pendingInvoiceExport by remember { mutableStateOf<PendingInvoiceExport?>(null) }
  val totalMrp = rows.sumOf { row -> row.displayAmount() }
  val invoiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
    val export = pendingInvoiceExport
    pendingInvoiceExport = null
    if (export == null) {
      return@rememberLauncherForActivityResult
    }
    scope.launch {
      val saved = if (uri == null) {
        false
      } else {
        withContext(Dispatchers.IO) {
          runCatching {
            context.contentResolver.openOutputStream(uri, "rwt")?.use { stream ->
              stream.write(export.pdfBytes)
              stream.flush()
            } != null
          }.getOrDefault(false)
        }
      }
      Toast.makeText(
        context,
        when {
          uri == null -> "Deal saved. Invoice download was skipped."
          saved -> "Invoice downloaded successfully."
          else -> "Deal saved, but invoice download failed."
        },
        Toast.LENGTH_SHORT,
      ).show()
      onOpenSavedDeals()
    }
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Bg)
      .navigationBarsPadding(),
  ) {
    val phoneScreen = isPhoneScreen(maxWidth)
    val compactScreen = isCompactPhoneScreen(maxWidth, maxHeight)
    val statusBarTopPaddingPx = ViewCompat.getRootWindowInsets(LocalView.current)
      ?.getInsets(WindowInsetsCompat.Type.statusBars())
      ?.top ?: 0
    val statusBarTopPadding = with(LocalDensity.current) { statusBarTopPaddingPx.toDp() }
    val contentWidth = if (phoneScreen) maxWidth else minOf(maxWidth - 28.dp, 980.dp)
    val horizontalPadding = when {
      phoneScreen -> OtcPhoneHeaderHorizontalPadding
      maxWidth > 700.dp -> OtcHeaderHorizontalPadding
      compactScreen -> 8.dp
      else -> 12.dp
    }
    val headerActionSize = when {
      phoneScreen -> OtcPhoneHeaderActionSize
      compactScreen -> 42.dp
      else -> OtcHeaderActionSize
    }
    val headerIconSize = if (phoneScreen) OtcPhoneHeaderIconSize else OtcHeaderIconSize
    val headerSpacing = if (phoneScreen) OtcPhoneHeaderSpacing else if (compactScreen) 12.dp else OtcHeaderActionSpacing
    val headerContainerHeight = when {
      phoneScreen -> OtcHeaderFrameHeight
      compactScreen -> 122.dp
      else -> OtcHeaderFrameHeight
    }
    val headerLogoWidth = when {
      phoneScreen -> OtcPhoneHeaderLogoWidth
      compactScreen -> 220.dp
      else -> OtcHeaderLogoWidth
    }
    val headerLogoHeight = when {
      phoneScreen -> OtcPhoneHeaderLogoHeight
      compactScreen -> 82.dp
      else -> OtcHeaderLogoHeight
    }
    val headerLogoTextSize = when {
      phoneScreen -> OtcPhoneHeaderLogoTextSize
      compactScreen -> 22.sp
      else -> 30.sp
    }
    val headerLogoMarkSize = if (phoneScreen) OtcPhoneHeaderLogoMarkSize else 60.dp
    val headerTopPadding = if (phoneScreen) statusBarTopPadding + OtcPhoneHeaderTopPadding else OtcHeaderTopOffset
    val contentTopPadding = headerTopPadding + headerContainerHeight + if (compactScreen) 2.dp else 12.dp
    Column(
      modifier = Modifier
        .width(contentWidth)
        .fillMaxSize()
        .align(Alignment.TopCenter)
        .padding(
          start = horizontalPadding,
          top = contentTopPadding,
          end = horizontalPadding,
          bottom = OtcPageVerticalPadding,
        ),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      horizontalAlignment = Alignment.Start,
    ) {
      CompactDetailCard(fontFamily = fontFamily) {
        LabeledInputCompactRow(
          label = "Client",
          labelImageRes = R.drawable.open_cart_client,
          value = clientName,
          onValueChange = onClientNameChange,
          fontFamily = fontFamily,
        )
        LabeledInputCompactRow(
          label = "Phone",
          labelImageRes = R.drawable.open_cart_phone,
          value = clientPhone,
          onValueChange = onClientPhoneChange,
          fontFamily = fontFamily,
          numeric = true,
        )
        LabeledInputCompactRow(
          label = "Client ID",
          labelImageRes = R.drawable.open_cart_client_id,
          value = clientId,
          onValueChange = onClientIdChange,
          fontFamily = fontFamily,
        )
        LabeledInputCompactRow(
          label = "Transaction code",
          labelImageRes = R.drawable.open_cart_transaction_code,
          value = transactionCode,
          onValueChange = onTransactionCodeChange,
          fontFamily = fontFamily,
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        LabeledReadOnlyCard(
          label = "Cart total",
          value = "\u20B9 ${formatAmount(totalMrp)}",
          fontFamily = fontFamily,
          modifier = Modifier.weight(1f),
          valueFontSize = 48.sp,
        )
        ActionButtonCard(
          title = "Review Payment",
          color = AccentGreen,
          fontFamily = fontFamily,
          onClick = { showPaymentGateway = true },
          modifier = Modifier.weight(0.72f),
        )
      }

      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        if (rows.isEmpty()) {
          item {
            EmptyStateCard(
              text = "No items in open cart yet. Add selections from OTC to create a product row.",
              fontFamily = fontFamily,
            )
          }
        } else {
          items(rows, key = { it.id }) { row ->
            OpenCartDealRow(
              row = row,
              clientName = clientName,
              transactionCode = transactionCode,
              fontFamily = fontFamily,
              onEdit = { rowItemPickerId = row.id },
              onCalculator = { rowCheckoutId = row.id },
              onItemTap = { item -> activeEditor = row.id to item },
              onLongPress = { pendingDeleteRowId = row.id },
            )
          }
        }
      }
    }

    val rowItemPicker = rowItemPickerId?.let { rowId -> rows.firstOrNull { it.id == rowId }?.let { rowId to it } }
    if (rowItemPicker != null) {
      OpenCartRowItemPickerDialog(
        rowTitle = rowItemPicker.second.title,
        fontFamily = fontFamily,
        onDismiss = { rowItemPickerId = null },
        onAddItems = { items ->
          onAddItemsToRow(rowItemPicker.first, items)
          rowItemPickerId = null
        },
      )
    }

    val rowCheckout = rowCheckoutId?.let { rowId -> rows.firstOrNull { it.id == rowId }?.let { rowId to it } }
    if (rowCheckout != null) {
      CheckoutAdjustmentsDialog(
        row = rowCheckout.second,
        fontFamily = fontFamily,
        onDismiss = { rowCheckoutId = null },
        onApply = { amount ->
          onUpdateRowAmount(rowCheckout.first, amount)
          rowCheckoutId = null
        },
      )
    }

    val editor = activeEditor
    if (editor != null) {
      CartItemEditorDialog(
        item = editor.second,
        fontFamily = fontFamily,
        onDismiss = { activeEditor = null },
        onQuantityChange = { onUpdateItemQuantity(editor.first, editor.second.id, it) },
        onDelete = {
          onDeleteItem(editor.first, editor.second.id)
          activeEditor = null
        },
      )
    }

    val deleteRowId = pendingDeleteRowId
    if (deleteRowId != null) {
      val rowTitle = rows.firstOrNull { it.id == deleteRowId }?.title ?: "this row"
      ConfirmDeleteItemDialog(
        itemName = rowTitle,
        onConfirm = {
          onDeleteRow(deleteRowId)
          pendingDeleteRowId = null
        },
        onDismiss = { pendingDeleteRowId = null },
      )
    }

    if (showClearCartConfirm) {
      ConfirmDeleteItemDialog(
        itemName = "Clear the entire open cart?",
        onConfirm = {
          onClearCart()
          showClearCartConfirm = false
        },
        onDismiss = { showClearCartConfirm = false },
      )
    }

    OpenCartHeader(
      modifier = Modifier
        .width(contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = horizontalPadding, top = headerTopPadding, end = horizontalPadding),
      onBack = onBack,
      onOpenSavedDeals = onOpenSavedDeals,
      actionButtonSize = headerActionSize,
      actionIconSize = headerIconSize,
      actionSpacing = headerSpacing,
      containerHeight = headerContainerHeight,
      logoWidth = headerLogoWidth,
      logoHeight = headerLogoHeight,
      logoTextSize = headerLogoTextSize,
      logoMarkSize = headerLogoMarkSize,
      actionTopPadding = if (phoneScreen) OtcPhoneHeaderActionTopPadding else 0.dp,
    )

    if (showPaymentGateway) {
      PaymentGatewayDialog(
        totalAmount = totalMrp,
        clientName = clientName,
        clientPhone = clientPhone,
        clientId = clientId,
        transactionCode = transactionCode,
        fontFamily = fontFamily,
        onDismiss = { showPaymentGateway = false },
        onOpenCart = {
          showPaymentGateway = false
        },
        onCloseDealAndClearCart = {
          showPaymentGateway = false
          if (rows.isNotEmpty()) {
            val closedDeal = SavedDeal(
              id = "saved-deal-${System.currentTimeMillis()}",
              title = rows.firstOrNull()?.title?.takeIf { it.isNotBlank() } ?: "Deal $nextSavedDealOrdinal",
              clientName = clientName,
              clientPhone = clientPhone,
              clientId = clientId,
              transactionCode = transactionCode,
              rows = rows.map { row -> row.copy(items = row.items.toList()) },
            )
            onSaveDeal(closedDeal)
            runCatching {
              PendingInvoiceExport(
                fileName = buildInvoiceFileName(closedDeal),
                pdfBytes = buildInvoicePdf(closedDeal),
              )
            }.onSuccess { export ->
              pendingInvoiceExport = export
              invoiceLauncher.launch(export.fileName)
            }.onFailure {
              Toast.makeText(context, "Deal saved, but invoice generation failed.", Toast.LENGTH_SHORT).show()
              onOpenSavedDeals()
            }
          }
        },
      )
    }
  }
}

@Composable
fun SavedDealsScreen(
  deals: List<SavedDeal>,
  onBack: () -> Unit,
  onClearCart: () -> Unit,
  onUpdateDealInfo: (String, String, String, String, String) -> Unit,
) {
  val fontFamily = rememberDashboardFont()
  var searchQuery by rememberSaveable { mutableStateOf("") }
  val filteredDeals = remember(deals, searchQuery) {
    if (searchQuery.isBlank()) {
      deals
    } else {
      deals.filter { deal ->
        val haystack = buildString {
          append(deal.title)
          append(' ')
          append(deal.clientName)
          append(' ')
          append(deal.clientId)
          append(' ')
          append(deal.transactionCode)
          append(' ')
          append(deal.rows.joinToString(" ") { row -> row.title })
        }
        haystack.contains(searchQuery.trim(), ignoreCase = true)
      }
    }
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Bg)
      .navigationBarsPadding(),
  ) {
    val phoneScreen = isPhoneScreen(maxWidth)
    val compactScreen = isCompactPhoneScreen(maxWidth, maxHeight)
    val statusBarTopPaddingPx = ViewCompat.getRootWindowInsets(LocalView.current)
      ?.getInsets(WindowInsetsCompat.Type.statusBars())
      ?.top ?: 0
    val statusBarTopPadding = with(LocalDensity.current) { statusBarTopPaddingPx.toDp() }
    val contentWidth = if (phoneScreen) maxWidth else minOf(maxWidth - 28.dp, 980.dp)
    val horizontalPadding = when {
      phoneScreen -> OtcPhoneHeaderHorizontalPadding
      maxWidth > 700.dp -> OtcHeaderHorizontalPadding
      compactScreen -> 8.dp
      else -> 12.dp
    }
    val headerActionSize = when {
      phoneScreen -> OtcPhoneHeaderActionSize
      compactScreen -> 42.dp
      else -> OtcHeaderActionSize
    }
    val headerIconSize = if (phoneScreen) OtcPhoneHeaderIconSize else OtcHeaderIconSize
    val headerSpacing = if (phoneScreen) OtcPhoneHeaderSpacing else if (compactScreen) 12.dp else OtcHeaderActionSpacing
    val headerContainerHeight = when {
      phoneScreen -> OtcHeaderFrameHeight
      compactScreen -> 122.dp
      else -> OtcHeaderFrameHeight
    }
    val headerLogoWidth = when {
      phoneScreen -> OtcPhoneHeaderLogoWidth
      compactScreen -> 220.dp
      else -> OtcHeaderLogoWidth
    }
    val headerLogoHeight = when {
      phoneScreen -> OtcPhoneHeaderLogoHeight
      compactScreen -> 82.dp
      else -> OtcHeaderLogoHeight
    }
    val headerLogoTextSize = when {
      phoneScreen -> OtcPhoneHeaderLogoTextSize
      compactScreen -> 22.sp
      else -> 30.sp
    }
    val headerLogoMarkSize = if (phoneScreen) OtcPhoneHeaderLogoMarkSize else 60.dp
    val headerTopPadding = if (phoneScreen) statusBarTopPadding + OtcPhoneHeaderTopPadding else OtcHeaderTopOffset
    val contentTopPadding = headerTopPadding + headerContainerHeight + if (compactScreen) 4.dp else 14.dp
    LazyColumn(
      modifier = Modifier
        .width(contentWidth)
        .fillMaxSize()
        .align(Alignment.TopCenter)
        .padding(
          start = horizontalPadding,
          top = contentTopPadding,
          end = horizontalPadding,
          bottom = OtcPageVerticalPadding,
        ),
      verticalArrangement = Arrangement.spacedBy(if (compactScreen) 10.dp else 14.dp),
    ) {
      item {
        SearchInventoryField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          fontFamily = fontFamily,
          placeholder = "Search closed deals",
        )
      }

      if (filteredDeals.isEmpty()) {
        item {
          EmptyStateCard(
            text = if (deals.isEmpty()) "No deals saved yet. Save a deal from Open Cart to keep a record here." else "No closed deals match this filter.",
            fontFamily = fontFamily,
          )
        }
      } else {
        items(filteredDeals, key = { it.id }) { deal ->
          SavedDealCard(
            deal = deal,
            fontFamily = fontFamily,
            onClearCart = onClearCart,
            onDealInfoChange = onUpdateDealInfo,
          )
        }
      }
    }

    SavedDealsHeader(
      modifier = Modifier
        .width(contentWidth)
        .align(Alignment.TopCenter)
        .padding(start = horizontalPadding, top = headerTopPadding, end = horizontalPadding),
      onBack = onBack,
      actionButtonSize = headerActionSize,
      actionIconSize = headerIconSize,
      actionSpacing = headerSpacing,
      containerHeight = headerContainerHeight,
      logoWidth = headerLogoWidth,
      logoHeight = headerLogoHeight,
      logoTextSize = headerLogoTextSize,
      logoMarkSize = headerLogoMarkSize,
      actionTopPadding = if (phoneScreen) OtcPhoneHeaderActionTopPadding else 0.dp,
    )
  }
}

@Composable
private fun OpenCartHeader(
  modifier: Modifier = Modifier,
  onBack: () -> Unit,
  onOpenSavedDeals: () -> Unit,
  actionButtonSize: Dp = OtcHeaderActionSize,
  actionIconSize: Dp = OtcHeaderIconSize,
  actionSpacing: Dp = OtcHeaderActionSpacing,
  containerHeight: Dp = OtcHeaderFrameHeight,
  logoWidth: Dp = OtcHeaderLogoWidth,
  logoHeight: Dp = OtcHeaderLogoHeight,
  logoTextSize: androidx.compose.ui.unit.TextUnit = 30.sp,
  logoMarkSize: Dp = 60.dp,
  actionTopPadding: Dp = 0.dp,
) {
  OtcActionOnlyHeader(
    modifier = modifier,
    onBack = onBack,
    actionButtonSize = actionButtonSize,
    actionIconSize = actionIconSize,
    actionSpacing = actionSpacing,
    containerHeight = containerHeight,
    logoWidth = logoWidth,
    logoHeight = logoHeight,
    logoTextSize = logoTextSize,
    logoMarkSize = logoMarkSize,
    actionTopPadding = actionTopPadding,
    middleAction = {
      CircleIconAction(
        size = actionButtonSize,
        borderColor = Accent,
        borderWidth = 2.dp,
        icon = {
          ClosedDealsShortcutIcon(actionIconSize)
        },
        onClick = onOpenSavedDeals,
      )
    },
  )
}

@Composable
private fun OpenCartRowItemPickerDialog(
  rowTitle: String,
  fontFamily: FontFamily,
  onDismiss: () -> Unit,
  onAddItems: (List<CartProductItem>) -> Unit,
) {
  val catalog = otcCatalog()
  var activeItemId by remember { mutableStateOf<String?>(null) }
  var draftQuantity by remember { mutableStateOf("1") }
  val quantities = remember { mutableStateMapOf<String, Int>() }
  val activeItem = catalog.firstOrNull { it.id == activeItemId }
  val selectedIds = quantities.keys.toSet()
  val selectedItems = catalog.filter { it.id in selectedIds }
  val selectedCartItems = selectedItems.mapNotNull { item ->
    val quantity = quantities[item.id] ?: return@mapNotNull null
    CartProductItem(
      id = item.id,
      name = item.name,
      shortName = item.shortName,
      mrp = item.price.toIntOrNull() ?: 0,
      landingPrice = item.landingPrice.toIntOrNull() ?: 0,
      quantity = quantity,
      imageUri = item.imageUri,
    )
  }
  val totalAmount = selectedCartItems.sumOf { it.mrp * it.quantity }
  val landingTotal = selectedCartItems.sumOf { it.landingPrice * it.quantity }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier.fillMaxWidth(0.96f),
      shape = CardShape,
      color = Bg,
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
    ) {
      BoxWithConstraints(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 16.dp),
      ) {
        val cardWidth = when {
          maxWidth > 900.dp -> 172.dp
          maxWidth > 700.dp -> 152.dp
          else -> minOf(maxWidth * 0.48f, 148.dp)
        }
        val cardHeight = if (maxHeight > 760.dp) 148.dp else 124.dp
        val photoSize = if (maxHeight > 760.dp) 80.dp else 62.dp
        val amountSize = if (maxWidth > 700.dp) 32.sp else 28.sp

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = if (rowTitle.isBlank()) "Edit product" else rowTitle,
                color = Ink,
                style = TextStyle(
                  fontFamily = fontFamily,
                  fontWeight = FontWeight.Normal,
                  fontSize = 24.sp,
                  lineHeight = 26.sp,
                ),
              )
              Text(
                text = "Add last-minute items",
                color = Accent,
                style = TextStyle(
                  fontFamily = fontFamily,
                  fontWeight = FontWeight.Normal,
                  fontSize = 14.sp,
                ),
              )
            }

            CircleIconAction(
              size = OtcHeaderActionSize,
              borderColor = Accent,
              borderWidth = 2.dp,
              icon = {
                Icon(
                  imageVector = Icons.Filled.Close,
                  contentDescription = "Close item picker",
                  tint = Accent,
                  modifier = Modifier.size(OtcHeaderIconSize),
                )
              },
              onClick = onDismiss,
            )
          }

          MrpEditor(
            totalAmount = totalAmount,
            landingTotal = landingTotal,
            fontFamily = fontFamily,
            labelSize = 10.sp,
            amountSize = amountSize,
            onAddToCart = {
              if (selectedCartItems.isNotEmpty()) {
                onAddItems(selectedCartItems)
              }
            },
            addEnabled = selectedCartItems.isNotEmpty(),
            leftAligned = true,
          )

          if (selectedItems.isNotEmpty()) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              repeat(maxOf(6, selectedItems.size)) { index ->
                OtcSelectionThumbnail(
                  item = selectedItems.getOrNull(index),
                  size = 56.dp,
                  fontFamily = fontFamily,
                  onDelete = { tapped ->
                    quantities.remove(tapped.id)
                    if (activeItemId == tapped.id) {
                      activeItemId = null
                      draftQuantity = "1"
                    }
                  },
                )
              }
            }
          }

          activeItem?.let { item ->
            OtcQuantityPopup(
              item = item,
              quantity = draftQuantity,
              onQuantityChange = { updated ->
                val sanitized = updated.filter(Char::isDigit).take(2)
                draftQuantity = sanitized.ifBlank { "0" }
              },
              onDecrease = {
                val current = draftQuantity.toIntOrNull() ?: 0
                draftQuantity = (current - 1).coerceAtLeast(0).toString()
              },
              onIncrease = {
                val current = draftQuantity.toIntOrNull() ?: 0
                draftQuantity = (current + 1).coerceAtMost(99).toString()
              },
              onConfirm = {
                val quantity = draftQuantity.toIntOrNull() ?: 0
                if (quantity <= 0) {
                  quantities.remove(item.id)
                } else {
                  quantities[item.id] = quantity
                }
                activeItemId = null
                draftQuantity = "1"
              },
              onClose = {
                activeItemId = null
                draftQuantity = "1"
              },
              fontFamily = fontFamily,
              imageSize = 74.dp,
            )
          }

          OtcCarouselSection(
            title = "Plants",
            items = catalog.filter { it.category == OtcCarouselCategory.Plant },
            selectedIds = selectedIds,
            onItemTap = { item ->
              activeItemId = item.id
              draftQuantity = (quantities[item.id] ?: 1).toString()
            },
            fontFamily = fontFamily,
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            photoSize = photoSize,
          )

          OtcCarouselSection(
            title = "Pots",
            items = catalog.filter { it.category == OtcCarouselCategory.Pots },
            selectedIds = selectedIds,
            onItemTap = { item ->
              activeItemId = item.id
              draftQuantity = (quantities[item.id] ?: 1).toString()
            },
            fontFamily = fontFamily,
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            photoSize = photoSize,
          )

          OtcCarouselSection(
            title = "Supplies",
            items = catalog.filter { it.category == OtcCarouselCategory.Supplies },
            selectedIds = selectedIds,
            onItemTap = { item ->
              activeItemId = item.id
              draftQuantity = (quantities[item.id] ?: 1).toString()
            },
            fontFamily = fontFamily,
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            photoSize = photoSize,
          )
        }
      }
    }
  }
}

@Composable
private fun SavedDealsHeader(
  modifier: Modifier = Modifier,
  onBack: () -> Unit,
  actionButtonSize: Dp = OtcHeaderActionSize,
  actionIconSize: Dp = OtcHeaderIconSize,
  actionSpacing: Dp = OtcHeaderActionSpacing,
  containerHeight: Dp = OtcHeaderFrameHeight,
  logoWidth: Dp = OtcHeaderLogoWidth,
  logoHeight: Dp = OtcHeaderLogoHeight,
  logoTextSize: androidx.compose.ui.unit.TextUnit = 30.sp,
  logoMarkSize: Dp = 60.dp,
  actionTopPadding: Dp = 0.dp,
) {
  OtcActionOnlyHeader(
    modifier = modifier,
    onBack = onBack,
    actionButtonSize = actionButtonSize,
    actionIconSize = actionIconSize,
    actionSpacing = actionSpacing,
    containerHeight = containerHeight,
    logoWidth = logoWidth,
    logoHeight = logoHeight,
    logoTextSize = logoTextSize,
    logoMarkSize = logoMarkSize,
    actionTopPadding = actionTopPadding,
  )
}

@Composable
private fun CompactDetailCard(
  fontFamily: FontFamily,
  content: @Composable () -> Unit,
) {
  BoxWithConstraints {
    val compactCard = maxWidth < 360.dp
    Surface(
      shape = CardShape,
      color = Panel,
      border = androidx.compose.foundation.BorderStroke(1.dp, PanelBorder),
      tonalElevation = 0.dp,
      shadowElevation = 4.dp,
    ) {
      Column(
        modifier = Modifier.padding(horizontal = if (compactCard) 12.dp else 16.dp, vertical = if (compactCard) 8.dp else 10.dp),
        verticalArrangement = Arrangement.spacedBy(if (compactCard) 6.dp else 8.dp),
      ) { content() }
    }
  }
}

@Composable
private fun LabeledInputCompactRow(
  label: String,
  labelImageRes: Int? = null,
  value: String,
  onValueChange: (String) -> Unit,
  fontFamily: FontFamily,
  numeric: Boolean = false,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (labelImageRes != null) {
      Box(
        modifier = Modifier
          .width(132.dp)
          .height(24.dp),
        contentAlignment = Alignment.CenterStart,
      ) {
        Image(
          painter = painterResource(labelImageRes),
          contentDescription = label,
          contentScale = ContentScale.Fit,
          modifier = Modifier.fillMaxSize(),
        )
      }
    } else {
      Text(
        text = "$label :",
        modifier = Modifier.width(132.dp),
        color = Ink.copy(alpha = 0.75f),
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Normal,
          fontSize = 14.sp,
        ),
      )
    }
    BasicTextField(
      value = value,
      onValueChange = onValueChange,
      modifier = Modifier.weight(1f),
      singleLine = true,
      cursorBrush = SolidColor(Accent),
      keyboardOptions = KeyboardOptions(
        keyboardType = if (numeric) androidx.compose.ui.text.input.KeyboardType.Number else androidx.compose.ui.text.input.KeyboardType.Text,
        imeAction = ImeAction.Done,
      ),
      textStyle = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        color = Ink,
      ),
    )
  }
}

@Composable
private fun ActionButtonCard(
  title: String,
  color: Color,
  fontFamily: FontFamily,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .shadow(
        elevation = 10.dp,
        shape = CardShape,
        ambientColor = SoftGreyShadow,
        spotColor = SoftGreyShadow,
      )
      .clickable(onClick = onClick),
    shape = CardShape,
    color = color,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 18.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = title,
        color = Color.White,
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.SemiBold,
          fontSize = 18.sp,
        ),
      )
    }
  }
}

@Composable
private fun PaymentGatewayDialog(
  totalAmount: Int,
  clientName: String,
  clientPhone: String,
  clientId: String,
  transactionCode: String,
  fontFamily: FontFamily,
  onDismiss: () -> Unit,
  onOpenCart: () -> Unit,
  onCloseDealAndClearCart: () -> Unit,
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier.fillMaxWidth(0.94f),
      shape = CardShape,
      color = Color(0xFFFDF1E4),
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        AshokvatikaLogo(
          modifier = Modifier.widthIn(max = 260.dp),
          textSize = 28.sp,
          markSize = 58.dp,
        )

        Text(
          text = "Collect payment and confirm the final invoice for this deal.",
          color = Ink.copy(alpha = 0.66f),
          style = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
          ),
        )

        LabeledReadOnlyCard(
          label = "Amount due",
          value = "\u20B9 ${formatAmount(totalAmount)}",
          fontFamily = fontFamily,
          valueFontSize = 28.sp,
        )

        CompactDetailCard(fontFamily = fontFamily) {
          PaymentInfoLine("Client", clientName, fontFamily, labelImageRes = R.drawable.payment_client)
          PaymentInfoLine("Phone", clientPhone, fontFamily, labelImageRes = R.drawable.payment_phone)
          PaymentInfoLine("Client ID", clientId, fontFamily, labelImageRes = R.drawable.payment_client_id)
          PaymentInfoLine("Transaction code", transactionCode, fontFamily, labelImageRes = R.drawable.payment_transaction_id)
        }

        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = CardShape,
          color = Panel,
          border = androidx.compose.foundation.BorderStroke(1.dp, PanelBorder),
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            contentAlignment = Alignment.Center,
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Image(
                painter = painterResource(R.drawable.dummy_qr),
                contentDescription = "Payment QR code",
                modifier = Modifier.size(220.dp),
              )
              Text(
                text = "Scan this code to complete payment before closing the deal.",
                color = Ink.copy(alpha = 0.62f),
                textAlign = TextAlign.Center,
                style = TextStyle(
                  fontFamily = fontFamily,
                  fontWeight = FontWeight.Normal,
                  fontSize = 13.sp,
                  lineHeight = 18.sp,
                ),
              )
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          ActionButtonCard(
            title = "Close Deal and Download Invoice",
            color = AccentGreen,
            fontFamily = fontFamily,
            onClick = onCloseDealAndClearCart,
            modifier = Modifier.weight(1f),
          )
          Surface(
            modifier = Modifier
              .size(64.dp)
              .clickable(onClick = onOpenCart),
            shape = CircleShape,
            color = Color(0xFFF3BF47),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
          ) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Filled.ShoppingCart,
                contentDescription = "Open cart",
                tint = Color.White,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PaymentInfoLine(
  label: String,
  value: String,
  fontFamily: FontFamily,
  labelImageRes: Int? = null,
  labelTint: Color? = null,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.Top,
  ) {
    if (labelImageRes != null) {
      Box(
        modifier = Modifier
          .width(132.dp)
          .height(24.dp),
        contentAlignment = Alignment.CenterStart,
      ) {
        Image(
          painter = painterResource(labelImageRes),
          contentDescription = label,
          contentScale = ContentScale.Fit,
          colorFilter = labelTint?.let { ColorFilter.tint(it) },
          modifier = Modifier.fillMaxSize(),
        )
      }
    } else {
      Text(
        text = "$label :",
        modifier = Modifier.width(132.dp),
        color = Ink.copy(alpha = 0.75f),
        style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
      )
    }
    Text(
      text = value.ifBlank { "-" },
      color = Ink,
      style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp),
    )
  }
}

@Composable
private fun LabeledReadOnlyCard(
  label: String,
  value: String,
  fontFamily: FontFamily,
  modifier: Modifier = Modifier,
  valueFontSize: androidx.compose.ui.unit.TextUnit = 20.sp,
) {
  BoxWithConstraints(modifier = modifier) {
    val compactCard = maxWidth < 220.dp
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = CardShape,
      color = PanelTint,
      border = androidx.compose.foundation.BorderStroke(1.dp, PanelBorder),
      tonalElevation = 0.dp,
      shadowElevation = 2.dp,
    ) {
      Column(
        modifier = Modifier.padding(horizontal = if (compactCard) 14.dp else 18.dp, vertical = if (compactCard) 10.dp else 14.dp),
        verticalArrangement = Arrangement.spacedBy(if (compactCard) 4.dp else 6.dp),
      ) {
        Text(
          text = label,
          color = Ink.copy(alpha = 0.6f),
          style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = if (compactCard) 13.sp else 16.sp),
        )
        Text(
          text = value,
          color = Ink,
          style = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = if (compactCard) (valueFontSize.value * 0.72f).sp else valueFontSize,
            lineHeight = if (compactCard) (valueFontSize.value * 0.8f).sp else valueFontSize * 1.1f,
          ),
        )
      }
    }
  }
}

@Composable
private fun EmptyStateCard(
  text: String,
  fontFamily: FontFamily,
) {
  Surface(
    shape = CardShape,
    color = Panel,
    border = androidx.compose.foundation.BorderStroke(1.dp, PanelBorder),
    tonalElevation = 0.dp,
    shadowElevation = 2.dp,
  ) {
    Text(
      text = text,
      modifier = Modifier.padding(horizontal = 20.dp, vertical = 28.dp),
      color = Ink.copy(alpha = 0.78f),
      textAlign = TextAlign.Center,
      style = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 22.sp,
      ),
    )
  }
}

@Composable
private fun OpenCartDealRow(
  row: OpenCartRow,
  clientName: String,
  transactionCode: String,
  fontFamily: FontFamily,
  onEdit: () -> Unit,
  onCalculator: () -> Unit,
  onItemTap: (CartProductItem) -> Unit,
  onLongPress: () -> Unit,
) {
  val linkedTitle = if ((row.title.isBlank() || row.title.startsWith("Product ")) && clientName.isNotBlank()) {
    clientName
  } else {
    row.title
  }
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .pointerInput(row.id) {
        detectTapGestures(onLongPress = { onLongPress() })
      }
      .shadow(
        elevation = 8.dp,
        shape = CardShape,
        ambientColor = SoftGreyShadow,
        spotColor = SoftGreyShadow,
      ),
    shape = CardShape,
    color = Panel,
    border = androidx.compose.foundation.BorderStroke(1.dp, PanelBorder),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = linkedTitle.ifBlank { if (clientName.isNotBlank()) clientName else "Product title" },
            color = Ink,
            style = TextStyle(
              fontFamily = fontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = 22.sp,
            ),
          )
          if (transactionCode.isNotBlank()) {
            Text(
              text = transactionCode,
              color = Accent.copy(alpha = 0.82f),
              style = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
              ),
            )
          }
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
            modifier = Modifier.clickable(onClick = onEdit),
            shape = CardShape,
            color = Accent.copy(alpha = 0.08f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Accent.copy(alpha = 0.28f)),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
          ) {
            Box(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              contentAlignment = Alignment.Center,
            ) {
              Image(
                painter = painterResource(R.drawable.open_cart_edit),
                contentDescription = "Edit row",
                modifier = Modifier.size(28.dp),
              )
            }
          }
          Surface(
            modifier = Modifier.clickable(onClick = onCalculator),
            shape = CardShape,
            color = Accent.copy(alpha = 0.08f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Accent.copy(alpha = 0.28f)),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
          ) {
            Box(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              contentAlignment = Alignment.Center,
            ) {
              Image(
                painter = painterResource(R.drawable.icon_calculator_money),
                contentDescription = "Open checkout calculator",
                modifier = Modifier.size(32.dp),
              )
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
      ) {
        DealAmountSection(
          amount = row.displayAmount(),
          fontFamily = fontFamily,
          modifier = Modifier.width(128.dp),
        )

        Row(
          modifier = Modifier
            .weight(1f)
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          row.items.forEach { item ->
            CartItemThumbnail(
              item = item,
              fontFamily = fontFamily,
              onClick = { onItemTap(item) },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun DealAmountSection(
  amount: Int,
  fontFamily: FontFamily,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = CardShape,
    color = Accent.copy(alpha = 0.12f),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 18.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        text = "₹ ${formatAmount(amount)}",
        color = Ink,
        style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp, lineHeight = 26.sp),
      )
    }
  }
}

@Composable
private fun CheckoutAdjustmentsDialog(
  row: OpenCartRow,
  fontFamily: FontFamily,
  onDismiss: () -> Unit,
  onApply: (Int) -> Unit,
) {
  var quantity by remember(row.id, row.customAmount) { mutableStateOf("1") }
  var discount by remember(row.id, row.customAmount) { mutableStateOf("0") }
  var discountPercent by remember(row.id, row.customAmount) { mutableStateOf("0") }
  var activeField by remember(row.id) { mutableStateOf<CheckoutPopupField?>(null) }
  var discountMode by remember(row.id) { mutableStateOf(CheckoutPopupField.Discount) }

  val baseAmount = row.baseAmount().toDouble()
  val baseLanding = row.items.sumOf { it.landingPrice * it.quantity }.toDouble()
  val quantityValue = parseDecimalInput(quantity).coerceAtLeast(0.0)
  val priceTotal = baseAmount * quantityValue
  val landingTotal = baseLanding * quantityValue
  val discountValue = parseDecimalInput(discount)
  val discountPercentValue = parseDecimalInput(discountPercent)

  LaunchedEffect(quantityValue, discount, discountMode, activeField, priceTotal) {
    if (discountMode == CheckoutPopupField.Discount && activeField != CheckoutPopupField.Percent) {
      val nextPercent = if (priceTotal == 0.0) 0.0 else (discountValue / priceTotal) * 100.0
      val formatted = formatEditableDecimal(nextPercent)
      if (discountPercent != formatted) {
        discountPercent = formatted
      }
    }
  }

  LaunchedEffect(quantityValue, discountPercent, discountMode, activeField, priceTotal) {
    if (discountMode == CheckoutPopupField.Percent && activeField != CheckoutPopupField.Discount) {
      val nextDiscount = (priceTotal * discountPercentValue) / 100.0
      val formatted = formatEditableDecimal(nextDiscount)
      if (discount != formatted) {
        discount = formatted
      }
    }
  }

  val appliedDiscount = if (discountMode == CheckoutPopupField.Percent) {
    (priceTotal * discountPercentValue) / 100.0
  } else {
    discountValue
  }
  val finalAmount = (priceTotal - appliedDiscount).coerceAtLeast(0.0)
  val profitLoss = finalAmount - landingTotal
  val mrpPerUnit = if (quantityValue > 0.0) priceTotal / quantityValue else 0.0

  fun activateField(field: CheckoutPopupField) {
    activeField = field
    when (field) {
      CheckoutPopupField.Quantity -> quantity = ""
      CheckoutPopupField.Discount -> {
        discountMode = CheckoutPopupField.Discount
        discount = ""
      }
      CheckoutPopupField.Percent -> {
        discountMode = CheckoutPopupField.Percent
        discountPercent = ""
      }
    }
  }

  fun updateActiveField(transform: (String) -> String) {
    when (activeField) {
      CheckoutPopupField.Quantity -> quantity = transform(quantity)
      CheckoutPopupField.Discount -> {
        discountMode = CheckoutPopupField.Discount
        discount = transform(discount)
      }
      CheckoutPopupField.Percent -> {
        discountMode = CheckoutPopupField.Percent
        discountPercent = transform(discountPercent)
      }
      null -> Unit
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier.fillMaxWidth(0.94f),
      shape = CardShape,
      color = Bg,
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
    ) {
      Column(
        modifier = Modifier
          .padding(horizontal = 18.dp, vertical = 18.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = row.title.ifBlank { "Checkout" },
              color = AccentGreen,
              style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp),
            )
          }
          CircleIconAction(
            size = 42.dp,
            icon = {
              Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close checkout popup",
                tint = Accent,
              )
            },
            onClick = onDismiss,
          )
        }

        CheckoutGrid(
          finalPrice = finalAmount,
          beforeMarkup = priceTotal,
          landingPrice = landingTotal,
          quantityValue = quantityValue,
          discountedUnitPrice = mrpPerUnit,
          discountTotal = appliedDiscount,
          priceTotal = finalAmount,
          profitLoss = profitLoss,
          discountPercent = if (priceTotal == 0.0) 0.0 else (appliedDiscount / priceTotal) * 100.0,
          fontFamily = fontFamily,
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          CheckoutPopupValueButton(
            label = "Q",
            value = quantity,
            fontFamily = fontFamily,
            modifier = Modifier.weight(1f),
            active = activeField == CheckoutPopupField.Quantity,
            onClick = { activateField(CheckoutPopupField.Quantity) },
          )
          CheckoutPopupValueButton(
            label = "D",
            value = discount,
            fontFamily = fontFamily,
            modifier = Modifier.weight(1f),
            prefix = "\u20B9",
            active = activeField == CheckoutPopupField.Discount,
            onClick = { activateField(CheckoutPopupField.Discount) },
          )
          CheckoutPopupValueButton(
            label = "%",
            value = discountPercent,
            fontFamily = fontFamily,
            modifier = Modifier.weight(1f),
            suffix = "%",
            active = activeField == CheckoutPopupField.Percent,
            onClick = { activateField(CheckoutPopupField.Percent) },
          )
        }

        if (activeField != null) {
          NumericKeypad(
            modifier = Modifier
              .fillMaxWidth()
              .clip(CardShape),
            onDigit = { digit ->
              updateActiveField { current ->
                sanitizeDecimalEntry(current + digit.toString(), allowNegative = activeField != CheckoutPopupField.Quantity)
              }
            },
            onDecimal = {
              updateActiveField { current ->
                sanitizeDecimalEntry(
                  if (current.isBlank() || current == "-") "${current}0." else "$current.",
                  allowNegative = activeField != CheckoutPopupField.Quantity,
                )
              }
            },
            onSignToggle = if (activeField == CheckoutPopupField.Discount || activeField == CheckoutPopupField.Percent) {
              {
                updateActiveField { current ->
                  if (current.startsWith("-")) current.removePrefix("-") else "-$current"
                }
              }
            } else {
              null
            },
            onBackspace = {
              updateActiveField { current ->
                current.dropLast(1)
              }
            },
            onConfirm = { activeField = null },
          )
        }

        ActionButtonCard(
          title = "Apply Amount",
          color = AccentGreen,
          fontFamily = fontFamily,
          onClick = { onApply(finalAmount.toInt()) },
        )
      }
    }
  }
}

private enum class CheckoutPopupField {
  Quantity,
  Discount,
  Percent,
}

@Composable
private fun CheckoutPopupValueButton(
  label: String,
  value: String,
  fontFamily: FontFamily,
  modifier: Modifier = Modifier,
  prefix: String = "",
  suffix: String = "",
  active: Boolean = false,
  onClick: () -> Unit,
) {
  Surface(
    modifier = modifier.clickable(onClick = onClick),
    shape = CardShape,
    color = Accent,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = label,
        color = Color.White.copy(alpha = 0.86f),
        style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp),
      )
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (prefix.isNotBlank()) {
          Text(prefix, color = Color.White, style = TextStyle(fontFamily = fontFamily, fontSize = 18.sp))
          Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
          text = if (value.isBlank()) " " else value,
          modifier = Modifier.weight(1f),
          color = Color.White,
          style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 20.sp),
        )
        if (suffix.isNotBlank()) {
          Spacer(modifier = Modifier.width(6.dp))
          Text(suffix, color = Color.White, style = TextStyle(fontFamily = fontFamily, fontSize = 18.sp))
        }
      }
      if (active) {
        Spacer(
          modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(Color.White.copy(alpha = 0.9f), CardShape),
        )
      }
    }
  }
}

@Composable
fun CheckoutScreen(
  row: OpenCartRow,
  cartCount: Int,
  onBack: () -> Unit,
  onOpenCart: () -> Unit,
  onAmountChange: (Int) -> Unit,
) {
  val fontFamily = rememberDashboardFont()
  var quantity by rememberSaveable(row.id) { mutableStateOf(row.items.sumOf { it.quantity }.coerceAtLeast(1).toString()) }
  var discount by rememberSaveable(row.id) { mutableStateOf("0") }
  var discountPercent by rememberSaveable(row.id) { mutableStateOf("0") }

  val baseAmount = row.baseAmount()
  val baseLanding = row.items.sumOf { it.landingPrice * it.quantity }
  val quantityValue = quantity.toIntOrNull()?.coerceAtLeast(1) ?: 1
  val priceTotal = baseAmount * quantityValue
  val landingTotal = baseLanding * quantityValue
  val discountValue = discount.toIntOrNull()?.coerceAtLeast(0) ?: 0
  val discountPercentValue = discountPercent.toIntOrNull()?.coerceIn(0, 100) ?: 0
  val discountByPercent = ((priceTotal * discountPercentValue) / 100f).toInt()
  val appliedDiscount = maxOf(discountValue, discountByPercent)
  val finalAmount = (priceTotal - appliedDiscount).coerceAtLeast(0)

  LaunchedEffect(finalAmount, row.id) {
    onAmountChange(finalAmount)
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Bg)
      .navigationBarsPadding(),
  ) {
    val contentWidth = minOf(maxWidth - 32.dp, 980.dp)

    Column(
      modifier = Modifier
        .width(contentWidth)
        .fillMaxSize()
        .align(Alignment.TopCenter)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      TopBrandBar(
        textSize = 34.sp,
        markSize = 74.dp,
        onBack = onBack,
        alignStart = true,
        logoMaxWidth = 280.dp,
        actionButtonSize = LockedHeaderActionSize,
        actionIconSize = LockedHeaderIconSize,
        actionSpacing = LockedHeaderActionSpacing,
        logoTopOffset = LockedHeaderLogoTopOffset,
        middleAction = {
          CircleIconAction(
            size = LockedHeaderActionSize,
            icon = {
              if (cartCount > 0) {
                Text(
                  text = cartCount.toString(),
                  color = Accent,
                  style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 18.sp),
                )
              } else {
                Icon(
                  imageVector = Icons.Filled.ShoppingCart,
                  contentDescription = "Open cart",
                  tint = Accent,
                  modifier = Modifier.size(LockedHeaderIconSize),
                )
              }
            },
            onClick = onOpenCart,
          )
        },
      )

      Text(
        text = row.title.ifBlank { "Checkout" },
        color = AccentGreen,
        style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 26.sp),
      )

        CheckoutGrid(
          finalPrice = finalAmount.toDouble(),
          beforeMarkup = priceTotal.toDouble(),
          landingPrice = landingTotal.toDouble(),
          quantityValue = quantityValue.toDouble(),
          discountedUnitPrice = if (quantityValue > 0) finalAmount.toDouble() / quantityValue.toDouble() else finalAmount.toDouble(),
          discountTotal = appliedDiscount.toDouble(),
          priceTotal = finalAmount.toDouble(),
          profitLoss = (finalAmount - landingTotal).toDouble(),
          discountPercent = discountPercentValue.toDouble(),
          fontFamily = fontFamily,
        )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        PopupValueEditor(
          label = "Q",
          value = quantity,
          onValueChange = { quantity = it.filter(Char::isDigit).ifBlank { "1" } },
          fontFamily = fontFamily,
          modifier = Modifier.weight(1f),
        )
        PopupValueEditor(
          label = "D",
          value = discount,
          onValueChange = {
            discount = it.filter(Char::isDigit).ifBlank { "0" }
            val raw = discount.toIntOrNull() ?: 0
            discountPercent = if (priceTotal > 0) ((raw * 100f) / priceTotal).toInt().coerceIn(0, 100).toString() else "0"
          },
          fontFamily = fontFamily,
          modifier = Modifier.weight(1f),
          prefix = "\u20B9",
        )
        PopupValueEditor(
          label = "%",
          value = discountPercent,
          onValueChange = {
            discountPercent = (it.filter(Char::isDigit).toIntOrNull() ?: 0).coerceIn(0, 100).toString()
            val raw = discountPercent.toIntOrNull() ?: 0
            discount = ((priceTotal * raw) / 100f).toInt().toString()
          },
          fontFamily = fontFamily,
          modifier = Modifier.weight(1f),
          suffix = "%",
        )
      }
    }
  }
}

@Composable
private fun CartItemThumbnail(
  item: CartProductItem,
  fontFamily: FontFamily,
  onClick: () -> Unit,
) {
  val imageBitmap by rememberDecodedImage(item.imageUri)
  Column(
    modifier = Modifier.width(84.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Box(
      modifier = Modifier
        .size(68.dp)
        .background(Accent, CircleShape)
        .clip(CircleShape)
        .clickable(onClick = onClick),
      contentAlignment = Alignment.Center,
    ) {
      if (imageBitmap != null) {
        Image(
          bitmap = imageBitmap!!,
          contentDescription = item.name,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
      } else {
        Text(
          text = item.shortName,
          color = Color.White,
          textAlign = TextAlign.Center,
          style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Thin, fontSize = 14.sp),
        )
      }
    }
    Text(
      text = item.name,
      color = Ink,
      textAlign = TextAlign.Center,
      maxLines = 2,
      style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 13.sp),
    )
    Text(
      text = "₹ ${formatAmount(item.mrp)}",
      color = Ink,
      textAlign = TextAlign.Center,
      style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    )
    Text(
      text = "Qty ${item.quantity}",
      color = Ink.copy(alpha = 0.62f),
      textAlign = TextAlign.Center,
      style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp),
    )
  }
}

@Composable
private fun CartItemEditorDialog(
  item: CartProductItem,
  fontFamily: FontFamily,
  onDismiss: () -> Unit,
  onQuantityChange: (Int) -> Unit,
  onDelete: () -> Unit,
) {
  var quantity by remember(item.id, item.quantity) { mutableStateOf(item.quantity.toString()) }
  val resolvedQuantity = quantity.toIntOrNull()?.coerceIn(1, 99) ?: 1

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier.fillMaxWidth(0.92f),
      shape = CardShape,
      color = Color.White,
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
    ) {
      Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
        CircleIconAction(
          modifier = Modifier.align(Alignment.TopEnd),
          size = 42.dp,
          icon = {
            Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = "Delete item",
              tint = Accent,
            )
          },
          onClick = onDelete,
        )

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Text(
            text = item.name,
            color = Ink,
            style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp),
          )
          Text(
            text = "Delete Item",
            color = Accent,
            style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              QuantityButton(
                text = "-",
                onClick = {
                  val next = (resolvedQuantity - 1).coerceAtLeast(1)
                  quantity = next.toString()
                },
                fontFamily = fontFamily,
              )
              BasicTextField(
                value = quantity,
                onValueChange = {
                  val clean = it.filter(Char::isDigit)
                  quantity = clean.ifBlank { "1" }
                },
                modifier = Modifier.width(48.dp),
                singleLine = true,
                cursorBrush = SolidColor(Accent),
                textStyle = TextStyle(
                  fontFamily = fontFamily,
                  fontWeight = FontWeight.Normal,
                  fontSize = 26.sp,
                  textAlign = TextAlign.Center,
                  color = Ink,
                ),
              )
              QuantityButton(
                text = "+",
                onClick = {
                  val next = (resolvedQuantity + 1).coerceAtMost(99)
                  quantity = next.toString()
                },
                fontFamily = fontFamily,
              )
            }

            CircleIconAction(
              size = 42.dp,
              icon = {
                Icon(
                  imageVector = Icons.Filled.Check,
                  contentDescription = "Confirm",
                  tint = AccentGreen,
                )
              },
              onClick = {
                onQuantityChange(resolvedQuantity)
                onDismiss()
              },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SavedDealCard(
  deal: SavedDeal,
  fontFamily: FontFamily,
  onClearCart: () -> Unit,
  onDealInfoChange: (String, String, String, String, String) -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val displayTitle = deal.rows.firstOrNull()?.title?.takeIf { it.isNotBlank() } ?: deal.title
  var showDealCart by remember(deal.id) { mutableStateOf(false) }
  var pendingInvoiceExport by remember(deal.id) { mutableStateOf<PendingInvoiceExport?>(null) }
  val invoiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
    val export = pendingInvoiceExport
    pendingInvoiceExport = null
    if (export == null) {
      return@rememberLauncherForActivityResult
    }
    scope.launch {
      val saved = if (uri == null) {
        false
      } else {
        withContext(Dispatchers.IO) {
          runCatching {
            context.contentResolver.openOutputStream(uri, "rwt")?.use { stream ->
              stream.write(export.pdfBytes)
              stream.flush()
            } != null
          }.getOrDefault(false)
        }
      }
      Toast.makeText(
        context,
        when {
          uri == null -> "Invoice download was skipped."
          saved -> "Invoice downloaded successfully."
          else -> "Invoice download failed."
        },
        Toast.LENGTH_SHORT,
      ).show()
    }
  }
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = CardShape,
    color = Color.White,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = displayTitle,
          modifier = Modifier.weight(1f),
          color = Ink,
          style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 22.sp),
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
      ) {
        Column(
          modifier = Modifier.width(124.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          DealAmountSection(
            amount = deal.rows.sumOf { row -> row.displayAmount() },
            fontFamily = fontFamily,
            modifier = Modifier.fillMaxWidth(),
          )
          Surface(
            modifier = Modifier.clickable(onClick = { showDealCart = true }),
            shape = CardShape,
            color = Accent.copy(alpha = 0.08f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Accent.copy(alpha = 0.28f)),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
          ) {
            Box(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              contentAlignment = Alignment.Center,
            ) {
              Image(
                painter = painterResource(R.drawable.icon_add_to_cart_wheel),
                contentDescription = "Open saved deal cart",
                modifier = Modifier.size(28.dp),
              )
            }
          }
        }

        SavedDealClientSection(
          deal = deal,
          fontFamily = fontFamily,
          modifier = Modifier.width(214.dp),
          onDealInfoChange = onDealInfoChange,
        )

        Row(
          modifier = Modifier
            .weight(1f)
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          deal.rows.flatMap { it.items }.forEach { item ->
            CartItemThumbnail(
              item = item,
              fontFamily = fontFamily,
              onClick = {},
            )
          }
        }
      }
    }
  }

  if (showDealCart) {
    ClosedDealCartDialog(
      deal = deal,
      fontFamily = fontFamily,
      onDismiss = { showDealCart = false },
      onDownloadInvoice = {
        runCatching {
          PendingInvoiceExport(
            fileName = buildInvoiceFileName(deal),
            pdfBytes = buildInvoicePdf(deal),
          )
        }.onSuccess { export ->
          pendingInvoiceExport = export
          invoiceLauncher.launch(export.fileName)
        }.onFailure {
          Toast.makeText(context, "Invoice generation failed.", Toast.LENGTH_SHORT).show()
        }
      },
      onClearCart = {
        onClearCart()
        showDealCart = false
      },
    )
  }
}

@Composable
private fun ClosedDealCartDialog(
  deal: SavedDeal,
  fontFamily: FontFamily,
  onDismiss: () -> Unit,
  onDownloadInvoice: () -> Unit,
  onClearCart: () -> Unit,
) {
  val scrollState = rememberScrollState()
  val totalAmount = deal.rows.sumOf { row -> row.displayAmount() }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier.fillMaxWidth(0.96f),
      shape = CardShape,
      color = Bg,
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Cart",
            color = AccentGreen,
            style = TextStyle(
              fontFamily = fontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = 24.sp,
            ),
          )
          CircleIconAction(
            size = 38.dp,
            icon = {
              Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close cart preview",
                tint = Accent,
              )
            },
            onClick = onDismiss,
          )
        }

        CompactDetailCard(fontFamily = fontFamily) {
          PaymentInfoLine("Client", deal.clientName, fontFamily, labelImageRes = R.drawable.closed_deal_user, labelTint = Accent)
          PaymentInfoLine("Phone", deal.clientPhone, fontFamily, labelImageRes = R.drawable.closed_deal_phone, labelTint = Accent)
          PaymentInfoLine("Client ID", deal.clientId, fontFamily, labelImageRes = R.drawable.closed_deal_client_id, labelTint = Accent)
          PaymentInfoLine("Transaction code", deal.transactionCode, fontFamily, labelImageRes = R.drawable.closed_deal_transaction_id, labelTint = Accent)
        }

        LabeledReadOnlyCard(
          label = "",
          value = "\u20B9 ${formatAmount(totalAmount)}",
          fontFamily = fontFamily,
          valueFontSize = 28.sp,
        )

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            .verticalScroll(scrollState),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          deal.rows.forEach { row ->
            ClosedDealRowCard(
              row = row,
              clientName = deal.clientName,
              transactionCode = deal.transactionCode,
              fontFamily = fontFamily,
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          ActionButtonCard(
            title = "Download invoice",
            color = Accent,
            fontFamily = fontFamily,
            onClick = onDownloadInvoice,
            modifier = Modifier.weight(1f),
          )
          ActionButtonCard(
            title = "Close deals",
            color = AccentGreen,
            fontFamily = fontFamily,
            onClick = onClearCart,
            modifier = Modifier.weight(1f),
          )
        }
      }
    }
  }
}

@Composable
private fun ClosedDealRowCard(
  row: OpenCartRow,
  clientName: String,
  transactionCode: String,
  fontFamily: FontFamily,
) {
  val linkedTitle = if ((row.title.isBlank() || row.title.startsWith("Product ")) && clientName.isNotBlank()) {
    clientName
  } else {
    row.title
  }

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(
        elevation = 8.dp,
        shape = CardShape,
        ambientColor = SoftGreyShadow,
        spotColor = SoftGreyShadow,
      ),
    shape = CardShape,
    color = Color.White,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = linkedTitle.ifBlank { "Product title" },
          color = Ink,
          style = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
          ),
        )
        if (transactionCode.isNotBlank()) {
          Text(
            text = transactionCode,
            color = Accent.copy(alpha = 0.82f),
            style = TextStyle(
              fontFamily = fontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = 13.sp,
            ),
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
      ) {
        DealAmountSection(
          amount = row.displayAmount(),
          fontFamily = fontFamily,
          modifier = Modifier.width(128.dp),
        )

        Row(
          modifier = Modifier
            .weight(1f)
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          row.items.forEach { item ->
            CartItemThumbnail(
              item = item,
              fontFamily = fontFamily,
              onClick = {},
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SavedDealClientSection(
  deal: SavedDeal,
  fontFamily: FontFamily,
  modifier: Modifier = Modifier,
  onDealInfoChange: (String, String, String, String, String) -> Unit,
) {
  Surface(
    modifier = modifier,
    shape = CardShape,
    color = Accent.copy(alpha = 0.08f),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      SavedDealClientField(
        label = "Client",
        labelImageRes = R.drawable.closed_deal_user,
        value = deal.clientName,
        placeholder = "Client name",
        fontFamily = fontFamily,
        onValueChange = { onDealInfoChange(deal.id, it, deal.clientPhone, deal.clientId, deal.transactionCode) },
      )
      SavedDealClientField(
        label = "Phone",
        labelImageRes = R.drawable.closed_deal_phone,
        value = deal.clientPhone,
        placeholder = "Phone number",
        fontFamily = fontFamily,
        onValueChange = { onDealInfoChange(deal.id, deal.clientName, it.filter(Char::isDigit), deal.clientId, deal.transactionCode) },
      )
      SavedDealClientField(
        label = "Client ID",
        labelImageRes = R.drawable.closed_deal_client_id,
        value = deal.clientId,
        placeholder = "Client ID",
        fontFamily = fontFamily,
        onValueChange = { onDealInfoChange(deal.id, deal.clientName, deal.clientPhone, it, deal.transactionCode) },
      )
      SavedDealClientField(
        label = "Transaction code",
        labelImageRes = R.drawable.closed_deal_transaction_id,
        value = deal.transactionCode,
        placeholder = "Transaction Code",
        fontFamily = fontFamily,
        onValueChange = { onDealInfoChange(deal.id, deal.clientName, deal.clientPhone, deal.clientId, it) },
      )
    }
  }
}

@Composable
private fun SavedDealClientField(
  label: String,
  labelImageRes: Int,
  value: String,
  placeholder: String,
  fontFamily: FontFamily,
  onValueChange: (String) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .width(28.dp)
        .height(28.dp),
      contentAlignment = Alignment.Center,
    ) {
      Image(
        painter = painterResource(labelImageRes),
        contentDescription = label,
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(Accent),
        modifier = Modifier.fillMaxSize(),
      )
    }
    InlineEditableText(
      value = value,
      placeholder = placeholder,
      fontFamily = fontFamily,
      onValueChange = onValueChange,
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun InlineEditableText(
  value: String,
  placeholder: String,
  fontFamily: FontFamily,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  BasicTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    singleLine = true,
    cursorBrush = SolidColor(Accent),
    textStyle = TextStyle(
      fontFamily = fontFamily,
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      color = Ink,
    ),
    decorationBox = { innerTextField ->
      if (value.isBlank()) {
        Text(
          text = placeholder,
          color = Ink.copy(alpha = 0.38f),
          style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
        )
      }
      innerTextField()
    },
  )
}

@Composable
private fun PriceBreakdownPopup(
  landingPrice: String,
  onLandingPriceChange: (String) -> Unit,
  buildProduct: String,
  onBuildProductClick: () -> Unit,
  valueAddition: String,
  others: String,
  onOthersChange: (String) -> Unit,
  markup: String,
  onMarkupChange: (String) -> Unit,
  selectedBuildItems: List<BuildProductSelection>,
  onEditSelections: () -> Unit,
  beforeMarkup: Int,
  finalPrice: Int,
  showMarkupValidation: Boolean,
  onClose: () -> Unit,
  onConfirm: () -> Unit,
  fontFamily: FontFamily,
  modifier: Modifier = Modifier,
) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Surface(
      modifier = modifier
        .shadow(
          elevation = 18.dp,
          shape = CardShape,
          ambientColor = SoftGreyShadow,
          spotColor = SoftGreyShadow,
        ),
      shape = CardShape,
      color = Color(0xFFFDF1E4),
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Text(
          text = "Price",
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Start,
          color = Ink,
          style = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Thin,
            fontSize = 24.sp,
            lineHeight = 26.sp,
          ),
        )
        Text(
          text = "₹ ${formatAmount(finalPrice)}",
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Center,
          color = Accent.copy(alpha = 0.78f),
          style = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Thin,
            fontSize = 36.sp,
            lineHeight = 40.sp,
          ),
        )

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            text = "Landing price ₹ ${formatAmount(landingPrice.toIntOrNull() ?: 0)}",
            color = Ink.copy(alpha = 0.42f),
            style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Thin, fontSize = 18.sp),
          )
          Text(
            text = "Before markup ₹ ${formatAmount(beforeMarkup)}",
            color = Ink.copy(alpha = 0.42f),
            style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Thin, fontSize = 18.sp),
          )
        }

        PriceInputField(
          label = "Landing Price",
          value = landingPrice,
          onValueChange = onLandingPriceChange,
          fontFamily = fontFamily,
          prefix = "₹",
        )

        BuildProductDropdown(
          selectedValue = buildProduct,
          onClick = onBuildProductClick,
          fontFamily = fontFamily,
        )

        SelectedValueAdditionCard(
          selections = selectedBuildItems,
          onEditClick = onEditSelections,
          fontFamily = fontFamily,
        )

        PriceInputField(
          label = "Value Addition*",
          value = valueAddition,
          onValueChange = {},
          fontFamily = fontFamily,
          readOnly = true,
          prefix = "₹",
        )

        PriceInputField(
          label = "Others",
          value = others,
          onValueChange = onOthersChange,
          fontFamily = fontFamily,
        )

        PriceInputField(
          label = "Markup*",
          value = markup,
          onValueChange = onMarkupChange,
          fontFamily = fontFamily,
          prefix = "\u20B9",
        )

        if (showMarkupValidation) {
          Text(
            text = "Markup is required to save the price.",
            color = Color(0xFFB00020),
            style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
        ) {
          TextButton(onClick = onClose) { Text("Back", color = Ink.copy(alpha = 0.5f)) }
          TextButton(onClick = onConfirm) { Text("Save", color = Accent) }
        }
      }
    }
  }
}

@Composable
private fun CheckoutSummaryPopup(
  quantity: String,
  onQuantityChange: (String) -> Unit,
  discount: String,
  onDiscountChange: (String) -> Unit,
  discountPercent: String,
  onDiscountPercentChange: (String) -> Unit,
  finalPrice: Int,
  beforeMarkup: Int,
  landingPrice: Int,
  quantityValue: Int,
  discountedUnitPrice: Int,
  discountTotal: Int,
  priceTotal: Int,
  profitLoss: Int,
  onClose: () -> Unit,
  fontFamily: FontFamily,
  modifier: Modifier = Modifier,
) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Surface(
      modifier = modifier
        .shadow(
          elevation = 18.dp,
          shape = CardShape,
          ambientColor = SoftGreyShadow,
          spotColor = SoftGreyShadow,
        ),
      shape = CardShape,
      color = Color(0xFFFDF1E4),
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 22.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          AshokvatikaLogo(
            modifier = Modifier.width(320.dp),
            textSize = 26.sp,
            markSize = 52.dp,
          )
          CircleIconAction(
            size = 42.dp,
            icon = {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Ink.copy(alpha = 0.55f),
              )
            },
            onClick = onClose,
          )
        }

        CheckoutGrid(
          finalPrice = finalPrice.toDouble(),
          beforeMarkup = beforeMarkup.toDouble(),
          landingPrice = landingPrice.toDouble(),
          quantityValue = quantityValue.toDouble(),
          discountedUnitPrice = discountedUnitPrice.toDouble(),
          discountTotal = discountTotal.toDouble(),
          priceTotal = priceTotal.toDouble(),
          profitLoss = profitLoss.toDouble(),
          discountPercent = discountPercent.toDoubleOrNull() ?: 0.0,
          fontFamily = fontFamily,
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          PopupValueEditor(
            label = "Q",
            value = quantity,
            onValueChange = onQuantityChange,
            fontFamily = fontFamily,
            modifier = Modifier.weight(1f),
          )
          PopupValueEditor(
            label = "D",
            value = discount,
            onValueChange = onDiscountChange,
            fontFamily = fontFamily,
            modifier = Modifier.weight(1f),
            prefix = "₹",
          )
          PopupValueEditor(
            label = "%",
            value = discountPercent,
            onValueChange = onDiscountPercentChange,
            fontFamily = fontFamily,
            modifier = Modifier.weight(1f),
            suffix = "%",
          )
        }
      }
    }
  }
}

@Composable
private fun MediaGalleryPopup(
  mediaUris: List<String>,
  productName: String,
  primaryTitle: String,
  onPrimaryTitleChange: (String) -> Unit,
  primaryDescription: String,
  onPrimaryDescriptionChange: (String) -> Unit,
  secondaryTitle: String,
  onSecondaryTitleChange: (String) -> Unit,
  secondaryDescription: String,
  onSecondaryDescriptionChange: (String) -> Unit,
  contactText: String,
  onContactTextChange: (String) -> Unit,
  fontFamily: FontFamily,
  onAddFromDrive: () -> Unit,
  onAddBrochureImage: (Int) -> Unit,
  onClose: () -> Unit,
) {
  var activeTab by rememberSaveable { mutableStateOf(MediaPopupTab.GALLERY) }
  val configuration = LocalConfiguration.current
  val tabletLayout = configuration.smallestScreenWidthDp >= 600
  Dialog(onDismissRequest = onClose) {
    Surface(
      modifier = Modifier.fillMaxWidth(if (tabletLayout) 0.82f else 0.96f),
      shape = CardShape,
      color = Bg,
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
    ) {
      Column(
        modifier = Modifier
          .padding(
            horizontal = if (tabletLayout) 24.dp else 18.dp,
            vertical = if (tabletLayout) 22.dp else 18.dp,
          )
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(if (tabletLayout) 20.dp else 16.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Media",
            color = AccentGreen,
            style = TextStyle(
              fontFamily = fontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = if (tabletLayout) 28.sp else 24.sp,
            ),
          )
          CircleIconAction(
            size = if (tabletLayout) 42.dp else 38.dp,
            icon = {
              Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close media gallery",
                tint = Accent,
              )
            },
            onClick = onClose,
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(if (tabletLayout) 12.dp else 10.dp),
        ) {
          MediaPopupTabButton(
            title = "Gallery",
            selected = activeTab == MediaPopupTab.GALLERY,
            onClick = { activeTab = MediaPopupTab.GALLERY },
            modifier = Modifier.weight(1f),
          )
          MediaPopupTabButton(
            title = "Broucher",
            selected = activeTab == MediaPopupTab.BROUCHER,
            onClick = { activeTab = MediaPopupTab.BROUCHER },
            modifier = Modifier.weight(1f),
          )
          MediaPopupTabButton(
            title = "Broucher2",
            selected = activeTab == MediaPopupTab.BROUCHER2,
            onClick = { activeTab = MediaPopupTab.BROUCHER2 },
            modifier = Modifier.weight(1f),
          )
        }

        if (activeTab == MediaPopupTab.GALLERY) {
          GalleryScreen(
            mediaUris = mediaUris,
            onClose = onClose,
            onAddFromDrive = onAddFromDrive,
            tabletLayout = tabletLayout,
          )
        } else if (activeTab == MediaPopupTab.BROUCHER) {
          BrochureScreen(
            mediaUris = mediaUris,
            productName = productName,
            onProductNameChange = {},
            description = primaryDescription,
            onDescriptionChange = onPrimaryDescriptionChange,
            contactText = contactText,
            onAddImageFromDrive = { onAddBrochureImage(0) },
            onAddImageFromCamera = null,
            fontFamily = fontFamily,
          )
        } else {
          BrochureTwoScreen(
            mediaUris = mediaUris,
            productName = productName,
            onProductNameChange = {},
            description = primaryDescription,
            onDescriptionChange = {},
            contactText = contactText,
            onAddImage = onAddBrochureImage,
            fontFamily = fontFamily,
            showBottomGrid = true,
          )
        }
      }
    }
  }
}

@Composable
private fun GalleryScreen(
  mediaUris: List<String>,
  onClose: () -> Unit,
  onAddFromDrive: () -> Unit,
  tabletLayout: Boolean = false,
) {
  val context = LocalContext.current
  val galleryImages = remember(mediaUris) {
    mediaUris.mapIndexed { index, uri ->
      GalleryImageItem(
        id = "gallery-$index-$uri",
        uriString = uri,
      )
    }
  }
  var selectedImages by rememberSaveable(mediaUris) { mutableStateOf(emptyList<String>()) }
  var isSheetOpen by rememberSaveable(mediaUris) { mutableStateOf(false) }
  val variant = remember { AppExperimentManager.galleryShareSheetVariant() }
  val sharePlatforms = remember {
    listOf(
      SharePlatformAction("share", "Share", Icons.Filled.Share),
      SharePlatformAction("whatsapp", "WhatsApp", Icons.AutoMirrored.Filled.Chat, "com.whatsapp"),
      SharePlatformAction("instagram", "Instagram", Icons.Filled.PhotoCamera, "com.instagram.android"),
      SharePlatformAction("messages", "Messages", Icons.AutoMirrored.Filled.Send, "com.google.android.apps.messaging"),
      SharePlatformAction("more", "More", Icons.Filled.MoreHoriz),
    )
  }

  LaunchedEffect(variant) {
    AppExperimentManager.logGalleryVariantExposure(variant)
  }

  fun toggleSelect(image: GalleryImageItem) {
    val nextState = applyGallerySelectionToggle(
      currentSelection = selectedImages,
      wasSheetOpen = isSheetOpen,
      uriString = image.uriString,
      variant = variant,
    )
    selectedImages = nextState.selectedImages
    isSheetOpen = nextState.isSheetOpen
  }

  fun removeSelectedImage(uriString: String) {
    val nextState = applyGallerySelectionRemoval(
      currentSelection = selectedImages,
      uriString = uriString,
    )
    selectedImages = nextState.selectedImages
    isSheetOpen = nextState.isSheetOpen
  }

  fun clearSelection() {
    selectedImages = emptyList()
    isSheetOpen = false
  }

  fun openShareSheet() {
    if (selectedImages.isNotEmpty()) {
      isSheetOpen = true
      AppExperimentManager.logGalleryShareSheetOpened(variant, selectedImages.size)
    }
  }

  fun closeShareSheet() {
    isSheetOpen = false
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(if (tabletLayout) 680.dp else 560.dp)
      .background(Color.White),
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(
            horizontal = if (tabletLayout) 18.dp else 12.dp,
            vertical = if (tabletLayout) 10.dp else 6.dp,
          ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        CircleIconAction(
          size = if (tabletLayout) 42.dp else 38.dp,
          icon = {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Accent,
            )
          },
          onClick = onClose,
        )
        Text(
          text = "Gallery",
          color = Ink,
          style = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = if (tabletLayout) 24.sp else 20.sp,
          ),
        )
        CircleIconAction(
          size = if (tabletLayout) 42.dp else 38.dp,
          icon = {
            Icon(
              imageVector = if (isSheetOpen) Icons.Filled.Close else Icons.Filled.AddToDrive,
              contentDescription = if (isSheetOpen) "Close sheet" else "Add media",
              tint = Accent,
            )
          },
          onClick = {
            if (isSheetOpen) closeShareSheet() else onAddFromDrive()
          },
        )
      }

      if (galleryImages.isEmpty()) {
        EmptyStateCard(
          text = "Add product media to open the modern gallery share sheet.",
          fontFamily = rememberDashboardFont(),
        )
      } else {
        ImageGrid(
          images = galleryImages,
          selectedImageUris = selectedImages,
          onImageTap = ::toggleSelect,
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        )
      }
    }

    if (isSheetOpen) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.24f))
          .clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = ::closeShareSheet,
          ),
      )
    }

    if (variant == GalleryShareSheetVariant.ManualOpen && selectedImages.isNotEmpty() && !isSheetOpen) {
      Surface(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 18.dp)
          .clickable(onClick = ::openShareSheet),
        shape = CircleShape,
        color = Accent,
        tonalElevation = 0.dp,
        shadowElevation = 10.dp,
      ) {
        Text(
          text = "Share ${selectedImages.size} selected",
          modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
          color = Color.White,
          style = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
          ),
        )
      }
    }

    if (selectedImages.isNotEmpty()) {
      BottomShareSheet(
        selectedImages = selectedImages,
        isOpen = isSheetOpen,
        platforms = sharePlatforms,
        onClose = ::closeShareSheet,
        onRemoveImage = ::removeSelectedImage,
        onClearSelection = ::clearSelection,
        onShareSelectedImages = { platform ->
          AppExperimentManager.logGalleryShareAction(variant, platform.id, selectedImages.size)
          shareSelectedImages(context, selectedImages, platform)
        },
        modifier = Modifier.align(Alignment.BottomCenter),
      )
    }
  }
}

@Composable
private fun ImageGrid(
  images: List<GalleryImageItem>,
  selectedImageUris: List<String>,
  onImageTap: (GalleryImageItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyVerticalGrid(
    columns = GridCells.Fixed(3),
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    items(images, key = { it.id }) { image ->
      GalleryImageTile(
        image = image,
        selected = image.uriString in selectedImageUris,
        onTap = { onImageTap(image) },
      )
    }
  }
}

@Composable
private fun GalleryImageTile(
  image: GalleryImageItem,
  selected: Boolean,
  onTap: () -> Unit,
) {
  val bitmap by rememberDecodedImage(image.uriString.takeUnless { isVideoUri(LocalContext.current, it) }, maxDimensionPx = 720)
  Box(
    modifier = Modifier
      .aspectRatio(1f)
      .clip(CardShape)
      .background(Color(0xFFF0F0F0))
      .clickable(onClick = onTap),
  ) {
    if (bitmap != null) {
      Image(
        bitmap = bitmap!!,
        contentDescription = "Gallery image",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
      )
    } else {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.PhotoCamera,
          contentDescription = null,
          tint = Ink.copy(alpha = 0.4f),
        )
      }
    }

    if (selected) {
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(8.dp)
          .size(24.dp)
          .background(Color(0xFF2F80ED), CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.Check,
          contentDescription = "Selected",
          tint = Color.White,
          modifier = Modifier.size(16.dp),
        )
      }
    } else {
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(8.dp)
          .size(24.dp)
          .border(1.dp, Color.White.copy(alpha = 0.85f), CircleShape),
      )
    }
  }
}

@Composable
private fun BottomShareSheet(
  selectedImages: List<String>,
  isOpen: Boolean,
  platforms: List<SharePlatformAction>,
  onClose: () -> Unit,
  onRemoveImage: (String) -> Unit,
  onClearSelection: () -> Unit,
  onShareSelectedImages: (SharePlatformAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  val offsetY by animateDpAsState(
    targetValue = if (isOpen) 0.dp else 260.dp,
    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
    label = "share-sheet-offset",
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .offset(y = offsetY),
    shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    color = Color.White,
    tonalElevation = 0.dp,
    shadowElevation = 14.dp,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Box(
        modifier = Modifier
          .align(Alignment.CenterHorizontally)
          .width(42.dp)
          .height(4.dp)
          .background(Color(0xFFD9D9D9), CircleShape),
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "${selectedImages.size} Selected",
            color = Ink,
            style = TextStyle(
              fontWeight = FontWeight.SemiBold,
              fontSize = 18.sp,
            ),
          )
          Text(
            text = "Swipe up for more options",
            color = Ink.copy(alpha = 0.55f),
            style = TextStyle(
              fontWeight = FontWeight.Normal,
              fontSize = 13.sp,
            ),
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          TextButton(onClick = onClearSelection) {
            Text("Clear", color = Accent)
          }
          CircleIconAction(
            size = 34.dp,
            icon = {
              Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close share sheet",
                tint = Accent,
              )
            },
            onClick = onClose,
          )
        }
      }

      LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(selectedImages, key = { it }) { uriString ->
          SelectedImagePreview(
            uriString = uriString,
            onRemove = { onRemoveImage(uriString) },
          )
        }
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
      ) {
        platforms.forEach { platform ->
          ShareActionButton(
            action = platform,
            onClick = { onShareSelectedImages(platform) },
          )
        }
      }
    }
  }
}

@Composable
private fun SelectedImagePreview(
  uriString: String,
  onRemove: () -> Unit,
) {
  val bitmap by rememberDecodedImage(uriString.takeUnless { isVideoUri(LocalContext.current, it) }, maxDimensionPx = 280)
  Box(
    modifier = Modifier
      .size(64.dp)
      .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
      .background(Color(0xFFF4F4F4)),
  ) {
    if (bitmap != null) {
      Image(
        bitmap = bitmap!!,
        contentDescription = "Selected preview",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
      )
    }
    Box(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(4.dp)
        .size(18.dp)
        .background(Color.Black.copy(alpha = 0.58f), CircleShape)
        .clickable(onClick = onRemove),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Filled.Close,
        contentDescription = "Remove selected image",
        tint = Color.White,
        modifier = Modifier.size(12.dp),
      )
    }
  }
}

@Composable
private fun MediaPopupTabButton(
  title: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.clickable(onClick = onClick),
    shape = CardShape,
    color = if (selected) Accent else Color.White,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = title,
        color = if (selected) Color.White else Ink.copy(alpha = 0.78f),
        style = TextStyle(
          fontWeight = FontWeight.SemiBold,
          fontSize = 14.sp,
        ),
      )
    }
  }
}

@Composable
private fun BrochureScreen(
  mediaUris: List<String>,
  productName: String,
  onProductNameChange: (String) -> Unit,
  description: String,
  onDescriptionChange: (String) -> Unit,
  contactText: String,
  onAddImageFromDrive: () -> Unit,
  onAddImageFromCamera: (() -> Unit)?,
  fontFamily: FontFamily,
  compactScreen: Boolean = false,
  standalonePage: Boolean = false,
) {
  val context = LocalContext.current
  val brochureImageUri = remember(mediaUris) { mediaUris.firstOrNull { !isVideoUri(context, it) } }
  var darkMode by rememberSaveable { mutableStateOf(false) }
  val brochureBackground = if (darkMode) Color(0xFF7B7B7B) else Color.White
  val titleColor = if (darkMode) Color.White.copy(alpha = 0.92f) else Color(0xFF6E6A64)
  val bodyColor = if (darkMode) Color.White.copy(alpha = 0.82f) else Color(0xFF5F5952)
  val contactValue = contactText

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = if (standalonePage) androidx.compose.foundation.shape.RoundedCornerShape(0.dp) else CardShape,
    color = brochureBackground,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val tabletLayout = maxWidth >= 700.dp
        val contentWidth = if (tabletLayout) minOf(maxWidth * 0.82f, 560.dp) else maxWidth
        val logoWidth = if (tabletLayout) 152.dp else 124.dp
        val logoHeight = if (tabletLayout) 36.dp else 30.dp
        val imageHeight = if (tabletLayout) 380.dp else 320.dp
        val titleSize = if (tabletLayout) 28.sp else 22.sp
        val titleLineHeight = if (tabletLayout) 32.sp else 26.sp
        val bodySize = if (tabletLayout) 14.sp else 12.sp
        val bodyLineHeight = if (tabletLayout) 20.sp else 16.sp
        val contactSize = if (tabletLayout) 13.sp else 11.sp

        Column(
          modifier = Modifier.width(contentWidth).align(Alignment.TopCenter),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          AshokvatikaLogo(
            modifier = Modifier
              .align(Alignment.Start)
              .padding(start = 2.dp, top = 2.dp)
              .width(logoWidth),
            textSize = if (tabletLayout) 22.sp else 18.sp,
            markSize = if (tabletLayout) 28.dp else 22.dp,
            fixedHeight = logoHeight,
          )

          Spacer(modifier = Modifier.height(if (tabletLayout) 14.dp else 10.dp))

          BrochureImageContainer(
            uriString = brochureImageUri,
            onAddImageFromDrive = onAddImageFromDrive,
            onAddImageFromCamera = onAddImageFromCamera,
            height = imageHeight,
            compactScreen = compactScreen,
          )

          Spacer(modifier = Modifier.height(if (tabletLayout) 16.dp else 12.dp))

          Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
          ) {
            TemplateEditableText(
              value = productName,
              onValueChange = onProductNameChange,
              placeholder = "Product Title",
              fontFamily = fontFamily,
              textAlign = TextAlign.Center,
              fontSize = titleSize,
              color = titleColor,
              lineHeight = titleLineHeight,
              fontWeight = FontWeight.SemiBold,
              clearOnFirstFocusValue = "Product Title",
              widthFraction = if (tabletLayout) 0.68f else 0.76f,
            )
          }

          Spacer(modifier = Modifier.height(if (tabletLayout) 14.dp else 10.dp))

          TemplateEditableText(
            value = description,
            onValueChange = onDescriptionChange,
            placeholder = "Product Description, Product Description, Product Description,\nProduct Description, Product Description, Product Description,\nProduct Description, Product Description,\nProduct Description, Product Description,",
            fontFamily = fontFamily,
            textAlign = TextAlign.Center,
            fontSize = bodySize,
            color = bodyColor,
            lineHeight = bodyLineHeight,
            minLines = 4,
            clearPlaceholderOnFocus = true,
            clearOnFirstFocusValue = "Product Description, Product Description, Product Description,\nProduct Description, Product Description, Product Description,\nProduct Description, Product Description,\nProduct Description, Product Description,",
            widthFraction = if (tabletLayout) 0.9f else 0.94f,
          )

          Spacer(modifier = Modifier.height(if (tabletLayout) 18.dp else 14.dp))

          Row(
            horizontalArrangement = Arrangement.spacedBy(if (tabletLayout) 18.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            BrochureCircleAction(
              onClick = { darkMode = !darkMode },
              icon = if (darkMode) Icons.Filled.DarkMode else Icons.Filled.WbSunny,
              tint = if (darkMode) Color.White else Ink.copy(alpha = 0.55f),
            )
            BrochureCircleAction(
              onClick = {},
              icon = Icons.Filled.WaterDrop,
              tint = if (darkMode) Color.White.copy(alpha = 0.9f) else Ink.copy(alpha = 0.55f),
            )
          }

          Spacer(modifier = Modifier.height(if (tabletLayout) 18.dp else 14.dp))

          Text(
            text = contactValue,
            modifier = Modifier.fillMaxWidth(),
            color = if (darkMode) Color.White.copy(alpha = 0.78f) else Ink.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            style = TextStyle(
              fontFamily = fontFamily,
              fontWeight = FontWeight.Medium,
              fontSize = contactSize,
              lineHeight = if (tabletLayout) 17.sp else 14.sp,
            ),
          )
        }
      }
    }
  }
}

@Composable
private fun BrochureTwoScreen(
  mediaUris: List<String>,
  productName: String,
  onProductNameChange: (String) -> Unit,
  description: String,
  onDescriptionChange: (String) -> Unit,
  contactText: String,
  onAddImage: (Int) -> Unit,
  onAddImageFromCamera: ((Int) -> Unit)? = null,
  fontFamily: FontFamily,
  standalonePage: Boolean = false,
  showBottomGrid: Boolean = true,
  showLogo: Boolean = true,
  useContainerBackground: Boolean = true,
  useUniformImageSizes: Boolean = false,
) {
  val context = LocalContext.current
  val brochureImages = remember(mediaUris) { mediaUris.filterNot { isVideoUri(context, it) } }
  var darkMode by rememberSaveable { mutableStateOf(false) }
  val background = when {
    !useContainerBackground -> Color.Transparent
    darkMode -> Color(0xFF7B7B7B)
    else -> Color.White
  }
  val titleColor = if (darkMode) Color.White.copy(alpha = 0.92f) else Color(0xFF6E6A64)
  val bodyColor = if (darkMode) Color.White.copy(alpha = 0.82f) else Color(0xFF5F5952)
  val contactValue = contactText

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = if (standalonePage) androidx.compose.foundation.shape.RoundedCornerShape(0.dp) else CardShape,
    color = background,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val tabletLayout = maxWidth >= 760.dp
        val layoutWidth = if (tabletLayout) minOf(maxWidth * 0.92f, 860.dp) else maxWidth
        val topImageHeight = if (tabletLayout) 332.dp else 248.dp
        val bottomImageHeight = if (useUniformImageSizes) topImageHeight else if (tabletLayout) 238.dp else 208.dp
        val titleSize = if (tabletLayout) 25.sp else 20.sp
        val titleLineHeight = if (tabletLayout) 30.sp else 24.sp
        val descriptionSize = if (tabletLayout) 13.sp else 11.sp
        val descriptionLineHeight = if (tabletLayout) 19.sp else 16.sp
        val contactSize = if (tabletLayout) 12.sp else 9.sp
        val logoWidth = if (tabletLayout) 154.dp else 126.dp
        val logoHeight = if (tabletLayout) 36.dp else 30.dp

        Column(
          modifier = Modifier.width(layoutWidth).align(Alignment.TopCenter),
          verticalArrangement = Arrangement.spacedBy(if (tabletLayout) 16.dp else 12.dp),
        ) {
          if (showLogo || contactValue.isNotBlank()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = if (showLogo) Arrangement.SpaceBetween else Arrangement.End,
              verticalAlignment = Alignment.Top,
            ) {
              if (showLogo) {
                AshokvatikaLogo(
                  modifier = Modifier
                    .padding(top = 2.dp)
                    .width(logoWidth),
                  textSize = if (tabletLayout) 22.sp else 18.sp,
                  markSize = if (tabletLayout) 28.dp else 22.dp,
                  fixedHeight = logoHeight,
                )
              }
              if (contactValue.isNotBlank()) {
                Text(
                  text = contactValue,
                  modifier = Modifier.padding(top = if (tabletLayout) 8.dp else 6.dp),
                  color = if (darkMode) Color.White.copy(alpha = 0.8f) else Ink.copy(alpha = 0.7f),
                  textAlign = TextAlign.End,
                  style = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = contactSize,
                    lineHeight = if (tabletLayout) 15.sp else 13.sp,
                  ),
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (tabletLayout) 14.dp else 10.dp),
            verticalAlignment = Alignment.Top,
          ) {
            BrochureImageContainer(
              uriString = brochureImages.getOrNull(0),
              onAddImageFromDrive = { onAddImage(0) },
              onAddImageFromCamera = onAddImageFromCamera?.let { cameraHandler -> { cameraHandler(0) } },
              modifier = Modifier.weight(0.92f),
              height = topImageHeight,
            )
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(if (tabletLayout) 12.dp else 8.dp),
            ) {
              TemplateEditableText(
                value = productName,
                onValueChange = onProductNameChange,
                placeholder = "Product Title",
                fontFamily = fontFamily,
                textAlign = TextAlign.Start,
                fontSize = titleSize,
                color = titleColor,
                lineHeight = titleLineHeight,
                fontWeight = FontWeight.SemiBold,
                clearOnFirstFocusValue = "Product Title",
              )
              TemplateEditableText(
                value = description,
                onValueChange = onDescriptionChange,
                placeholder = "Product Description, Product Description,\nProduct Description, Product Description,\nProduct Description, Product Description,\nProduct Description, Product Description,",
                fontFamily = fontFamily,
                textAlign = TextAlign.Start,
                fontSize = descriptionSize,
                color = bodyColor,
                lineHeight = descriptionLineHeight,
                minLines = 4,
                fontWeight = FontWeight.Medium,
                clearPlaceholderOnFocus = true,
                clearOnFirstFocusValue = "Product Description, Product Description,\nProduct Description, Product Description,\nProduct Description, Product Description,\nProduct Description, Product Description,",
              )
              Row(
                horizontalArrangement = Arrangement.spacedBy(if (tabletLayout) 16.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                BrochureCircleAction(
                  onClick = { darkMode = !darkMode },
                  icon = if (darkMode) Icons.Filled.DarkMode else Icons.Filled.WbSunny,
                  tint = if (darkMode) Color.White else Ink.copy(alpha = 0.55f),
                )
                BrochureCircleAction(
                  onClick = {},
                  icon = Icons.Filled.WaterDrop,
                  tint = if (darkMode) Color.White.copy(alpha = 0.9f) else Ink.copy(alpha = 0.55f),
                )
              }
            }
          }

          if (showBottomGrid) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(if (tabletLayout) 14.dp else 12.dp),
            ) {
              BrochureImageContainer(
                uriString = brochureImages.getOrNull(1),
                onAddImageFromDrive = { onAddImage(1) },
                onAddImageFromCamera = onAddImageFromCamera?.let { cameraHandler -> { cameraHandler(1) } },
                modifier = Modifier.weight(1f),
                height = bottomImageHeight,
              )
              BrochureImageContainer(
                uriString = brochureImages.getOrNull(2),
                onAddImageFromDrive = { onAddImage(2) },
                onAddImageFromCamera = onAddImageFromCamera?.let { cameraHandler -> { cameraHandler(2) } },
                modifier = Modifier.weight(1f),
                height = bottomImageHeight,
              )
            }
          }
        }
      }
    }
  }
}

private data class CatCatalogItemState(
  val sku: String,
  val onSkuChange: (String) -> Unit,
  val title: String,
  val onTitleChange: (String) -> Unit,
  val description: String,
  val onDescriptionChange: (String) -> Unit,
  val useCameraPlaceholder: Boolean = false,
)

private fun defaultCatalogDescription(): String =
  "Product Description, Product Description,\n" +
    "Product Description, Product Description,\n" +
    "Product Description, Product Description,\n" +
    "Product Description, Product Description,"

@Composable
private fun CatTwoScreen(
  mediaUris: List<String>,
  firstSku: String,
  onFirstSkuChange: (String) -> Unit,
  firstTitle: String,
  onFirstTitleChange: (String) -> Unit,
  firstDescription: String,
  onFirstDescriptionChange: (String) -> Unit,
  secondSku: String,
  onSecondSkuChange: (String) -> Unit,
  secondTitle: String,
  onSecondTitleChange: (String) -> Unit,
  secondDescription: String,
  onSecondDescriptionChange: (String) -> Unit,
  contactText: String,
  onAddImage: (Int) -> Unit,
  onAddImageFromCamera: ((Int) -> Unit)? = null,
  fontFamily: FontFamily,
  showLogo: Boolean = true,
) {
  CatCatalogScreen(
    mediaUris = mediaUris,
    items = listOf(
      CatCatalogItemState(firstSku, onFirstSkuChange, firstTitle, onFirstTitleChange, firstDescription, onFirstDescriptionChange),
      CatCatalogItemState(secondSku, onSecondSkuChange, secondTitle, onSecondTitleChange, secondDescription, onSecondDescriptionChange),
    ),
    contactText = contactText,
    onAddImage = onAddImage,
    onAddImageFromCamera = onAddImageFromCamera,
    fontFamily = fontFamily,
    showLogo = showLogo,
  )
}

@Composable
private fun CatCatalogScreen(
  mediaUris: List<String>,
  items: List<CatCatalogItemState>,
  contactText: String,
  onAddImage: (Int) -> Unit,
  onAddImageFromCamera: ((Int) -> Unit)? = null,
  fontFamily: FontFamily,
  showLogo: Boolean = true,
) {
  val context = LocalContext.current
  val brochureImages = remember(mediaUris) { mediaUris.filterNot { isVideoUri(context, it) } }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 14.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
      val tabletLayout = maxWidth >= 760.dp
      val layoutWidth = if (tabletLayout) minOf(maxWidth * 0.94f, 860.dp) else maxWidth

      Column(
        modifier = Modifier
          .width(layoutWidth)
          .align(Alignment.TopCenter),
        verticalArrangement = Arrangement.spacedBy(if (tabletLayout) 18.dp else 16.dp),
      ) {
        CatTwoHeader(
          contactText = contactText,
          fontFamily = fontFamily,
          tabletLayout = tabletLayout,
          showLogo = showLogo,
        )
        items.forEachIndexed { index, item ->
          CatTwoItemRow(
            sku = item.sku,
            onSkuChange = item.onSkuChange,
            title = item.title,
            onTitleChange = item.onTitleChange,
            description = item.description,
            onDescriptionChange = item.onDescriptionChange,
            uriString = brochureImages.getOrNull(index),
            onAddImage = { onAddImage(index) },
            onAddImageFromCamera = onAddImageFromCamera?.let { cameraHandler -> { cameraHandler(index) } },
            fontFamily = fontFamily,
            tabletLayout = tabletLayout,
            useCameraPlaceholder = item.useCameraPlaceholder,
          )
        }
      }
    }
  }
}

@Composable
private fun CatTwoHeader(
  contactText: String,
  fontFamily: FontFamily,
  tabletLayout: Boolean,
  showLogo: Boolean = true,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (showLogo) Arrangement.SpaceBetween else Arrangement.End,
    verticalAlignment = Alignment.Top,
  ) {
    if (showLogo) {
      AshokvatikaLogo(
        modifier = Modifier
          .padding(top = 2.dp)
          .width(if (tabletLayout) 154.dp else 126.dp),
        textSize = if (tabletLayout) 22.sp else 18.sp,
        markSize = if (tabletLayout) 28.dp else 22.dp,
        fixedHeight = if (tabletLayout) 36.dp else 30.dp,
      )
    }
    if (contactText.isNotBlank()) {
      Text(
        text = contactText,
        modifier = Modifier.padding(top = if (tabletLayout) 8.dp else 6.dp),
        color = Ink.copy(alpha = 0.7f),
        textAlign = TextAlign.End,
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Medium,
          fontSize = if (tabletLayout) 12.sp else 9.sp,
          lineHeight = if (tabletLayout) 15.sp else 13.sp,
        ),
      )
    }
  }
}

@Composable
private fun CatTwoItemRow(
  sku: String,
  onSkuChange: (String) -> Unit,
  title: String,
  onTitleChange: (String) -> Unit,
  description: String,
  onDescriptionChange: (String) -> Unit,
  uriString: String?,
  onAddImage: () -> Unit,
  onAddImageFromCamera: (() -> Unit)? = null,
  fontFamily: FontFamily,
  tabletLayout: Boolean,
  useCameraPlaceholder: Boolean = false,
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(if (tabletLayout) 10.dp else 8.dp),
  ) {
    TemplateEditableText(
      value = sku,
      onValueChange = onSkuChange,
      placeholder = "SKU#",
      fontFamily = fontFamily,
      textAlign = TextAlign.Start,
      fontSize = if (tabletLayout) 20.sp else 16.sp,
      color = Color(0xFF6E6A64),
      lineHeight = if (tabletLayout) 24.sp else 20.sp,
      fontWeight = FontWeight.SemiBold,
      clearOnFirstFocusValue = "SKU#",
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(if (tabletLayout) 14.dp else 10.dp),
      verticalAlignment = Alignment.Top,
    ) {
      BrochureImageContainer(
        uriString = uriString,
        onAddImageFromDrive = onAddImage,
        onAddImageFromCamera = onAddImageFromCamera,
        modifier = Modifier.weight(0.92f),
        height = if (tabletLayout) 332.dp else 248.dp,
        placeholderIcon = if (useCameraPlaceholder) Icons.Filled.PhotoCamera else Icons.Filled.AddToDrive,
        placeholderIconDescription = if (useCameraPlaceholder) "Camera" else "Google Drive",
        showActionButtons = !useCameraPlaceholder || onAddImageFromCamera != null,
        openPickerOnContainerTap = useCameraPlaceholder,
      )
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(if (tabletLayout) 12.dp else 8.dp),
      ) {
        TemplateEditableText(
          value = title,
          onValueChange = onTitleChange,
          placeholder = "Product Title",
          fontFamily = fontFamily,
          textAlign = TextAlign.Start,
          fontSize = if (tabletLayout) 25.sp else 20.sp,
          color = Color(0xFF6E6A64),
          lineHeight = if (tabletLayout) 30.sp else 24.sp,
          fontWeight = FontWeight.SemiBold,
          clearOnFirstFocusValue = "Product Title",
        )
        TemplateEditableText(
          value = description,
          onValueChange = onDescriptionChange,
          placeholder = "Product Description, Product Description,\nProduct Description, Product Description,\nProduct Description, Product Description,\nProduct Description, Product Description,",
          fontFamily = fontFamily,
          textAlign = TextAlign.Start,
          fontSize = if (tabletLayout) 13.sp else 11.sp,
          color = Color(0xFF5F5952),
          lineHeight = if (tabletLayout) 19.sp else 16.sp,
          minLines = 4,
          fontWeight = FontWeight.Medium,
          clearPlaceholderOnFocus = true,
          clearOnFirstFocusValue = "Product Description, Product Description,\nProduct Description, Product Description,\nProduct Description, Product Description,\nProduct Description, Product Description,",
        )
        Row(
          horizontalArrangement = Arrangement.spacedBy(if (tabletLayout) 16.dp else 12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          BrochureCircleAction(
            onClick = {},
            icon = Icons.Filled.WbSunny,
            tint = Ink.copy(alpha = 0.55f),
          )
          BrochureCircleAction(
            onClick = {},
            icon = Icons.Filled.WaterDrop,
            tint = Ink.copy(alpha = 0.55f),
          )
        }
      }
    }
  }
}

@Composable
private fun BrochureImageContainer(
  uriString: String?,
  onAddImageFromDrive: () -> Unit,
  onAddImageFromCamera: (() -> Unit)?,
  modifier: Modifier = Modifier,
  height: Dp = 320.dp,
  compactScreen: Boolean = false,
  placeholderIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Filled.AddToDrive,
  placeholderIconDescription: String = "Google Drive",
  showActionButtons: Boolean = true,
  openPickerOnContainerTap: Boolean = false,
) {
  val imageBitmap by rememberDecodedImage(uriString, maxDimensionPx = 1600)
  val hasImage = imageBitmap != null
  var actionButtonsVisible by remember(uriString) { mutableStateOf(uriString.isNullOrBlank()) }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .height(height)
      .clickable(
        enabled = openPickerOnContainerTap || hasImage,
      ) {
        when {
          openPickerOnContainerTap && !hasImage -> onAddImageFromDrive()
          hasImage -> actionButtonsVisible = !actionButtonsVisible
        }
      },
    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    color = Color(0xFFF9E5D9),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val largeContainer = maxWidth >= 280.dp || maxHeight >= 280.dp
      val placeholderIconSize = if (largeContainer) 46.dp else 34.dp
      val placeholderTextSize = if (largeContainer) 18.sp else 14.sp
      val overlayPadding = if (largeContainer) 16.dp else 10.dp
      val overlayIconSize = if (largeContainer) 18.dp else 16.dp
      val overlayTextSize = if (largeContainer) 14.sp else 12.sp
      val actionSize = if (compactScreen) 50.dp else 56.dp
      val actionIconSize = if (compactScreen) 22.dp else 26.dp

      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        if (imageBitmap != null) {
          Image(
            bitmap = imageBitmap!!,
            contentDescription = "Brochure image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
          )
        } else {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (largeContainer) 12.dp else 8.dp),
          ) {
            Icon(
              imageVector = placeholderIcon,
              contentDescription = placeholderIconDescription,
              tint = Ink.copy(alpha = 0.42f),
              modifier = Modifier.size(placeholderIconSize),
            )
            Text(
              text = "image container",
              color = Ink.copy(alpha = 0.4f),
              textAlign = TextAlign.Center,
              style = TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = placeholderTextSize,
              ),
            )
          }
        }

        if (showActionButtons && (!hasImage || actionButtonsVisible)) {
          Surface(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(bottom = overlayPadding),
            color = Color.Transparent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              if (onAddImageFromCamera != null) {
                CircleIconAction(
                  size = actionSize,
                  icon = {
                    Icon(
                      imageVector = Icons.Filled.PhotoCamera,
                      contentDescription = "Camera",
                      tint = Accent,
                      modifier = Modifier.size(actionIconSize),
                    )
                  },
                  onClick = onAddImageFromCamera,
                )
              }
              CircleIconAction(
                size = actionSize,
                icon = {
                  Icon(
                    imageVector = Icons.Filled.AddToDrive,
                    contentDescription = "Google Drive",
                    tint = Accent,
                    modifier = Modifier.size(overlayIconSize.coerceAtLeast(18.dp)),
                  )
                },
                onClick = onAddImageFromDrive,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun BrochureCircleAction(
  onClick: () -> Unit,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  tint: Color,
) {
  CircleIconAction(
    size = 42.dp,
    borderColor = Ink.copy(alpha = 0.12f),
    borderWidth = 1.dp,
    shadowElevation = 0.dp,
    icon = {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(20.dp),
      )
    },
    onClick = onClick,
  )
}

@Composable
private fun ShareActionButton(
  action: SharePlatformAction,
  onClick: () -> Unit,
) {
  Column(
    modifier = Modifier.clickable(onClick = onClick),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Box(
      modifier = Modifier
        .size(52.dp)
        .background(Color(0xFFF5F5F7), CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = action.icon,
        contentDescription = action.label,
        tint = Accent,
        modifier = Modifier.size(24.dp),
      )
    }
    Text(
      text = action.label,
      color = Ink.copy(alpha = 0.82f),
      style = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
      ),
    )
  }
}

@Composable
private fun MediaFeatureTemplateOne(
  uriString: String?,
  title: String,
  onTitleChange: (String) -> Unit,
  titlePlaceholder: String,
  description: String,
  onDescriptionChange: (String) -> Unit,
  contactText: String,
  onContactTextChange: (String) -> Unit,
  onAddImage: () -> Unit,
  fontFamily: FontFamily,
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState()),
    contentAlignment = Alignment.Center,
  ) {
    BrochurePage {
      BrochureLogo(fontFamily = fontFamily)
      ProductImage(
        uriString = uriString,
        containerX = 108.dp,
        containerY = 167.dp,
        containerWidth = 197.dp,
        containerHeight = 148.dp,
        contentScale = ContentScale.Fit,
        showWhiteBackground = true,
        addButtonLabel = "Add image",
        onAddImage = onAddImage,
      )
      TitleText(
        value = title,
        onValueChange = onTitleChange,
        placeholder = if (titlePlaceholder.isBlank()) "Pine Bonsai" else titlePlaceholder,
        modifier = Modifier
          .offset(x = 137.dp, y = 356.dp)
          .width(139.dp)
          .height(34.dp),
        fontFamily = fontFamily,
      )
      BodyDescription(
        value = description,
        onValueChange = onDescriptionChange,
        placeholder = "A premium bonsai statement piece with refined character and timeless calm.",
        modifier = Modifier
          .offset(x = 43.dp, y = 392.dp)
          .width(327.dp)
          .height(41.dp),
        fontFamily = fontFamily,
      )
      ContactNumber(
        value = contactText,
        onValueChange = onContactTextChange,
        placeholder = "+91 78085 80855",
        modifier = Modifier
          .offset(x = 123.dp, y = 472.dp)
          .width(167.dp)
          .height(19.dp),
        fontFamily = fontFamily,
      )
    }
  }
}

@Composable
private fun MediaFeatureTemplateTwo(
  uriString: String?,
  title: String,
  onTitleChange: (String) -> Unit,
  titlePlaceholder: String,
  description: String,
  onDescriptionChange: (String) -> Unit,
  contactText: String,
  onContactTextChange: (String) -> Unit,
  onAddImage: () -> Unit,
  fontFamily: FontFamily,
) {
  var activeIconIds by rememberSaveable { mutableStateOf(listOf<Int>()) }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState()),
    contentAlignment = Alignment.Center,
  ) {
    BrochurePage {
      BrochureLogo(fontFamily = fontFamily)
      ProductImage(
        uriString = uriString,
        containerX = 42.dp,
        containerY = 82.dp,
        containerWidth = 329.dp,
        containerHeight = 165.dp,
        contentScale = ContentScale.Crop,
        showWhiteBackground = false,
        addButtonLabel = "Add image",
        onAddImage = onAddImage,
      )
      CareIconsGroup(
        modifier = Modifier
          .offset(x = 144.dp, y = 278.dp)
          .width(125.dp)
          .height(47.dp),
        activeIconIds = activeIconIds,
        onToggleIcon = { iconId ->
          activeIconIds = if (iconId in activeIconIds) {
            activeIconIds - iconId
          } else {
            activeIconIds + iconId
          }
        },
      )
      TitleText(
        value = title,
        onValueChange = onTitleChange,
        placeholder = if (titlePlaceholder.isBlank()) "Pine Bonsai / care" else titlePlaceholder,
        modifier = Modifier
          .offset(x = 98.dp, y = 356.dp)
          .width(218.dp)
          .height(35.dp),
        fontFamily = fontFamily,
      )
      BodyDescription(
        value = description,
        onValueChange = onDescriptionChange,
        placeholder = "Place in bright, indirect light and water with care for a balanced, lasting display.",
        modifier = Modifier
          .offset(x = 50.dp, y = 392.dp)
          .width(314.dp)
          .height(55.dp),
        fontFamily = fontFamily,
      )
      ContactNumber(
        value = contactText,
        onValueChange = onContactTextChange,
        placeholder = "+91 78085 80855",
        modifier = Modifier
          .offset(x = 123.dp, y = 486.dp)
          .width(167.dp)
          .height(19.dp),
        fontFamily = fontFamily,
      )
    }
  }
}

@Composable
private fun BrochurePage(
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit,
) {
  Surface(
    modifier = modifier
      .width(413.dp)
      .height(531.dp),
    color = Color.White,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Box(modifier = Modifier.fillMaxSize(), content = content)
  }
}

@Composable
private fun BrochureLogo(
  fontFamily: FontFamily,
) {
  AshokvatikaLogo(
    modifier = Modifier
      .offset(x = 42.dp, y = 14.dp)
      .width(112.dp),
    textSize = 19.sp,
    markSize = 24.dp,
    fixedHeight = 27.dp,
  )
}

@Composable
private fun ProductImage(
  uriString: String?,
  containerX: Dp,
  containerY: Dp,
  containerWidth: Dp,
  containerHeight: Dp,
  contentScale: ContentScale,
  showWhiteBackground: Boolean,
  addButtonLabel: String,
  onAddImage: () -> Unit,
) {
  val imageBitmap by rememberDecodedImage(uriString, maxDimensionPx = 1600)

  Box(
    modifier = Modifier
      .offset(x = containerX, y = containerY)
      .width(containerWidth)
      .height(containerHeight)
      .background(if (showWhiteBackground) Color.White else Color(0xFFF1ECE4)),
    contentAlignment = Alignment.Center,
  ) {
    if (imageBitmap != null) {
      Image(
        bitmap = imageBitmap!!,
        contentDescription = "Brochure product image",
        contentScale = contentScale,
        modifier = Modifier
          .fillMaxSize()
          .padding(if (contentScale == ContentScale.Fit) 4.dp else 0.dp),
      )
    } else {
      Surface(
        modifier = Modifier.clickable(onClick = onAddImage),
        shape = CardShape,
        color = Accent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = addButtonLabel,
            tint = Color.White,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = addButtonLabel,
            color = Color.White,
            style = TextStyle(
              fontWeight = FontWeight.SemiBold,
              fontSize = 14.sp,
            ),
          )
        }
      }
    }

    if (imageBitmap != null) {
      Surface(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 6.dp)
          .clickable(onClick = onAddImage),
        shape = CardShape,
        color = Color.White.copy(alpha = 0.96f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
      ) {
        Text(
          text = "Change image",
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          color = Accent,
          style = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
          ),
        )
      }
    }
  }
}

@Composable
private fun TitleText(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  modifier: Modifier,
  fontFamily: FontFamily,
) {
  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    TemplateEditableText(
      value = value,
      onValueChange = onValueChange,
      placeholder = placeholder,
      fontFamily = fontFamily,
      textAlign = TextAlign.Center,
      fontSize = 25.sp,
      color = Color(0xFFAAA395),
      lineHeight = 29.sp,
      letterSpacing = (-0.2).sp,
      fontWeight = FontWeight.SemiBold,
    )
  }
}

@Composable
private fun BodyDescription(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  modifier: Modifier,
  fontFamily: FontFamily,
) {
  Box(
    modifier = modifier,
    contentAlignment = Alignment.TopCenter,
  ) {
    TemplateEditableText(
      value = value,
      onValueChange = onValueChange,
      placeholder = placeholder,
      fontFamily = fontFamily,
      textAlign = TextAlign.Center,
      fontSize = 10.5.sp,
      color = Color(0xFF564F47),
      lineHeight = 14.5.sp,
      minLines = 3,
      fontWeight = FontWeight.Medium,
    )
  }
}

@Composable
private fun ContactNumber(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  modifier: Modifier,
  fontFamily: FontFamily,
) {
  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    TemplateEditableText(
      value = value,
      onValueChange = onValueChange,
      placeholder = placeholder,
      fontFamily = fontFamily,
      textAlign = TextAlign.Center,
      fontSize = 14.5.sp,
      color = Color(0xFF6A625A),
      lineHeight = 18.sp,
      letterSpacing = 0.sp,
      fontWeight = FontWeight.SemiBold,
    )
  }
}

@Composable
private fun CareIconsGroup(
  modifier: Modifier = Modifier,
  activeIconIds: List<Int>,
  onToggleIcon: (Int) -> Unit,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    EditableCareIcon(
      iconId = 0,
      active = 0 in activeIconIds,
      icon = Icons.Filled.WbSunny,
      onClick = { onToggleIcon(0) },
      modifier = Modifier.weight(1f),
    )
    EditableCareIcon(
      iconId = 1,
      active = 1 in activeIconIds,
      icon = Icons.Filled.Cloud,
      onClick = { onToggleIcon(1) },
      modifier = Modifier.weight(1f),
    )
    EditableCareIcon(
      iconId = 2,
      active = 2 in activeIconIds,
      icon = Icons.Filled.Home,
      onClick = { onToggleIcon(2) },
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun EditableCareIcon(
  iconId: Int,
  active: Boolean,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val tint = if (active) Color(0xFFD9822B) else Color(0xFFB7AA98)
  Column(
    modifier = modifier.clickable(onClick = onClick),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = "Care icon $iconId",
      tint = tint,
      modifier = Modifier.size(22.dp),
    )
    Spacer(modifier = Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
      Icon(
        imageVector = Icons.Filled.WaterDrop,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(10.dp),
      )
      Icon(
        imageVector = Icons.Filled.WaterDrop,
        contentDescription = null,
        tint = tint.copy(alpha = 0.9f),
        modifier = Modifier.size(10.dp),
      )
    }
  }
}

@Composable
private fun MediaFeatureTemplateThree(
  primaryUri: String?,
  secondaryUri: String?,
  primaryTitle: String,
  onPrimaryTitleChange: (String) -> Unit,
  primaryTitlePlaceholder: String,
  primaryDescription: String,
  onPrimaryDescriptionChange: (String) -> Unit,
  secondaryTitle: String,
  onSecondaryTitleChange: (String) -> Unit,
  secondaryTitlePlaceholder: String,
  secondaryDescription: String,
  onSecondaryDescriptionChange: (String) -> Unit,
  contactText: String,
  onContactTextChange: (String) -> Unit,
  fontFamily: FontFamily,
) {
  TemplatePreviewCard {
    TemplateBrandHeader(
      fontFamily = fontFamily,
      contactText = contactText,
      onContactTextChange = onContactTextChange,
      editableContact = true,
    )
    Spacer(modifier = Modifier.height(18.dp))
    StackedTemplateRow(
      uriString = primaryUri,
      title = primaryTitle,
      onTitleChange = onPrimaryTitleChange,
      titlePlaceholder = primaryTitlePlaceholder,
      description = primaryDescription,
      onDescriptionChange = onPrimaryDescriptionChange,
      descriptionPlaceholder = "Add concise sales copy here.",
      fontFamily = fontFamily,
    )
    Spacer(modifier = Modifier.height(18.dp))
    StackedTemplateRow(
      uriString = secondaryUri,
      title = secondaryTitle,
      onTitleChange = onSecondaryTitleChange,
      titlePlaceholder = secondaryTitlePlaceholder,
      description = secondaryDescription,
      onDescriptionChange = onSecondaryDescriptionChange,
      descriptionPlaceholder = "Add care guidance here.",
      fontFamily = fontFamily,
    )
  }
}

@Composable
private fun TemplatePreviewCard(
  content: @Composable ColumnScope.() -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = CardShape,
    color = Color.White,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      content = content,
    )
  }
}

@Composable
private fun TemplateBrandHeader(
  fontFamily: FontFamily,
  contactText: String,
  onContactTextChange: (String) -> Unit = {},
  editableContact: Boolean = false,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top,
  ) {
    AshokvatikaLogo(
      modifier = Modifier.width(154.dp),
      textSize = 18.sp,
      markSize = 28.dp,
    )
    if (contactText.isNotBlank() || editableContact) {
      TemplateEditableText(
        value = contactText,
        onValueChange = onContactTextChange,
        placeholder = if (editableContact) "+91 78085 80855" else "",
        fontFamily = fontFamily,
        textAlign = TextAlign.End,
        fontSize = 14.sp,
        color = Ink.copy(alpha = 0.8f),
        lineHeight = 16.sp,
        enabled = editableContact,
      )
    } else {
      Spacer(modifier = Modifier.width(12.dp))
    }
  }
}

@Composable
private fun TemplateMediaImage(
  uriString: String?,
  modifier: Modifier = Modifier,
  rounded: Boolean = true,
  addButtonLabel: String? = null,
  onAddImage: (() -> Unit)? = null,
) {
  val imageBitmap by rememberDecodedImage(uriString, maxDimensionPx = 1400)
  val shape = if (rounded) CardShape else androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
  Surface(
    modifier = modifier,
    shape = shape,
    color = Color(0xFFE7E4DF),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    if (imageBitmap != null) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(10.dp),
        contentAlignment = Alignment.Center,
      ) {
        Image(
          bitmap = imageBitmap!!,
          contentDescription = "Template media",
          contentScale = ContentScale.Fit,
          modifier = Modifier.fillMaxSize(),
        )
        if (addButtonLabel != null && onAddImage != null) {
          Surface(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(bottom = 6.dp)
              .clickable(onClick = onAddImage),
            shape = CardShape,
            color = Color.White.copy(alpha = 0.94f),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Change image",
                tint = Accent,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = "Change image",
                color = Accent,
                style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
              )
            }
          }
        }
      }
    } else {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Text(
            text = "Image container",
            textAlign = TextAlign.Center,
            color = Ink.copy(alpha = 0.55f),
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
          )
          if (addButtonLabel != null && onAddImage != null) {
            Surface(
              modifier = Modifier.clickable(onClick = onAddImage),
              shape = CardShape,
              color = Accent,
              tonalElevation = 0.dp,
              shadowElevation = 0.dp,
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = Icons.Filled.Add,
                  contentDescription = addButtonLabel,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp),
                )
                Text(
                  text = addButtonLabel,
                  color = Color.White,
                  style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CareIconRow() {
  Row(
    horizontalArrangement = Arrangement.spacedBy(22.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    TemplateCareIcon(icon = Icons.Filled.WbSunny)
    TemplateCareIcon(icon = Icons.Filled.Cloud)
    TemplateCareIcon(icon = Icons.Filled.Home)
  }
}

@Composable
private fun TemplateCareIcon(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = Ink.copy(alpha = 0.42f),
      modifier = Modifier.size(28.dp),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
      Icon(
        imageVector = Icons.Filled.WaterDrop,
        contentDescription = null,
        tint = Ink.copy(alpha = 0.42f),
        modifier = Modifier.size(12.dp),
      )
      Icon(
        imageVector = Icons.Filled.WaterDrop,
        contentDescription = null,
        tint = Ink.copy(alpha = 0.42f),
        modifier = Modifier.size(12.dp),
      )
    }
  }
}

@Composable
private fun TemplateEditableText(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  fontFamily: FontFamily,
  textAlign: TextAlign,
  fontSize: androidx.compose.ui.unit.TextUnit,
  color: Color,
  lineHeight: androidx.compose.ui.unit.TextUnit,
  minLines: Int = 1,
  enabled: Boolean = true,
  letterSpacing: androidx.compose.ui.unit.TextUnit = 0.sp,
  fontWeight: FontWeight = FontWeight.Thin,
  clearPlaceholderOnFocus: Boolean = false,
  clearOnFirstFocusValue: String? = null,
  widthFraction: Float = 1f,
) {
  var isFocused by remember { mutableStateOf(false) }
  var clearedOnFocus by remember(clearOnFirstFocusValue) { mutableStateOf(false) }
  BasicTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = Modifier
      .fillMaxWidth(widthFraction)
      .onFocusChanged {
        isFocused = it.isFocused
        if (it.isFocused && clearOnFirstFocusValue != null && !clearedOnFocus && value == clearOnFirstFocusValue) {
          onValueChange("")
          clearedOnFocus = true
        }
      },
    enabled = enabled,
    minLines = minLines,
    textStyle = TextStyle(
      fontFamily = fontFamily,
      fontWeight = fontWeight,
      fontSize = fontSize,
      lineHeight = lineHeight,
      color = color,
      textAlign = textAlign,
      letterSpacing = letterSpacing,
    ),
    cursorBrush = SolidColor(Accent),
    decorationBox = { innerTextField ->
      Box(modifier = Modifier.fillMaxWidth()) {
        if (value.isBlank() && placeholder.isNotBlank() && (!clearPlaceholderOnFocus || !isFocused)) {
          Text(
            text = placeholder,
            modifier = Modifier.fillMaxWidth(),
            color = color.copy(alpha = 0.52f),
            textAlign = textAlign,
            style = TextStyle(
              fontFamily = fontFamily,
              fontWeight = fontWeight,
              fontSize = fontSize,
              lineHeight = lineHeight,
              letterSpacing = letterSpacing,
            ),
          )
        }
        innerTextField()
      }
    },
  )
}

@Composable
private fun StackedTemplateRow(
  uriString: String?,
  title: String,
  onTitleChange: (String) -> Unit,
  titlePlaceholder: String,
  description: String,
  onDescriptionChange: (String) -> Unit,
  descriptionPlaceholder: String,
  fontFamily: FontFamily,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(14.dp),
    verticalAlignment = Alignment.Top,
  ) {
    TemplateMediaImage(
      uriString = uriString,
      modifier = Modifier
        .width(124.dp)
        .height(124.dp),
    )
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      TemplateEditableText(
        value = title,
        onValueChange = onTitleChange,
        placeholder = titlePlaceholder,
        fontFamily = fontFamily,
        textAlign = TextAlign.Start,
        fontSize = 26.sp,
        color = Ink.copy(alpha = 0.78f),
        lineHeight = 28.sp,
      )
      TemplateEditableText(
        value = description,
        onValueChange = onDescriptionChange,
        placeholder = descriptionPlaceholder,
        fontFamily = fontFamily,
        textAlign = TextAlign.Start,
        fontSize = 10.5.sp,
        color = Ink.copy(alpha = 0.8f),
        lineHeight = 14.sp,
        minLines = 4,
      )
      CareIconRow()
    }
  }
}

@Composable
private fun MediaThumbnailCarousel(
  mediaUris: List<String>,
  selectedIndex: Int,
  fontFamily: FontFamily,
  onAddFromDrive: () -> Unit,
  onSelect: (Int) -> Unit,
  onShare: (String) -> Unit,
) {
  val scrollState = rememberScrollState()
  val density = LocalDensity.current
  val expandedWidth = 132.dp
  val collapsedWidth = 42.dp
  val addTileWidth = 76.dp
  val itemGap = 8.dp
  val horizontalPadding = 2.dp

  LaunchedEffect(mediaUris, selectedIndex) {
    if (mediaUris.isEmpty()) return@LaunchedEffect

    val targetCenterPx = with(density) {
      var position = addTileWidth.toPx() + itemGap.toPx()
      repeat(selectedIndex) {
        position += collapsedWidth.toPx()
        position += itemGap.toPx()
      }
      position + (expandedWidth.toPx() / 2f)
    }

    val viewportCenterPx = scrollState.viewportSize / 2f
    val desiredScroll = (targetCenterPx - viewportCenterPx).toInt().coerceAtLeast(0)
    val maxScroll = scrollState.maxValue.coerceAtLeast(0)
    scrollState.animateScrollTo(desiredScroll.coerceAtMost(maxScroll))
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(scrollState)
      .height(112.dp)
      .padding(horizontal = horizontalPadding),
    horizontalArrangement = Arrangement.spacedBy(itemGap),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    AddMediaThumbnailCard(
      fontFamily = fontFamily,
      width = addTileWidth,
      onClick = onAddFromDrive,
    )

    mediaUris.forEachIndexed { index, uriString ->
      MediaThumbnailCard(
        uriString = uriString,
        selected = index == selectedIndex,
        fontFamily = fontFamily,
        width = if (index == selectedIndex) expandedWidth else collapsedWidth,
        onClick = { onSelect(index) },
        onShare = { onShare(uriString) },
      )
    }
  }
}

@Composable
private fun AddMediaThumbnailCard(
  fontFamily: FontFamily,
  width: Dp = 132.dp,
  onClick: () -> Unit,
) {
  Surface(
    modifier = Modifier
      .width(width)
      .height(112.dp)
      .clickable(onClick = onClick),
    shape = CardShape,
    color = Accent.copy(alpha = 0.12f),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .background(Color.White, CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add image from Google Drive",
            tint = Accent,
          )
        }
        Text(
          text = "Drive",
          color = Accent,
          style = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
          ),
        )
      }
    }
  }
}

@Composable
private fun MediaViewerCard(
  uriString: String,
  fontFamily: FontFamily,
  modifier: Modifier = Modifier,
  onShare: () -> Unit,
  footerLabel: String,
) {
  val context = LocalContext.current
  val isVideo = isVideoUri(context, uriString)
  val imageBitmap by rememberDecodedImage(
    uriString = uriString.takeUnless { isVideo },
    maxDimensionPx = 1400,
  )

  Surface(
    modifier = modifier,
    shape = CardShape,
    color = Color.White,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(320.dp),
    ) {
      if (isVideo) {
        VideoSurface(uriString = uriString, modifier = Modifier.fillMaxSize())
      } else if (imageBitmap != null) {
        Image(
          bitmap = imageBitmap!!,
          contentDescription = "Media image",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
      } else {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Accent.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "Preview unavailable",
            color = Ink.copy(alpha = 0.62f),
            style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 18.sp),
          )
        }
      }

      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(12.dp),
      ) {
        CircleIconAction(
          size = 40.dp,
          icon = {
            Icon(
              imageVector = Icons.Filled.Share,
              contentDescription = "Share media",
              tint = Accent,
            )
          },
          onClick = onShare,
        )
      }

      Box(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(14.dp)
          .background(Accent.copy(alpha = 0.78f), CardShape)
          .padding(horizontal = 12.dp, vertical = 7.dp),
      ) {
        Text(
          text = footerLabel,
          color = Color.White,
          style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
        )
      }
    }
  }
}

@Composable
private fun MediaThumbnailCard(
  uriString: String,
  selected: Boolean,
  fontFamily: FontFamily,
  width: Dp = 132.dp,
  onClick: () -> Unit,
  onShare: () -> Unit,
) {
  val context = LocalContext.current
  val isVideo = isVideoUri(context, uriString)
  val imageBitmap by rememberDecodedImage(
    uriString = uriString.takeUnless { isVideo },
    maxDimensionPx = 320,
  )
  val animatedWidth by animateDpAsState(
    targetValue = width,
    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
    label = "mediaThumbnailWidth",
  )

  Surface(
    modifier = Modifier
      .width(animatedWidth)
      .height(112.dp)
      .border(
        width = if (selected) 2.dp else 0.dp,
        color = if (selected) Accent else Color.Transparent,
        shape = CardShape,
      )
      .clickable(onClick = onClick),
    shape = CardShape,
    color = Color.White,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      if (isVideo) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Accent.copy(alpha = 0.14f)),
          contentAlignment = Alignment.Center,
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.PlayArrow,
              contentDescription = "Video thumbnail",
              tint = Accent,
              modifier = Modifier.size(28.dp),
            )
            Text(
              text = "Video",
              color = Accent,
              style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
            )
          }
        }
      } else if (imageBitmap != null) {
        Image(
          bitmap = imageBitmap!!,
          contentDescription = "Media thumbnail",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
      }

      if (selected) {
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp),
        ) {
          CircleIconAction(
            size = 30.dp,
            icon = {
              Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = "Share media thumbnail",
                tint = Accent,
              )
            },
            onClick = onShare,
          )
        }
      }
    }
  }
}

@Composable
private fun VideoSurface(
  uriString: String,
  modifier: Modifier = Modifier,
) {
  val uri = remember(uriString) { uriString.toUri() }

  AndroidView(
    modifier = modifier,
    factory = { ctx ->
      android.widget.VideoView(ctx).apply {
        tag = uriString
        setVideoURI(uri)
        setOnPreparedListener { mediaPlayer ->
          mediaPlayer.isLooping = true
          start()
        }
      }
    },
    update = { videoView ->
      if (videoView.tag != uriString) {
        videoView.tag = uriString
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { mediaPlayer ->
          mediaPlayer.isLooping = true
          videoView.start()
        }
      }
    },
  )
}

@Composable
private fun DetailsEntryPopup(
  vendorName: String,
  onVendorNameChange: (String) -> Unit,
  phone: String,
  onPhoneChange: (String) -> Unit,
  email: String,
  onEmailChange: (String) -> Unit,
  landingPrice: String,
  onLandingPriceChange: (String) -> Unit,
  selectedDate: String,
  onDateSelected: (String) -> Unit,
  showCalendar: Boolean,
  onCalendarClick: () -> Unit,
  onClose: () -> Unit,
  onConfirm: () -> Unit,
  fontFamily: FontFamily,
  modifier: Modifier = Modifier,
) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Surface(
      modifier = modifier
        .shadow(
          elevation = 18.dp,
          shape = CardShape,
          ambientColor = SoftGreyShadow,
          spotColor = SoftGreyShadow,
        ),
      shape = CardShape,
      color = Color.White,
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        DetailsTextBox(
          value = vendorName,
          onValueChange = onVendorNameChange,
          label = "Vendor's name",
          fontFamily = fontFamily,
          keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
          onConfirm = onConfirm,
        )
        DetailsTextBox(
          value = phone,
          onValueChange = onPhoneChange,
          label = "phone",
          fontFamily = fontFamily,
          keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
          onConfirm = onConfirm,
        )
        DetailsTextBox(
          value = email,
          onValueChange = onEmailChange,
          label = "email",
          fontFamily = fontFamily,
          keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
          onConfirm = onConfirm,
        )
        DetailsTextBox(
          value = landingPrice,
          onValueChange = onLandingPriceChange,
          label = "landing price",
          fontFamily = fontFamily,
          keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
          onConfirm = onConfirm,
          prefix = "₹",
        )

        if (showCalendar) {
          ContemporaryCalendar(
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            fontFamily = fontFamily,
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            CircleIconAction(
              size = 46.dp,
              icon = {
                Icon(
                  imageVector = Icons.Filled.CalendarMonth,
                  contentDescription = "Calendar",
                  tint = Accent,
                )
              },
              onClick = onCalendarClick,
            )
            Text(
              text = if (selectedDate.isBlank()) "Select date" else selectedDate,
              color = Ink.copy(alpha = 0.72f),
              style = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Thin,
                fontSize = 18.sp,
                lineHeight = 20.sp,
              ),
            )
          }
          CircleIconAction(
            size = 46.dp,
            icon = {
              Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close details",
                tint = Accent,
              )
            },
            onClick = onClose,
          )
        }
      }
    }
  }
}

@Composable
private fun DetailsTextBox(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  fontFamily: FontFamily,
  keyboardType: androidx.compose.ui.text.input.KeyboardType,
  onConfirm: () -> Unit,
  prefix: String = "",
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    color = Bg,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        text = label,
        color = Accent,
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Normal,
          fontSize = 14.sp,
          lineHeight = 16.sp,
        ),
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (prefix.isNotBlank()) {
          Text(
            text = prefix,
            color = Accent,
            style = TextStyle(
              fontFamily = fontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = 18.sp,
            ),
          )
          Spacer(modifier = Modifier.width(8.dp))
        }
        BasicTextField(
          value = value,
          onValueChange = onValueChange,
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          textStyle = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 20.sp,
            color = Ink,
          ),
          cursorBrush = SolidColor(Accent),
          keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done,
          ),
          keyboardActions = KeyboardActions(
            onDone = { onConfirm() },
          ),
        )
      }
    }
  }
}

@Composable
private fun PriceInputField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  fontFamily: FontFamily,
  prefix: String = "",
  suffix: String = "",
  readOnly: Boolean = false,
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = CardShape,
    color = Bg,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        text = label,
        color = Accent,
        style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
      )
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (prefix.isNotBlank()) {
          Text(prefix, color = Accent, style = TextStyle(fontFamily = fontFamily, fontSize = 18.sp))
          Spacer(modifier = Modifier.width(8.dp))
        }
        BasicTextField(
          value = value,
          onValueChange = onValueChange,
          modifier = Modifier.weight(1f),
          singleLine = true,
          textStyle = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 18.sp, color = Ink),
          cursorBrush = SolidColor(Accent),
          readOnly = readOnly,
          keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
        )
        if (suffix.isNotBlank()) {
          Spacer(modifier = Modifier.width(8.dp))
          Text(suffix, color = Accent, style = TextStyle(fontFamily = fontFamily, fontSize = 18.sp))
        }
      }
    }
  }
}

@Composable
private fun BuildProductDropdown(
  selectedValue: String,
  onClick: () -> Unit,
  fontFamily: FontFamily,
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = CardShape,
    color = Bg,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        text = "Build Product",
        color = Accent,
        style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = if (selectedValue.isBlank()) "Choose pots or supplies" else selectedValue,
          color = Ink,
          style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 18.sp),
        )
        Icon(
          imageVector = Icons.Filled.ArrowDropDown,
          contentDescription = "Select build product",
          tint = Accent,
        )
      }
    }
  }
}

@Composable
private fun BuildProductDropdown(
  selectedValue: String,
  linkedValue: Int,
  onValueSelected: (String) -> Unit,
  fontFamily: FontFamily,
  expanded: Boolean,
  onExpandedChange: () -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = CardShape,
    color = Bg,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        text = "Build Product",
        color = Accent,
        style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
      )
      Box {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExpandedChange),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          val display = if (selectedValue.isBlank()) "Choose pots or supplies" else "${selectedValue.replaceFirstChar { it.uppercase() }}  •  ₹ ${formatAmount(linkedValue)}"
          Text(
            text = display,
            color = Ink,
            style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 18.sp),
          )
          Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = "Select build product",
            tint = Accent,
          )
        }
        DropdownMenu(
          expanded = expanded,
          onDismissRequest = onExpandedChange,
        ) {
          listOf("pots", "supplies").forEach { option ->
            DropdownMenuItem(
              text = {
                Text(
                  option.replaceFirstChar { it.uppercase() },
                  style = TextStyle(fontFamily = fontFamily),
                )
              },
              onClick = {
                onValueSelected(option)
                onExpandedChange()
              },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SelectedValueAdditionCard(
  selections: List<BuildProductSelection>,
  onEditClick: () -> Unit,
  fontFamily: FontFamily,
) {
  if (selections.isEmpty()) return

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = CardShape,
    color = Color(0x26E48A2A),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Selected value addition",
          color = Accent,
          style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp),
        )
        Text(
          text = "Edit",
          modifier = Modifier.clickable(onClick = onEditClick),
          color = Accent,
          style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp),
        )
      }
      selections.forEach { selection ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
        ) {
          Text(
            text = "${selection.item.category.label}: ${selection.item.name} x${selection.quantity}",
            modifier = Modifier.weight(1f),
            color = Ink.copy(alpha = 0.78f),
            style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = "\u20B9 ${formatAmount(selection.totalPrice)}",
            color = Ink,
            style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp),
          )
        }
      }
    }
  }
}

@Composable
private fun BuildProductPickerPopup(
  category: BuildProductCategory?,
  items: List<BuildProductItem>,
  onCategorySelected: (BuildProductCategory) -> Unit,
  onItemSelected: (BuildProductItem) -> Unit,
  onClose: () -> Unit,
  fontFamily: FontFamily,
  modifier: Modifier = Modifier,
) {
  val selectedCategory = category ?: BuildProductCategory.Pots
  val filteredItems = items.filter { it.category == selectedCategory }

  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Surface(modifier = modifier, shape = CardShape, color = Color(0xFFFDF1E4), tonalElevation = 0.dp, shadowElevation = 0.dp) {
      Column(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Text(
          text = "Price",
          color = Ink,
          style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Thin, fontSize = 24.sp),
        )
        BuildProductCategoryTabs(
          selectedCategory = selectedCategory,
          onCategorySelected = onCategorySelected,
          fontFamily = fontFamily,
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          filteredItems.forEach { item ->
            BuildProductCatalogRow(
              item = item,
              onClick = { onItemSelected(item) },
              fontFamily = fontFamily,
            )
          }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
          TextButton(onClick = onClose) { Text("Back", color = Ink.copy(alpha = 0.6f)) }
        }
      }
    }
  }
}

@Composable
private fun BuildProductQuantityPopup(
  item: BuildProductItem,
  quantity: String,
  onQuantityChange: (String) -> Unit,
  onDecrease: () -> Unit,
  onIncrease: () -> Unit,
  onConfirm: () -> Unit,
  onClose: () -> Unit,
  fontFamily: FontFamily,
  modifier: Modifier = Modifier,
) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Surface(modifier = modifier, shape = CardShape, color = Color(0xFFFDF1E4), tonalElevation = 0.dp, shadowElevation = 0.dp) {
      Column(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(text = "Price", modifier = Modifier.fillMaxWidth(), color = Ink, style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Thin, fontSize = 24.sp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color.White, CardShape),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = item.shortName,
            color = Accent,
            style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 42.sp),
          )
        }
        Text(text = item.name, color = Ink, style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp))
        Text(
          text = "\u20B9 ${item.price}",
          color = Ink,
          style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Thin, fontSize = 26.sp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
          QuantityButton("-", onDecrease, fontFamily)
          BasicTextField(
            value = quantity,
            onValueChange = onQuantityChange,
            singleLine = true,
            textStyle = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, textAlign = TextAlign.Center, color = Ink),
            modifier = Modifier.width(40.dp),
            cursorBrush = SolidColor(Accent),
            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
          )
          QuantityButton("+", onIncrease, fontFamily)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          TextButton(onClick = onClose) { Text("Back", color = Ink.copy(alpha = 0.6f)) }
          TextButton(onClick = onConfirm) { Text("OK", color = Accent) }
        }
      }
    }
  }
}

@Composable
private fun BuildProductSelectionsPopup(
  selections: List<BuildProductSelection>,
  onDeleteRequest: (BuildProductSelection) -> Unit,
  onClose: () -> Unit,
  fontFamily: FontFamily,
  modifier: Modifier = Modifier,
) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Surface(modifier = modifier, shape = CardShape, color = Color(0xFFFDF1E4), tonalElevation = 0.dp, shadowElevation = 0.dp) {
      Column(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Text(text = "Price", color = Ink, style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Thin, fontSize = 24.sp))
        Text(text = "Selected value addition", color = Accent, style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp))
        selections.forEach { selection ->
          Surface(shape = CardShape, color = Color.White, tonalElevation = 0.dp, shadowElevation = 0.dp) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(text = selection.item.name, color = Ink, style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp))
                Text(text = "${selection.item.category.label} x${selection.quantity}", color = Ink.copy(alpha = 0.42f), style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp))
              }
              Text(text = "\u20B9 ${formatAmount(selection.totalPrice)}", color = Accent, style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp))
              Spacer(modifier = Modifier.width(12.dp))
              Text(text = "Delete", modifier = Modifier.clickable { onDeleteRequest(selection) }, color = Accent, style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp))
            }
          }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          TextButton(onClick = onClose) { Text("Back", color = Ink.copy(alpha = 0.6f)) }
          TextButton(onClick = onClose) { Text("OK", color = Accent) }
        }
      }
    }
  }
}

@Composable
private fun DeleteBuildSelectionDialog(
  itemName: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  fontFamily: FontFamily,
  modifier: Modifier = Modifier,
) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Surface(modifier = modifier, shape = CardShape, color = Accent, tonalElevation = 0.dp, shadowElevation = 0.dp) {
      Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
      ) {
        Text(text = "Confirm to delete item :", color = Color.White, textAlign = TextAlign.Center, style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Thin, fontSize = 28.sp), modifier = Modifier.fillMaxWidth())
        Text(text = itemName, color = Color.White, textAlign = TextAlign.Center, style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 30.sp), modifier = Modifier.fillMaxWidth())
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          TextButton(onClick = onConfirm) { Text("Yes", color = Color.White) }
          TextButton(onClick = onDismiss) { Text("No", color = Color.White) }
        }
      }
    }
  }
}

@Composable
private fun BuildProductCategoryTabs(
  selectedCategory: BuildProductCategory,
  onCategorySelected: (BuildProductCategory) -> Unit,
  fontFamily: FontFamily,
) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
    BuildProductCategory.entries.forEach { category ->
      Surface(
        modifier = Modifier
          .weight(1f)
          .clickable { onCategorySelected(category) },
        shape = CardShape,
        color = if (category == selectedCategory) Accent.copy(alpha = 0.16f) else Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
      ) {
        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
          Text(text = category.label, color = Ink, style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp))
        }
      }
    }
  }
}

@Composable
private fun BuildProductCatalogRow(
  item: BuildProductItem,
  onClick: () -> Unit,
  fontFamily: FontFamily,
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = CardShape,
    color = Color.White,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .size(68.dp)
          .background(Accent.copy(alpha = 0.14f), CardShape),
        contentAlignment = Alignment.Center,
      ) {
        Text(text = item.shortName, color = Accent, style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp))
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(text = item.name, color = Ink, style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp))
        Text(text = "Stock ${item.stock}", color = Ink.copy(alpha = 0.42f), style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp))
        Text(
          text = "\u20B9 ${item.price}",
          color = Ink,
          style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Thin, fontSize = 18.sp),
        )
      }
      Text(text = ">", color = Accent, style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp))
    }
  }
}

@Composable
private fun CheckoutGrid(
  finalPrice: Double,
  beforeMarkup: Double,
  landingPrice: Double,
  quantityValue: Double,
  discountedUnitPrice: Double,
  discountTotal: Double,
  priceTotal: Double,
  profitLoss: Double,
  discountPercent: Double,
  fontFamily: FontFamily,
) {
  val valueTone = Color(0xFF3F3F3F)
  val labelTone = Accent
  val softGrayTone = Color(0xFF6F6F6F)
  val lightGrayTone = Color(0xFFB9B9B9)
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = CardShape,
    color = Color.Transparent,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        CheckoutCell(
          text = "PRICE",
          fontFamily = fontFamily,
          modifier = Modifier.weight(0.9f),
          emphasize = true,
          align = TextAlign.Start,
          tone = labelTone,
        )
        CheckoutCell(
          text = "\u20B9${formatAmount(priceTotal)}",
          fontFamily = fontFamily,
          modifier = Modifier.weight(1.8f),
          tone = valueTone,
          large = true,
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        CheckoutCell("0%", fontFamily, Modifier.weight(1f), tone = valueTone)
        CheckoutCell("\u20B9 ${formatAmount(finalPrice)}", fontFamily, Modifier.weight(1f), tone = valueTone)
        CheckoutCell("\u20B9 ${formatAmount(finalPrice)}", fontFamily, Modifier.weight(1f), tone = valueTone)
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        CheckoutCell("Discount", fontFamily, Modifier.weight(2f), emphasize = true, tone = labelTone)
        CheckoutCell(
          "\u20B9 ${formatAmount(discountTotal)}",
          fontFamily,
          Modifier.weight(1f),
          tone = AccentGreen,
          fontWeight = FontWeight.Normal,
        )
      }

      CheckoutCell("MRP", fontFamily, Modifier.fillMaxWidth(), emphasize = true, align = TextAlign.Start, tone = labelTone)

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        CheckoutCell("Q", fontFamily, Modifier.weight(1f), emphasize = true, tone = labelTone)
        CheckoutCell(
          "\u20B9 ${formatAmount(beforeMarkup)}",
          fontFamily,
          Modifier.weight(1f),
          emphasize = true,
          tone = Accent,
          fontWeight = FontWeight.Normal,
        )
        CheckoutCell("%", fontFamily, Modifier.weight(1f), emphasize = true, tone = labelTone)
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        CheckoutCell(formatEditableDecimal(quantityValue), fontFamily, Modifier.weight(1f), tone = valueTone)
        CheckoutCell("\u20B9 ${formatAmount(discountedUnitPrice)}", fontFamily, Modifier.weight(1f), tone = valueTone)
        CheckoutCell("${formatEditableDecimal(discountPercent)}%", fontFamily, Modifier.weight(1f), tone = valueTone)
      }

      CheckoutCell("COST", fontFamily, Modifier.fillMaxWidth(), emphasize = true, align = TextAlign.Start, tone = softGrayTone)

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        CheckoutCell("Q", fontFamily, Modifier.weight(1f), emphasize = true, tone = labelTone)
        CheckoutCell(
          "\u20B9 ${formatAmount(landingPrice)}",
          fontFamily,
          Modifier.weight(1f),
          emphasize = true,
          tone = lightGrayTone,
        )
        CheckoutCell("P&L", fontFamily, Modifier.weight(1f), emphasize = true, tone = lightGrayTone)
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        CheckoutCell(formatEditableDecimal(quantityValue), fontFamily, Modifier.weight(1f), tone = valueTone)
        CheckoutCell("\u20B9 ${formatAmount(landingPrice)}", fontFamily, Modifier.weight(1f), tone = valueTone)
        CheckoutCell("\u20B9 ${formatAmount(profitLoss)}", fontFamily, Modifier.weight(1f), tone = valueTone)
      }
    }
  }
}

@Composable
private fun CheckoutCell(
  text: String,
  fontFamily: FontFamily,
  modifier: Modifier,
  tone: Color = Ink,
  emphasize: Boolean = false,
  large: Boolean = false,
  align: TextAlign = TextAlign.Center,
  fontWeight: FontWeight? = null,
) {
  Surface(
    modifier = modifier,
    shape = CardShape,
    color = Color.White,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Text(
      text = text,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = if (large) 18.dp else 14.dp),
      textAlign = align,
      color = tone,
      style = TextStyle(
        fontFamily = fontFamily,
        fontWeight = fontWeight ?: if (emphasize) FontWeight.Normal else FontWeight.Thin,
        fontSize = if (large) 32.sp else 18.sp,
        lineHeight = if (large) 36.sp else 20.sp,
      ),
    )
  }
}

@Composable
private fun PopupValueEditor(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  fontFamily: FontFamily,
  modifier: Modifier = Modifier,
  prefix: String = "",
  suffix: String = "",
) {
  Surface(
    modifier = modifier,
    shape = CardShape,
    color = Accent,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = label,
        color = Color.White.copy(alpha = 0.86f),
        style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp),
      )
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (prefix.isNotBlank()) {
          Text(prefix, color = Color.White, style = TextStyle(fontFamily = fontFamily, fontSize = 18.sp))
          Spacer(modifier = Modifier.width(6.dp))
        }
        BasicTextField(
          value = value,
          onValueChange = onValueChange,
          modifier = Modifier.weight(1f),
          singleLine = true,
          textStyle = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 20.sp, color = Color.White),
          cursorBrush = SolidColor(Color.White),
          keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
        )
        if (suffix.isNotBlank()) {
          Spacer(modifier = Modifier.width(6.dp))
          Text(suffix, color = Color.White, style = TextStyle(fontFamily = fontFamily, fontSize = 18.sp))
        }
      }
    }
  }
}

@Composable
private fun ContemporaryCalendar(
  selectedDate: String,
  onDateSelected: (String) -> Unit,
  fontFamily: FontFamily,
) {
  val today = remember { java.util.Calendar.getInstance() }
  var displayMonth by remember { mutableIntStateOf(today.get(java.util.Calendar.MONTH)) }
  var displayYear by remember { mutableIntStateOf(today.get(java.util.Calendar.YEAR)) }
  val monthName = java.text.DateFormatSymbols().months[displayMonth]
  val currentDay = today.get(java.util.Calendar.DAY_OF_MONTH)
  val currentMonth = today.get(java.util.Calendar.MONTH)
  val currentYear = today.get(java.util.Calendar.YEAR)
  val firstDay = remember(displayMonth, displayYear) {
    java.util.Calendar.getInstance().apply {
      set(java.util.Calendar.YEAR, displayYear)
      set(java.util.Calendar.MONTH, displayMonth)
      set(java.util.Calendar.DAY_OF_MONTH, 1)
    }
  }
  val daysInMonth = firstDay.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
  val leadingBlanks = firstDay.get(java.util.Calendar.DAY_OF_WEEK) - 1
  val cells = List(leadingBlanks) { 0 } + (1..daysInMonth).toList()

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = CardShape,
    color = Bg,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        CircleIconAction(
          size = 34.dp,
          icon = {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
              contentDescription = "Previous month",
              tint = Accent,
            )
          },
          onClick = {
            if (displayMonth == 0) {
              displayMonth = 11
              displayYear -= 1
            } else {
              displayMonth -= 1
            }
          },
        )
        Text(
          text = "$monthName $displayYear",
          color = Ink,
          style = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Thin,
            fontSize = 20.sp,
            lineHeight = 22.sp,
          ),
        )
        CircleIconAction(
          size = 34.dp,
          icon = {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
              contentDescription = "Next month",
              tint = Accent,
            )
          },
          onClick = {
            if (displayMonth == 11) {
              displayMonth = 0
              displayYear += 1
            } else {
              displayMonth += 1
            }
          },
        )
      }
      listOf("S", "M", "T", "W", "T", "F", "S").forEachIndexed { index, _ ->
        if (index == 0) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
              Text(
                text = day,
                modifier = Modifier.width(28.dp),
                color = Ink.copy(alpha = 0.42f),
                textAlign = TextAlign.Center,
                style = TextStyle(
                  fontFamily = fontFamily,
                  fontWeight = FontWeight.Thin,
                  fontSize = 12.sp,
                ),
              )
            }
          }
        }
      }
      cells.chunked(7).forEach { week ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          week.forEach { day ->
            val dateLabel = if (day > 0) formatCalendarDate(day, displayMonth, displayYear) else ""
            val isSelected = day > 0 && dateLabel == selectedDate
            val isToday = day == currentDay && displayMonth == currentMonth && displayYear == currentYear
            Box(
              modifier = Modifier
                .size(28.dp)
                .background(
                  when {
                    isSelected -> Accent
                    isToday -> Accent.copy(alpha = 0.18f)
                    else -> Color.Transparent
                  },
                  CircleShape,
                )
                .clickable(enabled = day > 0) {
                  onDateSelected(dateLabel)
                },
              contentAlignment = Alignment.Center,
            ) {
              if (day > 0) {
                Text(
                  text = day.toString(),
                  color = if (isSelected) Color.White else Ink,
                  textAlign = TextAlign.Center,
                  style = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Thin,
                    fontSize = 13.sp,
                  ),
                )
              }
            }
          }
          repeat(7 - week.size) {
            Spacer(modifier = Modifier.size(28.dp))
          }
        }
      }
    }
  }
}

@Composable
private fun UploadIconOption(
  icon: @Composable () -> Unit,
  size: Dp = 58.dp,
  onClick: () -> Unit,
) {
  CircleIconAction(
    size = size,
    icon = icon,
    backgroundColor = Color.Transparent,
    shadowElevation = 0.dp,
    onClick = onClick,
  )
}

@Composable
private fun UploadOption(
  title: String,
  fontFamily: FontFamily,
  onClick: () -> Unit,
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .border(1.dp, Color.White.copy(alpha = 0.35f), CardShape),
    shape = CardShape,
    color = UploadPill,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 13.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = title,
        color = Color.White.copy(alpha = 0.98f),
        textAlign = TextAlign.Center,
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Normal,
          fontSize = 23.sp,
        ),
      )
    }
  }
}

@Composable
private fun ActionStatCard(
  label: String,
  value: String,
  onClick: () -> Unit,
  modifier: Modifier,
  fontFamily: FontFamily,
  cardHeight: Dp,
  valueFontSize: androidx.compose.ui.unit.TextUnit,
  valueLineHeight: androidx.compose.ui.unit.TextUnit,
) {
  val titleFontSize = 22.75.sp
  Surface(
    modifier = modifier
      .height(cardHeight)
      .shadow(
        elevation = 12.dp,
        shape = CardShape,
        ambientColor = SoftGreyShadow,
        spotColor = SoftGreyShadow,
      )
      .clickable(onClick = onClick),
    shape = CardShape,
    color = Accent,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = label,
        color = Color.White.copy(alpha = 0.88f),
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Thin,
          fontSize = titleFontSize,
        ),
      )
      Text(
        text = value,
        color = Color.White,
        maxLines = 1,
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Thin,
          fontSize = valueFontSize,
          lineHeight = valueLineHeight,
        ),
      )
    }
  }
}

@Composable
private fun EditableStatCard(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier,
  fontFamily: FontFamily,
  cardHeight: Dp,
  valueFontSize: androidx.compose.ui.unit.TextUnit,
  valueLineHeight: androidx.compose.ui.unit.TextUnit,
  numeric: Boolean = false,
  prefix: String = "",
) {
  val titleFontSize = 22.75.sp
  Surface(
    modifier = modifier
      .height(cardHeight)
      .shadow(
        elevation = 12.dp,
        shape = CardShape,
        ambientColor = SoftGreyShadow,
        spotColor = SoftGreyShadow,
      ),
    shape = CardShape,
    color = Accent,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    val valueStyle = TextStyle(
      fontFamily = fontFamily,
      fontWeight = FontWeight.Thin,
      fontSize = valueFontSize,
      lineHeight = valueLineHeight,
      color = Color.White,
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = label,
        color = Color.White.copy(alpha = 0.88f),
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Thin,
          fontSize = titleFontSize,
        ),
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(cardHeight * 0.52f),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (prefix.isNotEmpty()) {
          Text(
            text = prefix,
            color = Color.White,
            style = valueStyle,
          )
        }

        BasicTextField(
          value = value,
          onValueChange = onValueChange,
          modifier = Modifier.weight(1f),
          singleLine = true,
          textStyle = valueStyle,
          cursorBrush = SolidColor(Color.White),
          keyboardOptions = KeyboardOptions(
            keyboardType = if (numeric) androidx.compose.ui.text.input.KeyboardType.Number else androidx.compose.ui.text.input.KeyboardType.Text,
          ),
        )
      }
    }
  }
}

@Composable
private fun ReadonlyStatCard(
  label: String,
  value: String,
  modifier: Modifier,
  fontFamily: FontFamily,
  cardHeight: Dp,
  valueFontSize: androidx.compose.ui.unit.TextUnit,
  valueLineHeight: androidx.compose.ui.unit.TextUnit,
) {
  Surface(
    modifier = modifier
      .height(cardHeight)
      .shadow(
        elevation = 12.dp,
        shape = CardShape,
        ambientColor = SoftGreyShadow,
        spotColor = SoftGreyShadow,
      ),
    shape = CardShape,
    color = Accent,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 10.dp, vertical = 6.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = label,
        color = Color.White.copy(alpha = 0.88f),
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Thin,
          fontSize = 13.sp,
        ),
      )
      Text(
        text = value,
        color = Color.White,
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Thin,
          fontSize = valueFontSize,
          lineHeight = valueLineHeight,
        ),
      )
    }
  }
}

@Composable
private fun InlineEditor(
  value: String,
  onValueChange: (String) -> Unit,
  fontFamily: FontFamily,
  fontSize: androidx.compose.ui.unit.TextUnit,
  textAlign: TextAlign,
  widthFraction: Float,
  clearOnFirstTap: Boolean = false,
) {
  var clearedOnFocus by remember(clearOnFirstTap) { mutableStateOf(false) }
  Box(
    modifier = Modifier
      .fillMaxWidth(widthFraction)
      .height(52.dp),
    contentAlignment = Alignment.Center,
  ) {
    BasicTextField(
      value = value,
      onValueChange = onValueChange,
      modifier = Modifier
        .fillMaxWidth()
        .onFocusChanged { focusState ->
          if (focusState.isFocused && clearOnFirstTap && !clearedOnFocus && value == "Product Name") {
            onValueChange("")
            clearedOnFocus = true
          }
        },
      singleLine = true,
      textStyle = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = fontSize,
        lineHeight = 40.sp,
        textAlign = textAlign,
        color = Ink,
      ),
      cursorBrush = SolidColor(Accent),
    )
  }
}

@Composable
private fun CircleIconAction(
  modifier: Modifier = Modifier,
  size: Dp,
  icon: @Composable () -> Unit,
  borderColor: Color = Color.Transparent,
  borderWidth: Dp = 0.dp,
  backgroundColor: Color = Color.White,
  shadowElevation: Dp = 10.dp,
  onClick: () -> Unit,
) {
  Surface(
    modifier = modifier
      .size(size)
      .shadow(
        elevation = shadowElevation,
        shape = CircleShape,
        ambientColor = SoftGreyShadow,
        spotColor = SoftGreyShadow,
      )
      .clickable(onClick = onClick),
    shape = CircleShape,
    color = backgroundColor,
    border = if (borderWidth > 0.dp) {
      androidx.compose.foundation.BorderStroke(borderWidth, borderColor)
    } else {
      androidx.compose.foundation.BorderStroke(1.dp, PanelBorder.copy(alpha = 0.9f))
    },
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center,
    ) {
      icon()
    }
  }
}

private data class ProductDetail(
  val name: String,
  val batch: String,
  val sale: String,
  val rent: String,
  val details: String,
  val price: String,
  val checkout: String = "",
  val imageUri: String? = null,
  val mediaUris: List<String> = emptyList(),
  val templatePrimaryTitle: String = "",
  val templatePrimaryDescription: String = "",
  val templateSecondaryTitle: String = "",
  val templateSecondaryDescription: String = "",
  val templateContact: String = "",
)

private enum class OtcCarouselCategory {
  Plant,
  Pots,
  Supplies,
}

private data class OtcCarouselItem(
  val id: String,
  val category: OtcCarouselCategory,
  val name: String,
  val shortName: String,
  val price: String,
  val landingPrice: String,
  val imageUri: String? = null,
)

private fun otcCatalog(): List<OtcCarouselItem> {
  val liveItems = LiveInventoryCatalog.inventoryByDestination
    .filterKeys { destination ->
      destination == DashboardDestination.Plants ||
        destination == DashboardDestination.Pots ||
        destination == DashboardDestination.Supplies
    }
    .values
    .flatten()
    .map { item ->
      OtcCarouselItem(
        id = item.id,
        category = when (item.category) {
          DashboardDestination.Plants -> OtcCarouselCategory.Plant
          DashboardDestination.Pots -> OtcCarouselCategory.Pots
          DashboardDestination.Supplies -> OtcCarouselCategory.Supplies
          DashboardDestination.OTC -> OtcCarouselCategory.Plant
          DashboardDestination.Docs -> OtcCarouselCategory.Plant
          DashboardDestination.Art -> OtcCarouselCategory.Plant
        },
        name = item.name,
        shortName = item.name
          .split(" ")
          .filter { it.isNotBlank() }
          .take(2)
          .joinToString("") { it.take(1).uppercase(Locale.US) }
          .ifBlank { item.name.take(2).uppercase(Locale.US) },
        price = item.price.orZero(),
        landingPrice = item.checkout.orZero(),
        imageUri = item.mediaUris.firstOrNull() ?: item.imageUri,
      )
    }

  if (liveItems.isNotEmpty()) return liveItems

  return listOf(
    OtcCarouselItem("plant-1", OtcCarouselCategory.Plant, "Bamboo 3 Lucky", "B3", "1200", "820"),
    OtcCarouselItem("plant-2", OtcCarouselCategory.Plant, "Money Plant", "MP", "650", "430"),
    OtcCarouselItem("plant-3", OtcCarouselCategory.Plant, "Snake Plant", "SP", "850", "580"),
    OtcCarouselItem("plant-4", OtcCarouselCategory.Plant, "Areca Palm", "AP", "1400", "960"),
    OtcCarouselItem("plant-5", OtcCarouselCategory.Plant, "Jade Plant", "JP", "520", "340"),
    OtcCarouselItem("plant-6", OtcCarouselCategory.Plant, "Peace Lily", "PL", "950", "650"),
    OtcCarouselItem("pots-1", OtcCarouselCategory.Pots, "Terracotta Pot", "TP", "900", "610"),
    OtcCarouselItem("pots-2", OtcCarouselCategory.Pots, "Ceramic Pot", "CP", "750", "520"),
    OtcCarouselItem("pots-3", OtcCarouselCategory.Pots, "Hanging Pot", "HP", "680", "470"),
    OtcCarouselItem("pots-4", OtcCarouselCategory.Pots, "Table Pot", "TB", "420", "290"),
    OtcCarouselItem("pots-5", OtcCarouselCategory.Pots, "Round Pot", "RP", "560", "380"),
    OtcCarouselItem("pots-6", OtcCarouselCategory.Pots, "Tall Planter", "TL", "1100", "760"),
    OtcCarouselItem("supplies-1", OtcCarouselCategory.Supplies, "Garden Mix", "GM", "640", "440"),
    OtcCarouselItem("supplies-2", OtcCarouselCategory.Supplies, "Cocopeat", "CP", "320", "210"),
    OtcCarouselItem("supplies-3", OtcCarouselCategory.Supplies, "Fertilizer", "FT", "450", "310"),
    OtcCarouselItem("supplies-4", OtcCarouselCategory.Supplies, "Neem Spray", "NS", "360", "250"),
    OtcCarouselItem("supplies-5", OtcCarouselCategory.Supplies, "Plant Food", "PF", "390", "270"),
    OtcCarouselItem("supplies-6", OtcCarouselCategory.Supplies, "Pebbles", "PB", "280", "190"),
  )
}

private fun buildProductCatalog(): List<BuildProductItem> {
  return listOf(
    BuildProductItem("pots-ceramic-oak-green", BuildProductCategory.Pots, "Ceramic oak green", 74, 150, "COG"),
    BuildProductItem("pots-ceramic-sandy-aqua", BuildProductCategory.Pots, "Ceramic sandy aqua", 20, 180, "CSA"),
    BuildProductItem("pots-ceramic-sandy-brown", BuildProductCategory.Pots, "Ceramic sandy brown", 8, 270, "CSB"),
    BuildProductItem("pots-clay-oak", BuildProductCategory.Pots, "Clay oak", 59, 125, "CO"),
    BuildProductItem("pots-plastic-oggy-white", BuildProductCategory.Pots, "Plastic oggy white", 198, 140, "POW"),
    BuildProductItem("supplies-bridge", BuildProductCategory.Supplies, "Bridge", 17, 380, "BR"),
    BuildProductItem("supplies-fairy-resting", BuildProductCategory.Supplies, "Fairy resting", 12, 250, "FR"),
    BuildProductItem("supplies-pebbles-mix", BuildProductCategory.Supplies, "Pebbles mix", 31, 120, "PM"),
    BuildProductItem("supplies-mini-fence", BuildProductCategory.Supplies, "Mini fence", 22, 90, "MF"),
  )
}

private fun currentDateLabel(): String {
  val today = java.util.Calendar.getInstance()
  return formatCalendarDate(
    day = today.get(java.util.Calendar.DAY_OF_MONTH),
    month = today.get(java.util.Calendar.MONTH),
    year = today.get(java.util.Calendar.YEAR),
  )
}

private fun formatCalendarDate(
  day: Int,
  month: Int,
  year: Int,
): String {
  val shortMonth = java.text.DateFormatSymbols().shortMonths[month]
  return "$day $shortMonth $year"
}

private fun detailFor(destination: DashboardDestination): ProductDetail {
  return when (destination) {
    DashboardDestination.Plants -> ProductDetail(
      name = "Product Name",
      batch = "",
      sale = "",
      rent = "",
      details = "",
      price = "",
    )
    DashboardDestination.Pots -> ProductDetail(
      name = "Product Name",
      batch = "",
      sale = "",
      rent = "",
      details = "",
      price = "",
    )
    DashboardDestination.Supplies -> ProductDetail(
      name = "Product Name",
      batch = "",
      sale = "",
      rent = "",
      details = "",
      price = "",
    )
    DashboardDestination.OTC -> ProductDetail(
      name = "Product Name",
      batch = "",
      sale = "",
      rent = "",
      details = "",
      price = "",
    )
    DashboardDestination.Docs -> ProductDetail(
      name = "Product Name",
      batch = "",
      sale = "",
      rent = "",
      details = "",
      price = "",
    )
    DashboardDestination.Art -> ProductDetail(
      name = "Product Name",
      batch = "",
      sale = "",
      rent = "",
      details = "",
      price = "",
    )
  }
}

private fun formatAmount(value: Int): String {
  return "%,d".format(value.coerceAtLeast(0))
}

private fun formatAmount(value: Double): String {
  val normalized = if (kotlin.math.abs(value) < 0.005) 0.0 else value
  return java.text.DecimalFormat("#,##0.##").format(normalized)
}

private fun formatEditableDecimal(value: Double): String {
  val normalized = if (kotlin.math.abs(value) < 0.005) 0.0 else value
  return java.text.DecimalFormat("0.##").format(normalized)
}

private fun parseDecimalInput(value: String): Double {
  return value.toDoubleOrNull() ?: 0.0
}

private fun sanitizeDecimalEntry(
  raw: String,
  allowNegative: Boolean = false,
): String {
  if (raw.isBlank()) return ""
  val builder = StringBuilder()
  var hasDecimal = false
  raw.forEachIndexed { index, char ->
    when {
      char.isDigit() -> builder.append(char)
      char == '.' && !hasDecimal -> {
        if (builder.isEmpty() || builder.toString() == "-") {
          builder.append("0")
        }
        builder.append('.')
        hasDecimal = true
      }
      allowNegative && char == '-' && index == 0 -> builder.append('-')
    }
  }
  return builder.toString()
}

private fun OpenCartRow.baseAmount(): Int {
  return items.sumOf { it.mrp * it.quantity }
}

private fun OpenCartRow.displayAmount(): Int {
  return customAmount ?: baseAmount()
}

private fun buildInvoiceFileName(deal: SavedDeal): String {
  val rawLabel = buildString {
    append("ASHOKVATIKA_invoice_")
    append(
      deal.transactionCode
        .ifBlank { deal.clientName.ifBlank { "deal" } }
        .trim()
        .replace(Regex("[^A-Za-z0-9_-]+"), "_")
        .trim('_'),
    )
    append('_')
    append(System.currentTimeMillis())
  }
  return "${rawLabel.ifBlank { "ASHOKVATIKA_invoice" }}.pdf"
}

@Composable
private fun rememberDecodedImage(
  uriString: String?,
  maxDimensionPx: Int = 1280,
): State<ImageBitmap?> {
  val context = LocalContext.current
  return produceState<ImageBitmap?>(initialValue = null, uriString, maxDimensionPx) {
    value = if (uriString.isNullOrBlank()) {
      null
    } else {
      withContext(Dispatchers.IO) {
        decodeImageBitmap(context, uriString.toUri(), maxDimensionPx)
      }
    }
  }
}

private fun decodeImageBitmap(
  context: android.content.Context,
  uri: Uri,
  maxDimensionPx: Int,
): ImageBitmap? {
  return try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      val source = ImageDecoder.createSource(context.contentResolver, uri)
      ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        decoder.isMutableRequired = false
        if (maxDimensionPx > 0) {
          val width = info.size.width.coerceAtLeast(1)
          val height = info.size.height.coerceAtLeast(1)
          val largestEdge = maxOf(width, height)
          if (largestEdge > maxDimensionPx) {
            val scale = maxDimensionPx.toFloat() / largestEdge.toFloat()
            decoder.setTargetSize(
              (width * scale).toInt().coerceAtLeast(1),
              (height * scale).toInt().coerceAtLeast(1),
            )
          }
        }
      }.asImageBitmap()
    } else {
      val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, boundsOptions)
      }
      val sampledOptions = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(
          sourceWidth = boundsOptions.outWidth,
          sourceHeight = boundsOptions.outHeight,
          maxDimensionPx = maxDimensionPx,
        )
      }
      context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, sampledOptions)?.asImageBitmap()
      }
    }
  } catch (_: IOException) {
    null
  } catch (_: SecurityException) {
    null
  }
}

private fun calculateInSampleSize(
  sourceWidth: Int,
  sourceHeight: Int,
  maxDimensionPx: Int,
): Int {
  if (sourceWidth <= 0 || sourceHeight <= 0 || maxDimensionPx <= 0) return 1
  val largestEdge = maxOf(sourceWidth, sourceHeight)
  var sampleSize = 1
  while (largestEdge / sampleSize > maxDimensionPx) {
    sampleSize *= 2
  }
  return sampleSize.coerceAtLeast(1)
}

@Composable
private fun DashboardTile(
  title: String,
  fontFamily: FontFamily,
  tileWidth: Dp,
  icon: BrandButtonIcon?,
  delayMillis: Int,
  animateTiles: Boolean,
  baseColor: Color = Accent,
  onClick: () -> Unit,
) {
  val titleFontSize = when {
    tileWidth >= 270.dp && icon == null -> 36.sp
    tileWidth >= 250.dp && icon == null -> 33.sp
    tileWidth >= 250.dp -> 31.sp
    icon == null -> 30.sp
    else -> 28.sp
  }
  val tileAlpha by animateFloatAsState(
    targetValue = if (animateTiles) 1f else 0f,
    animationSpec = tween(durationMillis = 780, delayMillis = delayMillis, easing = FastOutSlowInEasing),
    label = "$title-tile-alpha",
  )
  val tileOffset by animateFloatAsState(
    targetValue = if (animateTiles) 0f else 68f,
    animationSpec = tween(durationMillis = 860, delayMillis = delayMillis, easing = FastOutSlowInEasing),
    label = "$title-tile-offset",
  )
  val tileScale by animateFloatAsState(
    targetValue = if (animateTiles) 1f else 0.92f,
    animationSpec = tween(durationMillis = 860, delayMillis = delayMillis, easing = FastOutSlowInEasing),
    label = "$title-tile-scale",
  )
  val titleAlpha by animateFloatAsState(
    targetValue = if (animateTiles) 1f else 0f,
    animationSpec = tween(durationMillis = 420, delayMillis = delayMillis + 540, easing = FastOutSlowInEasing),
    label = "$title-text-alpha",
  )
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val tileColor by animateColorAsState(
    targetValue = if (isPressed) AccentGreen else baseColor,
    animationSpec = tween(durationMillis = 180),
    label = "$title-tile-color",
  )

  Box(
    modifier = Modifier
      .width(tileWidth)
      .aspectRatio(1f)
      .graphicsLayer {
        translationY = tileOffset
        scaleX = tileScale
        scaleY = tileScale
      }
      .shadow(
        elevation = 18.dp,
        shape = CircleShape,
        ambientColor = TileShadow,
        spotColor = TileShadow,
      )
      .background(tileColor, CircleShape)
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick,
      ),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(if (icon == null) 0.dp else 8.dp),
    ) {
      if (icon != null) {
        BrandButtonGlyph(
          kind = icon,
          modifier = Modifier
            .size((tileWidth * 0.29f).coerceAtLeast(48.dp))
            .alpha(tileAlpha),
        )
      }

      Text(
        text = title,
        modifier = Modifier.alpha(titleAlpha * tileAlpha),
        color = Color.White,
        textAlign = TextAlign.Center,
        style = TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Thin,
          fontSize = titleFontSize,
          letterSpacing = 0.2.sp,
        ),
      )
    }
  }
}

private fun createCameraImageUri(
  context: android.content.Context,
): Uri {
  val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
  val imageFile = File(
    picturesDir,
    "ashokvatika_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg",
  )
  return FileProvider.getUriForFile(
    context,
    "${context.packageName}.fileprovider",
    imageFile,
  )
}

private fun isVideoUri(
  context: android.content.Context,
  uriString: String,
): Boolean {
  return try {
    val uri = uriString.toUri()
    val contentType = context.contentResolver.getType(uri).orEmpty()
    contentType.startsWith("video/") || uriString.substringAfterLast('.', "").lowercase(Locale.US) in setOf("mp4", "mov", "m4v", "avi", "mkv", "webm")
  } catch (_: Exception) {
    false
  }
}

private fun shareMediaUri(
  context: android.content.Context,
  uriString: String,
) {
  try {
    val uri = uriString.toUri()
    val mimeType = context.contentResolver.getType(uri) ?: if (isVideoUri(context, uriString)) "video/*" else "image/*"
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
      type = mimeType
      putExtra(android.content.Intent.EXTRA_STREAM, uri)
      addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(android.content.Intent.createChooser(intent, "Share media"))
  } catch (_: Exception) {
  }
}

private fun shareSelectedImages(
  context: android.content.Context,
  selectedImages: List<String>,
  platform: SharePlatformAction,
) {
  if (selectedImages.isEmpty()) return
  runCatching {
    val uris = selectedImages.map { it.toUri() }
    val mimeType = if (uris.size == 1) {
      context.contentResolver.getType(uris.first()) ?: "image/*"
    } else {
      "image/*"
    }
    val intent = if (uris.size == 1) {
      android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(android.content.Intent.EXTRA_STREAM, uris.first())
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
    } else {
      android.content.Intent(android.content.Intent.ACTION_SEND_MULTIPLE).apply {
        type = mimeType
        putParcelableArrayListExtra(
          android.content.Intent.EXTRA_STREAM,
          ArrayList(uris),
        )
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
    }

    val targetIntent = intent.apply {
      platform.packageName?.let { targetPackage ->
        `package` = targetPackage
      }
    }

    val launchIntent = if (platform.id == "more" || platform.packageName == null) {
      android.content.Intent.createChooser(targetIntent, "Share images")
    } else {
      targetIntent
    }
    context.startActivity(launchIntent)
  }
}

private fun InventoryItem.toProductDetail(): ProductDetail {
  return ProductDetail(
    name = name,
    batch = batch,
    sale = sale,
    rent = rent,
    details = details,
    price = price,
    checkout = checkout,
    imageUri = imageUri,
    mediaUris = mediaUris.ifEmpty { listOfNotNull(imageUri) },
    templatePrimaryTitle = templatePrimaryTitle,
    templatePrimaryDescription = templatePrimaryDescription,
    templateSecondaryTitle = templateSecondaryTitle,
    templateSecondaryDescription = templateSecondaryDescription,
    templateContact = templateContact,
  )
}

private fun buildInventoryItem(
  initialItem: InventoryItem?,
  title: DashboardDestination,
  productName: String,
  seed: ProductDetail,
  batchText: String,
  saleText: String,
  rentText: String,
  detailsText: String,
  selectedDetailsDate: String,
  unitPrice: Int,
  checkoutDisplay: String,
  selectedMediaUris: List<String>,
  templatePrimaryTitle: String,
  templatePrimaryDescription: String,
  templateSecondaryTitle: String,
  templateSecondaryDescription: String,
  templateContact: String,
  status: String,
): InventoryItem {
  val primaryMediaUri = selectedMediaUris.firstOrNull()
  return InventoryItem(
    id = initialItem?.id ?: "${title.name.lowercase(Locale.US)}-${System.currentTimeMillis()}",
    category = title,
    name = productName.ifBlank { seed.name },
    batch = batchText.ifBlank { "0" },
    sale = saleText.ifBlank { "0" },
    rent = rentText.ifBlank { "0" },
    details = if (detailsText.isBlank()) selectedDetailsDate else detailsText,
    price = unitPrice.toString(),
    checkout = checkoutDisplay.filter(Char::isDigit),
    imageUri = primaryMediaUri,
    mediaUris = selectedMediaUris,
    status = status,
    templatePrimaryTitle = productName.ifBlank { templatePrimaryTitle.ifBlank { seed.name } },
    templatePrimaryDescription = templatePrimaryDescription,
    templateSecondaryTitle = templateSecondaryTitle,
    templateSecondaryDescription = templateSecondaryDescription,
    templateContact = templateContact,
  )
}

private fun String?.orZero(): String {
  return if (this.isNullOrBlank()) "0" else this
}

@Composable
private fun rememberDashboardFont(): FontFamily {
  return FontFamily(
    Font(R.font.dm_sans_thin, weight = FontWeight.Thin),
    Font(R.font.dm_sans_regular, weight = FontWeight.Normal),
  )
}



