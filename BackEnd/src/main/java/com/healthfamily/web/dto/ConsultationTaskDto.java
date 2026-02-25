package com.healthfamily.web.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 待处理咨询任务
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationTaskDto {

    private Long sessionId;
    private String patientName;
    private String avatarUrl; // 患者头像
    private String requestSummary; // 患者诉求摘要
    private String suggestedDepartment; // AI建议科室
    private String waitingTime; // 等待时长 (e.g., "15分钟")

}
