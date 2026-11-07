package org.exteragram.rust.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MonetOceanBlue,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF0F141C),
    surface = Color(0xFF161D29),
    onPrimary = Color.White,
    onBackground = Color(0xFFF0F4F8),
    onSurface = Color(0xFFE2E8F0)
)

private val LightColorScheme = lightColorScheme(
    primary = MonetOceanBlue,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF1E293B)
)

@Composable
fun ExteraGramTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    customAccentColor: Color? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        customAccentColor != null -> {
            if (darkTheme) {
                DarkColorScheme.copy(primary = customAccentColor)
            } else {
                LightColorScheme.copy(primary = customAccentColor)
            }
        }
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
