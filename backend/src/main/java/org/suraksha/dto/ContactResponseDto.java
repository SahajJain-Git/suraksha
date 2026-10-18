package org.suraksha.dto;

import java.time.LocalDateTime;

public class ContactResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private String category;
    private String subject;
    private String status;
    private LocalDateTime createdAt;

    public ContactResponseDto() {}

    public ContactResponseDto(Long id, String fullName, String email, String category,
                              String subject, String status, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.category = category;
        this.subject = subject;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
