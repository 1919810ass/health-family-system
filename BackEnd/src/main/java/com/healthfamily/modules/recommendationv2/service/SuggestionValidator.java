package com.healthfamily.modules.recommendationv2.service;

import com.healthfamily.modules.recommendationv2.dto.RecommendationItemDto;
import com.healthfamily.modules.recommendationv2.domain.RuleV2;
import com.healthfamily.modules.recommendationv2.service.model.Preferences;
import com.healthfamily.modules.recommendationv2.service.model.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SuggestionValidator {
  public static class Result {
    public double matchScore;
    public boolean categoryMismatch;
    public boolean contraindicationHit;
    public RuleV2.Category detected;
  }

  public static Result validate(RecommendationItemDto item, UserProfile profile, Preferences pref) {
    Result r = new Result();
    String text = (item.getTitle() + " " + item.getContent()).toLowerCase(Locale.ROOT);
    r.detected = SuggestionClassifier.detect(text);
    r.categoryMismatch = item.getCategory() != null && !item.getCategory().equalsIgnoreCase(r.detected.name());
    List<String> tags = NutritionMapper.extractTags(text);
    double score = 0.5;
    score += likedBoost(pref, text);
    score -= dislikedPenalty(pref, text);
    r.contraindicationHit = hitsContra(profile, text);
    if (r.contraindicationHit) score -= 0.4;
    if (!tags.isEmpty()) score += 0.1;
    if (score < 0) score = 0;
    if (score > 1) score = 1;
    r.matchScore = score;
    List<String> st = item.getSource_tags() != null ? new ArrayList<>(item.getSource_tags()) : new ArrayList<>();
    st.add("detected:" + r.detected.name());
    st.add("match:" + String.format(Locale.ROOT, "%.2f", r.matchScore));
    if (!tags.isEmpty()) st.add("nutrition:" + String.join("|", tags));
    item.setSource_tags(st);
    item.setRisk_level(r.contraindicationHit ? "HIGH" : "LOW");
    return r;
  }

  private static double likedBoost(Preferences pref, String text) {
    double b = 0.0;
    if (pref.getLiked() == null) return b;
    for (String k : pref.getLiked()) {
      if (text.contains(k.toLowerCase(Locale.ROOT))) b += 0.1;
    }
    return b;
  }

  private static double dislikedPenalty(Preferences pref, String text) {
    double p = 0.0;
    if (pref.getDisliked() == null) return p;
    for (String k : pref.getDisliked()) {
      if (text.contains(k.toLowerCase(Locale.ROOT))) p += 0.1;
    }
    return p;
  }

  private static boolean hitsContra(UserProfile profile, String text) {
    if (profile.getContraindications() == null) return false;
    for (String c : profile.getContraindications()) {
      if (text.contains(c.toLowerCase(Locale.ROOT))) return true;
    }
    return false;
  }
}