package com.healthfamily.modules.recommendationv2.service.model;

import com.healthfamily.modules.recommendationv2.domain.RuleV2;
/**
 * CandidateItem服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
import java.util.List;

public class CandidateItem {
  private String id;
  private String title;
  private String content;
  private RuleV2.Category category;
  private double score;
  private List<String> steps;
  private List<String> sourceTags;
  private double weight;
  private double confidence;
  private List<String> matched;
  /**
   * 获取
   * @return 业务返回结果
   */
  public String getId() { return id; }
  /**
   * 执行业务操作
   * @param id 业务对象唯一标识
   * @return 无
   */
  public void setId(String id) { this.id = id; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public String getTitle() { return title; }
  /**
   * 执行业务操作
   * @param title 业务参数
   * @return 无
   */
  public void setTitle(String title) { this.title = title; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public String getContent() { return content; }
  /**
   * 执行业务操作
   * @param content 业务参数
   * @return 无
   */
  public void setContent(String content) { this.content = content; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public RuleV2.Category getCategory() { return category; }
  /**
   * 执行业务操作
   * @param category 业务参数
   * @return 无
   */
  public void setCategory(RuleV2.Category category) { this.category = category; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public double getScore() { return score; }
  /**
   * 执行业务操作
   * @param score 业务参数
   * @return 无
   */
  public void setScore(double score) { this.score = score; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public List<String> getSteps() { return steps; }
  /**
   * 执行业务操作
   * @param steps 业务参数
   * @return 无
   */
  public void setSteps(List<String> steps) { this.steps = steps; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public List<String> getSourceTags() { return sourceTags; }
  /**
   * 执行业务操作
   * @param sourceTags 业务参数
   * @return 无
   */
  public void setSourceTags(List<String> sourceTags) { this.sourceTags = sourceTags; }
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
  public double getConfidence() { return confidence; }
  /**
   * 执行业务操作
   * @param confidence 业务参数
   * @return 无
   */
  public void setConfidence(double confidence) { this.confidence = confidence; }
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
}
