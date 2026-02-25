package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.Family;
import com.healthfamily.domain.entity.FamilyDoctor;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 家庭医生数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import java.util.List;

public interface FamilyDoctorRepository extends JpaRepository<FamilyDoctor, Long> {
    List<FamilyDoctor> findByDoctor(User doctor);
    List<FamilyDoctor> findByFamily(Family family);
    List<FamilyDoctor> findByDoctor_Id(Long doctorId);

    long countByDoctorAndCreatedAtAfter(User doctor, java.time.LocalDateTime createdAt);
}
