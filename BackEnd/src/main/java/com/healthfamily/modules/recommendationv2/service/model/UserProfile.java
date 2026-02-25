package com.healthfamily.modules.recommendationv2.service.model;

/**
 * 用户画像服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
import java.util.List;

public class UserProfile {
  private Long userId;
  private List<String> tcmTags;
  private List<String> contraindications;
  private List<String> chronic;
  private List<String> goals;
  private Integer ageYears;
  private Double weightKg;
  private Double heightCm;
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
  public List<String> getTcmTags() { return tcmTags; }
  /**
   * 执行业务操作
   * @param tcmTags 业务参数
   * @return 无
   */
  public void setTcmTags(List<String> tcmTags) { this.tcmTags = tcmTags; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public List<String> getContraindications() { return contraindications; }
  /**
   * 执行业务操作
   * @param contraindications 业务参数
   * @return 无
   */
  public void setContraindications(List<String> contraindications) { this.contraindications = contraindications; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public List<String> getChronic() { return chronic; }
  /**
   * 执行业务操作
   * @param chronic 业务参数
   * @return 无
   */
  public void setChronic(List<String> chronic) { this.chronic = chronic; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public List<String> getGoals() { return goals; }
  /**
   * 执行业务操作
   * @param goals 业务参数
   * @return 无
   */
  public void setGoals(List<String> goals) { this.goals = goals; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public Integer getAgeYears() { return ageYears; }
  /**
   * 执行业务操作
   * @param ageYears 业务参数
   * @return 无
   */
  public void setAgeYears(Integer ageYears) { this.ageYears = ageYears; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public Double getWeightKg() { return weightKg; }
  /**
   * 执行业务操作
   * @param weightKg 业务参数
   * @return 无
   */
  public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public Double getHeightCm() { return heightCm; }
  /**
   * 执行业务操作
   * @param heightCm 业务参数
   * @return 无
   */
  public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }
}
