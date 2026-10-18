package org.suraksha.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.suraksha.dto.AuthResponseDto;
import org.suraksha.dto.LoginRequestDto;
import org.suraksha.dto.RegisterRequestDto;
import org.suraksha.entity.Role;
import org.suraksha.entity.User;
import org.suraksha.exception.DuplicateResourceException;
import org.suraksha.exception.ResourceNotFoundException;
import org.suraksha.repository.RoleRepository;
import org.suraksha.repository.UserRepository;
import org.suraksha.security.JwtService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService isolating repository and authentication behavior with Mockito,
 * combined with a genuine JwtService instance to avoid JDK 25 dynamic agent bytecode interference.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    private JwtService jwtService;
    private AuthService authService;

    private Role studentRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970", 86400000L);
        authService = new AuthService(userRepository, roleRepository, passwordEncoder, jwtService, authenticationManager);

        studentRole = new Role("ROLE_STUDENT", "Student learner");
        testUser = new User("Aarav Sharma", "aarav@suraksha.org", "encodedPassword123");
        testUser.setId(101L);
        testUser.addRole(studentRole);
    }

    @Test
    @DisplayName("Register: Successfully hashes password with BCrypt and assigns ROLE_STUDENT")
    void testRegisterSuccess() {
        RegisterRequestDto request = new RegisterRequestDto("  Aarav Sharma  ", "  AARAV@SURAKSHA.ORG  ", "PlainPassword123!");

        when(userRepository.existsByEmail("aarav@suraksha.org")).thenReturn(false);
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode("PlainPassword123!")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(101L);
            return u;
        });

        AuthResponseDto response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getExpiresInMs()).isEqualTo(86400000L);
        assertThat(response.getUser().getEmail()).isEqualTo("aarav@suraksha.org");
        assertThat(response.getUser().getFullName()).isEqualTo("Aarav Sharma");
        assertThat(response.getUser().getRoles()).contains("ROLE_STUDENT");

        // Verify the generated token decodes properly to the user's email
        assertThat(jwtService.extractEmail(response.getToken())).isEqualTo("aarav@suraksha.org");

        // Verify password was hashed and never passed in plaintext
        verify(passwordEncoder).encode("PlainPassword123!");
        verify(userRepository).save(argThat(u ->
                u.getEmail().equals("aarav@suraksha.org") &&
                u.getPasswordHash().equals("encodedPassword123") &&
                u.getRoles().contains(studentRole)
        ));
    }

    @Test
    @DisplayName("Register: Duplicate email throws DuplicateResourceException")
    void testRegisterDuplicateEmailThrows() {
        RegisterRequestDto request = new RegisterRequestDto("Aarav Sharma", "aarav@suraksha.org", "Password123!");

        when(userRepository.existsByEmail("aarav@suraksha.org")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("aarav@suraksha.org");

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Register: Automatically provisions ROLE_STUDENT if missing from database")
    void testRegisterProvisionsDefaultRoleWhenMissing() {
        RegisterRequestDto request = new RegisterRequestDto("New Learner", "new@suraksha.org", "Password123!");

        when(userRepository.existsByEmail("new@suraksha.org")).thenReturn(false);
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(studentRole);
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        AuthResponseDto response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
        verify(roleRepository).save(argThat(r -> r.getName().equals("ROLE_STUDENT")));
    }

    @Test
    @DisplayName("Login: Valid credentials authenticate via AuthenticationManager and return JWT")
    void testLoginSuccess() {
        LoginRequestDto request = new LoginRequestDto("  AARAV@SURAKSHA.ORG  ", "PlainPassword123!");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("aarav@suraksha.org", "PlainPassword123!"));
        when(userRepository.findByEmail("aarav@suraksha.org")).thenReturn(Optional.of(testUser));

        AuthResponseDto response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getUser().getEmail()).isEqualTo("aarav@suraksha.org");
        assertThat(jwtService.extractEmail(response.getToken())).isEqualTo("aarav@suraksha.org");

        verify(authenticationManager).authenticate(argThat(auth ->
                auth.getPrincipal().equals("aarav@suraksha.org") &&
                auth.getCredentials().equals("PlainPassword123!")
        ));
    }

    @Test
    @DisplayName("Login: Bad credentials throw BadCredentialsException")
    void testLoginBadCredentialsThrows() {
        LoginRequestDto request = new LoginRequestDto("aarav@suraksha.org", "WrongPassword!");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Bad credentials");

        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Login: User authenticated by manager but missing in repository throws ResourceNotFoundException")
    void testLoginUserNotFoundThrows() {
        LoginRequestDto request = new LoginRequestDto("ghost@suraksha.org", "Password123!");

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("ghost@suraksha.org")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ghost@suraksha.org");
    }
}
