package org.suraksha;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.suraksha.dto.LoginRequestDto;
import org.suraksha.entity.Course;
import org.suraksha.entity.Lesson;
import org.suraksha.entity.Role;
import org.suraksha.entity.User;
import org.suraksha.entity.UserLessonProgress;
import org.suraksha.repository.CourseRepository;
import org.suraksha.repository.LessonRepository;
import org.suraksha.repository.RoleRepository;
import org.suraksha.repository.UserLessonProgressRepository;
import org.suraksha.repository.UserRepository;
import org.suraksha.security.JwtService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security, Authorization, Validation, CORS, and Data Isolation Tests for Milestone 9.
 * Validates endpoint protection, token integrity, malformed JSON handling, input validation,
 * CORS preflight requests, and strict cross-user data isolation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {

    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private UserLessonProgressRepository progressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User userAlice;
    private User userBob;
    private String aliceToken;
    private String bobToken;
    private Course course;
    private Lesson lesson1;

    @BeforeEach
    void setUp() {
        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_STUDENT", "Student role")));

        userAlice = new User("Alice Walker", "alice.sec@suraksha.org", passwordEncoder.encode("AlicePass123!"));
        userAlice.addRole(studentRole);
        userRepository.save(userAlice);

        userBob = new User("Bob Builder", "bob.sec@suraksha.org", passwordEncoder.encode("BobPass123!"));
        userBob.addRole(studentRole);
        userRepository.save(userBob);

        aliceToken = jwtService.generateToken(userAlice);
        bobToken = jwtService.generateToken(userBob);

        course = new Course("sec-course", "Security Prep", "FIRE", "BEGINNER", 20, "Sec test course");
        courseRepository.save(course);

        lesson1 = new Lesson("sec-lesson-1", "Evacuation Route", 1, "Content", "Dos", "Donts");
        lesson1.setCourse(course);
        lessonRepository.save(lesson1);
    }

    // ==========================================
    // 1. Missing Token Tests (HTTP 401)
    // ==========================================

    @Test
    @DisplayName("GET /api/auth/me without token returns 401 Unauthorized")
    void testAuthMeWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/progress without token returns 401 Unauthorized")
    void testProgressWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/progress"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/progress/lessons/{id}/complete without token returns 401 Unauthorized")
    void testCompleteLessonWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/progress/lessons/" + lesson1.getId() + "/complete"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/quizzes/{id}/submit without token returns 401 Unauthorized")
    void testSubmitQuizWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/quizzes/1/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answers\":[]}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/drills/{id}/submit without token returns 401 Unauthorized")
    void testSubmitDrillWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/drills/1/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"choiceIds\":[1]}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==========================================
    // 2. Invalid, Expired, and Tampered Tokens
    // ==========================================

    @Test
    @DisplayName("Request with non-Bearer auth scheme returns 401")
    void testNonBearerSchemeReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Request with expired token returns 401")
    void testExpiredTokenReturns401() throws Exception {
        // JwtService with negative expiration produces expired token
        JwtService expiredJwtService = new JwtService(TEST_SECRET, -10000);
        String expiredToken = expiredJwtService.generateToken(userAlice);

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Request with tampered token signature returns 401")
    void testTamperedTokenReturns401() throws Exception {
        String tamperedToken = aliceToken.substring(0, aliceToken.length() - 5) + "ZZZZZ";

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tamperedToken))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // 3. Bad Credentials & Validation Errors
    // ==========================================

    @Test
    @DisplayName("Login with incorrect password returns 401 Unauthorized with safe message")
    void testBadCredentialsReturns401() throws Exception {
        LoginRequestDto badLogin = new LoginRequestDto("alice.sec@suraksha.org", "WrongPassword999!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Invalid email or password")));
    }

    @Test
    @DisplayName("POST /api/auth/register with malformed JSON returns 400 Bad Request without leaking internals")
    void testMalformedJsonReturns400() throws Exception {
        String malformedJson = "{ \"fullName\": \"Test\", \"email\": ";

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Malformed JSON")))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        // Ensure no stack traces or SQL details leaked
        assertThat(body).doesNotContain("org.hibernate");
        assertThat(body).doesNotContain("org.springframework.http.converter");
        assertThat(body).doesNotContain("at org.suraksha");
    }

    @Test
    @DisplayName("POST /api/auth/register with validation violations returns 400 with field details")
    void testValidationViolationsReturns400() throws Exception {
        String invalidPayload = "{ \"fullName\": \"\", \"email\": \"not-an-email\", \"password\": \"123\" }";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(3))));
    }

    // ==========================================
    // 4. CORS Preflight OPTIONS Request
    // ==========================================

    @Test
    @DisplayName("OPTIONS /api/courses with Origin returns 200 with CORS headers")
    void testCorsPreflightOptionsRequest() throws Exception {
        mockMvc.perform(options("/api/courses")
                        .header("Origin", "http://localhost:5500")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5500"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }

    // ==========================================
    // 5. Cross-User Data Isolation
    // ==========================================

    @Test
    @DisplayName("Strict User Isolation: Alice completing a lesson does NOT reflect in Bob's progress")
    void testCrossUserDataIsolation() throws Exception {
        // Alice completes lesson1
        progressRepository.save(new UserLessonProgress(userAlice, lesson1, true));

        // Alice queries progress -> shows 1 completed lesson
        mockMvc.perform(get("/api/progress")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCompletedLessonsCount").value(1));

        // Bob queries progress -> MUST show 0 completed lessons (strict isolation)
        mockMvc.perform(get("/api/progress")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCompletedLessonsCount").value(0))
                .andExpect(jsonPath("$.data.courseProgress[0].completedLessons").value(0));
    }
}
