package com.healthfamily.web.model.request;

import lombok.Data;

@Data
public class AssessmentQueryRequest {
    private int page = 1;
    private int size = 10;
    private String userId;
    private String constitutionType;
    private String startDate;
    private String endDate;
}
