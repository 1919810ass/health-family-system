package com.healthfamily.modules.recommendationv2.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
/**
 * SuggestionFeedback
 * <p>
 * 属于业务子模块的核心组件，用于承载该模块的领域模型与服务逻辑。
 * </p>
 */
@Table(name = "suggestion_feedback")
public class SuggestionFeedback {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "recommendation_id", nullable = false)
  private Long recommendationId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "useful", nullable = false)
  private Boolean useful;

  @Column(name = "reason")
  private String reason;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

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
  public Long getRecommendationId() { return recommendationId; }
  /**
   * 执行业务操作
   * @param recommendationId 业务对象唯一标识
   * @return 无
   */
  public void setRecommendationId(Long recommendationId) { this.recommendationId = recommendationId; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public Long getUserId() { return userId; }
  /**
   * 执行业务操作
   * @param userId 家庭成员唯一标识
   * @return 无
   */
  public void setUserId(Long userId) { this.userId = userId; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public Boolean getUseful() { return useful; }
  /**
   * 执行业务操作
   * @param useful 业务参数
   * @return 无
   */
  public void setUseful(Boolean useful) { this.useful = useful; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public String getReason() { return reason; }
  /**
   * 执行业务操作
   * @param reason 业务参数
   * @return 无
   */
  public void setReason(String reason) { this.reason = reason; }
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
}
