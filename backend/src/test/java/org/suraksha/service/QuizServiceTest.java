package org.suraksha.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.suraksha.dto.*;
import org.suraksha.entity.*;
import org.suraksha.exception.ResourceNotFoundException;
import org.suraksha.repository.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QuizService verifying Zero-Leak secrecy, server scoring,
 * rounding mechanics, pass threshold boundaries, and incomplete submission handling.
 */
@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizQuestionRepository quizQuestionRepository;

    @Mock
    private QuizOptionRepository quizOptionRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private QuizService quizService;

    private User student;
    private Quiz testQuiz;
    private QuizQuestion q1;
    private QuizQuestion q2;
    private QuizQuestion q3;
    private QuizOption q1Opt1Correct;
    private QuizOption q1Opt2Wrong;
    private QuizOption q2Opt1Correct;
    private QuizOption q2Opt2Wrong;
    private QuizOption q3Opt1Correct;
    private QuizOption q3Opt2Wrong;

    @BeforeEach
    void setUp() {
        student = new User("Priya Patel", "priya@school.edu", "hashedPwd");
        student.setId(50L);

        Course course = new Course("earthquake-101", "Earthquake Preparedness", "EARTHQUAKE", "BEGINNER", 20, "Summary");
        course.setId(10L);

        testQuiz = new Quiz("quiz-earthquake", "Earthquake Preparedness Quiz", 70);
        testQuiz.setId(1L);
        testQuiz.setCourse(course);

        q1 = new QuizQuestion(1, "What should you do during shaking?", "Drop, Cover, and Hold On shields from debris.");
        q1.setId(101L);
        q1Opt1Correct = new QuizOption("Drop, Cover, and Hold On", true, 1);
        q1Opt1Correct.setId(1001L);
        q1Opt2Wrong = new QuizOption("Run outside into traffic", false, 2);
        q1Opt2Wrong.setId(1002L);

        q2 = new QuizQuestion(2, "Where is the safest indoor spot?", "Under a sturdy desk away from glass windows.");
        q2.setId(102L);
        q2Opt1Correct = new QuizOption("Under a sturdy desk", true, 1);
        q2Opt1Correct.setId(2001L);
        q2Opt2Wrong = new QuizOption("Next to large glass windows", false, 2);
        q2Opt2Wrong.setId(2002L);

        q3 = new QuizQuestion(3, "How long should your emergency kit sustain you?", "A 72-hour kit covers typical initial rescue windows.");
        q3.setId(103L);
        q3Opt1Correct = new QuizOption("At least 72 hours", true, 1);
        q3Opt1Correct.setId(3001L);
        q3Opt2Wrong = new QuizOption("Only 2 hours", false, 2);
        q3Opt2Wrong.setId(3002L);
    }

    @Test
    @DisplayName("Zero-Leak Quiz Retrieval: Question DTOs omit explanation and option isCorrect flags")
    void testGetQuizZeroLeakIntegrity() {
        when(quizRepository.findBySlug("quiz-earthquake")).thenReturn(Optional.of(testQuiz));
        when(quizQuestionRepository.findByQuizIdOrderBySequenceOrderAsc(1L)).thenReturn(List.of(q1));
        when(quizOptionRepository.findByQuestionIdOrderBySequenceOrderAsc(101L)).thenReturn(List.of(q1Opt1Correct, q1Opt2Wrong));

        QuizDetailDto dto = quizService.getQuizByIdOrSlug("quiz-earthquake");

        assertThat(dto).isNotNull();
        assertThat(dto.getSlug()).isEqualTo("quiz-earthquake");
        assertThat(dto.getQuestions()).hasSize(1);

        QuizQuestionDto questionDto = dto.getQuestions().get(0);
        assertThat(questionDto.getQuestionText()).isEqualTo("What should you do during shaking?");
        assertThat(questionDto.getOptions()).hasSize(2);

        // Verify QuizOptionDto only has id, optionText, sequenceOrder - no isCorrect!
        QuizOptionDto option1 = questionDto.getOptions().get(0);
        assertThat(option1.getId()).isEqualTo(1001L);
        assertThat(option1.getOptionText()).isEqualTo("Drop, Cover, and Hold On");
    }

    @Test
    @DisplayName("Scoring: 100% correct answers passes threshold")
    void testSubmitQuiz100Percent() {
        when(userRepository.findByEmail("priya@school.edu")).thenReturn(Optional.of(student));
        when(quizRepository.findBySlug("quiz-earthquake")).thenReturn(Optional.of(testQuiz));
        when(quizQuestionRepository.findByQuizIdOrderBySequenceOrderAsc(1L)).thenReturn(List.of(q1, q2));
        when(quizOptionRepository.findByQuestionIdOrderBySequenceOrderAsc(101L)).thenReturn(List.of(q1Opt1Correct, q1Opt2Wrong));
        when(quizOptionRepository.findByQuestionIdOrderBySequenceOrderAsc(102L)).thenReturn(List.of(q2Opt1Correct, q2Opt2Wrong));
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> {
            QuizAttempt a = inv.getArgument(0);
            a.setId(901L);
            return a;
        });

        QuizSubmissionRequestDto submission = new QuizSubmissionRequestDto(List.of(
                new QuestionAnswerDto(101L, 1001L), // correct
                new QuestionAnswerDto(102L, 2001L)  // correct
        ));

        QuizResultDto result = quizService.submitQuiz("quiz-earthquake", submission, "priya@school.edu");

        assertThat(result.getScorePercent()).isEqualTo(100);
        assertThat(result.getTotalQuestions()).isEqualTo(2);
        assertThat(result.getCorrectAnswers()).isEqualTo(2);
        assertThat(result.getPassed()).isTrue();
        assertThat(result.getQuestionResults().get(0).getIsCorrect()).isTrue();
        assertThat(result.getQuestionResults().get(0).getExplanation()).isNotEmpty();
    }

    @Test
    @DisplayName("Scoring & Rounding: 2 of 3 correct rounds to 67% (0.666... * 100)")
    void testSubmitQuizFractionalRoundingTwoOfThree() {
        when(userRepository.findByEmail("priya@school.edu")).thenReturn(Optional.of(student));
        when(quizRepository.findBySlug("quiz-earthquake")).thenReturn(Optional.of(testQuiz));
        when(quizQuestionRepository.findByQuizIdOrderBySequenceOrderAsc(1L)).thenReturn(List.of(q1, q2, q3));
        when(quizOptionRepository.findByQuestionIdOrderBySequenceOrderAsc(101L)).thenReturn(List.of(q1Opt1Correct, q1Opt2Wrong));
        when(quizOptionRepository.findByQuestionIdOrderBySequenceOrderAsc(102L)).thenReturn(List.of(q2Opt1Correct, q2Opt2Wrong));
        when(quizOptionRepository.findByQuestionIdOrderBySequenceOrderAsc(103L)).thenReturn(List.of(q3Opt1Correct, q3Opt2Wrong));
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        // 2 correct, 1 wrong
        QuizSubmissionRequestDto submission = new QuizSubmissionRequestDto(List.of(
                new QuestionAnswerDto(101L, 1001L), // correct
                new QuestionAnswerDto(102L, 2001L), // correct
                new QuestionAnswerDto(103L, 3002L)  // wrong
        ));

        QuizResultDto result = quizService.submitQuiz("quiz-earthquake", submission, "priya@school.edu");

        // 2/3 = 0.6666... * 100 -> Math.round = 67
        assertThat(result.getScorePercent()).isEqualTo(67);
        assertThat(result.getCorrectAnswers()).isEqualTo(2);
        assertThat(result.getTotalQuestions()).isEqualTo(3);
        // Passing threshold is 70; 67 < 70 -> passed = false
        assertThat(result.getPassed()).isFalse();
    }

    @Test
    @DisplayName("Scoring & Rounding: 1 of 3 correct rounds to 33% (0.333... * 100)")
    void testSubmitQuizFractionalRoundingOneOfThree() {
        when(userRepository.findByEmail("priya@school.edu")).thenReturn(Optional.of(student));
        when(quizRepository.findBySlug("quiz-earthquake")).thenReturn(Optional.of(testQuiz));
        when(quizQuestionRepository.findByQuizIdOrderBySequenceOrderAsc(1L)).thenReturn(List.of(q1, q2, q3));
        when(quizOptionRepository.findByQuestionIdOrderBySequenceOrderAsc(101L)).thenReturn(List.of(q1Opt1Correct, q1Opt2Wrong));
        when(quizOptionRepository.findByQuestionIdOrderBySequenceOrderAsc(102L)).thenReturn(List.of(q2Opt1Correct, q2Opt2Wrong));
        when(quizOptionRepository.findByQuestionIdOrderBySequenceOrderAsc(103L)).thenReturn(List.of(q3Opt1Correct, q3Opt2Wrong));
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        QuizSubmissionRequestDto submission = new QuizSubmissionRequestDto(List.of(
                new QuestionAnswerDto(101L, 1001L), // correct
                new QuestionAnswerDto(102L, 2002L), // wrong
                new QuestionAnswerDto(103L, 3002L)  // wrong
        ));

        QuizResultDto result = quizService.submitQuiz("quiz-earthquake", submission, "priya@school.edu");

        // 1/3 = 0.3333... * 100 -> Math.round = 33
        assertThat(result.getScorePercent()).isEqualTo(33);
        assertThat(result.getCorrectAnswers()).isEqualTo(1);
        assertThat(result.getPassed()).isFalse();
    }

    @Test
    @DisplayName("Pass Threshold: Exact match with passing threshold passes (e.g., 75% on 75% passing threshold)")
    void testSubmitQuizExactPassingThresholdBoundary() {
        testQuiz.setPassingScorePercent(50);

        when(userRepository.findByEmail("priya@school.edu")).thenReturn(Optional.of(student));
        when(quizRepository.findBySlug("quiz-earthquake")).thenReturn(Optional.of(testQuiz));
        when(quizQuestionRepository.findByQuizIdOrderBySequenceOrderAsc(1L)).thenReturn(List.of(q1, q2));
        when(quizOptionRepository.findByQuestionIdOrderBySequenceOrderAsc(101L)).thenReturn(List.of(q1Opt1Correct, q1Opt2Wrong));
        when(quizOptionRepository.findByQuestionIdOrderBySequenceOrderAsc(102L)).thenReturn(List.of(q2Opt1Correct, q2Opt2Wrong));
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        // 1 of 2 correct = 50%
        QuizSubmissionRequestDto submission = new QuizSubmissionRequestDto(List.of(
                new QuestionAnswerDto(101L, 1001L),
                new QuestionAnswerDto(102L, 2002L)
        ));

        QuizResultDto result = quizService.submitQuiz("quiz-earthquake", submission, "priya@school.edu");

        assertThat(result.getScorePercent()).isEqualTo(50);
        assertThat(result.getPassed()).isTrue(); // 50 >= 50
    }

    @Test
    @DisplayName("Duplicate Answers: Multiple answers for same question are deduplicated without crashing")
    void testSubmitQuizDuplicateAnswersDeduplication() {
        when(userRepository.findByEmail("priya@school.edu")).thenReturn(Optional.of(student));
        when(quizRepository.findBySlug("quiz-earthquake")).thenReturn(Optional.of(testQuiz));
        when(quizQuestionRepository.findByQuizIdOrderBySequenceOrderAsc(1L)).thenReturn(List.of(q1));
        when(quizOptionRepository.findByQuestionIdOrderBySequenceOrderAsc(101L)).thenReturn(List.of(q1Opt1Correct, q1Opt2Wrong));
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        // Learner submits question 101 twice: first with wrong option, then corrected to right option
        QuizSubmissionRequestDto submission = new QuizSubmissionRequestDto(List.of(
                new QuestionAnswerDto(101L, 1002L), // first wrong
                new QuestionAnswerDto(101L, 1001L)  // then corrected
        ));

        QuizResultDto result = quizService.submitQuiz("quiz-earthquake", submission, "priya@school.edu");

        // Last submitted answer overrides in Map
        assertThat(result.getScorePercent()).isEqualTo(100);
        assertThat(result.getCorrectAnswers()).isEqualTo(1);
    }

    @Test
    @DisplayName("Incomplete Submission: Missing questions treated as incorrect")
    void testSubmitQuizIncompleteSubmission() {
        when(userRepository.findByEmail("priya@school.edu")).thenReturn(Optional.of(student));
        when(quizRepository.findBySlug("quiz-earthquake")).thenReturn(Optional.of(testQuiz));
        when(quizQuestionRepository.findByQuizIdOrderBySequenceOrderAsc(1L)).thenReturn(List.of(q1, q2));
        when(quizOptionRepository.findByQuestionIdOrderBySequenceOrderAsc(101L)).thenReturn(List.of(q1Opt1Correct, q1Opt2Wrong));
        when(quizOptionRepository.findByQuestionIdOrderBySequenceOrderAsc(102L)).thenReturn(List.of(q2Opt1Correct, q2Opt2Wrong));
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        // Learner only answers question 1; leaves question 2 blank
        QuizSubmissionRequestDto submission = new QuizSubmissionRequestDto(List.of(
                new QuestionAnswerDto(101L, 1001L)
        ));

        QuizResultDto result = quizService.submitQuiz("quiz-earthquake", submission, "priya@school.edu");

        assertThat(result.getTotalQuestions()).isEqualTo(2);
        assertThat(result.getCorrectAnswers()).isEqualTo(1);
        assertThat(result.getScorePercent()).isEqualTo(50);
        assertThat(result.getQuestionResults().get(1).getSelectedOptionId()).isNull();
        assertThat(result.getQuestionResults().get(1).getIsCorrect()).isFalse();
    }

    @Test
    @DisplayName("Validation: Quiz with no questions throws IllegalArgumentException")
    void testSubmitQuizWithNoQuestionsThrows() {
        when(userRepository.findByEmail("priya@school.edu")).thenReturn(Optional.of(student));
        when(quizRepository.findBySlug("quiz-empty")).thenReturn(Optional.of(testQuiz));
        when(quizQuestionRepository.findByQuizIdOrderBySequenceOrderAsc(1L)).thenReturn(Collections.emptyList());

        QuizSubmissionRequestDto submission = new QuizSubmissionRequestDto(Collections.emptyList());

        assertThatThrownBy(() -> quizService.submitQuiz("quiz-empty", submission, "priya@school.edu"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot submit quiz with no questions");
    }

    @Test
    @DisplayName("Validation: Non-existent quiz throws ResourceNotFoundException")
    void testSubmitQuizNotFoundThrows() {
        when(userRepository.findByEmail("priya@school.edu")).thenReturn(Optional.of(student));
        when(quizRepository.findBySlug("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.submitQuiz("non-existent", new QuizSubmissionRequestDto(List.of()), "priya@school.edu"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("non-existent");
    }
}
