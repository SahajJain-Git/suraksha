package org.suraksha.dto;

import java.time.LocalDateTime;

public class QuizAttemptSummaryDto {
    private Long id;
    private Long quizId;
    private String quizSlug;
    private String quizTitle;
    private Integer scorePercent;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Boolean passed;
    private LocalDateTime attemptedAt;

    public QuizAttemptSummaryDto() {}

    public QuizAttemptSummaryDto(Long id, Long quizId, String quizSlug, String quizTitle,
                                 Integer scorePercent, Integer totalQuestions,
                                 Integer correctAnswers, Boolean passed, LocalDateTime attemptedAt) {
        this.id = id;
        this.quizId = quizId;
        this.quizSlug = quizSlug;
        this.quizTitle = quizTitle;
        this.scorePercent = scorePercent;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.passed = passed;
        this.attemptedAt = attemptedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public String getQuizSlug() { return quizSlug; }
    public void setQuizSlug(String quizSlug) { this.quizSlug = quizSlug; }

    public String getQuizTitle() { return quizTitle; }
    public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }

    public Integer getScorePercent() { return scorePercent; }
    public void setScorePercent(Integer scorePercent) { this.scorePercent = scorePercent; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public Integer getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(Integer correctAnswers) { this.correctAnswers = correctAnswers; }

    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }

    public LocalDateTime getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(LocalDateTime attemptedAt) { this.attemptedAt = attemptedAt; }
}
