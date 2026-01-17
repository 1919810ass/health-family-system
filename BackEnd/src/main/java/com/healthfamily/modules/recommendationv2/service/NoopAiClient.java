package com.healthfamily.modules.recommendationv2.service;

import com.healthfamily.modules.recommendationv2.dto.EvidenceDto;
import com.healthfamily.modules.recommendationv2.dto.RecommendationItemDto;
import com.healthfamily.modules.recommendationv2.dto.RecommendationResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoopAiClient implements AiClient {
  public RecommendationResponse generate(Map<String,Object> input, int timeoutMs) {
    RecommendationResponse res = new RecommendationResponse();
    List<Map<String,Object>> candidates = (List<Map<String,Object>>) input.getOrDefault("candidateItemsFromRules", new ArrayList<>());
    List<RecommendationItemDto> items = new ArrayList<>();
    for (Map<String,Object> m : candidates) {
      RecommendationItemDto dto = new RecommendationItemDto();
      dto.setId(String.valueOf(m.getOrDefault("id","")));
      dto.setTitle(String.valueOf(m.getOrDefault("title","")));
      dto.setContent(String.valueOf(m.getOrDefault("content","")));
      dto.setCategory(String.valueOf(m.getOrDefault("category","DIET")));
      dto.setSteps((List<String>) m.getOrDefault("steps", new ArrayList<>()));
      dto.setRisk_level("LOW");
      dto.setTime_cost("15min");
      dto.setSource_tags((List<String>) m.getOrDefault("sourceTags", new ArrayList<>()));
      items.add(dto);
    }
    res.setItems(items);
    List<EvidenceDto> evs = new ArrayList<>();
    res.setEvidence(evs);
    res.setReasoning("rules-only");
    RecommendationResponse.Safety s = new RecommendationResponse.Safety();
    s.setContraindications((List<String>) input.getOrDefault("contraindications", new ArrayList<>()));
    s.setRefuse(false);
    s.setMedical_advice("");
    res.setSafety(s);
    RecommendationResponse.Telemetry t = new RecommendationResponse.Telemetry();
    t.setAi(false);
    t.setLatency_ms(0);
    t.setLlm_calls(0);
    res.setTelemetry(t);
    return res;
  }
}
