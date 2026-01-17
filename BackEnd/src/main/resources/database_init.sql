-- ============================================================================
-- 数据库初始化脚本
-- ============================================================================

-- 1. 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) UNIQUE COMMENT '用户名',
    password_hash VARCHAR(128) NOT NULL COMMENT '加密密码',
    nickname VARCHAR(64) COMMENT '昵称',
    role VARCHAR(32) NOT NULL DEFAULT 'MEMBER' COMMENT '角色',
    phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态 1:正常 0:禁用',
    failed_attempts INT DEFAULT 0 COMMENT '登录失败次数',
    lock_expires_at DATETIME COMMENT '锁定过期时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 家庭表
CREATE TABLE IF NOT EXISTS families (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL COMMENT '家庭名称',
    owner_id BIGINT NOT NULL COMMENT '所有者ID',
    invite_code VARCHAR(16) NOT NULL UNIQUE COMMENT '邀请码',
    status INT NOT NULL DEFAULT 0 COMMENT '状态:0待审核/禁用,1启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭表';

-- 3. 家庭成员关联表
CREATE TABLE IF NOT EXISTS family_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    relation VARCHAR(32) COMMENT '与家庭关系',
    is_admin TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为管理员',
    role VARCHAR(16) DEFAULT 'MEMBER' COMMENT '家庭角色',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (family_id) REFERENCES families(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_family_user (family_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭成员表';

-- 4. 健康日志表
CREATE TABLE IF NOT EXISTS health_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL COMMENT '日志类型: DIET, SLEEP, EXERCISE, MOOD, VITALS',
    content TEXT COMMENT '日志内容JSON',
    log_date DATE NOT NULL COMMENT '记录日期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_date (user_id, log_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康日志表';

-- 5. 系统设置表
CREATE TABLE IF NOT EXISTS system_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(64) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统设置表';

-- 6. 系统日志表
CREATE TABLE IF NOT EXISTS system_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    type VARCHAR(16) NOT NULL COMMENT '日志类型',
    module VARCHAR(64) COMMENT '模块',
    action VARCHAR(128) COMMENT '操作',
    detail TEXT COMMENT '详情',
    trace_id VARCHAR(64),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';

-- 7. 系统设置历史表 (版本控制)
CREATE TABLE IF NOT EXISTS system_setting_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(64) NOT NULL,
    config_value TEXT NOT NULL,
    version VARCHAR(64) NOT NULL,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ssh_key_created (config_key, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置历史记录表';

-- 8. 健康计划表
CREATE TABLE IF NOT EXISTS health_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    patient_user_id BIGINT NOT NULL,
    family_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    title VARCHAR(128) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE,
    frequency_type VARCHAR(32) NOT NULL,
    frequency_value INT,
    frequency_detail JSON,
    target_indicators JSON,
    reminder_strategy JSON,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    completion_rate DECIMAL(5,2) DEFAULT 0.00,
    compliance_rate DECIMAL(5,2) DEFAULT 0.00,
    metadata_json JSON,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (doctor_id) REFERENCES users(id),
    FOREIGN KEY (patient_user_id) REFERENCES users(id),
    FOREIGN KEY (family_id) REFERENCES families(id),
    INDEX idx_health_plans_family (family_id),
    INDEX idx_health_plans_patient (patient_user_id),
    INDEX idx_health_plans_doctor (doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康计划表';

-- 9. 健康提醒表
CREATE TABLE IF NOT EXISTS health_reminders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    assigned_to BIGINT,
    creator_id BIGINT,
    family_id BIGINT,
    type VARCHAR(32) NOT NULL COMMENT '提醒类型',
    title VARCHAR(128) NOT NULL,
    content TEXT COMMENT '提醒内容',
    trigger_condition JSON COMMENT '触发条件',
    scheduled_time DATETIME COMMENT '计划提醒时间',
    actual_time DATETIME COMMENT '实际提醒时间',
    status VARCHAR(16) NOT NULL COMMENT '状态',
    priority VARCHAR(16) NOT NULL COMMENT '优先级',
    channel VARCHAR(32) COMMENT '发送渠道',
    metadata_json JSON COMMENT '元数据',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (creator_id) REFERENCES users(id),
    FOREIGN KEY (assigned_to) REFERENCES users(id),
    FOREIGN KEY (family_id) REFERENCES families(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康提醒表';

-- 10. 中医知识库
CREATE TABLE IF NOT EXISTS tcm_knowledge_base (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    type VARCHAR(32),
    constitution_type VARCHAR(32),
    difficulty VARCHAR(10),
    evidence_level VARCHAR(10),
    tags VARCHAR(255),
    seasonality JSON,
    contraindications JSON,
    duration VARCHAR(50),
    updated_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中医知识库';

-- 11. 审计日志表
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(64) NOT NULL,
    resource VARCHAR(128) NOT NULL,
    sensitivity_level VARCHAR(16) NOT NULL,
    result VARCHAR(16) NOT NULL,
    ip VARCHAR(45),
    user_agent VARCHAR(255),
    extra_json JSON,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- 12. 告警表 (含系统告警)
CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT '关联用户，系统告警可为空',
    family_id BIGINT COMMENT '关联家庭',
    type VARCHAR(64) NOT NULL,
    level VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL,
    payload_json JSON NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (family_id) REFERENCES families(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警表';

-- 初始管理员账号 (admin/123456) - 使用手机号 13800000000
INSERT INTO users (phone, password_hash, nickname, role, status) 
VALUES ('13800000000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd7.jRjD.a', '系统管理员', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE id=id;

-- 初始化中医知识库数据 (气虚体质示例)
DELETE FROM tcm_knowledge_base WHERE constitution_type = 'QI_DEFICIENCY';
INSERT INTO tcm_knowledge_base (title, content, type, constitution_type, difficulty, evidence_level, updated_at, created_at) VALUES 
('黄芪炖鸡', '取黄芪30克，母鸡1只。将母鸡洗净，黄芪塞入鸡腹中，加水适量，炖至鸡肉烂熟，加盐调味。具有补气固表功效。', 'DIET', 'QI_DEFICIENCY', 'MEDIUM', 'HIGH', NOW(), NOW()),
('山药粥', '山药30克，大米100克。将山药去皮切块，与大米同煮成粥。具有益气养阴、补脾肺肾的功效。', 'DIET', 'QI_DEFICIENCY', 'EASY', 'HIGH', NOW(), NOW()),
('八段锦', '练习八段锦，重点练习“调理脾胃须单举”一式。每日1-2次，每次15-20分钟。', 'EXERCISE', 'QI_DEFICIENCY', 'EASY', 'HIGH', NOW(), NOW()),
('太极拳', '练习太极拳，动作柔和缓慢，有助于气血流通。建议每日清晨练习30分钟。', 'EXERCISE', 'QI_DEFICIENCY', 'MEDIUM', 'HIGH', NOW(), NOW()),
('足三里按摩', '每日按揉足三里穴（膝盖外侧凹陷下3寸）10-15分钟，有酸胀感为宜。可补中益气。', 'ACUPUNCTURE', 'QI_DEFICIENCY', 'EASY', 'MEDIUM', NOW(), NOW()),
('关元穴艾灸', '使用艾条温和灸关元穴（脐下3寸），每日1次，每次15分钟。可培元固本。', 'ACUPUNCTURE', 'QI_DEFICIENCY', 'MEDIUM', 'MEDIUM', NOW(), NOW()),
('黄芪红枣茶', '黄芪10克，红枣5枚。开水冲泡代茶饮。具有补气生血功效。', 'TEA', 'QI_DEFICIENCY', 'EASY', 'HIGH', NOW(), NOW()),
('生活起居', '注意保暖，避免受风寒。避免过度劳累，保证充足睡眠。', 'LIFESTYLE', 'QI_DEFICIENCY', 'EASY', 'HIGH', NOW(), NOW()),
('情志调节', '保持心情舒畅，避免过度思虑。多听轻柔音乐，放松心情。', 'EMOTION', 'QI_DEFICIENCY', 'EASY', 'HIGH', NOW(), NOW());
