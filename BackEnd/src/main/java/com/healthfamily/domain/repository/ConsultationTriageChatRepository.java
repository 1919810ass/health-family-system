package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.ConsultationTriageChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 问诊TriageChat数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
@Repository
public interface ConsultationTriageChatRepository extends JpaRepository<ConsultationTriageChat, Long> {
    
    List<ConsultationTriageChat> findBySessionIdOrderByGmtCreateAsc(Long sessionId);
}
