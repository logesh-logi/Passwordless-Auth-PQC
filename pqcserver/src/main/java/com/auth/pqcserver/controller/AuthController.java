package com.auth.pqcserver.controller;

import com.auth.pqcserver.dto.*;
import com.auth.pqcserver.service.AuthService;
import com.auth.pqcserver.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.auth.pqcserver.utils.SecurityUtils.formatKey;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtUtils jwtUtil;

    public AuthController(AuthService authService, JwtUtils jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    // ======================= Registration Endpoints =======================

    /**
     * Initiates user registration by generating a registration challenge.
     *
     * @param requestDto The registration request containing username and email.
     * @return RegistrationChallengeDto containing the generated challenge and user ID.
     */
    @PostMapping("/register/start")
    public ResponseEntity<RegistrationChallengeDto> startRegistration(@RequestBody RegistrationRequestDto requestDto) {
        String username = requestDto.getUsername();
        logger.info("[START REGISTRATION] - Initiating registration for user: {}", username);

        RegistrationChallengeDto challenge = authService.startRegistration(requestDto);
        logger.info("[REGISTRATION CHALLENGE GENERATED] - User: {}, Challenge: {}", username, challenge.getChallenge());

        return ResponseEntity.ok(challenge);
    }

    /**
     * Completes user registration by verifying the response and storing credentials.
     *
     * @param responseDto The registration response containing public keys.
     * @param username    The username associated with registration.
     * @return RegistrationResultDto indicating success/failure and an optional JWT token.
     * @throws Exception If verification fails.
     */
    @PostMapping("/register/finish")
    public ResponseEntity<RegistrationResultDto> finishRegistration(
            @RequestBody RegistrationResponseDto responseDto,
            @RequestParam String username) throws Exception {

        logger.info("[FINISH REGISTRATION] - Verifying registration for user: {}", username);
        logPublicKeys(responseDto.getPublicKeyDilithium(), responseDto.getPublicKeyRSA());

        boolean success = authService.finishRegistration(responseDto, username);
        return generateRegistrationResponse(success, username);
    }

    // ======================= Authentication Endpoints =======================

    /**
     * Initiates user authentication by generating an authentication challenge.
     *
     * @param username The username requesting authentication.
     * @return AuthenticationChallengeDto containing the generated challenge and user ID.
     */
    @PostMapping("/login/start")
    public ResponseEntity<AuthenticationChallengeDto> startAuthentication(@RequestParam String username) {
        logger.info("[START AUTHENTICATION] - Generating authentication challenge for user: {}", username);

        AuthenticationChallengeDto challenge = authService.startAuthentication(username);
        logger.info("[AUTHENTICATION CHALLENGE GENERATED] - User: {}, Challenge: {}", username, challenge.getChallenge());

        return ResponseEntity.ok(challenge);
    }

    /**
     * Completes user authentication by verifying the response and returning a JWT token.
     *
     * @param responseDto The authentication response containing signatures.
     * @param username    The username associated with authentication.
     * @return AuthenticationResultDto indicating success/failure along with a JWT token if successful.
     * @throws Exception If verification fails.
     */
    @PostMapping("/login/finish")
    public ResponseEntity<AuthenticationResultDto> finishAuthentication(
            @RequestBody AuthenticationResponseDto responseDto,
            @RequestParam String username) throws Exception {

        logger.info("[FINISH AUTHENTICATION] - Verifying authentication for user: {}", username);

        boolean success = authService.finishAuthentication(responseDto, username);
        return generateAuthenticationResponse(success, username);
    }

    // ======================= Helper Methods =======================

    /**
     * Logs formatted public keys for debugging purposes.
     *
     * @param publicKeyDilithium Base64-encoded Dilithium public key.
     * @param publicKeyRSA       Base64-encoded RSA public key.
     */
    private void logPublicKeys(String publicKeyDilithium, String publicKeyRSA) {
        logger.debug("Public Key (Dilithium): {}", formatKey(publicKeyDilithium));
        logger.debug("Public Key (RSA): {}", formatKey(publicKeyRSA));
    }

    /**
     * Generates a standardized registration response.
     *
     * @param success  Indicates whether registration was successful.
     * @param username The username associated with the registration.
     * @return ResponseEntity containing RegistrationResultDto.
     */
    private ResponseEntity<RegistrationResultDto> generateRegistrationResponse(boolean success, String username) {
        RegistrationResultDto resultDto = new RegistrationResultDto();
        resultDto.setSuccess(success);

        if (success) {
            logger.info("[REGISTRATION SUCCESS] - User: {}", username);

            // Generate JWT token
            String token = jwtUtil.generateToken(username);
            resultDto.setMessage("Registration successful");
            resultDto.setToken(token);

            logger.info("[TOKEN ISSUED] - User: {}, Token: {}", username, token);
            return ResponseEntity.ok(resultDto);
        } else {
            logger.warn("[REGISTRATION FAILED] - User: {}", username);
            resultDto.setMessage("Registration failed");
            return ResponseEntity.badRequest().body(resultDto);
        }
    }

    /**
     * Generates a standardized authentication response.
     *
     * @param success  Indicates whether authentication was successful.
     * @param username The username associated with authentication.
     * @return ResponseEntity containing AuthenticationResultDto.
     */
    private ResponseEntity<AuthenticationResultDto> generateAuthenticationResponse(boolean success, String username) {
        AuthenticationResultDto resultDto = new AuthenticationResultDto();
        resultDto.setSuccess(success);

        if (success) {
            logger.info("[AUTHENTICATION SUCCESS] - User: {}", username);

            // Generate JWT token
            String token = jwtUtil.generateToken(username);
            resultDto.setMessage("Authentication successful");
            resultDto.setToken(token);

            logger.info("[TOKEN ISSUED] - User: {}, Token: {}", username, token);
            return ResponseEntity.ok(resultDto);
        } else {
            logger.warn("[AUTHENTICATION FAILED] - User: {}", username);
            resultDto.setMessage("Authentication failed");
            return ResponseEntity.badRequest().body(resultDto);
        }
    }
}
