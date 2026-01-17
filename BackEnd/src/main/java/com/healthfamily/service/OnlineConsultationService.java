package com.healthfamily.service;

import com.healthfamily.web.dto.ConsultationMessageRequest;
import com.healthfamily.web.dto.ConsultationMessageResponse;
import com.healthfamily.web.dto.ConsultationSessionResponse;
import com.healthfamily.web.dto.CreateSessionRequest;

import java.util.List;

/**
 * 在线咨询服务接口
 */
public interface OnlineConsultationService {

    /**
     * 获取或创建会话（按患者维度）
     */
    ConsultationSessionResponse getOrCreateSession(Long requesterId, Long patientUserId, Long familyId);

    /**
     * 创建会话
     */
    ConsultationSessionResponse createSession(Long requesterId, CreateSessionRequest request);

    /**
     * 获取会话列表（医生端）
     */
    List<ConsultationSessionResponse> listSessionsForDoctor(Long doctorId, Long familyId);

    /**
     * 获取会话列表（患者端/家庭端）
     */
    List<ConsultationSessionResponse> listSessionsForPatient(Long patientUserId, Long familyId);

    /**
     * 获取会话详情
     */
    ConsultationSessionResponse getSession(Long requesterId, Long sessionId);

    /**
     * 获取会话消息列表
     */
    List<ConsultationMessageResponse> getMessages(Long requesterId, Long sessionId);

    /**
     * 发送消息
     */
    ConsultationMessageResponse sendMessage(Long requesterId, ConsultationMessageRequest request);

    /**
     * 标记消息为已读
     */
    void markMessagesAsRead(Long requesterId, Long sessionId);

    /**
     * 关闭会话
     */
    void closeSession(Long requesterId, Long sessionId);
}

