plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.bugsnag)
}

android {
    compileSdk = 34
    namespace = "tk.zwander.wifilist"

    defaultConfig {
        applicationId = "tk.zwander.wifilist"
        minSdk = 30
        targetSdk = 34
        versionCode = 13
        versionName = "1.3.2"

        setProperty("archivesBaseName", "WiFiList_$versionName")

        vectorDrawables {
            useSupportLibrary = true
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

    implementation(libs.patreonSupportersRetrieval)
    implementation(libs.gson)
    implementation(libs.bugsnag.android)
    implementation(libs.fastcsv)
    implementation(libs.relinker)
}