plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.rusroute.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.rusroute.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "3.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("com.wireguard.android:tunnel:1.0.20260102")
    // sing-box libbox provides VLESS/Reality and other proxy protocol engines.
    implementation("com.github.singbox-android:libbox:1.14.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}
