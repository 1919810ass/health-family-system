package com.healthfamily.web.model.response;

import lombok.Data;

import java.util.Date;

@Data
public class AssessmentResponse {
    private Long id;
    private UserInfo user;
    private String constitutionType;
    private Date assessmentDate;

    @Data
    public static class UserInfo {
        private String name;
        private String gender;
        private int age;
    }
}
