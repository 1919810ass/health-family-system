package com.healthfamily.service;

import com.healthfamily.domain.constant.Permission;
import com.healthfamily.domain.constant.UserRole;

import java.util.Set;

public interface RolePermissionService {
    Set<Permission> getPermissions(UserRole role);
    boolean hasPermission(UserRole role, Permission permission);
}
