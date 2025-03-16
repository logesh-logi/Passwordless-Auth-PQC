package com.auth.pqcserver.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * Utility class for generating and validating JWT tokens.
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private Key signingKey;

    /**
     * Initializes the signing key after the bean's construction.
     */
    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generates a JWT token for the given username.
     *
     * @param username the username for which the token is generated
     * @return the generated JWT token
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates the JWT token against the provided username.
     *
     * @param token    the JWT token to validate
     * @param username the username to validate against
     * @return true if the token is valid and matches the username; false otherwise
     */
    public boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            // Log the exception or handle it as needed
            return false;
        }
    }

    /**
     * Extracts the username (subject) from the JWT token.
     *
     * @param token the JWT token
     * @return the username (subject) contained in the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the JWT token.
     *
     * @param token the JWT token
     * @return the expiration date of the token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from the JWT token using the provided claims resolver function.
     *
     * @param <T>            the type of the claim
     * @param token          the JWT token
     * @param claimsResolver the function to extract the claim
     * @return the extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from the JWT token.
     *
     * @param token the JWT token
     * @return the claims contained in the token
     * @throws JwtException if the token is invalid or cannot be parsed
     */
    private Claims extractAllClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if the JWT token is expired.
     *
     * @param token the JWT token
     * @return true if the token is expired; false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
