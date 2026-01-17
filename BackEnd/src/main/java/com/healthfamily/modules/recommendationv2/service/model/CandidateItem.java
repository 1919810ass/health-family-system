package com.healthfamily.modules.recommendationv2.service.model;

import com.healthfamily.modules.recommendationv2.domain.Rule;
import java.util.List;

public class CandidateItem {
  private String id;
  private String title;
  private String content;
  private Rule.Category category;
  private double score;
  private List<String> steps;
  private List<String> sourceTags;
  private double weight;
  private double confidence;
  private List<String> matched;
  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public Rule.Category getCategory() { return category; }
  public void setCategory(Rule.Category category) { this.category = category; }
  public double getScore() { return score; }
  public void setScore(double score) { this.score = score; }
  public List<String> getSteps() { return steps; }
  public void setSteps(List<String> steps) { this.steps = steps; }
  public List<String> getSourceTags() { return sourceTags; }
  public void setSourceTags(List<String> sourceTags) { this.sourceTags = sourceTags; }
  public double getWeight() { return weight; }
  public void setWeight(double weight) { this.weight = weight; }
  public double getConfidence() { return confidence; }
  public void setConfidence(double confidence) { this.confidence = confidence; }
  public List<String> getMatched() { return matched; }
  public void setMatched(List<String> matched) { this.matched = matched; }
}
