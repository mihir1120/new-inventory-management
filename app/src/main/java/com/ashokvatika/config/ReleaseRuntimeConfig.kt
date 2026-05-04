package com.ashokvatika.config

import com.ashokvatika.app.BuildConfig

object ReleaseRuntimeConfig {
  val screenshotsProtected: Boolean
    get() = BuildConfig.ENABLE_SCREENSHOT_PROTECTION

  val experimentsEnabled: Boolean
    get() = BuildConfig.ENABLE_EXPERIMENTS

  val releaseChannel: String
    get() = BuildConfig.RELEASE_CHANNEL
}
