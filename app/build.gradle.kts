plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)

    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "com.noobzsociety.smsgames"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.noobzsociety.smsgames"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))

    // Compose
    implementation(libs.bundles.androidx.compose)
    implementation (libs.androidx.navigation.compose)

    // Kotlinx
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization)

    // Koin
    implementation(libs.bundles.koin)

    // Room
    ksp(libs.room.compiler)
    implementation(libs.bundles.room)

    // Arrow
    implementation(libs.arrow.core)
    implementation(libs.arrow.fx.coroutines)

    // Code editor
    implementation(libs.codeEditor)

    // QuickJS
    implementation(libs.quickjs)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.koin.test)

    // Android testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // UI testing
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

room {
    schemaDirectory("src/main/schemas")
}

ksp {
    arg("room.generateKotlin", "true")
}
