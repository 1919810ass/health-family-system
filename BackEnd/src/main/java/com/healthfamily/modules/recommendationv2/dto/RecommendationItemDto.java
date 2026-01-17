package com.healthfamily.modules.recommendationv2.dto;

import java.util.List;

public class RecommendationItemDto {
  private String id;
  private String title;
  private String content;
  private String category;
  private List<String> steps;
  private String risk_level;
  private String time_cost;
  private List<String> source_tags;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }
  public List<String> getSteps() { return steps; }
  public void setSteps(List<String> steps) { this.steps = steps; }
  public String getRisk_level() { return risk_level; }
  public void setRisk_level(String risk_level) { this.risk_level = risk_level; }
  public String getTime_cost() { return time_cost; }
  public void setTime_cost(String time_cost) { this.time_cost = time_cost; }
  public List<String> getSource_tags() { return source_tags; }
  public void setSource_tags(List<String> source_tags) { this.source_tags = source_tags; }
}
