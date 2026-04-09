import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

fun localProp(key: String, default: String = ""): String =
    localProperties.getProperty(key, default)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.synapseai"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.synapseai"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // ── Vedaspark AI API Configuration ──
        // Keys loaded from local.properties (never committed to VCS)
        buildConfigField("String", "DOGRAH_API_KEY", "\"${localProp("DOGRAH_API_KEY", "dgr_kmbt68fZ5NfpVlpE6ZBff4082QPpiLLek1CFWnO1Mvw")}\"")
        buildConfigField("String", "DOGRAH_AGENT_ID", "\"${localProp("DOGRAH_AGENT_ID", "af96de66-753e-4201-b166-ce5eccab3951")}\"")
        buildConfigField("String", "DOGRAH_BASE_URL", "\"${localProp("DOGRAH_BASE_URL", "https://api.dograh.com")}\"")
        buildConfigField("String", "FASTAPI_BASE_URL", "\"${localProp("FASTAPI_BASE_URL", "http://localhost:8000")}\"")
        buildConfigField("String", "ESPO_CRM_BASE_URL", "\"${localProp("ESPO_CRM_BASE_URL", "https://your-crm.espocrm.com")}\"")
        buildConfigField("String", "ESPO_CRM_API_KEY", "\"${localProp("ESPO_CRM_API_KEY", "your-espo-crm-api-key")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Networking
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Image loading
    implementation(libs.coil.compose)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}