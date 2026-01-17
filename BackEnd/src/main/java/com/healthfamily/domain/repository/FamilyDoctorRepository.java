package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.Family;
import com.healthfamily.domain.entity.FamilyDoctor;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FamilyDoctorRepository extends JpaRepository<FamilyDoctor, Long> {
    List<FamilyDoctor> findByDoctor(User doctor);
    List<FamilyDoctor> findByFamily(Family family);
    List<FamilyDoctor> findByDoctor_Id(Long doctorId);
}
