package com.fidoauth.pqcclient.viewmodel

import android.util.Log
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

    private val TAG = "AuthViewModel"

    private val _authStatus = MutableStateFlow("")
    val authStatus: StateFlow<String> = _authStatus

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _showKeyGeneration = MutableStateFlow(false)
    val showKeyGeneration: StateFlow<Boolean> = _showKeyGeneration

    private val _showServerComm = MutableStateFlow(false)
    val showServerComm: StateFlow<Boolean> = _showServerComm

    private val _showSuccess = MutableStateFlow(false)
    val showSuccess: StateFlow<Boolean> = _showSuccess

    fun registerUser(activity: FragmentActivity, username: String, email: String) {
        viewModelScope.launch {
            resetState()
            _isLoading.value = true
            Log.d(TAG, "Starting registration for user: $username")

            try {
                val context = getApplication<Application>().applicationContext
                val authService = RetrofitClient.create(context)

                Log.d(TAG, "Requesting challenge from server...")
                val challengeDto: RegistrationRequestDto = authService.startRegistration(RegisterDto(username, email))
                Log.d(TAG, "Received challenge: ${challengeDto.challenge}")

                if (!performBiometricAuth(activity)) {
                    Log.e(TAG, "Biometric authentication failed")
                    _authStatus.value = "Error: Biometric authentication failed"
                    _isLoading.value = false
                    return@launch
                }

                _showKeyGeneration.value = true
                Log.d(TAG, "Biometric authentication successful. Generating keys...")
                delay(2000)

                val challenge = challengeDto.challenge
                val signatureResult = CryptoManager.generateKeysAndSign(context, challenge)
                Log.d(TAG, "Generated keys and signatures successfully")

                _showServerComm.value = true
                Log.d(TAG, "Sending registration completion request...")
                delay(1500)

                val registrationResponseDto = RegistrationResponseDto(
                    userid = challengeDto.userid,
                    challenge = challenge,
                    publicKeyRSA = signatureResult.rsaPublicKey,
                    signatureRSA = signatureResult.rsaSignature,
                    publicKeyDilithium = signatureResult.dilithiumPublicKey,
                    signatureDilithium = signatureResult.dilithiumSignature
                )

                val result = authService.finishRegistration(registrationResponseDto, username)
                Log.d(TAG, "Registration successful. Token received.")

                SecureStorage.saveAuthToken(context, result.token)

                delay(1000)
                _showSuccess.value = true
                _authStatus.value = "Success: Registration completed. Token stored securely."
                Log.d(TAG, "Registration process completed successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Registration failed: ${e.message}", e)
                handleFailure("Error: Registration failed - ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginUser(activity: FragmentActivity, username: String) {
        viewModelScope.launch {
            resetState()
            _isLoading.value = true
            Log.d(TAG, "Starting login for user: $username")

            try {
                val context = getApplication<Application>().applicationContext
                val authService = RetrofitClient.create(context)

                Log.d(TAG, "Requesting login challenge from server...")
                val challengeDto = authService.startLogin(username)
                Log.d(TAG, "Received challenge: ${challengeDto.challenge}")

                if (!performBiometricAuth(activity)) {
                    Log.e(TAG, "Biometric authentication failed")
                    _authStatus.value = "Error: Biometric authentication failed"
                    _isLoading.value = false
                    return@launch
                }

                _showKeyGeneration.value = true
                Log.d(TAG, "Biometric authentication successful. Signing challenge...")
                delay(2000)

                val challenge = challengeDto.challenge
                val signatureResult = CryptoManager.signForLogin(context, challenge)
                Log.d(TAG, "Signed challenge successfully")

                _showServerComm.value = true
                Log.d(TAG, "Sending login request...")
                delay(1500)

                val loginRequestDto = LoginResponseDto(
                    userid = challengeDto.userid,
                    signatureRSA = signatureResult.rsaSignature,
                    signatureDilithium = signatureResult.dilithiumSignature
                )

                val result = authService.finishLogin(loginRequestDto, username)
                Log.d(TAG, "Login successful. Token received.")

                SecureStorage.saveAuthToken(context, result.token)

                delay(1000)
                _showSuccess.value = true
                _authStatus.value = "Success: Logged in. Token stored securely."
                Log.d(TAG, "Login process completed successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Login failed: ${e.message}", e)
                handleFailure("Error: Login failed - ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        Log.d(TAG, "Resetting authentication state")
        _authStatus.value = ""
        _showKeyGeneration.value = false
        _showServerComm.value = false
        _showSuccess.value = false
        _isLoading.value = false
    }

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
                        Log.d(TAG, "Biometric authentication succeeded")
                        continuation.resume(true)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        Log.e(TAG, "Biometric authentication error: $errString")
                        handleFailure("Biometric verification failed: $errString")
                        continuation.resume(false)
                    }

                    override fun onAuthenticationFailed() {
                        Log.e(TAG, "Biometric authentication failed")
                        handleFailure("Biometric verification failed")
                        continuation.resume(false)
                    }
                })

            Log.d(TAG, "Launching biometric authentication prompt")
            biometricPrompt.authenticate(promptInfo)
        }

    private fun handleFailure(message: String) {
        Log.e(TAG, message)
        _authStatus.value = if (!message.startsWith("Error:")) "Error: $message" else message
        _showKeyGeneration.value = false
        _showServerComm.value = false
        _showSuccess.value = false
        _isLoading.value = false
    }
}
