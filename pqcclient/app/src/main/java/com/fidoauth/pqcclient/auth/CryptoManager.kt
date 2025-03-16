package com.fidoauth.pqcclient.auth

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

data class SignatureResult(
    val rsaPublicKey: String,
    val rsaSignature: String,
    val dilithiumPublicKey: String,
    val dilithiumSignature: String
)

data class LoginSignatureResult(
    val rsaSignature: String,
    val dilithiumSignature: String
)

object CryptoManager {

    private const val TAG = "CryptoManager"
    private const val PREF_NAME = "secure_prefs"
    private const val RSA_PRIVATE_KEY = "rsa_private_key"
    private const val DILITHIUM_PRIVATE_KEY = "dilithium_private_key"

    init {
        Security.removeProvider("BC")
        Security.addProvider(BouncyCastleProvider())
        Security.addProvider(BouncyCastlePQCProvider())
        Log.d(TAG, "BouncyCastle and BCPQC providers initialized")
    }

    private fun generateRSAKeyPair(): KeyPair = runCatching {
        Log.d(TAG, "Generating RSA key pair...")
        KeyPairGenerator.getInstance("RSA").apply { initialize(3072) }.generateKeyPair().also {
            Log.d(TAG, "RSA key pair generated successfully")
        }
    }.getOrElse {
        Log.e(TAG, "Failed to generate RSA key pair", it)
        throw SecurityException("Failed to generate RSA key pair", it)
    }

    private fun generateDilithiumKeyPair(): KeyPair = runCatching {
        Log.d(TAG, "Generating Dilithium key pair...")
        KeyPairGenerator.getInstance("Dilithium", "BCPQC").apply {
            initialize(DilithiumParameterSpec.dilithium3)
        }.generateKeyPair().also {
            Log.d(TAG, "Dilithium key pair generated successfully")
        }
    }.getOrElse {
        Log.e(TAG, "Failed to generate Dilithium key pair", it)
        throw SecurityException("Failed to generate Dilithium key pair", it)
    }

    private fun signData(privateKey: PrivateKey, data: ByteArray, algorithm: String): ByteArray =
        runCatching {
            Log.d(TAG, "Signing data using $algorithm...")
            Signature.getInstance(algorithm).apply {
                initSign(privateKey)
                update(data)
            }.sign().also {
                Log.d(TAG, "Data signed successfully using $algorithm")
            }
        }.getOrElse {
            Log.e(TAG, "Failed to sign data with $algorithm", it)
            throw SecurityException("Failed to sign data with $algorithm", it)
        }

    private fun savePrivateKey(context: Context, key: PrivateKey, keyAlias: String) {
        val keyEncoded = Base64.getEncoder().encodeToString(key.encoded)
        Log.d(TAG, "Saving private key for $keyAlias securely...")
        getPreferences(context).edit().putString(keyAlias, keyEncoded).apply()
        Log.d(TAG, "Private key for $keyAlias saved successfully")
    }

    private fun getPrivateKey(context: Context, keyAlias: String): ByteArray? =
        runCatching {
            Log.d(TAG, "Retrieving private key for $keyAlias...")
            getPreferences(context).getString(keyAlias, null)?.let {
                Base64.getDecoder().decode(it).also {
                    Log.d(TAG, "Private key for $keyAlias retrieved successfully")
                }
            }
        }.getOrElse {
            Log.e(TAG, "Failed to retrieve private key for $keyAlias", it)
            throw SecurityException("Failed to retrieve private key for $keyAlias", it)
        }

    fun generateKeysAndSign(context: Context, challenge: String): SignatureResult = runCatching {
        val challengeBytes = challenge.toByteArray(Charsets.UTF_8)
        Log.d(TAG, "Generating keys and signing challenge...")

        val rsaKeyPair = generateRSAKeyPair().also {
            savePrivateKey(context, it.private, RSA_PRIVATE_KEY)
        }
        val rsaSignature = signData(rsaKeyPair.private, challengeBytes, "SHA256withRSA")

        val dilithiumKeyPair = generateDilithiumKeyPair().also {
            savePrivateKey(context, it.private, DILITHIUM_PRIVATE_KEY)
        }
        val dilithiumSignature = signData(dilithiumKeyPair.private, challengeBytes, "Dilithium")

        Log.d(TAG, "Keys generated and challenge signed successfully")

        SignatureResult(
            rsaPublicKey = Base64.getEncoder().encodeToString(rsaKeyPair.public.encoded),
            rsaSignature = Base64.getEncoder().encodeToString(rsaSignature),
            dilithiumPublicKey = Base64.getEncoder().encodeToString(dilithiumKeyPair.public.encoded),
            dilithiumSignature = Base64.getEncoder().encodeToString(dilithiumSignature)
        )
    }.getOrElse {
        Log.e(TAG, "Failed to generate keys and sign challenge", it)
        throw SecurityException("Failed to generate keys and sign challenge", it)
    }

    fun signForLogin(context: Context, challenge: String): LoginSignatureResult = runCatching {
        val challengeBytes = challenge.toByteArray(Charsets.UTF_8)
        Log.d(TAG, "Signing challenge for login...")

        val rsaPrivateKey = getPrivateKey(context, RSA_PRIVATE_KEY)?.let {
            KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(it))
        } ?: throw SecurityException("RSA private key not found")

        val dilithiumPrivateKey = getPrivateKey(context, DILITHIUM_PRIVATE_KEY)?.let {
            KeyFactory.getInstance("Dilithium", "BCPQC")
                .generatePrivate(PKCS8EncodedKeySpec(it))
        } ?: throw SecurityException("Dilithium private key not found")

        val rsaSignature = signData(rsaPrivateKey, challengeBytes, "SHA256withRSA")
        val dilithiumSignature = signData(dilithiumPrivateKey, challengeBytes, "Dilithium")

        Log.d(TAG, "Challenge signed successfully for login")

        LoginSignatureResult(
            rsaSignature = Base64.getEncoder().encodeToString(rsaSignature),
            dilithiumSignature = Base64.getEncoder().encodeToString(dilithiumSignature)
        )
    }.getOrElse {
        Log.e(TAG, "Failed to sign challenge for login", it)
        throw SecurityException("Failed to sign challenge for login", it)
    }

    private fun getPreferences(context: Context) =
        EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ).also {
            Log.d(TAG, "EncryptedSharedPreferences initialized successfully")
        }
}
