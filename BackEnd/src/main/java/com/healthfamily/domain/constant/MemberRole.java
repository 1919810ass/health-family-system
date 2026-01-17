package com.healthfamily.domain.constant;

/**
 * 家庭成员角色
 * ADMIN: 家庭管理员，可管理家庭信息和所有成员
 * MEMBER: 普通成员，可查看和编辑自己的数据
 * VISITOR: 临时访客（如家庭医生），仅可查看共享的健康数据
 */
public enum MemberRole {
    ADMIN,
    MEMBER,
    VISITOR
}

