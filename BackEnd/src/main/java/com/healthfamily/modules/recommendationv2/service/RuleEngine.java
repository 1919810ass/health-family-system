package com.healthfamily.modules.recommendationv2.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.modules.recommendationv2.domain.RuleV2;
import com.healthfamily.modules.recommendationv2.repository.RuleV2Repository;
import com.healthfamily.modules.recommendationv2.service.model.CandidateItem;
import com.healthfamily.modules.recommendationv2.service.model.LogsSummary;
import com.healthfamily.modules.recommendationv2.service.model.Preferences;
import com.healthfamily.modules.recommendationv2.service.model.UserProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RuleEngine {
  private final RuleV2Repository ruleRepository;
  private final ObjectMapper mapper = new ObjectMapper();
  private final double lambdaPenalty;
  private final double lambdaPrefer;
  private final double lambdaDistance;
  private final double lambdaAdherence;

  public RuleEngine(RuleV2Repository ruleRepository,
                    @Value("${recommendation.lambda.penaltyContra}") double lambdaPenalty,
                    @Value("${recommendation.lambda.preferMatch}") double lambdaPrefer,
                    @Value("${recommendation.lambda.distance}") double lambdaDistance,
                    @Value("${recommendation.lambda.adherence}") double lambdaAdherence) {
    this.ruleRepository = ruleRepository;
    this.lambdaPenalty = lambdaPenalty;
    this.lambdaPrefer = lambdaPrefer;
    this.lambdaDistance = lambdaDistance;
    this.lambdaAdherence = lambdaAdherence;
  }

  public List<CandidateItem> generate(UserProfile profile, LogsSummary summary, Preferences preferences, int maxItems) {
    List<RuleV2> rules = new ArrayList<>();
    rules.addAll(ruleRepository.findByCategoryAndStatus(RuleV2.Category.DIET, RuleV2.Status.ENABLED));
    rules.addAll(ruleRepository.findByCategoryAndStatus(RuleV2.Category.SLEEP, RuleV2.Status.ENABLED));
    rules.addAll(ruleRepository.findByCategoryAndStatus(RuleV2.Category.SPORT, RuleV2.Status.ENABLED));
    rules.addAll(ruleRepository.findByCategoryAndStatus(RuleV2.Category.MOOD, RuleV2.Status.ENABLED));
    rules.addAll(ruleRepository.findByCategoryAndStatus(RuleV2.Category.VITALS, RuleV2.Status.ENABLED));
    List<CandidateItem> items = new ArrayList<>();
    for (RuleV2 r : rules) {
      try {
        JsonNode cond = mapper.readTree(r.getConditionJson());
        if (!matches(cond, profile, summary)) continue;
        JsonNode action = mapper.readTree(r.getActionTemplate());
        CandidateItem ci = new CandidateItem();
        ci.setId(String.valueOf(r.getId()));
        ci.setCategory(r.getCategory());
        ci.setTitle(action.path("title").asText(""));
        ci.setContent(action.path("content").asText(""));
        List<String> steps = new ArrayList<>();
        if (action.has("steps") && action.get("steps").isArray()) {
          action.get("steps").forEach(n -> steps.add(n.asText()));
        }
        ci.setSteps(steps);
        ci.setSourceTags(Arrays.asList(Optional.ofNullable(r.getSource()).orElse("rules")));
        ci.setWeight(Optional.ofNullable(r.getWeight()).orElse(1.0));
        ci.setConfidence(0.8);
        ci.setMatched(extractMatchedKeys(cond));
        double score = score(ci, preferences, profile);
        ci.setScore(score);
        items.add(ci);
      } catch (Exception ignored) {}
    }
    items.sort(Comparator.comparingDouble(CandidateItem::getScore).reversed());
    List<CandidateItem> dedup = dedupByTitle(items);
    if (dedup.size() > maxItems) return dedup.subList(0, maxItems);
    return dedup;
  }

  public List<CandidateItem> generateByCategory(UserProfile profile, LogsSummary summary, Preferences preferences, int maxItems, RuleV2.Category category) {
    List<RuleV2> rules = new ArrayList<>();
    rules.addAll(ruleRepository.findByCategoryAndStatus(category, RuleV2.Status.ENABLED));
    List<CandidateItem> items = new ArrayList<>();
    for (RuleV2 r : rules) {
      try {
        JsonNode cond = mapper.readTree(r.getConditionJson());
        if (!matches(cond, profile, summary)) continue;
        JsonNode action = mapper.readTree(r.getActionTemplate());
        CandidateItem ci = new CandidateItem();
        ci.setId(String.valueOf(r.getId()));
        ci.setCategory(r.getCategory());
        ci.setTitle(action.path("title").asText(""));
        ci.setContent(action.path("content").asText(""));
        List<String> steps = new ArrayList<>();
        if (action.has("steps") && action.get("steps").isArray()) {
          action.get("steps").forEach(n -> steps.add(n.asText()));
        }
        ci.setSteps(steps);
        ci.setSourceTags(Arrays.asList(Optional.ofNullable(r.getSource()).orElse("rules")));
        ci.setWeight(Optional.ofNullable(r.getWeight()).orElse(1.0));
        ci.setConfidence(0.8);
        ci.setMatched(extractMatchedKeys(cond));
        double score = score(ci, preferences, profile);
        ci.setScore(score);
        items.add(ci);
      } catch (Exception ignored) {}
    }
    items.sort(Comparator.comparingDouble(CandidateItem::getScore).reversed());
    List<CandidateItem> dedup = dedupByTitle(items);
    if (dedup.size() > maxItems) return dedup.subList(0, maxItems);
    return dedup;
  }

  private boolean matches(JsonNode cond, UserProfile profile, LogsSummary summary) {
    if (cond.has("requiredTags")) {
      Set<String> req = new HashSet<>();
      cond.get("requiredTags").forEach(n -> req.add(n.asText()));
      if (profile.getTcmTags() == null || !profile.getTcmTags().containsAll(req)) return false;
    }
    if (cond.has("minSleepMinutes")) {
      int min = cond.get("minSleepMinutes").asInt();
      if (summary.getSleepAvgMinutes() < min) return true; else return false;
    }
    if (cond.has("maxSaltDays")) {
      int max = cond.get("maxSaltDays").asInt();
      if (summary.getDietHighSaltDays() > max) return true; else return false;
    }
    return true;
  }

  private double score(CandidateItem ci, Preferences preferences, UserProfile profile) {
    double s = ci.getWeight() * ci.getConfidence();
    double penalty = 0.0;
    if (profile.getContraindications() != null) {
      for (String c : profile.getContraindications()) {
        if (ci.getTitle() != null && ci.getTitle().toLowerCase().contains(c.toLowerCase())) {
          penalty += lambdaPenalty;
        }
      }
    }
    double prefer = 0.0;
    if (preferences.getLiked() != null) {
      for (String p : preferences.getLiked()) {
        if (ci.getContent() != null && ci.getContent().toLowerCase().contains(p.toLowerCase())) prefer += lambdaPrefer;
      }
    }
    double distance = lambdaDistance * 0.1;
    double adherence = lambdaAdherence * 0.3;
    return s - penalty + prefer - distance + adherence;
  }

  private List<String> extractMatchedKeys(JsonNode cond) {
    List<String> keys = new ArrayList<>();
    cond.fieldNames().forEachRemaining(keys::add);
    return keys;
  }

  private List<CandidateItem> dedupByTitle(List<CandidateItem> items) {
    Map<String, CandidateItem> map = new LinkedHashMap<>();
    for (CandidateItem c : items) {
      String k = Optional.ofNullable(c.getTitle()).orElse("").toLowerCase();
      if (!map.containsKey(k)) map.put(k, c);
    }
    return new ArrayList<>(map.values());
  }
}
