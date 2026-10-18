package org.suraksha.dto;

import java.time.LocalDateTime;

public class LessonProgressResponseDto {
    private Long lessonId;
    private String lessonSlug;
    private String lessonTitle;
    private Long courseId;
    private String courseSlug;
    private String courseTitle;
    private Boolean completed;
    private LocalDateTime completedAt;

    public LessonProgressResponseDto() {}

    public LessonProgressResponseDto(Long lessonId, String lessonSlug, String lessonTitle,
                                     Long courseId, String courseSlug, String courseTitle,
                                     Boolean completed, LocalDateTime completedAt) {
        this.lessonId = lessonId;
        this.lessonSlug = lessonSlug;
        this.lessonTitle = lessonTitle;
        this.courseId = courseId;
        this.courseSlug = courseSlug;
        this.courseTitle = courseTitle;
        this.completed = completed;
        this.completedAt = completedAt;
    }

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

    public String getLessonSlug() { return lessonSlug; }
    public void setLessonSlug(String lessonSlug) { this.lessonSlug = lessonSlug; }

    public String getLessonTitle() { return lessonTitle; }
    public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseSlug() { return courseSlug; }
    public void setCourseSlug(String courseSlug) { this.courseSlug = courseSlug; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
