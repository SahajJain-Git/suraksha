package org.suraksha.dto;

import jakarta.validation.constraints.NotNull;

public class QuestionAnswerDto {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotNull(message = "Selected option ID is required")
    private Long selectedOptionId;

    public QuestionAnswerDto() {}

    public QuestionAnswerDto(Long questionId, Long selectedOptionId) {
        this.questionId = questionId;
        this.selectedOptionId = selectedOptionId;
    }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public Long getSelectedOptionId() { return selectedOptionId; }
    public void setSelectedOptionId(Long selectedOptionId) { this.selectedOptionId = selectedOptionId; }
}
