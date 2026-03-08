package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
/**
 * 家庭数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import java.util.Optional;

public interface FamilyRepository extends JpaRepository<Family, Long>, JpaSpecificationExecutor<Family> {

    Optional<Family> findByInviteCode(String inviteCode);

    boolean existsByOwner_Id(Long ownerId);

    List<Family> findByOwner_Id(Long ownerId);
}

