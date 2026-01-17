package com.healthfamily.modules.recommendationv2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.domain.entity.ConstitutionAssessment;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.ConstitutionAssessmentRepository;
import com.healthfamily.modules.recommendationv2.service.model.ConstitutionFeatures;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConstitutionProcessor {
  private final ConstitutionAssessmentRepository repo;
  private final ObjectMapper mapper = new ObjectMapper();
  private final Map<Long, CacheEntry> cache = new ConcurrentHashMap<>();
  private final long ttlMs;

  public ConstitutionProcessor(ConstitutionAssessmentRepository repo, long ttlMs) {
    this.repo = repo;
    this.ttlMs = ttlMs;
  }

  public ConstitutionFeatures load(Long userId, int lookback) {
    CacheEntry e = cache.get(userId);
    long now = System.currentTimeMillis();
    if (e != null && now - e.ts <= ttlMs) return e.features;
    List<ConstitutionAssessment> list = repo.findByUserOrderByCreatedAtDesc(User.builder().id(userId).build());
    List<ConstitutionAssessment> recent = list.stream().limit(Math.max(1, lookback)).toList();
    ConstitutionFeatures f = build(recent);
    cache.put(userId, new CacheEntry(f, now));
    return f;
  }

  private ConstitutionFeatures build(List<ConstitutionAssessment> recent) {
    ConstitutionFeatures f = new ConstitutionFeatures();
    if (recent == null || recent.isEmpty()) { f.setHasData(false); return f; }
    ConstitutionAssessment latest = recent.get(0);
    Map<String, Object> vec = parse(latest.getScoreVector());
    f.setBalanced(norm(vec.getOrDefault("balanced", 0)));
    f.setQiDeficiency(norm(vec.getOrDefault("qi_deficiency", vec.getOrDefault("qi", 0))));
    f.setYangDeficiency(norm(vec.getOrDefault("yang_deficiency", vec.getOrDefault("yang", 0))));
    f.setYinDeficiency(norm(vec.getOrDefault("yin_deficiency", vec.getOrDefault("yin", 0))));
    f.setPhlegmDamp(norm(vec.getOrDefault("phlegm_damp", vec.getOrDefault("phlegm", 0))));
    f.setDampHeat(norm(vec.getOrDefault("damp_heat", vec.getOrDefault("heat", 0))));
    f.setBloodStasis(norm(vec.getOrDefault("blood_stasis", vec.getOrDefault("blood", 0))));
    f.setQiStagnation(norm(vec.getOrDefault("qi_stagnation", vec.getOrDefault("stagnation", 0))));
    f.setSpecial(norm(vec.getOrDefault("special", 0)));
    f.setPrimaryType(latest.getPrimaryType());
    f.setHasData(true);
    Map<String, Double> trend = computeTrend(recent);
    f.setTrend(trend);
    return f;
  }

  private Map<String, Object> parse(String json) {
    try {
      Map<?,?> m = mapper.readValue(json, Map.class);
      Map<String,Object> out = new HashMap<>();
      for (Map.Entry<?,?> en : m.entrySet()) {
        out.put(String.valueOf(en.getKey()).toLowerCase(Locale.ROOT), en.getValue());
      }
      return out;
    } catch (Exception e) { return Map.of(); }
  }

  private double norm(Object v) {
    try {
      double d = Double.parseDouble(String.valueOf(v));
      return Math.max(0.0, Math.min(1.0, d / 100.0));
    } catch (Exception e) { return 0.0; }
  }

  private Map<String, Double> computeTrend(List<ConstitutionAssessment> recent) {
    List<Map<String,Object>> vecs = new ArrayList<>();
    for (ConstitutionAssessment a : recent) vecs.add(parse(a.getScoreVector()));
    Map<String, Double> slope = new HashMap<>();
    String[] keys = new String[]{"balanced","qi_deficiency","yang_deficiency","yin_deficiency","phlegm_damp","damp_heat","blood_stasis","qi_stagnation","special"};
    for (String k : keys) {
      double s = linSlope(vecs, k);
      slope.put(k, s);
    }
    return slope;
  }

  private double linSlope(List<Map<String,Object>> vecs, String key) {
    int n = vecs.size();
    if (n <= 1) return 0.0;
    double sumx=0,sumy=0,sumxy=0,sumx2=0;
    for (int i=0;i<n;i++) {
      double x = i;
      double y = norm(vecs.get(i).getOrDefault(key, 0));
      sumx += x; sumy += y; sumxy += x*y; sumx2 += x*x;
    }
    double denom = n*sumx2 - sumx*sumx;
    if (denom == 0) return 0.0;
    return (n*sumxy - sumx*sumy) / denom;
  }

  private static class CacheEntry {
    final ConstitutionFeatures features; final long ts;
    CacheEntry(ConstitutionFeatures f, long t) { features = f; ts = t; }
  }
}