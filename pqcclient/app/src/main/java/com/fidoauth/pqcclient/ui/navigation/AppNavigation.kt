package com.fidoauth.pqcclient.ui.navigation

import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fidoauth.pqcclient.ui.screens.AuthScreen
import com.fidoauth.pqcclient.ui.screens.DashboardScreen
import com.fidoauth.pqcclient.ui.screens.IntroScreen
import com.fidoauth.pqcclient.ui.screens.SplashScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(activity: FragmentActivity) {
    val navController = rememberNavController()

    // Add a splash screen for initial entry
    var showSplash by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        coroutineScope.launch {
            delay(2500)
            showSplash = false
        }
    }

    if (showSplash) {
        SplashScreen()
    } else {
        NavHost(navController = navController, startDestination = "intro") {
            composable("intro") { IntroScreen(navController) }
            composable("auth") { AuthScreen(navController, activity) }
            composable("dashboard") { DashboardScreen(navController) }
        }
    }
}