package org.suraksha.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "quiz_attempts",
    indexes = {
        @Index(name = "idx_user_quiz_attempts", columnList = "user_id, quiz_id")
    }
)
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "score_percent", nullable = false)
    private Integer scorePercent;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers;

    @Column(nullable = false)
    private Boolean passed;

    @Column(name = "attempted_at")
    private LocalDateTime attemptedAt;

    public QuizAttempt() {}

    public QuizAttempt(User user, Quiz quiz, Integer scorePercent, Integer totalQuestions, Integer correctAnswers, Boolean passed) {
        this.user = user;
        this.quiz = quiz;
        this.scorePercent = scorePercent;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.passed = passed;
    }

    @PrePersist
    protected void onCreate() {
        this.attemptedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }

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
