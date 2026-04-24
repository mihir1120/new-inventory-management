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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.ashokvatika.R
import com.ashokvatika.ui.components.AshokvatikaLogo

private val Bg = Color(0xFFF7F7F7)
private val Ink = Color(0xFF141414)
private val Accent = Color(0xFFF17C32)
private val LabelGreen = Color(0xFF57A46B)
private val FieldFill = Color(0xFFEFF1F2)

@Composable
fun LandingLoginScreen(
  modifier: Modifier = Modifier,
  onLoggedIn: () -> Unit,
) {
  val dmSans = rememberDmSans()
  val keyboard = LocalSoftwareKeyboardController.current

  var username by remember { mutableStateOf("admin") }
  var password by remember { mutableStateOf("123") }
  var showUser by remember { mutableStateOf(true) }
  var showPass by remember { mutableStateOf(false) }
  var error by remember { mutableStateOf<String?>(null) }

  fun submit() {
    if (username == "admin" && password == "123") {
      error = null
      keyboard?.hide()
      onLoggedIn()
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
    val poweredByHeight = when {
      maxWidth > 1200.dp -> 150.dp
      maxWidth > 800.dp -> 132.dp
      else -> 118.dp
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
          .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Spacer(modifier = Modifier.height(topSpacing))

        AshokvatikaLogo(
          modifier = Modifier
            .fillMaxWidth(0.96f)
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
          )

          LoginField(
            label = "Password",
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

        Spacer(modifier = Modifier.weight(1f, fill = true))
        Spacer(modifier = Modifier.height(72.dp))

        Image(
          painter = painterResource(R.drawable.logo_powered_by),
          contentDescription = "Powered by BrandStock",
          contentScale = ContentScale.Fit,
          modifier = Modifier
            .fillMaxWidth(0.46f)
            .height(poweredByHeight),
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
          text = "Forgot password ?\nprivacy policy",
          color = Accent,
          textAlign = TextAlign.Center,
          style = TextStyle(
            fontFamily = dmSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
          ),
        )

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun LoginField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  showValue: Boolean,
  onToggleVisibility: () -> Unit,
  dmSans: FontFamily,
  keyboardType: KeyboardType,
  imeAction: ImeAction,
  onDone: (() -> Unit)? = null,
  showConfirm: Boolean = false,
  onConfirm: (() -> Unit)? = null,
) {
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

  TextField(
    value = value,
    onValueChange = onValueChange,
    singleLine = true,
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
      .height(46.dp)
      .sizeIn(minHeight = 46.dp),
    shape = RoundedCornerShape(16.dp),
    trailingIcon = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (showConfirm && onConfirm != null) {
          IconButton(onClick = onConfirm) {
            Icon(
              imageVector = Icons.Filled.CheckCircle,
              contentDescription = "Confirm login",
              tint = Accent,
            )
          }
        }
        IconButton(onClick = onToggleVisibility) {
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
      fontWeight = FontWeight.Medium,
      fontSize = 15.sp,
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
    Font(R.font.dm_sans_regular, weight = FontWeight.Normal),
    Font(R.font.dm_sans_semibold, weight = FontWeight.SemiBold),
  )
}
