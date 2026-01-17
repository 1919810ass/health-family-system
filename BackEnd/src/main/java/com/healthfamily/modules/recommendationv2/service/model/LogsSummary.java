package com.healthfamily.modules.recommendationv2.service.model;

public class LogsSummary {
  private int sleepAvgMinutes;
  private int sportDays;
  private int moodNegativeDays;
  private int dietHighSaltDays;
  public int getSleepAvgMinutes() { return sleepAvgMinutes; }
  public void setSleepAvgMinutes(int sleepAvgMinutes) { this.sleepAvgMinutes = sleepAvgMinutes; }
  public int getSportDays() { return sportDays; }
  public void setSportDays(int sportDays) { this.sportDays = sportDays; }
  public int getMoodNegativeDays() { return moodNegativeDays; }
  public void setMoodNegativeDays(int moodNegativeDays) { this.moodNegativeDays = moodNegativeDays; }
  public int getDietHighSaltDays() { return dietHighSaltDays; }
  public void setDietHighSaltDays(int dietHighSaltDays) { this.dietHighSaltDays = dietHighSaltDays; }
}
