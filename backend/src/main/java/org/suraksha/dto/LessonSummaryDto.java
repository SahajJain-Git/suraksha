package org.suraksha.dto;

public class LessonSummaryDto {
    private Long id;
    private String slug;
    private String title;
    private Integer sequenceOrder;

    public LessonSummaryDto() {}

    public LessonSummaryDto(Long id, String slug, String title, Integer sequenceOrder) {
        this.id = id;
        this.slug = slug;
        this.title = title;
        this.sequenceOrder = sequenceOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }
}
