package org.suraksha;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.suraksha.dto.LoginRequestDto;
import org.suraksha.dto.RegisterRequestDto;
import org.suraksha.entity.Role;
import org.suraksha.entity.User;
import org.suraksha.repository.RoleRepository;
import org.suraksha.repository.UserRepository;
import org.suraksha.security.JwtService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // Ensure default roles exist in the database
        if (roleRepository.findByName("ROLE_STUDENT").isEmpty()) {
            roleRepository.save(new Role("ROLE_STUDENT", "Student role"));
        }
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(new Role("ROLE_ADMIN", "Administrator role"));
        }
    }

    @Test
    void testRegisterSuccess() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(
                "Aarav Sharma",
                "aarav.sharma@example.com",
                "Password123!"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Account registered successfully"))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.email").value("aarav.sharma@example.com"))
                .andExpect(jsonPath("$.data.user.fullName").value("Aarav Sharma"))
                .andExpect(jsonPath("$.data.user.roles[0]").value("ROLE_STUDENT"));

        // Verify user in database
        User savedUser = userRepository.findByEmail("aarav.sharma@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getFullName()).isEqualTo("Aarav Sharma");

        // Verify BCrypt hashing (strength 10 format starts with $2a$10$)
        assertThat(savedUser.getPasswordHash()).startsWith("$2a$10$");
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("Password123!");
        assertThat(passwordEncoder.matches("Password123!", savedUser.getPasswordHash())).isTrue();

        // Verify assigned role is strictly ROLE_STUDENT
        assertThat(savedUser.getRoles()).extracting(Role::getName).containsExactly("ROLE_STUDENT");
    }

    @Test
    void testRegisterPreventsSelfAssignedAdminRole() throws Exception {
        // Client maliciously attempts to send an extra role field
        Map<String, Object> maliciousPayload = Map.of(
                "fullName", "Sneaky Attacker",
                "email", "attacker@example.com",
                "password", "SecurePass123!",
                "role", "ROLE_ADMIN",
                "roles", new String[]{"ROLE_ADMIN"}
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.user.roles[0]").value("ROLE_STUDENT"));

        // Verify in database that user was NOT given ROLE_ADMIN
        User user = userRepository.findByEmail("attacker@example.com").orElseThrow();
        assertThat(user.getRoles()).extracting(Role::getName).containsExactly("ROLE_STUDENT");
        assertThat(user.getRoles()).extracting(Role::getName).doesNotContain("ROLE_ADMIN");
    }

    @Test
    void testRegisterDuplicateEmailFails() throws Exception {
        RegisterRequestDto firstRequest = new RegisterRequestDto(
                "First User",
                "duplicate@example.com",
                "Password123!"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        RegisterRequestDto duplicateRequest = new RegisterRequestDto(
                "Second User",
                "duplicate@example.com",
                "DifferentPass123!"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An account with email 'duplicate@example.com' already exists."));
    }

    @Test
    void testRegisterValidationFailures() throws Exception {
        // Short password, invalid email, blank name
        RegisterRequestDto invalidRequest = new RegisterRequestDto(
                "",
                "invalid-email-address",
                "short"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Register user first
        RegisterRequestDto register = new RegisterRequestDto(
                "Diya Patel",
                "diya@example.com",
                "MyStrongPassword123!"
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        // Perform login
        LoginRequestDto login = new LoginRequestDto("diya@example.com", "MyStrongPassword123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.email").value("diya@example.com"))
                .andExpect(jsonPath("$.data.user.fullName").value("Diya Patel"));
    }

    @Test
    void testLoginFailureWrongPassword() throws Exception {
        RegisterRequestDto register = new RegisterRequestDto(
                "Karan Gupta",
                "karan@example.com",
                "CorrectPassword123!"
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        LoginRequestDto badLogin = new LoginRequestDto("karan@example.com", "WrongPassword999!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    void testLoginFailureUnknownEmail() throws Exception {
        LoginRequestDto unknownLogin = new LoginRequestDto("unknown.user@example.com", "AnyPassword123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unknownLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    void testGetCurrentUserWithValidToken() throws Exception {
        // Register and obtain token
        RegisterRequestDto register = new RegisterRequestDto(
                "Meera Roy",
                "meera@example.com",
                "MeeraPass123!"
        );
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).path("data").path("token").asText();

        // Call protected /api/auth/me
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Profile retrieved successfully"))
                .andExpect(jsonPath("$.data.email").value("meera@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Meera Roy"))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_STUDENT"));
    }

    @Test
    void testGetCurrentUserUnauthenticatedFails() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required. Please provide a valid Bearer token."));
    }

    @Test
    void testGetCurrentUserWithInvalidTokenFails() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer invalid.tampered.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void testGetCurrentUserWithExpiredTokenFails() throws Exception {
        // Register user
        User user = new User("Expired User", "expired@example.com", passwordEncoder.encode("Pass123!"));
        user.addRole(roleRepository.findByName("ROLE_STUDENT").orElseThrow());
        userRepository.save(user);

        // Generate expired token
        JwtService expiredJwtService = new JwtService(
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
                -10000 // Expired 10 seconds ago
        );
        String expiredToken = expiredJwtService.generateToken(user);

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401));
    }
}
