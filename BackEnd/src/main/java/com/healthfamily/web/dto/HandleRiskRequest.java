package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 医生在工作台对高风险患者进行处置的请求体
 */
@Data
public class HandleRiskRequest {

    /**
     * 患者用户ID
     */
    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    /**
     * 处置方式，如：PHONE_CALL、ONLINE_CONSULTATION、IGNORE、MANUAL
     */
    private String handlingMethod;

    /**
     * 处置备注
     */
    private String notes;
}

