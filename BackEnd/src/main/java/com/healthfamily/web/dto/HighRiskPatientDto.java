package com.healthfamily.web.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 高风险患者信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HighRiskPatientDto {

    private Long patientId;
    private String name;
    private String avatar;
    private boolean hasVitalsAlert; // 是否有体征预警
    private String tcmType; // 中医体质类型
    private LocalDateTime lastAbnormalTime; // 最近一次异常时间

}
