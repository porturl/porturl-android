plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.openapi.generator)
    id("kotlin-parcelize")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn("openApiGenerate")
}

tasks.matching { it.name.startsWith("ksp") }.configureEach {
    dependsOn("openApiGenerate")
}

afterEvaluate {
    tasks.findByName("preBuild")?.dependsOn("openApiGenerate")
}

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("$projectDir/src/main/openapi/openapi.yaml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.absolutePath)
    apiPackage.set("org.friesoft.porturl.client.api")
    modelPackage.set("org.friesoft.porturl.client.model")
    configOptions.set(mapOf(
        "library" to "jvm-retrofit2",
        "serializationLibrary" to "kotlinx_serialization",
        "useCoroutines" to "true",
        "enumPropertyNaming" to "UPPERCASE",
        "packageName" to "org.friesoft.porturl.client",
        "useResponseWrapper" to "false",
        "useRetrofit2Response" to "false"
    ))
}

android {
    namespace = "org.friesoft.porturl"
    compileSdk = 36

    sourceSets {
        getByName("main") {
            kotlin.directories.add(layout.buildDirectory.dir("generated/openapi/src/main/kotlin").get().asFile.absolutePath)
        }
    }

    defaultConfig {
        applicationId = "org.friesoft.porturl"
        minSdk = 31
        targetSdk = 36

        val otlpEndpoint = project.findProperty("OTLP_ENDPOINT") ?: "https://otlp-gateway-prod-us-east-0.grafana.net/otlp"
        val otlpAuth = project.findProperty("OTLP_AUTH") ?: ""
        
        buildConfigField("String", "OTLP_ENDPOINT", "\"$otlpEndpoint\"")
        buildConfigField("String", "OTLP_AUTH", "\"$otlpAuth\"")

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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
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
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.retrofit.scalars)
    implementation(libs.logging.interceptor)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.converter.jackson)

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

    implementation(libs.androidx.security.crypto)

    // OpenTelemetry
    implementation(libs.opentelemetry.android.agent)

    // DataStore for Preferences
    implementation(libs.androidx.datastore.preferences)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    // Kotlin Color Picker
    implementation(libs.compose.colorpicker)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.register("printVersionName") {
    doLast {
        // This accesses the versionName from your android defaultConfig block
        println(project.the<com.android.build.api.dsl.ApplicationExtension>().defaultConfig.versionName)
    }
}
