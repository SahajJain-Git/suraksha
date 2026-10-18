package org.suraksha.dto;

import java.util.ArrayList;
import java.util.List;

public class DrillStepDto {
    private Long id;
    private String stepKey;
    private String situationText;
    private String questionText;
    private Boolean isTerminal;
    private List<DrillChoiceDto> choices = new ArrayList<>();

    public DrillStepDto() {}

    public DrillStepDto(Long id, String stepKey, String situationText, String questionText,
                        Boolean isTerminal, List<DrillChoiceDto> choices) {
        this.id = id;
        this.stepKey = stepKey;
        this.situationText = situationText;
        this.questionText = questionText;
        this.isTerminal = isTerminal;
        this.choices = choices != null ? choices : new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStepKey() { return stepKey; }
    public void setStepKey(String stepKey) { this.stepKey = stepKey; }

    public String getSituationText() { return situationText; }
    public void setSituationText(String situationText) { this.situationText = situationText; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public Boolean getIsTerminal() { return isTerminal; }
    public void setIsTerminal(Boolean isTerminal) { this.isTerminal = isTerminal; }

    public List<DrillChoiceDto> getChoices() { return choices; }
    public void setChoices(List<DrillChoiceDto> choices) { this.choices = choices; }
}
