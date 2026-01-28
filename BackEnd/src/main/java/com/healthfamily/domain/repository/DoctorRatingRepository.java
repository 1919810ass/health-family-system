package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.DoctorRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRatingRepository extends JpaRepository<DoctorRating, Long> {
    List<DoctorRating> findByDoctorId(Long doctorId);
    
    // 检查用户是否已评价过该医生（可选，如果限制一次评价）
    boolean existsByDoctorIdAndUserId(Long doctorId, Long userId);
}
