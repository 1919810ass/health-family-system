package com.healthfamily.modules.recommendationv2.service.model;

import java.util.Map;

public class ConstitutionFeatures {
  private double balanced;
  private double qiDeficiency;
  private double yangDeficiency;
  private double yinDeficiency;
  private double phlegmDamp;
  private double dampHeat;
  private double bloodStasis;
  private double qiStagnation;
  private double special;
  private String primaryType;
  private Map<String, Double> trend;
  private boolean hasData;

  public double getBalanced() { return balanced; }
  public void setBalanced(double v) { balanced = v; }
  public double getQiDeficiency() { return qiDeficiency; }
  public void setQiDeficiency(double v) { qiDeficiency = v; }
  public double getYangDeficiency() { return yangDeficiency; }
  public void setYangDeficiency(double v) { yangDeficiency = v; }
  public double getYinDeficiency() { return yinDeficiency; }
  public void setYinDeficiency(double v) { yinDeficiency = v; }
  public double getPhlegmDamp() { return phlegmDamp; }
  public void setPhlegmDamp(double v) { phlegmDamp = v; }
  public double getDampHeat() { return dampHeat; }
  public void setDampHeat(double v) { dampHeat = v; }
  public double getBloodStasis() { return bloodStasis; }
  public void setBloodStasis(double v) { bloodStasis = v; }
  public double getQiStagnation() { return qiStagnation; }
  public void setQiStagnation(double v) { qiStagnation = v; }
  public double getSpecial() { return special; }
  public void setSpecial(double v) { special = v; }
  public String getPrimaryType() { return primaryType; }
  public void setPrimaryType(String s) { primaryType = s; }
  public Map<String, Double> getTrend() { return trend; }
  public void setTrend(Map<String, Double> t) { trend = t; }
  public boolean isHasData() { return hasData; }
  public void setHasData(boolean b) { hasData = b; }
}