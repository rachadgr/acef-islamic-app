import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
}

// ---------------------------------------------------------------------------
// Read the .env file (simple KEY=VALUE parser, same convention as AI Studio)
// ---------------------------------------------------------------------------
fun readEnvFile(): Map<String, String> {
  val result = mutableMapOf<String, String>()
  val candidates = listOf(rootProject.file(".env"), rootProject.file(".env.example"))
  candidates.forEach { f ->
    if (f.exists()) {
      f.readLines().forEach { rawLine ->
        val line = rawLine.trim()
        if (line.isNotEmpty() && !line.startsWith("#") && line.contains("=")) {
          val key = line.substringBefore("=").trim()
          val value = line.substringAfter("=").trim().trim('"')
          if (key.isNotEmpty() && !result.containsKey(key)) result[key] = value
        }
      }
    }
  }
  return result
}

val envVars = readEnvFile()

fun envValue(key: String): String =
  (System.getenv(key) ?: envVars[key] ?: "").trim()

// Optional keystore configuration coming from key.properties (used by CI / release builds)
val keystorePropertiesFile = rootProject.file("key.properties")
val keystoreProperties = Properties().apply {
  if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use { load(it) }
  }
}

android {
  namespace = "com.example"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.aistudio.acef.islamic"
    minSdk = 24
    targetSdk = 36
    versionCode = 2
    versionName = "2.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    val geminiKey = envValue("GEMINI_API_KEY")
    buildConfigField("String", "GEMINI_ENV_API_KEY", "\"$geminiKey\"")
    buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
  }

  signingConfigs {
    // Release signing: read from key.properties / environment when available.
    create("release") {
      val storeFilePath = keystoreProperties.getProperty("storeFile")
        ?: System.getenv("KEYSTORE_PATH")
        ?: "${rootDir}/release-key.jks"
      val ksFile = file(storeFilePath)
      if (ksFile.exists()) {
        storeFile = ksFile
        storePassword = keystoreProperties.getProperty("storePassword") ?: System.getenv("STORE_PASSWORD")
        keyAlias = keystoreProperties.getProperty("keyAlias") ?: System.getenv("KEY_ALIAS") ?: "acef"
        keyPassword = keystoreProperties.getProperty("keyPassword") ?: System.getenv("KEY_PASSWORD")
      }
    }
    // Debug signing: always available so debug builds never fail.
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      // Fall back to the debug keystore when no release keystore is configured,
      // so the release build never fails to produce an installable APK.
      val releaseCfg = signingConfigs.getByName("release")
      signingConfig = if (releaseCfg.storeFile != null) releaseCfg else signingConfigs.getByName("debugConfig")
    }
    debug {
      applicationIdSuffix = ".debug"
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

kotlin {
  jvmToolchain(21)
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.converter.moshi)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
