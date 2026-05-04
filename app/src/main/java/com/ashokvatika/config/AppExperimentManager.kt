package com.ashokvatika.config

import android.os.Bundle
import android.content.Context
import android.util.Log
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.tasks.await

enum class GalleryShareSheetVariant(
  val id: String,
) {
  AutoOpen("auto_open"),
  ManualOpen("manual_open"),
}

object AppExperimentManager {
  private const val Tag = "AppExperimentManager"
  private const val KeyExperimentsEnabled = "experiments_enabled"
  private const val KeyGalleryShareSheetVariant = "gallery_share_sheet_variant"

  @Volatile
  private var initialized = false

  private val remoteConfig by lazy { Firebase.remoteConfig }
  private val analytics by lazy { Firebase.analytics }

  fun initialize(context: Context) {
    if (initialized) return
    synchronized(this) {
      if (initialized) return

      val minimumFetchInterval = if (ReleaseRuntimeConfig.releaseChannel == "debug") 0L else 3600L
      remoteConfig.setConfigSettingsAsync(
        remoteConfigSettings {
          minimumFetchIntervalInSeconds = minimumFetchInterval
        },
      )
      remoteConfig.setDefaultsAsync(com.ashokvatika.app.R.xml.remote_config_defaults)
      initialized = true
    }
  }

  suspend fun fetchAndActivate(): Boolean {
    return runCatching { remoteConfig.fetchAndActivate().await() }
      .onFailure { error -> Log.w(Tag, "Remote Config fetch failed", error) }
      .getOrDefault(false)
  }

  fun experimentsEnabled(): Boolean {
    return ReleaseRuntimeConfig.experimentsEnabled && remoteConfig.getBoolean(KeyExperimentsEnabled)
  }

  fun galleryShareSheetVariant(): GalleryShareSheetVariant {
    if (!experimentsEnabled()) return GalleryShareSheetVariant.AutoOpen
    return when (remoteConfig.getString(KeyGalleryShareSheetVariant)) {
      GalleryShareSheetVariant.ManualOpen.id -> GalleryShareSheetVariant.ManualOpen
      else -> GalleryShareSheetVariant.AutoOpen
    }
  }

  fun logGalleryVariantExposure(variant: GalleryShareSheetVariant) {
    analytics.logEvent(
      "ab_gallery_variant_exposure",
      Bundle().apply {
        putString("variant", variant.id)
        putString("channel", ReleaseRuntimeConfig.releaseChannel)
      },
    )
  }

  fun logGalleryShareSheetOpened(variant: GalleryShareSheetVariant, selectedCount: Int) {
    analytics.logEvent(
      "ab_gallery_sheet_opened",
      Bundle().apply {
        putString("variant", variant.id)
        putLong("selected_count", selectedCount.toLong())
      },
    )
  }

  fun logGalleryShareAction(variant: GalleryShareSheetVariant, platformId: String, selectedCount: Int) {
    analytics.logEvent(
      "ab_gallery_share_action",
      Bundle().apply {
        putString("variant", variant.id)
        putString("platform", platformId)
        putLong("selected_count", selectedCount.toLong())
      },
    )
  }
}
