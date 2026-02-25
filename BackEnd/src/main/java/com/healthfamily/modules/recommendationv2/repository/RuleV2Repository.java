package com.healthfamily.modules.recommendationv2.repository;

import com.healthfamily.modules.recommendationv2.domain.RuleV2;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * 规则V2数据访问接口
 * <p>
 * 属于业务子模块的核心组件，用于承载该模块的领域模型与服务逻辑。
 * </p>
 */
import java.util.List;

public interface RuleV2Repository extends JpaRepository<RuleV2, Long> {
  List<RuleV2> findByCategoryAndStatus(RuleV2.Category category, RuleV2.Status status);
}
