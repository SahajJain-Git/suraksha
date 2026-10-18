package org.suraksha.dto;

import java.util.ArrayList;
import java.util.List;

public class CourseDetailDto {
    private Long id;
    private String slug;
    private String title;
    private String category;
    private String difficulty;
    private Integer estimatedMinutes;
    private String summary;
    private Long quizId;
    private String quizSlug;
    private Long drillId;
    private String drillSlug;
    private List<LessonDetailDto> lessons = new ArrayList<>();

    public CourseDetailDto() {}

    public CourseDetailDto(Long id, String slug, String title, String category,
                           String difficulty, Integer estimatedMinutes, String summary,
                           List<LessonDetailDto> lessons) {
        this.id = id;
        this.slug = slug;
        this.title = title;
        this.category = category;
        this.difficulty = difficulty;
        this.estimatedMinutes = estimatedMinutes;
        this.summary = summary;
        this.lessons = lessons != null ? lessons : new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public String getQuizSlug() { return quizSlug; }
    public void setQuizSlug(String quizSlug) { this.quizSlug = quizSlug; }

    public Long getDrillId() { return drillId; }
    public void setDrillId(Long drillId) { this.drillId = drillId; }

    public String getDrillSlug() { return drillSlug; }
    public void setDrillSlug(String drillSlug) { this.drillSlug = drillSlug; }

    public List<LessonDetailDto> getLessons() { return lessons; }
    public void setLessons(List<LessonDetailDto> lessons) { this.lessons = lessons; }
}
