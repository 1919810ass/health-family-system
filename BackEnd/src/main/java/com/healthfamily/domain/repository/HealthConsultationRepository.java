package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.HealthConsultation;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HealthConsultationRepository extends JpaRepository<HealthConsultation, Long> {

    List<HealthConsultation> findByUserOrderByCreatedAtDesc(User user);

    List<HealthConsultation> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    List<HealthConsultation> findByUser_IdAndSessionIdOrderByCreatedAtAsc(Long userId, String sessionId);
}

