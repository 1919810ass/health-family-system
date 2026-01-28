package com.healthfamily.recommendation;

import com.healthfamily.modules.recommendationv2.repository.RecommendationV2Repository;
import com.healthfamily.modules.recommendationv2.repository.RuleV2Repository;
import com.healthfamily.modules.recommendationv2.repository.SuggestionFeedbackRepository;
import com.healthfamily.modules.recommendationv2.repository.DocFragmentRepository;
import com.healthfamily.modules.recommendationv2.service.DocRagService;
import com.healthfamily.modules.recommendationv2.service.RecommendationService;
import com.healthfamily.modules.recommendationv2.service.RuleEngine;
import com.healthfamily.modules.recommendationv2.dto.RecommendationResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.vectorstore.VectorStore;

import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class RecommendationServiceDegradeTest {
  @Test
  void testDegradeToRulesOnly() {
    RuleV2Repository rrepo = Mockito.mock(RuleV2Repository.class);
    RecommendationV2Repository recRepo = Mockito.mock(RecommendationV2Repository.class);
    VectorStore vectorStore = Mockito.mock(VectorStore.class);
    DocRagService rag = new DocRagService(Mockito.mock(DocFragmentRepository.class), vectorStore, 4, 0.35, "./vector-store.json");
    RuleEngine engine = new RuleEngine(rrepo,0.7,0.5,0.2,0.3);
    com.healthfamily.modules.recommendationv2.repository.SuggestionFeedbackRepository fbrepo = Mockito.mock(com.healthfamily.modules.recommendationv2.repository.SuggestionFeedbackRepository.class);
    com.healthfamily.domain.repository.ProfileRepository profileRepo = Mockito.mock(com.healthfamily.domain.repository.ProfileRepository.class);
    com.healthfamily.domain.repository.HealthLogRepository logRepo = Mockito.mock(com.healthfamily.domain.repository.HealthLogRepository.class);
    com.healthfamily.domain.repository.ConstitutionAssessmentRepository constitutionRepo = Mockito.mock(com.healthfamily.domain.repository.ConstitutionAssessmentRepository.class);
    RecommendationService svc = new RecommendationService(engine, rag, recRepo, fbrepo, profileRepo, logRepo, constitutionRepo, 10, false);
    RecommendationResponse res = svc.generate(1L, Date.valueOf(LocalDate.now()), 7, 6, false);
    assertNotNull(res.getItems());
    assertFalse(Boolean.TRUE.equals(res.getTelemetry().getAi()));
  }
}

