plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    id("module.kotlin-jvm-toolchain")
}

android {
    namespace = "Kernel.Extend"
    compileSdk = 35

    defaultConfig {
        applicationId = "Kernel.Extend"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

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
        debug {
            applicationIdSuffix = ""
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(projects.miuixCore)
    implementation(projects.miuixUi)
    implementation(projects.miuixPreference)
    implementation(projects.miuixIcons)
    implementation(projects.miuixBlur)
    implementation(projects.miuixSquircle)
    implementation(projects.miuixNavigation3Ui)

    implementation(libs.androidx.activity)
    implementation(libs.jetbrains.compose.foundation)
    implementation(libs.jetbrains.compose.components.resources)
    implementation(libs.androidx.navigationevent)
    implementation(libs.materialKolor.utilities)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.jetbrains.lifecycle.runtime.compose)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
}
