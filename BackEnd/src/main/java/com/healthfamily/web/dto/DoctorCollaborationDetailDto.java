package com.healthfamily.web.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DoctorCollaborationDetailDto {
    private Long id;
    private String nickname;
    private String phone;
    
    // 活跃会话列表
    private List<ConsultationSessionInfo> activeSessions;
    
    // 负责的健康计划列表 (概要)
    private List<HealthPlanInfo> activePlans;
    
    @Data
    public static class ConsultationSessionInfo {
        private Long sessionId;
        private String patientName;
        private String title;
        private LocalDateTime lastMessageAt;
        private String status;
    }
    
    @Data
    public static class HealthPlanInfo {
        private Long planId;
        private String patientName;
        private String title;
        private String type;
        private String status;
    }
}
