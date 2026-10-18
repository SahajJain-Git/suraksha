package org.suraksha.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuizResultDto {
    private Long attemptId;
    private Long quizId;
    private String quizSlug;
    private String quizTitle;
    private Integer scorePercent;
    private Integer passingScorePercent;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Boolean passed;
    private LocalDateTime attemptedAt;
    private List<QuestionResultDto> questionResults = new ArrayList<>();

    public QuizResultDto() {}

    public QuizResultDto(Long attemptId, Long quizId, String quizSlug, String quizTitle,
                         Integer scorePercent, Integer passingScorePercent,
                         Integer totalQuestions, Integer correctAnswers, Boolean passed,
                         LocalDateTime attemptedAt, List<QuestionResultDto> questionResults) {
        this.attemptId = attemptId;
        this.quizId = quizId;
        this.quizSlug = quizSlug;
        this.quizTitle = quizTitle;
        this.scorePercent = scorePercent;
        this.passingScorePercent = passingScorePercent;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.passed = passed;
        this.attemptedAt = attemptedAt;
        this.questionResults = questionResults != null ? questionResults : new ArrayList<>();
    }

    public Long getAttemptId() { return attemptId; }
    public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public String getQuizSlug() { return quizSlug; }
    public void setQuizSlug(String quizSlug) { this.quizSlug = quizSlug; }

    public String getQuizTitle() { return quizTitle; }
    public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }

    public Integer getScorePercent() { return scorePercent; }
    public void setScorePercent(Integer scorePercent) { this.scorePercent = scorePercent; }

    public Integer getPassingScorePercent() { return passingScorePercent; }
    public void setPassingScorePercent(Integer passingScorePercent) { this.passingScorePercent = passingScorePercent; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public Integer getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(Integer correctAnswers) { this.correctAnswers = correctAnswers; }

    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }

    public LocalDateTime getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(LocalDateTime attemptedAt) { this.attemptedAt = attemptedAt; }

    public List<QuestionResultDto> getQuestionResults() { return questionResults; }
    public void setQuestionResults(List<QuestionResultDto> questionResults) { this.questionResults = questionResults; }
}
