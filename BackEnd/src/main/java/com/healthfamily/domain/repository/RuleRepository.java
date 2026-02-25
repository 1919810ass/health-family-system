package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 规则数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import java.util.List;

public interface RuleRepository extends JpaRepository<Rule, Long> {

    List<Rule> findByEnabledTrue();
}

