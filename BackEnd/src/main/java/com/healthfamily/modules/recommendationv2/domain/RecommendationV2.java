package com.healthfamily.modules.recommendationv2.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity(name = "RecommendationV2")
@Table(name = "recommendations_v2", uniqueConstraints = {
    @UniqueConstraint(name = "uk_reco_v2_user_date", columnNames = {"user_id","date"})
})
public class RecommendationV2 {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "date", nullable = false)
  private java.sql.Date date;

  @Column(name = "items_json", nullable = false, columnDefinition = "json")
  private String itemsJson;

  @Column(name = "evidence_json", nullable = false, columnDefinition = "json")
  private String evidenceJson;

  @Column
  private Double score;

  @Column(name = "ai", nullable = false)
  private Boolean ai;

  @Column(name = "accepted", nullable = false)
  private Boolean accepted;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public java.sql.Date getDate() { return date; }
  public void setDate(java.sql.Date date) { this.date = date; }
  public String getItemsJson() { return itemsJson; }
  public void setItemsJson(String itemsJson) { this.itemsJson = itemsJson; }
  public String getEvidenceJson() { return evidenceJson; }
  public void setEvidenceJson(String evidenceJson) { this.evidenceJson = evidenceJson; }
  public Double getScore() { return score; }
  public void setScore(Double score) { this.score = score; }
  public Boolean getAi() { return ai; }
  public void setAi(Boolean ai) { this.ai = ai; }
  public Boolean getAccepted() { return accepted; }
  public void setAccepted(Boolean accepted) { this.accepted = accepted; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
