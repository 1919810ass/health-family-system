package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.ConstitutionAssessment;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Constitution测评数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import java.util.List;

public interface ConstitutionAssessmentRepository extends JpaRepository<ConstitutionAssessment, Long> {

    List<ConstitutionAssessment> findByUserOrderByCreatedAtDesc(User user);

    long countByUser_IdIn(List<Long> userIds);
}

