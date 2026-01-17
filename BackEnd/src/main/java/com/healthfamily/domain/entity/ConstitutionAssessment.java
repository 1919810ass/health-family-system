package com.healthfamily.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "constitution_assessments")
public class ConstitutionAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 32)
    private String type;

    @Column(name = "score_vector", nullable = false, columnDefinition = "json")
    private String scoreVector;

    @Column(name = "primary_type", nullable = false, length = 32)
    private String primaryType;

    @Column(name = "report_json", nullable = false, columnDefinition = "json")
    private String reportJson;

    @Column(name = "conversation_history", columnDefinition = "json")
    private String conversationHistory; // AI对话历史

    @Column(name = "constitution_tags", columnDefinition = "json")
    private String constitutionTags;

    @Column(name = "confidence_score", columnDefinition = "DECIMAL(5,2)")
    private Double confidenceScore;

    @Column(name = "assessment_version", length = 20)
    private String assessmentVersion;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @Column(name = "follow_up_recommendations", columnDefinition = "json")
    private String followUpRecommendations;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "assessment_source", length = 20, nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'MANUAL'")
    @Builder.Default
    private String assessmentSource = "MANUAL"; // 测评来源：MANUAL-手动，AI-AI驱动

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getScoreVector() {
        return scoreVector;
    }
    
    public void setScoreVector(String scoreVector) {
        this.scoreVector = scoreVector;
    }
    
    public String getPrimaryType() {
        return primaryType;
    }
    
    public void setPrimaryType(String primaryType) {
        this.primaryType = primaryType;
    }
    
    public String getReportJson() {
        return reportJson;
    }
    
    public void setReportJson(String reportJson) {
        this.reportJson = reportJson;
    }
    
    public String getConversationHistory() {
        return conversationHistory;
    }
    
    public void setConversationHistory(String conversationHistory) {
        this.conversationHistory = conversationHistory;
    }
    
    public String getConstitutionTags() {
        return constitutionTags;
    }
    
    public void setConstitutionTags(String constitutionTags) {
        this.constitutionTags = constitutionTags;
    }
    
    public Double getConfidenceScore() {
        return confidenceScore;
    }
    
    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
    
    public String getAssessmentVersion() {
        return assessmentVersion;
    }
    
    public void setAssessmentVersion(String assessmentVersion) {
        this.assessmentVersion = assessmentVersion;
    }
    
    public Boolean getIsPrimary() {
        return isPrimary;
    }
    
    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
    
    public String getFollowUpRecommendations() {
        return followUpRecommendations;
    }
    
    public void setFollowUpRecommendations(String followUpRecommendations) {
        this.followUpRecommendations = followUpRecommendations;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getAssessmentSource() {
        return assessmentSource;
    }
    
    public void setAssessmentSource(String assessmentSource) {
        this.assessmentSource = assessmentSource;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

