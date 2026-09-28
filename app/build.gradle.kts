import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        load(FileInputStream(file))
    }
}

fun signingValue(key: String): String? =
    System.getenv(key)?.takeIf { it.isNotEmpty() } ?: localProperties.getProperty(key)

android {
    namespace = "com.dnodevelopment.padelcompanion"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dnodevelopment.padelcompanion"
        minSdk = 30
        targetSdk = 35
        // The release workflow passes -PversionName=x.y.z and
        // -PversionCode=<major*10000 + minor*100 + patch> derived from the git tag.
        versionCode = (findProperty("versionCode") as String?)?.toInt() ?: 5
        versionName = findProperty("versionName") as String? ?: "1.4"

    }

    signingConfigs {
        // Local builds read the upload key from local.properties; CI passes the
        // same keys as environment variables. Without a store file the release
        // build is left unsigned, so debug builds and tests still work.
        val storePath = signingValue("STORE_FILE")
        if (storePath != null) {
            create("release") {
                storeFile = file(storePath)
                storePassword = signingValue("STORE_PASSWORD")
                keyAlias = signingValue("KEY_ALIAS")
                keyPassword = signingValue("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.wear.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    testImplementation(libs.junit)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.runtime)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)

    debugImplementation(libs.ui.test.manifest)

    implementation("androidx.compose.material:material:1.6.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
}