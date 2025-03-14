package com.fidoauth.pqcclient.viewmodel

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import com.fidoauth.pqcclient.auth.SecureStorage
import com.fidoauth.pqcclient.dto.RegistrationResponseDto
import com.fidoauth.pqcclient.dto.RegistrationRequestDto
import com.fidoauth.pqcclient.dto.LoginResponseDto
import com.fidoauth.pqcclient.network.RetrofitClient
import com.fidoauth.pqcclient.auth.CryptoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import java.util.concurrent.Executor

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // Authentication state
    private val _authStatus = MutableStateFlow("")
    val authStatus: StateFlow<String> = _authStatus

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Animation states
    private val _showKeyGeneration = MutableStateFlow(false)
    val showKeyGeneration: StateFlow<Boolean> = _showKeyGeneration

    private val _showServerComm = MutableStateFlow(false)
    val showServerComm: StateFlow<Boolean> = _showServerComm

    private val _showSuccess = MutableStateFlow(false)
    val showSuccess: StateFlow<Boolean> = _showSuccess

    /**
     * Handles user registration with animation updates.
     */
// Updates for AuthViewModel.kt

    fun registerUser(username: String) {
        viewModelScope.launch {
            resetState()
            _isLoading.value = true

            try {
                val context = getApplication<Application>().applicationContext
                val authService = RetrofitClient.create(context)

                // Step 1: Show key generation animation
                _showKeyGeneration.value = true
                delay(2000)  // Increased delay for better visibility

                // Step 2: Request challenge from server
                val challengeDto: RegistrationRequestDto = authService.startRegistration(username)

                // Step 3: Show server communication animation
                _showServerComm.value = true
                delay(1500)

                val challenge = challengeDto.challenge
                val signatureResult = CryptoManager.generateKeysAndSign(context, challenge)

                // Step 4: Send registration completion request
                val registrationResponseDto = RegistrationResponseDto(
                    userid = challengeDto.userid,
                    challenge = challenge,
                    publicKeyRSA = signatureResult.rsaPublicKey,
                    signatureRSA = signatureResult.rsaSignature,
                    publicKeyDilithium = signatureResult.dilithiumPublicKey,
                    signatureDilithium = signatureResult.dilithiumSignature
                )

                val result = authService.finishRegistration(registrationResponseDto, username)

                // Store token securely
                SecureStorage.saveAuthToken(context, result.token)

                // Step 5: Show success animation
                delay(1000)  // Add delay before success to show server communication
                _showSuccess.value = true
                _authStatus.value = "Success: Registration completed. Token stored securely."
            } catch (e: Exception) {
                // Reset animations on error
                _showKeyGeneration.value = false
                _showServerComm.value = false
                _authStatus.value = "Error: Registration failed - ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginUser(activity: FragmentActivity, username: String) {
        viewModelScope.launch {
            resetState()
            _isLoading.value = true

            try {
                val context = getApplication<Application>().applicationContext
                val authService = RetrofitClient.create(context)

                // Step 1: Perform biometric authentication before login
                if (!performBiometricAuth(activity)) {
                    _authStatus.value = "Error: Biometric authentication failed"
                    _isLoading.value = false
                    return@launch
                }

                // Step 2: Show key generation animation
                _showKeyGeneration.value = true
                delay(2000)  // Increased delay for better visibility

                // Step 3: Request challenge from server
                val challengeDto = authService.startLogin(username)

                // Step 4: Show server communication animation
                _showServerComm.value = true
                delay(1500)

                val challenge = challengeDto.challenge
                val signatureResult = CryptoManager.generateKeysAndSign(context, challenge)

                // Step 5: Send login completion request
                val loginRequestDto = LoginResponseDto(
                    userid = challengeDto.userid,
                    signatureRSA = signatureResult.rsaSignature,
                    signatureDilithium = signatureResult.dilithiumSignature
                )

                val result = authService.finishLogin(loginRequestDto, username)

                // Store token securely
                SecureStorage.saveAuthToken(context, result.token)

                // Step 6: Show success animation
                delay(1000)  // Add delay before success to show server communication
                _showSuccess.value = true
                _authStatus.value = "Success: Logged in. Token stored securely."
            } catch (e: Exception) {
                // Reset animations on error
                _showKeyGeneration.value = false
                _showServerComm.value = false
                _authStatus.value = "Error: Login failed - ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Resets all state values before starting authentication.
     */
    private fun resetState() {
        _authStatus.value = ""
        _showKeyGeneration.value = false
        _showServerComm.value = false
        _showSuccess.value = false
        // Don't reset isLoading here, as it's set immediately after this call
    }
    /**
     * Perform biometric authentication.
     */
    private suspend fun performBiometricAuth(activity: FragmentActivity): Boolean =
        suspendCoroutine { continuation ->
            val executor: Executor = ContextCompat.getMainExecutor(activity)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Authenticate to sign challenge")
                .setNegativeButtonText("Cancel")
                .build()

            val biometricPrompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        continuation.resume(true)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        continuation.resume(false)
                    }

                    override fun onAuthenticationFailed() {
                        continuation.resume(false)
                    }
                })

            biometricPrompt.authenticate(promptInfo)
        }
}
