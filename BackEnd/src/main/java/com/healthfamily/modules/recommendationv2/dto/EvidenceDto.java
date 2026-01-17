package com.healthfamily.modules.recommendationv2.dto;

import java.util.List;

public class EvidenceDto {
  private String rule_id;
  private List<String> matched;
  private double weight;
  private String source;
  private double confidence;

  public String getRule_id() { return rule_id; }
  public void setRule_id(String rule_id) { this.rule_id = rule_id; }
  public List<String> getMatched() { return matched; }
  public void setMatched(List<String> matched) { this.matched = matched; }
  public double getWeight() { return weight; }
  public void setWeight(double weight) { this.weight = weight; }
  public String getSource() { return source; }
  public void setSource(String source) { this.source = source; }
  public double getConfidence() { return confidence; }
  public void setConfidence(double confidence) { this.confidence = confidence; }
}
