plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
android {
    namespace = "org.friesoft.porturl"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "org.friesoft.porturl"
        minSdk = 31

        versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
        
        // Dynamic version name logic
        val isRelease = System.getenv("GITHUB_REF")?.startsWith("refs/tags/v") == true || System.getenv("IS_RELEASE_BUILD") == "true"
        var version = project.version.toString()
        
        if (!isRelease) {
             val gitCommit = providers.exec {
                commandLine("git", "rev-parse", "--short", "HEAD")
            }.standardOutput.asText.get().trim()
            
             if (gitCommit.isNotEmpty()) {
                version = "$version-$gitCommit"
            }
        }
        
        versionName = version

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    androidResources {
        generateLocaleConfig = true
        localeFilters += arrayOf("en", "de")
    }

    signingConfigs {
        create("release") {
            val storePath = System.getenv("SIGNING_KEY_STORE_PATH")
            if (!storePath.isNullOrEmpty() && file(storePath).exists()) {
                storeFile = file(storePath)
                storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
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
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
            }
        }
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.foundation)
    ksp(libs.kotlin.metadata.jvm)
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
    implementation(libs.androidx.material3.window.size.class1)
    implementation(libs.androidx.ui.text)
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
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
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
        println(project.the<com.android.build.api.dsl.ApplicationExtension>().defaultConfig.versionName)
    }
}
