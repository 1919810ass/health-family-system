package com.healthfamily.web.controller;

import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.OnlineConsultationService;
import com.healthfamily.web.dto.ConsultationMessageRequest;
import com.healthfamily.web.dto.ConsultationMessageResponse;
import com.healthfamily.web.dto.ConsultationSessionResponse;
import com.healthfamily.web.dto.CreateSessionRequest;
import com.healthfamily.web.dto.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在线咨询Controller
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/consultations")
@Slf4j
public class OnlineConsultationController {

    private final OnlineConsultationService consultationService;

    /**
     * 获取或创建会话（按患者维度）
     * GET /api/consultations/sessions?patientUserId={patientUserId}&familyId={familyId}
     */
    @GetMapping("/sessions")
    public Result<ConsultationSessionResponse> getOrCreateSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("patientUserId") Long patientUserId,
            @RequestParam("familyId") Long familyId) {
        Long requesterId = principal.getUserId();
        try {
            return Result.success(consultationService.getOrCreateSession(requesterId, patientUserId, familyId));
        } catch (Exception e) {
            log.error("获取或创建会话失败: patientUserId={}, familyId={}, requesterId={}", patientUserId, familyId, requesterId, e);
            return Result.error(500, "操作失败");
        }
    }

    /**
     * 创建会话
     * POST /api/consultations/sessions
     */
    @PostMapping("/sessions")
    public Result<ConsultationSessionResponse> createSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateSessionRequest request) {
        Long requesterId = principal.getUserId();
        try {
            return Result.success(consultationService.createSession(requesterId, request));
        } catch (Exception e) {
            log.error("创建会话失败: requesterId={}", requesterId, e);
            return Result.error(500, "创建会话失败");
        }
    }

    /**
     * 获取会话列表（医生端）
     * GET /api/consultations/sessions/doctor?familyId={familyId}
     */
    @GetMapping("/sessions/doctor")
    public Result<List<ConsultationSessionResponse>> listSessionsForDoctor(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("familyId") Long familyId) {
        Long doctorId = principal.getUserId();
        try {
            return Result.success(consultationService.listSessionsForDoctor(doctorId, familyId));
        } catch (Exception e) {
            log.error("获取医生会话列表失败: doctorId={}, familyId={}", doctorId, familyId, e);
            return Result.error(500, "获取会话列表失败");
        }
    }

    /**
     * 获取会话列表（患者端/家庭端）
     * GET /api/consultations/sessions/patient?familyId={familyId}
     */
    @GetMapping("/sessions/patient")
    public Result<List<ConsultationSessionResponse>> listSessionsForPatient(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("familyId") Long familyId) {
        Long patientUserId = principal.getUserId();
        try {
            return Result.success(consultationService.listSessionsForPatient(patientUserId, familyId));
        } catch (Exception e) {
            log.error("获取患者会话列表失败: patientUserId={}, familyId={}", patientUserId, familyId, e);
            return Result.error(500, "获取会话列表失败");
        }
    }

    /**
     * 获取会话详情
     * GET /api/consultations/sessions/{sessionId}
     */
    @GetMapping("/sessions/{sessionId}")
    public Result<ConsultationSessionResponse> getSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("sessionId") Long sessionId) {
        Long requesterId = principal.getUserId();
        try {
            return Result.success(consultationService.getSession(requesterId, sessionId));
        } catch (Exception e) {
            log.error("获取会话详情失败: sessionId={}, requesterId={}", sessionId, requesterId, e);
            return Result.error(500, "获取会话详情失败");
        }
    }

    /**
     * 获取会话消息列表
     * GET /api/consultations/sessions/{sessionId}/messages
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ConsultationMessageResponse>> getMessages(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("sessionId") Long sessionId) {
        Long requesterId = principal.getUserId();
        try {
            return Result.success(consultationService.getMessages(requesterId, sessionId));
        } catch (Exception e) {
            log.error("获取消息列表失败: sessionId={}, requesterId={}", sessionId, requesterId, e);
            return Result.error(500, "获取消息列表失败");
        }
    }

    /**
     * 发送消息
     * POST /api/consultations/messages
     */
    @PostMapping("/messages")
    public Result<ConsultationMessageResponse> sendMessage(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ConsultationMessageRequest request) {
        Long requesterId = principal.getUserId();
        try {
            return Result.success(consultationService.sendMessage(requesterId, request));
        } catch (Exception e) {
            log.error("发送消息失败: requesterId={}", requesterId, e);
            return Result.error(500, "发送消息失败");
        }
    }

    /**
     * 标记消息为已读
     * POST /api/consultations/sessions/{sessionId}/read
     */
    @PostMapping("/sessions/{sessionId}/read")
    public Result<Void> markMessagesAsRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("sessionId") Long sessionId) {
        Long requesterId = principal.getUserId();
        try {
            consultationService.markMessagesAsRead(requesterId, sessionId);
            return Result.success();
        } catch (Exception e) {
            log.error("标记消息已读失败: sessionId={}, requesterId={}", sessionId, requesterId, e);
            return Result.error(500, "操作失败");
        }
    }

    /**
     * 关闭会话
     * POST /api/consultations/sessions/{sessionId}/close
     */
    @PostMapping("/sessions/{sessionId}/close")
    public Result<Void> closeSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("sessionId") Long sessionId) {
        Long requesterId = principal.getUserId();
        try {
            consultationService.closeSession(requesterId, sessionId);
            return Result.success();
        } catch (Exception e) {
            log.error("关闭会话失败: sessionId={}, requesterId={}", sessionId, requesterId, e);
            return Result.error(500, "操作失败");
        }
    }
}

