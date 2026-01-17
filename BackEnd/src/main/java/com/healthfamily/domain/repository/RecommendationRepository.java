package com.healthfamily.domain.repository;

import com.healthfamily.domain.constant.RecommendationCategory;
import com.healthfamily.domain.entity.Recommendation;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {


    void deleteByUserAndForDate(User user, LocalDate targetDate);

    List<Recommendation> findByUserAndForDate(User user, LocalDate targetDate);

    Optional<Recommendation> findByIdAndUser(Long recommendationId, User user);

    Optional<Recommendation> findByUserAndForDateAndCategory(User user, LocalDate targetDate, RecommendationCategory category);
}

