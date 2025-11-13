package com.helpdesk.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "response_templates")
public class ResponseTemplate extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(length = 10000)
    private String content;

    @Column(nullable = false)
    private String createdBy;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}