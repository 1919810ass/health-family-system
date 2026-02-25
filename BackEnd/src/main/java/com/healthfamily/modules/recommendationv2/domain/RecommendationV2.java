package com.healthfamily.modules.recommendationv2.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity(name = "RecommendationV2")
@Table(name = "recommendations_v2", uniqueConstraints = {
    @UniqueConstraint(name = "uk_reco_v2_user_date", columnNames = {"user_id","date"})
/**
 * 推荐V2
 * <p>
 * 属于业务子模块的核心组件，用于承载该模块的领域模型与服务逻辑。
 * </p>
 */
})
public class RecommendationV2 {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "date", nullable = false)
  private java.sql.Date date;

  @Column(name = "items_json", nullable = false, columnDefinition = "json")
  private String itemsJson;

  @Column(name = "evidence_json", nullable = false, columnDefinition = "json")
  private String evidenceJson;

  @Column
  private Double score;

  @Column(name = "ai", nullable = false)
  private Boolean ai;

  @Column(name = "accepted", nullable = false)
  private Boolean accepted;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

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
  public java.sql.Date getDate() { return date; }
  /**
   * 执行业务操作
   * @param date 日期
   * @return 无
   */
  public void setDate(java.sql.Date date) { this.date = date; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public String getItemsJson() { return itemsJson; }
  /**
   * 执行业务操作
   * @param itemsJson 业务参数
   * @return 无
   */
  public void setItemsJson(String itemsJson) { this.itemsJson = itemsJson; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public String getEvidenceJson() { return evidenceJson; }
  /**
   * 执行业务操作
   * @param evidenceJson 业务参数
   * @return 无
   */
  public void setEvidenceJson(String evidenceJson) { this.evidenceJson = evidenceJson; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public Double getScore() { return score; }
  /**
   * 执行业务操作
   * @param score 业务参数
   * @return 无
   */
  public void setScore(Double score) { this.score = score; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public Boolean getAi() { return ai; }
  /**
   * 执行业务操作
   * @param ai 业务参数
   * @return 无
   */
  public void setAi(Boolean ai) { this.ai = ai; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public Boolean getAccepted() { return accepted; }
  /**
   * 执行业务操作
   * @param accepted 业务参数
   * @return 无
   */
  public void setAccepted(Boolean accepted) { this.accepted = accepted; }
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
