package org.suraksha.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class QuizSubmissionRequestDto {

    @NotEmpty(message = "Answers list cannot be empty")
    @Valid
    private List<QuestionAnswerDto> answers = new ArrayList<>();

    public QuizSubmissionRequestDto() {}

    public QuizSubmissionRequestDto(List<QuestionAnswerDto> answers) {
        this.answers = answers != null ? answers : new ArrayList<>();
    }

    public List<QuestionAnswerDto> getAnswers() { return answers; }
    public void setAnswers(List<QuestionAnswerDto> answers) { this.answers = answers; }
}
