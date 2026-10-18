package org.suraksha.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "drill_steps",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_scenario_step_key", columnNames = {"scenario_id", "step_key"})
    }
)
public class DrillStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    private DrillScenario scenario;

    @Column(name = "step_key", nullable = false, length = 50)
    private String stepKey;

    @Column(name = "situation_text", nullable = false, columnDefinition = "TEXT")
    private String situationText;

    @Column(name = "question_text", length = 500)
    private String questionText;

    @Column(name = "is_terminal", nullable = false)
    private Boolean isTerminal = false;

    @OneToMany(mappedBy = "step", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    private List<DrillChoice> choices = new ArrayList<>();

    public DrillStep() {}

    public DrillStep(String stepKey, String situationText, String questionText, Boolean isTerminal) {
        this.stepKey = stepKey;
        this.situationText = situationText;
        this.questionText = questionText;
        this.isTerminal = isTerminal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DrillScenario getScenario() { return scenario; }
    public void setScenario(DrillScenario scenario) { this.scenario = scenario; }

    public String getStepKey() { return stepKey; }
    public void setStepKey(String stepKey) { this.stepKey = stepKey; }

    public String getSituationText() { return situationText; }
    public void setSituationText(String situationText) { this.situationText = situationText; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public Boolean getIsTerminal() { return isTerminal; }
    public void setIsTerminal(Boolean isTerminal) { this.isTerminal = isTerminal; }

    public List<DrillChoice> getChoices() { return choices; }
    public void setChoices(List<DrillChoice> choices) { this.choices = choices; }

    public void addChoice(DrillChoice choice) {
        choices.add(choice);
        choice.setStep(this);
    }
}
