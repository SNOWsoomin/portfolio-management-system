package com.example.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class JobPost extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String companyName;

    @Lob
    private String description;

    @Column(nullable = false)
    private String position;

    private String sourceName;
    private String sourceUrl;
    private String externalId;

    // Getters and Setters
    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    private java.time.LocalDateTime crawledAt;

    public java.time.LocalDateTime getCrawledAt() { return crawledAt; }
    public void setCrawledAt(java.time.LocalDateTime crawledAt) { this.crawledAt = crawledAt; }
}
