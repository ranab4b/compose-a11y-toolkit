import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.a11ytoolkit.sample"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.a11ytoolkit.sample"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":toolkit"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    // Adds a placeholder <activity> to the debug manifest that
    // createComposeRule() needs to host content under Robolectric. Only
    // meaningful for the debug variant, since AccessibilityAuditTest lives in
    // src/testDebug and never runs against release.
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testDebugImplementation("junit:junit:4.13.2")
    testDebugImplementation("org.robolectric:robolectric:4.13")
    testDebugImplementation(platform(libs.androidx.compose.bom))
    testDebugImplementation("androidx.compose.ui:ui-test-junit4")
}
