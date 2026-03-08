package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.ConsultationMessage;
import com.healthfamily.domain.entity.ConsultationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 问诊Message数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import java.util.List;

public interface ConsultationMessageRepository extends JpaRepository<ConsultationMessage, Long> {

    /**
     * 根据会话查找所有消息（按创建时间正序）
     */
    List<ConsultationMessage> findBySessionOrderByCreatedAtAsc(ConsultationSession session);

    /**
     * 根据会话ID查找所有消息（按创建时间正序）
     */
    @Query("SELECT m FROM ConsultationMessage m WHERE m.session.id = :sessionId ORDER BY m.createdAt ASC")
    List<ConsultationMessage> findBySessionIdOrderByCreatedAtAsc(@Param("sessionId") Long sessionId);

    /**
     * 统计会话中未读消息数（医生端）
     */
    @Query("SELECT COUNT(m) FROM ConsultationMessage m WHERE m.session = :session AND m.readByDoctor = false AND m.senderType != 'DOCTOR'")
    long countUnreadByDoctor(@Param("session") ConsultationSession session);

    /**
     * 统计会话中未读消息数（患者端）
     */
    @Query("SELECT COUNT(m) FROM ConsultationMessage m WHERE m.session = :session AND m.readByPatient = false AND m.senderType = 'DOCTOR'")
    long countUnreadByPatient(@Param("session") ConsultationSession session);

    /**
     * 标记会话中所有消息为已读（医生端）
     */
    @Modifying
    @Query("UPDATE ConsultationMessage m SET m.readByDoctor = true WHERE m.session = :session AND m.readByDoctor = false AND m.senderType != 'DOCTOR'")
    void markAllReadByDoctor(@Param("session") ConsultationSession session);

    /**
     * 标记会话中所有消息为已读（患者端）
     */
    @Modifying
    @Query("UPDATE ConsultationMessage m SET m.readByPatient = true WHERE m.session = :session AND m.readByPatient = false AND m.senderType = 'DOCTOR'")
    void markAllReadByPatient(@Param("session") ConsultationSession session);
}

