package com.ashokvatika

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ashokvatika.ui.AppRoot

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      App()
    }
  }
}

@Composable
private fun App() {
  val scheme = lightColorScheme(
    background = Color(0xFFF7F7F7),
    surface = Color(0xFFF7F7F7),
    onBackground = Color(0xFF141414),
    onSurface = Color(0xFF141414),
  )

  MaterialTheme(colorScheme = scheme) {
    Surface(color = scheme.background) {
      AppRoot()
    }
  }
}
