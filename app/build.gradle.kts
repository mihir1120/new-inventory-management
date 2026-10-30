plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.plugin.compose")
  id("com.google.gms.google-services")
}

android {
  namespace = "com.ashokvatika.app"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.ashokvatika.admin"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    manifestPlaceholders["usesCleartextTraffic"] = "false"
    manifestPlaceholders["localBridgeAuthority"] = "${applicationId}.bridge"
    buildConfigField("String", "LOCAL_BRIDGE_AUTHORITY", "\"${applicationId}.bridge\"")
  }

  buildTypes {
    debug {
      isMinifyEnabled = false
      isShrinkResources = false
      buildConfigField("boolean", "ENABLE_SCREENSHOT_PROTECTION", "false")
      buildConfigField("boolean", "ENABLE_EXPERIMENTS", "true")
      buildConfigField("String", "RELEASE_CHANNEL", "\"debug\"")
      manifestPlaceholders["usesCleartextTraffic"] = "true"
    }

    release {
      isMinifyEnabled = true
      isShrinkResources = true
      isDebuggable = false
      isJniDebuggable = false
      buildConfigField("boolean", "ENABLE_SCREENSHOT_PROTECTION", "true")
      buildConfigField("boolean", "ENABLE_EXPERIMENTS", "false")
      buildConfigField("String", "RELEASE_CHANNEL", "\"production\"")
      manifestPlaceholders["usesCleartextTraffic"] = "false"
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
      )
    }
  }

  lint {
    abortOnError = true
    checkReleaseBuilds = true
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }

  testOptions {
    unitTests.isIncludeAndroidResources = true
  }
}

dependencies {
  implementation("androidx.core:core-ktx:1.18.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
  implementation("androidx.activity:activity-compose:1.13.0")
  implementation("androidx.constraintlayout:constraintlayout:2.2.1")

  implementation(platform("androidx.compose:compose-bom:2026.03.01"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.material:material-icons-extended")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
  implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
  implementation("com.google.firebase:firebase-analytics-ktx")
  implementation("com.google.firebase:firebase-auth")
  implementation("com.google.firebase:firebase-database-ktx")
  implementation("com.google.firebase:firebase-config-ktx")
  implementation("com.google.firebase:firebase-firestore-ktx")
  implementation("com.google.zxing:core:3.5.3")
  implementation("org.apache.poi:poi:3.17")

  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")

  testImplementation("junit:junit:4.13.2")

  androidTestImplementation(platform("androidx.compose:compose-bom:2026.03.01"))
  androidTestImplementation("androidx.test.ext:junit:1.3.0")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
