package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleRepository extends JpaRepository<Rule, Long> {

    List<Rule> findByEnabledTrue();
}

