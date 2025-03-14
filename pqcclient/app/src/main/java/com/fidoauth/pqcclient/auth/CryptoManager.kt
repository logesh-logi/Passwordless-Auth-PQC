package com.fidoauth.pqcclient.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec
import java.security.*
import java.util.Base64

data class SignatureResult(
    val rsaPublicKey: String,
    val rsaSignature: String,
    val dilithiumPublicKey: String,
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
    private fun generateRSAKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(3072)
        return keyPairGenerator.generateKeyPair()
    }

    /**
     * Generates a Dilithium key pair.
     */
    private fun generateDilithiumKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("Dilithium", "BCPQC")
        keyPairGenerator.initialize(DilithiumParameterSpec.dilithium3)
        return keyPairGenerator.generateKeyPair()
    }

    /**
     * Signs the given data with the specified private key using the provided algorithm.
     */
    private fun signData(privateKey: PrivateKey, data: ByteArray, algorithm: String): ByteArray {
        val signature = Signature.getInstance(algorithm)
        signature.initSign(privateKey)
        signature.update(data)
        return signature.sign()
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
    fun getPrivateKey(context: Context, keyAlias: String): ByteArray? {
        val encodedKey = getPreferences(context).getString(keyAlias, null)
        return encodedKey?.let { Base64.getDecoder().decode(it) }
    }

    /**
     * Generates RSA and Dilithium key pairs, signs the challenge, and returns SignatureResult.
     */
    fun generateKeysAndSign(context: Context, challenge: String): SignatureResult {
        val challengeBytes = challenge.toByteArray(Charsets.UTF_8)

        // RSA Key Generation & Signing
        val rsaKeyPair = generateRSAKeyPair()
        savePrivateKey(context, rsaKeyPair.private, RSA_PRIVATE_KEY)
        val rsaSignatureBytes = signData(rsaKeyPair.private, challengeBytes, "SHA256withRSA")
        val rsaPublicKeyEncoded = Base64.getEncoder().encodeToString(rsaKeyPair.public.encoded)
        val rsaSignatureEncoded = Base64.getEncoder().encodeToString(rsaSignatureBytes)

        // Dilithium Key Generation & Signing
        val dilithiumKeyPair = generateDilithiumKeyPair()
        savePrivateKey(context, dilithiumKeyPair.private, DILITHIUM_PRIVATE_KEY)
        val dilithiumSignatureBytes = signData(dilithiumKeyPair.private, challengeBytes, "Dilithium")
        val dilithiumPublicKeyEncoded = Base64.getEncoder().encodeToString(dilithiumKeyPair.public.encoded)
        val dilithiumSignatureEncoded = Base64.getEncoder().encodeToString(dilithiumSignatureBytes)

        return SignatureResult(
            rsaPublicKey = rsaPublicKeyEncoded,
            rsaSignature = rsaSignatureEncoded,
            dilithiumPublicKey = dilithiumPublicKeyEncoded,
            dilithiumSignature = dilithiumSignatureEncoded
        )
    }

    /**
     * Creates and returns an EncryptedSharedPreferences instance.
     */
    private fun getPreferences(context: Context) =
        EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
}
