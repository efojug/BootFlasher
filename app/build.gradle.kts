import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    id("org.jetbrains.kotlin.android")
}

val versionPropsFile = File(project.rootProject.projectDir, "version.properties")
fun getVersionCode(): Int {
    val props = Properties()
    versionPropsFile.inputStream().use { props.load(it) }
    val versionCode = props.getProperty("VERSION_CODE").toInt()
    val newVersionCode = versionCode + 1
    props["VERSION_CODE"] = newVersionCode.toString()
    versionPropsFile.writer().use { props.store(it, null) }
    return versionCode
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
        versionCode = getVersionCode()
        versionName = "3.1"
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
    implementation(libs.core.ktx)
    implementation(libs.kotlinx.coroutines)
}
//configurations.all {
//    exclude("org.jetbrains.kotlin", "kotlin-stdlib")
//}