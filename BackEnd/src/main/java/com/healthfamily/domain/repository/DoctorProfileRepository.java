package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {

    /**
     * 根据医生ID查找医生信息
     */
    Optional<DoctorProfile> findByDoctorId(Long doctorId);

    /**
     * 根据认证状态查找医生列表
     */
    List<DoctorProfile> findByCertificationStatus(String certificationStatus);

    /**
     * 根据医院名称查找医生列表
     */
    @Query("SELECT p FROM DoctorProfile p WHERE p.hospital LIKE %:hospital%")
    List<DoctorProfile> findByHospitalContaining(@Param("hospital") String hospital);

    /**
     * 根据专业领域查找医生列表
     */
    @Query("SELECT p FROM DoctorProfile p WHERE p.specialty LIKE %:specialty%")
    List<DoctorProfile> findBySpecialtyContaining(@Param("specialty") String specialty);

    /**
     * 根据科室查找医生列表
     */
    @Query("SELECT p FROM DoctorProfile p WHERE p.department LIKE %:department%")
    List<DoctorProfile> findByDepartmentContaining(@Param("department") String department);
}



