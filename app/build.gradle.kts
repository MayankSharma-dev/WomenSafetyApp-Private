
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

val tomtomApiKey: String by project

android {
    namespace = "com.ms.womensafetyapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ms.womensafetyapp"
        //minSdk = 24
        // increased min sdk for Tom Tom map
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    signingConfigs{
        create("release"){
            storeFile = file(
                "D:/Android/Keys/womensafety/womensafety.jks"
            )
            storePassword = "123456789"
            keyAlias = "womenkey"
            keyPassword = "123456789"
        }
    }


    buildTypes {

        /*
        getByName("release"){
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }*/


        release {

            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }

    }
    buildFeatures {
        // viewBinding
        compose = true
        viewBinding = true
        buildConfig = true
    }

    buildTypes.configureEach {
        buildConfigField("String", "TOMTOM_API_KEY", "\"$tomtomApiKey\"")
    }



    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    packaging {
        jniLibs.pickFirsts.add("lib/**/libc++_shared.so")
    }

}



dependencies {

    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.compose.ui:ui-tooling-preview-android:1.7.5")
    //implementation("androidx.navigation:navigation-fragment:2.8.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // activities
    val activity_version = "1.9.0"
    implementation("androidx.activity:activity-ktx:$activity_version")

    //fragments
    val fragment_version = "1.8.5"
    implementation("androidx.fragment:fragment-ktx:$fragment_version")

    //recyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    // For control over item selection of both touch and mouse driven selection
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")

    val lifecycle_version = "2.7.0"
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    //Kotlin coroutines on Android
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    //Room
    val room_version = "2.6.1"

    implementation("androidx.room:room-ktx:$room_version")
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    // Google Location Services.
    implementation("com.google.android.gms:play-services-location:21.2.0")

    //Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")

    // Preference DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    //CardView
    implementation("androidx.cardview:cardview:1.0.0")

    //Maps
//    implementation("com.google.android.gms:play-services-maps:18.2.0")
    //implementation("org.osmdroid:osmdroid-android:6.1.14")

    //Splash Screen
    implementation("androidx.core:core-splashscreen:1.2.0-alpha01")

    // Square/Seismic shake detection.
    implementation("com.squareup:seismic:1.0.3")

    //Navigation Jetpack
    val nav_version = "2.8.3"
    // Views/Fragments integration
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // Feature module support for Fragments
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")

    //Tom Tom Maps
    implementation("com.tomtom.sdk.maps:map-display:1.21.0")
    implementation("com.tomtom.sdk.search:search-online:1.21.0")

    // Glance Widget
    // For AppWidgets support
    implementation("androidx.glance:glance-appwidget:1.1.1")

    // For interop APIs with Material 3
    implementation("androidx.glance:glance-material3:1.1.1")

    // For interop APIs with Material 2
    implementation("androidx.glance:glance-material:1.1.1")

    //leakCanary
    //debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")

}

// Allow references to generated code for dagger hilt
kapt {
    correctErrorTypes = true
}