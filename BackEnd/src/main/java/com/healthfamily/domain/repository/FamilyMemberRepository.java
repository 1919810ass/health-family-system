package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.Family;
import com.healthfamily.domain.entity.FamilyMember;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
/**
 * 家庭Member数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import java.util.Optional;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<FamilyMember> findByFamily(Family family);

    @EntityGraph(attributePaths = {"family"})
    List<FamilyMember> findByUser(User user);

    @Query("select fm.family from FamilyMember fm where fm.user = :user")
    List<Family> findFamiliesByUser(@Param("user") User user);

    Optional<FamilyMember> findByFamilyAndUser(Family family, User user);

    long countByFamily_Id(Long familyId);

    @Query("select fm.user.id from FamilyMember fm where fm.family.id = :familyId")
    List<Long> findUserIdsByFamilyId(@Param("familyId") Long familyId);

    long countByFamilyIdIn(List<Long> familyIds);
}
