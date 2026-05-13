package com.ashokvatika

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.ashokvatika.config.AppExperimentManager
import com.ashokvatika.config.ReleaseRuntimeConfig
import com.ashokvatika.notifications.AdminApprovalNotifier
import com.ashokvatika.ui.AppRoot
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AdminApprovalNotifier.ensureChannel(applicationContext)
    requestNotificationPermissionIfNeeded()
    runCatching {
      AppExperimentManager.initialize(applicationContext)
    }.onFailure { error ->
      Log.w("AshokvatikaMain", "Experiment manager initialization skipped", error)
    }
    lifecycleScope.launch {
      runCatching {
        AppExperimentManager.fetchAndActivate()
      }.onFailure { error ->
        Log.w("AshokvatikaMain", "Experiment manager fetch skipped", error)
      }
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

  private fun requestNotificationPermissionIfNeeded() {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) return
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) return

    ActivityCompat.requestPermissions(
      this,
      arrayOf(Manifest.permission.POST_NOTIFICATIONS),
      1001,
    )
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
