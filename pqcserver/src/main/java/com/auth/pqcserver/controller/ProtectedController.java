package com.auth.pqcserver.controller;

import com.auth.pqcserver.dto.UserCredentialDto;
import com.auth.pqcserver.service.AuthService;
import com.auth.pqcserver.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")  // Endpoints within this controller require authentication
public class ProtectedController {

    private static final Logger logger = LoggerFactory.getLogger(ProtectedController.class);
    private final AuthService authService;

    public ProtectedController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Retrieves authenticated user credentials.
     *
     * @return UserCredentialDto containing the authenticated user's details.
     */
    @GetMapping("/user")
    public ResponseEntity<UserCredentialDto> getUserInfo() {
        logger.info("[USER INFO REQUEST] - Retrieving user information");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("[UNAUTHORIZED ACCESS] - No valid authentication found");
            return ResponseEntity.status(401).body(new UserCredentialDto("null", "null","null", "null"));
        }

        String username = authentication.getName();
        logger.info("[USER AUTHENTICATED] - Username: {}, Roles: {}", username, authentication.getAuthorities());

        UserCredentialDto userCredentialDto = authService.getUserCredentials(username);
        return ResponseEntity.ok(userCredentialDto);
    }
}
