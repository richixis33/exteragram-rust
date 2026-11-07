package org.extera.light.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MdPrimary,
    onPrimary = MdOnPrimary,
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD3E3FD),
    secondary = MdSecondary,
    onSecondary = MdOnSecondary,
    background = MdSurfaceDark,
    surface = MdSurfaceDark,
    surfaceVariant = MdSurfaceVariantDark,
    surfaceContainer = MdSurfaceContainerDark,
    surfaceContainerHigh = MdSurfaceContainerHighDark,
    surfaceContainerLow = MdSurfaceContainerLowDark,
    onBackground = MdOnSurfaceDark,
    onSurface = MdOnSurfaceDark,
    onSurfaceVariant = MdOnSurfaceVariantDark,
    outline = Color(0xFF8E919A),
    outlineVariant = Color(0xFF44474F)
)

private val LightColorScheme = lightColorScheme(
    primary = MdPrimary,
    onPrimary = MdOnPrimary,
    primaryContainer = MdPrimaryContainer,
    onPrimaryContainer = MdOnPrimaryContainer,
    secondary = MdSecondary,
    onSecondary = MdOnSecondary,
    background = MdSurface,
    surface = MdSurface,
    surfaceVariant = MdSurfaceVariant,
    surfaceContainer = MdSurfaceContainerLight,
    surfaceContainerHigh = MdSurfaceContainerHighLight,
    surfaceContainerLow = MdSurfaceContainerLowLight,
    onBackground = MdOnSurface,
    onSurface = MdOnSurface,
    onSurfaceVariant = MdOnSurfaceVariant,
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0)
)

@Composable
fun ExteraLightTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    customSeedColor: Color? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val baseScheme = when {
        customSeedColor != null -> {
            if (darkTheme) {
                DarkColorScheme.copy(
                    primary = customSeedColor,
                    primaryContainer = customSeedColor.copy(alpha = 0.25f),
                    onPrimaryContainer = Color.White
                )
            } else {
                LightColorScheme.copy(
                    primary = customSeedColor,
                    primaryContainer = customSeedColor.copy(alpha = 0.18f),
                    onPrimaryContainer = customSeedColor
                )
            }
        }
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = baseScheme,
        typography = Typography(),
        content = content
    )
}
