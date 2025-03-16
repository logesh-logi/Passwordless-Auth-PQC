package com.auth.pqcserver.service;

import com.auth.pqcserver.dto.*;
import com.auth.pqcserver.entity.Credential;
import com.auth.pqcserver.entity.User;
import com.auth.pqcserver.repository.CredentialRepository;
import com.auth.pqcserver.repository.UserRepository;
import com.auth.pqcserver.utils.SecurityUtils;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;

    public AuthService(UserRepository userRepository, CredentialRepository credentialRepository) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
    }

    // ======================= Registration Methods =======================

    /**
     * Generates a registration challenge for a given username.
     *
     * @param requestDto The registration request containing username and email.
     * @return A DTO containing the generated challenge and user ID.
     */
    public RegistrationChallengeDto startRegistration(RegistrationRequestDto requestDto) {
        String challenge = SecurityUtils.generateChallenge();

        User user = userRepository.findByUsername(requestDto.getUsername())
                .orElseGet(() -> userRepository.save(new User(requestDto.getUsername(), challenge, requestDto.getEmail())));

        return new RegistrationChallengeDto(challenge, user.getId());
    }

    /**
     * Verifies the registration response and stores the user's credential.
     *
     * @param responseDto The registration response containing public keys.
     * @param username    The username associated with the registration.
     * @return True if registration is successful, otherwise throws an exception.
     * @throws Exception If verification fails.
     */
    public boolean finishRegistration(RegistrationResponseDto responseDto, String username) throws Exception {
        User user = getUserByUsername(username);

        if (credentialRepository.existsByUser(user)) {
            throw new IllegalStateException("Credential already exists for this user.");
        }

        Credential credential = new Credential(responseDto.getPublicKeyDilithium(), responseDto.getPublicKeyRSA(), user);
        credentialRepository.save(credential);

        return SecurityUtils.verifyRegistrationResponse(responseDto, credential, user.getChallenge());
    }

    // ======================= Authentication Methods =======================

    /**
     * Generates an authentication challenge for a given username.
     *
     * @param username The username requesting authentication.
     * @return A DTO containing the generated challenge and user ID.
     */
    public AuthenticationChallengeDto startAuthentication(String username) {
        User user = getUserByUsername(username);

        String challenge = SecurityUtils.generateChallenge();
        user.setChallenge(challenge);
        userRepository.save(user); // Ensure challenge persistence

        return new AuthenticationChallengeDto(challenge, user.getId());
    }

    /**
     * Verifies the authentication response using the stored credential.
     *
     * @param responseDto The authentication response containing signatures.
     * @param username    The username associated with the authentication.
     * @return True if authentication is successful, otherwise false.
     * @throws Exception If verification fails.
     */
    public boolean finishAuthentication(AuthenticationResponseDto responseDto, String username) throws Exception {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return false;

        Credential credential = credentialRepository.findByUser(user).orElse(null);
        if (credential == null) return false;

        return SecurityUtils.verifyAuthenticationResponse(responseDto, credential, user.getChallenge());
    }

    // ======================= User Credential Retrieval =======================

    /**
     * Retrieves a user's credentials (username, RSA public key, Dilithium public key).
     *
     * @param username The username whose credentials are requested.
     * @return A DTO containing username and associated public keys.
     * @throws IllegalStateException If the user or credentials are not found.
     */
    public UserCredentialDto getUserCredentials(String username) {
        User user = getUserByUsername(username);
        Credential credential = credentialRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Credentials not found for user"));

        return new UserCredentialDto(user.getUsername(), user.getEmail(), credential.getPublicKeyRSA(), credential.getPublicKeyDilithium());
    }

    // ======================= Helper Methods =======================

    /**
     * Retrieves a user by username or throws an exception if not found.
     *
     * @param username The username to search for.
     * @return The User entity.
     * @throws IllegalStateException If the user is not found.
     */
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
    }
}
