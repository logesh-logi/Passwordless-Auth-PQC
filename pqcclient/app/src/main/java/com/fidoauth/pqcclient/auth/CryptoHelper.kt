//package com.fidoauth.pqcclient.auth
//
//import android.content.Context
//import org.bouncycastle.jce.provider.BouncyCastleProvider
//import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider
//import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec
//import java.security.*
//import java.util.Base64
//
//data class SignatureResult(
//    val rsaPublicKey: String,
//    val rsaSignature: String,
//    val dilithiumPublicKey: String,
//    val dilithiumSignature: String
//)
//
//object CryptoHelper {
//
//    init {
//            Security.removeProvider("BC");
//            Security.addProvider(BouncyCastleProvider())
//            Security.addProvider(BouncyCastlePQCProvider())
//    }
//
//    fun generateRSAKeyPair(): KeyPair {
//        val keyPairGenerator = KeyPairGenerator.getInstance("RSA") // No "BC" needed for RSA
//        keyPairGenerator.initialize(3072) // RSA-3072 for security
//        return keyPairGenerator.generateKeyPair()
//    }
//
//
//    fun generateDilithiumKeyPair(): KeyPair {
//        val keyPairGenerator = KeyPairGenerator.getInstance("Dilithium", "BCPQC")
//        keyPairGenerator.initialize(DilithiumParameterSpec.dilithium3)
//        return keyPairGenerator.generateKeyPair()
//    }
//
//    private fun signData(privateKey: PrivateKey, data: ByteArray, algorithm: String): ByteArray {
//        val signature = Signature.getInstance(algorithm)
//        signature.initSign(privateKey)
//        signature.update(data)
//        return signature.sign()
//    }
//
//    /**
//     * Generate both RSA and Dilithium key pairs and sign the provided challenge.
//     * Returns a SignatureResult containing both public keys and both signatures (Base64-encoded).
//     */
//    fun generateKeysAndSign(context: Context, challenge: String): SignatureResult {
//        val challengeBytes = challenge.toByteArray(Charsets.UTF_8)
//
//        // RSA signing
//        val rsaKeyPair = generateRSAKeyPair()
//        SecureStorage.savePrivateKey(context, rsaKeyPair.private, "rsa_private_key") // Save RSA Private Key
//        val rsaSignatureBytes = signData(rsaKeyPair.private, challengeBytes, "SHA256withRSA")
//        val rsaPublicKeyEncoded = Base64.getEncoder().encodeToString(rsaKeyPair.public.encoded)
//        val rsaSignatureEncoded = Base64.getEncoder().encodeToString(rsaSignatureBytes)
//
//        // Dilithium signing
//        val dilithiumKeyPair = generateDilithiumKeyPair()
//        SecureStorage.savePrivateKey(context, dilithiumKeyPair.private, "dilithium_private_key") // Save Dilithium Private Key
//        val dilithiumSignatureBytes = signData(dilithiumKeyPair.private, challengeBytes, "Dilithium")
//        val dilithiumPublicKeyEncoded = Base64.getEncoder().encodeToString(dilithiumKeyPair.public.encoded)
//        val dilithiumSignatureEncoded = Base64.getEncoder().encodeToString(dilithiumSignatureBytes)
//
//        return SignatureResult(
//            rsaPublicKey = rsaPublicKeyEncoded,
//            rsaSignature = rsaSignatureEncoded,
//            dilithiumPublicKey = dilithiumPublicKeyEncoded,
//            dilithiumSignature = dilithiumSignatureEncoded
//        )
//    }
//
//}
