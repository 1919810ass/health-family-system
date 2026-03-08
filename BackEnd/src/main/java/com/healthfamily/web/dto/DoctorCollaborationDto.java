package com.healthfamily.web.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DoctorCollaborationDto {
    private Long id;
    private String nickname;
    private String phone;
    private Integer activeConsultations; // 正在进行的咨询
    private Integer totalHealthPlans;    // 负责的健康计划总数
    private Integer unreadMessages;      // 未读消息数 (给医生)
    private LocalDateTime lastActivityAt; // 最后活跃时间
    private String collaborationStatus;   // 协作状态 (e.g., "忙碌", "空闲", "离线")
}
