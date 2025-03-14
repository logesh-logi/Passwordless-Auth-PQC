package com.auth.pqcserver.utils;

import com.auth.pqcserver.dto.AuthenticationResponseDto;
import com.auth.pqcserver.dto.RegistrationResponseDto;
import com.auth.pqcserver.entity.Credential;
import com.auth.pqcserver.entity.User;
import com.auth.pqcserver.repository.UserRepository;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SecurityUtils {

    static {
        Security.addProvider(new BouncyCastlePQCProvider());
        Security.addProvider(new BouncyCastleProvider());
    }

    // Generate a random challenge (Base64-encoded).
    public static String generateChallenge() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] challengeBytes = new byte[32];
        secureRandom.nextBytes(challengeBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(challengeBytes);
    }

    public static boolean verifyRegistrationResponse(RegistrationResponseDto responseDto, Credential credential, String challenge) throws Exception {

        PublicKey publicKey1 = decodeRSAPublicKey(credential.getPublicKeyRSA());
        Signature verifier1 = Signature.getInstance("SHA256withRSA");
        byte[] sign1 =  Base64.getDecoder().decode(responseDto.getSignatureRSA());
        verifier1.initVerify(publicKey1);
        verifier1.update(challenge.getBytes(StandardCharsets.UTF_8));

        PublicKey publicKey2 = decodeDilithiumPublicKey(credential.getPublicKeyDilithium());
        Signature verifier2 = Signature.getInstance("Dilithium");
        byte[] sign2 =  Base64.getDecoder().decode(responseDto.getSignatureDilithium());
        verifier2.initVerify(publicKey2);
        verifier2.update(challenge.getBytes(StandardCharsets.UTF_8));


        boolean isVerified1 = verifier1.verify(sign1);
        boolean isVerified2 = verifier2.verify(sign2);
        return isVerified1 && isVerified2;
    }

    public static boolean verifyAuthenticationResponse(AuthenticationResponseDto responseDto, Credential credential, String challenge) throws Exception {
        
        PublicKey publicKey1 = decodeRSAPublicKey(credential.getPublicKeyRSA());
        Signature verifier1 = Signature.getInstance("SHA256withRSA");
        byte[] sign1 =  Base64.getDecoder().decode(responseDto.getSignatureRSA());
        verifier1.initVerify(publicKey1);
        verifier1.update(challenge.getBytes(StandardCharsets.UTF_8));

        PublicKey publicKey2 = decodeDilithiumPublicKey(credential.getPublicKeyDilithium());
        Signature verifier2 = Signature.getInstance("Dilithium");
        byte[] sign2 =  Base64.getDecoder().decode(responseDto.getSignatureDilithium());
        verifier2.initVerify(publicKey2);
        verifier2.update(challenge.getBytes(StandardCharsets.UTF_8));
        
        boolean isVerified1 = verifier1.verify(sign1);
        boolean isVerified2 = verifier2.verify(sign2);
        return isVerified1 && isVerified2;
    }

    public static PublicKey decodeDilithiumPublicKey(String base64PublicKey) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(base64PublicKey);

        // Use Bouncy Castle PQC KeyFactory to reconstruct the key
        KeyFactory keyFactory = KeyFactory.getInstance("Dilithium", "BCPQC");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);

        return keyFactory.generatePublic(keySpec);
    }

    public static PublicKey decodeRSAPublicKey(String base64PublicKey) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(base64PublicKey);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);

        return keyFactory.generatePublic(keySpec);
    }
}
