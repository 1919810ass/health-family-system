package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.TcmPersonalizedPlan;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 中医体质Personalized计划数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import java.util.List;

public interface TcmPersonalizedPlanRepository extends JpaRepository<TcmPersonalizedPlan, Long> {
    List<TcmPersonalizedPlan> findByUserIdOrderByGeneratedAtDesc(Long userId);
    
    TcmPersonalizedPlan findTopByUserIdOrderByGeneratedAtDesc(Long userId);
}