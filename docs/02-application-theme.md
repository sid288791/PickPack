# Step 2: Application Class & Compose Theme

This step creates the Hilt Application class and the Jetpack Compose Material3 theme (colors, typography, theme setup).

---

## 2.1 Application Class

**File:** `app/src/main/java/com/autopart/ordermgmt/OrderManagementApplication.kt`

```kotlin
package com.autopart.ordermgmt

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OrderManagementApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Application initialization logic can be added here
    }
}
```

**Key points:**
- `@HiltAndroidApp` enables Hilt dependency injection throughout the app
- Registered in `AndroidManifest.xml` as `android:name=".OrderManagementApplication"`

---

## 2.2 Color Definitions

**File:** `app/src/main/java/com/autopart/ordermgmt/ui/theme/Color.kt`

```kotlin
package com.autopart.ordermgmt.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
```

---

## 2.3 Typography

**File:** `app/src/main/java/com/autopart/ordermgmt/ui/theme/Type.kt`

```kotlin
package com.autopart.ordermgmt.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)
```

---

## 2.4 Theme Composable

**File:** `app/src/main/java/com/autopart/ordermgmt/ui/theme/Theme.kt`

```kotlin
package com.autopart.ordermgmt.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

**Key points:**
- Supports dynamic color (Android 12+), dark theme, and light theme
- Used in `MainActivity` as `MyApplicationTheme { ... }`
- Material3 color scheme with purple/pink palette

---

## Summary

After this step you should have:
- `OrderManagementApplication.kt` - Hilt-annotated Application class
- `ui/theme/Color.kt` - Color palette definitions
- `ui/theme/Type.kt` - Material3 typography
- `ui/theme/Theme.kt` - `MyApplicationTheme` composable with dynamic color support
