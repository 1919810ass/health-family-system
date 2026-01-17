package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.ConstitutionAssessment;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConstitutionAssessmentRepository extends JpaRepository<ConstitutionAssessment, Long> {

    List<ConstitutionAssessment> findByUserOrderByCreatedAtDesc(User user);

    long countByUser_IdIn(List<Long> userIds);
}

