package org.suraksha.dto;

public class CourseSummaryDto {
    private Long id;
    private String slug;
    private String title;
    private String category;
    private String difficulty;
    private Integer estimatedMinutes;
    private String summary;
    private Integer lessonsCount;

    public CourseSummaryDto() {}

    public CourseSummaryDto(Long id, String slug, String title, String category,
                            String difficulty, Integer estimatedMinutes, String summary, Integer lessonsCount) {
        this.id = id;
        this.slug = slug;
        this.title = title;
        this.category = category;
        this.difficulty = difficulty;
        this.estimatedMinutes = estimatedMinutes;
        this.summary = summary;
        this.lessonsCount = lessonsCount;
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

    public Integer getLessonsCount() { return lessonsCount; }
    public void setLessonsCount(Integer lessonsCount) { this.lessonsCount = lessonsCount; }
}
