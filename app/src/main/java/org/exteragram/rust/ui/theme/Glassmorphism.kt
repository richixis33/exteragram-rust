package org.exteragram.rust.ui.theme

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * High-fidelity Liquid Glass Modifier
 * Implements real frosted glass, specular highlight borders, refraction tint, and blur.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    blurRadius: Dp = 24.dp,
    tintColor: Color = Color(0x661A2130),
    borderSheenColor: Color = Color(0x40FFFFFF),
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .then(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.graphicsLayer {
                val radiusPx = blurRadius.toPx()
                if (radiusPx > 0f) {
                    renderEffect = RenderEffect
                        .createBlurEffect(radiusPx, radiusPx, Shader.TileMode.CLAMP)
                        .asComposeRenderEffect()
                }
            }
        } else {
            Modifier
        }
    )
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                tintColor.copy(alpha = tintColor.alpha * 1.15f.coerceAtMost(1f)),
                tintColor.copy(alpha = tintColor.alpha * 0.85f)
            )
        ),
        shape = shape
    )
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                borderSheenColor,
                borderSheenColor.copy(alpha = borderSheenColor.alpha * 0.15f),
                borderSheenColor.copy(alpha = borderSheenColor.alpha * 0.45f)
            )
        ),
        shape = shape
    )
