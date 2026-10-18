package org.suraksha.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.suraksha.entity.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DataJpaTest repository slice test for Quiz, Question, Option, and Attempt persistence.
 */
@DataJpaTest
@ActiveProfiles("test")
class QuizRepositoryJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private QuizOptionRepository quizOptionRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    private User testUser;
    private Course testCourse;
    private Quiz testQuiz;

    @BeforeEach
    void setUp() {
        testUser = new User("Rahul Verma", "rahul.quiz@suraksha.org", "pwdHash");
        entityManager.persist(testUser);

        testCourse = new Course("flood-101", "Flood Safety", "FLOOD", "BEGINNER", 20, "Summary");
        entityManager.persist(testCourse);

        testQuiz = new Quiz("quiz-flood", "Flood Certification Quiz", 75);
        testQuiz.setCourse(testCourse);
        entityManager.persist(testQuiz);

        QuizQuestion q1 = new QuizQuestion(1, "What depth of moving water can knock down an adult?", "6 inches of moving water");
        q1.setQuiz(testQuiz);
        entityManager.persist(q1);

        QuizOption opt1 = new QuizOption("6 inches (15 cm)", true, 1);
        opt1.setQuestion(q1);
        entityManager.persist(opt1);

        QuizOption opt2 = new QuizOption("3 feet (90 cm)", false, 2);
        opt2.setQuestion(q1);
        entityManager.persist(opt2);

        QuizQuestion q2 = new QuizQuestion(2, "Should you drive into flooded waters?", "Turn Around, Don't Drown");
        q2.setQuiz(testQuiz);
        entityManager.persist(q2);

        entityManager.flush();
    }

    @Test
    @DisplayName("Queries: findBySlug and findByCourseId resolve quiz correctly")
    void testFindQuizBySlugAndCourseId() {
        Optional<Quiz> bySlug = quizRepository.findBySlug("quiz-flood");
        assertThat(bySlug).isPresent();
        assertThat(bySlug.get().getTitle()).isEqualTo("Flood Certification Quiz");

        Optional<Quiz> byCourse = quizRepository.findByCourseId(testCourse.getId());
        assertThat(byCourse).isPresent();
        assertThat(byCourse.get().getSlug()).isEqualTo("quiz-flood");
    }

    @Test
    @DisplayName("Hierarchy: Ordered questions and options resolve in sequence")
    void testQuestionAndOptionHierarchy() {
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderBySequenceOrderAsc(testQuiz.getId());
        assertThat(questions).hasSize(2);
        assertThat(questions.get(0).getSequenceOrder()).isEqualTo(1);
        assertThat(questions.get(1).getSequenceOrder()).isEqualTo(2);

        List<QuizOption> options = quizOptionRepository.findByQuestionIdOrderBySequenceOrderAsc(questions.get(0).getId());
        assertThat(options).hasSize(2);
        assertThat(options.get(0).getIsCorrect()).isTrue();
        assertThat(options.get(1).getIsCorrect()).isFalse();
    }

    @Test
    @DisplayName("Attempts: Attempt logging and reverse chronological ordering per user")
    void testQuizAttemptLoggingAndOrder() {
        QuizAttempt attempt1 = new QuizAttempt(testUser, testQuiz, 50, 2, 1, false);
        entityManager.persist(attempt1);

        QuizAttempt attempt2 = new QuizAttempt(testUser, testQuiz, 100, 2, 2, true);
        entityManager.persist(attempt2);
        entityManager.flush();

        List<QuizAttempt> attempts = quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(testUser.getId());
        assertThat(attempts).hasSize(2);
        // Latest attempt should be first in reverse-chronological order
        assertThat(attempts.get(0).getId()).isEqualTo(attempt2.getId());
        assertThat(attempts.get(0).getScorePercent()).isEqualTo(100);
        assertThat(attempts.get(1).getScorePercent()).isEqualTo(50);
    }

    @Test
    @DisplayName("Constraint: Duplicate quiz slug throws DataIntegrityViolationException on flush")
    void testDuplicateQuizSlugThrows() {
        Quiz duplicate = new Quiz("quiz-flood", "Another Flood Quiz", 80);
        duplicate.setCourse(testCourse);

        assertThatThrownBy(() -> {
            quizRepository.save(duplicate);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
