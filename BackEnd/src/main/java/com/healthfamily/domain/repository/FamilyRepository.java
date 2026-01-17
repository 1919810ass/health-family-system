package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FamilyRepository extends JpaRepository<Family, Long> {

    Optional<Family> findByInviteCode(String inviteCode);

    boolean existsByOwner_Id(Long ownerId);

    List<Family> findByOwner_Id(Long ownerId);
}

