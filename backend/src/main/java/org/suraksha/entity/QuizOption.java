package org.suraksha.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "quiz_options")
public class QuizOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;

    @Column(name = "option_text", nullable = false, length = 500)
    private String optionText;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect = false;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder = 1;

    public QuizOption() {}

    public QuizOption(String optionText, Boolean isCorrect, Integer sequenceOrder) {
        this.optionText = optionText;
        this.isCorrect = isCorrect;
        this.sequenceOrder = sequenceOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public QuizQuestion getQuestion() { return question; }
    public void setQuestion(QuizQuestion question) { this.question = question; }

    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }
}
