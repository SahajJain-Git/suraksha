package org.suraksha.dto;

public class CourseProgressSummaryDto {
    private Long courseId;
    private String courseSlug;
    private String courseTitle;
    private String category;
    private Integer totalLessons;
    private Integer completedLessons;
    private Boolean isCompleted;
    private Integer progressPercent;

    public CourseProgressSummaryDto() {}

    public CourseProgressSummaryDto(Long courseId, String courseSlug, String courseTitle,
                                    String category, Integer totalLessons, Integer completedLessons,
                                    Boolean isCompleted, Integer progressPercent) {
        this.courseId = courseId;
        this.courseSlug = courseSlug;
        this.courseTitle = courseTitle;
        this.category = category;
        this.totalLessons = totalLessons;
        this.completedLessons = completedLessons;
        this.isCompleted = isCompleted;
        this.progressPercent = progressPercent;
    }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseSlug() { return courseSlug; }
    public void setCourseSlug(String courseSlug) { this.courseSlug = courseSlug; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getTotalLessons() { return totalLessons; }
    public void setTotalLessons(Integer totalLessons) { this.totalLessons = totalLessons; }

    public Integer getCompletedLessons() { return completedLessons; }
    public void setCompletedLessons(Integer completedLessons) { this.completedLessons = completedLessons; }

    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }

    public Integer getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }
}
