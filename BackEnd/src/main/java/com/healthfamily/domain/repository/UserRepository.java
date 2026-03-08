package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 用户数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);

    List<User> findByNicknameContainingOrPhoneContaining(String nickname, String phone);

    java.util.List<User> findByRole(com.healthfamily.domain.constant.UserRole role);
}

