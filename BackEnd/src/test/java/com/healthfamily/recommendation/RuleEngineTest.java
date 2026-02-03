package com.healthfamily.recommendation;

import com.healthfamily.modules.recommendationv2.domain.RuleV2;
import com.healthfamily.modules.recommendationv2.repository.RuleV2Repository;
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
    RuleV2Repository repo = Mockito.mock(RuleV2Repository.class);
    RuleV2 r = new RuleV2();
    r.setId(1L);
    r.setCategory(RuleV2.Category.DIET);
    r.setConditionJson("{\"maxSaltDays\":3}");
    r.setActionTemplate("{\"title\":\"低盐饮食\",\"content\":\"减少盐摄入\",\"steps\":[\"少盐\"]}");
    r.setWeight(1.0);
    r.setSource("rule");
    r.setStatus(RuleV2.Status.ENABLED);
    Mockito.when(repo.findByCategoryAndStatus(RuleV2.Category.DIET, RuleV2.Status.ENABLED)).thenReturn(List.of(r));
    Mockito.when(repo.findByCategoryAndStatus(RuleV2.Category.SLEEP, RuleV2.Status.ENABLED)).thenReturn(List.of());
    Mockito.when(repo.findByCategoryAndStatus(RuleV2.Category.SPORT, RuleV2.Status.ENABLED)).thenReturn(List.of());
    Mockito.when(repo.findByCategoryAndStatus(RuleV2.Category.MOOD, RuleV2.Status.ENABLED)).thenReturn(List.of());
    Mockito.when(repo.findByCategoryAndStatus(RuleV2.Category.VITALS, RuleV2.Status.ENABLED)).thenReturn(List.of());
    RuleEngine engine = new RuleEngine(repo, 0.7, 0.5, 0.2, 0.3);
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
