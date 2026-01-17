package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.TcmPersonalizedPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TcmPersonalizedPlanRepository extends JpaRepository<TcmPersonalizedPlan, Long> {
    List<TcmPersonalizedPlan> findByUserIdOrderByGeneratedAtDesc(Long userId);
    
    TcmPersonalizedPlan findTopByUserIdOrderByGeneratedAtDesc(Long userId);
}