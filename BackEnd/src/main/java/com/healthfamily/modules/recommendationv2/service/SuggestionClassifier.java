package com.healthfamily.modules.recommendationv2.service;

import com.healthfamily.modules.recommendationv2.domain.Rule;

import java.util.Locale;

public class SuggestionClassifier {
  public static Rule.Category detect(String text) {
    if (text == null) return Rule.Category.DIET;
    String t = text.toLowerCase(Locale.ROOT);
    if (containsAny(t, new String[]{"饮食","餐","膳食","菜","食谱","热量","卡路里","低盐","低脂","高蛋白"})) return Rule.Category.DIET;
    if (containsAny(t, new String[]{"睡","作息","打盹","熬夜","失眠","休息","早睡"})) return Rule.Category.SLEEP;
    if (containsAny(t, new String[]{"运动","跑","走","步数","健身","拉伸","有氧","力量"})) return Rule.Category.SPORT;
    if (containsAny(t, new String[]{"情绪","压力","焦虑","抑郁","心情","冥想","放松"})) return Rule.Category.MOOD;
    if (containsAny(t, new String[]{"血压","血糖","体温","心率","体重","BMI","体征"})) return Rule.Category.VITALS;
    return Rule.Category.DIET;
  }

  private static boolean containsAny(String text, String[] keys) {
    for (String k : keys) {
      if (text.contains(k)) return true;
    }
    return false;
  }
}