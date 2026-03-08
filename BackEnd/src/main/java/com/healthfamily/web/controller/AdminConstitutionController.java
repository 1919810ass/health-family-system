package com.healthfamily.web.controller;

import com.healthfamily.domain.constant.ConstitutionType;
import com.healthfamily.domain.entity.Constitution;
import com.healthfamily.service.AdminConstitutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/constitutions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminConstitutionController {

    private final AdminConstitutionService constitutionService;

    @GetMapping
    public List<Constitution> getAllConstitutions() {
        return constitutionService.getAllConstitutions();
    }

    @GetMapping("/{type}")
    public Constitution getConstitutionByType(@PathVariable ConstitutionType type) {
        return constitutionService.getConstitutionByType(type);
    }

    @PostMapping
    public Constitution createOrUpdateConstitution(@RequestBody Constitution constitution) {
        return constitutionService.createOrUpdateConstitution(constitution);
    }
}
