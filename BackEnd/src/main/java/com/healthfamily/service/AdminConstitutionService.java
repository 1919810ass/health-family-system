package com.healthfamily.service;

import com.healthfamily.domain.constant.ConstitutionType;
import com.healthfamily.domain.entity.Constitution;
import com.healthfamily.domain.repository.ConstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminConstitutionService {

    private final ConstitutionRepository constitutionRepository;

    public List<Constitution> getAllConstitutions() {
        return constitutionRepository.findAll();
    }

    public Constitution getConstitutionByType(ConstitutionType type) {
        return constitutionRepository.findById(type).orElse(null);
    }

    public Constitution createOrUpdateConstitution(Constitution constitution) {
        // The primary key is the enum type, so save will handle both create and update.
        return constitutionRepository.save(constitution);
    }
}
