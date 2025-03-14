package com.fidoauth.pqcclient.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fidoauth.pqcclient.ui.components.AnimatedAuthProcess
import com.fidoauth.pqcclient.ui.components.AuthStatusCard
import com.fidoauth.pqcclient.viewmodel.AuthViewModel
import androidx.compose.material3.CircularProgressIndicator

@Composable
fun AuthScreen(navController: NavController, activity: FragmentActivity) {
    val authViewModel: AuthViewModel = viewModel()

    val authStatus by authViewModel.authStatus.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val showKeyGeneration by authViewModel.showKeyGeneration.collectAsState()
    val showServerComm by authViewModel.showServerComm.collectAsState()
    val showSuccess by authViewModel.showSuccess.collectAsState()

    var username by remember { mutableStateOf("") }

    // Disable text input during loading
    val inputEnabled = !isLoading

    // Main content
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Header section
            AuthHeader()

            // Card containing login form
            AuthCard(
                username = username,
                onUsernameChange = { if (inputEnabled) username = it },
                isLoading = isLoading,
                onRegister = { authViewModel.registerUser(username) },
                onLogin = { authViewModel.loginUser(activity, username) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Animated Authentication Process - always show the container, visibility of steps handled inside
            AnimatedAuthProcess(
                showKeyGeneration = showKeyGeneration,
                showServerComm = showServerComm,
                showSuccess = showSuccess
            )

            Spacer(modifier = Modifier.height(24.dp))

            // "Go to Dashboard" Button (Visible Only After Success)
            AnimatedVisibility(visible = showSuccess) {
                Button(
                    onClick = { navController.navigate("dashboard") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Go to Dashboard", style = MaterialTheme.typography.titleMedium)
                }
            }

            // Status Message Card - show after animation completes or on error
            AnimatedVisibility(visible = authStatus.isNotEmpty()) {
                AuthStatusCard(statusMessage = authStatus)
            }
        }

        // Overlay loading indicator when processing
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}
@Composable
fun AuthHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Quantum-Resistant",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Authentication",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Protected by hybrid RSA-Dilithium keys",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AuthCard(
    username: String,
    onUsernameChange: (String) -> Unit,
    isLoading: Boolean,
    onRegister: () -> Unit,
    onLogin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Username Input
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Action buttons
            AuthButtons(
                username = username,
                isLoading = isLoading,
                onRegister = onRegister,
                onLogin = onLogin
            )
        }
    }
}

@Composable
fun AuthButtons(
    username: String,
    isLoading: Boolean,
    onRegister: () -> Unit,
    onLogin: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Register Button
        Button(
            onClick = onRegister,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = username.isNotBlank() && !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Register", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Login Button
        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = username.isNotBlank() && !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Login", style = MaterialTheme.typography.titleMedium)
        }
    }
}
