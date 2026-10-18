package org.suraksha.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "drill_scenarios")
public class DrillScenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "hazard_category", nullable = false, length = 50)
    private String hazardCategory;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initial_step_id")
    private DrillStep initialStep;

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DrillStep> steps = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public DrillScenario() {}

    public DrillScenario(String slug, String title, String hazardCategory, String description) {
        this.slug = slug;
        this.title = title;
        this.hazardCategory = hazardCategory;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getHazardCategory() { return hazardCategory; }
    public void setHazardCategory(String hazardCategory) { this.hazardCategory = hazardCategory; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DrillStep getInitialStep() { return initialStep; }
    public void setInitialStep(DrillStep initialStep) { this.initialStep = initialStep; }

    public List<DrillStep> getSteps() { return steps; }
    public void setSteps(List<DrillStep> steps) { this.steps = steps; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void addStep(DrillStep step) {
        steps.add(step);
        step.setScenario(this);
    }
}
