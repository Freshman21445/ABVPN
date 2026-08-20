plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.abvpn.vpn"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.abvpn.vpn"
        minSdk = 26 // VpnService + modern crypto APIs need reasonably recent Android
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0-prototype"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.datastore:datastore-preferences:1.1.1") // encrypted-at-rest via EncryptedFile wrapper, see data/
    implementation("androidx.security:security-crypto:1.1.0")
    // NOTE: real WireGuard tunneling requires the official WireGuard Android library
    // (com.wireguard.android:tunnel), which this prototype does not wire in yet — see README.
    // implementation("com.wireguard.android:tunnel:1.0.20230706")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}
