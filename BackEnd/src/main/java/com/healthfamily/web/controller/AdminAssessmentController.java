
package com.healthfamily.web.controller;

import com.healthfamily.model.Result;
import com.healthfamily.service.AdminAssessmentService;
import com.healthfamily.web.model.Paging;
import com.healthfamily.web.model.request.AssessmentQueryRequest;
import com.healthfamily.web.model.response.AssessmentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/health/assessments")
public class AdminAssessmentController {

    @Autowired
    private AdminAssessmentService adminAssessmentService;

    @GetMapping
    public Result<Paging<AssessmentResponse>> getAssessments(AssessmentQueryRequest request) {
        return Result.success(adminAssessmentService.getAssessments(request));
    }

    @GetMapping("/stats")
    public Result<Map<String, Long>> getAssessmentStats() {
        return Result.success(adminAssessmentService.getAssessmentStats());
    }
}
