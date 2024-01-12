plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.efojug.bootflasher"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.efojug.bootflasher"
        minSdk = 30
        targetSdk = 34
        versionCode = 17
        versionName = "1.1"
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