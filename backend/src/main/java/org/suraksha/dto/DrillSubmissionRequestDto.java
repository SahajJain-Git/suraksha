package org.suraksha.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class DrillSubmissionRequestDto {

    @NotEmpty(message = "Selected choices list cannot be empty")
    private List<Long> selectedChoiceIds = new ArrayList<>();

    public DrillSubmissionRequestDto() {}

    public DrillSubmissionRequestDto(List<Long> selectedChoiceIds) {
        this.selectedChoiceIds = selectedChoiceIds != null ? selectedChoiceIds : new ArrayList<>();
    }

    public List<Long> getSelectedChoiceIds() { return selectedChoiceIds; }
    public void setSelectedChoiceIds(List<Long> selectedChoiceIds) { this.selectedChoiceIds = selectedChoiceIds; }
}
