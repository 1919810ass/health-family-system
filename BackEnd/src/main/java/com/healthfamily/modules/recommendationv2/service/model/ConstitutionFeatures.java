package com.healthfamily.modules.recommendationv2.service.model;

/**
 * ConstitutionFeatures服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
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

  /**

   * 获取

   * @return 业务返回结果

   */

  public double getBalanced() { return balanced; }
  /**
   * 执行业务操作
   * @param v 业务参数
   * @return 无
   */
  public void setBalanced(double v) { balanced = v; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public double getQiDeficiency() { return qiDeficiency; }
  /**
   * 执行业务操作
   * @param v 业务参数
   * @return 无
   */
  public void setQiDeficiency(double v) { qiDeficiency = v; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public double getYangDeficiency() { return yangDeficiency; }
  /**
   * 执行业务操作
   * @param v 业务参数
   * @return 无
   */
  public void setYangDeficiency(double v) { yangDeficiency = v; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public double getYinDeficiency() { return yinDeficiency; }
  /**
   * 执行业务操作
   * @param v 业务参数
   * @return 无
   */
  public void setYinDeficiency(double v) { yinDeficiency = v; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public double getPhlegmDamp() { return phlegmDamp; }
  /**
   * 执行业务操作
   * @param v 业务参数
   * @return 无
   */
  public void setPhlegmDamp(double v) { phlegmDamp = v; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public double getDampHeat() { return dampHeat; }
  /**
   * 执行业务操作
   * @param v 业务参数
   * @return 无
   */
  public void setDampHeat(double v) { dampHeat = v; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public double getBloodStasis() { return bloodStasis; }
  /**
   * 执行业务操作
   * @param v 业务参数
   * @return 无
   */
  public void setBloodStasis(double v) { bloodStasis = v; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public double getQiStagnation() { return qiStagnation; }
  /**
   * 执行业务操作
   * @param v 业务参数
   * @return 无
   */
  public void setQiStagnation(double v) { qiStagnation = v; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public double getSpecial() { return special; }
  /**
   * 执行业务操作
   * @param v 业务参数
   * @return 无
   */
  public void setSpecial(double v) { special = v; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public String getPrimaryType() { return primaryType; }
  /**
   * 执行业务操作
   * @param s 业务参数
   * @return 无
   */
  public void setPrimaryType(String s) { primaryType = s; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public Map<String, Double> getTrend() { return trend; }
  /**
   * 执行业务操作
   * @param t 业务参数
   * @return 无
   */
  public void setTrend(Map<String, Double> t) { trend = t; }
  /**
   * 执行业务操作
   * @return 业务返回结果
   */
  public boolean isHasData() { return hasData; }
  /**
   * 执行业务操作
   * @param b 业务参数
   * @return 无
   */
  public void setHasData(boolean b) { hasData = b; }
}