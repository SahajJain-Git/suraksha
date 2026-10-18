package org.suraksha.dto;

public class DrillChoiceFeedbackDto {
    private Long choiceId;
    private String choiceText;
    private String outcomeType;
    private String feedbackText;
    private Integer scoreImpact;

    public DrillChoiceFeedbackDto() {}

    public DrillChoiceFeedbackDto(Long choiceId, String choiceText, String outcomeType,
                                  String feedbackText, Integer scoreImpact) {
        this.choiceId = choiceId;
        this.choiceText = choiceText;
        this.outcomeType = outcomeType;
        this.feedbackText = feedbackText;
        this.scoreImpact = scoreImpact;
    }

    public Long getChoiceId() { return choiceId; }
    public void setChoiceId(Long choiceId) { this.choiceId = choiceId; }

    public String getChoiceText() { return choiceText; }
    public void setChoiceText(String choiceText) { this.choiceText = choiceText; }

    public String getOutcomeType() { return outcomeType; }
    public void setOutcomeType(String outcomeType) { this.outcomeType = outcomeType; }

    public String getFeedbackText() { return feedbackText; }
    public void setFeedbackText(String feedbackText) { this.feedbackText = feedbackText; }

    public Integer getScoreImpact() { return scoreImpact; }
    public void setScoreImpact(Integer scoreImpact) { this.scoreImpact = scoreImpact; }
}
