package com.healthfamily.recommendation;

import com.healthfamily.modules.recommendationv2.domain.Rule;
import com.healthfamily.modules.recommendationv2.repository.RuleRepository;
import com.healthfamily.modules.recommendationv2.service.RuleEngine;
import com.healthfamily.modules.recommendationv2.service.model.LogsSummary;
import com.healthfamily.modules.recommendationv2.service.model.Preferences;
import com.healthfamily.modules.recommendationv2.service.model.UserProfile;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RuleEngineTest {
  @Test
  void testGenerate() {
    RuleRepository repo = Mockito.mock(RuleRepository.class);
    Rule r = new Rule();
    r.setId(1L);
    r.setCategory(Rule.Category.DIET);
    r.setConditionJson("{\"maxSaltDays\":3}");
    r.setActionTemplate("{\"title\":\"低盐饮食\",\"content\":\"减少盐摄入\",\"steps\":[\"少盐\"]}");
    r.setWeight(1.0);
    r.setSource("rule");
    r.setStatus(Rule.Status.ENABLED);
    Mockito.when(repo.findByCategoryAndStatus(Rule.Category.DIET, Rule.Status.ENABLED)).thenReturn(List.of(r));
    Mockito.when(repo.findByCategoryAndStatus(Rule.Category.SLEEP, Rule.Status.ENABLED)).thenReturn(List.of());
    Mockito.when(repo.findByCategoryAndStatus(Rule.Category.SPORT, Rule.Status.ENABLED)).thenReturn(List.of());
    Mockito.when(repo.findByCategoryAndStatus(Rule.Category.MOOD, Rule.Status.ENABLED)).thenReturn(List.of());
    Mockito.when(repo.findByCategoryAndStatus(Rule.Category.VITALS, Rule.Status.ENABLED)).thenReturn(List.of());
    RuleEngine engine = new RuleEngine(repo,0.7,0.5,0.2,0.3);
    UserProfile p = new UserProfile();
    p.setTcmTags(java.util.List.of("Qi-deficiency"));
    p.setContraindications(java.util.List.of("alcohol"));
    LogsSummary s = new LogsSummary();
    s.setDietHighSaltDays(4);
    Preferences pref = new Preferences();
    pref.setLiked(java.util.List.of("fruit"));
    var list = engine.generate(p,s,pref,6);
    assertFalse(list.isEmpty());
    assertEquals("低盐饮食", list.get(0).getTitle());
  }
}
