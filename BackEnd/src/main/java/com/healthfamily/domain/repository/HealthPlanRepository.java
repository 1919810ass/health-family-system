package com.healthfamily.domain.repository;

import com.healthfamily.domain.constant.HealthPlanStatus;
import com.healthfamily.domain.constant.HealthPlanType;
import com.healthfamily.domain.entity.Family;
import com.healthfamily.domain.entity.HealthPlan;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
/**
 * 健康计划数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import java.util.Optional;

public interface HealthPlanRepository extends JpaRepository<HealthPlan, Long> {

    /**
     * 根据医生和患者查询计划列表（按创建时间倒序）
     */
    List<HealthPlan> findByDoctorAndPatientOrderByCreatedAtDesc(User doctor, User patient);

    /**
     * 根据患者查询所有计划列表（按创建时间倒序）
     */
    List<HealthPlan> findByPatientOrderByCreatedAtDesc(User patient);

    /**
     * 根据家庭查询所有计划列表（按创建时间倒序）
     */
    List<HealthPlan> findByFamilyOrderByCreatedAtDesc(Family family);

    /**
     * 根据医生和家庭查询计划列表（按创建时间倒序）
     */
    List<HealthPlan> findByDoctorAndFamilyOrderByCreatedAtDesc(User doctor, Family family);

    /**
     * 根据ID和医生查询（用于权限验证）
     */
    Optional<HealthPlan> findByIdAndDoctor(Long id, User doctor);

    /**
     * 根据状态查询计划列表
     */
    List<HealthPlan> findByStatusOrderByCreatedAtDesc(HealthPlanStatus status);

    /**
     * 根据类型查询计划列表
     */
    List<HealthPlan> findByTypeOrderByCreatedAtDesc(HealthPlanType type);

    /**
     * 根据患者和状态查询计划列表
     */
    List<HealthPlan> findByPatientAndStatusOrderByCreatedAtDesc(User patient, HealthPlanStatus status);

    /**
     * 根据患者、类型和状态查询计划列表
     */
    List<HealthPlan> findByPatientAndTypeAndStatusOrderByCreatedAtDesc(User patient, HealthPlanType type, HealthPlanStatus status);

    /**
     * 统计医生负责的计划总数
     */
    int countByDoctor_Id(Long doctorId);

    /**
     * 查询医生负责的所有计划
     */
    List<HealthPlan> findByDoctorOrderByCreatedAtDesc(User doctor);
}

