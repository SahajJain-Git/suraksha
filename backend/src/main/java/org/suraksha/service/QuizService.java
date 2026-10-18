package org.suraksha.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.suraksha.dto.*;
import org.suraksha.entity.*;
import org.suraksha.exception.ResourceNotFoundException;
import org.suraksha.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;

    public QuizService(QuizRepository quizRepository,
                       QuizQuestionRepository quizQuestionRepository,
                       QuizOptionRepository quizOptionRepository,
                       QuizAttemptRepository quizAttemptRepository,
                       UserRepository userRepository) {
        this.quizRepository = quizRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.quizOptionRepository = quizOptionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves quiz metadata and questions stripped of correct answers and explanations.
     * Accessible publicly for students preparing to take a quiz.
     */
    public QuizDetailDto getQuizByIdOrSlug(String idOrSlug) {
        Quiz quiz = findQuizEntity(idOrSlug);
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderBySequenceOrderAsc(quiz.getId());

        List<QuizQuestionDto> questionDtos = questions.stream().map(q -> {
            List<QuizOption> options = quizOptionRepository.findByQuestionIdOrderBySequenceOrderAsc(q.getId());
            List<QuizOptionDto> optionDtos = options.stream()
                    .map(opt -> new QuizOptionDto(opt.getId(), opt.getOptionText(), opt.getSequenceOrder()))
                    .collect(Collectors.toList());

            // STRICT INTEGRITY: explanation is NOT included here
            return new QuizQuestionDto(q.getId(), q.getSequenceOrder(), q.getQuestionText(), optionDtos);
        }).collect(Collectors.toList());

        String courseTitle = (quiz.getCourse() != null) ? quiz.getCourse().getTitle() : null;
        Long courseId = (quiz.getCourse() != null) ? quiz.getCourse().getId() : null;

        return new QuizDetailDto(
                quiz.getId(),
                courseId,
                courseTitle,
                quiz.getSlug(),
                quiz.getTitle(),
                quiz.getPassingScorePercent(),
                questionDtos
        );
    }

    /**
     * Submits and evaluates a student's quiz answers server-side.
     * Calculates score, evaluates pass/fail against passing threshold, persists a QuizAttempt,
     * and returns educational debrief with correct answers and explanations.
     */
    @Transactional
    public QuizResultDto submitQuiz(String idOrSlug, QuizSubmissionRequestDto submission, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Quiz quiz = findQuizEntity(idOrSlug);
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderBySequenceOrderAsc(quiz.getId());

        if (questions.isEmpty()) {
            throw new IllegalArgumentException("Cannot submit quiz with no questions.");
        }

        // Map submitted answers for O(1) lookup: questionId -> selectedOptionId
        Map<Long, Long> submittedAnswers = new HashMap<>();
        if (submission != null && submission.getAnswers() != null) {
            for (QuestionAnswerDto answer : submission.getAnswers()) {
                if (answer.getQuestionId() != null && answer.getSelectedOptionId() != null) {
                    submittedAnswers.put(answer.getQuestionId(), answer.getSelectedOptionId());
                }
            }
        }

        int totalQuestions = questions.size();
        int correctCount = 0;
        List<QuestionResultDto> questionResults = new ArrayList<>();

        for (QuizQuestion question : questions) {
            List<QuizOption> options = quizOptionRepository.findByQuestionIdOrderBySequenceOrderAsc(question.getId());
            Long correctOptionId = null;

            for (QuizOption opt : options) {
                if (Boolean.TRUE.equals(opt.getIsCorrect())) {
                    correctOptionId = opt.getId();
                    break;
                }
            }

            Long selectedOptionId = submittedAnswers.get(question.getId());
            boolean isQuestionCorrect = (selectedOptionId != null && selectedOptionId.equals(correctOptionId));
            if (isQuestionCorrect) {
                correctCount++;
            }

            // In post-submission review, revealing isCorrect is legitimate educational feedback
            List<QuizOptionResultDto> optionResultDtos = options.stream()
                    .map(opt -> new QuizOptionResultDto(opt.getId(), opt.getOptionText(), opt.getIsCorrect(), opt.getSequenceOrder()))
                    .collect(Collectors.toList());

            questionResults.add(new QuestionResultDto(
                    question.getId(),
                    question.getSequenceOrder(),
                    question.getQuestionText(),
                    selectedOptionId,
                    correctOptionId,
                    isQuestionCorrect,
                    question.getExplanation(),
                    optionResultDtos
            ));
        }

        int scorePercent = (int) Math.round(((double) correctCount / totalQuestions) * 100);
        boolean passed = scorePercent >= quiz.getPassingScorePercent();

        QuizAttempt attempt = new QuizAttempt(user, quiz, scorePercent, totalQuestions, correctCount, passed);
        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        return new QuizResultDto(
                savedAttempt.getId(),
                quiz.getId(),
                quiz.getSlug(),
                quiz.getTitle(),
                scorePercent,
                quiz.getPassingScorePercent(),
                totalQuestions,
                correctCount,
                passed,
                savedAttempt.getAttemptedAt(),
                questionResults
        );
    }

    private Quiz findQuizEntity(String idOrSlug) {
        if (idOrSlug.matches("\\d+")) {
            Long id = Long.parseLong(idOrSlug);
            return quizRepository.findById(id)
                    .orElseGet(() -> quizRepository.findBySlug(idOrSlug)
                            .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with identifier: " + idOrSlug)));
        }
        return quizRepository.findBySlug(idOrSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with identifier: " + idOrSlug));
    }
}
