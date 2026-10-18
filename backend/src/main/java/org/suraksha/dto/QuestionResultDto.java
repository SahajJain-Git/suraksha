package org.suraksha.dto;

import java.util.ArrayList;
import java.util.List;

public class QuestionResultDto {
    private Long questionId;
    private Integer sequenceOrder;
    private String questionText;
    private Long selectedOptionId;
    private Long correctOptionId;
    private Boolean isCorrect;
    private String explanation;
    private List<QuizOptionResultDto> options = new ArrayList<>();

    public QuestionResultDto() {}

    public QuestionResultDto(Long questionId, Integer sequenceOrder, String questionText,
                             Long selectedOptionId, Long correctOptionId, Boolean isCorrect,
                             String explanation, List<QuizOptionResultDto> options) {
        this.questionId = questionId;
        this.sequenceOrder = sequenceOrder;
        this.questionText = questionText;
        this.selectedOptionId = selectedOptionId;
        this.correctOptionId = correctOptionId;
        this.isCorrect = isCorrect;
        this.explanation = explanation;
        this.options = options != null ? options : new ArrayList<>();
    }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public Long getSelectedOptionId() { return selectedOptionId; }
    public void setSelectedOptionId(Long selectedOptionId) { this.selectedOptionId = selectedOptionId; }

    public Long getCorrectOptionId() { return correctOptionId; }
    public void setCorrectOptionId(Long correctOptionId) { this.correctOptionId = correctOptionId; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public List<QuizOptionResultDto> getOptions() { return options; }
    public void setOptions(List<QuizOptionResultDto> options) { this.options = options; }
}
