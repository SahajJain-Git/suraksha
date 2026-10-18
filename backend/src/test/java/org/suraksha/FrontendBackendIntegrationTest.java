package org.suraksha;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.suraksha.dto.*;
import org.suraksha.entity.*;
import org.suraksha.repository.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real End-to-End HTTP Integration Test for Milestone 8 (Frontend-Backend Integration).
 * Boots embedded Tomcat web server on a live network port and verifies actual HTTP
 * request/response exchange matching SurakshaApi client contracts.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FrontendBackendIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

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
    private QuizRepository quizRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private QuizOptionRepository quizOptionRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private DrillScenarioRepository drillScenarioRepository;

    @Autowired
    private DrillStepRepository drillStepRepository;

    @Autowired
    private DrillChoiceRepository drillChoiceRepository;

    @Autowired
    private DrillAttemptRepository drillAttemptRepository;

    @Autowired
    private UserLessonProgressRepository userLessonProgressRepository;

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String baseUrl;
    private Course seededCourse;
    private Lesson seededLesson1;
    private Lesson seededLesson2;
    private Quiz seededQuiz;
    private QuizQuestion question1;
    private QuizOption q1OptCorrect;
    private QuizOption q1OptWrong;
    private DrillScenario seededDrill;
    private DrillChoice seededChoiceSafe;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api";

        // Clean tables
        drillAttemptRepository.deleteAll();
        drillScenarioRepository.findAll().forEach(scenario -> {
            scenario.setInitialStep(null);
            drillScenarioRepository.saveAndFlush(scenario);
        });
        drillChoiceRepository.deleteAll();
        drillStepRepository.deleteAll();
        drillScenarioRepository.deleteAll();

        quizAttemptRepository.deleteAll();
        quizOptionRepository.deleteAll();
        quizQuestionRepository.deleteAll();
        quizRepository.deleteAll();

        userLessonProgressRepository.deleteAll();
        lessonRepository.deleteAll();
        courseRepository.deleteAll();

        contactMessageRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Ensure Roles
        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_STUDENT", "Student Role")));

        // 2. Seed Course & Lessons
        seededCourse = new Course("earthquake-101", "Earthquake Preparedness", "EARTHQUAKE", "BEGINNER", 25, "Drop, Cover, and Hold On.");
        seededLesson1 = new Lesson("eq-lesson-1", "Science of Shaking", 1, "<p>Seismic waves</p>", "DO anchor shelves", "DONT use elevators");
        seededLesson2 = new Lesson("eq-lesson-2", "Drop, Cover, Hold On", 2, "<p>Drop technique</p>", "DO stay low", "DONT run outside");
        seededCourse.addLesson(seededLesson1);
        seededCourse.addLesson(seededLesson2);
        seededCourse = courseRepository.save(seededCourse);
        seededLesson1 = seededCourse.getLessons().get(0);
        seededLesson2 = seededCourse.getLessons().get(1);

        // 3. Seed Quiz & Questions (Zero-Leak test fixture)
        seededQuiz = new Quiz("quiz-earthquake", "Earthquake Safety Certification Quiz", 75);
        seededQuiz.setCourse(seededCourse);
        seededQuiz = quizRepository.save(seededQuiz);

        question1 = new QuizQuestion(1, "What is the safest immediate action during shaking?", "Drop, Cover, and Hold On shields against flying debris.");
        seededQuiz.addQuestion(question1);
        question1 = quizQuestionRepository.save(question1);

        q1OptCorrect = new QuizOption("Drop, Cover, and Hold On under a sturdy desk", true, 1);
        q1OptWrong = new QuizOption("Run outside immediately", false, 2);
        question1.addOption(q1OptCorrect);
        question1.addOption(q1OptWrong);
        quizOptionRepository.save(q1OptCorrect);
        quizOptionRepository.save(q1OptWrong);

        // 4. Seed Drill Scenario
        seededDrill = new DrillScenario("drill-earthquake", "School Earthquake Simulation", "EARTHQUAKE", "Tremors on 3rd floor");
        seededDrill.setCourse(seededCourse);
        seededDrill = drillScenarioRepository.save(seededDrill);

        DrillStep step1 = new DrillStep("step-1", "Ground shakes violently.", "What do you do?", false);
        step1.setScenario(seededDrill);
        step1 = drillStepRepository.save(step1);
        seededDrill.setInitialStep(step1);
        seededDrill = drillScenarioRepository.save(seededDrill);

        seededChoiceSafe = new DrillChoice("Drop under sturdy wooden desk.", "SAFE", "Shielded head from lights.", 50, 1);
        seededChoiceSafe.setStep(step1);
        seededChoiceSafe = drillChoiceRepository.save(seededChoiceSafe);
    }

    @Test
    void testFullStudentLearningLifecycleE2E() throws Exception {
        // Step 1: Register new student via HTTP POST /api/auth/register
        RegisterRequestDto regReq = new RegisterRequestDto("Neha Sen", "neha.sen@school.edu", "Password123!");
        ResponseEntity<String> regResp = restTemplate.postForEntity(baseUrl + "/auth/register", regReq, String.class);

        assertThat(regResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode regJson = objectMapper.readTree(regResp.getBody());
        assertThat(regJson.get("success").asBoolean()).isTrue();
        String token = regJson.get("data").get("token").asText();
        assertThat(token).isNotEmpty();

        // Step 1b: Verify Duplicate Registration Rejection (HTTP 409 Conflict)
        ResponseEntity<String> dupResp = restTemplate.postForEntity(baseUrl + "/auth/register", regReq, String.class);
        assertThat(dupResp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Step 2: Successful Login via HTTP POST /api/auth/login
        LoginRequestDto loginReq = new LoginRequestDto("neha.sen@school.edu", "Password123!");
        ResponseEntity<String> loginResp = restTemplate.postForEntity(baseUrl + "/auth/login", loginReq, String.class);

        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode loginJson = objectMapper.readTree(loginResp.getBody());
        assertThat(loginJson.get("success").asBoolean()).isTrue();
        String loginToken = loginJson.get("data").get("token").asText();
        assertThat(loginToken).isNotEmpty();

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(loginToken);
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Step 3: Verify Profile via HTTP GET /api/auth/me
        HttpEntity<Void> meEntity = new HttpEntity<>(authHeaders);
        ResponseEntity<String> meResp = restTemplate.exchange(baseUrl + "/auth/me", HttpMethod.GET, meEntity, String.class);

        assertThat(meResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode meJson = objectMapper.readTree(meResp.getBody());
        assertThat(meJson.get("data").get("email").asText()).isEqualTo("neha.sen@school.edu");
        assertThat(meJson.get("data").get("fullName").asText()).isEqualTo("Neha Sen");

        // Step 4a: Browse Courses via HTTP GET /api/courses with category filter
        ResponseEntity<String> coursesResp = restTemplate.getForEntity(baseUrl + "/courses?category=EARTHQUAKE", String.class);
        assertThat(coursesResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode coursesJson = objectMapper.readTree(coursesResp.getBody());
        assertThat(coursesJson.get("data").size()).isEqualTo(1);
        assertThat(coursesJson.get("data").get(0).get("slug").asText()).isEqualTo("earthquake-101");

        // Step 4b: Search Courses via HTTP GET /api/courses?search=Drop
        ResponseEntity<String> searchResp = restTemplate.getForEntity(baseUrl + "/courses?search=Drop", String.class);
        assertThat(searchResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode searchJson = objectMapper.readTree(searchResp.getBody());
        assertThat(searchJson.get("data").size()).isEqualTo(1);

        // Step 5: Get Course Detail via HTTP GET /api/courses/{slug}
        ResponseEntity<String> courseDetailResp = restTemplate.getForEntity(baseUrl + "/courses/earthquake-101", String.class);
        assertThat(courseDetailResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode courseDetailJson = objectMapper.readTree(courseDetailResp.getBody());
        assertThat(courseDetailJson.get("data").get("lessons").size()).isEqualTo(2);

        // Step 6: Mark Lesson 1 Complete via HTTP POST /api/progress/lessons/{id}
        HttpEntity<Void> progressEntity = new HttpEntity<>(authHeaders);
        ResponseEntity<String> progress1Resp = restTemplate.exchange(
                baseUrl + "/progress/lessons/" + seededLesson1.getId(),
                HttpMethod.POST,
                progressEntity,
                String.class
        );
        assertThat(progress1Resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode progress1Json = objectMapper.readTree(progress1Resp.getBody());
        assertThat(progress1Json.get("data").get("completed").asBoolean()).isTrue();

        // Idempotency: Call same completion again
        ResponseEntity<String> progressIdempotentResp = restTemplate.exchange(
                baseUrl + "/progress/lessons/" + seededLesson1.getId(),
                HttpMethod.POST,
                progressEntity,
                String.class
        );
        assertThat(progressIdempotentResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Step 7: Check User Progress Dashboard via HTTP GET /api/progress
        ResponseEntity<String> getProgressResp = restTemplate.exchange(
                baseUrl + "/progress",
                HttpMethod.GET,
                progressEntity,
                String.class
        );
        assertThat(getProgressResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode getProgressJson = objectMapper.readTree(getProgressResp.getBody());
        assertThat(getProgressJson.get("data").get("totalCompletedLessonsCount").asInt()).isEqualTo(1);
        assertThat(getProgressJson.get("data").get("completedCoursesCount").asInt()).isEqualTo(0); // 1 of 2 done

        // Mark Lesson 2 Complete -> Course completes
        restTemplate.exchange(
                baseUrl + "/progress/lessons/" + seededLesson2.getId(),
                HttpMethod.POST,
                progressEntity,
                String.class
        );
        ResponseEntity<String> courseCompleteResp = restTemplate.exchange(
                baseUrl + "/progress",
                HttpMethod.GET,
                progressEntity,
                String.class
        );
        JsonNode courseCompleteJson = objectMapper.readTree(courseCompleteResp.getBody());
        assertThat(courseCompleteJson.get("data").get("totalCompletedLessonsCount").asInt()).isEqualTo(2);
        assertThat(courseCompleteJson.get("data").get("completedCoursesCount").asInt()).isEqualTo(1);

        // Step 8: Verify Zero-Leak on HTTP GET /api/quizzes/{id}
        ResponseEntity<String> quizGetResp = restTemplate.getForEntity(baseUrl + "/quizzes/quiz-earthquake", String.class);
        assertThat(quizGetResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode quizGetJson = objectMapper.readTree(quizGetResp.getBody());
        // Verify no leak of answers or explanations in public retrieval
        assertThat(quizGetJson.get("data").get("questions").get(0).has("explanation")).isFalse();
        assertThat(quizGetJson.get("data").get("questions").get(0).get("options").get(0).has("isCorrect")).isFalse();

        // Step 9: Submit Quiz via HTTP POST /api/quizzes/{id}/submit
        QuizSubmissionRequestDto quizSubmitReq = new QuizSubmissionRequestDto(List.of(
                new QuestionAnswerDto(question1.getId(), q1OptCorrect.getId())
        ));
        HttpEntity<QuizSubmissionRequestDto> quizSubmitEntity = new HttpEntity<>(quizSubmitReq, authHeaders);
        ResponseEntity<String> quizSubmitResp = restTemplate.exchange(
                baseUrl + "/quizzes/quiz-earthquake/submit",
                HttpMethod.POST,
                quizSubmitEntity,
                String.class
        );
        assertThat(quizSubmitResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode quizSubmitJson = objectMapper.readTree(quizSubmitResp.getBody());
        assertThat(quizSubmitJson.get("data").get("scorePercent").asInt()).isEqualTo(100);
        assertThat(quizSubmitJson.get("data").get("passed").asBoolean()).isTrue();
        // Server revealed explanation during review
        assertThat(quizSubmitJson.get("data").get("questionResults").get(0).get("explanation").asText()).isNotEmpty();

        // Step 10a: Retrieve Mock Drill Scenario via HTTP GET /api/drills/{slug}
        ResponseEntity<String> drillGetResp = restTemplate.getForEntity(baseUrl + "/drills/drill-earthquake", String.class);
        assertThat(drillGetResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode drillGetJson = objectMapper.readTree(drillGetResp.getBody());
        assertThat(drillGetJson.get("data").get("slug").asText()).isEqualTo("drill-earthquake");
        assertThat(drillGetJson.get("data").get("steps").size()).isGreaterThanOrEqualTo(1);

        // Step 10b: Submit Mock Drill Decisions via HTTP POST /api/drills/{id}/submit
        DrillSubmissionRequestDto drillSubmitReq = new DrillSubmissionRequestDto(List.of(seededChoiceSafe.getId()));
        HttpEntity<DrillSubmissionRequestDto> drillSubmitEntity = new HttpEntity<>(drillSubmitReq, authHeaders);
        ResponseEntity<String> drillSubmitResp = restTemplate.exchange(
                baseUrl + "/drills/" + seededDrill.getId() + "/submit",
                HttpMethod.POST,
                drillSubmitEntity,
                String.class
        );
        assertThat(drillSubmitResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode drillSubmitJson = objectMapper.readTree(drillSubmitResp.getBody());
        assertThat(drillSubmitJson.get("data").get("totalScore").asInt()).isEqualTo(50);
        assertThat(drillSubmitJson.get("data").get("ratingLabel").asText()).isEqualTo("Safety Conscious");

        // Step 11: Submit Contact Form via HTTP POST /api/contact
        ContactRequestDto contactReq = new ContactRequestDto(
                "Headmaster Roy",
                "roy@delhischool.edu",
                "SCHOOL",
                "Campus Workshop Request",
                "Please coordinate an on-campus earthquake evacuation drill for 500 students."
        );
        ResponseEntity<String> contactResp = restTemplate.postForEntity(baseUrl + "/contact", contactReq, String.class);
        assertThat(contactResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode contactJson = objectMapper.readTree(contactResp.getBody());
        assertThat(contactJson.get("data").get("status").asText()).isEqualTo("NEW");

        // Step 12: Verify Invalid/Expired Token Rejection (HTTP 401)
        HttpHeaders badHeaders = new HttpHeaders();
        badHeaders.setBearerAuth("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature");
        HttpEntity<Void> badAuthEntity = new HttpEntity<>(badHeaders);
        ResponseEntity<String> badAuthResp = restTemplate.exchange(baseUrl + "/progress", HttpMethod.GET, badAuthEntity, String.class);
        assertThat(badAuthResp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @AfterEach
    void tearDown() {
        drillAttemptRepository.deleteAll();
        drillScenarioRepository.findAll().forEach(scenario -> {
            scenario.setInitialStep(null);
            drillScenarioRepository.saveAndFlush(scenario);
        });
        drillChoiceRepository.deleteAll();
        drillStepRepository.deleteAll();
        drillScenarioRepository.deleteAll();

        quizAttemptRepository.deleteAll();
        quizOptionRepository.deleteAll();
        quizQuestionRepository.deleteAll();
        quizRepository.deleteAll();

        userLessonProgressRepository.deleteAll();
        lessonRepository.deleteAll();
        courseRepository.deleteAll();

        contactMessageRepository.deleteAll();
        userRepository.deleteAll();
    }
}
