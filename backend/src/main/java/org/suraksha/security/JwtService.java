package org.suraksha.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.suraksha.entity.Role;
import org.suraksha.entity.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * Service responsible for creating, signing, and validating JSON Web Tokens (JWT).
 * Utilizes HMAC-SHA256 with an externalized signing secret key.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long jwtExpirationMs;

    public JwtService(
            @Value("${suraksha.jwt.secret}") String secretKey,
            @Value("${suraksha.jwt.expiration-ms}") long jwtExpirationMs) {

        if (secretKey == null || secretKey.trim().length() < 32) {
            throw new IllegalArgumentException(
                    "JWT secret key is missing or insecure. It must be at least 32 characters (256 bits) long.");
        }

        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = jwtExpirationMs;
    }

    /**
     * Generates a signed, expiring JWT token for an authenticated user.
     */
    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtExpirationMs);

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("fullName", user.getFullName())
                .claim("roles", roleNames)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extracts the subject (email address) from the JWT.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validates whether a token belongs to the given user and has not expired.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String email = extractEmail(token);
            return email != null && email.equalsIgnoreCase(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Checks if a token has expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            return expiration.before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }
}
