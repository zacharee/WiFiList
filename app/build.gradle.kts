plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.bugsnag.android.gradle")
}

android {
    compileSdk = 34
    namespace = "tk.zwander.wifilist"

    defaultConfig {
        applicationId = "tk.zwander.wifilist"
        minSdk = 30
        targetSdk = 33
        versionCode = 10
        versionName = "1.2.1"

        vectorDrawables {
            useSupportLibrary = true
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
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    implementation(libs.compose.ui)
    implementation(libs.compose.compiler)
    implementation(libs.core.ktx)
    implementation(libs.compose.material3)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(libs.fragment.ktx)
    implementation(libs.datastore.preferences)

    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.hiddenapibypass)
    implementation(libs.material)
    implementation(libs.accompanist.flowlayout)

    implementation(libs.patreonSupportersRetrieval)
    implementation(libs.gson)
    implementation(libs.bugsnag.android)
    implementation(libs.fastcsv)
}