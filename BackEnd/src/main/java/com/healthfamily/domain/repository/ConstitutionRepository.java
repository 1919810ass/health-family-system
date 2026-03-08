package com.healthfamily.domain.repository;

import com.healthfamily.domain.constant.ConstitutionType;
import com.healthfamily.domain.entity.Constitution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConstitutionRepository extends JpaRepository<Constitution, ConstitutionType> {
}
