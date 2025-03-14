package com.fidoauth.pqcclient.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fidoauth.pqcclient.auth.SecureStorage
import com.fidoauth.pqcclient.network.RetrofitClient
import com.fidoauth.pqcclient.ui.components.SecurityDetailItem
import com.fidoauth.pqcclient.ui.theme.QuantumColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    var responseText by remember { mutableStateOf("No data fetched yet") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Enhanced security status animations
    val infiniteTransition = rememberInfiniteTransition(label = "securityAnimations")

    // Pulse animation
    val securityPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseEffect"
    )

    // Rotation animation for shield effect
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing)
        ),
        label = "rotationEffect"
    )

    // Scanner line animation
    val scannerPosition by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scannerEffect"
    )

    // Pre-fetch theme colors for Canvas usage
    val gridColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                    )
                )
            )
    ) {
        // Background grid effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 40f
            val gridAlpha = 0.07f

            for (i in 0..size.width.toInt() step gridSpacing.toInt()) {
                drawLine(
                    color = gridColor.copy(alpha = gridAlpha),
                    start = Offset(i.toFloat(), 0f),
                    end = Offset(i.toFloat(), size.height),
                    strokeWidth = 1f
                )
            }

            for (i in 0..size.height.toInt() step gridSpacing.toInt()) {
                drawLine(
                    color = gridColor.copy(alpha = gridAlpha),
                    start = Offset(0f, i.toFloat()),
                    end = Offset(size.width, i.toFloat()),
                    strokeWidth = 1f
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Enhanced security indicator with rotation and gradient
                Canvas(modifier = Modifier.size(180.dp)) {
                    // Outer rotating circle
                    val outerCircleRadius = size.minDimension / 2
                    val dotCount = 36
                    val dotRadius = 3f

                    for (i in 0 until dotCount) {
                        val angle = (i * 360f / dotCount + rotation) % 360
                        val radius = outerCircleRadius * 0.9f
                        val x = center.x + radius * cos(Math.toRadians(angle.toDouble())).toFloat()
                        val y = center.y + radius * sin(Math.toRadians(angle.toDouble())).toFloat()

                        drawCircle(
                            color = tertiaryColor.copy(alpha = 0.7f),
                            radius = dotRadius,
                            center = Offset(x, y)
                        )
                    }

                    // Inner circle with gradient
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                tertiaryColor.copy(alpha = 0.2f * securityPulse),
                                tertiaryColor.copy(alpha = 0.1f * securityPulse),
                                Color.Transparent
                            )
                        ),
                        radius = outerCircleRadius * 0.7f
                    )

                    // Protection rings
                    drawCircle(
                        color = tertiaryColor.copy(alpha = 0.6f * securityPulse),
                        radius = outerCircleRadius * 0.6f,
                        style = Stroke(width = 2f)
                    )

                    drawCircle(
                        color = tertiaryColor.copy(alpha = 0.3f * securityPulse),
                        radius = outerCircleRadius * 0.7f,
                        style = Stroke(width = 1f)
                    )

                    // Scanner line effect
                    drawLine(
                        color = QuantumColors.ScannerLine,
                        start = Offset(center.x - outerCircleRadius * 0.6f, center.y + scannerPosition),
                        end = Offset(center.x + outerCircleRadius * 0.6f, center.y + scannerPosition),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(20.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            CircleShape
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        "QUANTUM SECURE",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Advanced Protection",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Rest of the code remains the same...
        }
    }
}