package com.healthfamily.modules.recommendationv2.dto;

/**
 * 推荐ItemDto
 * <p>
 * 属于业务子模块的核心组件，用于承载该模块的领域模型与服务逻辑。
 * </p>
 */
import java.util.List;

public class RecommendationItemDto {
  private String id;
  private String title;
  private String content;
  private String category;
  private List<String> steps;
  private String risk_level;
  private String time_cost;
  private List<String> source_tags;

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
  public String getCategory() { return category; }
  /**
   * 执行业务操作
   * @param category 业务参数
   * @return 无
   */
  public void setCategory(String category) { this.category = category; }
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
  public String getRisk_level() { return risk_level; }
  /**
   * 执行业务操作
   * @param risk_level 业务参数
   * @return 无
   */
  public void setRisk_level(String risk_level) { this.risk_level = risk_level; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public String getTime_cost() { return time_cost; }
  /**
   * 执行业务操作
   * @param time_cost 业务参数
   * @return 无
   */
  public void setTime_cost(String time_cost) { this.time_cost = time_cost; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public List<String> getSource_tags() { return source_tags; }
  /**
   * 执行业务操作
   * @param source_tags 业务参数
   * @return 无
   */
  public void setSource_tags(List<String> source_tags) { this.source_tags = source_tags; }
}
