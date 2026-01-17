package com.healthfamily.service;

import com.healthfamily.web.dto.ConsultationRequest;
import com.healthfamily.web.dto.ConsultationResponse;

import java.util.List;

/**
 * 智能健康咨询服务
 */
public interface HealthConsultationService {

    /**
     * 健康咨询（支持上下文对话）
     * @param userId 用户ID
     * @param request 咨询请求
     * @return 咨询响应
     */
    ConsultationResponse consult(Long userId, ConsultationRequest request);

    /**
     * 获取咨询历史
     * @param userId 用户ID
     * @param sessionId 会话ID（可选）
     * @return 咨询历史列表
     */
    List<ConsultationResponse> getHistory(Long userId, String sessionId);

    /**
     * 反馈咨询质量
     * @param consultationId 咨询记录ID
     * @param userId 用户ID
     * @param feedback 反馈：1有用 0无用
     */
    void feedback(Long consultationId, Long userId, Integer feedback);
}

