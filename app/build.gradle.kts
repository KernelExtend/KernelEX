plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    id("module.kotlin-jvm-toolchain")
}

android {
    namespace = "Kernel.Extend"
    compileSdk = 37

    defaultConfig {
        applicationId = "Kernel.Extend"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("KernelEX.jks")
            storePassword = "KernelEX"
            keyAlias = "KernelEX"
            keyPassword = "KernelEX"
            enableV1Signing = false
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ""
            signingConfig = signingConfigs.getByName("release")
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
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/*.version",
                "/META-INF/*.kotlin_module",
                "/META-INF/**/LICENSE*",
                "/META-INF/**/NOTICE*",
                "/META-INF/**/license*",
                "/META-INF/**/notice*",
                "/META-INF/**/*.properties",
                "DebugProbesKt.bin",
                "kotlin/**",
                "kotlin-tooling-metadata.json",
                "assets/**",
                "assets/dexopt/**"
            )
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

// 禁用 assets 合并与 ART BaselineProfile 生成，彻底移除 APK 中的 assets/ 文件夹
tasks.matching {
    it.name.contains("ArtProfile") || (it.name.startsWith("merge") && it.name.endsWith("Assets"))
}.configureEach {
    enabled = false
}
