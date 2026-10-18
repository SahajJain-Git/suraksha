package org.suraksha.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_lesson_progress",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_lesson_progress", columnNames = {"user_id", "lesson_id"})
    }
)
public class UserLessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false)
    private Boolean completed = true;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public UserLessonProgress() {}

    public UserLessonProgress(User user, Lesson lesson, Boolean completed) {
        this.user = user;
        this.lesson = lesson;
        this.completed = completed;
    }

    @PrePersist
    protected void onCreate() {
        this.completedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Lesson getLesson() { return lesson; }
    public void setLesson(Lesson lesson) { this.lesson = lesson; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
