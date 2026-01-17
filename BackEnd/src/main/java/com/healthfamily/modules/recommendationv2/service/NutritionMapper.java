package com.healthfamily.modules.recommendationv2.service;

import java.util.*;

public class NutritionMapper {
  private static final Map<String, String> dict = new HashMap<>();
  static {
    dict.put("低盐","low_salt");
    dict.put("少盐","low_salt");
    dict.put("清淡","low_fat");
    dict.put("低脂","low_fat");
    dict.put("高蛋白","high_protein");
    dict.put("蛋白质","high_protein");
    dict.put("高纤维","high_fiber");
    dict.put("蔬菜","high_fiber");
    dict.put("水果","high_fiber");
    dict.put("低GI","low_gi");
    dict.put("全谷物","low_gi");
    dict.put("低糖","low_sugar");
    dict.put("控糖","low_sugar");
    dict.put("少油","low_fat");
    dict.put("低钠","low_salt");
  }

  public static List<String> extractTags(String text) {
    if (text == null || text.isEmpty()) return List.of();
    String t = text;
    Set<String> tags = new LinkedHashSet<>();
    for (Map.Entry<String, String> e : dict.entrySet()) {
      if (t.contains(e.getKey())) tags.add(e.getValue());
    }
    return new ArrayList<>(tags);
  }
}