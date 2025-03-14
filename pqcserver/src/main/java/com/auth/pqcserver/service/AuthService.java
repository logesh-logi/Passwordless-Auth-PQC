package com.auth.pqcserver.service;

import com.auth.pqcserver.dto.AuthenticationChallengeDto;
import com.auth.pqcserver.dto.AuthenticationResponseDto;
import com.auth.pqcserver.dto.RegistrationChallengeDto;
import com.auth.pqcserver.dto.RegistrationResponseDto;
import com.auth.pqcserver.entity.Credential;
import com.auth.pqcserver.entity.User;
import com.auth.pqcserver.repository.CredentialRepository;
import com.auth.pqcserver.repository.UserRepository;
import com.auth.pqcserver.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;

    public AuthService(UserRepository userRepository, CredentialRepository credentialRepository) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
    }

    // Generate a registration challenge for a given username.
    public RegistrationChallengeDto startRegistration(String username) {
        // Generate a random challenge (in a real system, store this securely)
        String challenge = SecurityUtils.generateChallenge();
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(new User(username, challenge)));
        return new RegistrationChallengeDto(challenge, user.getId());
    }

    // Verify the registration response and store the credential.
    public boolean finishRegistration(RegistrationResponseDto responseDto, String username) throws Exception {
        // Verify the response (in production, validate clientDataJSON, attestation, and signature)
        boolean isValid = true;  // Placeholder for actual validation
        if (!isValid) {
            return false;
        }

        // Get or create the user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found for registration"));

        // Check if a credential already exists
        if (credentialRepository.existsByUser(user)) {
            throw new IllegalStateException("Credential already exists for this user.");
        }

        // Save the credential
        Credential credential = new Credential(
                responseDto.getPublicKeyDilithium(),
                responseDto.getPublicKeyRSA(),
                user
        );
        credentialRepository.save(credential);

        return SecurityUtils.verifyRegistrationResponse(responseDto, credential, user.getChallenge());
    }


    // Generate an authentication challenge for a given username.
    public AuthenticationChallengeDto startAuthentication(String username) {
        String challenge = SecurityUtils.generateChallenge();
        Optional<User> userOpt = userRepository.findByUsername(username);
        return new AuthenticationChallengeDto(challenge, userOpt.get().getId());
    }

    // Verify the authentication response using the stored credential.
    public boolean finishAuthentication(AuthenticationResponseDto responseDto, String username) throws Exception {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        Optional<Credential> credentialOpt = credentialRepository.findByUser(user);
        if (credentialOpt.isEmpty()) {
            return false;
        }
        Credential credential = credentialOpt.get();
        // Validate the response using public key data and the stored challenge.
        return SecurityUtils.verifyAuthenticationResponse(responseDto, credential, user.getChallenge());
    }
}
