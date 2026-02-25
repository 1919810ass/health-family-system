package com.healthfamily.modules.recommendationv2.dto;

/**
 * 推荐Response
 * <p>
 * 属于业务子模块的核心组件，用于承载该模块的领域模型与服务逻辑。
 * </p>
 */
import java.util.List;

public class RecommendationResponse {
  private List<RecommendationItemDto> items;
  private List<EvidenceDto> evidence;
  private String reasoning;
  private Safety safety;
  private Telemetry telemetry;

  public static class Safety {
    private List<String> contraindications;
    private boolean refuse;
    private String medical_advice;
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
     * 执行业务操作
     * @return 业务返回结果
     */
    public boolean isRefuse() { return refuse; }
    /**
     * 执行业务操作
     * @param refuse 业务参数
     * @return 无
     */
    public void setRefuse(boolean refuse) { this.refuse = refuse; }
    /**
     * 获取
     * @return 业务返回结果
     */
    public String getMedical_advice() { return medical_advice; }
    /**
     * 执行业务操作
     * @param medical_advice 业务参数
     * @return 无
     */
    public void setMedical_advice(String medical_advice) { this.medical_advice = medical_advice; }
  }

  public static class Telemetry {
    private Integer llm_calls;
    private Integer latency_ms;
    private Boolean ai;
    /**
     * 获取
     * @return 业务返回结果
     */
    public Integer getLlm_calls() { return llm_calls; }
    /**
     * 执行业务操作
     * @param llm_calls 业务参数
     * @return 无
     */
    public void setLlm_calls(Integer llm_calls) { this.llm_calls = llm_calls; }
    /**
     * 获取
     * @return 业务返回结果
     */
    public Integer getLatency_ms() { return latency_ms; }
    /**
     * 执行业务操作
     * @param latency_ms 业务参数
     * @return 无
     */
    public void setLatency_ms(Integer latency_ms) { this.latency_ms = latency_ms; }
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
  }

  /**

   * 获取

   * @return 业务返回结果

   */

  public List<RecommendationItemDto> getItems() { return items; }
  /**
   * 执行业务操作
   * @param items 业务参数
   * @return 无
   */
  public void setItems(List<RecommendationItemDto> items) { this.items = items; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public List<EvidenceDto> getEvidence() { return evidence; }
  /**
   * 执行业务操作
   * @param evidence 业务参数
   * @return 无
   */
  public void setEvidence(List<EvidenceDto> evidence) { this.evidence = evidence; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public String getReasoning() { return reasoning; }
  /**
   * 执行业务操作
   * @param reasoning 业务参数
   * @return 无
   */
  public void setReasoning(String reasoning) { this.reasoning = reasoning; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public Safety getSafety() { return safety; }
  /**
   * 执行业务操作
   * @param safety 业务参数
   * @return 无
   */
  public void setSafety(Safety safety) { this.safety = safety; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public Telemetry getTelemetry() { return telemetry; }
  /**
   * 执行业务操作
   * @param telemetry 业务参数
   * @return 无
   */
  public void setTelemetry(Telemetry telemetry) { this.telemetry = telemetry; }
}
