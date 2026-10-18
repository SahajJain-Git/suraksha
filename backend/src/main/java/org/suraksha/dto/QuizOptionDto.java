package org.suraksha.dto;

/**
 * QuizOptionDto represents an answer choice sent to students taking the quiz.
 * SECURITY NOTICE: Does NOT leak isCorrect or correctness indicators.
 */
public class QuizOptionDto {
    private Long id;
    private String optionText;
    private Integer sequenceOrder;

    public QuizOptionDto() {}

    public QuizOptionDto(Long id, String optionText, Integer sequenceOrder) {
        this.id = id;
        this.optionText = optionText;
        this.sequenceOrder = sequenceOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }

    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }
}
