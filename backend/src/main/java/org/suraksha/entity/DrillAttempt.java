package org.suraksha.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "drill_attempts")
public class DrillAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    private DrillScenario scenario;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore = 0;

    @Column(name = "rating_label", nullable = false, length = 50)
    private String ratingLabel;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public DrillAttempt() {}

    public DrillAttempt(User user, DrillScenario scenario, Integer totalScore, String ratingLabel) {
        this.user = user;
        this.scenario = scenario;
        this.totalScore = totalScore;
        this.ratingLabel = ratingLabel;
    }

    @PrePersist
    protected void onCreate() {
        this.completedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public DrillScenario getScenario() { return scenario; }
    public void setScenario(DrillScenario scenario) { this.scenario = scenario; }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }

    public String getRatingLabel() { return ratingLabel; }
    public void setRatingLabel(String ratingLabel) { this.ratingLabel = ratingLabel; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
