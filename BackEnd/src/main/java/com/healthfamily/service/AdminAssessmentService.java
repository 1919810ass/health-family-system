package com.healthfamily.service;

import com.healthfamily.web.model.Paging;
import com.healthfamily.web.model.request.AssessmentQueryRequest;
import com.healthfamily.web.model.response.AssessmentResponse;

import java.util.Map;

public interface AdminAssessmentService {
    Paging<AssessmentResponse> getAssessments(AssessmentQueryRequest request);
    Map<String, Long> getAssessmentStats();
}
