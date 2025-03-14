package com.fidoauth.pqcclient.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AuthStatusCard(statusMessage: String) {
    val isSuccess = statusMessage.startsWith("Success")

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSuccess) Color(0xFF1B5E20).copy(alpha = 0.2f)
            else Color(0xFFC62828).copy(alpha = 0.2f),
            contentColor = if (isSuccess) Color(0xFF81C784) else Color(0xFFEF9A9A)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isSuccess) "✅ Authentication Successful" else "❌ Authentication Failed",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = statusMessage.replace("Success: ", "").replace("Error: ", ""),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}