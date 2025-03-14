package com.fidoauth.pqcclient.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Modern cybersecurity-inspired color palette
private val CyberBlue = Color(0xFF2C7BF2)       // Primary - Slightly darker blue
private val VioletPurple = Color(0xFF8E24AA)    // Secondary - Rich vibrant purple
private val NeonGreen = Color(0xFF00E676)       // Tertiary - Bright green for security indicators
private val CyberCyan = Color(0xFF00B8D4)       // Accent for highlights

// Dark theme base colors with more depth
private val DarkBackgroundAbyss = Color(0xFF0A0A0A)  // Deeper black
private val DarkSurfaceElevated = Color(0xFF141414)  // Slightly lighter surface
private val DarkCardElevated = Color(0xFF1C1C1C)     // Card background
private val PureWhite = Color(0xFFFFFFFF)            // Brighter white text
private val SilverGray = Color(0xFFBDBDBD)           // Slightly brighter silver
private val DeepCharcoal = Color(0xFF212121)         // Darker gray for contrast

// Alert colors
private val SuccessGreen = Color(0xFF00C853)
private val WarningAmber = Color(0xFFFFAB00)
private val ErrorRed = Color(0xFFD50000)

private val EnhancedQuantumDarkScheme = darkColorScheme(
    primary = CyberBlue,
    secondary = VioletPurple,
    tertiary = NeonGreen,
    background = DarkBackgroundAbyss,
    surface = DarkSurfaceElevated,
    surfaceVariant = DarkCardElevated,
    error = ErrorRed,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onTertiary = DeepCharcoal,
    onBackground = PureWhite,
    onSurface = PureWhite,
    onSurfaceVariant = SilverGray,
    onError = PureWhite,
    primaryContainer = CyberBlue.copy(alpha = 0.15f),
    secondaryContainer = VioletPurple.copy(alpha = 0.15f),
    tertiaryContainer = NeonGreen.copy(alpha = 0.15f),
    onPrimaryContainer = CyberBlue,
    onSecondaryContainer = VioletPurple,
    onTertiaryContainer = NeonGreen
)

// Additional theme colors as a companion object
object QuantumColors {
    val SuccessGreen = Color(0xFF00C853)
    val WarningAmber = Color(0xFFFFAB00)
    val ErrorRed = Color(0xFFD50000)
    val CyberCyan = Color(0xFF00B8D4)

    // Gradient colors for security indicators
    val SecurityGradientStart = Color(0xFF2C7BF2)
    val SecurityGradientEnd = Color(0xFF00E676)

    // Scanner effect colors
    val ScannerLine = Color(0xFF00E676).copy(alpha = 0.8f)
}

private val EnhancedQuantumTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun QuantumSecureTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Always use enhanced dark theme for this security app
    MaterialTheme(
        colorScheme = EnhancedQuantumDarkScheme,
        typography = EnhancedQuantumTypography,
        content = content
    )
}