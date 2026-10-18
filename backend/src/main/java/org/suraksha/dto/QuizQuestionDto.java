package org.suraksha.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * QuizQuestionDto represents a question sent to a student taking the quiz.
 * SECURITY NOTICE: Does NOT contain explanation or correct answer flags.
 */
public class QuizQuestionDto {
    private Long id;
    private Integer sequenceOrder;
    private String questionText;
    private List<QuizOptionDto> options = new ArrayList<>();

    public QuizQuestionDto() {}

    public QuizQuestionDto(Long id, Integer sequenceOrder, String questionText, List<QuizOptionDto> options) {
        this.id = id;
        this.sequenceOrder = sequenceOrder;
        this.questionText = questionText;
        this.options = options != null ? options : new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public List<QuizOptionDto> getOptions() { return options; }
    public void setOptions(List<QuizOptionDto> options) { this.options = options; }
}
