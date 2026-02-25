package com.healthfamily.modules.recommendationv2.repository;

import com.healthfamily.modules.recommendationv2.domain.SuggestionFeedback;
/**
 * SuggestionFeedback数据访问接口
 * <p>
 * 属于业务子模块的核心组件，用于承载该模块的领域模型与服务逻辑。
 * </p>
 */
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuggestionFeedbackRepository extends JpaRepository<SuggestionFeedback, Long> {
}
