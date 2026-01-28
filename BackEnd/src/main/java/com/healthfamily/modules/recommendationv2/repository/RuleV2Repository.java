package com.healthfamily.modules.recommendationv2.repository;

import com.healthfamily.modules.recommendationv2.domain.RuleV2;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RuleV2Repository extends JpaRepository<RuleV2, Long> {
  List<RuleV2> findByCategoryAndStatus(RuleV2.Category category, RuleV2.Status status);
}
