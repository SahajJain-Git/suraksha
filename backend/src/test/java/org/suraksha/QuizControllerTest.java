package org.suraksha;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.suraksha.dto.QuestionAnswerDto;
import org.suraksha.dto.QuizSubmissionRequestDto;
import org.suraksha.entity.*;
import org.suraksha.repository.*;
import org.suraksha.security.CustomUserDetails;
import org.suraksha.security.JwtService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private QuizOptionRepository quizOptionRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User testStudent;
    private String jwtToken;
    private Quiz testQuiz;
    private QuizQuestion question1;
    private QuizOption opt1Correct;
    private QuizOption opt1Wrong;
    private QuizQuestion question2;
    private QuizOption opt2Correct;
    private QuizOption opt2Wrong;

    @BeforeEach
    void setUp() {
        quizAttemptRepository.deleteAll();
        quizRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Setup Student User & JWT
        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_STUDENT", "Student role")));

        testStudent = new User("Aarav Student", "student.quiz@suraksha.org", passwordEncoder.encode("Password123!"));
        testStudent.addRole(studentRole);
        testStudent = userRepository.save(testStudent);

        jwtToken = jwtService.generateToken(testStudent);

        // 2. Setup Course and Quiz
        Course course = new Course("earthquake-101", "Earthquake Prep", "EARTHQUAKE", "BEGINNER", 20, "Summary");
        course = courseRepository.save(course);

        testQuiz = new Quiz("quiz-earthquake", "Earthquake Certification Quiz", 75);
        testQuiz.setCourse(course);
        testQuiz = quizRepository.save(testQuiz);

        // Question 1
        question1 = new QuizQuestion(1, "What is the primary action during shaking?", "Drop, Cover, and Hold On shields vital organs.");
        testQuiz.addQuestion(question1);
        question1 = quizQuestionRepository.save(question1);

        opt1Correct = new QuizOption("Drop, Cover, and Hold On", true, 1);
        opt1Wrong = new QuizOption("Run outside immediately", false, 2);
        question1.addOption(opt1Correct);
        question1.addOption(opt1Wrong);
        quizOptionRepository.save(opt1Correct);
        quizOptionRepository.save(opt1Wrong);

        // Question 2
        question2 = new QuizQuestion(2, "What should you do if you smell gas post-shaking?", "Sparks ignite gas leaks.");
        testQuiz.addQuestion(question2);
        question2 = quizQuestionRepository.save(question2);

        opt2Correct = new QuizOption("Evacuate without using any electrical switches", true, 1);
        opt2Wrong = new QuizOption("Turn on lights to inspect pipes", false, 2);
        question2.addOption(opt2Correct);
        question2.addOption(opt2Wrong);
        quizOptionRepository.save(opt2Correct);
        quizOptionRepository.save(opt2Wrong);
    }

    @Test
    void testGetQuizPublicAndNeverLeaksAnswers() throws Exception {
        mockMvc.perform(get("/api/quizzes/" + testQuiz.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testQuiz.getId()))
                .andExpect(jsonPath("$.data.title").value("Earthquake Certification Quiz"))
                .andExpect(jsonPath("$.data.totalQuestions").value(2))
                .andExpect(jsonPath("$.data.questions", hasSize(2)))
                // CRITICAL SECURITY CHECKS: Neither explanation nor isCorrect is exposed
                .andExpect(jsonPath("$.data.questions[0].explanation").doesNotExist())
                .andExpect(jsonPath("$.data.questions[0].options[0].isCorrect").doesNotExist())
                .andExpect(jsonPath("$.data.questions[0].options[1].isCorrect").doesNotExist())
                .andExpect(jsonPath("$.data.questions[1].explanation").doesNotExist())
                .andExpect(jsonPath("$.data.questions[1].options[0].isCorrect").doesNotExist());
    }

    @Test
    void testGetQuizBySlugPublic() throws Exception {
        mockMvc.perform(get("/api/quizzes/quiz-earthquake")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.slug").value("quiz-earthquake"));
    }

    @Test
    void testGetQuizNotFound() throws Exception {
        mockMvc.perform(get("/api/quizzes/nonexistent-quiz-slug")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testSubmitQuizUnauthorizedWithoutToken() throws Exception {
        QuizSubmissionRequestDto request = new QuizSubmissionRequestDto(List.of(
                new QuestionAnswerDto(question1.getId(), opt1Correct.getId())
        ));

        mockMvc.perform(post("/api/quizzes/" + testQuiz.getId() + "/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testSubmitQuizSuccessPassingScore() throws Exception {
        // Both answers correct -> 100% -> Passed (threshold 75%)
        QuizSubmissionRequestDto request = new QuizSubmissionRequestDto(List.of(
                new QuestionAnswerDto(question1.getId(), opt1Correct.getId()),
                new QuestionAnswerDto(question2.getId(), opt2Correct.getId())
        ));

        mockMvc.perform(post("/api/quizzes/" + testQuiz.getId() + "/submit")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scorePercent").value(100))
                .andExpect(jsonPath("$.data.passed").value(true))
                .andExpect(jsonPath("$.data.totalQuestions").value(2))
                .andExpect(jsonPath("$.data.correctAnswers").value(2))
                // Post-submission review legitimately reveals explanation and answer breakdown
                .andExpect(jsonPath("$.data.questionResults[0].isCorrect").value(true))
                .andExpect(jsonPath("$.data.questionResults[0].explanation").isNotEmpty())
                .andExpect(jsonPath("$.data.questionResults[1].isCorrect").value(true));

        // Verify QuizAttempt persisted in DB
        List<QuizAttempt> attempts = quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(testStudent.getId());
        assertThat(attempts).hasSize(1);
        assertThat(attempts.get(0).getScorePercent()).isEqualTo(100);
        assertThat(attempts.get(0).getPassed()).isTrue();
    }

    @Test
    void testSubmitQuizSuccessFailingScore() throws Exception {
        // Both answers incorrect -> 0% -> Failed
        QuizSubmissionRequestDto request = new QuizSubmissionRequestDto(List.of(
                new QuestionAnswerDto(question1.getId(), opt1Wrong.getId()),
                new QuestionAnswerDto(question2.getId(), opt2Wrong.getId())
        ));

        mockMvc.perform(post("/api/quizzes/" + testQuiz.getId() + "/submit")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scorePercent").value(0))
                .andExpect(jsonPath("$.data.passed").value(false))
                .andExpect(jsonPath("$.data.correctAnswers").value(0))
                .andExpect(jsonPath("$.data.questionResults[0].isCorrect").value(false));

        List<QuizAttempt> attempts = quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(testStudent.getId());
        assertThat(attempts).hasSize(1);
        assertThat(attempts.get(0).getScorePercent()).isEqualTo(0);
        assertThat(attempts.get(0).getPassed()).isFalse();
    }
}
