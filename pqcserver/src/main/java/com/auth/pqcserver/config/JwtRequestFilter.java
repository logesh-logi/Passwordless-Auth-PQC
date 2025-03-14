package com.auth.pqcserver.config;

import com.auth.pqcserver.utils.JwtUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtil;

    public JwtRequestFilter(JwtUtils jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        logger.info("JwtRequestFilter started for request: " + request.getMethod() + " " + request.getRequestURI());

        final String authorizationHeader = request.getHeader("Authorization");
        logger.info("Authorization header: " + (authorizationHeader != null ? "present" : "missing"));

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            logger.info("JWT token extracted from header");
            try {
                username = jwtUtil.extractUsername(jwt);
                logger.info("Extracted username: " + username);
            } catch (Exception e) {
                logger.error("JWT extraction failed: " + e.getMessage(), e);
            }
        } else {
            logger.info("No valid Authorization header found");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.info("Attempting to validate token for user: " + username);
            try {
                if (jwtUtil.validateToken(jwt, username)) {
                    // Create list with USER authority
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("USER")
                    );

                    // Create authentication token with authorities
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("User authenticated successfully with USER authority: " + username);
                } else {
                    logger.warn("JWT validation failed for user: " + username);
                }
            } catch (Exception e) {
                logger.error("Exception during token validation: " + e.getMessage(), e);
            }
        } else if (username == null) {
            logger.info("No username extracted from token");
        } else {
            logger.info("User already authenticated: " + SecurityContextHolder.getContext().getAuthentication().getName());
        }

        try {
            logger.info("Continuing filter chain execution");
            chain.doFilter(request, response);
            logger.info("Filter chain completed for request: " + request.getRequestURI());
        } catch (Exception e) {
            logger.error("Exception during filter chain execution: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean shouldSkip = path.startsWith("/auth/") || path.startsWith("/h2-console/");
        if (shouldSkip) {
            logger.info("Skipping JWT filter for path: " + path);
        }
        return shouldSkip;
    }
}