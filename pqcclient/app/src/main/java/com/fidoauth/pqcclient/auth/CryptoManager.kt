package com.fidoauth.pqcclient.auth

import android.content.Context
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

    private const val PREF_NAME = "secure_prefs"
    private const val RSA_PRIVATE_KEY = "rsa_private_key"
    private const val DILITHIUM_PRIVATE_KEY = "dilithium_private_key"

    init {
        Security.removeProvider("BC")
        Security.addProvider(BouncyCastleProvider())
        Security.addProvider(BouncyCastlePQCProvider())
    }

    /**
     * Generates an RSA key pair with a 3072-bit key size.
     */
    private fun generateRSAKeyPair(): KeyPair = runCatching {
        KeyPairGenerator.getInstance("RSA").apply { initialize(3072) }.generateKeyPair()
    }.getOrElse { throw SecurityException("Failed to generate RSA key pair", it) }

    /**
     * Generates a Dilithium key pair.
     */
    private fun generateDilithiumKeyPair(): KeyPair = runCatching {
        KeyPairGenerator.getInstance("Dilithium", "BCPQC").apply {
            initialize(DilithiumParameterSpec.dilithium3)
        }.generateKeyPair()
    }.getOrElse { throw SecurityException("Failed to generate Dilithium key pair", it) }

    /**
     * Signs the given data with the specified private key using the provided algorithm.
     */
    private fun signData(privateKey: PrivateKey, data: ByteArray, algorithm: String): ByteArray =
        runCatching {
            Signature.getInstance(algorithm).apply {
                initSign(privateKey)
                update(data)
            }.sign()
        }.getOrElse {
            throw SecurityException("Failed to sign data with $algorithm", it)
        }

    /**
     * Securely saves the private key in EncryptedSharedPreferences.
     */
    private fun savePrivateKey(context: Context, key: PrivateKey, keyAlias: String) {
        val keyEncoded = Base64.getEncoder().encodeToString(key.encoded)
        getPreferences(context).edit().putString(keyAlias, keyEncoded).apply()
    }

    /**
     * Retrieves the private key from EncryptedSharedPreferences.
     */
    private fun getPrivateKey(context: Context, keyAlias: String): ByteArray? =
        runCatching {
            getPreferences(context).getString(keyAlias, null)?.let {
                Base64.getDecoder().decode(it)
            }
        }.getOrElse {
            throw SecurityException("Failed to retrieve private key for $keyAlias", it)
        }

    /**
     * Generates RSA and Dilithium key pairs, signs the challenge, and returns SignatureResult.
     */
    fun generateKeysAndSign(context: Context, challenge: String): SignatureResult = runCatching {
        val challengeBytes = challenge.toByteArray(Charsets.UTF_8)

        // RSA Key Generation & Signing
        val rsaKeyPair = generateRSAKeyPair().also {
            savePrivateKey(context, it.private, RSA_PRIVATE_KEY)
        }
        val rsaSignature = signData(rsaKeyPair.private, challengeBytes, "SHA256withRSA")

        // Dilithium Key Generation & Signing
        val dilithiumKeyPair = generateDilithiumKeyPair().also {
            savePrivateKey(context, it.private, DILITHIUM_PRIVATE_KEY)
        }
        val dilithiumSignature = signData(dilithiumKeyPair.private, challengeBytes, "Dilithium")

        SignatureResult(
            rsaPublicKey = Base64.getEncoder().encodeToString(rsaKeyPair.public.encoded),
            rsaSignature = Base64.getEncoder().encodeToString(rsaSignature),
            dilithiumPublicKey = Base64.getEncoder().encodeToString(dilithiumKeyPair.public.encoded),
            dilithiumSignature = Base64.getEncoder().encodeToString(dilithiumSignature)
        )
    }.getOrElse {
        throw SecurityException("Failed to generate keys and sign challenge", it)
    }

    /**
     * Signs the challenge using previously stored RSA and Dilithium private keys.
     * Returns signatures only (no public keys) for login purposes.
     */
    fun signForLogin(context: Context, challenge: String): LoginSignatureResult = runCatching {
        val challengeBytes = challenge.toByteArray(Charsets.UTF_8)

        val rsaPrivateKey = getPrivateKey(context, RSA_PRIVATE_KEY)?.let {
            KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(it))
        } ?: throw SecurityException("RSA private key not found")

        val dilithiumPrivateKey = getPrivateKey(context, DILITHIUM_PRIVATE_KEY)?.let {
            KeyFactory.getInstance("Dilithium", "BCPQC")
                .generatePrivate(PKCS8EncodedKeySpec(it))
        } ?: throw SecurityException("Dilithium private key not found")

        val rsaSignature = signData(rsaPrivateKey, challengeBytes, "SHA256withRSA")
        val dilithiumSignature = signData(dilithiumPrivateKey, challengeBytes, "Dilithium")

        LoginSignatureResult(
            rsaSignature = Base64.getEncoder().encodeToString(rsaSignature),
            dilithiumSignature = Base64.getEncoder().encodeToString(dilithiumSignature)
        )
    }.getOrElse {
        throw SecurityException("Failed to sign challenge for login", it)
    }

    /**
     * Creates and returns an EncryptedSharedPreferences instance.
     */
    private fun getPreferences(context: Context) =
        EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
}
