package com.healthfamily.recommendation;

import com.healthfamily.modules.recommendationv2.repository.RecommendationRepository;
import com.healthfamily.modules.recommendationv2.repository.RuleRepository;
import com.healthfamily.modules.recommendationv2.repository.DocFragmentRepository;
import com.healthfamily.modules.recommendationv2.service.DocRagService;
import com.healthfamily.modules.recommendationv2.service.RecommendationService;
import com.healthfamily.modules.recommendationv2.service.RuleEngine;
import com.healthfamily.modules.recommendationv2.dto.RecommendationResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class RecommendationServiceDegradeTest {
  @Test
  void testDegradeToRulesOnly() {
    RuleRepository rrepo = Mockito.mock(RuleRepository.class);
    RecommendationRepository recRepo = Mockito.mock(RecommendationRepository.class);
    DocRagService rag = new DocRagService(Mockito.mock(DocFragmentRepository.class),4,0.35);
    RuleEngine engine = new RuleEngine(rrepo,0.7,0.5,0.2,0.3);
    com.healthfamily.modules.recommendationv2.repository.SuggestionFeedbackRepository fbrepo = Mockito.mock(com.healthfamily.modules.recommendationv2.repository.SuggestionFeedbackRepository.class);
    com.healthfamily.domain.repository.ProfileRepository profileRepo = Mockito.mock(com.healthfamily.domain.repository.ProfileRepository.class);
    com.healthfamily.domain.repository.HealthLogRepository logRepo = Mockito.mock(com.healthfamily.domain.repository.HealthLogRepository.class);
    RecommendationService svc = new RecommendationService(engine, rag, recRepo, fbrepo, profileRepo, logRepo, 10, false);
    RecommendationResponse res = svc.generate(1L, Date.valueOf(LocalDate.now()), 7, 6, false);
    assertNotNull(res.getItems());
    assertFalse(Boolean.TRUE.equals(res.getTelemetry().getAi()));
  }
}

