package com.healthfamily.security;

import com.healthfamily.domain.entity.Family;
import com.healthfamily.domain.entity.FamilyDoctor;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.FamilyDoctorRepository;
import com.healthfamily.domain.repository.FamilyMemberRepository;
import com.healthfamily.domain.repository.FamilyRepository;
import com.healthfamily.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessGuard {

    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final FamilyDoctorRepository familyDoctorRepository;

    public void assertFamilyAccess(Long requesterId, Long familyId) {
        User user = userRepository.findById(requesterId).orElseThrow(() -> new RuntimeException("用户不存在"));
        Family family = familyRepository.findById(familyId).orElseThrow(() -> new RuntimeException("家庭不存在"));
        
        // 检查用户是否是家庭成员
        boolean member = familyMemberRepository.findByFamilyAndUser(family, user).isPresent();
        
        // 检查用户是否是家庭医生
        boolean doctor = !familyDoctorRepository.findByDoctor(user).stream().filter(fd -> fd.getFamily().getId().equals(familyId)).findFirst().isEmpty();
        
        // 检查用户是否是家庭管理员（家庭所有者）
        boolean isAdmin = family.getOwner().getId().equals(requesterId);
        
        if (!member && !doctor && !isAdmin) throw new RuntimeException("无权访问家庭数据");
    }
}

