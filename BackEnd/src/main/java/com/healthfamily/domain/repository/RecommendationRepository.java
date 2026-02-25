package com.healthfamily.domain.repository;

import com.healthfamily.domain.constant.RecommendationCategory;
import com.healthfamily.domain.entity.Recommendation;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
/**
 * 推荐数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import java.util.Optional;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {


    void deleteByUserAndForDate(User user, LocalDate targetDate);

    List<Recommendation> findByUserAndForDate(User user, LocalDate targetDate);

    Optional<Recommendation> findByIdAndUser(Long recommendationId, User user);

    Optional<Recommendation> findByUserAndForDateAndCategory(User user, LocalDate targetDate, RecommendationCategory category);
}

