package com.healthfamily.domain.repository;

import com.healthfamily.domain.constant.RecommendationCategory;
import com.healthfamily.domain.entity.AiRecommendation;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AiRecommendationRepository extends JpaRepository<AiRecommendation, Long> {

    List<AiRecommendation> findByUserOrderByForDateDesc(User user);

    List<AiRecommendation> findByUserAndForDate(User user, LocalDate forDate);

    Optional<AiRecommendation> findByUserAndForDateAndCategory(User user, LocalDate forDate, RecommendationCategory category);

    List<AiRecommendation> findByUser_IdAndForDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}

