package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.AssessmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, Long> {
}
