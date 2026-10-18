package org.suraksha.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.suraksha.dto.AuthResponseDto;
import org.suraksha.dto.LoginRequestDto;
import org.suraksha.dto.RegisterRequestDto;
import org.suraksha.dto.UserProfileDto;
import org.suraksha.entity.Role;
import org.suraksha.entity.User;
import org.suraksha.exception.DuplicateResourceException;
import org.suraksha.exception.ResourceNotFoundException;
import org.suraksha.repository.RoleRepository;
import org.suraksha.repository.UserRepository;
import org.suraksha.security.JwtService;

/**
 * Service orchestrating user registration, credential authentication,
 * password hashing with BCrypt, and JWT token issuance.
 */
@Service
public class AuthService {

    private static final String DEFAULT_ROLE = "ROLE_STUDENT";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registers a new user account.
     * Validates uniqueness, hashes password with BCrypt (factor 10),
     * and server-side assigns ROLE_STUDENT. Never accepts a client role.
     */
    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        final String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("An account with email '" + normalizedEmail + "' already exists.");
        }

        // Retrieve or create the default student role
        Role studentRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseGet(() -> roleRepository.save(new Role(DEFAULT_ROLE, "Standard student learner")));

        // Hash password securely with BCrypt
        String passwordHash = passwordEncoder.encode(request.getPassword());

        User newUser = new User(request.getFullName().trim(), normalizedEmail, passwordHash);
        newUser.addRole(studentRole);

        User savedUser = userRepository.save(newUser);

        String token = jwtService.generateToken(savedUser);
        UserProfileDto profileDto = UserProfileDto.fromEntity(savedUser);

        return new AuthResponseDto(token, jwtService.getExpirationMs(), profileDto);
    }

    /**
     * Authenticates user credentials and issues a signed, expiring JWT token.
     */
    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto request) {
        final String normalizedEmail = request.getEmail().trim().toLowerCase();

        // Will throw BadCredentialsException if authentication fails
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", normalizedEmail));

        String token = jwtService.generateToken(user);
        UserProfileDto profileDto = UserProfileDto.fromEntity(user);

        return new AuthResponseDto(token, jwtService.getExpirationMs(), profileDto);
    }

    /**
     * Retrieves the safe user profile for the currently authenticated principal.
     */
    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUserProfile(String email) {
        final String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", normalizedEmail));

        return UserProfileDto.fromEntity(user);
    }
}
