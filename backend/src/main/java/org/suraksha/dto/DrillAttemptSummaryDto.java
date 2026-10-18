package org.suraksha.dto;

import java.time.LocalDateTime;

public class DrillAttemptSummaryDto {
    private Long id;
    private Long scenarioId;
    private String scenarioSlug;
    private String scenarioTitle;
    private Integer totalScore;
    private String ratingLabel;
    private LocalDateTime completedAt;

    public DrillAttemptSummaryDto() {}

    public DrillAttemptSummaryDto(Long id, Long scenarioId, String scenarioSlug, String scenarioTitle,
                                  Integer totalScore, String ratingLabel, LocalDateTime completedAt) {
        this.id = id;
        this.scenarioId = scenarioId;
        this.scenarioSlug = scenarioSlug;
        this.scenarioTitle = scenarioTitle;
        this.totalScore = totalScore;
        this.ratingLabel = ratingLabel;
        this.completedAt = completedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getScenarioId() { return scenarioId; }
    public void setScenarioId(Long scenarioId) { this.scenarioId = scenarioId; }

    public String getScenarioSlug() { return scenarioSlug; }
    public void setScenarioSlug(String scenarioSlug) { this.scenarioSlug = scenarioSlug; }

    public String getScenarioTitle() { return scenarioTitle; }
    public void setScenarioTitle(String scenarioTitle) { this.scenarioTitle = scenarioTitle; }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }

    public String getRatingLabel() { return ratingLabel; }
    public void setRatingLabel(String ratingLabel) { this.ratingLabel = ratingLabel; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
