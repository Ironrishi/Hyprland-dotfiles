package com.disaster.management.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand palette
val RedAlert   = Color(0xFFD32F2F)
val OrangeWarn = Color(0xFFE64A19)
val GreenSafe  = Color(0xFF388E3C)
val DeepBlue   = Color(0xFF1565C0)
val SurfaceGray = Color(0xFFF5F5F5)
val CardWhite  = Color(0xFFFFFFFF)

// Risk colours
val RiskHigh   = Color(0xFFD32F2F)
val RiskMedium = Color(0xFFF57C00)
val RiskLow    = Color(0xFF388E3C)
val RiskUnknown = Color(0xFF757575)

private val LightColors = lightColorScheme(
    primary        = DeepBlue,
    onPrimary      = Color.White,
    secondary      = OrangeWarn,
    onSecondary    = Color.White,
    error          = RedAlert,
    background     = SurfaceGray,
    surface        = CardWhite,
    onBackground   = Color(0xFF1C1B1F),
    onSurface      = Color(0xFF1C1B1F),
)

@Composable
fun DisasterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
