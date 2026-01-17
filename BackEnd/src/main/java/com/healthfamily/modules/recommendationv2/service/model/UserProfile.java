package com.healthfamily.modules.recommendationv2.service.model;

import java.util.List;

public class UserProfile {
  private Long userId;
  private List<String> tcmTags;
  private List<String> contraindications;
  private List<String> chronic;
  private List<String> goals;
  private Integer ageYears;
  private Double weightKg;
  private Double heightCm;
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public List<String> getTcmTags() { return tcmTags; }
  public void setTcmTags(List<String> tcmTags) { this.tcmTags = tcmTags; }
  public List<String> getContraindications() { return contraindications; }
  public void setContraindications(List<String> contraindications) { this.contraindications = contraindications; }
  public List<String> getChronic() { return chronic; }
  public void setChronic(List<String> chronic) { this.chronic = chronic; }
  public List<String> getGoals() { return goals; }
  public void setGoals(List<String> goals) { this.goals = goals; }
  public Integer getAgeYears() { return ageYears; }
  public void setAgeYears(Integer ageYears) { this.ageYears = ageYears; }
  public Double getWeightKg() { return weightKg; }
  public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
  public Double getHeightCm() { return heightCm; }
  public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }
}
