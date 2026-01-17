package com.healthfamily.modules.recommendationv2.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class GenerateParams {
  @NotNull
  private String date;
  @Min(7)
  @Max(30)
  private Integer scope;
  @Min(1)
  @Max(20)
  private Integer maxItems;
  private Boolean strictMode;

  public String getDate() { return date; }
  public void setDate(String date) { this.date = date; }
  public Integer getScope() { return scope; }
  public void setScope(Integer scope) { this.scope = scope; }
  public Integer getMaxItems() { return maxItems; }
  public void setMaxItems(Integer maxItems) { this.maxItems = maxItems; }
  public Boolean getStrictMode() { return strictMode; }
  public void setStrictMode(Boolean strictMode) { this.strictMode = strictMode; }
}
