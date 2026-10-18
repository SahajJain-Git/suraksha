package org.suraksha;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.suraksha.entity.Role;
import org.suraksha.entity.User;
import org.suraksha.security.CustomUserDetails;
import org.suraksha.security.JwtService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION_MS = 3600000; // 1 hour

    private JwtService jwtService;
    private User testUser;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, EXPIRATION_MS);

        testUser = new User("Rahul Sharma", "rahul@example.com", "$2a$10$dummyHash");
        testUser.setId(101L);
        testUser.addRole(new Role("ROLE_STUDENT", "Student role"));
        userDetails = new CustomUserDetails(testUser);
    }

    @Test
    void testGenerateTokenAndExtractEmail() {
        String token = jwtService.generateToken(testUser);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // Header.Payload.Signature

        String extractedEmail = jwtService.extractEmail(token);
        assertThat(extractedEmail).isEqualTo("rahul@example.com");
    }

    @Test
    void testTokenValidation() {
        String token = jwtService.generateToken(testUser);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
        assertThat(jwtService.isTokenExpired(token)).isFalse();
    }

    @Test
    void testTokenInvalidForDifferentUser() {
        String token = jwtService.generateToken(testUser);

        User anotherUser = new User("Priya Verma", "priya@example.com", "$2a$10$dummyHash");
        anotherUser.setId(102L);
        anotherUser.addRole(new Role("ROLE_STUDENT", "Student role"));
        UserDetails anotherUserDetails = new CustomUserDetails(anotherUser);

        assertThat(jwtService.isTokenValid(token, anotherUserDetails)).isFalse();
    }

    @Test
    void testExpiredTokenValidation() {
        // Create JwtService with negative expiration (already expired)
        JwtService expiredJwtService = new JwtService(TEST_SECRET, -5000);
        String expiredToken = expiredJwtService.generateToken(testUser);

        assertThat(jwtService.isTokenExpired(expiredToken)).isTrue();
        assertThat(jwtService.isTokenValid(expiredToken, userDetails)).isFalse();
    }

    @Test
    void testTamperedTokenValidation() {
        String token = jwtService.generateToken(testUser);
        String tamperedToken = token.substring(0, token.length() - 4) + "XXXX";

        assertThat(jwtService.isTokenValid(tamperedToken, userDetails)).isFalse();
    }

    @Test
    void testConstructorFailsSafelyOnShortSecret() {
        assertThatThrownBy(() -> new JwtService("short-secret-key", EXPIRATION_MS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 32 characters");
    }
}
