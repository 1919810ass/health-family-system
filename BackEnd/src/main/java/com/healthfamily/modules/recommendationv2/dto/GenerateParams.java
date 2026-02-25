package com.healthfamily.modules.recommendationv2.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
/**
 * GenerateParams
 * <p>
 * 属于业务子模块的核心组件，用于承载该模块的领域模型与服务逻辑。
 * </p>
 */
import jakarta.validation.constraints.NotNull;

public class GenerateParams {
  @NotNull
  private String date;
  @Min(7)
  @Max(30)
  private Integer scope;
  @Min(1)
  @Max(20)
  private Integer maxItems;
  private Boolean strictMode;

  /**

   * 获取

   * @return 业务返回结果

   */

  public String getDate() { return date; }
  /**
   * 执行业务操作
   * @param date 日期
   * @return 无
   */
  public void setDate(String date) { this.date = date; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public Integer getScope() { return scope; }
  /**
   * 执行业务操作
   * @param scope 业务参数
   * @return 无
   */
  public void setScope(Integer scope) { this.scope = scope; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public Integer getMaxItems() { return maxItems; }
  /**
   * 执行业务操作
   * @param maxItems 业务参数
   * @return 无
   */
  public void setMaxItems(Integer maxItems) { this.maxItems = maxItems; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public Boolean getStrictMode() { return strictMode; }
  /**
   * 执行业务操作
   * @param strictMode 业务参数
   * @return 无
   */
  public void setStrictMode(Boolean strictMode) { this.strictMode = strictMode; }
}
