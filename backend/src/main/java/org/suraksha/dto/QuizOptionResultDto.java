package org.suraksha.dto;

public class QuizOptionResultDto {
    private Long id;
    private String optionText;
    private Boolean isCorrect;
    private Integer sequenceOrder;

    public QuizOptionResultDto() {}

    public QuizOptionResultDto(Long id, String optionText, Boolean isCorrect, Integer sequenceOrder) {
        this.id = id;
        this.optionText = optionText;
        this.isCorrect = isCorrect;
        this.sequenceOrder = sequenceOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }
}
