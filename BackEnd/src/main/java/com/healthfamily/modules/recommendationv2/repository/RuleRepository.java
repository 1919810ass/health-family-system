package com.healthfamily.modules.recommendationv2.repository;

import com.healthfamily.modules.recommendationv2.domain.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RuleRepository extends JpaRepository<Rule, Long> {
  List<Rule> findByCategoryAndStatus(Rule.Category category, Rule.Status status);
}
