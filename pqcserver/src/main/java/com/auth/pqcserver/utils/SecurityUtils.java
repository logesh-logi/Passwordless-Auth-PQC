package com.auth.pqcserver.utils;

import com.auth.pqcserver.dto.AuthenticationResponseDto;
import com.auth.pqcserver.dto.RegistrationResponseDto;
import com.auth.pqcserver.entity.Credential;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SecurityUtils {

    static {
        // Register Bouncy Castle providers for cryptographic operations
        Security.addProvider(new BouncyCastlePQCProvider());
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Generates a random cryptographic challenge (Base64 URL-encoded).
     *
     * @return A 32-byte challenge encoded in Base64 URL format.
     */
    public static String generateChallenge() {
        byte[] challengeBytes = new byte[32];
        new SecureRandom().nextBytes(challengeBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(challengeBytes);
    }

    /**
     * Verifies both RSA and Dilithium signatures for authentication or registration responses.
     *
     * @param signatureRSA       RSA signature in Base64 format.
     * @param signatureDilithium Dilithium signature in Base64 format.
     * @param credential         User's credential containing public keys.
     * @param challenge          Challenge string that was originally signed.
     * @return True if both signatures are valid, false otherwise.
     * @throws Exception If cryptographic operations fail.
     */
    public static boolean verifyResponse(String signatureRSA, String signatureDilithium, Credential credential, String challenge) throws Exception {
        return verifySignature(signatureRSA, credential.getPublicKeyRSA(), "SHA256withRSA", challenge) &&
                verifySignature(signatureDilithium, credential.getPublicKeyDilithium(), "Dilithium", challenge);
    }

    /**
     * Verifies a digital signature using the given algorithm and public key.
     *
     * @param signature  Signature in Base64 format.
     * @param publicKey  Public key in Base64 format.
     * @param algorithm  Signature algorithm ("SHA256withRSA" or "Dilithium").
     * @param challenge  Challenge string that was originally signed.
     * @return True if the signature is valid, false otherwise.
     * @throws Exception If cryptographic operations fail.
     */
    private static boolean verifySignature(String signature, String publicKey, String algorithm, String challenge) throws Exception {
        PublicKey decodedKey = algorithm.equals("SHA256withRSA")
                ? decodeRSAPublicKey(publicKey)
                : decodeDilithiumPublicKey(publicKey);

        Signature verifier = Signature.getInstance(algorithm);
        verifier.initVerify(decodedKey);
        verifier.update(challenge.getBytes(StandardCharsets.UTF_8));

        return verifier.verify(Base64.getDecoder().decode(signature));
    }

    /**
     * Verifies a registration response by checking both RSA and Dilithium signatures.
     *
     * @param responseDto Registration response DTO.
     * @param credential  User's credential containing public keys.
     * @param challenge   Challenge string that was originally signed.
     * @return True if both signatures are valid, false otherwise.
     * @throws Exception If cryptographic operations fail.
     */
    public static boolean verifyRegistrationResponse(RegistrationResponseDto responseDto, Credential credential, String challenge) throws Exception {
        return verifyResponse(responseDto.getSignatureRSA(), responseDto.getSignatureDilithium(), credential, challenge);
    }

    /**
     * Verifies an authentication response by checking both RSA and Dilithium signatures.
     *
     * @param responseDto Authentication response DTO.
     * @param credential  User's credential containing public keys.
     * @param challenge   Challenge string that was originally signed.
     * @return True if both signatures are valid, false otherwise.
     * @throws Exception If cryptographic operations fail.
     */
    public static boolean verifyAuthenticationResponse(AuthenticationResponseDto responseDto, Credential credential, String challenge) throws Exception {
        return verifyResponse(responseDto.getSignatureRSA(), responseDto.getSignatureDilithium(), credential, challenge);
    }

    // ======================= Key Decoding Methods =======================

    /**
     * Decodes a Base64-encoded Dilithium public key.
     *
     * @param base64PublicKey Public key in Base64 format.
     * @return Decoded PublicKey object.
     * @throws Exception If decoding fails.
     */
    public static PublicKey decodeDilithiumPublicKey(String base64PublicKey) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(base64PublicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("Dilithium", "BCPQC");
        return keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
    }

    /**
     * Decodes a Base64-encoded RSA public key.
     *
     * @param base64PublicKey Public key in Base64 format.
     * @return Decoded PublicKey object.
     * @throws Exception If decoding fails.
     */
    public static PublicKey decodeRSAPublicKey(String base64PublicKey) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(base64PublicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
    }

    // ======================= Utility Methods =======================

    /**
     * Formats a Base64-encoded key for display by converting it to a shortened hex format.
     *
     * @param base64Key Base64-encoded key string.
     * @return A truncated hex representation of the key.
     */
    public static String formatKey(String base64Key) {
        if (base64Key == null || base64Key.isEmpty()) return "N/A";

        String hexKey = bytesToHex(Base64.getDecoder().decode(base64Key));
        return (hexKey.length() <= 20) ? hexKey : hexKey.substring(0, 20) + " ... " + hexKey.substring(hexKey.length() - 20);
    }

    /**
     * Converts a byte array into a hexadecimal string.
     *
     * @param bytes Byte array to convert.
     * @return Hexadecimal representation of the byte array.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
