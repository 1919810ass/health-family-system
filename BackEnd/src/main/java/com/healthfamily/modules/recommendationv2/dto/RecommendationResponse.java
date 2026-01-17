package com.healthfamily.modules.recommendationv2.dto;

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
    public List<String> getContraindications() { return contraindications; }
    public void setContraindications(List<String> contraindications) { this.contraindications = contraindications; }
    public boolean isRefuse() { return refuse; }
    public void setRefuse(boolean refuse) { this.refuse = refuse; }
    public String getMedical_advice() { return medical_advice; }
    public void setMedical_advice(String medical_advice) { this.medical_advice = medical_advice; }
  }

  public static class Telemetry {
    private Integer llm_calls;
    private Integer latency_ms;
    private Boolean ai;
    public Integer getLlm_calls() { return llm_calls; }
    public void setLlm_calls(Integer llm_calls) { this.llm_calls = llm_calls; }
    public Integer getLatency_ms() { return latency_ms; }
    public void setLatency_ms(Integer latency_ms) { this.latency_ms = latency_ms; }
    public Boolean getAi() { return ai; }
    public void setAi(Boolean ai) { this.ai = ai; }
  }

  public List<RecommendationItemDto> getItems() { return items; }
  public void setItems(List<RecommendationItemDto> items) { this.items = items; }
  public List<EvidenceDto> getEvidence() { return evidence; }
  public void setEvidence(List<EvidenceDto> evidence) { this.evidence = evidence; }
  public String getReasoning() { return reasoning; }
  public void setReasoning(String reasoning) { this.reasoning = reasoning; }
  public Safety getSafety() { return safety; }
  public void setSafety(Safety safety) { this.safety = safety; }
  public Telemetry getTelemetry() { return telemetry; }
  public void setTelemetry(Telemetry telemetry) { this.telemetry = telemetry; }
}
