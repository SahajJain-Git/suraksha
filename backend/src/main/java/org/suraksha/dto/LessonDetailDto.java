package org.suraksha.dto;

public class LessonDetailDto {
    private Long id;
    private String slug;
    private String title;
    private Integer sequenceOrder;
    private String content;
    private String dosGuidelines;
    private String dontsGuidelines;

    public LessonDetailDto() {}

    public LessonDetailDto(Long id, String slug, String title, Integer sequenceOrder,
                           String content, String dosGuidelines, String dontsGuidelines) {
        this.id = id;
        this.slug = slug;
        this.title = title;
        this.sequenceOrder = sequenceOrder;
        this.content = content;
        this.dosGuidelines = dosGuidelines;
        this.dontsGuidelines = dontsGuidelines;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
}
