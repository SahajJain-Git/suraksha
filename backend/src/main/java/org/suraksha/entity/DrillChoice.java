package org.suraksha.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "drill_choices")
public class DrillChoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private DrillStep step;

    @Column(name = "choice_text", nullable = false, length = 500)
    private String choiceText;

    @Column(name = "outcome_type", nullable = false, length = 20)
    private String outcomeType; // SAFE, DANGER, NEUTRAL

    @Column(name = "feedback_text", nullable = false, columnDefinition = "TEXT")
    private String feedbackText;

    @Column(name = "score_impact", nullable = false)
    private Integer scoreImpact = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_step_id")
    private DrillStep nextStep;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder = 1;

    public DrillChoice() {}

    public DrillChoice(String choiceText, String outcomeType, String feedbackText, Integer scoreImpact, Integer sequenceOrder) {
        this.choiceText = choiceText;
        this.outcomeType = outcomeType;
        this.feedbackText = feedbackText;
        this.scoreImpact = scoreImpact;
        this.sequenceOrder = sequenceOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DrillStep getStep() { return step; }
    public void setStep(DrillStep step) { this.step = step; }

    public String getChoiceText() { return choiceText; }
    public void setChoiceText(String choiceText) { this.choiceText = choiceText; }

    public String getOutcomeType() { return outcomeType; }
    public void setOutcomeType(String outcomeType) { this.outcomeType = outcomeType; }

    public String getFeedbackText() { return feedbackText; }
    public void setFeedbackText(String feedbackText) { this.feedbackText = feedbackText; }

    public Integer getScoreImpact() { return scoreImpact; }
    public void setScoreImpact(Integer scoreImpact) { this.scoreImpact = scoreImpact; }

    public DrillStep getNextStep() { return nextStep; }
    public void setNextStep(DrillStep nextStep) { this.nextStep = nextStep; }

    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }
}
