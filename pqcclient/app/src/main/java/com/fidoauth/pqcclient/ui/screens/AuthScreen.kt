package com.fidoauth.pqcclient.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.fidoauth.pqcclient.viewmodel.AuthViewModel
import com.fidoauth.pqcclient.ui.components.AuthStatusCard

@Composable
fun AuthScreen(navController: NavController, activity: FragmentActivity) {
    val authViewModel: AuthViewModel = viewModel(activity)

    val authStatus by authViewModel.authStatus.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val showKeyGeneration by authViewModel.showKeyGeneration.collectAsState()
    val showServerComm by authViewModel.showServerComm.collectAsState()
    val showSuccess by authViewModel.showSuccess.collectAsState()

    // Auth mode state
    var isRegisterMode by remember { mutableStateOf(true) }

    // Form state
    var registerUsername by remember { mutableStateOf("") }
    var registerEmail by remember { mutableStateOf("") }
    var loginUsername by remember { mutableStateOf("") }

    // Flag to determine if the auth process has started
    val authStarted = showKeyGeneration || showServerComm || showSuccess

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val scrollState = rememberScrollState()
        val availableHeight = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Adjustable top spacing
            Spacer(modifier = Modifier.height(availableHeight * 0.08f))

            // Header
            AuthHeader()

            Spacer(modifier = Modifier.height(32.dp))

            // Auth mode selector
            AuthModeSelector(
                isRegisterMode = isRegisterMode,
                onModeChange = { isRegisterMode = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Auth form based on selected mode
            AuthFormCard(
                isRegisterMode = isRegisterMode,
                registerUsername = registerUsername,
                registerEmail = registerEmail,
                loginUsername = loginUsername,
                onRegisterUsernameChange = { registerUsername = it },
                onRegisterEmailChange = { registerEmail = it },
                onLoginUsernameChange = { loginUsername = it },
                isLoading = isLoading,
                onRegister = { authViewModel.registerUser(activity, registerUsername, registerEmail) },
                onLogin = { authViewModel.loginUser(activity, loginUsername) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Only show the authentication process section if auth has started
            AnimatedVisibility(
                visible = authStarted,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
            ) {
                Column {
                    // Process title
                    Text(
                        text = "Authentication Progress",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Animated Authentication Process
                    AnimatedAuthProcess(
                        isRegisterMode = isRegisterMode,
                        showKeyGeneration = showKeyGeneration,
                        showServerComm = showServerComm,
                        showSuccess = showSuccess
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Dashboard navigation button
            AnimatedVisibility(
                visible = showSuccess,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(500))
            ) {
                DashboardButton(onClick = { navController.navigate("dashboard") })
            }

            // Status message
            AnimatedVisibility(
                visible = authStatus.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(500))
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                AuthStatusCard(statusMessage = authStatus)
            }

            // Adjustable bottom spacing
            Spacer(modifier = Modifier.height(availableHeight * 0.08f))
        }

        // Loading overlay
        if (isLoading) {
            LoadingOverlay()
        }
    }
}

@Composable
fun AuthHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            "PQC-Enhanced Password-less",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            "Authentication",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Protected by hybrid RSA-Dilithium keys",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AuthModeSelector(
    isRegisterMode: Boolean,
    onModeChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        TabRow(
            selectedTabIndex = if (isRegisterMode) 0 else 1,
            modifier = Modifier.width(320.dp),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (isRegisterMode) 0 else 1]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 3.dp
                )
            },
            divider = {}
        ) {
            Tab(
                selected = isRegisterMode,
                onClick = { onModeChange(true) },
                text = {
                    Text(
                        "Register",
                        fontWeight = if (isRegisterMode) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = !isRegisterMode,
                onClick = { onModeChange(false) },
                text = {
                    Text(
                        "Login",
                        fontWeight = if (!isRegisterMode) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
fun AuthFormCard(
    isRegisterMode: Boolean,
    registerUsername: String,
    registerEmail: String,
    loginUsername: String,
    onRegisterUsernameChange: (String) -> Unit,
    onRegisterEmailChange: (String) -> Unit,
    onLoginUsernameChange: (String) -> Unit,
    isLoading: Boolean,
    onRegister: () -> Unit,
    onLogin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (isRegisterMode) {
                // Register form
                AuthTextField(
                    value = registerUsername,
                    onValueChange = onRegisterUsernameChange,
                    label = "Username",
                    enabled = !isLoading
                )

                AuthTextField(
                    value = registerEmail,
                    onValueChange = onRegisterEmailChange,
                    label = "Email",
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                AuthButton(
                    text = "Register",
                    onClick = onRegister,
                    enabled = registerUsername.isNotBlank() && registerEmail.isNotBlank() && !isLoading
                )
            } else {
                // Login form
                AuthTextField(
                    value = loginUsername,
                    onValueChange = onLoginUsernameChange,
                    label = "Username",
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                AuthButton(
                    text = "Login",
                    onClick = onLogin,
                    enabled = loginUsername.isNotBlank() && !isLoading
                )
            }
        }
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            focusedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun AuthButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DashboardButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            "Go to Dashboard",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp),
            strokeWidth = 4.dp
        )
    }
}


