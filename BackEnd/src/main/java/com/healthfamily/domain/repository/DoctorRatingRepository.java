package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.DoctorRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 医生Rating数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
@Repository
public interface DoctorRatingRepository extends JpaRepository<DoctorRating, Long> {
    List<DoctorRating> findByDoctorId(Long doctorId);
    
    // 检查用户是否已评价过该医生（可选，如果限制一次评价）
    boolean existsByDoctorIdAndUserId(Long doctorId, Long userId);
}
