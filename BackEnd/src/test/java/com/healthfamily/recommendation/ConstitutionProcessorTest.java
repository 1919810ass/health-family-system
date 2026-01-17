package com.healthfamily.recommendation;

import com.healthfamily.domain.entity.ConstitutionAssessment;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.ConstitutionAssessmentRepository;
import com.healthfamily.modules.recommendationv2.service.ConstitutionProcessor;
import com.healthfamily.modules.recommendationv2.service.model.ConstitutionFeatures;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConstitutionProcessorTest {
  @Test
  void testLoadAndNormalize() {
    ConstitutionAssessmentRepository repo = Mockito.mock(ConstitutionAssessmentRepository.class);
    ConstitutionAssessment a1 = ConstitutionAssessment.builder()
            .user(User.builder().id(1L).build())
            .scoreVector("{\"balanced\":80,\"qi_deficiency\":60,\"phlegm_damp\":70}")
            .primaryType("qi_deficiency")
            .createdAt(LocalDateTime.now())
            .build();
    Mockito.when(repo.findByUserOrderByCreatedAtDesc(User.builder().id(1L).build())).thenReturn(List.of(a1));
    ConstitutionProcessor p = new ConstitutionProcessor(repo, 600000);
    ConstitutionFeatures f = p.load(1L, 5);
    assertTrue(f.isHasData());
    assertEquals(0.8, f.getBalanced(), 1e-6);
    assertEquals(0.6, f.getQiDeficiency(), 1e-6);
    assertEquals("qi_deficiency", f.getPrimaryType());
  }
}