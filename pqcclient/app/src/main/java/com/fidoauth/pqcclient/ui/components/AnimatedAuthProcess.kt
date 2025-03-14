package com.fidoauth.pqcclient.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun AnimatedAuthProcess(
    showKeyGeneration: Boolean,
    showServerComm: Boolean,
    showSuccess: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "authAnimation")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAnimation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Only render the component if at least one step is active
        if (showKeyGeneration || showServerComm || showSuccess) {
            // Step 1: Key Generation
            ProcessStep(
                title = "Generating Keys",
                description = "Creating hybrid RSA-Dilithium key pair",
                isActive = showKeyGeneration && !showServerComm && !showSuccess,
                isCompleted = (showServerComm || showSuccess) && showKeyGeneration,
                isVisible = true,  // Always visible when component renders
                dotAlpha = dotAlpha
            )

            // Connecting line between steps
            if (showKeyGeneration) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(16.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                )
            }

            // Step 2: Server Communication
            ProcessStep(
                title = "Server Verification",
                description = "Verifying signatures with the server",
                isActive = showServerComm && !showSuccess,
                isCompleted = showSuccess && showServerComm,
                isVisible = showKeyGeneration,  // Only visible after key generation starts
                dotAlpha = dotAlpha
            )

            // Connecting line between steps
            if (showServerComm) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(16.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                )
            }

            // Step 3: Success
            ProcessStep(
                title = "Authentication Complete",
                description = "Protected with quantum-resistant security",
                isActive = false,  // Never in "active" state, only completed
                isCompleted = showSuccess,
                isVisible = showServerComm,  // Only visible after server comm starts
                dotAlpha = dotAlpha
            )
        }
    }
}

@Composable
fun ProcessStep(
    title: String,
    description: String,
    isActive: Boolean,
    isCompleted: Boolean,
    isVisible: Boolean = true,
    dotAlpha: Float
) {
    if (!isVisible) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isVisible) 1f else 0f)
            .padding(vertical = 4.dp)
    ) {
        // Status indicator
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .padding(end = 12.dp)
        ) {
            when {
                isCompleted -> {
                    // Show checkmark for completed step using Material icons
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                isActive -> {
                    // Show pulsing circle for active step
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .alpha(dotAlpha)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.small
                            )
                    )
                }
                else -> {
                    // Show empty circle for future step
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .alpha(0.3f)
                            .background(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.small
                            )
                    )
                }
            }
        }

        // Step text content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    isCompleted -> MaterialTheme.colorScheme.primary
                    isActive -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                }
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isActive || isCompleted) 0.7f else 0.4f),
                textAlign = TextAlign.Start
            )
        }
    }
}