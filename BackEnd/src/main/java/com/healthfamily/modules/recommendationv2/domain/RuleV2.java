package com.healthfamily.modules.recommendationv2.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity(name = "RuleV2")
/**
 * 规则V2
 * <p>
 * 属于业务子模块的核心组件，用于承载该模块的领域模型与服务逻辑。
 * </p>
 */
@Table(name = "rules_v2")
public class RuleV2 {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Category category;

  @Column(name = "condition_json", nullable = false, columnDefinition = "json")
  private String conditionJson;

  @Column(name = "action_template", nullable = false, columnDefinition = "json")
  private String actionTemplate;

  @Column(nullable = false)
  private Double weight;

  private String source;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Status status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public enum Category { DIET, SLEEP, SPORT, MOOD, VITALS }
  public enum Status { ENABLED, DISABLED }

  /**

   * 获取

   * @return 业务返回结果

   */

  public Long getId() { return id; }
  /**
   * 执行业务操作
   * @param id 业务对象唯一标识
   * @return 无
   */
  public void setId(Long id) { this.id = id; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public Category getCategory() { return category; }
  /**
   * 执行业务操作
   * @param category 业务参数
   * @return 无
   */
  public void setCategory(Category category) { this.category = category; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public String getConditionJson() { return conditionJson; }
  /**
   * 执行业务操作
   * @param conditionJson 业务参数
   * @return 无
   */
  public void setConditionJson(String conditionJson) { this.conditionJson = conditionJson; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public String getActionTemplate() { return actionTemplate; }
  /**
   * 执行业务操作
   * @param actionTemplate 业务参数
   * @return 无
   */
  public void setActionTemplate(String actionTemplate) { this.actionTemplate = actionTemplate; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public Double getWeight() { return weight; }
  /**
   * 执行业务操作
   * @param weight 业务参数
   * @return 无
   */
  public void setWeight(Double weight) { this.weight = weight; }
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
  public Status getStatus() { return status; }
  /**
   * 执行业务操作
   * @param status 业务参数
   * @return 无
   */
  public void setStatus(Status status) { this.status = status; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public Instant getCreatedAt() { return createdAt; }
  /**
   * 执行业务操作
   * @param createdAt 业务参数
   * @return 无
   */
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public Instant getUpdatedAt() { return updatedAt; }
  /**
   * 执行业务操作
   * @param updatedAt 业务参数
   * @return 无
   */
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
