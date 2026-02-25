package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.SystemSettingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 系统SettingHistory数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
@Repository
public interface SystemSettingHistoryRepository extends JpaRepository<SystemSettingHistory, Long> {
    List<SystemSettingHistory> findByKeyOrderByCreatedAtDesc(String key);
}
