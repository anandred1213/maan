package com.practise.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

/**
 * JWT Utility Class
 * 
 * Handles JWT token generation, validation, and extraction of claims.
 * Uses HMAC-SHA algorithm for signing tokens.
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Generate signing key from secret
     * Converts secret string to SecretKey for HMAC-SHA signing
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = HexFormat.of().parseHex(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate JWT Token
     * 
     * Flow:
     * 1. Set subject (user email)
     * 2. Set issued date (current time)
     * 3. Set expiration date (current time + expiration period)
     * 4. Sign with secret key
     * 5. Return compact JWT string
     * 
     * @param userDetails User's email to embed in token
     * @return JWT token string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
        log.info("Generating JWT token for user: {}", userDetails.getUsername());
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract email from JWT token
     * @param token JWT token
     * @return User's email from token subject
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Validate JWT Token
     * 
     * Flow:
     * 1. Parse and extract claims from token
     * 2. Check if token is expired
     * 3. Return true if valid and not expired
     * 4. Return false if parsing fails or token expired
     * 
     * @param token JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractClaims(token);
            boolean isValid = !claims.getExpiration().before(new Date());
            log.debug("Token validation result: {}", isValid);
            return isValid;
        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Malformed token: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("Invalid token signature: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * Extract all claims from JWT token
     * Parses token and verifies signature
     */
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // Verify signature
                .build()
                .parseSignedClaims(token)  // Parse token
                .getPayload();  // Extract claims
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}
