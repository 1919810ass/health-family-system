/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80044
 Source Host           : localhost:3306
 Source Schema         : health_family

 Target Server Type    : MySQL
 Target Server Version : 80044
 File Encoding         : 65001

 Date: 25/02/2026 19:00:46
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for abnormal_handling_records
-- ----------------------------
DROP TABLE IF EXISTS `abnormal_handling_records`;
CREATE TABLE `abnormal_handling_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `alert_id` bigint NULL DEFAULT NULL COMMENT '关联的异常记录ID',
  `doctor_id` bigint NOT NULL COMMENT '处理医生ID',
  `patient_id` bigint NOT NULL COMMENT '涉及的患者ID',
  `handling_action` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '处理动作：notify(发送提醒), call(电话联系), referral(转诊建议)',
  `handling_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `handling_note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `handled_at` datetime NOT NULL COMMENT '处理时间',
  `follow_up_required` tinyint(1) NULL DEFAULT NULL COMMENT '是否需要后续跟踪',
  `follow_up_time` datetime NULL DEFAULT NULL COMMENT '跟踪时间',
  `follow_up_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_alert_id`(`alert_id` ASC) USING BTREE,
  INDEX `idx_doctor_id`(`doctor_id` ASC) USING BTREE,
  INDEX `idx_patient_id`(`patient_id` ASC) USING BTREE,
  CONSTRAINT `abnormal_handling_records_ibfk_1` FOREIGN KEY (`alert_id`) REFERENCES `health_alerts` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `abnormal_handling_records_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `abnormal_handling_records_ibfk_3` FOREIGN KEY (`patient_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '异常处理记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_recommendations
-- ----------------------------
DROP TABLE IF EXISTS `ai_recommendations`;
CREATE TABLE `ai_recommendations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `for_date` date NOT NULL COMMENT '建议日期',
  `category` enum('DIET','EMOTION','LIFESTYLE','REST','SPORT','VITALS') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '建议标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `reasoning` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `priority` enum('HIGH','LOW','MEDIUM') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `data_sources` json NULL COMMENT '数据来源（引用的健康日志、评估等）',
  `is_accepted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '用户是否采纳',
  `feedback` tinyint NULL DEFAULT NULL COMMENT '用户反馈：1有用 0无用',
  `ai_model` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '使用的AI模型',
  `prompt_version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '提示词版本',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_reco_user_date`(`user_id` ASC, `for_date` ASC) USING BTREE,
  INDEX `idx_ai_reco_category`(`category` ASC) USING BTREE,
  INDEX `idx_ai_reco_created`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_ai_reco_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI个性化建议（增强版）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_request_logs
-- ----------------------------
DROP TABLE IF EXISTS `ai_request_logs`;
CREATE TABLE `ai_request_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) NOT NULL,
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `input_tokens` int NULL DEFAULT NULL,
  `latency` bigint NULL DEFAULT NULL,
  `model_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `output_tokens` int NULL DEFAULT NULL,
  `service_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `trace_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_usage_logs
-- ----------------------------
DROP TABLE IF EXISTS `ai_usage_logs`;
CREATE TABLE `ai_usage_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `input_tokens` int NULL DEFAULT NULL,
  `latency_ms` bigint NULL DEFAULT NULL,
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `output_tokens` int NULL DEFAULT NULL,
  `success` bit(1) NULL DEFAULT NULL,
  `total_tokens` int NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for alerts
-- ----------------------------
DROP TABLE IF EXISTS `alerts`;
CREATE TABLE `alerts`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NULL DEFAULT NULL,
  `family_id` bigint NULL DEFAULT NULL,
  `type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '如SLEEP_ANOMALY/VITALS_ALERT',
  `level` enum('CRITICAL','INFO','WARN','WARNING') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` enum('ACKED','ACKNOWLEDGED','CLOSED','ESCALATED','PENDING','RESOLVED','UNREAD') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `payload_json` json NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `message` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_alerts_user_time`(`user_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_alerts_family_time`(`family_id` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `fk_alerts_family` FOREIGN KEY (`family_id`) REFERENCES `families` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_alerts_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 257 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '异常与预警' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for audit_logs
-- ----------------------------
DROP TABLE IF EXISTS `audit_logs`;
CREATE TABLE `audit_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NULL DEFAULT NULL,
  `action` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'READ/EXPORT/GENERATE/LOGIN/AI_QA等',
  `resource` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资源路径或实体类型',
  `sensitivity_level` enum('CRITICAL','HIGH','LOW','MEDIUM','NORMAL') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `result` enum('ALLOW','DENY','FAILURE','SUCCESS') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `ip` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `extra_json` json NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_audit_user_time`(`user_id` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `fk_audit_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 507 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作审计日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bad_case_table
-- ----------------------------
DROP TABLE IF EXISTS `bad_case_table`;
CREATE TABLE `bad_case_table`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ai_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `auditor_id` bigint NULL DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `human_correction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `message_id` bigint NULL DEFAULT NULL,
  `question` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `risk_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `session_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for constitution_assessments
-- ----------------------------
DROP TABLE IF EXISTS `constitution_assessments`;
CREATE TABLE `constitution_assessments`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'TCM_9' COMMENT '量表类型：TCM_9等',
  `score_vector` json NOT NULL,
  `primary_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主导体质',
  `report_json` json NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `assessment_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '1.0',
  `confidence_score` decimal(5, 2) NULL DEFAULT NULL,
  `constitution_tags` json NULL,
  `follow_up_recommendations` json NULL,
  `is_primary` tinyint(1) NULL DEFAULT 1,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `assessment_source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'MANUAL',
  `conversation_history` json NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ca_user_time`(`user_id` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `fk_ca_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '体质测评' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for constitution_trend_records
-- ----------------------------
DROP TABLE IF EXISTS `constitution_trend_records`;
CREATE TABLE `constitution_trend_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `assessment_id` bigint NOT NULL COMMENT '关联的体质测评ID',
  `constitution_scores` json NOT NULL,
  `primary_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主导体质',
  `trend_analysis` json NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `fk_trend_assessment`(`assessment_id` ASC) USING BTREE,
  CONSTRAINT `fk_trend_assessment` FOREIGN KEY (`assessment_id`) REFERENCES `constitution_assessments` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_trend_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '体质变化趋势记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for consultation_messages
-- ----------------------------
DROP TABLE IF EXISTS `consultation_messages`;
CREATE TABLE `consultation_messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `sender_id` bigint NOT NULL COMMENT '发送者用户ID',
  `sender_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者类型：DOCTOR（医生）、FAMILY_MEMBER（家庭成员）、MEMBER（患者本人）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `message_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'TEXT' COMMENT '消息类型：TEXT（文本）、TEMPLATE（模板回复）',
  `template_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模板ID（如果是模板回复）',
  `read_by_doctor` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读（医生端）',
  `read_by_patient` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读（患者端）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_consultation_messages_session`(`session_id` ASC) USING BTREE,
  INDEX `idx_consultation_messages_sender`(`sender_id` ASC) USING BTREE,
  INDEX `idx_consultation_messages_created`(`created_at` ASC) USING BTREE,
  CONSTRAINT `consultation_messages_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `consultation_sessions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `consultation_messages_ibfk_2` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '咨询消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for consultation_sessions
-- ----------------------------
DROP TABLE IF EXISTS `consultation_sessions`;
CREATE TABLE `consultation_sessions`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `patient_user_id` bigint NOT NULL COMMENT '患者用户ID',
  `family_id` bigint NOT NULL COMMENT '家庭ID',
  `doctor_id` bigint NULL DEFAULT NULL COMMENT '医生用户ID（可选）',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会话标题',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '会话状态：ACTIVE（进行中）、CLOSED（已关闭）',
  `last_message_at` datetime NULL DEFAULT NULL COMMENT '最后一条消息的时间',
  `unread_count_doctor` int NOT NULL DEFAULT 0 COMMENT '未读消息数（医生端）',
  `unread_count_patient` int NOT NULL DEFAULT 0 COMMENT '未读消息数（患者端）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_ai_triaged` bit(1) NULL DEFAULT NULL,
  `patient_symptoms` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `triage_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_consultation_sessions_patient`(`patient_user_id` ASC) USING BTREE,
  INDEX `idx_consultation_sessions_family`(`family_id` ASC) USING BTREE,
  INDEX `idx_consultation_sessions_doctor`(`doctor_id` ASC) USING BTREE,
  INDEX `idx_consultation_sessions_status`(`status` ASC) USING BTREE,
  INDEX `idx_consultation_sessions_last_message`(`last_message_at` ASC) USING BTREE,
  CONSTRAINT `consultation_sessions_ibfk_1` FOREIGN KEY (`patient_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `consultation_sessions_ibfk_2` FOREIGN KEY (`family_id`) REFERENCES `families` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `consultation_sessions_ibfk_3` FOREIGN KEY (`doctor_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '在线咨询会话表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for consultation_triage_chat
-- ----------------------------
DROP TABLE IF EXISTS `consultation_triage_chat`;
CREATE TABLE `consultation_triage_chat`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL COMMENT '关联会话ID',
  `sender_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发送者: AI, USER',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gmt_create` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session`(`session_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI预问诊对话详情' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for doc_fragments_v2
-- ----------------------------
DROP TABLE IF EXISTS `doc_fragments_v2`;
CREATE TABLE `doc_fragments_v2`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `embedding` tinyblob NULL,
  `tags` json NULL,
  `source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_doc_frag_v2_title`(`title` ASC) USING BTREE,
  FULLTEXT INDEX `ft_doc_frag_v2_content`(`content`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '宣教片段库V2' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for doctor_notes
-- ----------------------------
DROP TABLE IF EXISTS `doctor_notes`;
CREATE TABLE `doctor_notes`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '病历记录ID',
  `doctor_id` bigint NOT NULL COMMENT '医生用户ID',
  `patient_user_id` bigint NOT NULL COMMENT '患者用户ID',
  `family_id` bigint NOT NULL COMMENT '家庭ID',
  `consultation_date` date NOT NULL COMMENT '问诊日期',
  `chief_complaint` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `past_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `medication` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `lifestyle_assessment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `diagnosis_opinion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `followup_suggestion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_doctor_notes_doctor`(`doctor_id` ASC) USING BTREE,
  INDEX `idx_doctor_notes_patient`(`patient_user_id` ASC) USING BTREE,
  INDEX `idx_doctor_notes_family`(`family_id` ASC) USING BTREE,
  INDEX `idx_doctor_notes_consultation_date`(`consultation_date` ASC) USING BTREE,
  INDEX `idx_doctor_notes_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `doctor_notes_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `doctor_notes_ibfk_2` FOREIGN KEY (`patient_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `doctor_notes_ibfk_3` FOREIGN KEY (`family_id`) REFERENCES `families` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '医生病历记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for doctor_profiles
-- ----------------------------
DROP TABLE IF EXISTS `doctor_profiles`;
CREATE TABLE `doctor_profiles`  (
  `doctor_id` bigint NOT NULL,
  `hospital` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执业医院',
  `department` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '科室',
  `specialty` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '专业领域',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职称',
  `bio` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `certification_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '认证状态：PENDING-待审核，APPROVED-已认证，REJECTED-已拒绝',
  `certified_at` datetime NULL DEFAULT NULL COMMENT '认证时间',
  `certified_by` bigint NULL DEFAULT NULL COMMENT '认证审核人ID（管理员ID）',
  `reject_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `license_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执业证书编号',
  `license_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执业证书图片路径',
  `id_card` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号',
  `id_card_front` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证正面图片路径',
  `id_card_back` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证反面图片路径',
  `rating` decimal(3, 2) NULL DEFAULT NULL,
  `rating_count` int NULL DEFAULT 0 COMMENT '评分人数',
  `service_count` int NULL DEFAULT 0 COMMENT '服务用户数（累计服务过的家庭数）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`doctor_id`) USING BTREE,
  CONSTRAINT `fk_doctor_profile_user` FOREIGN KEY (`doctor_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生扩展信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for doctor_ratings
-- ----------------------------
DROP TABLE IF EXISTS `doctor_ratings`;
CREATE TABLE `doctor_ratings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `created_at` datetime(6) NOT NULL,
  `doctor_id` bigint NOT NULL,
  `rating` int NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for families
-- ----------------------------
DROP TABLE IF EXISTS `families`;
CREATE TABLE `families`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '家庭ID',
  `owner_id` bigint NOT NULL COMMENT '家庭所有者用户ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '家庭名称',
  `invite_code` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邀请码',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `invite_code`(`invite_code` ASC) USING BTREE,
  INDEX `fk_families_owner`(`owner_id` ASC) USING BTREE,
  CONSTRAINT `fk_families_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '家庭' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for family_doctors
-- ----------------------------
DROP TABLE IF EXISTS `family_doctors`;
CREATE TABLE `family_doctors`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL,
  `doctor_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_family_doctor_unique`(`family_id` ASC) USING BTREE,
  INDEX `fk_family_doctors_doctor`(`doctor_id` ASC) USING BTREE,
  CONSTRAINT `fk_family_doctors_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_family_doctors_family` FOREIGN KEY (`family_id`) REFERENCES `families` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for family_interactions
-- ----------------------------
DROP TABLE IF EXISTS `family_interactions`;
CREATE TABLE `family_interactions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `family_id` bigint NOT NULL,
  `is_read` bit(1) NULL DEFAULT NULL,
  `sender_id` bigint NOT NULL,
  `target_user_id` bigint NOT NULL,
  `type` enum('LIKE','MESSAGE','NUDGE') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for family_members
-- ----------------------------
DROP TABLE IF EXISTS `family_members`;
CREATE TABLE `family_members`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `relation` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关系：本人/父/母/子女等',
  `is_admin` bit(1) NOT NULL,
  `role` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '成员角色：ADMIN/MEMBER/VISITOR',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_family_user`(`family_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `fk_fm_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_fm_family` FOREIGN KEY (`family_id`) REFERENCES `families` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_fm_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '家庭-成员' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for family_tcm_health_overviews
-- ----------------------------
DROP TABLE IF EXISTS `family_tcm_health_overviews`;
CREATE TABLE `family_tcm_health_overviews`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL,
  `overview_content` json NOT NULL,
  `constitution_distribution` json NULL,
  `family_recommendation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `generated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_family_time`(`family_id` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `fk_overview_family` FOREIGN KEY (`family_id`) REFERENCES `families` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '家庭中医健康概览' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for health_alerts
-- ----------------------------
DROP TABLE IF EXISTS `health_alerts`;
CREATE TABLE `health_alerts`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `family_id` bigint NULL DEFAULT NULL,
  `metric` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` double NOT NULL,
  `threshold` double NULL DEFAULT NULL,
  `severity` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `channel` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `escalation_level` int NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `handled_at` datetime NULL DEFAULT NULL,
  `handled_by` bigint NULL DEFAULT NULL COMMENT '处理人ID（医生）',
  `handling_note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `notification_sent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `notification_time` datetime NULL DEFAULT NULL COMMENT '通知发送时间',
  `notification_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_alert_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_alert_family_created`(`family_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_alert_status_created`(`status` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `fk_alert_family` FOREIGN KEY (`family_id`) REFERENCES `families` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_alert_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for health_consultations
-- ----------------------------
DROP TABLE IF EXISTS `health_consultations`;
CREATE TABLE `health_consultations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话ID（用于上下文关联）',
  `question` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `context_json` json NULL COMMENT '上下文信息（用户健康标签、历史对话等）',
  `tools_used` json NULL COMMENT '使用的工具列表（如查询药品、医院等）',
  `sources` json NULL COMMENT '知识来源（引用的文档、指南等）',
  `feedback` tinyint NULL DEFAULT NULL COMMENT '用户反馈：1有用 0无用 -1未反馈',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_consult_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_consult_session`(`session_id` ASC) USING BTREE,
  INDEX `idx_consult_created`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_consult_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '健康咨询记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for health_inference_report
-- ----------------------------
DROP TABLE IF EXISTS `health_inference_report`;
CREATE TABLE `health_inference_report`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ai_analysis_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `constitution_snapshot` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `gmt_create` datetime(6) NULL DEFAULT NULL,
  `gmt_modified` datetime(6) NULL DEFAULT NULL,
  `input_summary` json NULL,
  `is_viewed` bit(1) NULL DEFAULT NULL,
  `report_date` date NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_date`(`user_id` ASC, `report_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for health_logs
-- ----------------------------
DROP TABLE IF EXISTS `health_logs`;
CREATE TABLE `health_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `log_date` date NOT NULL,
  `type` enum('DIET','MOOD','SLEEP','SPORT','VITALS') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content_json` json NOT NULL COMMENT '结构化内容：如时长、项目、情绪分等',
  `score` decimal(5, 2) NULL DEFAULT NULL,
  `data_source` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据来源：MANUAL/DEVICE/OCR/VOICE',
  `device_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备唯一标识',
  `is_abnormal` tinyint(1) NULL DEFAULT NULL COMMENT '是否异常数据：0否 1是',
  `metadata_json` json NULL COMMENT '元数据（异常检测结果、设备信息等）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_log_user_type`(`user_id` ASC, `type` ASC) USING BTREE,
  CONSTRAINT `fk_logs_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 207 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '健康日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for health_plans
-- ----------------------------
DROP TABLE IF EXISTS `health_plans`;
CREATE TABLE `health_plans`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '计划ID',
  `doctor_id` bigint NOT NULL COMMENT '创建计划的医生ID',
  `patient_user_id` bigint NOT NULL COMMENT '患者用户ID（计划针对的成员）',
  `family_id` bigint NOT NULL COMMENT '家庭ID',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '计划类型：BLOOD_PRESSURE_FOLLOWUP（血压随访）/DIET_MANAGEMENT（饮食管理）/EXERCISE_PRESCRIPTION（运动处方）/MEDICATION_MANAGEMENT（用药管理）/WEIGHT_MANAGEMENT（体重管理）/OTHER（其他）',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '计划标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '结束日期（可选，null表示无结束日期）',
  `frequency_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行频率类型：DAILY/WEEKLY/MONTHLY/CUSTOM',
  `frequency_value` int NULL DEFAULT NULL COMMENT '频率值（如每周3次、每2周1次等，配合frequency_type使用）',
  `frequency_detail` json NULL COMMENT '频率详情（如每周一三五、每月1号和15号等）',
  `target_indicators` json NULL COMMENT '目标指标（如血压<140/90、体重<70kg等）',
  `reminder_strategy` json NULL COMMENT '提醒策略（提醒时间、提前多久提醒、提醒渠道等）',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '计划状态：ACTIVE（进行中）/COMPLETED（已完成）/OVERDUE（逾期）/CANCELLED（已取消）/PAUSED（已暂停）',
  `completion_rate` decimal(5, 2) NULL DEFAULT NULL,
  `compliance_rate` decimal(5, 2) NULL DEFAULT NULL,
  `metadata_json` json NULL COMMENT '元数据（关联的日志ID、提醒ID等）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_health_plans_doctor`(`doctor_id` ASC) USING BTREE,
  INDEX `idx_health_plans_patient`(`patient_user_id` ASC) USING BTREE,
  INDEX `idx_health_plans_family`(`family_id` ASC) USING BTREE,
  INDEX `idx_health_plans_type`(`type` ASC) USING BTREE,
  INDEX `idx_health_plans_status`(`status` ASC) USING BTREE,
  INDEX `idx_health_plans_dates`(`start_date` ASC, `end_date` ASC) USING BTREE,
  INDEX `idx_health_plans_doctor_patient`(`doctor_id` ASC, `patient_user_id` ASC) USING BTREE,
  CONSTRAINT `health_plans_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `health_plans_ibfk_2` FOREIGN KEY (`patient_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `health_plans_ibfk_3` FOREIGN KEY (`family_id`) REFERENCES `families` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '健康计划与随访表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for health_reminders
-- ----------------------------
DROP TABLE IF EXISTS `health_reminders`;
CREATE TABLE `health_reminders`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `assigned_to` bigint NULL DEFAULT NULL,
  `family_id` bigint NULL DEFAULT NULL,
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '提醒类型：MEDICATION/MEASUREMENT/VACCINE/LIFESTYLE/ABNORMAL',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '提醒标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `trigger_condition` json NULL COMMENT '触发条件（如血压>140时触发）',
  `scheduled_time` datetime NULL DEFAULT NULL COMMENT '计划提醒时间',
  `actual_time` datetime NULL DEFAULT NULL COMMENT '实际提醒时间',
  `status` enum('ACKNOWLEDGED','CANCELLED','COMPLETED','PENDING','SENT','SKIPPED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `priority` enum('HIGH','LOW','MEDIUM','URGENT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `channel` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '提醒渠道：APP/SMS/VOICE',
  `metadata_json` json NULL COMMENT '元数据（AI生成建议、关联数据等）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `creator_id` bigint NULL DEFAULT NULL COMMENT '提醒创建者ID',
  `visibility` enum('PRIVATE','FAMILY','DOCTOR','ALL') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PRIVATE' COMMENT '可见性：私有/家庭/医生/全部',
  `authorized_roles` json NULL COMMENT '授权角色列表，如[\"ADMIN\", \"DOCTOR\", \"FAMILY_ADMIN\"]',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_reminder_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_reminder_status`(`status` ASC) USING BTREE,
  INDEX `idx_reminder_scheduled`(`scheduled_time` ASC) USING BTREE,
  INDEX `idx_reminder_type`(`type` ASC) USING BTREE,
  INDEX `idx_health_reminders_assigned_to`(`assigned_to` ASC) USING BTREE,
  INDEX `idx_health_reminders_family`(`family_id` ASC) USING BTREE,
  INDEX `idx_health_reminders_user_scheduled`(`user_id` ASC, `scheduled_time` ASC) USING BTREE,
  INDEX `idx_health_reminders_status_time`(`status` ASC, `scheduled_time` ASC) USING BTREE,
  INDEX `idx_health_reminders_creator`(`creator_id` ASC) USING BTREE,
  CONSTRAINT `fk_health_reminders_assigned_to` FOREIGN KEY (`assigned_to`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_health_reminders_family` FOREIGN KEY (`family_id`) REFERENCES `families` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_reminder_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `FKg6cb9shyrq683klmad5855bk6` FOREIGN KEY (`creator_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 387 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '智能健康提醒' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for health_reports
-- ----------------------------
DROP TABLE IF EXISTS `health_reports`;
CREATE TABLE `health_reports`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `error_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `image_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `interpretation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `ocr_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `report_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `report_type` enum('EXAM_REPORT','LAB_REPORT','OTHER','PRESCRIPTION') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` enum('COMPLETED','FAILED','PENDING','PROCESSING') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `doctor_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `doctor_comment_time` datetime(6) NULL DEFAULT NULL,
  `progress_percent` int NULL DEFAULT NULL,
  `progress_stage` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK1x7pagf8d16jp4flrve7j9xjd`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK1x7pagf8d16jp4flrve7j9xjd` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 50 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for health_thresholds
-- ----------------------------
DROP TABLE IF EXISTS `health_thresholds`;
CREATE TABLE `health_thresholds`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `metric` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `lower_bound` double NULL DEFAULT NULL,
  `upper_bound` double NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_metric`(`user_id` ASC, `metric` ASC) USING BTREE,
  CONSTRAINT `fk_threshold_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for knowledge_documents
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_documents`;
CREATE TABLE `knowledge_documents`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文档标题',
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类：GUIDELINE/DRUG/DIET/EXERCISE/DISEASE等',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源（如《中国高血压防治指南》）',
  `tags` json NULL COMMENT '标签（如[\"高血压\",\"饮食\",\"运动\"]）',
  `embedding` longblob NULL,
  `embedding_model` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '嵌入模型名称',
  `chunk_index` int NULL DEFAULT NULL COMMENT '文档分块索引（大文档分块存储）',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父文档ID（用于分块关联）',
  `version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'v1',
  `enabled` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_knowledge_category`(`category` ASC) USING BTREE,
  INDEX `idx_knowledge_enabled`(`enabled` ASC) USING BTREE,
  INDEX `idx_knowledge_parent`(`parent_id` ASC) USING BTREE,
  FULLTEXT INDEX `ft_knowledge_content`(`content`),
  CONSTRAINT `FK53r05hjafl2ysqdfwhfl89d5g` FOREIGN KEY (`parent_id`) REFERENCES `knowledge_documents` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识库文档（RAG）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for plans
-- ----------------------------
DROP TABLE IF EXISTS `plans`;
CREATE TABLE `plans`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `type` enum('CHECKIN','MEDICATION','SLEEP','SPORT','WATER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `schedule_json` json NOT NULL,
  `payload_json` json NULL,
  `enabled` bit(1) NOT NULL,
  `next_run_at` datetime NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_plans_user_type`(`user_id` ASC, `type` ASC) USING BTREE,
  CONSTRAINT `fk_plans_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '提醒计划' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for profiles
-- ----------------------------
DROP TABLE IF EXISTS `profiles`;
CREATE TABLE `profiles`  (
  `user_id` bigint NOT NULL,
  `sex` enum('F','M','OTHER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `birthday` date NULL DEFAULT NULL,
  `height_cm` decimal(5, 2) NULL DEFAULT NULL,
  `weight_kg` decimal(5, 2) NULL DEFAULT NULL,
  `allergies` json NULL COMMENT '过敏史',
  `preferences` json NULL COMMENT '饮食/作息/运动偏好',
  `goals` json NULL COMMENT '健康目标',
  `tcm_tags` json NULL COMMENT '体质标签/证候标签（冗余缓存）',
  `health_tags` json NULL COMMENT '健康标签（如糖尿病、高血压、过敏史等）',
  `lifestyle` json NULL COMMENT '生活习惯（饮食、运动、睡眠等）',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE,
  CONSTRAINT `fk_profiles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户画像' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for recommendations
-- ----------------------------
DROP TABLE IF EXISTS `recommendations`;
CREATE TABLE `recommendations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `for_date` date NOT NULL,
  `category` enum('DIET','EMOTION','LIFESTYLE','REST','SPORT','VITALS') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `items_json` json NOT NULL,
  `evidence_json` json NOT NULL,
  `version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'v1',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'READY',
  `ai_model` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `prompt_version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `metadata_json` json NULL,
  `is_accepted` bit(1) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rec_user_date_cat`(`user_id` ASC, `for_date` ASC, `category` ASC) USING BTREE,
  INDEX `idx_rec_user_date`(`user_id` ASC, `for_date` ASC) USING BTREE,
  CONSTRAINT `fk_rec_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 143 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '个性化建议（含可解释证据）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for recommendations_v2
-- ----------------------------
DROP TABLE IF EXISTS `recommendations_v2`;
CREATE TABLE `recommendations_v2`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `date` date NOT NULL,
  `items_json` json NOT NULL,
  `evidence_json` json NOT NULL,
  `score` double NULL DEFAULT NULL,
  `ai` bit(1) NOT NULL,
  `accepted` bit(1) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_reco_v2_user_date`(`user_id` ASC, `date` ASC) USING BTREE,
  INDEX `idx_reco_v2_user_date`(`user_id` ASC, `date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '个性化建议V2' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for rules
-- ----------------------------
DROP TABLE IF EXISTS `rules`;
CREATE TABLE `rules`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` enum('DIET','EMOTION','MEDICATION','REST','SPORT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `condition_json` json NOT NULL,
  `action_json` json NOT NULL,
  `weight` decimal(6, 3) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源/参考',
  `version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'v1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_rules_cat_en`(`category` ASC, `enabled` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '建议规则库' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for rules_v2
-- ----------------------------
DROP TABLE IF EXISTS `rules_v2`;
CREATE TABLE `rules_v2`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` enum('DIET','SLEEP','SPORT','MOOD','VITALS') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `condition_json` json NOT NULL,
  `action_template` json NOT NULL,
  `weight` double NOT NULL,
  `source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` enum('ENABLED','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ENABLED',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_rules_v2_cat_status`(`category` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '规则库V2' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for suggestion_feedback
-- ----------------------------
DROP TABLE IF EXISTS `suggestion_feedback`;
CREATE TABLE `suggestion_feedback`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `recommendation_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `useful` bit(1) NOT NULL,
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_feedback_reco_user`(`recommendation_id` ASC, `user_id` ASC) USING BTREE,
  CONSTRAINT `fk_feedback_reco` FOREIGN KEY (`recommendation_id`) REFERENCES `recommendations_v2` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '建议反馈' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for system_logs
-- ----------------------------
DROP TABLE IF EXISTS `system_logs`;
CREATE TABLE `system_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NULL DEFAULT NULL,
  `type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `level` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `module` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `action` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_system_logs_type_created`(`type` ASC, `created_at` ASC) USING BTREE,
  INDEX `FK3duy1vdqrob9rjxy67079ja4w`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK3duy1vdqrob9rjxy67079ja4w` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for system_setting_histories
-- ----------------------------
DROP TABLE IF EXISTS `system_setting_histories`;
CREATE TABLE `system_setting_histories`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_by` bigint NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ssh_key_created`(`config_key` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统配置历史记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for system_settings
-- ----------------------------
DROP TABLE IF EXISTS `system_settings`;
CREATE TABLE `system_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for tcm_knowledge_base
-- ----------------------------
DROP TABLE IF EXISTS `tcm_knowledge_base`;
CREATE TABLE `tcm_knowledge_base`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` enum('ACUPUNCTURE','DIET','EMOTION','EXERCISE','HERBAL','LIFESTYLE','SEASONAL','TEA') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `constitution_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '适用体质类型',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tags` json NULL,
  `seasonality` json NULL,
  `difficulty` enum('EASY','HARD','MEDIUM') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `duration` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '建议时长',
  `contraindications` json NULL,
  `evidence_level` enum('HIGH','LOW','MEDIUM') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type_constitution`(`type` ASC, `constitution_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2125 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '中医养生知识库' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for tcm_personalized_plans
-- ----------------------------
DROP TABLE IF EXISTS `tcm_personalized_plans`;
CREATE TABLE `tcm_personalized_plans`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `plan_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '方案名称',
  `primary_constitution` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主导体质',
  `plan_content` json NOT NULL,
  `seasonal_recommendations` json NULL,
  `priority_recommendations` json NULL,
  `generated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expires_at` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `fk_plan_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '个性化中医养生方案' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_login_logs
-- ----------------------------
DROP TABLE IF EXISTS `user_login_logs`;
CREATE TABLE `user_login_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名/手机号',
  `role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户角色',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录IP地址',
  `user_agent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `login_time` datetime NOT NULL COMMENT '登录时间',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录状态：SUCCESS/FAILED',
  `login_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录类型：APP/WEB/WECHAT',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_login_time`(`login_time` ASC) USING BTREE,
  INDEX `idx_ip_address`(`ip_address` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 625 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户登录日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号（可空）',
  `wechat_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信OpenID（可空）',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码哈希（微信登录可空）',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `role` enum('ADMIN','DOCTOR','FAMILY_ADMIN','MEMBER','VIEWER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` int NOT NULL,
  `last_login_at` datetime NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `failed_attempts` int NULL DEFAULT NULL,
  `lock_expires_at` datetime(6) NULL DEFAULT NULL,
  `audit_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `wechat_openid`(`wechat_openid` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 256 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
