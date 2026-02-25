package com.healthfamily.modules.recommendationv2.dto;

/**
 * EvidenceDto
 * <p>
 * 属于业务子模块的核心组件，用于承载该模块的领域模型与服务逻辑。
 * </p>
 */
import java.util.List;

public class EvidenceDto {
  private String rule_id;
  private List<String> matched;
  private double weight;
  private String source;
  private double confidence;

  /**

   * 获取

   * @return 业务返回结果

   */

  public String getRule_id() { return rule_id; }
  /**
   * 执行业务操作
   * @param rule_id 业务参数
   * @return 无
   */
  public void setRule_id(String rule_id) { this.rule_id = rule_id; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public List<String> getMatched() { return matched; }
  /**
   * 执行业务操作
   * @param matched 业务参数
   * @return 无
   */
  public void setMatched(List<String> matched) { this.matched = matched; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public double getWeight() { return weight; }
  /**
   * 执行业务操作
   * @param weight 业务参数
   * @return 无
   */
  public void setWeight(double weight) { this.weight = weight; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public String getSource() { return source; }
  /**
   * 执行业务操作
   * @param source 业务参数
   * @return 无
   */
  public void setSource(String source) { this.source = source; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public double getConfidence() { return confidence; }
  /**
   * 执行业务操作
   * @param confidence 业务参数
   * @return 无
   */
  public void setConfidence(double confidence) { this.confidence = confidence; }
}
