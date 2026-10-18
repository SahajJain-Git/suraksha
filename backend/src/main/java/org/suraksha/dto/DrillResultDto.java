package org.suraksha.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DrillResultDto {
    private Long attemptId;
    private Long scenarioId;
    private String scenarioSlug;
    private String scenarioTitle;
    private Integer totalScore;
    private String ratingLabel;
    private LocalDateTime completedAt;
    private List<DrillChoiceFeedbackDto> choiceFeedbacks = new ArrayList<>();

    public DrillResultDto() {}

    public DrillResultDto(Long attemptId, Long scenarioId, String scenarioSlug, String scenarioTitle,
                          Integer totalScore, String ratingLabel, LocalDateTime completedAt,
                          List<DrillChoiceFeedbackDto> choiceFeedbacks) {
        this.attemptId = attemptId;
        this.scenarioId = scenarioId;
        this.scenarioSlug = scenarioSlug;
        this.scenarioTitle = scenarioTitle;
        this.totalScore = totalScore;
        this.ratingLabel = ratingLabel;
        this.completedAt = completedAt;
        this.choiceFeedbacks = choiceFeedbacks != null ? choiceFeedbacks : new ArrayList<>();
    }

    public Long getAttemptId() { return attemptId; }
    public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }

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

    public List<DrillChoiceFeedbackDto> getChoiceFeedbacks() { return choiceFeedbacks; }
    public void setChoiceFeedbacks(List<DrillChoiceFeedbackDto> choiceFeedbacks) { this.choiceFeedbacks = choiceFeedbacks; }
}
