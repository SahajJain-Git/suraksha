package org.suraksha.dto;

import java.util.ArrayList;
import java.util.List;

public class DrillScenarioDto {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String slug;
    private String title;
    private String hazardCategory;
    private String description;
    private Long initialStepId;
    private List<DrillStepDto> steps = new ArrayList<>();

    public DrillScenarioDto() {}

    public DrillScenarioDto(Long id, Long courseId, String courseTitle, String slug,
                            String title, String hazardCategory, String description,
                            Long initialStepId, List<DrillStepDto> steps) {
        this.id = id;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.slug = slug;
        this.title = title;
        this.hazardCategory = hazardCategory;
        this.description = description;
        this.initialStepId = initialStepId;
        this.steps = steps != null ? steps : new ArrayList<>();
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

    public String getHazardCategory() { return hazardCategory; }
    public void setHazardCategory(String hazardCategory) { this.hazardCategory = hazardCategory; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getInitialStepId() { return initialStepId; }
    public void setInitialStepId(Long initialStepId) { this.initialStepId = initialStepId; }

    public List<DrillStepDto> getSteps() { return steps; }
    public void setSteps(List<DrillStepDto> steps) { this.steps = steps; }
}
