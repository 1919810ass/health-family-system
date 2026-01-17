package com.healthfamily.service.impl;

import com.healthfamily.domain.constant.Permission;
import com.healthfamily.domain.constant.UserRole;
import com.healthfamily.service.RolePermissionService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
public class RolePermissionServiceImpl implements RolePermissionService {

    private final Map<UserRole, Set<Permission>> rolePermissions;

    public RolePermissionServiceImpl() {
        rolePermissions = new EnumMap<>(UserRole.class);
        
        // ADMIN
        rolePermissions.put(UserRole.ADMIN, EnumSet.allOf(Permission.class));
        
        // DOCTOR
        rolePermissions.put(UserRole.DOCTOR, EnumSet.of(
            Permission.USER_READ,
            Permission.HEALTH_DATA_READ,
            Permission.HEALTH_DATA_WRITE,
            Permission.PATIENT_READ,
            Permission.PATIENT_WRITE,
            Permission.FAMILY_READ
        ));
        
        // FAMILY_ADMIN
        rolePermissions.put(UserRole.FAMILY_ADMIN, EnumSet.of(
            Permission.FAMILY_READ,
            Permission.FAMILY_WRITE,
            Permission.HEALTH_DATA_READ,
            Permission.HEALTH_DATA_WRITE,
            Permission.USER_READ // Only own family
        ));
        
        // MEMBER
        rolePermissions.put(UserRole.MEMBER, EnumSet.of(
            Permission.FAMILY_READ,
            Permission.HEALTH_DATA_READ,
            Permission.HEALTH_DATA_WRITE
        ));
        
        // VIEWER
        rolePermissions.put(UserRole.VIEWER, EnumSet.of(
            Permission.FAMILY_READ,
            Permission.HEALTH_DATA_READ
        ));
    }

    @Override
    public Set<Permission> getPermissions(UserRole role) {
        return rolePermissions.getOrDefault(role, Collections.emptySet());
    }

    @Override
    public boolean hasPermission(UserRole role, Permission permission) {
        return getPermissions(role).contains(permission);
    }
}
