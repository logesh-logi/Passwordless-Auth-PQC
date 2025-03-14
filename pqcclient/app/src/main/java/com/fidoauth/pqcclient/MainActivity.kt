package com.fidoauth.pqcclient

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import com.fidoauth.pqcclient.ui.navigation.AppNavigation
import com.fidoauth.pqcclient.ui.theme.QuantumSecureTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuantumSecureTheme {
                AppNavigation(this)
            }
        }
    }
}