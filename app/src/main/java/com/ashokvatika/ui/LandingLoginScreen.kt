@file:Suppress("FunctionName")

package com.ashokvatika.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.ashokvatika.app.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashokvatika.ui.components.AshokvatikaLogo
import kotlinx.coroutines.launch

private val Bg = Color(0xFFF7F7F7)
private val Ink = Color(0xFF141414)
private val Accent = Color(0xFFF17C32)
private val LabelGreen = Color(0xFF57A46B)
private val FieldFill = Color(0xFFEFF1F2)
private const val AdminAlias = "admin"
private const val AdminPassword = "123"

@Composable
fun LandingLoginScreen(
  modifier: Modifier = Modifier,
  onLoggedIn: () -> Unit,
) {
  val dmSans = rememberDmSans()
  val keyboard = LocalSoftwareKeyboardController.current
  val scope = rememberCoroutineScope()

  var username by remember { mutableStateOf(AdminAlias) }
  var password by remember { mutableStateOf(AdminPassword) }
  var showUser by remember { mutableStateOf(true) }
  var showPass by remember { mutableStateOf(false) }
  var error by remember { mutableStateOf<String?>(null) }
  var isSubmitting by remember { mutableStateOf(false) }

  fun submit() {
    if (isSubmitting) return
    val loginId = username.trim()
    if (loginId.isBlank() || password.isBlank()) {
      error = "Use username admin and password 123."
      return
    }
    if (loginId.equals(AdminAlias, ignoreCase = true) && password == AdminPassword) {
      error = null
      keyboard?.hide()
      isSubmitting = true
      scope.launch {
        onLoggedIn()
        isSubmitting = false
      }
    } else {
      error = "Use username admin and password 123."
    }
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .background(Bg)
      .navigationBarsPadding()
      .imePadding(),
  ) {
    val contentWidth = minOf(maxWidth - 24.dp, 760.dp)
    val formWidth = minOf(contentWidth * 0.7f, 380.dp)
    val topSpacing = when {
      maxHeight > 1000.dp -> 108.dp
      maxHeight > 800.dp -> 84.dp
      maxHeight > 650.dp -> 66.dp
      else -> 48.dp
    }
    val formLift = when {
      maxHeight > 1000.dp -> 30.dp
      maxHeight > 800.dp -> 18.dp
      else -> 10.dp
    }
    val logoHeight = when {
      maxWidth > 1200.dp -> 180.dp
      maxWidth > 800.dp -> 156.dp
      maxWidth > 500.dp -> 132.dp
      else -> 116.dp
    }
    val brandTextSize = when {
      maxWidth > 1200.dp -> 72.sp
      maxWidth > 800.dp -> 64.sp
      maxWidth > 500.dp -> 56.sp
      else -> 44.sp
    }
    Box(
      modifier = Modifier
        .fillMaxSize()
        .clickable(
          indication = null,
          interactionSource = remember { MutableInteractionSource() },
        ) { keyboard?.hide() },
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier = Modifier
          .width(contentWidth)
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Spacer(modifier = Modifier.height(topSpacing))

        AshokvatikaLogo(
          modifier = Modifier
            .fillMaxWidth(0.6f)
            .padding(horizontal = 8.dp),
          textSize = brandTextSize,
          markSize = logoHeight * 0.54f,
        )

        Spacer(modifier = Modifier.height(38.dp + formLift))

        Column(
          modifier = Modifier
            .width(formWidth)
            .fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
          LoginField(
            label = "User name",
            labelImageRes = R.drawable.icon_username,
            labelImageSize = 34.dp,
            value = username,
            onValueChange = {
              username = it
              error = null
            },
            showValue = showUser,
            onToggleVisibility = { showUser = !showUser },
            dmSans = dmSans,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            enabled = !isSubmitting,
          )

          LoginField(
            label = "Password",
            labelImageRes = R.drawable.icon_password,
            labelImageSize = 42.dp,
            value = password,
            onValueChange = {
              password = it
              error = null
            },
            showValue = showPass,
            onToggleVisibility = { showPass = !showPass },
            dmSans = dmSans,
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done,
            onDone = ::submit,
            showConfirm = password.isNotBlank(),
            onConfirm = ::submit,
            enabled = !isSubmitting,
          )
        }

        if (error != null) {
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = error!!,
            color = Color(0xFFB00020),
            textAlign = TextAlign.Center,
            style = TextStyle(
              fontFamily = dmSans,
              fontWeight = FontWeight.SemiBold,
              fontSize = 14.sp,
            ),
          )
        }

        if (isSubmitting) {
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Opening admin panel...",
            color = LabelGreen,
            textAlign = TextAlign.Center,
            style = TextStyle(
              fontFamily = dmSans,
              fontWeight = FontWeight.SemiBold,
              fontSize = 14.sp,
            ),
          )
        }

        Spacer(modifier = Modifier.height(72.dp))
      }
    }
  }
}

@Composable
private fun LoginField(
  label: String,
  labelImageRes: Int? = null,
  labelImageSize: androidx.compose.ui.unit.Dp = 34.dp,
  value: String,
  onValueChange: (String) -> Unit,
  showValue: Boolean,
  onToggleVisibility: () -> Unit,
  dmSans: FontFamily,
  keyboardType: KeyboardType,
  imeAction: ImeAction,
  enabled: Boolean,
  onDone: (() -> Unit)? = null,
  showConfirm: Boolean = false,
  onConfirm: (() -> Unit)? = null,
) {
  if (labelImageRes != null) {
    Image(
      painter = painterResource(labelImageRes),
      contentDescription = label,
      contentScale = ContentScale.Fit,
      modifier = Modifier
        .size(labelImageSize),
    )
  } else {
    Text(
      text = label,
      color = LabelGreen,
      modifier = Modifier.fillMaxWidth(),
      style = TextStyle(
        fontFamily = dmSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
      ),
    )
  }

  TextField(
    value = value,
    onValueChange = onValueChange,
    singleLine = true,
    enabled = enabled,
    visualTransformation = if (showValue) VisualTransformation.None else PasswordVisualTransformation(),
    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
      keyboardType = keyboardType,
      imeAction = imeAction,
    ),
    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
      onDone = { onDone?.invoke() },
    ),
    modifier = Modifier
      .fillMaxWidth()
      .height(56.dp)
      .sizeIn(minHeight = 56.dp),
    shape = RoundedCornerShape(16.dp),
    trailingIcon = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (showConfirm && onConfirm != null) {
          IconButton(onClick = onConfirm, enabled = enabled) {
            Icon(
              imageVector = Icons.Filled.CheckCircle,
              contentDescription = "Confirm login",
              tint = Accent,
            )
          }
        }
        IconButton(onClick = onToggleVisibility, enabled = enabled) {
          Icon(
            imageVector = if (showValue) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
            contentDescription = "Toggle visibility",
            tint = LabelGreen,
          )
        }
      }
    },
    textStyle = TextStyle(
      fontFamily = dmSans,
      fontWeight = FontWeight.Thin,
      fontSize = 18.sp,
      lineHeight = 22.sp,
      color = Ink,
    ),
    colors = TextFieldDefaults.colors(
      focusedContainerColor = FieldFill,
      unfocusedContainerColor = FieldFill,
      disabledContainerColor = FieldFill,
      focusedIndicatorColor = Color.Transparent,
      unfocusedIndicatorColor = Color.Transparent,
      disabledIndicatorColor = Color.Transparent,
      cursorColor = Accent,
    ),
  )
}

@Composable
private fun rememberDmSans(): FontFamily {
  return FontFamily(
    Font(R.font.dm_sans_thin, weight = FontWeight.Thin),
    Font(R.font.dm_sans_regular, weight = FontWeight.Normal),
    Font(R.font.dm_sans_semibold, weight = FontWeight.SemiBold),
  )
}
