plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val versionMajor = (project.findProperty("VERSION_MAJOR") as? String)?.toInt() ?: 0
val versionMinor = (project.findProperty("VERSION_MINOR") as? String)?.toInt() ?: 0
val versionPatch = (project.findProperty("VERSION_PATCH") as? String)?.toInt() ?: 1
val appVersionName = "$versionMajor.$versionMinor.$versionPatch"
// versionCode comes from CI: -PVERSION_CODE=$GITHUB_RUN_NUMBER. Local builds use 1.
val appVersionCode = (project.findProperty("VERSION_CODE") as? String)?.toIntOrNull() ?: 1

android {
    namespace = "io.github.elli0t0.calendar"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.elli0t0.calendar"
        minSdk = 26
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH")?.trim()
            val keystorePassword = System.getenv("KEYSTORE_PASSWORD")?.trim()
            val keyAliasVal = System.getenv("KEY_ALIAS")?.trim()
            val keyPasswordVal = System.getenv("KEY_PASSWORD")?.trim()
            if (!keystorePath.isNullOrEmpty() && !keystorePassword.isNullOrEmpty()
                && !keyAliasVal.isNullOrEmpty() && !keyPasswordVal.isNullOrEmpty()
            ) {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                keyAlias = keyAliasVal
                keyPassword = keyPasswordVal
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseKeystore = signingConfigs.getByName("release")
            if (releaseKeystore.storeFile != null) {
                signingConfig = releaseKeystore
            }
        }
    }

    androidResources {
        localeFilters += listOf("fa")
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    packaging {
        resources {
            excludes += listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/*.version",
                "/META-INF/*.kotlin_module",
                "DebugProbesKt.bin",
                "kotlin-tooling-metadata.json"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

base {
    archivesName.set("Calendar-$appVersionName")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
}
