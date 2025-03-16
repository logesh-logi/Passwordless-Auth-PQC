package com.fidoauth.pqcclient.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
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
    isRegisterMode: Boolean,
    showKeyGeneration: Boolean,
    showServerComm: Boolean,
    showSuccess: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Key Generation/Loading Step
        AnimatedVisibility(
            visible = showKeyGeneration,
            enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300))
        ) {
            AuthProcessStep(
                active = showKeyGeneration && !showServerComm && !showSuccess,
                completed = showServerComm || showSuccess,
                label = if (isRegisterMode) "Generating Secure Keys" else "Loading Secure Keys"
            )
        }

        // Server Communication Step
        AnimatedVisibility(
            visible = showServerComm || showSuccess,
            enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300))
        ) {
            AuthProcessStep(
                active = showServerComm && !showSuccess,
                completed = showSuccess,
                label = "Communicating with Server"
            )
        }

        // Success Step
        AnimatedVisibility(
            visible = showSuccess,
            enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300))
        ) {
            AuthProcessStep(
                active = showSuccess,
                completed = showSuccess,
                label = "Authentication Successful"
            )
        }
    }
}


@Composable
fun AuthProcessStep(
    active: Boolean,
    completed: Boolean,
    label: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status indicator
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    when {
                        completed -> MaterialTheme.colorScheme.primary
                        active -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (active && !completed) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else if (completed) {
                Text(
                    "✓",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                active || completed -> MaterialTheme.colorScheme.onBackground
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            }
        )
    }
}