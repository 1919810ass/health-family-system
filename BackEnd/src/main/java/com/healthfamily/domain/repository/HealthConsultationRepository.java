package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.HealthConsultation;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
/**
 * 健康问诊数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import java.util.Optional;

public interface HealthConsultationRepository extends JpaRepository<HealthConsultation, Long> {

    List<HealthConsultation> findByUserOrderByCreatedAtDesc(User user);

    List<HealthConsultation> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    List<HealthConsultation> findByUser_IdAndSessionIdOrderByCreatedAtAsc(Long userId, String sessionId);
}

