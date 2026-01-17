package com.healthfamily.service;

import com.healthfamily.web.dto.AssessmentHistoryResponse;
import com.healthfamily.web.dto.AssessmentResponse;
import com.healthfamily.web.dto.AssessmentSchemaResponse;
import com.healthfamily.web.dto.AssessmentSubmitRequest;
import com.healthfamily.web.dto.FamilyMemberLatestResponse;
import com.healthfamily.web.dto.TcmPersonalizedPlanResponse;
import com.healthfamily.web.dto.ConstitutionTrendResponse;
import com.healthfamily.web.dto.FamilyTcmHealthOverviewResponse;

import java.util.List;
import java.util.Map;

public interface AssessmentService {

    AssessmentSchemaResponse getSchema();

    AssessmentResponse submitAssessment(Long userId, AssessmentSubmitRequest request);

    AssessmentResponse getAssessment(Long userId, Long assessmentId);

    List<AssessmentHistoryResponse> listHistory(Long userId);

    java.util.List<FamilyMemberLatestResponse> listFamilyLatest(Long userId, Long familyId);
    
    TcmPersonalizedPlanResponse getPersonalizedPlan(Long userId);
    
    ConstitutionTrendResponse getConstitutionTrend(Long userId, int lookbackDays);
    
    FamilyTcmHealthOverviewResponse getFamilyHealthOverview(Long familyId, Long userId);
    
    // AI驱动的体质测评
    Map<String, Object> startAiAssessment(Long userId);
    
    Map<String, Object> processAiAnswer(Long userId, String sessionId, String userAnswer);
    
    Map<String, Object> generateFinalAiAssessment(Long userId, String sessionId, String finalAnswers);
}

