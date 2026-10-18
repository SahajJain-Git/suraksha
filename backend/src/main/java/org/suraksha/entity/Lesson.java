package org.suraksha.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "lessons",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_course_lesson_order", columnNames = {"course_id", "sequence_order"}),
        @UniqueConstraint(name = "uk_course_lesson_slug", columnNames = {"course_id", "slug"})
    }
)
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 100)
    private String slug;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder = 1;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(name = "dos_guidelines", columnDefinition = "TEXT")
    private String dosGuidelines;

    @Column(name = "donts_guidelines", columnDefinition = "TEXT")
    private String dontsGuidelines;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Lesson() {}

    public Lesson(String slug, String title, Integer sequenceOrder, String content, String dosGuidelines, String dontsGuidelines) {
        this.slug = slug;
        this.title = title;
        this.sequenceOrder = sequenceOrder;
        this.content = content;
        this.dosGuidelines = dosGuidelines;
        this.dontsGuidelines = dontsGuidelines;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDosGuidelines() { return dosGuidelines; }
    public void setDosGuidelines(String dosGuidelines) { this.dosGuidelines = dosGuidelines; }

    public String getDontsGuidelines() { return dontsGuidelines; }
    public void setDontsGuidelines(String dontsGuidelines) { this.dontsGuidelines = dontsGuidelines; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
