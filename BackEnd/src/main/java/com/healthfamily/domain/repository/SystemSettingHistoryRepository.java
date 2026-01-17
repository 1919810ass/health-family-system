package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.SystemSettingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemSettingHistoryRepository extends JpaRepository<SystemSettingHistory, Long> {
    List<SystemSettingHistory> findByKeyOrderByCreatedAtDesc(String key);
}
