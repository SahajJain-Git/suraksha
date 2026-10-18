package org.suraksha.dto;

public class DrillChoiceDto {
    private Long id;
    private String choiceText;
    private String outcomeType;
    private String feedbackText;
    private Integer scoreImpact;
    private Long nextStepId;
    private Integer sequenceOrder;

    public DrillChoiceDto() {}

    public DrillChoiceDto(Long id, String choiceText, String outcomeType, String feedbackText,
                          Integer scoreImpact, Long nextStepId, Integer sequenceOrder) {
        this.id = id;
        this.choiceText = choiceText;
        this.outcomeType = outcomeType;
        this.feedbackText = feedbackText;
        this.scoreImpact = scoreImpact;
        this.nextStepId = nextStepId;
        this.sequenceOrder = sequenceOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getChoiceText() { return choiceText; }
    public void setChoiceText(String choiceText) { this.choiceText = choiceText; }

    public String getOutcomeType() { return outcomeType; }
    public void setOutcomeType(String outcomeType) { this.outcomeType = outcomeType; }

    public String getFeedbackText() { return feedbackText; }
    public void setFeedbackText(String feedbackText) { this.feedbackText = feedbackText; }

    public Integer getScoreImpact() { return scoreImpact; }
    public void setScoreImpact(Integer scoreImpact) { this.scoreImpact = scoreImpact; }

    public Long getNextStepId() { return nextStepId; }
    public void setNextStepId(Long nextStepId) { this.nextStepId = nextStepId; }

    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }
}
