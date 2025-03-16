package com.fidoauth.pqcclient.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fidoauth.pqcclient.auth.SecureStorage
import com.fidoauth.pqcclient.network.RetrofitClient
import com.fidoauth.pqcclient.ui.components.SecurityDetailItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Base64

@Composable
fun MinimalistSecurityIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "securityAnimation")
    val pulseEffect by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseEffect"
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .size(90.dp)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(90.dp)) {
            drawCircle(
                color = primaryColor.copy(alpha = 0.2f * pulseEffect),
                radius = size.minDimension / 2f
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.5f),
                radius = size.minDimension / 3f
            )
        }

        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = "Security Indicator",
            tint = primaryColor,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    var responseText by remember { mutableStateOf("No data fetched yet") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(45.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MinimalistSecurityIndicator()

            Spacer(modifier = Modifier.height(16.dp))

            // Main Data Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Protected Data",
                        style = MaterialTheme.typography.titleMedium,
                        color = onSurfaceColor,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Increased Box height and added scrolling
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp) // Increased height
                            .background(Color.DarkGray.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()), // Enables scrolling
                        contentAlignment = Alignment.Center
                    ) {
                        SelectionContainer { // Allows text selection
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = primaryColor,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = responseText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = onSurfaceColor.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                try {
                                    delay(1000)
                                    val response = RetrofitClient.create(context).getProtectedData()

                                    response.let {
                                        val username = it.username ?: "Unknown"
                                        val email = it.email ?: "Unknown"
                                        val rsaKeyHex = it.publicKeyRSA.base64ToHex().shortenHex() ?: "N/A"
                                        val dilithiumKeyHex = it.publicKeyDilithium.base64ToHex().shortenHex()?: "N/A"

                                        responseText = """
                                                                            🔐 User: $username
                                                                            🔐 email: $email
                                                                            🔑 RSA Public Key: 
                                                                            $rsaKeyHex
                                                                            
                                                                            🔑 Dilithium Public Key:
                                                                            $dilithiumKeyHex
                                                                        """.trimIndent()
                                    } ?: run {
                                        responseText = "Error: Empty response"
                                    }
                                } catch (e: Exception) {
                                    responseText = "Error: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = onPrimaryColor
                        ),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Access Data", fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Security Details",
                        style = MaterialTheme.typography.titleSmall,
                        color = primaryColor,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SecurityDetailItem("Hybrid Keys", "RSA + Dilithium-3", onSurfaceColor.copy(0.9f), onSurfaceColor.copy(0.6f))
                    SecurityDetailItem("Key Size", "3072 | 4000(private key)", onSurfaceColor.copy(0.9f), onSurfaceColor.copy(0.6f))
                    SecurityDetailItem("Claimed NIST Level Quantum Security", "Level 3", onSurfaceColor.copy(0.9f), onSurfaceColor.copy(0.6f))
                    SecurityDetailItem("Session", "120 minutes", onSurfaceColor.copy(0.9f), onSurfaceColor.copy(0.6f))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout Button
            TextButton(
                onClick = {
                    SecureStorage.clearAuthToken(context)
                    navController.navigate("auth") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F)),
                modifier = Modifier.fillMaxWidth(0.5f).height(44.dp)
            ) {
                Text("Logout", fontWeight = FontWeight.Medium)
            }
        }
    }
}

// Function to convert Base64 to Hexadecimal
fun String.base64ToHex(): String {
    return Base64.decode(this, Base64.DEFAULT)
        .joinToString("") { "%02x".format(it) }
}

fun String.shortenHex(): String {
    return if (this.length > 20) {
        "${this.take(20)}....................${this.takeLast(20)}"
    } else {
        this // If the key is short, return as is
    }
}

