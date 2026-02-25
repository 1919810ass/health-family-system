package com.healthfamily.modules.recommendationv2.service.model;

/**
 * Preferences服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
import java.util.List;

public class Preferences {
  private List<String> liked;
  private List<String> disliked;
  /**
   * 获取
   * @return 业务返回结果
   */
  public List<String> getLiked() { return liked; }
  /**
   * 执行业务操作
   * @param liked 业务参数
   * @return 无
   */
  public void setLiked(List<String> liked) { this.liked = liked; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public List<String> getDisliked() { return disliked; }
  /**
   * 执行业务操作
   * @param disliked 业务参数
   * @return 无
   */
  public void setDisliked(List<String> disliked) { this.disliked = disliked; }
}
