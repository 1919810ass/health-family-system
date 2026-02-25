/**
 * LogsSummary服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
package com.healthfamily.modules.recommendationv2.service.model;

public class LogsSummary {
  private int sleepAvgMinutes;
  private int sportDays;
  private int moodNegativeDays;
  private int dietHighSaltDays;
  /**
   * 获取
   * @return 业务返回结果
   */
  public int getSleepAvgMinutes() { return sleepAvgMinutes; }
  /**
   * 执行业务操作
   * @param sleepAvgMinutes 业务参数
   * @return 无
   */
  public void setSleepAvgMinutes(int sleepAvgMinutes) { this.sleepAvgMinutes = sleepAvgMinutes; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public int getSportDays() { return sportDays; }
  /**
   * 执行业务操作
   * @param sportDays 业务参数
   * @return 无
   */
  public void setSportDays(int sportDays) { this.sportDays = sportDays; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public int getMoodNegativeDays() { return moodNegativeDays; }
  /**
   * 执行业务操作
   * @param moodNegativeDays 业务参数
   * @return 无
   */
  public void setMoodNegativeDays(int moodNegativeDays) { this.moodNegativeDays = moodNegativeDays; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public int getDietHighSaltDays() { return dietHighSaltDays; }
  /**
   * 执行业务操作
   * @param dietHighSaltDays 业务参数
   * @return 无
   */
  public void setDietHighSaltDays(int dietHighSaltDays) { this.dietHighSaltDays = dietHighSaltDays; }
}
