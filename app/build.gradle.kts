plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.examforge"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.examforge"
        minSdk = 30
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
    packaging {
        resources {
            pickFirsts += "com/tom_roush/pdfbox/resources/glyphlist/glyphlist.txt"
            pickFirsts += "com/tom_roush/pdfbox/resources/glyphlist/glyphlist3.txt"
        }
    }
}

dependencies {
    // AndroidX & Material
    implementation(libs.appcompat)
    implementation(libs.material.v160) // Use this one or libs.material if you prefer a different version
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase (choose one Firebase Auth version)
    implementation(libs.firebase.auth)
    // Remove duplicate: implementation(libs.firebase.auth.v2101)
    implementation(libs.firebase.core)

    // Google Sign-In / Credentials
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Room Database
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // PDF Libraries
    // Use only one PdfBox-Android dependency
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    // Remove duplicate: implementation(libs.pdfbox.android)

    // Retrofit and Gson Converter (avoid duplicates)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // PDF Generation
    implementation(libs.itextg)
}