package com.healthfamily.modules.recommendationv2.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "suggestion_feedback")
public class SuggestionFeedback {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "recommendation_id", nullable = false)
  private Long recommendationId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "useful", nullable = false)
  private Boolean useful;

  @Column(name = "reason")
  private String reason;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getRecommendationId() { return recommendationId; }
  public void setRecommendationId(Long recommendationId) { this.recommendationId = recommendationId; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public Boolean getUseful() { return useful; }
  public void setUseful(Boolean useful) { this.useful = useful; }
  public String getReason() { return reason; }
  public void setReason(String reason) { this.reason = reason; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
