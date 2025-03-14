package com.auth.pqcserver.controller;

import com.auth.pqcserver.dto.*;
import com.auth.pqcserver.utils.JwtUtils;
import com.auth.pqcserver.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtil;

    public AuthController(AuthService authService, JwtUtils jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    // Start registration: generate and return a registration challenge.
    @GetMapping("/register/start")
    public ResponseEntity<RegistrationChallengeDto> startRegistration(@RequestParam String username) {
        RegistrationChallengeDto challenge = authService.startRegistration(username);
        return ResponseEntity.ok(challenge);
    }

    // Finish registration: verify response and store credential.
    @PostMapping("/register/finish")
    public ResponseEntity<RegistrationResultDto> finishRegistration(
            @RequestBody RegistrationResponseDto responseDto,
            @RequestParam String username) throws Exception {
        boolean result = authService.finishRegistration(responseDto, username);

        RegistrationResultDto resultDto = new RegistrationResultDto();
        if (result) {
            resultDto.setSuccess(true);
            resultDto.setMessage("Registration successful");

            // Generate JWT token after successful registration (optional)
            String token = jwtUtil.generateToken(username);
            resultDto.setToken(token);

            return ResponseEntity.ok(resultDto);
        } else {
            resultDto.setSuccess(false);
            resultDto.setMessage("Registration failed");
            return ResponseEntity.badRequest().body(resultDto);
        }
    }

    // Start authentication: generate and return an authentication challenge.
    @PostMapping("/login/start")
    public ResponseEntity<AuthenticationChallengeDto> startAuthentication(@RequestParam String username) {
        AuthenticationChallengeDto challenge = authService.startAuthentication(username);
        return ResponseEntity.ok(challenge);
    }

    // Finish authentication: verify authentication response and return JWT token.
    @PostMapping("/login/finish")
    public ResponseEntity<AuthenticationResultDto> finishAuthentication(
            @RequestBody AuthenticationResponseDto responseDto,
            @RequestParam String username) throws Exception {
        boolean result = authService.finishAuthentication(responseDto, username);

        AuthenticationResultDto resultDto = new AuthenticationResultDto();
        if (result) {
            // Generate JWT token
            String token = jwtUtil.generateToken(username);

            resultDto.setSuccess(true);
            resultDto.setMessage("Authentication successful");
            resultDto.setToken(token);

            return ResponseEntity.ok(resultDto);
        } else {
            resultDto.setSuccess(false);
            resultDto.setMessage("Authentication failed");
            return ResponseEntity.badRequest().body(resultDto);
        }
    }
}