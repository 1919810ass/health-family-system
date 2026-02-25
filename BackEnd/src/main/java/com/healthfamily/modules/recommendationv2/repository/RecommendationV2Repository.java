package com.healthfamily.modules.recommendationv2.repository;

import com.healthfamily.modules.recommendationv2.domain.RecommendationV2;
import org.springframework.data.jpa.repository.JpaRepository;
import java.sql.Date;
import java.util.Optional;
/**
 * 推荐V2数据访问接口
 * <p>
 * 属于业务子模块的核心组件，用于承载该模块的领域模型与服务逻辑。
 * </p>
 */
import java.util.List;

public interface RecommendationV2Repository extends JpaRepository<RecommendationV2, Long> {
  Optional<RecommendationV2> findByUserIdAndDate(Long userId, Date date);
  List<RecommendationV2> findByDateBetween(Date from, Date to);
}
