package com.ashokvatika

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.ashokvatika.config.AppExperimentManager
import com.ashokvatika.config.ReleaseRuntimeConfig
import com.ashokvatika.ui.AppRoot
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AppExperimentManager.initialize(applicationContext)
    lifecycleScope.launch {
      AppExperimentManager.fetchAndActivate()
    }
    if (ReleaseRuntimeConfig.screenshotsProtected) {
      window.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE,
      )
    }
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
