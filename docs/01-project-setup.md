# Step 1: Project Setup

This step covers all project-level configuration files needed to create an exact replica of the AutoPart Order Management Android app.

---

## 1.1 Root `build.gradle.kts`

**File:** `build.gradle.kts`

```kotlin
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
```

---

## 1.2 `settings.gradle.kts`

**File:** `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "My Application"
include(":app")
```

---

## 1.3 `gradle.properties`

**File:** `gradle.properties`

```properties
# Project-wide Gradle settings.
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.configuration-cache=true
kotlin.code.style=official
# Opt out of new Android DSL for Hilt compatibility with AGP 9
android.newDsl=false
# Allow KSP to use kotlin.sourceSets DSL with built-in Kotlin
android.disallowKotlinSourceSets=false
```

---

## 1.4 Version Catalog `libs.versions.toml`

**File:** `gradle/libs.versions.toml`

```toml
[versions]
agp = "9.2.1"
coreKtx = "1.19.0"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
lifecycleRuntimeKtx = "2.11.0"
activityCompose = "1.13.0"
kotlin = "2.2.10"
composeBom = "2026.02.01"
firebaseBom = "33.9.0"
googleServices = "4.4.2"
hilt = "2.57.1"
hiltNavigationCompose = "1.2.0"
datastore = "1.1.4"
ksp = "2.2.10-2.0.2"
ktor = "2.3.10"
kotlinx-serialization = "1.6.3"
kotlin-serialization-plugin = "2.0.0"
coil = "2.4.0"
material-icons-extended = "1.5.4"
sceneview = "2.3.0"
gson = "2.10.1"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-messaging = { group = "com.google.firebase", name = "firebase-messaging" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
ksp-gradlePlugin = { group = "com.google.devtools.ksp", name = "com.google.devtools.ksp.gradle.plugin", version.ref = "ksp" }
ktor-client-core = { group = "io.ktor", name = "ktor-client-core", version.ref = "ktor" }
ktor-client-android = { group = "io.ktor", name = "ktor-client-android", version.ref = "ktor" }
ktor-client-content-negotiation = { group = "io.ktor", name = "ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { group = "io.ktor", name = "ktor-serialization-kotlinx-json", version.ref = "ktor" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended", version.ref = "material-icons-extended" }
sceneview = { group = "io.github.sceneview", name = "sceneview", version.ref = "sceneview" }
gson = { group = "com.google.code.gson", name = "gson", version.ref = "gson" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin-serialization-plugin" }
```

---

## 1.5 App `build.gradle.kts`

**File:** `app/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-parcelize")
}

android {
    namespace = "com.autopart.ordermgmt"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.autopart.ordermgmt"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    lint {
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coil.compose)
    implementation(libs.material.icons.extended)
    implementation(libs.sceneview)
    implementation(libs.gson)
    
    // Ktor dependencies
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
```

---

## 1.6 `google-services.json`

**File:** `app/google-services.json`

```json
{
  "project_info": {
    "project_number": "57836849727",
    "project_id": "auto-part-ordermgmt",
    "storage_bucket": "auto-part-ordermgmt.firebasestorage.app"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:57836849727:android:2096f06438198f0a06468a",
        "android_client_info": {
          "package_name": "com.autopart.ordermgmt"
        }
      },
      "oauth_client": [],
      "api_key": [
        {
          "current_key": "AIzaSyBUDMwsjrf3z4SwVPT_D98MXY7nL8V6YuY"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": []
        }
      }
    }
  ],
  "configuration_version": "1"
}
```

> **Note:** Replace with your own Firebase project's `google-services.json` if creating a new project.

---

## 1.7 `AndroidManifest.xml`

**File:** `app/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />

    <application
        android:name=".OrderManagementApplication"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MyApplication">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.MyApplication"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".data.remote.firebase.OrderManagementFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>

        <service
            android:name=".data.remote.firebase.NotificationSoundService"
            android:exported="false"
            android:foregroundServiceType="mediaPlayback" />

        <receiver
            android:name=".data.remote.firebase.NotificationActionReceiver"
            android:exported="false" />
    </application>

</manifest>
```

---

## 1.8 Resource Files

### `res/values/strings.xml`

```xml
<resources>
    <string name="app_name">AutoPart</string>
</resources>
```

### `res/values/themes.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.MyApplication" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

### `res/values/colors.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="purple_200">#FFBB86FC</color>
    <color name="purple_500">#FF6200EE</color>
    <color name="purple_700">#FF3700B3</color>
    <color name="teal_200">#FF03DAC5</color>
    <color name="teal_700">#FF018786</color>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
    <color name="ic_launcher_background">#FFFFFF</color>
</resources>
```

### `res/xml/backup_rules.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
</full-backup-content>
```

### `res/xml/data_extraction_rules.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
    </cloud-backup>
</data-extraction-rules>
```

### `res/drawable/ic_notification.xml`

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
  <path
      android:fillColor="#FFFFFF"
      android:pathData="M12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.9,2 2,2zM18,16v-5c0,-3.07 -1.63,-5.64 -4.5,-6.32V4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68C7.64,5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2z"/>
</vector>
```

### `res/raw/notification_sound.mp3`

Place any `.mp3` notification sound file at `app/src/main/res/raw/notification_sound.mp3`.

### `res/drawable/paronama.png`

Place a 360-degree panoramic warehouse image at `app/src/main/res/drawable/paronama.png`.

### `res/drawable/icin.webp`

Place a product placeholder image at `app/src/main/res/drawable/icin.webp`.

### `assets/panorama_data.json`

**File:** `app/src/main/assets/panorama_data.json`

```json
{
  "panoramas": [
    {
      "id": "1",
      "title": "Warehouse View",
      "description": "360-degree view of the main warehouse storage area",
      "imageUrl": "panorama_dummy.jpg",
      "itemLocations": [
        { "id": "A101", "name": "Brake Pads", "yaw": 45.5, "pitch": -8.0 },
        { "id": "B220", "name": "Oil Filters", "yaw": 120.0, "pitch": 5.0 },
        { "id": "C330", "name": "Spark Plugs", "yaw": -30.0, "pitch": 10.0 }
      ]
    },
    {
      "id": "2",
      "title": "Loading Dock",
      "description": "Panoramic view of the loading and unloading area",
      "imageUrl": "panorama_dummy.jpg",
      "itemLocations": [
        { "id": "D440", "name": "Timing Belt", "yaw": 60.0, "pitch": -5.0 },
        { "id": "E550", "name": "Water Pump", "yaw": 180.0, "pitch": 8.0 }
      ]
    },
    {
      "id": "3",
      "title": "Storage Room A",
      "description": "Interior view of primary storage facility",
      "imageUrl": "panorama_dummy.jpg",
      "itemLocations": [
        { "id": "F660", "name": "Shock Absorbers", "yaw": 90.0, "pitch": -2.0 },
        { "id": "G770", "name": "Struts", "yaw": 270.0, "pitch": 12.0 },
        { "id": "H880", "name": "Control Arms", "yaw": 150.0, "pitch": -10.0 }
      ]
    }
  ]
}
```

---

## Summary

After this step you should have:
- Root `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`
- `gradle/libs.versions.toml` version catalog
- `app/build.gradle.kts` with all plugins and dependencies
- `app/google-services.json` for Firebase
- `app/src/main/AndroidManifest.xml` with permissions, activities, services, receiver
- All resource files (strings, themes, colors, drawables, raw sound, xml backup rules)
- `assets/panorama_data.json`
