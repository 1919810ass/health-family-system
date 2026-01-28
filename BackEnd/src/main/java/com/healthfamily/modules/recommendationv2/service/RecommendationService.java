package com.healthfamily.modules.recommendationv2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.modules.recommendationv2.domain.RecommendationV2;
import com.healthfamily.modules.recommendationv2.dto.EvidenceDto;
import com.healthfamily.modules.recommendationv2.dto.RecommendationItemDto;
import com.healthfamily.modules.recommendationv2.dto.RecommendationResponse;
import com.healthfamily.modules.recommendationv2.repository.RecommendationV2Repository;
import com.healthfamily.modules.recommendationv2.service.model.CandidateItem;
import com.healthfamily.modules.recommendationv2.service.model.LogsSummary;
import com.healthfamily.modules.recommendationv2.service.model.Preferences;
import com.healthfamily.modules.recommendationv2.service.model.UserProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Instant;
import java.util.*;

@Service
public class RecommendationService {
  private final RuleEngine ruleEngine;
  private final DocRagService ragService;
  private final RecommendationV2Repository repository;
  private final com.healthfamily.modules.recommendationv2.repository.SuggestionFeedbackRepository feedbackRepository;
  private final com.healthfamily.domain.repository.ProfileRepository profileRepository;
  private final com.healthfamily.domain.repository.HealthLogRepository healthLogRepository;
  private final com.healthfamily.domain.repository.ConstitutionAssessmentRepository constitutionRepository;
  private final ObjectMapper mapper = new ObjectMapper();
  private final AiClient aiClient;
  private final int timeoutMs;
  private final ConstitutionProcessor constitutionProcessor;

  public RecommendationService(RuleEngine ruleEngine,
                               DocRagService ragService,
                               RecommendationV2Repository repository,
                               com.healthfamily.modules.recommendationv2.repository.SuggestionFeedbackRepository feedbackRepository,
                               com.healthfamily.domain.repository.ProfileRepository profileRepository,
                               com.healthfamily.domain.repository.HealthLogRepository healthLogRepository,
                               com.healthfamily.domain.repository.ConstitutionAssessmentRepository constitutionRepository,
                               @Value("${recommendation.ai.timeoutMs:120000}") int timeoutMs,
                               @Value("${ai.enabled:true}") boolean aiEnabled) {
    this.ruleEngine = ruleEngine;
    this.ragService = ragService;
    this.repository = repository;
    this.feedbackRepository = feedbackRepository;
    this.profileRepository = profileRepository;
    this.healthLogRepository = healthLogRepository;
    this.constitutionRepository = constitutionRepository;
    this.timeoutMs = timeoutMs;
    this.aiClient = new NoopAiClient();
    this.constitutionProcessor = new ConstitutionProcessor(constitutionRepository, 10 * 60 * 1000);
  }

  public RecommendationResponse generate(Long userId, Date date, int scope, int maxItems, boolean strictMode) {
    UserProfile profile = buildProfile(userId);
    LogsSummary summary = buildLogsSummary(userId, date, scope);
    Preferences pref = buildPreferences(userId);
    var constitution = constitutionProcessor.load(userId, 5);
    List<CandidateItem> candidates = ruleEngine.generateByCategory(profile, summary, pref, maxItems, com.healthfamily.modules.recommendationv2.domain.RuleV2.Category.DIET);
    List<Map<String,Object>> cMaps = new ArrayList<>();
    for (CandidateItem c : candidates) {
      Map<String,Object> m = new HashMap<>();
      m.put("id", c.getId());
      m.put("title", c.getTitle());
      m.put("content", c.getContent());
      m.put("category", c.getCategory().name());
      m.put("steps", c.getSteps());
      m.put("sourceTags", c.getSourceTags());
      m.put("score", c.getScore());
      m.put("matched", c.getMatched());
      cMaps.add(m);
    }
    RecommendationItemDto dietItem = buildDietItem(profile, pref, summary, constitution);
    Map<String,Object> dietMap = new HashMap<>();
    dietMap.put("id", "diet-structured");
    dietMap.put("title", dietItem.getTitle());
    dietMap.put("content", dietItem.getContent());
    dietMap.put("category", "DIET");
    dietMap.put("steps", dietItem.getSteps());
    dietMap.put("sourceTags", dietItem.getSource_tags());
    dietMap.put("score", 1.0);
    dietMap.put("matched", java.util.List.of("diet"));
    cMaps.add(0, dietMap);
    List<Map<String,Object>> frags = ragService.search("diet");
    Map<String,Object> input = new HashMap<>();
    input.put("profile", profile);
    input.put("logsSummary"+scope, summary);
    input.put("preferences", pref);
    input.put("contraindications", profile.getContraindications());
    input.put("goals", profile.getGoals());
    input.put("candidateItemsFromRules", cMaps);
    input.put("ragFragments", frags);
    input.put("maxItems", maxItems);
    RecommendationResponse aiRes;
    boolean ai = false;
    try {
      aiRes = aiClient.generate(input, timeoutMs);
      ai = Optional.ofNullable(aiRes.getTelemetry()).map(RecommendationResponse.Telemetry::getAi).orElse(false);
    } catch (Exception e) {
      aiRes = rulesOnlyResponse(candidates, profile.getContraindications());
      ai = false;
    }
    int mismatch = 0;
    if (aiRes.getItems() != null) {
      for (com.healthfamily.modules.recommendationv2.dto.RecommendationItemDto it : aiRes.getItems()) {
        var res = SuggestionValidator.validate(it, profile, pref);
        if (res.categoryMismatch) mismatch++;
      }
    }
    RecommendationV2 rec = new RecommendationV2();
    rec.setUserId(userId);
    rec.setDate(date);
    try {
      rec.setItemsJson(mapper.writeValueAsString(aiRes.getItems()));
      rec.setEvidenceJson(mapper.writeValueAsString(aiRes.getEvidence()));
    } catch (Exception ex) {
      rec.setItemsJson("[]");
      rec.setEvidenceJson("[]");
    }
    rec.setScore(candidates.stream().mapToDouble(CandidateItem::getScore).average().orElse(0.0));
    rec.setAi(ai);
    rec.setAccepted(false);
    rec.setCreatedAt(Instant.now());
    rec.setUpdatedAt(Instant.now());
    repository.save(rec);
    if (mismatch > 0) {
      com.healthfamily.modules.recommendationv2.domain.SuggestionFeedback fb = new com.healthfamily.modules.recommendationv2.domain.SuggestionFeedback();
      fb.setRecommendationId(rec.getId());
      fb.setUserId(userId);
      fb.setUseful(false);
      fb.setReason("category_mismatch:" + mismatch);
      fb.setCreatedAt(Instant.now());
      feedbackRepository.save(fb);
    }
    return aiRes;
  }

  private RecommendationItemDto buildDietItem(UserProfile profile, Preferences pref, LogsSummary summary, com.healthfamily.modules.recommendationv2.service.model.ConstitutionFeatures constitution) {
    double weight = Optional.ofNullable(profile.getWeightKg()).orElse(60.0);
    double height = Optional.ofNullable(profile.getHeightCm()).orElse(170.0);
    double bmi = height > 0 ? (weight / Math.pow(height/100.0, 2)) : 22.0;
    int age = Optional.ofNullable(profile.getAgeYears()).orElse(30);
    boolean ht = Optional.ofNullable(profile.getChronic()).orElse(java.util.List.of()).stream().anyMatch(s -> s.toLowerCase().contains("hypertension") || s.contains("高血压"));
    boolean dm = Optional.ofNullable(profile.getChronic()).orElse(java.util.List.of()).stream().anyMatch(s -> s.toLowerCase().contains("diabetes") || s.contains("糖尿病"));
    double proteinFactor = (age >= 65 ? 1.2 : 1.0);
    if (constitution != null && constitution.isHasData()) {
      proteinFactor += Math.min(0.2, constitution.getQiDeficiency() * 0.3);
    }
    double proteinG = Math.round(proteinFactor * weight);
    String salt = ht ? "≤5g/日" : "≤6g/日";
    String sugar = dm ? "≤25g/日，选择低GI主食，水果分次食用" : "≤25g/日";
    String grains = dm ? "全谷物和低GI主食每餐2-3份，每份熟重~100g" : "全谷物主食每餐2-3份，每份熟重~100g";
    java.util.List<String> steps = new java.util.ArrayList<>();
    steps.add("主食:" + grains);
    steps.add("蛋白质: 鸡胸/鱼/豆腐 每餐1份(肉类75-100g或豆腐150g)，每日2-3餐，总蛋白约" + (int)proteinG + "g");
    steps.add("蔬菜水果: 蔬菜≥500g/日(深色≥一半)，水果200-350g/日");
    String oil = "食用油25-30g/日";
    if (constitution != null && constitution.isHasData()) {
      double pd = constitution.getPhlegmDamp() + constitution.getDampHeat();
      if (pd > 0.6) oil = "食用油20-25g/日";
    }
    steps.add("奶类坚果: 低脂奶300ml/日，坚果25-30g，每周3-5次");
    steps.add("油盐糖:" + oil + ", 食盐" + salt + ", 添加糖" + sugar);
    steps.add("水分: 1500-2000ml/日，少含糖饮料");
    if (pref.getDisliked() != null && !pref.getDisliked().isEmpty()) {
      steps.add("避免: " + String.join("/", pref.getDisliked()));
    }
    java.util.List<String> tags = new java.util.ArrayList<>();
    tags.add("guideline");
    tags.add("profile");
    tags.addAll(NutritionMapper.extractTags(String.join(" ", steps)));
    if (constitution != null && constitution.isHasData()) {
      tags.add("constitution:" + Optional.ofNullable(constitution.getPrimaryType()).orElse("unknown"));
      double qiImp = constitution.getQiDeficiency() * 0.3;
      double pdImp = (constitution.getPhlegmDamp() + constitution.getDampHeat()) * 0.25;
      tags.add("importance:qi_deficiency:" + String.format(java.util.Locale.ROOT, "%.2f", qiImp));
      tags.add("importance:phlegm_damp_heat:" + String.format(java.util.Locale.ROOT, "%.2f", pdImp));
    }
    RecommendationItemDto dto = new RecommendationItemDto();
    dto.setId("diet-structured");
    dto.setTitle("饮食计划(个性化)");
    dto.setContent("根据年龄:" + age + "岁, 体重:" + (int)weight + "kg, BMI:" + String.format(java.util.Locale.ROOT, "%.1f", bmi) + "，建议如下: \n" + String.join("; ", steps));
    dto.setCategory("DIET");
    dto.setSteps(steps);
    dto.setRisk_level("LOW");
    dto.setTime_cost("daily");
    dto.setSource_tags(tags);
    return dto;
  }

  private UserProfile buildProfile(Long userId) {
    UserProfile p = new UserProfile();
    p.setUserId(userId);
    var opt = profileRepository.findById(userId);
    if (opt.isEmpty()) {
      p.setTcmTags(new java.util.ArrayList<>());
      p.setContraindications(new java.util.ArrayList<>());
      p.setChronic(new java.util.ArrayList<>());
      p.setGoals(new java.util.ArrayList<>());
      return p;
    }
    var prof = opt.get();
    java.util.List<String> tcm = parseList(prof.getTcmTags());
    java.util.List<String> allergies = parseList(prof.getAllergies());
    java.util.List<String> health = parseList(prof.getHealthTags());
    java.util.List<String> goals = parseList(prof.getGoals());
    p.setTcmTags(tcm);
    p.setContraindications(allergies);
    p.setChronic(health);
    p.setGoals(goals);
    if (prof.getBirthday() != null) {
      java.time.Period period = java.time.Period.between(prof.getBirthday(), java.time.LocalDate.now());
      p.setAgeYears(period.getYears());
    }
    if (prof.getWeightKg() != null) {
      p.setWeightKg(prof.getWeightKg().doubleValue());
    }
    if (prof.getHeightCm() != null) {
      p.setHeightCm(prof.getHeightCm().doubleValue());
    }
    return p;
  }

  private Preferences buildPreferences(Long userId) {
    Preferences pref = new Preferences();
    var opt = profileRepository.findById(userId);
    if (opt.isEmpty()) return pref;
    var prof = opt.get();
    java.util.Map<String,Object> map = parseMap(prof.getPreferences());
    java.util.List<String> liked = new java.util.ArrayList<>();
    java.util.List<String> disliked = new java.util.ArrayList<>();
    Object dietObj = map.get("diet");
    if (dietObj instanceof java.util.Map<?,?> d) {
      Object l = d.get("liked");
      Object dl = d.get("disliked");
      if (l instanceof java.util.Collection<?> li) {
        for (Object o : li) liked.add(String.valueOf(o));
      }
      if (dl instanceof java.util.Collection<?> di) {
        for (Object o : di) disliked.add(String.valueOf(o));
      }
    }
    pref.setLiked(liked);
    pref.setDisliked(disliked);
    return pref;
  }

  private LogsSummary buildLogsSummary(Long userId, Date date, int scope) {
    java.time.LocalDate end = date.toLocalDate();
    java.time.LocalDate start = end.minusDays(scope);
    java.util.List<com.healthfamily.domain.entity.HealthLog> logs = healthLogRepository.findByUserAndLogDateBetweenOrderByLogDateDesc(
            com.healthfamily.domain.entity.User.builder().id(userId).build(), start, end);
    LogsSummary s = new LogsSummary();
    int sleepSum = 0; int sleepCnt = 0; int sportDays = 0; int moodNeg = 0; int saltHigh = 0;
    for (var log : logs) {
      var content = String.valueOf(log.getContentJson());
      switch (log.getType()) {
        case SLEEP -> { sleepCnt++; sleepSum += extractInt(content, "minutes", 0); }
        case SPORT -> sportDays++;
        case MOOD -> { if (content.contains("差") || content.contains("压力")) moodNeg++; }
        case DIET -> { if (content.contains("重盐") || content.contains("咸")) saltHigh++; }
        default -> {}
      }
    }
    s.setSleepAvgMinutes(sleepCnt == 0 ? 0 : sleepSum / Math.max(1, sleepCnt));
    s.setSportDays(sportDays);
    s.setMoodNegativeDays(moodNeg);
    s.setDietHighSaltDays(saltHigh);
    return s;
  }

  private int extractInt(String json, String key, int def) {
    try {
      var node = mapper.readTree(json);
      if (node.has(key)) return node.get(key).asInt(def);
      return def;
    } catch (Exception e) { return def; }
  }

  private java.util.List<String> parseList(String json) {
    if (json == null || json.isEmpty()) return new java.util.ArrayList<>();
    try {
      java.util.List<?> arr = mapper.readValue(json, java.util.List.class);
      java.util.List<String> out = new java.util.ArrayList<>();
      for (Object o : arr) out.add(String.valueOf(o));
      return out;
    } catch (Exception e) { return new java.util.ArrayList<>(); }
  }

  private java.util.Map<String,Object> parseMap(String json) {
    if (json == null || json.isEmpty()) return new java.util.HashMap<>();
    try {
      return mapper.readValue(json, java.util.Map.class);
    } catch (Exception e) { return new java.util.HashMap<>(); }
  }

  private RecommendationResponse rulesOnlyResponse(List<CandidateItem> candidates, List<String> contraindications) {
    RecommendationResponse res = new RecommendationResponse();
    List<RecommendationItemDto> items = new ArrayList<>();
    for (CandidateItem c : candidates) {
      RecommendationItemDto dto = new RecommendationItemDto();
      dto.setId(c.getId());
      dto.setTitle(c.getTitle());
      dto.setContent(c.getContent());
      dto.setCategory(c.getCategory().name());
      dto.setSteps(c.getSteps());
      dto.setRisk_level("LOW");
      dto.setTime_cost("15min");
      dto.setSource_tags(c.getSourceTags());
      items.add(dto);
    }
    res.setItems(items);
    List<EvidenceDto> evs = new ArrayList<>();
    res.setEvidence(evs);
    res.setReasoning("rules-only");
    RecommendationResponse.Safety s = new RecommendationResponse.Safety();
    s.setContraindications(Optional.ofNullable(contraindications).orElse(new ArrayList<>()));
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
