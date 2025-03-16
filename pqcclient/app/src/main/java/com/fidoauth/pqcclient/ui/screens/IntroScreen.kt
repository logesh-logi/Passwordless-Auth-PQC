package com.fidoauth.pqcclient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun IntroScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    // Using the existing theme colors with slight adjustments
    val colorProblem = Color(0xFFE57373) // Error color from theme
    val colorSolution = Color(0xFF00BFA5) // MintGreen/Tertiary from theme
    val colorHybrid = Color(0xFF4285F4)   // ElectricBlue/Primary from theme

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(35.dp))

            Text(
                "Post-Quantum Cryptography",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                "Secure your digital future",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // The Problem Card
            StepCard(
                title = "The Problem",
                description = "Traditional cryptography is vulnerable to quantum attacks. When quantum computers become powerful enough, they will break RSA and ECC.",
                color = colorProblem,
                number = "1"
            )

            // The Solution Card
            StepCard(
                title = "The Solution",
                description = "Post-quantum cryptography uses mathematical problems that resist quantum attacks, ensuring security in the quantum era.",
                color = colorSolution,
                number = "2"
            )

            // Hybrid Approach Card
            StepCard(
                title = "Hybrid Approach",
                description = "Our system uses both RSA and Dilithium signatures, combining classical and quantum-resistant cryptography for maximum security.",
                color = colorHybrid,
                number = "3"
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { navController.navigate("auth") },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Text(
                    "Start Application",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StepCard(
    title: String,
    description: String,
    color: Color,
    number: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Number circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    number,
                    color = Color.Black,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = color,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}