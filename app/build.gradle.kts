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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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

    implementation(libs.accompanist.permissions)

    // Compose
    implementation(libs.bundles.androidx.compose)
    implementation (libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended)

    // Kotlinx
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization)

    // Koin
    implementation(libs.bundles.koin)

    // Room
    ksp(libs.room.compiler)
    implementation(libs.bundles.room)

    // Datastore
    implementation(libs.bundles.multiplatformSettings)

    // Arrow
    implementation(libs.bundles.arrow)
    implementation(libs.bundles.pedestal)

    // Commands
    implementation(libs.clikt)

    // Code editor
    implementation(libs.codeEditor)

    // QuickJS
    api(libs.quickjs)

    // Clikt
    implementation(libs.clikt)
//    implementation("com.github.Tyranozomby:clikt:patch-1-94bb361bca-1")

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.koin.test)
    testImplementation(libs.kotlinx.coroutines.test)

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
