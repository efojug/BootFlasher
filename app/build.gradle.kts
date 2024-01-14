plugins {
    alias(libs.plugins.androidApplication)
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
        versionCode = 21
        versionName = "1.2"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    implementation(kotlin("stdlib-jdk8", "1.8.22"))
}
//configurations.all {
//    exclude("org.jetbrains.kotlin", "kotlin-stdlib")
//}