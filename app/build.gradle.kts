plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.compose.compiler)
    id("pl.allegro.tech.build.axion-release") version "1.20.1"
}

scmVersion {
    tag {
        // Tell the plugin that your tags start with "v" (e.g., v1.0.0)
        prefix.set("v")
        versionSeparator.set("")
    }
}

android {
    namespace = "org.friesoft.porturl"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.friesoft.porturl"
        minSdk = 31
        targetSdk = 36

        versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
        // the versionName is now set dynamically by the axion-release-plugin which provides the `project.version` property.
        versionName = project.version.toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    androidResources {
        generateLocaleConfig = true
        localeFilters += arrayOf("en", "de")
    }

    signingConfigs {
        create("release") {
            val storePath = System.getenv("SIGNING_KEY_STORE_PATH")
            if (storePath != null) {
                storeFile = file(storePath)
                storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.materialcomponents)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended.android)

    implementation(libs.androidx.window)

    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.material3.window.size.class1)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Hilt for Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Retrofit for Networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // AppAuth for OAuth2/OIDC
    implementation(libs.appauth)

    // Jetpack Compose & Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Add Coil for loading images from URLs in Jetpack Compose
    implementation(libs.coil.compose)

    // reorderable list
    implementation(libs.reorderable)

    implementation(libs.androidx.browser)

    // DataStore for Preferences
    implementation(libs.androidx.datastore.preferences)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    // Color Picker
    implementation(libs.compose.colorpicker)
}

tasks.register("printVersionName") {
    doLast {
        // This accesses the versionName from your android defaultConfig block
        println(android.defaultConfig.versionName)
    }
}
