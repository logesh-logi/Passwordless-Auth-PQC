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
import com.fidoauth.pqcclient.dto.RegisterDto
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
     * Handles user registration with animations and biometric authentication.
     */
    fun registerUser(activity: FragmentActivity, username: String, email: String) {
        viewModelScope.launch {
            resetState()
            _isLoading.value = true

            try {
                val context = getApplication<Application>().applicationContext
                val authService = RetrofitClient.create(context)

                // Step 1: Request challenge from server
                val challengeDto: RegistrationRequestDto = authService.startRegistration(RegisterDto(username, email))

                // Step 2: Perform biometric authentication immediately
                if (!performBiometricAuth(activity)) {
                    _authStatus.value = "Error: Biometric authentication failed"
                    _isLoading.value = false
                    return@launch
                }

                // Step 3: Show key generation animation
                _showKeyGeneration.value = true
                delay(2000)  // Delay for better visibility

                // Step 4: Generate keys and sign the challenge
                val challenge = challengeDto.challenge
                val signatureResult = CryptoManager.generateKeysAndSign(context, challenge)

                // Step 5: Show server communication animation
                _showServerComm.value = true
                delay(1500)

                // Step 6: Send registration completion request
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

                // Step 7: Show success animation
                delay(1000)
                _showSuccess.value = true
                _authStatus.value = "Success: Registration completed. Token stored securely."
            } catch (e: Exception) {
                handleFailure("Error: Registration failed - ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Handles user login with animations and biometric authentication.
     */
    fun loginUser(activity: FragmentActivity, username: String) {
        viewModelScope.launch {
            resetState()
            _isLoading.value = true

            try {
                val context = getApplication<Application>().applicationContext
                val authService = RetrofitClient.create(context)

                // Step 1: Request challenge from server
                val challengeDto = authService.startLogin(username)

                // Step 2: Perform biometric authentication immediately
                if (!performBiometricAuth(activity)) {
                    _authStatus.value = "Error: Biometric authentication failed"
                    _isLoading.value = false
                    return@launch
                }

                // Step 3: Show key generation animation
                _showKeyGeneration.value = true
                delay(2000)  // Delay for better visibility

                // Step 4: Sign challenge using stored keys
                val challenge = challengeDto.challenge
                val signatureResult = CryptoManager.signForLogin(context, challenge)

                // Step 5: Show server communication animation
                _showServerComm.value = true
                delay(1500)

                // Step 6: Send login request
                val loginRequestDto = LoginResponseDto(
                    userid = challengeDto.userid,
                    signatureRSA = signatureResult.rsaSignature,
                    signatureDilithium = signatureResult.dilithiumSignature
                )

                val result = authService.finishLogin(loginRequestDto, username)

                // Store token securely
                SecureStorage.saveAuthToken(context, result.token)

                // Step 7: Show success animation
                delay(1000)
                _showSuccess.value = true
                _authStatus.value = "Success: Logged in. Token stored securely."
            } catch (e: Exception) {
                handleFailure("Error: Login failed - ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Performs biometric authentication and immediately handles failures.
     */
    private suspend fun performBiometricAuth(activity: FragmentActivity): Boolean =
        suspendCoroutine { continuation ->
            val executor: Executor = ContextCompat.getMainExecutor(activity)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Quantum-Secure Authentication")
                .setSubtitle("Verify your identity")
                .setDescription("Your biometric is used to authorize cryptographic operations locally")
                .setNegativeButtonText("Cancel")
                .build()

            val biometricPrompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        _authStatus.value = "Biometric verification successful"
                        continuation.resume(true)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        handleFailure("Biometric verification failed: $errString")
                        continuation.resume(false)
                    }

                    override fun onAuthenticationFailed() {
                        handleFailure("Biometric verification failed")
                        continuation.resume(false)
                    }
                })

            biometricPrompt.authenticate(promptInfo)
        }

    /**
     * Resets all state values before starting authentication.
     */
    private fun resetState() {
        _authStatus.value = ""
        _showKeyGeneration.value = false
        _showServerComm.value = false
        _showSuccess.value = false
    }

    /**
     * Handles errors and resets UI animations immediately.
     */
    private fun handleFailure(message: String) {
        _authStatus.value = message
        _showKeyGeneration.value = false
        _showServerComm.value = false
        _showSuccess.value = false
        _isLoading.value = false
    }
}
