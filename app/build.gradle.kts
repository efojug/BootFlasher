plugins {
    alias(libs.plugins.androidApplication)
    id("org.jetbrains.kotlin.android")
}

android {
    signingConfigs {
        getByName("debug") {
            storeFile = file("..\\certificate\\bootflasher.jks")
            storePassword = "000000"
            keyPassword = "000000"
            keyAlias = "key0"
        }
    }
    namespace = "com.efojug.bootflasher"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.efojug.bootflasher"
        minSdk = 30
        targetSdk = 34
        versionCode = 60
        versionName = "2.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    implementation(kotlin("stdlib-jdk8", "1.8.22"))
    implementation("androidx.core:core-ktx:1.12.0")
}
//configurations.all {
//    exclude("org.jetbrains.kotlin", "kotlin-stdlib")
//}