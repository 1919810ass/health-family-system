package com.healthfamily.modules.recommendationv2.repository;

import com.healthfamily.modules.recommendationv2.domain.RecommendationV2;
import org.springframework.data.jpa.repository.JpaRepository;
import java.sql.Date;
import java.util.Optional;
import java.util.List;

public interface RecommendationV2Repository extends JpaRepository<RecommendationV2, Long> {
  Optional<RecommendationV2> findByUserIdAndDate(Long userId, Date date);
  List<RecommendationV2> findByDateBetween(Date from, Date to);
}
