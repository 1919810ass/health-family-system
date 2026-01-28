package com.healthfamily.modules.recommendationv2.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity(name = "RuleV2")
@Table(name = "rules_v2")
public class RuleV2 {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Category category;

  @Column(name = "condition_json", nullable = false, columnDefinition = "json")
  private String conditionJson;

  @Column(name = "action_template", nullable = false, columnDefinition = "json")
  private String actionTemplate;

  @Column(nullable = false)
  private Double weight;

  private String source;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Status status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public enum Category { DIET, SLEEP, SPORT, MOOD, VITALS }
  public enum Status { ENABLED, DISABLED }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Category getCategory() { return category; }
  public void setCategory(Category category) { this.category = category; }
  public String getConditionJson() { return conditionJson; }
  public void setConditionJson(String conditionJson) { this.conditionJson = conditionJson; }
  public String getActionTemplate() { return actionTemplate; }
  public void setActionTemplate(String actionTemplate) { this.actionTemplate = actionTemplate; }
  public Double getWeight() { return weight; }
  public void setWeight(Double weight) { this.weight = weight; }
  public String getSource() { return source; }
  public void setSource(String source) { this.source = source; }
  public Status getStatus() { return status; }
  public void setStatus(Status status) { this.status = status; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
