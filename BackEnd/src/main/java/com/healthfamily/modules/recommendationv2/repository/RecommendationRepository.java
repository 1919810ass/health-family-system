package com.healthfamily.modules.recommendationv2.repository;

import com.healthfamily.modules.recommendationv2.domain.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.sql.Date;
import java.util.Optional;
import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
  Optional<Recommendation> findByUserIdAndDate(Long userId, Date date);
  List<Recommendation> findByDateBetween(Date from, Date to);
}
