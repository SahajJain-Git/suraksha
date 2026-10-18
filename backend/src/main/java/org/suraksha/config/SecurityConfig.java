package org.suraksha.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.suraksha.security.CustomUserDetailsService;
import org.suraksha.security.JwtAuthenticationEntryPoint;
import org.suraksha.security.JwtAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 6 / Spring Boot 3.3.5 Central Security Configuration.
 * Configures stateless JWT Bearer token authentication, BCrypt password hashing,
 * and endpoint authorization policies.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(
            CustomUserDetailsService userDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt with strength factor 10 as specified in requirements
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Stateless REST APIs with Bearer tokens do not use browser session cookies, so CSRF is disabled
            .csrf(AbstractHttpConfigurer::disable)
            // Enable CORS using our configured origin rules
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Exception handling with custom entry point returning 401 JSON
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            // Stateless session policy: no HTTP sessions will be created or used
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Endpoint authorization policies
            .authorizeHttpRequests(auth -> auth
                // Allow CORS pre-flight OPTIONS requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Public health check endpoint
                .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                // Public authentication endpoints (registration & login)
                .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                // Public educational courses and lesson content
                .requestMatchers(HttpMethod.GET, "/api/courses/**").permitAll()
                // Public quiz preview (questions & options without leaked answers)
                .requestMatchers(HttpMethod.GET, "/api/quizzes/**").permitAll()
                // Public virtual drill simulations
                .requestMatchers(HttpMethod.GET, "/api/drills/**").permitAll()
                // Public contact inquiries
                .requestMatchers(HttpMethod.POST, "/api/contact").permitAll()
                // Protected quiz submissions
                .requestMatchers(HttpMethod.POST, "/api/quizzes/**").authenticated()
                // Protected drill submissions
                .requestMatchers(HttpMethod.POST, "/api/drills/**").authenticated()
                // Protected student learning progress
                .requestMatchers("/api/progress/**").authenticated()
                // Protected current user profile
                .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:[*]",
                "http://127.0.0.1:[*]",
                "http://localhost:*",
                "http://127.0.0.1:*",
                "*"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
