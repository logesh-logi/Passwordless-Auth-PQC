package com.auth.pqcserver.controller;

import com.auth.pqcserver.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")  // This requires authentication
public class ProtectedController {

    private final JwtUtils jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(ProtectedController.class);

    public ProtectedController(JwtUtils jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/user")
    public ResponseEntity<String> getUserInfo() {
        logger.info("getUserInfo endpoint called");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            logger.info("Authentication found: " + authentication.getName());
            logger.info("Authorities: " + authentication.getAuthorities());
            return ResponseEntity.ok("Authenticated user: " + authentication.getName());
        } else {
            logger.warn("No authentication found in SecurityContext");
            return ResponseEntity.status(401).body("Not authenticated");
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<String> getDashboardData() {
        logger.info("getDashboardData endpoint called");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok("Welcome to your dashboard, " + authentication.getName());
    }

    // Simple test endpoint
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        logger.info("Test endpoint reached");
        return ResponseEntity.ok("Test endpoint reached");
    }
}