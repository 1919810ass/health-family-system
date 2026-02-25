package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.BadCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadCaseRepository extends JpaRepository<BadCase, Long> {
    List<BadCase> findBySessionId(Long sessionId);
}
