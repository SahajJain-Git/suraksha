package org.suraksha.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * QuizDetailDto returned for GET /api/quizzes/{id}.
 * SECURITY NOTICE: Stripped of correct answer indicators and explanations.
 */
public class QuizDetailDto {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String slug;
    private String title;
    private Integer passingScorePercent;
    private Integer totalQuestions;
    private List<QuizQuestionDto> questions = new ArrayList<>();

    public QuizDetailDto() {}

    public QuizDetailDto(Long id, Long courseId, String courseTitle, String slug,
                         String title, Integer passingScorePercent, List<QuizQuestionDto> questions) {
        this.id = id;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.slug = slug;
        this.title = title;
        this.passingScorePercent = passingScorePercent;
        this.questions = questions != null ? questions : new ArrayList<>();
        this.totalQuestions = this.questions.size();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getPassingScorePercent() { return passingScorePercent; }
    public void setPassingScorePercent(Integer passingScorePercent) { this.passingScorePercent = passingScorePercent; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public List<QuizQuestionDto> getQuestions() { return questions; }
    public void setQuestions(List<QuizQuestionDto> questions) {
        this.questions = questions;
        this.totalQuestions = (questions != null) ? questions.size() : 0;
    }
}
