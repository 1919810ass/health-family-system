package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 系统Setting数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import java.util.Optional;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {
    Optional<SystemSetting> findByKey(String key);
}
