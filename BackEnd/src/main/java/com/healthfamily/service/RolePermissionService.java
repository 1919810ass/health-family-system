package com.healthfamily.service;

import com.healthfamily.domain.constant.Permission;
import com.healthfamily.domain.constant.UserRole;

/**
 * RolePermission服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
import java.util.Set;

public interface RolePermissionService {
    Set<Permission> getPermissions(UserRole role);
    boolean hasPermission(UserRole role, Permission permission);
}
