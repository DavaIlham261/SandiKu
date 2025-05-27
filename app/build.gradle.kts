
plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.passwordmanager"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.passwordmanager"
        minSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)

    // Navigation Component
    implementation(libs.navigation.fragment.v275)
    implementation(libs.navigation.ui.v275)

    // Room Database - PERBAIKAN UTAMA
    implementation (libs.room.runtime.v250)
    implementation(libs.biometric)
    implementation(libs.constraintlayout)
    annotationProcessor (libs.room.compiler.v250)

    // ViewModel and LiveData
    implementation(libs.lifecycle.viewmodel.v270)
    implementation(libs.lifecycle.livedata.v270)

    // RecyclerView
    implementation(libs.recyclerview.v132)

    // Security for encryption
    implementation(libs.security.crypto.v110alpha06)
    implementation(libs.security.crypto)
    implementation(libs.activity)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Fragment & Activity
    implementation(libs.fragment)

    // LocalBroadcastManager
    implementation(libs.localbroadcastmanager)

}