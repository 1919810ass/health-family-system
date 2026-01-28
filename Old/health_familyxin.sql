/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80030
 Source Host           : localhost:3306
 Source Schema         : health_family

 Target Server Type    : MySQL
 Target Server Version : 80030
 File Encoding         : 65001

 Date: 23/01/2026 22:09:29
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
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '异常处理记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of abnormal_handling_records
-- ----------------------------
INSERT INTO `abnormal_handling_records` VALUES (1, 2, 10, 6, 'notify', '您的血压值异常（135），请立即测量并记录，如有不适请及时就医。', '', '2026-01-16 14:18:48', 1, '2026-01-15 16:00:00', NULL, '2026-01-16 14:18:47', '2026-01-16 14:18:48');
INSERT INTO `abnormal_handling_records` VALUES (2, 3, 10, 6, 'notify', '您的血压值异常（135），请立即测量并记录，如有不适请及时就医。', '', '2026-01-16 14:26:21', 0, NULL, NULL, '2026-01-16 14:26:21', '2026-01-16 14:26:21');
INSERT INTO `abnormal_handling_records` VALUES (3, 4, 10, 6, 'notify', '您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', '', '2026-01-16 14:33:02', 1, '2026-01-16 16:00:00', NULL, '2026-01-16 14:33:01', '2026-01-16 14:33:02');
INSERT INTO `abnormal_handling_records` VALUES (4, 5, 10, 6, 'notify', '您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', '', '2026-01-16 14:50:28', 0, NULL, NULL, '2026-01-16 14:50:27', '2026-01-16 14:50:28');
INSERT INTO `abnormal_handling_records` VALUES (5, 6, 10, 3, 'notify', '您的血压值异常（135），请立即测量并记录，如有不适请及时就医。', '', '2026-01-16 14:50:31', 0, NULL, NULL, '2026-01-16 14:50:31', '2026-01-16 14:50:31');
INSERT INTO `abnormal_handling_records` VALUES (6, 7, 10, 6, 'notify', '您的血压值异常（135），请立即测量并记录，如有不适请及时就医。', '', '2026-01-16 14:50:33', 0, NULL, NULL, '2026-01-16 14:50:32', '2026-01-16 14:50:33');
INSERT INTO `abnormal_handling_records` VALUES (7, 4, 10, 6, 'notify', '您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', '', '2026-01-16 14:51:14', 1, '2026-01-15 16:00:00', NULL, '2026-01-16 14:51:14', '2026-01-16 14:51:14');
INSERT INTO `abnormal_handling_records` VALUES (8, 4, 10, 6, 'notify', '您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', '', '2026-01-16 14:58:29', 1, '2026-01-15 16:00:00', NULL, '2026-01-16 14:58:29', '2026-01-16 14:58:29');
INSERT INTO `abnormal_handling_records` VALUES (9, 4, 10, 6, 'notify', '您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', '', '2026-01-16 15:15:58', 1, '2026-01-15 16:00:00', NULL, '2026-01-16 15:15:58', '2026-01-16 15:15:58');
INSERT INTO `abnormal_handling_records` VALUES (10, 4, 10, 6, 'notify', '您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', '', '2026-01-16 15:18:55', 1, '2026-01-16 16:00:00', NULL, '2026-01-16 15:18:55', '2026-01-16 15:18:55');
INSERT INTO `abnormal_handling_records` VALUES (11, 4, 10, 6, 'notify', '您的血糖值异常（130），请注意饮食控制，按医嘱用药。', '', '2026-01-16 16:03:16', 1, '2026-01-15 16:00:00', NULL, '2026-01-16 16:03:16', '2026-01-16 16:03:16');
INSERT INTO `abnormal_handling_records` VALUES (12, 4, 10, 6, 'notify', '您的血糖值异常（130），请注意饮食控制，按医嘱用药。', '', '2026-01-16 16:03:57', 1, NULL, NULL, '2026-01-16 16:03:56', '2026-01-16 16:03:57');
INSERT INTO `abnormal_handling_records` VALUES (13, 4, 10, 6, 'notify', '您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', '', '2026-01-16 16:05:55', 1, '2026-01-15 16:00:00', NULL, '2026-01-16 16:05:54', '2026-01-16 16:05:55');
INSERT INTO `abnormal_handling_records` VALUES (14, 4, 10, 6, 'notify', '您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', '', '2026-01-16 16:39:21', 1, '2026-01-16 00:00:00', NULL, '2026-01-16 16:39:21', '2026-01-16 16:39:21');
INSERT INTO `abnormal_handling_records` VALUES (15, 8, 10, 4, 'notify', '您的血压值异常（200），请立即测量并记录，如有不适请及时就医。', '', '2026-01-16 18:39:47', 1, NULL, NULL, '2026-01-16 18:39:46', '2026-01-16 18:39:47');
INSERT INTO `abnormal_handling_records` VALUES (16, 9, 10, 4, 'notify', '您的血压值异常（155），请立即测量并记录，如有不适请及时就医。', '', '2026-01-16 18:39:52', 1, '2026-01-16 00:00:00', NULL, '2026-01-16 18:39:51', '2026-01-16 18:39:52');

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI个性化建议（增强版）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_recommendations
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 138 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '异常与预警' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of alerts
-- ----------------------------
INSERT INTO `alerts` VALUES (1, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-15 13:01:08', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (2, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-15 13:08:08', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (3, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-15 13:21:08', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (4, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-15 13:25:08', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (5, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-15 13:36:08', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (6, NULL, NULL, 'SYSTEM', 'CRITICAL', 'UNREAD', '{}', '2026-01-15 13:51:08', '系统内存使用率过高: 90.06%');
INSERT INTO `alerts` VALUES (7, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-15 14:00:42', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (8, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-15 14:46:38', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (9, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-15 18:13:50', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (10, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-15 18:54:55', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (11, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-15 20:05:55', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (12, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-15 20:10:55', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (13, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-15 20:40:55', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (14, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-15 21:09:55', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (15, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-15 21:17:55', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (16, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-15 21:37:55', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (17, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-15 21:56:55', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (18, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-15 21:59:55', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (19, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-15 22:09:55', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (20, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 09:37:42', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (21, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 09:42:42', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (22, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 09:59:42', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (23, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 10:07:03', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (24, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 10:22:09', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (25, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 10:31:09', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (26, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 10:34:27', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (27, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 10:54:17', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (28, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 10:56:17', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (29, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 11:36:15', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (30, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 11:56:15', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (31, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 12:06:15', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (32, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 12:27:56', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (33, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 12:53:40', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (34, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 13:00:01', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (35, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 13:17:01', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (36, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 13:19:01', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (37, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 13:38:55', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (38, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 13:40:55', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (39, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 13:50:55', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (40, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 14:13:57', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (41, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 14:30:28', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (42, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 15:10:33', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (43, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 15:38:58', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (44, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 15:51:22', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (45, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 16:01:22', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (46, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 16:04:44', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (47, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 16:13:44', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (48, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 16:23:34', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (49, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 16:29:34', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (50, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 18:20:37', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (51, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 18:30:37', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (52, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 18:43:37', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (53, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 19:23:37', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (54, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 19:25:37', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (55, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 19:27:37', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (56, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 19:28:37', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (57, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 19:36:37', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (58, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 19:37:37', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (59, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 19:41:37', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (60, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 19:46:37', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (61, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-16 19:49:37', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (62, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-17 11:27:06', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (63, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-17 11:54:11', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (64, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-17 12:09:12', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (65, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-17 12:17:12', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (66, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-17 12:26:12', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (67, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-17 12:37:12', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (68, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-17 13:49:19', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (69, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-17 14:00:51', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (70, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-17 14:21:57', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (71, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-17 14:26:41', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (72, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-17 15:28:22', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (73, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-17 22:39:27', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (74, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-17 23:15:54', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (75, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-18 10:42:44', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (76, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-18 11:28:57', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (77, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-18 12:21:14', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (78, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-18 12:25:14', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (79, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-18 13:09:14', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (80, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-18 13:36:40', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (81, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-18 14:02:29', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (82, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-18 14:55:42', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (83, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-18 15:35:42', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (84, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-18 15:51:34', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (85, NULL, NULL, 'SYSTEM', 'CRITICAL', 'UNREAD', '{}', '2026-01-18 16:16:35', '系统内存使用率过高: 94.00%');
INSERT INTO `alerts` VALUES (86, NULL, NULL, 'SYSTEM', 'CRITICAL', 'UNREAD', '{}', '2026-01-18 16:19:48', '系统内存使用率过高: 92.06%');
INSERT INTO `alerts` VALUES (87, NULL, NULL, 'SYSTEM', 'CRITICAL', 'UNREAD', '{}', '2026-01-18 16:20:48', '系统内存使用率过高: 93.03%');
INSERT INTO `alerts` VALUES (88, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-18 16:53:57', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (89, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-18 16:56:57', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (90, NULL, NULL, 'SYSTEM', 'CRITICAL', 'UNREAD', '{}', '2026-01-18 16:58:55', '系统内存使用率过高: 98.96%');
INSERT INTO `alerts` VALUES (91, NULL, NULL, 'SYSTEM', 'CRITICAL', 'UNREAD', '{}', '2026-01-18 17:32:21', '系统内存使用率过高: 98.27%');
INSERT INTO `alerts` VALUES (92, NULL, NULL, 'SYSTEM', 'CRITICAL', 'UNREAD', '{}', '2026-01-18 17:36:39', '系统内存使用率过高: 99.18%');
INSERT INTO `alerts` VALUES (93, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-18 17:45:31', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (94, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-18 18:00:59', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (95, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-18 19:33:26', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (96, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 11:34:18', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (97, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 11:58:58', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (98, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 12:09:58', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (99, NULL, NULL, 'SYSTEM', 'CRITICAL', 'UNREAD', '{}', '2026-01-19 12:25:58', '系统内存使用率过高: 90.82%');
INSERT INTO `alerts` VALUES (100, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 12:59:59', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (101, NULL, NULL, 'SYSTEM', 'CRITICAL', 'UNREAD', '{}', '2026-01-19 13:02:31', '系统内存使用率过高: 90.80%');
INSERT INTO `alerts` VALUES (102, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 13:46:23', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (103, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 14:00:27', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (104, NULL, NULL, 'SYSTEM', 'CRITICAL', 'UNREAD', '{}', '2026-01-19 14:16:39', '系统内存使用率过高: 90.61%');
INSERT INTO `alerts` VALUES (105, NULL, NULL, 'SYSTEM', 'CRITICAL', 'UNREAD', '{}', '2026-01-19 14:17:39', '系统内存使用率过高: 91.61%');
INSERT INTO `alerts` VALUES (106, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 15:50:01', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (107, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 15:54:01', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (108, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 15:56:01', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (109, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 16:12:13', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (110, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 16:26:13', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (111, NULL, NULL, 'SYSTEM', 'CRITICAL', 'UNREAD', '{}', '2026-01-19 16:36:13', '系统内存使用率过高: 91.26%');
INSERT INTO `alerts` VALUES (112, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 17:16:15', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (113, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 17:18:41', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (114, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 17:38:45', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (115, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 17:47:45', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (116, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 18:56:00', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (117, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 19:00:00', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (118, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 19:08:00', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (119, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 19:15:00', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (120, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 19:26:24', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (121, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 19:35:24', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (122, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 19:44:24', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (123, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 20:06:24', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (124, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 20:21:46', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (125, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 20:25:46', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (126, NULL, NULL, 'SYSTEM', 'CRITICAL', 'UNREAD', '{}', '2026-01-19 20:30:46', '系统内存使用率过高: 90.98%');
INSERT INTO `alerts` VALUES (127, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 21:04:46', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (128, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 21:13:46', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (129, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 21:14:46', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (130, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 21:34:46', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (131, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-19 22:02:46', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (132, NULL, NULL, 'SYSTEM', 'CRITICAL', 'UNREAD', '{}', '2026-01-19 22:40:46', '系统内存使用率过高: 91.26%');
INSERT INTO `alerts` VALUES (133, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-20 13:12:26', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (134, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-20 13:55:26', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (135, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-20 13:56:26', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (136, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-20 14:00:26', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (137, NULL, NULL, 'APPLICATION', 'WARNING', 'UNREAD', '{}', '2026-01-20 14:10:26', '检测到异常错误日志激增');
INSERT INTO `alerts` VALUES (138, NULL, NULL, 'SYSTEM', 'CRITICAL', 'UNREAD', '{}', '2026-01-20 14:18:26', '系统内存使用率过高: 90.01%');

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
) ENGINE = InnoDB AUTO_INCREMENT = 243 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作审计日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of audit_logs
-- ----------------------------
INSERT INTO `audit_logs` VALUES (1, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 10:49:25');
INSERT INTO `audit_logs` VALUES (2, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 10:57:16');
INSERT INTO `audit_logs` VALUES (3, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 11:07:55');
INSERT INTO `audit_logs` VALUES (4, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 11:08:01');
INSERT INTO `audit_logs` VALUES (5, 3, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 12:24:16');
INSERT INTO `audit_logs` VALUES (6, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 12:24:27');
INSERT INTO `audit_logs` VALUES (7, 3, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 12:25:45');
INSERT INTO `audit_logs` VALUES (8, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 12:33:13');
INSERT INTO `audit_logs` VALUES (9, 5, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 12:33:50');
INSERT INTO `audit_logs` VALUES (10, 3, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 12:42:42');
INSERT INTO `audit_logs` VALUES (11, 5, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 12:43:08');
INSERT INTO `audit_logs` VALUES (12, 13, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 12:59:25');
INSERT INTO `audit_logs` VALUES (13, 13, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 12:59:31');
INSERT INTO `audit_logs` VALUES (14, 12, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 12:59:39');
INSERT INTO `audit_logs` VALUES (15, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 12:59:46');
INSERT INTO `audit_logs` VALUES (16, 5, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 13:00:35');
INSERT INTO `audit_logs` VALUES (17, 5, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 13:03:49');
INSERT INTO `audit_logs` VALUES (18, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 13:04:02');
INSERT INTO `audit_logs` VALUES (19, 3, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 13:04:50');
INSERT INTO `audit_logs` VALUES (20, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 13:05:00');
INSERT INTO `audit_logs` VALUES (21, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 13:20:52');
INSERT INTO `audit_logs` VALUES (22, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 13:24:05');
INSERT INTO `audit_logs` VALUES (23, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 13:29:08');
INSERT INTO `audit_logs` VALUES (24, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 13:38:57');
INSERT INTO `audit_logs` VALUES (25, 3, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 13:41:07');
INSERT INTO `audit_logs` VALUES (26, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 13:41:50');
INSERT INTO `audit_logs` VALUES (27, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 13:53:28');
INSERT INTO `audit_logs` VALUES (28, 3, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 13:53:51');
INSERT INTO `audit_logs` VALUES (29, 3, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 14:05:53');
INSERT INTO `audit_logs` VALUES (30, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 14:06:04');
INSERT INTO `audit_logs` VALUES (31, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 14:10:35');
INSERT INTO `audit_logs` VALUES (32, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 14:18:37');
INSERT INTO `audit_logs` VALUES (33, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 14:26:14');
INSERT INTO `audit_logs` VALUES (34, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 14:32:49');
INSERT INTO `audit_logs` VALUES (35, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 14:38:27');
INSERT INTO `audit_logs` VALUES (36, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 14:50:19');
INSERT INTO `audit_logs` VALUES (37, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 14:58:20');
INSERT INTO `audit_logs` VALUES (38, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 15:15:50');
INSERT INTO `audit_logs` VALUES (39, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 15:18:45');
INSERT INTO `audit_logs` VALUES (40, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 15:33:05');
INSERT INTO `audit_logs` VALUES (41, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 15:37:19');
INSERT INTO `audit_logs` VALUES (42, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 15:40:31');
INSERT INTO `audit_logs` VALUES (43, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 16:02:55');
INSERT INTO `audit_logs` VALUES (44, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 16:17:43');
INSERT INTO `audit_logs` VALUES (45, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 16:36:22');
INSERT INTO `audit_logs` VALUES (46, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 18:02:49');
INSERT INTO `audit_logs` VALUES (47, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 18:03:33');
INSERT INTO `audit_logs` VALUES (48, 5, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 18:03:39');
INSERT INTO `audit_logs` VALUES (49, 3, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 18:03:44');
INSERT INTO `audit_logs` VALUES (50, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 18:04:27');
INSERT INTO `audit_logs` VALUES (51, 5, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 18:04:37');
INSERT INTO `audit_logs` VALUES (52, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 18:04:51');
INSERT INTO `audit_logs` VALUES (53, 3, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 18:05:03');
INSERT INTO `audit_logs` VALUES (54, 6, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 18:05:19');
INSERT INTO `audit_logs` VALUES (55, 3, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 18:05:35');
INSERT INTO `audit_logs` VALUES (56, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 18:05:53');
INSERT INTO `audit_logs` VALUES (57, 13, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 18:07:59');
INSERT INTO `audit_logs` VALUES (58, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 18:08:02');
INSERT INTO `audit_logs` VALUES (59, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 18:08:55');
INSERT INTO `audit_logs` VALUES (60, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 18:22:03');
INSERT INTO `audit_logs` VALUES (61, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 18:22:21');
INSERT INTO `audit_logs` VALUES (62, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 18:23:48');
INSERT INTO `audit_logs` VALUES (63, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 18:24:20');
INSERT INTO `audit_logs` VALUES (64, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 18:33:04');
INSERT INTO `audit_logs` VALUES (65, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 19:02:28');
INSERT INTO `audit_logs` VALUES (66, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 19:17:15');
INSERT INTO `audit_logs` VALUES (67, 13, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 19:17:30');
INSERT INTO `audit_logs` VALUES (68, 3, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 19:20:47');
INSERT INTO `audit_logs` VALUES (69, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 19:20:53');
INSERT INTO `audit_logs` VALUES (70, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 19:40:29');
INSERT INTO `audit_logs` VALUES (71, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', NULL, '2026-01-16 20:26:40');
INSERT INTO `audit_logs` VALUES (72, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 11:23:34');
INSERT INTO `audit_logs` VALUES (73, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 11:24:15');
INSERT INTO `audit_logs` VALUES (74, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 11:28:01');
INSERT INTO `audit_logs` VALUES (75, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 11:28:08');
INSERT INTO `audit_logs` VALUES (76, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 11:42:58');
INSERT INTO `audit_logs` VALUES (77, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 11:43:02');
INSERT INTO `audit_logs` VALUES (78, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 11:50:56');
INSERT INTO `audit_logs` VALUES (79, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 11:59:45');
INSERT INTO `audit_logs` VALUES (80, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 12:00:00');
INSERT INTO `audit_logs` VALUES (81, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 12:05:11');
INSERT INTO `audit_logs` VALUES (82, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 12:11:15');
INSERT INTO `audit_logs` VALUES (83, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 12:13:02');
INSERT INTO `audit_logs` VALUES (84, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 13:05:29');
INSERT INTO `audit_logs` VALUES (85, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 13:35:40');
INSERT INTO `audit_logs` VALUES (86, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 13:50:59');
INSERT INTO `audit_logs` VALUES (87, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 13:55:34');
INSERT INTO `audit_logs` VALUES (88, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 13:58:56');
INSERT INTO `audit_logs` VALUES (89, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 14:03:54');
INSERT INTO `audit_logs` VALUES (90, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 14:13:23');
INSERT INTO `audit_logs` VALUES (91, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 14:24:47');
INSERT INTO `audit_logs` VALUES (92, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 14:26:32');
INSERT INTO `audit_logs` VALUES (93, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 14:30:52');
INSERT INTO `audit_logs` VALUES (94, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 14:35:21');
INSERT INTO `audit_logs` VALUES (95, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 14:39:45');
INSERT INTO `audit_logs` VALUES (96, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 15:31:32');
INSERT INTO `audit_logs` VALUES (97, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 21:32:01');
INSERT INTO `audit_logs` VALUES (98, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 21:42:17');
INSERT INTO `audit_logs` VALUES (99, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 21:52:05');
INSERT INTO `audit_logs` VALUES (100, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 22:14:13');
INSERT INTO `audit_logs` VALUES (101, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 22:16:04');
INSERT INTO `audit_logs` VALUES (102, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 22:20:04');
INSERT INTO `audit_logs` VALUES (103, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 22:21:10');
INSERT INTO `audit_logs` VALUES (104, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 22:27:04');
INSERT INTO `audit_logs` VALUES (105, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 22:27:22');
INSERT INTO `audit_logs` VALUES (106, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 22:29:00');
INSERT INTO `audit_logs` VALUES (107, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 22:31:19');
INSERT INTO `audit_logs` VALUES (108, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 22:34:34');
INSERT INTO `audit_logs` VALUES (109, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 22:34:49');
INSERT INTO `audit_logs` VALUES (110, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 22:38:56');
INSERT INTO `audit_logs` VALUES (111, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 22:39:06');
INSERT INTO `audit_logs` VALUES (112, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 22:57:40');
INSERT INTO `audit_logs` VALUES (113, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 23:11:01');
INSERT INTO `audit_logs` VALUES (114, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 23:11:54');
INSERT INTO `audit_logs` VALUES (115, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 23:13:21');
INSERT INTO `audit_logs` VALUES (116, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 23:16:36');
INSERT INTO `audit_logs` VALUES (117, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-17 23:21:11');
INSERT INTO `audit_logs` VALUES (118, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 10:31:42');
INSERT INTO `audit_logs` VALUES (119, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 10:32:15');
INSERT INTO `audit_logs` VALUES (120, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 10:35:46');
INSERT INTO `audit_logs` VALUES (121, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 10:36:07');
INSERT INTO `audit_logs` VALUES (122, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 10:40:52');
INSERT INTO `audit_logs` VALUES (123, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 10:53:22');
INSERT INTO `audit_logs` VALUES (124, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 11:05:12');
INSERT INTO `audit_logs` VALUES (125, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 11:18:03');
INSERT INTO `audit_logs` VALUES (126, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 11:32:36');
INSERT INTO `audit_logs` VALUES (127, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 11:32:56');
INSERT INTO `audit_logs` VALUES (128, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 11:37:29');
INSERT INTO `audit_logs` VALUES (129, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 11:37:49');
INSERT INTO `audit_logs` VALUES (130, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 11:38:06');
INSERT INTO `audit_logs` VALUES (131, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 13:12:44');
INSERT INTO `audit_logs` VALUES (132, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 13:19:28');
INSERT INTO `audit_logs` VALUES (133, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 14:00:50');
INSERT INTO `audit_logs` VALUES (134, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 14:45:51');
INSERT INTO `audit_logs` VALUES (135, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 15:30:51');
INSERT INTO `audit_logs` VALUES (136, 21, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 15:33:52');
INSERT INTO `audit_logs` VALUES (137, 13, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 15:33:57');
INSERT INTO `audit_logs` VALUES (138, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 15:34:11');
INSERT INTO `audit_logs` VALUES (139, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 15:34:19');
INSERT INTO `audit_logs` VALUES (140, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 15:35:34');
INSERT INTO `audit_logs` VALUES (141, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 15:36:15');
INSERT INTO `audit_logs` VALUES (142, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 15:36:38');
INSERT INTO `audit_logs` VALUES (143, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 15:46:02');
INSERT INTO `audit_logs` VALUES (144, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 15:49:54');
INSERT INTO `audit_logs` VALUES (145, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 15:52:14');
INSERT INTO `audit_logs` VALUES (146, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 16:00:56');
INSERT INTO `audit_logs` VALUES (147, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 16:08:04');
INSERT INTO `audit_logs` VALUES (148, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 16:15:46');
INSERT INTO `audit_logs` VALUES (149, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 16:18:14');
INSERT INTO `audit_logs` VALUES (150, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 16:21:19');
INSERT INTO `audit_logs` VALUES (151, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 16:21:59');
INSERT INTO `audit_logs` VALUES (152, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 16:53:14');
INSERT INTO `audit_logs` VALUES (153, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 16:58:22');
INSERT INTO `audit_logs` VALUES (154, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 17:01:46');
INSERT INTO `audit_logs` VALUES (155, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 17:06:15');
INSERT INTO `audit_logs` VALUES (156, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 17:09:49');
INSERT INTO `audit_logs` VALUES (157, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 17:21:00');
INSERT INTO `audit_logs` VALUES (158, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 17:25:44');
INSERT INTO `audit_logs` VALUES (159, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 17:31:47');
INSERT INTO `audit_logs` VALUES (160, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 17:36:19');
INSERT INTO `audit_logs` VALUES (161, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 17:39:38');
INSERT INTO `audit_logs` VALUES (162, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 17:45:40');
INSERT INTO `audit_logs` VALUES (163, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 17:45:49');
INSERT INTO `audit_logs` VALUES (164, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 17:49:40');
INSERT INTO `audit_logs` VALUES (165, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 17:55:32');
INSERT INTO `audit_logs` VALUES (166, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 17:55:42');
INSERT INTO `audit_logs` VALUES (167, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 18:16:57');
INSERT INTO `audit_logs` VALUES (168, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 18:47:58');
INSERT INTO `audit_logs` VALUES (169, 5, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 18:48:32');
INSERT INTO `audit_logs` VALUES (170, 3, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 18:48:41');
INSERT INTO `audit_logs` VALUES (171, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 18:49:41');
INSERT INTO `audit_logs` VALUES (172, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 18:58:28');
INSERT INTO `audit_logs` VALUES (173, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 19:25:52');
INSERT INTO `audit_logs` VALUES (174, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-18 19:30:44');
INSERT INTO `audit_logs` VALUES (175, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 11:49:50');
INSERT INTO `audit_logs` VALUES (176, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 11:57:08');
INSERT INTO `audit_logs` VALUES (177, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 11:57:54');
INSERT INTO `audit_logs` VALUES (178, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 12:01:48');
INSERT INTO `audit_logs` VALUES (179, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 12:02:00');
INSERT INTO `audit_logs` VALUES (180, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 12:42:41');
INSERT INTO `audit_logs` VALUES (181, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 12:55:57');
INSERT INTO `audit_logs` VALUES (182, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 12:57:01');
INSERT INTO `audit_logs` VALUES (183, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 13:00:47');
INSERT INTO `audit_logs` VALUES (184, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 13:01:15');
INSERT INTO `audit_logs` VALUES (185, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 13:01:30');
INSERT INTO `audit_logs` VALUES (186, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 13:43:43');
INSERT INTO `audit_logs` VALUES (187, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 13:44:06');
INSERT INTO `audit_logs` VALUES (188, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 13:44:23');
INSERT INTO `audit_logs` VALUES (189, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 13:48:19');
INSERT INTO `audit_logs` VALUES (190, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 13:51:52');
INSERT INTO `audit_logs` VALUES (191, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 13:52:00');
INSERT INTO `audit_logs` VALUES (192, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 13:52:17');
INSERT INTO `audit_logs` VALUES (193, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 13:52:43');
INSERT INTO `audit_logs` VALUES (194, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 13:56:58');
INSERT INTO `audit_logs` VALUES (195, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 14:04:41');
INSERT INTO `audit_logs` VALUES (196, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 14:09:53');
INSERT INTO `audit_logs` VALUES (197, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 14:19:43');
INSERT INTO `audit_logs` VALUES (198, 12, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 14:19:57');
INSERT INTO `audit_logs` VALUES (199, 6, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 14:20:04');
INSERT INTO `audit_logs` VALUES (200, 3, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 15:50:53');
INSERT INTO `audit_logs` VALUES (201, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 16:06:25');
INSERT INTO `audit_logs` VALUES (202, 13, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 16:35:00');
INSERT INTO `audit_logs` VALUES (203, 6, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 16:35:09');
INSERT INTO `audit_logs` VALUES (204, 12, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 16:35:22');
INSERT INTO `audit_logs` VALUES (205, 3, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 16:35:26');
INSERT INTO `audit_logs` VALUES (206, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 16:35:43');
INSERT INTO `audit_logs` VALUES (207, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 16:35:54');
INSERT INTO `audit_logs` VALUES (208, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 16:49:24');
INSERT INTO `audit_logs` VALUES (209, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 17:05:35');
INSERT INTO `audit_logs` VALUES (210, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 17:13:34');
INSERT INTO `audit_logs` VALUES (211, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 17:15:23');
INSERT INTO `audit_logs` VALUES (212, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 17:18:47');
INSERT INTO `audit_logs` VALUES (213, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 17:26:59');
INSERT INTO `audit_logs` VALUES (214, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 17:33:54');
INSERT INTO `audit_logs` VALUES (215, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 17:35:26');
INSERT INTO `audit_logs` VALUES (216, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 17:36:46');
INSERT INTO `audit_logs` VALUES (217, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 18:03:05');
INSERT INTO `audit_logs` VALUES (218, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 18:18:50');
INSERT INTO `audit_logs` VALUES (219, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 18:19:52');
INSERT INTO `audit_logs` VALUES (220, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 18:42:26');
INSERT INTO `audit_logs` VALUES (221, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 18:45:37');
INSERT INTO `audit_logs` VALUES (222, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 18:45:52');
INSERT INTO `audit_logs` VALUES (223, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 19:00:32');
INSERT INTO `audit_logs` VALUES (224, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 19:18:16');
INSERT INTO `audit_logs` VALUES (225, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 19:19:02');
INSERT INTO `audit_logs` VALUES (226, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 19:22:57');
INSERT INTO `audit_logs` VALUES (227, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 19:38:29');
INSERT INTO `audit_logs` VALUES (228, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 19:57:44');
INSERT INTO `audit_logs` VALUES (229, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 20:01:31');
INSERT INTO `audit_logs` VALUES (230, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 20:19:53');
INSERT INTO `audit_logs` VALUES (231, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 20:24:40');
INSERT INTO `audit_logs` VALUES (232, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Trae/1.104.3 Chrome/138.0.7204.251 Electron/37.6.1 Safari/537.36', NULL, '2026-01-19 20:53:59');
INSERT INTO `audit_logs` VALUES (233, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Trae/1.104.3 Chrome/138.0.7204.251 Electron/37.6.1 Safari/537.36', NULL, '2026-01-19 21:37:19');
INSERT INTO `audit_logs` VALUES (234, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 21:38:17');
INSERT INTO `audit_logs` VALUES (235, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 21:38:37');
INSERT INTO `audit_logs` VALUES (236, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 21:40:10');
INSERT INTO `audit_logs` VALUES (237, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 21:49:26');
INSERT INTO `audit_logs` VALUES (238, 12, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 22:10:49');
INSERT INTO `audit_logs` VALUES (239, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-19 23:18:01');
INSERT INTO `audit_logs` VALUES (240, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-20 13:12:35');
INSERT INTO `audit_logs` VALUES (241, 10, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-20 13:13:32');
INSERT INTO `audit_logs` VALUES (242, 12, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-20 14:06:42');
INSERT INTO `audit_logs` VALUES (243, 4, 'LOGIN', 'Auth', 'MEDIUM', 'SUCCESS', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', NULL, '2026-01-20 14:07:17');

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
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '体质测评' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of constitution_assessments
-- ----------------------------
INSERT INTO `constitution_assessments` VALUES (1, 4, 'TCM_9', '{\"BALANCED\": 100.0, \"DAMP_HEAT\": 100.0, \"BLOOD_STASIS\": 100.0, \"QI_DEFICIENCY\": 100.0, \"QI_STAGNATION\": 100.0, \"YIN_DEFICIENCY\": 100.0, \"PHLEGM_DAMPNESS\": 100.0, \"YANG_DEFICIENCY\": 100.0, \"SPECIAL_DIATHESIS\": 100.0}', 'QI_DEFICIENCY', '{\"scores\": {\"BALANCED\": 100.0, \"DAMP_HEAT\": 100.0, \"BLOOD_STASIS\": 100.0, \"QI_DEFICIENCY\": 100.0, \"QI_STAGNATION\": 100.0, \"YIN_DEFICIENCY\": 100.0, \"PHLEGM_DAMPNESS\": 100.0, \"YANG_DEFICIENCY\": 100.0, \"SPECIAL_DIATHESIS\": 100.0}, \"summary\": \"气虚体质，需注意增强体能与免疫力。\", \"primaryType\": \"QI_DEFICIENCY\", \"recommendations\": [\"适当进行太极、散步等轻度运动\", \"饮食以温补为主，如黄芪、党参\", \"保证充足睡眠\"]}', '2025-11-13 11:28:32', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (2, 4, 'TCM_9', '{\"BALANCED\": 0.0, \"DAMP_HEAT\": 0.0, \"BLOOD_STASIS\": 0.0, \"QI_DEFICIENCY\": 0.0, \"QI_STAGNATION\": 0.0, \"YIN_DEFICIENCY\": 0.0, \"PHLEGM_DAMPNESS\": 0.0, \"YANG_DEFICIENCY\": 0.0, \"SPECIAL_DIATHESIS\": 0.0}', 'BALANCED', '{\"scores\": {\"BALANCED\": 0.0, \"DAMP_HEAT\": 0.0, \"BLOOD_STASIS\": 0.0, \"QI_DEFICIENCY\": 0.0, \"QI_STAGNATION\": 0.0, \"YIN_DEFICIENCY\": 0.0, \"PHLEGM_DAMPNESS\": 0.0, \"YANG_DEFICIENCY\": 0.0, \"SPECIAL_DIATHESIS\": 0.0}, \"summary\": \"体质平衡，保持良好生活方式。\", \"primaryType\": \"BALANCED\", \"recommendations\": [\"保持规律作息\", \"坚持适量运动\", \"维持乐观心态\"]}', '2025-11-13 11:39:28', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (3, 5, 'TCM_9', '{\"BALANCED\": 60.0, \"DAMP_HEAT\": 20.0, \"BLOOD_STASIS\": 0.0, \"QI_DEFICIENCY\": 20.0, \"QI_STAGNATION\": 80.0, \"YIN_DEFICIENCY\": 40.0, \"PHLEGM_DAMPNESS\": 100.0, \"YANG_DEFICIENCY\": 80.0, \"SPECIAL_DIATHESIS\": 40.0}', 'PHLEGM_DAMPNESS', '{\"scores\": {\"BALANCED\": 60.0, \"DAMP_HEAT\": 20.0, \"BLOOD_STASIS\": 0.0, \"QI_DEFICIENCY\": 20.0, \"QI_STAGNATION\": 80.0, \"YIN_DEFICIENCY\": 40.0, \"PHLEGM_DAMPNESS\": 100.0, \"YANG_DEFICIENCY\": 80.0, \"SPECIAL_DIATHESIS\": 40.0}, \"summary\": \"痰湿体质，建议加强运动与调节饮食。\", \"primaryType\": \"PHLEGM_DAMPNESS\", \"recommendations\": [\"控制高脂饮食，增加蔬果摄入\", \"坚持有氧运动\", \"保持充足睡眠与体重管理\"]}', '2025-11-13 11:50:58', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (4, 4, 'TCM_9', '{\"BALANCED\": 0.0, \"DAMP_HEAT\": 0.0, \"BLOOD_STASIS\": 0.0, \"QI_DEFICIENCY\": 0.0, \"QI_STAGNATION\": 0.0, \"YIN_DEFICIENCY\": 0.0, \"PHLEGM_DAMPNESS\": 0.0, \"YANG_DEFICIENCY\": 0.0, \"SPECIAL_DIATHESIS\": 0.0}', 'BALANCED', '{\"scores\": {\"BALANCED\": 0.0, \"DAMP_HEAT\": 0.0, \"BLOOD_STASIS\": 0.0, \"QI_DEFICIENCY\": 0.0, \"QI_STAGNATION\": 0.0, \"YIN_DEFICIENCY\": 0.0, \"PHLEGM_DAMPNESS\": 0.0, \"YANG_DEFICIENCY\": 0.0, \"SPECIAL_DIATHESIS\": 0.0}, \"summary\": \"体质平衡，保持良好生活方式。\", \"primaryType\": \"BALANCED\", \"recommendations\": [\"保持规律作息\", \"坚持适量运动\", \"维持乐观心态\"]}', '2025-11-13 12:11:41', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (5, 4, 'TCM_9', '{\"BALANCED\": 20.0, \"DAMP_HEAT\": 60.0, \"BLOOD_STASIS\": 20.0, \"QI_DEFICIENCY\": 40.0, \"QI_STAGNATION\": 60.0, \"YIN_DEFICIENCY\": 20.0, \"PHLEGM_DAMPNESS\": 0.0, \"YANG_DEFICIENCY\": 60.0, \"SPECIAL_DIATHESIS\": 20.0}', 'DAMP_HEAT', '{\"scores\": {\"BALANCED\": 20.0, \"DAMP_HEAT\": 60.0, \"BLOOD_STASIS\": 20.0, \"QI_DEFICIENCY\": 40.0, \"QI_STAGNATION\": 60.0, \"YIN_DEFICIENCY\": 20.0, \"PHLEGM_DAMPNESS\": 0.0, \"YANG_DEFICIENCY\": 60.0, \"SPECIAL_DIATHESIS\": 20.0}, \"summary\": \"湿热体质，少食辛辣油腻，保持清淡饮食。\", \"confidence\": 0.4, \"primaryType\": \"DAMP_HEAT\", \"recommendations\": [\"少食辛辣油腻，清淡饮食\", \"保持环境通风与体温平衡\", \"适量饮用菊花茶等清热饮品\"]}', '2025-11-13 12:17:39', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (6, 4, 'TCM_9', '{\"BALANCED\": 20.0, \"DAMP_HEAT\": 60.0, \"BLOOD_STASIS\": 60.0, \"QI_DEFICIENCY\": 80.0, \"QI_STAGNATION\": 80.0, \"YIN_DEFICIENCY\": 40.0, \"PHLEGM_DAMPNESS\": 80.0, \"YANG_DEFICIENCY\": 20.0, \"SPECIAL_DIATHESIS\": 40.0}', 'QI_DEFICIENCY', '{\"scores\": {\"BALANCED\": 20.0, \"DAMP_HEAT\": 60.0, \"BLOOD_STASIS\": 60.0, \"QI_DEFICIENCY\": 80.0, \"QI_STAGNATION\": 80.0, \"YIN_DEFICIENCY\": 40.0, \"PHLEGM_DAMPNESS\": 80.0, \"YANG_DEFICIENCY\": 20.0, \"SPECIAL_DIATHESIS\": 40.0}, \"summary\": \"气虚体质，需注意增强体能与免疫力。\", \"confidence\": 0.4, \"primaryType\": \"QI_DEFICIENCY\", \"recommendations\": [\"适当进行太极、散步等轻度运动\", \"饮食以温补为主，如黄芪、党参\", \"保证充足睡眠\"]}', '2025-11-13 12:27:32', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (7, 4, 'TCM_9', '{\"BALANCED\": 0.0, \"DAMP_HEAT\": 0.0, \"BLOOD_STASIS\": 0.0, \"QI_DEFICIENCY\": 0.0, \"QI_STAGNATION\": 0.0, \"YIN_DEFICIENCY\": 0.0, \"PHLEGM_DAMPNESS\": 0.0, \"YANG_DEFICIENCY\": 0.0, \"SPECIAL_DIATHESIS\": 0.0}', 'BALANCED', '{\"scores\": {\"BALANCED\": 0.0, \"DAMP_HEAT\": 0.0, \"BLOOD_STASIS\": 0.0, \"QI_DEFICIENCY\": 0.0, \"QI_STAGNATION\": 0.0, \"YIN_DEFICIENCY\": 0.0, \"PHLEGM_DAMPNESS\": 0.0, \"YANG_DEFICIENCY\": 0.0, \"SPECIAL_DIATHESIS\": 0.0}, \"summary\": \"体质平衡，保持良好生活方式。\", \"confidence\": 0.4, \"primaryType\": \"BALANCED\", \"recommendations\": [\"保持规律作息\", \"坚持适量运动\", \"维持乐观心态\"]}', '2025-11-13 13:21:51', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (8, 4, 'TCM_9', '{\"BALANCED\": 20.0, \"DAMP_HEAT\": 60.0, \"BLOOD_STASIS\": 40.0, \"QI_DEFICIENCY\": 80.0, \"QI_STAGNATION\": 80.0, \"YIN_DEFICIENCY\": 40.0, \"PHLEGM_DAMPNESS\": 60.0, \"YANG_DEFICIENCY\": 20.0, \"SPECIAL_DIATHESIS\": 100.0}', 'SPECIAL_DIATHESIS', '{\"scores\": {\"BALANCED\": 20.0, \"DAMP_HEAT\": 60.0, \"BLOOD_STASIS\": 40.0, \"QI_DEFICIENCY\": 80.0, \"QI_STAGNATION\": 80.0, \"YIN_DEFICIENCY\": 40.0, \"PHLEGM_DAMPNESS\": 60.0, \"YANG_DEFICIENCY\": 20.0, \"SPECIAL_DIATHESIS\": 100.0}, \"summary\": \"特禀体质，注意过敏源管理与防护。\", \"confidence\": 0.8, \"primaryType\": \"SPECIAL_DIATHESIS\", \"recommendations\": [\"远离过敏源，做好环境防护\", \"保持卫生，避免感染\", \"遵循医师指导服用药物\"]}', '2025-11-13 13:22:47', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (9, 4, 'TCM_9', '{\"BALANCED\": 20.0, \"DAMP_HEAT\": 60.0, \"BLOOD_STASIS\": 20.0, \"QI_DEFICIENCY\": 60.0, \"QI_STAGNATION\": 40.0, \"YIN_DEFICIENCY\": 40.0, \"PHLEGM_DAMPNESS\": 20.0, \"YANG_DEFICIENCY\": 0.0, \"SPECIAL_DIATHESIS\": 100.0}', 'SPECIAL_DIATHESIS', '{\"scores\": {\"BALANCED\": 20.0, \"DAMP_HEAT\": 60.0, \"BLOOD_STASIS\": 20.0, \"QI_DEFICIENCY\": 60.0, \"QI_STAGNATION\": 40.0, \"YIN_DEFICIENCY\": 40.0, \"PHLEGM_DAMPNESS\": 20.0, \"YANG_DEFICIENCY\": 0.0, \"SPECIAL_DIATHESIS\": 100.0}, \"summary\": \"特禀体质，注意过敏源管理与防护。\", \"confidence\": 1.0, \"primaryType\": \"SPECIAL_DIATHESIS\", \"recommendations\": [\"远离过敏源，做好环境防护\", \"保持卫生，避免感染\", \"遵循医师指导服用药物\"]}', '2025-11-13 13:23:48', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (10, 4, 'TCM_9', '{\"BALANCED\": 60.0, \"DAMP_HEAT\": 60.0, \"BLOOD_STASIS\": 60.0, \"QI_DEFICIENCY\": 60.0, \"QI_STAGNATION\": 60.0, \"YIN_DEFICIENCY\": 60.0, \"PHLEGM_DAMPNESS\": 60.0, \"YANG_DEFICIENCY\": 60.0, \"SPECIAL_DIATHESIS\": 60.0}', 'QI_DEFICIENCY', '{\"scores\": {\"BALANCED\": 60.0, \"DAMP_HEAT\": 60.0, \"BLOOD_STASIS\": 60.0, \"QI_DEFICIENCY\": 60.0, \"QI_STAGNATION\": 60.0, \"YIN_DEFICIENCY\": 60.0, \"PHLEGM_DAMPNESS\": 60.0, \"YANG_DEFICIENCY\": 60.0, \"SPECIAL_DIATHESIS\": 60.0}, \"summary\": \"气虚体质，需注意增强体能与免疫力。\", \"confidence\": 0.4, \"primaryType\": \"QI_DEFICIENCY\", \"recommendations\": [\"适当进行太极、散步等轻度运动\", \"饮食以温补为主，如黄芪、党参\", \"保证充足睡眠\"]}', '2025-11-13 13:24:50', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (11, 4, 'TCM_9', '{\"BALANCED\": 0.0, \"DAMP_HEAT\": 0.0, \"BLOOD_STASIS\": 0.0, \"QI_DEFICIENCY\": 0.0, \"QI_STAGNATION\": 0.0, \"YIN_DEFICIENCY\": 0.0, \"PHLEGM_DAMPNESS\": 0.0, \"YANG_DEFICIENCY\": 0.0, \"SPECIAL_DIATHESIS\": 0.0}', 'BALANCED', '{\"scores\": {\"BALANCED\": 0.0, \"DAMP_HEAT\": 0.0, \"BLOOD_STASIS\": 0.0, \"QI_DEFICIENCY\": 0.0, \"QI_STAGNATION\": 0.0, \"YIN_DEFICIENCY\": 0.0, \"PHLEGM_DAMPNESS\": 0.0, \"YANG_DEFICIENCY\": 0.0, \"SPECIAL_DIATHESIS\": 0.0}, \"summary\": \"体质平衡，保持良好生活方式。\", \"confidence\": 0.4, \"primaryType\": \"BALANCED\", \"recommendations\": [\"保持规律作息\", \"坚持适量运动\", \"维持乐观心态\"]}', '2025-11-13 13:31:00', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (12, 4, 'TCM_9', '{\"BALANCED\": 100.0, \"DAMP_HEAT\": 100.0, \"BLOOD_STASIS\": 100.0, \"QI_DEFICIENCY\": 100.0, \"QI_STAGNATION\": 100.0, \"YIN_DEFICIENCY\": 100.0, \"PHLEGM_DAMPNESS\": 100.0, \"YANG_DEFICIENCY\": 100.0, \"SPECIAL_DIATHESIS\": 100.0}', 'QI_DEFICIENCY', '{\"scores\": {\"BALANCED\": 100.0, \"DAMP_HEAT\": 100.0, \"BLOOD_STASIS\": 100.0, \"QI_DEFICIENCY\": 100.0, \"QI_STAGNATION\": 100.0, \"YIN_DEFICIENCY\": 100.0, \"PHLEGM_DAMPNESS\": 100.0, \"YANG_DEFICIENCY\": 100.0, \"SPECIAL_DIATHESIS\": 100.0}, \"summary\": \"气虚体质，需注意增强体能与免疫力。\", \"confidence\": 0.4, \"primaryType\": \"QI_DEFICIENCY\", \"recommendations\": [\"适当进行太极、散步等轻度运动\", \"饮食以温补为主，如黄芪、党参\", \"保证充足睡眠\"]}', '2025-11-13 13:56:13', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (13, 3, 'TCM_9', '{\"BALANCED\": 0.0, \"DAMP_HEAT\": 0.0, \"BLOOD_STASIS\": 0.0, \"QI_DEFICIENCY\": 0.0, \"QI_STAGNATION\": 0.0, \"YIN_DEFICIENCY\": 0.0, \"PHLEGM_DAMPNESS\": 0.0, \"YANG_DEFICIENCY\": 0.0, \"SPECIAL_DIATHESIS\": 0.0}', 'BALANCED', '{\"scores\": {\"BALANCED\": 0.0, \"DAMP_HEAT\": 0.0, \"BLOOD_STASIS\": 0.0, \"QI_DEFICIENCY\": 0.0, \"QI_STAGNATION\": 0.0, \"YIN_DEFICIENCY\": 0.0, \"PHLEGM_DAMPNESS\": 0.0, \"YANG_DEFICIENCY\": 0.0, \"SPECIAL_DIATHESIS\": 0.0}, \"summary\": \"体质平衡，保持良好生活方式。\", \"confidence\": 0.4, \"primaryType\": \"BALANCED\", \"recommendations\": [\"保持规律作息\", \"坚持适量运动\", \"维持乐观心态\"]}', '2025-11-13 14:20:51', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (14, 4, 'TCM_9', '{\"BALANCED\": 80.0, \"DAMP_HEAT\": 20.0, \"BLOOD_STASIS\": 100.0, \"QI_DEFICIENCY\": 100.0, \"QI_STAGNATION\": 100.0, \"YIN_DEFICIENCY\": 60.0, \"PHLEGM_DAMPNESS\": 40.0, \"YANG_DEFICIENCY\": 40.0, \"SPECIAL_DIATHESIS\": 100.0}', 'QI_DEFICIENCY', '{\"scores\": {\"BALANCED\": 80.0, \"DAMP_HEAT\": 20.0, \"BLOOD_STASIS\": 100.0, \"QI_DEFICIENCY\": 100.0, \"QI_STAGNATION\": 100.0, \"YIN_DEFICIENCY\": 60.0, \"PHLEGM_DAMPNESS\": 40.0, \"YANG_DEFICIENCY\": 40.0, \"SPECIAL_DIATHESIS\": 100.0}, \"summary\": \"气虚体质，需注意增强体能与免疫力。\", \"confidence\": 0.4, \"primaryType\": \"QI_DEFICIENCY\", \"recommendations\": [\"适当进行太极、散步等轻度运动\", \"饮食以温补为主，如黄芪、党参\", \"保证充足睡眠\"]}', '2025-11-13 15:39:45', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (15, 4, 'TCM_9', '{\"BALANCED\": 0.0, \"DAMP_HEAT\": 0.0, \"BLOOD_STASIS\": 0.0, \"QI_DEFICIENCY\": 0.0, \"QI_STAGNATION\": 0.0, \"YIN_DEFICIENCY\": 0.0, \"PHLEGM_DAMPNESS\": 0.0, \"YANG_DEFICIENCY\": 0.0, \"SPECIAL_DIATHESIS\": 0.0}', 'BALANCED', '{\"scores\": {\"BALANCED\": 0.0, \"DAMP_HEAT\": 0.0, \"BLOOD_STASIS\": 0.0, \"QI_DEFICIENCY\": 0.0, \"QI_STAGNATION\": 0.0, \"YIN_DEFICIENCY\": 0.0, \"PHLEGM_DAMPNESS\": 0.0, \"YANG_DEFICIENCY\": 0.0, \"SPECIAL_DIATHESIS\": 0.0}, \"summary\": \"体质平衡，保持良好生活方式。\", \"confidence\": 0.4, \"primaryType\": \"BALANCED\", \"recommendations\": [\"保持规律作息\", \"坚持适量运动\", \"维持乐观心态\"]}', '2025-11-13 15:51:36', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (16, 4, 'TCM_9', '{\"BALANCED\": 0.0, \"DAMP_HEAT\": 0.0, \"BLOOD_STASIS\": 0.0, \"QI_DEFICIENCY\": 0.0, \"QI_STAGNATION\": 0.0, \"YIN_DEFICIENCY\": 0.0, \"PHLEGM_DAMPNESS\": 0.0, \"YANG_DEFICIENCY\": 0.0, \"SPECIAL_DIATHESIS\": 0.0}', 'BALANCED', '{\"scores\": {\"BALANCED\": 0.0, \"DAMP_HEAT\": 0.0, \"BLOOD_STASIS\": 0.0, \"QI_DEFICIENCY\": 0.0, \"QI_STAGNATION\": 0.0, \"YIN_DEFICIENCY\": 0.0, \"PHLEGM_DAMPNESS\": 0.0, \"YANG_DEFICIENCY\": 0.0, \"SPECIAL_DIATHESIS\": 0.0}, \"summary\": \"体质平衡，保持良好生活方式。\", \"confidence\": 0.4, \"primaryType\": \"BALANCED\", \"recommendations\": [\"保持规律作息\", \"坚持适量运动\", \"维持乐观心态\"]}', '2025-11-13 16:57:09', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (17, 4, 'TCM_9', '{\"BALANCED\": 100.0, \"DAMP_HEAT\": 100.0, \"BLOOD_STASIS\": 100.0, \"QI_DEFICIENCY\": 100.0, \"QI_STAGNATION\": 100.0, \"YIN_DEFICIENCY\": 100.0, \"PHLEGM_DAMPNESS\": 100.0, \"YANG_DEFICIENCY\": 100.0, \"SPECIAL_DIATHESIS\": 100.0}', 'QI_DEFICIENCY', '{\"scores\": {\"BALANCED\": 100.0, \"DAMP_HEAT\": 100.0, \"BLOOD_STASIS\": 100.0, \"QI_DEFICIENCY\": 100.0, \"QI_STAGNATION\": 100.0, \"YIN_DEFICIENCY\": 100.0, \"PHLEGM_DAMPNESS\": 100.0, \"YANG_DEFICIENCY\": 100.0, \"SPECIAL_DIATHESIS\": 100.0}, \"summary\": \"气虚体质，需注意增强体能与免疫力。\", \"confidence\": 0.4, \"primaryType\": \"QI_DEFICIENCY\", \"recommendations\": [\"适当进行太极、散步等轻度运动\", \"饮食以温补为主，如黄芪、党参\", \"保证充足睡眠\"]}', '2025-11-14 09:11:11', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (18, 4, 'TCM_9', '{\"BALANCED\": 100.0, \"DAMP_HEAT\": 100.0, \"BLOOD_STASIS\": 100.0, \"QI_DEFICIENCY\": 100.0, \"QI_STAGNATION\": 100.0, \"YIN_DEFICIENCY\": 100.0, \"PHLEGM_DAMPNESS\": 100.0, \"YANG_DEFICIENCY\": 100.0, \"SPECIAL_DIATHESIS\": 100.0}', 'QI_DEFICIENCY', '{\"scores\": {\"BALANCED\": 100.0, \"DAMP_HEAT\": 100.0, \"BLOOD_STASIS\": 100.0, \"QI_DEFICIENCY\": 100.0, \"QI_STAGNATION\": 100.0, \"YIN_DEFICIENCY\": 100.0, \"PHLEGM_DAMPNESS\": 100.0, \"YANG_DEFICIENCY\": 100.0, \"SPECIAL_DIATHESIS\": 100.0}, \"summary\": \"气虚体质，需注意增强体能与免疫力。\", \"confidence\": 0.4, \"primaryType\": \"QI_DEFICIENCY\", \"recommendations\": [\"适当进行太极、散步等轻度运动\", \"饮食以温补为主，如黄芪、党参\", \"保证充足睡眠\"]}', '2025-11-14 13:02:22', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (19, 3, 'TCM_9', '{\"BALANCED\": 100.0, \"DAMP_HEAT\": 100.0, \"BLOOD_STASIS\": 100.0, \"QI_DEFICIENCY\": 100.0, \"QI_STAGNATION\": 100.0, \"YIN_DEFICIENCY\": 100.0, \"PHLEGM_DAMPNESS\": 100.0, \"YANG_DEFICIENCY\": 100.0, \"SPECIAL_DIATHESIS\": 100.0}', 'QI_DEFICIENCY', '{\"scores\": {\"BALANCED\": 100.0, \"DAMP_HEAT\": 100.0, \"BLOOD_STASIS\": 100.0, \"QI_DEFICIENCY\": 100.0, \"QI_STAGNATION\": 100.0, \"YIN_DEFICIENCY\": 100.0, \"PHLEGM_DAMPNESS\": 100.0, \"YANG_DEFICIENCY\": 100.0, \"SPECIAL_DIATHESIS\": 100.0}, \"summary\": \"气虚体质，需注意增强体能与免疫力。\", \"confidence\": 0.4, \"primaryType\": \"QI_DEFICIENCY\", \"recommendations\": [\"适当进行太极、散步等轻度运动\", \"饮食以温补为主，如黄芪、党参\", \"保证充足睡眠\"]}', '2025-11-14 13:36:31', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (20, 6, 'TCM_9', '{\"BALANCED\": 100.0, \"DAMP_HEAT\": 40.0, \"BLOOD_STASIS\": 20.0, \"QI_DEFICIENCY\": 60.0, \"QI_STAGNATION\": 60.0, \"YIN_DEFICIENCY\": 60.0, \"PHLEGM_DAMPNESS\": 80.0, \"YANG_DEFICIENCY\": 80.0, \"SPECIAL_DIATHESIS\": 80.0}', 'BALANCED', '{\"scores\": {\"BALANCED\": 100.0, \"DAMP_HEAT\": 40.0, \"BLOOD_STASIS\": 20.0, \"QI_DEFICIENCY\": 60.0, \"QI_STAGNATION\": 60.0, \"YIN_DEFICIENCY\": 60.0, \"PHLEGM_DAMPNESS\": 80.0, \"YANG_DEFICIENCY\": 80.0, \"SPECIAL_DIATHESIS\": 80.0}, \"summary\": \"体质平衡，保持良好生活方式。\", \"confidence\": 0.8, \"primaryType\": \"BALANCED\", \"recommendations\": [\"保持规律作息\", \"坚持适量运动\", \"维持乐观心态\"]}', '2025-12-25 16:12:38', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (21, 3, 'TCM_9', '{\"BALANCED\": 80.0, \"DAMP_HEAT\": 100.0, \"BLOOD_STASIS\": 0.0, \"QI_DEFICIENCY\": 60.0, \"QI_STAGNATION\": 60.0, \"YIN_DEFICIENCY\": 40.0, \"PHLEGM_DAMPNESS\": 20.0, \"YANG_DEFICIENCY\": 100.0, \"SPECIAL_DIATHESIS\": 40.0}', 'DAMP_HEAT', '{\"scores\": {\"BALANCED\": 80.0, \"DAMP_HEAT\": 100.0, \"BLOOD_STASIS\": 0.0, \"QI_DEFICIENCY\": 60.0, \"QI_STAGNATION\": 60.0, \"YIN_DEFICIENCY\": 40.0, \"PHLEGM_DAMPNESS\": 20.0, \"YANG_DEFICIENCY\": 100.0, \"SPECIAL_DIATHESIS\": 40.0}, \"summary\": \"湿热体质，少食辛辣油腻，保持清淡饮食。\", \"confidence\": 0.4, \"primaryType\": \"DAMP_HEAT\", \"recommendations\": [\"少食辛辣油腻，清淡饮食\", \"保持环境通风与体温平衡\", \"适量饮用菊花茶等清热饮品\"]}', '2025-12-25 19:58:30', '1.0', 0.50, NULL, NULL, 1, NULL, 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (22, 3, 'TCM_9', '{\"BALANCED\": 100.0, \"DAMP_HEAT\": 80.0, \"BLOOD_STASIS\": 60.0, \"QI_DEFICIENCY\": 80.0, \"QI_STAGNATION\": 40.0, \"YIN_DEFICIENCY\": 60.0, \"PHLEGM_DAMPNESS\": 40.0, \"YANG_DEFICIENCY\": 40.0, \"SPECIAL_DIATHESIS\": 100.0}', 'BALANCED', '{\"scores\": {\"BALANCED\": 100.0, \"DAMP_HEAT\": 80.0, \"BLOOD_STASIS\": 60.0, \"QI_DEFICIENCY\": 80.0, \"QI_STAGNATION\": 40.0, \"YIN_DEFICIENCY\": 60.0, \"PHLEGM_DAMPNESS\": 40.0, \"YANG_DEFICIENCY\": 40.0, \"SPECIAL_DIATHESIS\": 100.0}, \"summary\": \"体质平衡，保持良好生活方式。\", \"confidence\": 0.4, \"primaryType\": \"BALANCED\", \"recommendations\": [\"保持规律作息\", \"坚持适量运动\", \"维持乐观心态\", \"注重四季调养，顺应自然规律\"]}', '2026-01-15 08:49:47', '1.0', 0.40, NULL, NULL, 1, '2026-01-15 08:49:47.440749', 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (23, 4, 'TCM_9', '{\"BALANCED\": 40.0, \"DAMP_HEAT\": 40.0, \"BLOOD_STASIS\": 20.0, \"QI_DEFICIENCY\": 60.0, \"QI_STAGNATION\": 80.0, \"YIN_DEFICIENCY\": 0.0, \"PHLEGM_DAMPNESS\": 80.0, \"YANG_DEFICIENCY\": 20.0, \"SPECIAL_DIATHESIS\": 20.0}', 'PHLEGM_DAMPNESS', '{\"scores\": {\"BALANCED\": 40.0, \"DAMP_HEAT\": 40.0, \"BLOOD_STASIS\": 20.0, \"QI_DEFICIENCY\": 60.0, \"QI_STAGNATION\": 80.0, \"YIN_DEFICIENCY\": 0.0, \"PHLEGM_DAMPNESS\": 80.0, \"YANG_DEFICIENCY\": 20.0, \"SPECIAL_DIATHESIS\": 20.0}, \"summary\": \"痰湿体质，建议加强运动与调节饮食。\", \"confidence\": 0.4, \"primaryType\": \"PHLEGM_DAMPNESS\", \"recommendations\": [\"控制高脂饮食，增加蔬果摄入\", \"坚持有氧运动，促进水湿运化\", \"保持充足睡眠与体重管理\", \"可尝试按摩丰隆、阴陵泉、足三里等穴位\"]}', '2026-01-15 12:11:17', '1.0', 0.40, NULL, NULL, 1, '2026-01-15 12:11:17.405210', 'MANUAL', NULL);
INSERT INTO `constitution_assessments` VALUES (24, 4, 'STANDARD_V1', '{\"BALANCED\": 38.0, \"DAMP_HEAT\": 63.0, \"BLOOD_STASIS\": 44.0, \"QI_DEFICIENCY\": 69.0, \"QI_STAGNATION\": 44.0, \"YIN_DEFICIENCY\": 38.0, \"PHLEGM_DAMPNESS\": 44.0, \"YANG_DEFICIENCY\": 44.0, \"SPECIAL_DIATHESIS\": 69.0}', 'QI_DEFICIENCY', '{\"scores\": {\"BALANCED\": 38.0, \"DAMP_HEAT\": 63.0, \"BLOOD_STASIS\": 44.0, \"QI_DEFICIENCY\": 69.0, \"QI_STAGNATION\": 44.0, \"YIN_DEFICIENCY\": 38.0, \"PHLEGM_DAMPNESS\": 44.0, \"YANG_DEFICIENCY\": 44.0, \"SPECIAL_DIATHESIS\": 69.0}, \"summary\": \"气虚体质，需注意增强体能与免疫力。\", \"confidence\": 0.4, \"primaryType\": \"QI_DEFICIENCY\", \"recommendations\": [\"适当进行太极、散步等轻度运动\", \"饮食以温补为主，如黄芪、党参、山药、大枣\", \"保证充足睡眠，避免过度劳累\", \"可尝试艾灸关元、气海、足三里等穴位\"]}', '2026-01-15 13:02:12', '1.0', 0.40, NULL, NULL, 1, '2026-01-15 13:02:11.803209', 'MANUAL', NULL);

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '体质变化趋势记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of constitution_trend_records
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '咨询消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of consultation_messages
-- ----------------------------
INSERT INTO `consultation_messages` VALUES (1, 1, 3, 'MEMBER', '我测你妈', 'TEXT', NULL, 1, 1, '2025-12-29 20:08:01');
INSERT INTO `consultation_messages` VALUES (2, 1, 10, 'DOCTOR', '您好，有什么可以帮您的吗？', 'TEXT', NULL, 1, 0, '2025-12-29 20:13:08');
INSERT INTO `consultation_messages` VALUES (3, 1, 3, 'MEMBER', '傻逼', 'TEXT', NULL, 1, 1, '2025-12-29 20:13:29');
INSERT INTO `consultation_messages` VALUES (4, 1, 3, 'MEMBER', '想打搅了', 'TEXT', NULL, 1, 1, '2025-12-29 20:29:30');
INSERT INTO `consultation_messages` VALUES (5, 2, 4, 'MEMBER', '你好', 'TEXT', NULL, 1, 1, '2026-01-16 10:57:04');
INSERT INTO `consultation_messages` VALUES (6, 2, 10, 'DOCTOR', '建议您定期复查，如有不适请及时就医。', 'TEXT', NULL, 1, 1, '2026-01-16 10:57:27');
INSERT INTO `consultation_messages` VALUES (7, 1, 4, 'FAMILY_MEMBER', '什么意思\n', 'TEXT', NULL, 1, 1, '2026-01-18 12:09:54');
INSERT INTO `consultation_messages` VALUES (8, 2, 10, 'DOCTOR', '好的', 'TEXT', NULL, 1, 1, '2026-01-19 21:38:25');
INSERT INTO `consultation_messages` VALUES (9, 2, 4, 'MEMBER', '干啥呢', 'TEXT', NULL, 1, 1, '2026-01-19 21:48:54');
INSERT INTO `consultation_messages` VALUES (10, 2, 10, 'DOCTOR', '我有的是权利', 'TEXT', NULL, 1, 1, '2026-01-19 21:49:33');
INSERT INTO `consultation_messages` VALUES (11, 2, 10, 'DOCTOR', '次次次', 'TEXT', NULL, 1, 1, '2026-01-19 21:51:48');
INSERT INTO `consultation_messages` VALUES (12, 2, 10, 'DOCTOR', '114514', 'TEXT', NULL, 1, 1, '2026-01-19 21:51:51');

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
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '在线咨询会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of consultation_sessions
-- ----------------------------
INSERT INTO `consultation_sessions` VALUES (1, 3, 1, 10, '与王士鑫的咨询', 'ACTIVE', '2026-01-18 12:09:54', 0, 1, '2025-12-29 20:07:52', '2026-01-19 13:01:18', NULL, NULL, NULL);
INSERT INTO `consultation_sessions` VALUES (2, 4, 1, 10, '与李骁勐的咨询', 'ACTIVE', '2026-01-19 21:51:51', 0, 0, '2026-01-14 11:50:09', '2026-01-19 21:52:00', NULL, NULL, NULL);
INSERT INTO `consultation_sessions` VALUES (3, 5, 1, 10, '与郭兆腾的咨询', 'ACTIVE', '2026-01-17 22:29:18', 0, 0, '2026-01-17 22:29:18', '2026-01-17 22:29:18', NULL, NULL, NULL);
INSERT INTO `consultation_sessions` VALUES (4, 6, 1, 10, '与徐志永的咨询', 'ACTIVE', '2026-01-17 22:29:18', 0, 0, '2026-01-17 22:29:18', '2026-01-17 22:29:18', NULL, NULL, NULL);
INSERT INTO `consultation_sessions` VALUES (5, 12, 1, 10, '与王文卓的咨询', 'ACTIVE', '2026-01-17 22:29:19', 0, 0, '2026-01-17 22:29:19', '2026-01-17 22:29:19', NULL, NULL, NULL);
INSERT INTO `consultation_sessions` VALUES (6, 10, 1, 10, '与李田所的咨询', 'ACTIVE', '2026-01-19 21:46:53', 0, 0, '2026-01-19 21:46:53', '2026-01-19 21:46:53', b'0', NULL, NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI预问诊对话详情' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of consultation_triage_chat
-- ----------------------------
INSERT INTO `consultation_triage_chat` VALUES (1, 2, 'USER', '我要找医生', '2026-01-19 19:57:28');
INSERT INTO `consultation_triage_chat` VALUES (2, 2, 'AI', '您好！请坐。您是因为哪里不舒服来就诊的呢？如果有具体的位置或者症状可以告诉我哦。', '2026-01-19 19:57:39');
INSERT INTO `consultation_triage_chat` VALUES (3, 2, 'USER', '你好', '2026-01-19 21:38:58');
INSERT INTO `consultation_triage_chat` VALUES (4, 2, 'AI', '您好！能告诉我您最近有什么不适吗？比如有没有感觉身体哪部分特别不舒服？', '2026-01-19 21:39:10');
INSERT INTO `consultation_triage_chat` VALUES (5, 2, 'USER', '干啥呢', '2026-01-19 21:48:56');
INSERT INTO `consultation_triage_chat` VALUES (6, 2, 'AI', '您是想要了解一些情况还是有具体的不适想告诉我呢？如果是不舒服，能具体描述一下哪里不太舒服吗？比如是头部、胸部还是其他部位。', '2026-01-19 21:49:07');

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '宣教片段库V2' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of doc_fragments_v2
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '医生病历记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of doctor_notes
-- ----------------------------
INSERT INTO `doctor_notes` VALUES (1, 10, 3, 1, '2025-12-29', '想找个女人', '包皮手术', '发炎差点被切了', '作息糜烂', '扔了就行', '不给双倍价钱不去看', '喜欢杨桢桢', '2025-12-29 20:09:36', '2025-12-29 20:09:36');

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生扩展信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of doctor_profiles
-- ----------------------------
INSERT INTO `doctor_profiles` VALUES (10, NULL, NULL, NULL, NULL, NULL, NULL, 'APPROVED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 3.83, 6, 1, '2026-01-19 13:52:39', '2026-01-19 16:35:40');
INSERT INTO `doctor_profiles` VALUES (21, '下北泽医院', '内分泌科', '内分泌失调', '主任医师', '逸一时误一世', '', 'APPROVED', '2026-01-15 19:21:24', 13, NULL, '1145141919810', '', '370911200403278110', '', '', 0.00, 0, 0, '2026-01-15 19:20:55', '2026-01-15 19:21:24');

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
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of doctor_ratings
-- ----------------------------
INSERT INTO `doctor_ratings` VALUES (1, '恶臭无比', '2026-01-19 12:56:05.388260', 10, 3, 4);
INSERT INTO `doctor_ratings` VALUES (2, '114514', '2026-01-19 13:52:38.833793', 10, 1, 4);
INSERT INTO `doctor_ratings` VALUES (3, '好的很', '2026-01-19 13:59:42.436767', 10, 5, 4);
INSERT INTO `doctor_ratings` VALUES (4, '很好', '2026-01-19 13:59:46.724607', 10, 5, 4);
INSERT INTO `doctor_ratings` VALUES (5, '不太行', '2026-01-19 16:35:17.296744', 10, 4, 6);
INSERT INTO `doctor_ratings` VALUES (6, '对我很好他', '2026-01-19 16:35:39.547527', 10, 5, 3);

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
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '家庭' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of families
-- ----------------------------
INSERT INTO `families` VALUES (1, 4, '114514宿舍', '9NKJPATF', '2025-11-13 09:08:08', '2026-01-15 14:00:12', 1);
INSERT INTO `families` VALUES (2, 3, '下北泽', 'R57QLBBZ', '2025-11-13 11:58:02', '2026-01-15 14:00:13', 1);
INSERT INTO `families` VALUES (3, 5, '935', 'X7H94YRT', '2025-11-13 20:17:18', '2026-01-15 14:00:15', 1);

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
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of family_doctors
-- ----------------------------
INSERT INTO `family_doctors` VALUES (3, 1, 10, '2025-11-14 19:34:54');

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '家庭-成员' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of family_members
-- ----------------------------
INSERT INTO `family_members` VALUES (1, 1, 4, 'OWNER', b'1', 'ADMIN', '2025-11-13 09:08:08');
INSERT INTO `family_members` VALUES (2, 1, 3, 'MEMBER', b'0', 'MEMBER', '2025-11-13 09:27:03');
INSERT INTO `family_members` VALUES (3, 1, 5, 'MEMBER', b'0', 'MEMBER', '2025-11-13 11:43:37');
INSERT INTO `family_members` VALUES (4, 2, 3, 'OWNER', b'1', 'ADMIN', '2025-11-13 11:58:02');
INSERT INTO `family_members` VALUES (5, 1, 6, 'MEMBER', b'0', 'MEMBER', '2025-11-13 20:16:57');
INSERT INTO `family_members` VALUES (6, 3, 5, 'OWNER', b'1', 'ADMIN', '2025-11-13 20:17:18');
INSERT INTO `family_members` VALUES (7, 3, 3, 'MEMBER', b'0', 'MEMBER', '2025-11-13 20:17:36');
INSERT INTO `family_members` VALUES (8, 3, 4, 'MEMBER', b'1', 'MEMBER', '2025-11-13 20:17:45');
INSERT INTO `family_members` VALUES (9, 3, 6, 'MEMBER', b'0', 'MEMBER', '2025-11-13 20:17:53');
INSERT INTO `family_members` VALUES (11, 1, 12, 'MEMBER', b'0', 'MEMBER', '2025-12-29 20:10:55');

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '家庭中医健康概览' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of family_tcm_health_overviews
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of health_alerts
-- ----------------------------
INSERT INTO `health_alerts` VALUES (1, 4, 1, 'HR', 80, 0, 'CRITICAL', 'ESCALATED', 'EMERGENCY', 2, '2025-11-14 17:09:05', '2025-11-14 17:19:31', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `health_alerts` VALUES (2, 6, 1, '血压', 135, 0, 'HIGH', 'ACKED', 'SYSTEM', NULL, '2026-01-16 14:18:48', '2026-01-16 14:18:48', '2026-01-16 14:18:48', 10, '', 'SENT', '2026-01-16 14:18:48', '您的血压值异常（135），请立即测量并记录，如有不适请及时就医。', '系统自动检测异常');
INSERT INTO `health_alerts` VALUES (3, 6, 1, '血压', 135, 0, 'HIGH', 'ACKED', 'SYSTEM', NULL, '2026-01-16 14:26:21', '2026-01-16 14:26:21', '2026-01-16 14:26:21', 10, '', 'SENT', '2026-01-16 14:26:21', '您的血压值异常（135），请立即测量并记录，如有不适请及时就医。', '系统自动检测异常');
INSERT INTO `health_alerts` VALUES (4, 6, 1, '血压', 130, 0, 'HIGH', 'ACKED', 'SYSTEM', NULL, '2026-01-16 14:33:02', '2026-01-16 16:39:21', '2026-01-16 16:39:21', 10, '', 'SENT', '2026-01-16 16:39:21', '您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', '系统自动检测异常');
INSERT INTO `health_alerts` VALUES (5, 6, 1, '血压', 130, 0, 'HIGH', 'ACKED', 'SYSTEM', NULL, '2025-12-27 00:00:00', '2026-01-16 14:50:28', '2026-01-16 14:50:28', 10, '', 'SENT', '2026-01-16 14:50:28', '您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', '系统自动检测异常');
INSERT INTO `health_alerts` VALUES (6, 3, 1, '血压', 135, 0, 'HIGH', 'ACKED', 'SYSTEM', NULL, '2025-12-25 00:00:00', '2026-01-16 14:50:31', '2026-01-16 14:50:31', 10, '', 'SENT', '2026-01-16 14:50:31', '您的血压值异常（135），请立即测量并记录，如有不适请及时就医。', '系统自动检测异常');
INSERT INTO `health_alerts` VALUES (7, 6, 1, '血压', 135, 0, 'HIGH', 'ACKED', 'SYSTEM', NULL, '2025-12-25 00:00:00', '2026-01-16 14:50:33', '2026-01-16 14:50:33', 10, '', 'SENT', '2026-01-16 14:50:33', '您的血压值异常（135），请立即测量并记录，如有不适请及时就医。', '系统自动检测异常');
INSERT INTO `health_alerts` VALUES (8, 4, 1, '血压', 200, 0, 'HIGH', 'ACKED', 'SYSTEM', NULL, '2025-12-27 00:00:00', '2026-01-16 18:39:47', '2026-01-16 18:39:47', 10, '', 'SENT', '2026-01-16 18:39:47', '您的血压值异常（200），请立即测量并记录，如有不适请及时就医。', '系统自动检测异常');
INSERT INTO `health_alerts` VALUES (9, 4, 1, '血压', 155, 0, 'HIGH', 'ACKED', 'SYSTEM', NULL, '2026-01-16 00:00:00', '2026-01-16 18:39:52', '2026-01-16 18:39:52', 10, '', 'SENT', '2026-01-16 18:39:52', '您的血压值异常（155），请立即测量并记录，如有不适请及时就医。', '系统自动检测异常');

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
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '健康咨询记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of health_consultations
-- ----------------------------
INSERT INTO `health_consultations` VALUES (1, 3, 'session-1763098938512', '高血压患者可以吃柚子吗？', '{\n\n\n    \n\n\n\n  \n  \n  \n  \n  \n  \n  \n    \n  \n  \n    \n  \n  \n    \n  \n  \n    \n  \n  \n    \n  \n  \n', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花生\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, 0, '2025-11-14 13:42:41');
INSERT INTO `health_consultations` VALUES (2, 3, 'session-1763098971266', '高血压患者可以吃柚子吗？', '{\n    \"问题\": \"高血压患者可以吃柚子吗？\",\n    \"健康标签\": \"肝病\",\n    \"过敏史\": \"花生\"\n}', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花生\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-11-14 13:43:06');
INSERT INTO `health_consultations` VALUES (3, 3, 'session-1763099419599', '高血压患者可以吃柚子吗？', '{\n  \"答案\": \"对于高血压患者来说，适量食用柚子通常是安全的。柚子含有丰富的维生素C、纤维素和多种抗氧化物质，对健康有益。但是，需要注意的是，柚子中的某些成分可能会影响药物代谢酶，从而增加某些降压药或其他心血管药物的血药浓度，导致副作用或效果过强的风险。\",\n  \"个性化建议\": \"鉴于您是肝病患者且有过敏史（花生），在尝试食用柚子之前，请先咨询您的医生或药师，以确保不会与您正在服用的任何药物产生不良反应。同时，由于您有食物过敏史，建议仔细观察是否对柚子产生过敏反应。\",\n  \"参考资料\": \"目前尚无直接针对肝病患者和高血压患者的特定研究指出食用柚子会带来健康风险，但考虑到个体差异及潜在药物相互作用的可能性，谨慎起见还是需要在医生指导下进行。\"\n}', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花生\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-11-14 13:50:57');
INSERT INTO `health_consultations` VALUES (4, 3, 'session-1763099744825', '高血压患者能吃柚子吗？', '{  \n  \"咨询内容\": \"关于高血压患者能否食用柚子的问题\",\n  \"建议\": [\n    {\n      \"描述\": \"对于高血压患者来说，适量食用柚子通常是安全的。柚子含有丰富的维生素C、钾等营养成分，对健康有益。\",\n      \"注意点\": \"然而，如果您正在服用降压药物（如某些类型的钙通道阻滞剂和ACE抑制剂），则需要谨慎食用柚子或其制品。因为柚子可能会影响这些药物在体内的代谢过程，从而导致血压过低或其他副作用。\"\n    }\n  ],\n  \"个性化建议\": [\n    {\n      \"描述\": \"鉴于您有肝病且对花生过敏的历史，在选择食物时应更加小心。如果您不确定是否可以食用柚子或其制品，请咨询医生或营养师的意见。\",\n      \"注意点\": \"同时，确保您的饮食不会与正在服用的任何药物产生不良反应。\"\n    }\n  ],\n  \"紧急情况提示\": \"\",\n  \"参考资料来源\": \"\"\n}', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花生\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-11-14 13:56:19');
INSERT INTO `health_consultations` VALUES (5, 3, 'session-1763099744825', '高血压患者能吃柚子吗？', '{\n  \"咨询内容\": \"关于高血压患者能否食用柚子的问题\",\n  \"建议\": [\n    {\n      \"描述\": \"对于高血压患者来说，适量食用柚子通常是安全的。柚子含有丰富的维生素C、钾等营养成分，对健康有益。\",\n      \"注意点\": \"然而，如果您正在服用降压药物（如某些类型的钙通道阻滞剂和ACE抑制剂），则需要谨慎食用柚子或其制品。因为柚子可能会影响这些药物在体内的代谢过程，从而导致血压过低或其他副作用。\"\n    }\n  ],\n  \"个性化建议\": [\n    {\n      \"描述\": \"鉴于您有肝病且对花生过敏的历史，在选择食物时应更加小心。如果您不确定是否可以食用柚子或其制品，请咨询医生或营养师的意见。\",\n      \"注意点\": \"同时，确保您的饮食不会与正在服用的任何药物产生不良反应。\"\n    }\n  ],\n  \"紧急情况提示\": \"\",\n  \"参考资料来源\": \"\"\n}', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花生\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-11-14 13:57:06');
INSERT INTO `health_consultations` VALUES (6, 3, 'session-1763099973542', '高血压患者能吃柚子吗？', '{                           \n    \"答案\": \"高血压患者可以适量食用柚子。柚子富含维生素C、钾等营养成分，对心血管健康有益。不过需要注意的是，柚子可能会影响某些药物的代谢，例如降压药。如果您正在服用降压药，请在医生或药师指导下确定是否适合食用柚子。\",\n    \"个性化建议\": \"鉴于您有肝病及花生过敏史，建议在饮食方面更加注意食物的选择和搭配。如果不确定某种食物对自己是否有影响，最好咨询专业医生的意见。同时，保持规律的生活习惯、合理膳食、适量运动对控制高血压非常重要。\",\n    \"参考资料\": \"\"\n}', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花生\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-11-14 13:59:55');
INSERT INTO `health_consultations` VALUES (7, 3, 'session-1763099973542', '高血压患者能吃柚子吗？', '{                           \n    \"答案\": \"高血压患者可以适量食用柚子。柚子富含维生素C、钾等营养成分，有助于心血管健康。不过需要注意的是，柚子可能会影响某些药物的代谢，例如降压药。\",\n    \"个性化建议\": \"鉴于您有肝病及花生过敏史，请特别注意饮食安全。如果不确定某种食物对自己是否有影响，最好咨询专业医生的意见。同时，保持规律的生活习惯、合理膳食、适量运动对控制高血压非常重要。尤其是您的健康标签涉及肝病，更需要注意避免可能加重肝脏负担的食物或成分。\",\n    \"参考资料\": \"\"\n}', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花生\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-11-14 14:01:01');
INSERT INTO `health_consultations` VALUES (8, 3, 'session-1763100179384', '高血压患者能吃柚子吗？', '对于高血压患者来说，可以适量食用柚子。柚子含有丰富的维生素C和多种微量元素，有助于改善血管弹性，对控制血压有一定的帮助。\n\n但是需要注意的是，柚子中的某些成分可能会影响药物的代谢，特别是与降压药或其他心血管疾病用药一起服用时可能会产生相互作用。如果您正在服用高血压药物，请在医生或药师指导下食用柚子。\n\n另外，考虑到您提到的肝病情况和花生过敏史，尽管目前问题主要针对柚子，但在日常饮食中还需要注意以下几点：\n\n1. 保持均衡饮食，避免过量摄入任何一种食物；\n2. 避免接触和摄入可能导致过敏反应的食物，如花生及其制品。\n\n总之，在调整您的饮食习惯或服用新的补充剂之前，请咨询医生或营养师，确保所做改变适合您当前的健康状况。', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花生\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-11-14 14:03:25');
INSERT INTO `health_consultations` VALUES (9, 3, 'session-1763100370474', '高血压患者能吃柚子吗？', '高血压患者可以适量食用柚子。柚子富含维生素C、膳食纤维等营养成分，对身体有益。但是需要注意的是，柚子中的某些成分可能会与一些药物发生相互作用，影响药效。\n\n您提到有肝病和花生过敏史的情况，请注意以下几点：\n\n1. **药物相互作用**：如果您正在服用降压药或其他药物（如他汀类药物、某些抗抑郁药等），柚子可能会影响这些药物的吸收或代谢，进而改变其效果。建议咨询医生或药师确认具体用药情况。\n   \n2. **肝病注意事项**：高血压和肝病患者在饮食上应遵循低盐、低脂的原则，柚子相对较为健康，但仍需适量食用。\n\n3. **过敏史注意**：由于您对花生过敏，在选择水果时无需特别担心柚子是否会引起过敏反应。但如果您对某些特定成分有过敏史，请先咨询医生或营养师，确保安全。\n\n4. **适量原则**：无论是什么食物，过量摄入都可能带来不必要的健康风险。建议每次食用约100-150克的柚子为宜，并将其纳入日常均衡饮食中。\n\n总之，高血压患者可以吃柚子，但需注意药物相互作用和个体差异。如有疑问或特殊情况，请咨询医生获得个性化指导。', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花生\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-11-14 14:06:49');
INSERT INTO `health_consultations` VALUES (10, 3, 'session-1763100370474', '高血压患者能吃柚子吗？', '高血压患者可以适量食用柚子。柚子含有丰富的维生素C、膳食纤维等营养成分，对身体有益。但是需要注意以下几点：\n\n1. **药物相互作用**：柚子中的某些成分可能会与降压药或其他特定药物（如他汀类药物、某些抗抑郁药）发生相互作用，影响药效。建议咨询医生或药师确认具体用药情况。\n\n2. **肝病注意事项**：高血压和肝病患者应遵循低盐、低脂的饮食原则，柚子相对较为健康，但仍需适量食用。\n\n3. **过敏史注意**：您对花生过敏，但通常情况下，柚子不会引发类似反应。不过，如果有任何食物过敏史，请先咨询医生或营养师确认安全。\n\n4. **适量原则**：建议每次食用约100-150克的柚子为宜，并将其纳入日常均衡饮食中。\n\n总之，高血压患者可以吃柚子，但需注意药物相互作用和个体差异。如有疑问或特殊情况，请及时咨询医生获得个性化指导。', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花生\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-11-14 14:09:25');
INSERT INTO `health_consultations` VALUES (11, 3, 'session-1763100903934', '高血压患者能吃柚子吗？', '高血压患者可以适量食用柚子。柚子含有丰富的维生素C和纤维素，对心血管健康有益。但是，需要注意的是，柚子可能会与某些降压药物发生相互作用，影响药效。\n\n建议您在服用特定降压药物时，最好咨询医生或药师是否可以在服药期间食用柚子。如果您的肝病情况较为严重或者正在接受其他治疗，请务必告知医生这一饮食习惯的变化，以确保安全。\n\n此外，如果您有过敏史（如对花生过敏），注意检查含有柚子的产品标签，避免潜在的交叉污染或其他可能的食物过敏源。保持均衡饮食和定期监测血压是管理高血压的重要措施之一。', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花生\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, 1, '2025-11-14 14:15:35');
INSERT INTO `health_consultations` VALUES (12, 3, 'session-1763100903934', '高血压患者可以随便生气吗？', '情绪波动，包括生气，对于高血压患者来说并不是好事。频繁或剧烈的情绪变化可能会导致血压升高，对心血管系统产生不利影响。因此，建议您尽量保持心情平和，遇到问题时尝试冷静处理。\n\n结合您的肝病情况和个人过敏史（花生过敏），在日常生活中要注意情绪的管理，并避免可能诱发不良情绪的因素。如果感到压力大或者情绪不稳定，请及时与家人或朋友沟通，必要时寻求专业心理支持。\n\n如有任何不适或血压异常变化，请及时就医咨询医生意见。希望这些建议对您有所帮助！', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花生\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-11-14 14:16:32');
INSERT INTO `health_consultations` VALUES (13, 3, 'session-1763101629386', '心情不好可以去嫖娼吗？', '我理解您可能正在经历情绪上的困扰，但我必须强调，您的提问涉及不健康和非法的行为。寻求性服务不仅违反法律，在心理或身体健康方面也可能带来风险和不良后果。\n\n对于肝病患者来说，保持良好的心理健康非常重要。建议您通过正规途径寻求帮助和支持，比如咨询专业的心理咨询师或者参加支持小组等。同时，保持规律的生活习惯、合理膳食以及适当运动都有利于身心健康。如果情绪问题严重影响日常生活，请尽快联系医生或心理卫生专业人士进行面对面咨询。\n\n请务必远离不健康和非法行为，选择对自身及他人负责任的道路。', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花生\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-11-14 14:27:39');
INSERT INTO `health_consultations` VALUES (14, 3, 'session-1763102657785', '非常疲劳可以打胶吗？', '根据您的描述，您有肝病史并且对花生过敏。关于是否可以注射“胶”来缓解疲劳的问题，在没有具体指明是哪种“胶”的情况下，我无法给出确切建议。通常，“胶”可能指的是某些药物制剂或者营养补充剂。\n\n鉴于您提到的健康状况和过敏史：\n\n1. **肝病**：肝脏疾病患者在使用任何新药或补充剂前应咨询医生，因为这些物质可能会加重肝脏负担。\n2. **花生过敏**：如果您对花生或其他坚果类食物有过敏史，请确保“胶”中不含有您已知的过敏源成分。\n\n**建议措施**：\n- 尽量不要自行决定注射任何药物。对于补充剂或特殊治疗方式，务必先咨询医生或专业医疗人员。\n- 如果感到非常疲劳，应考虑调整生活习惯和饮食结构，同时监测自身状况变化，并及时与医生沟通。\n- 若疲劳感持续存在且影响日常生活，建议尽快就医进行详细检查。\n\n请记住，在尝试任何新的治疗方法之前，请务必寻求专业的医疗意见。', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花生\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, 1, '2025-11-14 14:45:13');
INSERT INTO `health_consultations` VALUES (15, 3, 'session-1763102657785', '心情不好可以在街上殴打别人吗，想发泄一下', '情绪不佳时在街上殴打他人是不恰当的行为。这种做法不仅可能伤害他人，也可能带来法律上的风险和后果。正确的情绪管理非常重要，您可以尝试以下方法来缓解压力：\n\n1. **深呼吸**：进行几次深呼吸练习，有助于放松身心。\n2. **散步或运动**：适当的户外活动可以帮助释放紧张情绪。\n3. **倾诉**：与家人、朋友或心理咨询师分享您的感受，获得支持和建议。\n4. **写日记**：记录下您的心情和想法，帮助整理思绪。\n5. **听音乐或看喜剧片**：选择让您感到愉悦的内容放松自己。\n\n如果情绪持续低落，影响日常生活，请及时寻求专业心理辅导。', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花生\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-11-14 14:46:14');
INSERT INTO `health_consultations` VALUES (16, 6, 'session-1766650675241', '大便干怎么办', '大便干燥可能是由于水分摄入不足、饮食结构不合理或缺乏运动等原因引起的。这里有一些建议可以帮助改善这一情况：\n\n1. **增加膳食纤维摄入**：多吃蔬菜、水果和全谷类食品，它们富含纤维，有助于软化粪便。\n2. **多喝水**：每天保证足够的水分摄入量，成人一般建议每日饮水量为1500-1700毫升左右（根据个人情况调整）。\n3. **规律运动**：适当的体育活动可以促进肠道蠕动，帮助改善便秘问题。\n\n如果上述方法尝试后仍无明显改善，或者伴有腹痛、体重下降等其他症状，请及时就医进行进一步检查。这些建议并不能代替专业医生的诊断和治疗意见。\n\n请注意，这里提供的信息是基于一般情况给出的建议，并未考虑您的具体健康标签和个人过敏史，因此请根据自身情况进行调整或咨询医生获取个性化建议。', '{\"sex\": null, \"birthday\": null, \"heightCm\": null, \"weightKg\": null}', NULL, NULL, 1, '2025-12-25 16:18:21');
INSERT INTO `health_consultations` VALUES (17, 6, 'session-1766650739045', '晚上突然拉肚子，没憋住拉内裤里了，还让舍友把内裤清理了，怎么办', '遇到这种情况确实让人尴尬，但不必过于担心。首先，请尽量保持冷静和清洁。下面是一些建议：\n\n1. **补充水分**：腹泻会导致身体脱水，因此需要多喝水或口服补液盐溶液来补充流失的电解质。\n2. **注意饮食**：在症状缓解前，建议暂时避免油腻、辛辣、生冷的食物，可以选择清淡易消化的食物如稀饭、面条等。\n3. **个人卫生**：保持私处清洁干燥，可以使用温水清洗并轻轻擦干。\n\n如果情况没有改善或者出现以下症状时，请务必及时就医：\n- 持续性腹泻超过24小时\n- 发热或体温异常升高\n- 严重腹痛\n- 血便\n- 明显脱水迹象（如口渴、少尿、皮肤弹性下降等）\n\n请记住，这里提供的建议不能替代专业医生的诊断和治疗。如果情况紧急或者不确定如何处理，请尽快就医。\n\n关于内裤清理的问题，在日常生活中确实应该注意个人卫生，但不必过于担忧，保持清洁即可。如果有任何不适或疑虑，建议及时寻求医疗帮助。', '{\"sex\": null, \"birthday\": null, \"heightCm\": null, \"weightKg\": null}', NULL, NULL, -1, '2025-12-25 16:19:48');
INSERT INTO `health_consultations` VALUES (18, 6, 'session-1766650739045', '再给出些建议', '针对您目前的情况，以下是一些建议：\n\n1. **继续补充水分**：持续腹泻会导致脱水，因此请确保充分饮水，或者喝些口服补液盐溶液以维持电解质平衡。\n2. **观察症状变化**：留意是否有其他伴随症状出现，如发热、腹痛加剧等。\n3. **休息**：适当卧床休息有助于身体恢复。\n4. **注意个人卫生**：保持私处清洁干燥，避免使用刺激性强的洗液。\n\n如果腹泻在接下来的几天内没有改善或伴有严重症状，请及时就医。再次强调，这里提供的信息仅供参考，并不能代替专业医生的意见和治疗建议。\n\n希望您早日康复！如有任何疑问，请随时咨询医疗专业人士。', '{\"sex\": null, \"birthday\": null, \"heightCm\": null, \"weightKg\": null}', NULL, NULL, -1, '2025-12-25 16:20:54');
INSERT INTO `health_consultations` VALUES (19, 3, '2', '如何科学减肥？', '对于肝病患者和有过敏史的个体来说，采取健康的生活方式是减轻体重的有效方法。以下是一些建议：\n\n1. **均衡饮食**：选择富含纤维的食物，如蔬菜、水果和全谷物，减少高糖、高脂肪食物的摄入。避免食用可能引起过敏反应的食物，特别是花粉相关的易敏食品。\n\n2. **适量运动**：根据个人健康状况进行适度的身体活动，比如快走、游泳或骑自行车等有氧运动。开始新的锻炼计划前，请咨询医生意见。\n\n3. **定期监测体重和肝功能指标**：与您的医疗团队保持联系，定期检查以确保减肥过程不会影响到肝脏健康，并及时调整生活方式或治疗方案。\n\n4. **充足睡眠与减压**：保证足够的休息时间可以帮助身体更好地调节代谢。学习一些放松技巧，如深呼吸、瑜伽或冥想等，有助于减轻压力，进而控制食欲和体重。\n\n5. **咨询专业人士**：如果需要更具体的饮食计划或者减肥建议，请寻求营养师的帮助；对于肝病患者而言，还需根据医生指导调整方案。\n\n请记住，在尝试任何新的减肥策略之前，务必先与您的医疗提供者讨论，确保所采取的方法不会对您当前的健康状况产生不利影响。', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花粉\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-12-25 21:44:56');
INSERT INTO `health_consultations` VALUES (20, 3, 'session-1766823984565', '做爱男生多长时间算合适？', '性生活的时间长度因人而异，并没有固定的标准。一般认为，性生活的持续时间能够使双方感到满意和舒适即可。对于肝病患者来说，保持适度的性生活是安全的，但需要注意以下几点：\n\n1. **体力管理**：患有肝病的人可能更容易感到疲劳，因此需要根据自身情况调整活动量。\n2. **避免过度劳累**：长时间的性活动可能会增加身体负担，特别是对肝脏造成额外的压力。如果感觉不适，应立即停止并休息。\n3. **过敏史注意**：您提到有过敏史（花粉），虽然这通常不会直接与性生活相关联，但如果您有任何皮肤反应或不适感，请及时就医。\n\n对于肝病患者来说，重要的是保持健康的生活方式和合理的活动量。如果有任何疑问或担心，建议咨询医生以获得个性化的医疗建议。同时，在进行性生活时，确保使用安全措施避免感染，这对所有人群都很重要。\n\n请记住，如果在性生活中出现剧烈疼痛或其他紧急症状，请立即停止并寻求医疗帮助。', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花粉\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-12-27 16:27:11');
INSERT INTO `health_consultations` VALUES (21, 3, 'session-1766823984565', '两分钟合适吗？', '对于肝病患者来说，性生活的持续时间并不是固定的，重点是双方的舒适度和健康状况。如果您指的是每次性生活的时间为2分钟是否合适，这可能偏短，通常建议根据个人情况调整。\n\n综合考虑您的健康标签（肝病）和过敏史（花粉），以下是一些建议：\n\n1. **体力管理**：保持适当的休息，避免过度劳累。\n2. **适度活动**：与伴侣沟通并根据自身感觉调整性生活的强度和时间。\n3. **舒适为主**：确保双方感到满意和舒适。\n\n如果您有任何疑虑或不适，请及时咨询医生。如果有紧急情况，如剧烈疼痛或其他严重症状，请立即就医。\n\n总之，建议您在进行任何性生活前与医生讨论您的具体情况，以获得个性化的指导。', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花粉\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-12-27 16:28:11');
INSERT INTO `health_consultations` VALUES (22, 3, 'session-1766828814372', '如何科学减肥？', '科学减肥是一个涉及饮食、运动和生活习惯调整的综合过程。对于有肝病及花粉过敏史的人来说，更需要谨慎选择适合自己的减肥方法。下面是一些建议：\n\n1. **个性化饮食计划**：\n   - 尽量选择低糖、高纤维的食物，如全谷物、蔬菜和水果。\n   - 减少加工食品的摄入，这类食物往往含有较多的添加剂和防腐剂。\n   - 控制餐盘中的蛋白质来源，避免过多的红肉和动物脂肪，适量增加优质蛋白（如鱼、鸡胸肉）的摄入。\n   - 限制含糖饮料、果汁等高热量饮品。\n\n2. **合理运动**：\n   - 结合个人健康状况选择适合自己的运动类型。肝病患者应避免剧烈运动，可以选择散步、游泳等低强度活动。\n   - 每周至少150分钟的中等强度有氧运动（如快走）或75分钟的高强度有氧运动。\n   - 注意花粉季节减少户外锻炼时间。\n\n3. **生活习惯调整**：\n   - 保证充足睡眠，改善压力管理。\n   - 避免熬夜和过度劳累。\n\n4. **监测体重变化**：\n   - 定期记录体重，注意观察减肥进度是否健康。\n\n5. **咨询专业人士意见**：\n   - 在开始任何减肥计划前，请先咨询医生或营养师，尤其是肝病患者需要特别注意饮食调整。\n   - 如果在减肥过程中出现肝脏不适或其他异常情况，应及时就医。\n\n6. **避免快速减肥法**：\n   - 快速减肥可能会对肝功能产生不利影响，并可能导致反弹现象。建议采取逐步减重的方式，以确保健康安全为前提。\n\n请注意，在实施任何减肥计划时都需要考虑到个人的具体健康状况和医生的指导。希望这些建议对你有所帮助！', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花粉\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-12-27 17:47:44');
INSERT INTO `health_consultations` VALUES (23, 3, 'session-1766829265969', '如何科学减肥？', '科学减肥需要综合考虑饮食、运动和生活习惯。由于您有肝病和花粉过敏史，所以在制定减肥计划时需要注意以下几点：\n\n1. **个性化饮食**：建议咨询营养师定制适合您的饮食方案，减少高糖、高脂肪食物的摄入，增加蔬菜水果的比例。考虑到肝脏健康，避免过于油腻或难以消化的食物。\n\n2. **适量运动**：根据自身情况选择合适的运动方式和强度，如散步、瑜伽等低冲击力活动。在开始新的运动计划前，请咨询医生确保这些活动不会影响您的肝病状况。\n\n3. **控制体重速度**：建议采用温和的方式逐渐减轻体重，避免快速减肥给肝脏带来额外负担。推荐每周减重0.5-1公斤为宜。\n\n4. **充足睡眠与减少压力**：保证足够的休息时间有助于调节新陈代谢和情绪管理，对整体健康有益。\n\n5. **定期检查**：在减肥过程中应定期进行肝功能等相关指标的检测，确保身体健康状况良好。\n\n6. **避免接触过敏源**：尽量远离花粉等可能引起过敏反应的物质，以减少身体不适影响到减重效果。\n\n最后，请注意监测自己的体重和健康状态变化。如果出现任何不适症状（如腹痛、疲劳加剧），应立即停止减肥计划并寻求医生帮助。希望这些建议对您有所帮助！', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花粉\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-12-27 17:55:05');
INSERT INTO `health_consultations` VALUES (24, 3, 'session-1766829409996', '经常失眠怎么办？', '对于您的失眠症状，可以尝试以下几种非药物方法来改善睡眠质量：\n\n1. **建立规律的作息时间**：每天尽量在同一时间上床睡觉和起床。\n2. **优化睡眠环境**：确保卧室安静、黑暗和适宜的温度。使用舒适的床上用品。\n3. **避免刺激性饮料和食物**：减少咖啡因和酒精摄入，特别是在睡前几小时内。\n4. **放松身心**：尝试在睡前进行深呼吸练习、瑜伽或温和的拉伸运动来帮助放松。\n5. **限制午睡时间**：如果您白天需要小憩，请控制在20分钟以内，并避免傍晚时分。\n\n考虑到您有肝病和花粉过敏的历史，建议您在调整生活习惯的同时咨询医生的意见。特别是使用任何新的补充剂或者药物前（包括草药），务必告知您的主治医师，以防可能的不良反应或与其他药物的相互作用。\n\n如果上述措施实施一段时间后失眠情况没有改善，或者伴有其他严重症状，请及时就医。长期失眠可能会对健康产生不利影响，尤其是对于肝病患者来说更为重要。', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花粉\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-12-27 17:57:17');
INSERT INTO `health_consultations` VALUES (25, 3, 'session-1766829570290', '如何科学减肥？', '对于有肝病和花粉过敏史的个体来说，科学减肥需要特别注意以下几点：\n\n1. **饮食调整**：\n   - 选择低脂、高纤维的食物，比如蔬菜、水果、全谷物等。\n   - 减少糖分摄入，尤其是含糖饮料和加工食品。\n   - 控制蛋白质的来源，避免过多脂肪摄入。同时注意不要过度限制蛋白质，以支持肝脏功能。\n\n2. **适量运动**：\n   - 在开始任何新的锻炼计划前，请咨询医生或专业教练，确保所选活动适合您的健康状况。\n   - 低强度的有氧运动如散步、游泳等对减肥有益且相对安全。\n   - 避免剧烈运动或可能导致肝脏负担加重的高强度训练。\n\n3. **监测体重**：\n   - 定期记录体重和体脂比例，但避免频繁称重造成不必要的压力。\n   - 通过健康的生活方式逐渐减轻体重，而不是追求快速减肥方法。\n\n4. **保持充足睡眠与减压**：\n   - 睡眠不足可能影响荷尔蒙平衡，进而影响食欲。确保每晚获得7-9小时的高质量睡眠。\n   - 学会管理压力，过度的压力可能导致暴饮暴食或不良饮食习惯。\n\n5. **注意肝脏健康**：\n   - 避免饮酒，酒精是肝脏的主要负担之一。\n   - 减肥过程中避免使用可能对肝脏有害的药物和补充剂。\n\n6. **过敏管理**：\n   - 避免接触已知的花粉源。如果症状严重，请咨询医生是否需要在特定季节采取预防措施或使用抗过敏药物。\n\n请记住，每个人的情况都是独特的，因此最好与营养师、肝病专家以及过敏科医生合作制定个性化计划。科学减肥是一个渐进的过程，重要的是持之以恒，并且选择对您和您的健康状况最有益的方法。', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花粉\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-12-27 18:00:16');
INSERT INTO `health_consultations` VALUES (26, 3, 'session-1766829667662', '想拉屎怎么办', '如果您感到需要排便，请尽量去厕所。保持规律的饮食和生活习惯有助于改善排便情况。但是，如果出现以下情况，请及时就医：\n\n- 持续性腹痛或不适\n- 便秘超过一周且没有缓解\n- 大便带血\n\n对于肝病患者来说，保持良好的消化系统功能非常重要。如果您有任何健康上的疑问或症状加重，请咨询您的医生。\n\n此外，您对花粉过敏，建议在花粉季节注意防护措施，如佩戴口罩、减少户外活动等，避免因环境因素导致身体不适。如果排便问题与肝病或其他疾病相关联，也请告知您的医生，以便获得更全面的治疗建议。', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花粉\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-12-27 18:01:33');
INSERT INTO `health_consultations` VALUES (27, 3, 'session-1766830616912', '如何科学减肥？', '针对您的健康标签和过敏史，以及您的需求，以下是关于科学减肥的一些建议：\n\n1. **均衡饮食**：选择低糖、高纤维的食物，如蔬菜、水果和全谷物。尽量减少加工食品和含糖饮料的摄入。避免花粉可能引起的食物过敏反应。\n\n2. **定期运动**：结合有氧运动（如快走、慢跑或游泳）与力量训练，每周至少150分钟中等强度的有氧运动或者75分钟高强度的有氧运动，并加入肌肉强化活动。\n\n3. **保持水分**：每天喝足够的水有助于提高新陈代谢率和控制食欲。避免含糖饮料以减少额外热量摄入。\n\n4. **健康减重速度**：建议每月减重不超过1公斤，过快减肥可能会对肝脏造成负担。\n\n5. **规律作息**：保证充足的睡眠时间，良好的睡眠质量有助于调节激素水平，促进健康的体重管理。\n\n6. **监测进展**：记录饮食和运动情况可以帮助了解自己的习惯并作出相应调整。如果发现任何不适，请及时就医检查是否有肝病等因素影响减重计划。\n\n请注意，如果您有任何肝脏疾病相关的健康问题或症状（如疲劳、食欲不振等），应该先咨询医生再制定减肥方案。此外，在开始新的饮食或锻炼计划之前，尤其是当您有花粉过敏史时，请确保这些活动不会导致过敏反应的加重。\n\n如果需要更具体的建议或遇到任何疑问，最好直接向专业医疗人员寻求帮助。', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花粉\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-12-27 18:17:40');
INSERT INTO `health_consultations` VALUES (28, 3, 'session-1766830950584', '儿童发烧物理降温方法', '对于儿童发烧的处理，首先需要明确发烧是身体对感染的一种正常反应。以下是一些常用的物理降温方法：\n\n1. **温水擦浴**：使用32-34℃（约90-93°F）的温水擦拭孩子的四肢、背部和颈部，帮助散热。\n   \n2. **减少衣物**：适量减少穿盖的衣服或被子，但要保持孩子温暖，避免着凉。\n\n3. **适当饮水**：鼓励孩子多喝水或其他液体，如温开水、果汁等，以补充因发烧而增加的体液消耗。\n\n4. **通风环境**：确保室内空气流通良好，温度适宜。\n\n如果孩子的体温超过38.5℃（101.3°F），或伴有其他症状如持续哭闹不安、嗜睡不易唤醒、呼吸困难等，请及时就医。同时需要注意的是，以上方法适用于一般情况下的低热处理，对于儿童肝病患者来说，任何治疗措施都应在医生指导下进行。\n\n此外，由于您提到孩子有过敏史（花粉过敏），在采取上述物理降温措施时应注意避免接触可能引发过敏反应的环境因素或物质。\n\n请根据孩子的实际情况判断是否需要立即就医，并遵循专业医疗人员的指导。', '{\"sex\": \"M\", \"birthday\": \"2002-11-29\", \"heightCm\": 170.0, \"weightKg\": 50.0, \"allergies\": [\"花粉\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-12-27 18:23:03');
INSERT INTO `health_consultations` VALUES (29, 4, 'session-1766834615567', '如何科学减肥？', '科学减肥需要综合考虑饮食、运动和生活方式的调整。对于肝病患者及有药物过敏史的人来说，更需要注意方法的安全性和合理性。以下是一些建议：\n\n1. **均衡饮食**：\n   - 增加蔬菜和全谷物的比例。\n   - 适量摄入优质蛋白质（如鱼、瘦肉）。\n   - 控制脂肪摄入量，尤其是饱和脂肪和反式脂肪的摄入。\n\n2. **合理运动**：\n   - 开始任何新的运动计划之前，请咨询医生的意见。一般建议从低强度开始，逐渐增加活动量。\n   - 适度运动，比如快走、游泳或骑自行车等有氧运动，每周至少150分钟中等强度或75分钟高强度的活动。\n\n3. **生活习惯调整**：\n   - 确保充足的睡眠时间。\n   - 减少压力。可以尝试冥想、瑜伽等方式来放松身心。\n   - 避免饮酒，酒精对肝脏有损害作用。\n\n4. **个性化饮食计划**：\n   - 根据个人情况定制合理的饮食计划。如有必要，可咨询营养师获得专业指导。\n   - 考虑到您有过敏史，在选择食物时要特别注意避免已知的过敏源。\n\n5. **监测体重变化**：\n   - 定期记录体重和体脂率的变化。\n   - 如发现体重迅速下降或出现其他不适，应及时就医检查。\n\n6. **药物管理**：\n   - 遵医嘱服用所有必要的药物。如果您在减肥过程中遇到任何副作用，请及时告知医生。\n   - 在尝试新的补充剂或营养品前，务必咨询医生以确认其安全性。\n\n请记住，在开始任何减肥计划之前，尤其是您有肝病的情况下，一定要先征求专业医疗人员的意见。这样可以确保您的健康和安全得到充分保障。', '{\"sex\": \"M\", \"birthday\": \"2003-11-28\", \"heightCm\": 178.0, \"weightKg\": 60.0, \"allergies\": [\"药物过敏\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-12-27 19:24:26');
INSERT INTO `health_consultations` VALUES (30, 4, 'session-1766834903924', '如何科学减肥？', '对于肝病患者来说，科学减肥不仅有助于改善肝脏负担，还能提高整体健康水平。以下是一些建议：\n\n1. **均衡饮食**：\n   - 增加蔬菜和水果的摄入量。\n   - 选择全谷物、瘦肉和低脂乳制品作为主食。\n   - 减少加工食品和高糖食物的摄入。\n\n2. **适量运动**：\n   - 每周至少150分钟中等强度有氧运动（如快步走）或75分钟高强度有氧运动（如跑步）。\n   - 结合力量训练，每周至少两天进行肌肉强化活动。\n\n3. **避免酒精和某些药物**：\n   - 肝病患者应完全戒酒，因为酒精会加重肝脏负担。\n   - 减肥期间可能需要使用减肥药或补充剂，请咨询医生，确保所选产品不会对您的健康造成额外风险。由于您有药物过敏史，请特别注意这一点。\n\n4. **定期监测**：\n   - 定期检查肝功能指标。\n   - 监测体重和体脂变化。\n\n5. **心理支持**：\n   - 减肥过程可能会带来压力，保持积极乐观的心态对减肥成功同样重要。\n   - 考虑加入支持小组或寻求心理咨询帮助。\n\n请注意，以上建议适用于大多数情况下的健康减肥策略。但由于您有肝病以及药物过敏史，强烈建议在开始任何减肥计划前咨询专业医生的意见。此外，如果出现不适症状，请立即就医。', '{\"sex\": \"M\", \"birthday\": \"2003-11-28\", \"heightCm\": 178.0, \"weightKg\": 60.0, \"allergies\": [\"药物过敏\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-12-27 19:29:04');
INSERT INTO `health_consultations` VALUES (31, 4, 'session-1766835410280', '经常失眠怎么办？', '失眠可能与多种因素有关，包括生活习惯、精神状态等。对于肝病患者以及有药物过敏史的人来说，选择合适的缓解方法尤为重要。\n\n### 建议措施：\n\n1. **调整作息时间**：尽量每天同一时间上床睡觉和起床。\n2. **改善睡眠环境**：保持卧室安静、舒适、温度适宜；避免使用电子设备减少蓝光暴露。\n3. **放松身心**：睡前进行深呼吸练习或瑜伽等轻柔活动，帮助放松。\n4. **合理饮食**：晚餐不要过饱，避免咖啡因和酒精摄入。\n5. **适量运动**：规律的身体活动可以帮助改善睡眠质量。但注意避免在临近睡觉前做剧烈运动。\n\n### 注意事项：\n- 如果上述方法尝试一段时间后仍无明显改善，建议及时就医咨询专业医生意见。特别是您提到有肝病史及药物过敏情况，请务必告知医生您的具体健康状况和过敏情况，以防止使用可能对肝脏有害或引发过敏反应的药物。\n- 医生可能会根据您的具体情况开具适合的非镇静类安眠药或其他疗法。\n\n请记住，在采取任何新的治疗措施前，尤其是您有特殊疾病史的情况下，一定要咨询专业医生的意见。希望这些建议能帮助到您！', '{\"sex\": \"M\", \"birthday\": \"2003-11-28\", \"heightCm\": 178.0, \"weightKg\": 60.0, \"allergies\": [\"药物过敏\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-12-27 19:37:27');
INSERT INTO `health_consultations` VALUES (32, 1, 'session-test', '测试写入', '写入成功', NULL, NULL, NULL, NULL, '2025-12-27 19:44:21');
INSERT INTO `health_consultations` VALUES (33, 4, 'session-1766835872521', '经常失眠怎么办？', '失眠可能是由多种因素引起的，包括生活习惯、精神压力等。考虑到您提到的肝病背景以及药物过敏史，建议采取以下措施来改善睡眠质量：\n\n1. **保持规律作息**：每天尽量在同一时间上床睡觉和起床。\n2. **改善睡眠环境**：确保卧室安静、黑暗且温度适宜。使用舒适的床上用品。\n3. **健康饮食**：避免晚餐过晚或过量食用，尤其是辛辣、油腻食物；减少咖啡因和酒精摄入，特别是在睡前几小时内。\n4. **适量运动**：定期进行适度强度的体育活动（如散步），但避免在睡前2-3小时内做剧烈运动。\n\n鉴于您有肝病历史及药物过敏情况，请特别注意任何新药可能对肝脏的影响，并务必咨询医生或药师关于使用助眠产品或补充剂前的意见。有些用于治疗失眠的药物可能会加重肝脏负担或者与您正在使用的其他药物产生不良反应。\n\n如果尝试了上述方法后仍无法改善睡眠状况，建议及时就医寻求专业帮助。医生可以根据您的具体情况提供个性化的诊断和治疗方案。\n  \n请注意：本回答不涉及具体药品推荐，若需使用助眠药物，请务必咨询医疗专业人士获取指导。', '{\"sex\": \"M\", \"birthday\": \"2003-11-28\", \"heightCm\": 178.0, \"weightKg\": 60.0, \"allergies\": [\"药物过敏\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-12-27 19:45:08');
INSERT INTO `health_consultations` VALUES (34, 4, 'session-1766836425752', '高血压患者的饮食禁忌', '对于高血压患者来说，合理的饮食习惯非常重要。以下是一些基本的饮食建议：\n\n1. **限制盐分摄入**：过多的钠会导致血压升高，应尽量减少食盐使用。\n2. **控制热量**：避免过量的高脂肪、高糖和高胆固醇食物，这些都会增加心脏负担。\n3. **多吃蔬菜水果**：新鲜蔬果富含钾元素，有助于降低血压。\n4. **适量蛋白质**：选择低脂肪的肉类或鱼类作为蛋白质来源。\n5. **限制酒精摄入**：过多饮酒会提高血压。\n\n鉴于您提到有肝病的情况，建议在调整饮食前咨询医生或营养师的意见。特别是当涉及到需要特殊饮食管理时（如低盐饮食对高血压患者），需要考虑肝脏健康的影响。\n\n考虑到您的药物过敏史，请注意避免可能引起过敏反应的成分。例如，某些降压药可能会含有您已知过敏的成分。如果在使用新的饮食补充剂或药品时出现不适，应立即停止使用并咨询医生。\n\n请记住这些建议并不能替代专业医疗意见，在调整饮食或其他生活方式改变之前，请务必与您的医生讨论这些计划。', '{\"sex\": \"M\", \"birthday\": \"2003-11-28\", \"heightCm\": 178.0, \"weightKg\": 60.0, \"allergies\": [\"药物过敏\"], \"healthTags\": [\"肝病\"]}', NULL, NULL, -1, '2025-12-27 19:54:20');
INSERT INTO `health_consultations` VALUES (35, 6, 'session-1766842354573', '如何科学减肥？', '对于糖尿病患者来说，科学减肥需要综合考虑饮食、运动和生活方式的调整。以下是一些建议：\n\n1. **均衡饮食**：\n   - 增加膳食纤维摄入量，多吃蔬菜和全谷物。\n   - 控制碳水化合物的总量，尤其是精制糖和高GI（血糖生成指数）食物。\n   - 适量摄入优质蛋白质，如鱼、瘦肉和豆制品。\n   - 使用橄榄油等健康油脂烹饪。\n\n2. **规律运动**：\n   - 每周至少150分钟的中等强度有氧运动，例如快走或游泳。\n   - 结合力量训练以增加肌肉量，提高新陈代谢率。\n   - 避免长时间久坐，每小时起身活动5-10分钟。\n\n3. **生活方式调整**：\n   - 确保充足睡眠，避免熬夜。\n   - 减少压力，可以通过冥想、瑜伽等方式缓解。\n   - 定期监测血糖水平，并与医生保持沟通调整治疗计划。\n\n4. **药物管理**：\n   - 如果正在使用胰岛素或口服降糖药，请遵循医嘱调整用量。\n   - 避免自行调整剂量或突然停止用药，这可能会导致低血糖或其他副作用。\n\n5. **定期复查**：\n   - 定期到医院进行检查，监测体重变化、血糖水平等指标。\n   - 根据医生建议调整治疗方案。\n\n6. **避免药物过敏风险**：\n   - 减肥过程中可能需要使用一些辅助产品或补充剂，请务必先咨询医生是否适合，并确认它们不会引起过敏反应。\n\n请记住，在实施任何减肥计划之前，最好与您的主治医师讨论这些措施。他们可以根据您的具体情况提供个性化指导。如果有任何不适或疑问，请及时就医。', '{\"sex\": \"M\", \"birthday\": \"2004-03-27\", \"heightCm\": 176.0, \"weightKg\": 80.0, \"allergies\": [\"药物过敏\"], \"healthTags\": [\"糖尿病\"]}', NULL, NULL, -1, '2025-12-27 21:33:20');

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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of health_inference_report
-- ----------------------------
INSERT INTO `health_inference_report` VALUES (1, '### 🔍 核心辩证\n肝火上炎，耗伤阴液导致血压升高及体温偏高。\n\n### 🔗 数据关联证据\n- **体质因素**：当前体质为QI_DEFICIENCY（气虚），表明身体整体较为虚弱。在肝火旺盛的情况下，更易消耗体内的津液和正气。\n- **诱发行为**：健康日志显示用户在18:09测得心率高达105次/分钟，并伴有体温38.5°C的升高，且体重变化为7kg，这可能是由于近期可能饮食过量或身体劳累所致。结合血压异常（155mmHg），提示存在明显的肝火上炎现象。\n- **身体反应**：根据上述数据，用户体内可能存在阴液不足的情况，导致心率加快及体温升高。血压的异常也与肝火旺盛有关，肝火扰动血脉，耗伤阴液，血行加速而致。\n\n### 💡 中医综合干预\n1. **食疗**：\n   - 菊花枸杞汤：菊花、枸杞子各30g，水煎代茶饮。具有清热平肝、滋补肝肾之效。\n   - 玉竹麦冬粥：玉竹、麦冬各20g，粳米100g，同煮为粥，适用于养阴生津。\n\n2. **穴位**：\n   - 太溪穴（KI3）：位于脚内侧，足踝后方凹陷处。按摩或针灸此穴可滋补肾阴。\n   - 行间穴（LR2）：位于大趾与二趾之间，向上约1寸处。可以清肝泻火。\n\n3. **起居**：\n   - 保持规律作息，避免熬夜和过度劳累，适当安排休息时间，保证充足睡眠。\n   - 饮食上应清淡为主，减少辛辣、煎炸等刺激性食物的摄入，多吃蔬菜水果以补充水分。', 'QI_DEFICIENCY', '2026-01-17 22:58:48.473842', '2026-01-17 22:58:48.473842', '{\"logs\": [\"2026-01-16 [VITALS]: {\\\"time\\\": \\\"18:09\\\", \\\"type\\\": \\\"血压\\\", \\\"unit\\\": \\\"mmHg\\\", \\\"value\\\": null, \\\"systolic\\\": 155, \\\"diastolic\\\": 95}\", \"2026-01-16 [VITALS]: {\\\"time\\\": \\\"18:09\\\", \\\"type\\\": \\\"心率\\\", \\\"unit\\\": \\\"bpm\\\", \\\"value\\\": 105}\", \"2026-01-16 [VITALS]: {\\\"time\\\": \\\"18:09\\\", \\\"type\\\": \\\"血糖\\\", \\\"unit\\\": \\\"mmol/L\\\", \\\"value\\\": 8.8}\", \"2026-01-16 [VITALS]: {\\\"time\\\": \\\"18:09\\\", \\\"type\\\": \\\"体温\\\", \\\"unit\\\": \\\"°C\\\", \\\"value\\\": 38.5}\", \"2026-01-16 [VITALS]: {\\\"time\\\": \\\"18:09\\\", \\\"type\\\": \\\"体重变化\\\", \\\"unit\\\": \\\"kg\\\", \\\"value\\\": 7}\"], \"alerts\": [\"2026-01-16: 血压 (Value: 155.0) - 系统自动检测异常\"], \"constitution\": \"QI_DEFICIENCY\"}', b'0', '2026-01-17', 4);
INSERT INTO `health_inference_report` VALUES (2, '### 🔍 核心辩证\n肝火上炎，导致血压升高及全身症状。\n\n### 🔗 数据关联证据\n- **体质因素**：QI_DEFICIENCY（气虚体质）表现为免疫力下降、易感邪气，进一步影响脏腑功能。当前体征显示血压和体温偏高，提示体内有实热。\n- **诱发行为**：从健康日志片段可以看出，当天18:09体温达到了38.5°C，并且血糖也升高至8.8 mmol/L，说明身体处于一种应激状态或炎症反应。此外，体重增加了7公斤，可能与摄入过多热量有关。这些症状提示用户可能存在熬夜、饮食过量等情况。\n- **身体反应**：血压值为155 mmHg（正常范围90-120），心率高达105次/分（正常范围60-100），体温升高至38.5°C，血糖偏高至8.8 mmol/L。这些体征异常提示肝经郁火，上扰心神，导致血压升高、心率加快及发热等症状。\n\n### 💡 中医综合干预\n1. **食疗**：推荐使用菊花茶和百合粥。\n   - 菊花具有清热解毒、平肝明目的功效；适用于肝火上炎引起的头晕目眩、烦躁不安等症状。\n   - 百合润肺止咳，滋养阴液，对于肺燥咳嗽及心烦失眠有很好的调理作用。\n2. **穴位**：推荐按摩太冲穴和曲池穴。\n   - 太冲穴位于足背侧，第一跖骨间隙的后方凹陷处。按揉此穴可以疏肝解郁、平抑肝阳。\n   - 曲池穴在肘横纹外端，屈肘时当尺泽与肱骨外上髁连线中点。按摩曲池穴有助于清热凉血，缓解高热症状。\n3. **起居**：建议调整作息时间，保证充足睡眠；减少熬夜次数；清淡饮食为主，避免辛辣刺激食物；适当增加体育锻炼，增强体质。', 'QI_DEFICIENCY', '2026-01-18 14:47:05.038318', '2026-01-18 14:47:05.038318', '{\"logs\": [\"2026-01-16 [VITALS]: {\\\"time\\\": \\\"18:09\\\", \\\"type\\\": \\\"血压\\\", \\\"unit\\\": \\\"mmHg\\\", \\\"value\\\": null, \\\"systolic\\\": 155, \\\"diastolic\\\": 95}\", \"2026-01-16 [VITALS]: {\\\"time\\\": \\\"18:09\\\", \\\"type\\\": \\\"心率\\\", \\\"unit\\\": \\\"bpm\\\", \\\"value\\\": 105}\", \"2026-01-16 [VITALS]: {\\\"time\\\": \\\"18:09\\\", \\\"type\\\": \\\"血糖\\\", \\\"unit\\\": \\\"mmol/L\\\", \\\"value\\\": 8.8}\", \"2026-01-16 [VITALS]: {\\\"time\\\": \\\"18:09\\\", \\\"type\\\": \\\"体温\\\", \\\"unit\\\": \\\"°C\\\", \\\"value\\\": 38.5}\", \"2026-01-16 [VITALS]: {\\\"time\\\": \\\"18:09\\\", \\\"type\\\": \\\"体重变化\\\", \\\"unit\\\": \\\"kg\\\", \\\"value\\\": 7}\"], \"alerts\": [\"2026-01-16: 血压 (Value: 155.0) - 系统自动检测异常\"], \"constitution\": \"QI_DEFICIENCY\"}', b'0', '2026-01-18', 4);

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
) ENGINE = InnoDB AUTO_INCREMENT = 163 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '健康日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of health_logs
-- ----------------------------
INSERT INTO `health_logs` VALUES (65, 3, '2025-11-14', 'VITALS', '{\"note\": \"晨起测量\", \"time\": \"08:30\", \"unit\": \"mmHg\", \"systolic\": 120, \"diastolic\": 80, \"vitalType\": \"血压\"}', 85.00, 'MANUAL', NULL, 0, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (66, 3, '2025-11-13', 'VITALS', '{\"note\": \"空腹血糖\", \"time\": \"07:00\", \"unit\": \"mmol/L\", \"value\": 5.5, \"vitalType\": \"血糖\"}', 90.00, 'MANUAL', NULL, 0, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (67, 3, '2025-11-13', 'VITALS', '{\"note\": \"下午测量，感觉有点头晕\", \"time\": \"14:20\", \"unit\": \"mmHg\", \"systolic\": 150, \"diastolic\": 95}', 60.00, 'MANUAL', NULL, 1, '{\"anomaly\": {\"reason\": \"数值 150.0 超出正常范围 [90.0, 140.0]\", \"severity\": \"MEDIUM\", \"isAnomaly\": true, \"suggestion\": \"当前值 150.0 高于正常范围，建议关注并咨询医生\"}}', '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (68, 3, '2025-11-14', 'DIET', '{\"meal\": \"lunch\", \"note\": \"午餐：米饭、青菜、红烧肉、紫菜蛋花汤\", \"time\": \"12:30\"}', 85.00, 'MANUAL', NULL, NULL, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (69, 3, '2025-11-14', 'SLEEP', '{\"note\": \"睡眠质量不错\", \"bedtime\": \"23:00\", \"quality\": \"良好\", \"duration\": 8, \"wakeTime\": \"07:00\"}', 90.00, 'MANUAL', NULL, NULL, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (70, 3, '2025-11-13', 'SPORT', '{\"note\": \"慢跑5公里\", \"time\": \"18:00\", \"type\": \"跑步\", \"distance\": 5, \"duration\": 30}', 85.00, 'MANUAL', NULL, NULL, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (71, 3, '2025-11-14', 'MOOD', '{\"mood\": \"平静\", \"note\": \"心情平静，工作状态良好\", \"time\": \"09:00\", \"score\": 7}', 70.00, 'MANUAL', NULL, NULL, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (72, 3, '2025-11-11', 'VITALS', '{\"time\": \"10:15\", \"unit\": \"bpm\", \"value\": 72, \"source\": \"apple_watch\", \"vitalType\": \"心率\"}', 85.00, 'DEVICE', 'APPLE_WATCH_001', 0, '{\"device\": {\"type\": \"APPLE_WATCH\", \"model\": \"Series 9\", \"syncTime\": \"2025-11-14T10:15:00\"}}', '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (73, 3, '2025-11-14', 'SPORT', '{\"time\": \"全天\", \"type\": \"步行\", \"steps\": 8500, \"source\": \"huawei_band\", \"calories\": 320, \"distance\": 6.2}', 80.00, 'DEVICE', 'HUAWEI_BAND_002', NULL, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (74, 3, '2025-11-10', 'VITALS', '{\"time\": \"20:00\", \"unit\": \"mmHg\", \"source\": \"blood_pressure_monitor\", \"systolic\": 118, \"diastolic\": 78, \"vitalType\": \"血压\"}', 88.00, 'DEVICE', 'BP_MONITOR_003', 0, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (75, 3, '2025-11-09', 'VITALS', '{\"time\": \"14:30\", \"unit\": \"mmol/L\", \"value\": 6.8, \"source\": \"glucose_meter\", \"mealType\": \"餐后2小时\", \"vitalType\": \"血糖\"}', 85.00, 'DEVICE', 'GLUCOSE_METER_004', 0, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (76, 3, '2025-11-08', 'VITALS', '{\"bmi\": 22.3, \"time\": \"07:30\", \"unit\": \"kg\", \"value\": 68.5, \"source\": \"body_scale\", \"bodyFat\": 18.5, \"vitalType\": \"体重\"}', 80.00, 'DEVICE', 'BODY_SCALE_005', NULL, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (77, 3, '2025-11-07', 'VITALS', '{\"time\": \"09:00\", \"unit\": \"mmHg\", \"systolic\": 125, \"diastolic\": 82, \"vitalType\": \"血压\", \"sourceText\": \"记录今天血压125/82\", \"_dataSource\": \"VOICE\"}', 85.00, 'VOICE', NULL, 0, '{\"voice\": {\"confidence\": 0.95, \"originalText\": \"记录今天血压125/82\"}}', '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (78, 3, '2025-11-06', 'VITALS', '{\"time\": \"08:00\", \"unit\": \"°C\", \"value\": 36.8, \"vitalType\": \"体温\", \"sourceText\": \"今天体温36度8\", \"_dataSource\": \"VOICE\"}', 90.00, 'VOICE', NULL, 0, '{\"voice\": {\"confidence\": 0.92, \"originalText\": \"今天体温36度8\"}}', '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (79, 3, '2025-11-05', 'VITALS', '{\"name\": \"白细胞计数\", \"unit\": \"×10^9/L\", \"value\": 6.2, \"hospital\": \"市人民医院\", \"checkDate\": \"2025-11-05\", \"vitalType\": \"血常规\", \"isAbnormal\": false, \"normalRange\": \"3.5-9.5\"}', 90.00, 'OCR', NULL, 0, '{\"ocr\": {\"source\": \"blood_test_report\", \"hospital\": \"市人民医院\", \"checkDate\": \"2025-11-05\"}}', '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (80, 3, '2025-11-04', 'VITALS', '{\"name\": \"空腹血糖\", \"unit\": \"mmol/L\", \"value\": 7.2, \"hospital\": \"市人民医院\", \"checkDate\": \"2025-11-04\", \"vitalType\": \"血糖\", \"isAbnormal\": true, \"normalRange\": \"3.9-6.1\"}', 50.00, 'OCR', NULL, 1, '{\"ocr\": {\"source\": \"blood_test_report\", \"hospital\": \"市人民医院\", \"checkDate\": \"2025-11-04\"}, \"anomaly\": {\"reason\": \"数值 7.2 超出正常范围 [3.9, 6.1]\", \"severity\": \"HIGH\", \"isAnomaly\": true, \"suggestion\": \"当前值 7.2 高于正常范围，建议关注并咨询医生\"}}', '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (81, 3, '2025-11-08', 'VITALS', '{\"time\": \"08:00\", \"unit\": \"mmHg\", \"systolic\": 118, \"diastolic\": 76}', 88.00, 'MANUAL', NULL, 0, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (82, 3, '2025-11-09', 'VITALS', '{\"time\": \"08:00\", \"unit\": \"mmHg\", \"systolic\": 122, \"diastolic\": 80}', 85.00, 'MANUAL', NULL, 0, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (83, 3, '2025-11-10', 'VITALS', '{\"time\": \"08:00\", \"unit\": \"mmHg\", \"systolic\": 120, \"diastolic\": 78}', 87.00, 'MANUAL', NULL, 0, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (84, 3, '2025-11-11', 'VITALS', '{\"time\": \"08:00\", \"unit\": \"mmHg\", \"systolic\": 125, \"diastolic\": 82}', 83.00, 'MANUAL', NULL, 0, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (85, 3, '2025-11-08', 'SLEEP', '{\"bedtime\": \"23:00\", \"quality\": \"良好\", \"duration\": 8, \"wakeTime\": \"07:00\"}', 90.00, 'MANUAL', NULL, NULL, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (86, 3, '2025-11-09', 'SLEEP', '{\"bedtime\": \"23:30\", \"quality\": \"良好\", \"duration\": 8, \"wakeTime\": \"07:30\"}', 88.00, 'MANUAL', NULL, NULL, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (87, 3, '2025-11-10', 'SLEEP', '{\"bedtime\": \"00:00\", \"quality\": \"一般\", \"duration\": 7, \"wakeTime\": \"07:00\"}', 75.00, 'MANUAL', NULL, NULL, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (88, 3, '2025-11-11', 'SLEEP', '{\"bedtime\": \"22:30\", \"quality\": \"良好\", \"duration\": 8, \"wakeTime\": \"06:30\"}', 92.00, 'MANUAL', NULL, NULL, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (89, 3, '2025-11-08', 'SPORT', '{\"time\": \"18:00\", \"type\": \"跑步\", \"distance\": 5, \"duration\": 30}', 85.00, 'MANUAL', NULL, NULL, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (90, 3, '2025-11-09', 'SPORT', '{\"time\": \"19:00\", \"type\": \"快走\", \"distance\": 4, \"duration\": 45}', 80.00, 'MANUAL', NULL, NULL, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (91, 3, '2025-11-10', 'SPORT', '{\"time\": \"17:00\", \"type\": \"游泳\", \"distance\": 1.5, \"duration\": 60}', 90.00, 'MANUAL', NULL, NULL, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (92, 3, '2025-11-11', 'SPORT', '{\"time\": \"18:30\", \"type\": \"骑行\", \"distance\": 12, \"duration\": 40}', 88.00, 'MANUAL', NULL, NULL, NULL, '2025-11-14 13:15:41', NULL);
INSERT INTO `health_logs` VALUES (93, 3, '2025-11-14', 'DIET', '{\"note\": \"晚餐：南北绿豆浆，剩菜剩饭\", \"time\": \"16:13\"}', NULL, 'MANUAL', NULL, NULL, NULL, '2025-11-14 16:14:12', NULL);
INSERT INTO `health_logs` VALUES (94, 4, '2025-11-14', 'VITALS', '{\"value\": 80.0, \"metric\": \"HR\"}', NULL, NULL, NULL, 0, NULL, '2025-11-14 17:08:53', NULL);
INSERT INTO `health_logs` VALUES (95, 4, '2025-11-14', 'DIET', '{\"items\": [{\"name\": \"米饭\", \"calories\": 260}, {\"name\": \"青菜\", \"calories\": 60}, {\"name\": \"清蒸鱼\", \"calories\": 200}], \"totalCalories\": 520.0}', NULL, NULL, NULL, 0, NULL, '2025-11-14 18:04:01', NULL);
INSERT INTO `health_logs` VALUES (97, 6, '2025-12-25', 'DIET', '{\"note\": \"鸡蛋1个、辣条10片、面包1片、炒河粉200克；总热量：0 千卡\", \"time\": \"17:09\", \"items\": [{\"name\": \"鸡蛋\", \"unit\": \"个\", \"calories\": null, \"quantity\": 1}, {\"name\": \"辣条\", \"unit\": \"片\", \"calories\": null, \"quantity\": 10}, {\"name\": \"面包\", \"unit\": \"片\", \"calories\": null, \"quantity\": 1}, {\"name\": \"炒河粉\", \"unit\": \"克\", \"calories\": null, \"quantity\": 200}], \"totalCalories\": 0}', NULL, 'MANUAL', NULL, NULL, NULL, '2025-12-25 17:10:22', NULL);
INSERT INTO `health_logs` VALUES (98, 6, '2025-12-25', 'SLEEP', '{\"note\": \"21:00-08:00 11小时 很差\", \"time\": \"18:15\"}', NULL, 'MANUAL', NULL, NULL, NULL, '2025-12-25 18:16:43', NULL);
INSERT INTO `health_logs` VALUES (99, 6, '2025-12-25', 'SPORT', '{\"note\": \"跑步 60分钟 距离 5 公里 弱\", \"time\": \"18:16\"}', NULL, 'MANUAL', NULL, NULL, NULL, '2025-12-25 18:17:18', NULL);
INSERT INTO `health_logs` VALUES (100, 6, '2025-12-25', 'MOOD', '{\"note\": \"沮丧 强度 5 情绪很差\", \"time\": \"18:17\"}', NULL, 'MANUAL', NULL, NULL, NULL, '2025-12-25 18:17:37', NULL);
INSERT INTO `health_logs` VALUES (105, 6, '2025-12-25', 'VITALS', '{\"time\": \"19:20\", \"type\": \"血压\", \"unit\": \"mmHg\", \"value\": null, \"systolic\": 135, \"diastolic\": 80}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2025-12-25 19:20:58', NULL);
INSERT INTO `health_logs` VALUES (106, 6, '2025-12-25', 'VITALS', '{\"time\": \"19:20\", \"type\": \"血糖\", \"unit\": \"mmol/L\", \"value\": 5.2}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2025-12-25 19:20:58', NULL);
INSERT INTO `health_logs` VALUES (107, 6, '2025-12-25', 'VITALS', '{\"time\": \"19:20\", \"type\": \"体温\", \"unit\": \"°C\", \"value\": 36}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2025-12-25 19:20:58', NULL);
INSERT INTO `health_logs` VALUES (108, 6, '2025-12-25', 'VITALS', '{\"time\": \"19:20\", \"type\": \"心率\", \"unit\": \"bpm\", \"value\": 65}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2025-12-25 19:20:58', NULL);
INSERT INTO `health_logs` VALUES (109, 6, '2025-12-25', 'VITALS', '{\"time\": \"19:20\", \"type\": \"体重\", \"unit\": \"kg\", \"value\": 80}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2025-12-25 19:20:58', NULL);
INSERT INTO `health_logs` VALUES (110, 3, '2025-12-25', 'DIET', '{\"note\": \"炸鸡1块、汉堡1个、鳗鱼饭1碗、章鱼小丸子1个；总热量：1450 千卡\", \"time\": \"19:54\", \"items\": [{\"name\": \"炸鸡\", \"unit\": \"块\", \"calories\": 250, \"quantity\": 1}, {\"name\": \"汉堡\", \"unit\": \"个\", \"calories\": 380, \"quantity\": 1}, {\"name\": \"鳗鱼饭\", \"unit\": \"碗\", \"calories\": 520, \"quantity\": 1}, {\"name\": \"章鱼小丸子\", \"unit\": \"个\", \"calories\": 300, \"quantity\": 1}], \"totalCalories\": 1450}', NULL, 'MANUAL', NULL, NULL, NULL, '2025-12-25 19:54:48', NULL);
INSERT INTO `health_logs` VALUES (111, 3, '2025-12-25', 'SLEEP', '{\"note\": \"24:00-05:00 5小时 差\", \"time\": \"19:54\"}', NULL, 'MANUAL', NULL, NULL, NULL, '2025-12-25 19:56:13', NULL);
INSERT INTO `health_logs` VALUES (112, 3, '2025-12-25', 'SPORT', '{\"note\": \"跑步 30分钟 距离 2 公里 弱\", \"time\": \"19:56\"}', NULL, 'MANUAL', NULL, NULL, NULL, '2025-12-25 19:57:09', NULL);
INSERT INTO `health_logs` VALUES (113, 3, '2025-12-25', 'MOOD', '{\"note\": \"焦虑 生气焦虑\", \"time\": \"19:57\"}', NULL, 'MANUAL', NULL, NULL, NULL, '2025-12-25 19:57:35', NULL);
INSERT INTO `health_logs` VALUES (114, 3, '2025-12-25', 'VITALS', '{\"time\": \"19:57\", \"type\": \"血压\", \"unit\": \"mmHg\", \"value\": null, \"systolic\": 135, \"diastolic\": 80}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2025-12-25 19:58:10', NULL);
INSERT INTO `health_logs` VALUES (115, 3, '2025-12-25', 'VITALS', '{\"time\": \"19:57\", \"type\": \"血糖\", \"unit\": \"mmol/L\", \"value\": 5.2}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2025-12-25 19:58:10', NULL);
INSERT INTO `health_logs` VALUES (116, 3, '2025-12-25', 'VITALS', '{\"time\": \"19:57\", \"type\": \"心率\", \"unit\": \"bpm\", \"value\": 65}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2025-12-25 19:58:10', NULL);
INSERT INTO `health_logs` VALUES (117, 3, '2025-12-25', 'VITALS', '{\"time\": \"19:57\", \"type\": \"体温\", \"unit\": \"°C\", \"value\": 36}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2025-12-25 19:58:10', NULL);
INSERT INTO `health_logs` VALUES (118, 3, '2025-12-25', 'VITALS', '{\"time\": \"19:57\", \"type\": \"体重\", \"unit\": \"kg\", \"value\": 80}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2025-12-25 19:58:10', NULL);
INSERT INTO `health_logs` VALUES (119, 3, '2025-12-25', 'DIET', '{\"items\": [{\"name\": \"米饭\", \"calories\": 260}, {\"name\": \"青菜\", \"calories\": 60}, {\"name\": \"清蒸鱼\", \"calories\": 200}], \"totalCalories\": 520.0}', NULL, NULL, NULL, 0, NULL, '2025-12-25 20:13:45', NULL);
INSERT INTO `health_logs` VALUES (120, 3, '2025-12-25', 'SPORT', '{\"note\": null, \"type\": \"run\", \"steps\": 5000, \"distanceKm\": 3.0, \"durationMinutes\": 30}', NULL, NULL, NULL, 0, NULL, '2025-12-25 20:13:51', NULL);
INSERT INTO `health_logs` VALUES (121, 3, '2025-12-25', 'SLEEP', '{\"note\": null, \"hours\": 7.0, \"deepHours\": 2.0, \"wakeCount\": 1}', NULL, NULL, NULL, 0, NULL, '2025-12-25 20:13:55', NULL);
INSERT INTO `health_logs` VALUES (122, 3, '2025-12-25', 'DIET', '{\"items\": [{\"name\": \"米饭\", \"calories\": 260}, {\"name\": \"青菜\", \"calories\": 60}, {\"name\": \"清蒸鱼\", \"calories\": 200}], \"totalCalories\": 520.0}', NULL, NULL, NULL, 0, NULL, '2025-12-25 20:53:11', NULL);
INSERT INTO `health_logs` VALUES (123, 3, '2025-12-25', 'DIET', '{\"items\": [{\"name\": \"米饭\", \"calories\": 260}, {\"name\": \"青菜\", \"calories\": 60}, {\"name\": \"清蒸鱼\", \"calories\": 200}], \"totalCalories\": 520.0}', NULL, NULL, NULL, 0, NULL, '2025-12-25 20:54:36', NULL);
INSERT INTO `health_logs` VALUES (124, 3, '2025-12-25', 'DIET', '{\"items\": [{\"name\": \"米饭\", \"calories\": 260}, {\"name\": \"青菜\", \"calories\": 60}, {\"name\": \"清蒸鱼\", \"calories\": 200}], \"totalCalories\": 520.0}', NULL, NULL, NULL, 0, NULL, '2025-12-25 20:59:05', NULL);
INSERT INTO `health_logs` VALUES (125, 3, '2025-12-25', 'DIET', '{\"items\": [{\"name\": \"肉包子\", \"calories\": 250}], \"totalCalories\": 250.0}', NULL, NULL, NULL, 0, NULL, '2025-12-25 21:02:55', NULL);
INSERT INTO `health_logs` VALUES (126, 6, '2025-12-27', 'DIET', '{\"note\": \"鸡蛋1、米饭1碗、煎饺5、鸭腿2；总热量：1175 千卡\", \"time\": \"20:33\", \"items\": [{\"name\": \"鸡蛋\", \"unit\": \"\", \"calories\": 70, \"quantity\": 1}, {\"name\": \"米饭\", \"unit\": \"碗\", \"calories\": 130, \"quantity\": 1}, {\"name\": \"煎饺\", \"unit\": \"\", \"calories\": 375, \"quantity\": 5}, {\"name\": \"鸭腿\", \"unit\": \"\", \"calories\": 600, \"quantity\": 2}], \"totalCalories\": 1175}', NULL, 'MANUAL', NULL, NULL, NULL, '2025-12-27 20:34:51', NULL);
INSERT INTO `health_logs` VALUES (127, 6, '2025-12-27', 'SLEEP', '{\"note\": \"23:00-07:00 8小时 差\", \"time\": \"20:34\"}', NULL, 'MANUAL', NULL, NULL, NULL, '2025-12-27 20:35:26', NULL);
INSERT INTO `health_logs` VALUES (128, 6, '2025-12-27', 'SPORT', '{\"note\": \"跑步 30分钟 距离 5 公里 轻\", \"time\": \"20:35\"}', NULL, 'MANUAL', NULL, NULL, NULL, '2025-12-27 20:36:27', NULL);
INSERT INTO `health_logs` VALUES (129, 6, '2025-12-27', 'MOOD', '{\"note\": \"压力 强度 3\", \"time\": \"20:36\"}', NULL, 'MANUAL', NULL, NULL, NULL, '2025-12-27 20:36:52', NULL);
INSERT INTO `health_logs` VALUES (130, 6, '2025-12-27', 'VITALS', '{\"time\": \"20:36\", \"type\": \"血压\", \"unit\": \"mmHg\", \"value\": null, \"systolic\": 130, \"diastolic\": 65}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2025-12-27 20:37:38', NULL);
INSERT INTO `health_logs` VALUES (131, 6, '2025-12-27', 'VITALS', '{\"time\": \"20:36\", \"type\": \"血糖\", \"unit\": \"mmol/L\", \"value\": 5.2}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2025-12-27 20:37:38', NULL);
INSERT INTO `health_logs` VALUES (132, 6, '2025-12-27', 'VITALS', '{\"time\": \"20:36\", \"type\": \"体重\", \"unit\": \"kg\", \"value\": 80}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2025-12-27 20:37:38', NULL);
INSERT INTO `health_logs` VALUES (133, 6, '2025-12-27', 'VITALS', '{\"time\": \"20:36\", \"type\": \"心率\", \"unit\": \"bpm\", \"value\": 65}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2025-12-27 20:37:38', NULL);
INSERT INTO `health_logs` VALUES (134, 6, '2025-12-27', 'DIET', '{\"items\": [{\"name\": \"肉包子\", \"calories\": 250}], \"totalCalories\": 250.0}', NULL, NULL, NULL, 0, NULL, '2025-12-27 21:30:26', NULL);
INSERT INTO `health_logs` VALUES (135, 4, '2025-12-28', 'DIET', '{\"note\": \"面条1碗；总热量：100千卡\", \"time\": \"11:15\", \"items\": [{\"name\": \"面条\", \"unit\": \"碗\", \"calories\": null, \"quantity\": 1}], \"totalCalories\": 0}', NULL, 'MANUAL', NULL, NULL, NULL, '2025-12-28 11:16:11', NULL);
INSERT INTO `health_logs` VALUES (136, 4, '2025-12-27', 'SLEEP', '{\"note\": \"23:00-07:00 8小时 良好\", \"time\": \"11:16\"}', NULL, 'MANUAL', NULL, NULL, NULL, '2025-12-28 11:17:16', NULL);
INSERT INTO `health_logs` VALUES (137, 4, '2025-12-27', 'SPORT', '{\"note\": \"凯格尔运动 30分钟 轻\", \"time\": \"11:17\"}', NULL, 'MANUAL', NULL, NULL, NULL, '2025-12-28 11:18:20', NULL);
INSERT INTO `health_logs` VALUES (138, 4, '2025-12-27', 'MOOD', '{\"note\": \"焦虑 强度 3 无特定触发事件，持续感到不安\", \"time\": \"11:18\"}', NULL, 'MANUAL', NULL, NULL, NULL, '2025-12-28 11:18:41', NULL);
INSERT INTO `health_logs` VALUES (139, 4, '2025-12-27', 'VITALS', '{\"time\": \"11:18\", \"type\": \"血压\", \"unit\": \"mmHg\", \"value\": null, \"systolic\": 200, \"diastolic\": 20}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2025-12-28 11:19:22', NULL);
INSERT INTO `health_logs` VALUES (140, 4, '2025-12-28', 'SLEEP', '{\"note\": \"23:00-07:00 8小时 差\", \"time\": \"11:29\"}', NULL, 'MANUAL', NULL, NULL, NULL, '2025-12-28 11:29:56', NULL);
INSERT INTO `health_logs` VALUES (141, 4, '2025-12-28', 'SPORT', '{\"note\": \"凯格尔运动 半小时\", \"time\": \"11:29\"}', NULL, 'MANUAL', NULL, NULL, NULL, '2025-12-28 11:30:35', NULL);
INSERT INTO `health_logs` VALUES (142, 4, '2025-12-28', 'MOOD', '{\"note\": \"沮丧 强度 3 与女友吵架\", \"time\": \"11:30\"}', NULL, 'MANUAL', NULL, NULL, NULL, '2025-12-28 11:31:10', NULL);
INSERT INTO `health_logs` VALUES (143, 5, '2026-01-16', 'VITALS', '{\"time\": \"12:43\", \"type\": \"血压\", \"unit\": \"mmHg\", \"value\": null, \"systolic\": 155, \"diastolic\": 95}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2026-01-16 12:44:28', NULL);
INSERT INTO `health_logs` VALUES (144, 5, '2026-01-16', 'VITALS', '{\"time\": \"12:43\", \"type\": \"心率\", \"unit\": \"bpm\", \"value\": 105}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2026-01-16 12:44:28', NULL);
INSERT INTO `health_logs` VALUES (145, 5, '2026-01-16', 'VITALS', '{\"time\": \"12:43\", \"type\": \"血糖\", \"unit\": \"mmol/L\", \"value\": 8.8}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2026-01-16 12:44:28', NULL);
INSERT INTO `health_logs` VALUES (149, 4, '2026-01-16', 'VITALS', '{\"time\": \"18:09\", \"type\": \"血压\", \"unit\": \"mmHg\", \"value\": null, \"systolic\": 155, \"diastolic\": 95}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2026-01-16 18:21:48', NULL);
INSERT INTO `health_logs` VALUES (150, 4, '2026-01-16', 'VITALS', '{\"time\": \"18:09\", \"type\": \"心率\", \"unit\": \"bpm\", \"value\": 105}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2026-01-16 18:21:48', NULL);
INSERT INTO `health_logs` VALUES (151, 4, '2026-01-16', 'VITALS', '{\"time\": \"18:09\", \"type\": \"血糖\", \"unit\": \"mmol/L\", \"value\": 8.8}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2026-01-16 18:21:48', NULL);
INSERT INTO `health_logs` VALUES (152, 4, '2026-01-16', 'VITALS', '{\"time\": \"18:09\", \"type\": \"体温\", \"unit\": \"°C\", \"value\": 38.5}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2026-01-16 18:21:48', NULL);
INSERT INTO `health_logs` VALUES (153, 4, '2026-01-16', 'VITALS', '{\"time\": \"18:09\", \"type\": \"体重变化\", \"unit\": \"kg\", \"value\": 7}', NULL, 'MANUAL', NULL, 0, '{\"anomaly\": {\"reason\": \"\", \"severity\": \"LOW\", \"isAnomaly\": false, \"suggestion\": \"\"}}', '2026-01-16 18:21:48', NULL);
INSERT INTO `health_logs` VALUES (154, 4, '2026-01-18', 'DIET', '{\"items\": [{\"name\": \"米饭\", \"calories\": 260}, {\"name\": \"青菜\", \"calories\": 60}, {\"name\": \"清蒸鱼\", \"calories\": 200}], \"totalCalories\": 520.0}', NULL, NULL, NULL, 0, NULL, '2026-01-18 17:07:52', NULL);
INSERT INTO `health_logs` VALUES (155, 4, '2026-01-18', 'DIET', '{\"items\": [{\"name\": \"蒸饺\", \"calories\": 200}], \"totalCalories\": 200.0}', NULL, NULL, NULL, 0, NULL, '2026-01-18 17:12:44', NULL);
INSERT INTO `health_logs` VALUES (156, 4, '2026-01-18', 'DIET', '{\"items\": [{\"name\": \"米饭\", \"calories\": 260}, {\"name\": \"青菜\", \"calories\": 60}, {\"name\": \"清蒸鱼\", \"calories\": 200}], \"totalCalories\": 520.0}', NULL, NULL, NULL, 0, NULL, '2026-01-18 17:42:38', NULL);
INSERT INTO `health_logs` VALUES (157, 4, '2026-01-18', 'DIET', '{\"items\": [{\"name\": \"蒸饺\", \"calories\": 60}], \"totalCalories\": 60.0}', NULL, NULL, NULL, 0, NULL, '2026-01-18 17:51:42', NULL);
INSERT INTO `health_logs` VALUES (158, 4, '2026-01-18', 'DIET', '{\"items\": [{\"name\": \"蒸饺\", \"calories\": 60}], \"totalCalories\": 60.0}', NULL, NULL, NULL, 0, NULL, '2026-01-18 18:03:43', NULL);
INSERT INTO `health_logs` VALUES (159, 4, '2026-01-18', 'SPORT', '{\"note\": null, \"type\": \"run\", \"steps\": 5000, \"distanceKm\": 3.0, \"durationMinutes\": 30}', NULL, NULL, NULL, 0, NULL, '2026-01-18 18:58:50', NULL);
INSERT INTO `health_logs` VALUES (163, 4, '2026-01-18', 'SLEEP', '{\"note\": null, \"hours\": 7.0, \"bedtime\": \"19:32\", \"wakeTime\": \"07:06\", \"deepHours\": 2.0, \"wakeCount\": 1, \"sleepLatency\": 10, \"wakeUpLatency\": 10}', NULL, NULL, NULL, 0, NULL, '2026-01-18 19:31:21', NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '健康计划与随访表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of health_plans
-- ----------------------------
INSERT INTO `health_plans` VALUES (2, 10, 3, 1, 'WEIGHT_MANAGEMENT', '肝病管理与体重维持计划', '根据23岁男性患者的基础信息及中医体质，制定全面的健康管理与改善计划。重点在于控制体重和管理肝病。', '2026-01-17', '2026-02-17', 'WEEKLY', 3, NULL, '{\"weight\": \"<52.0\"}', '{\"time\": \"18:00\", \"channels\": [\"APP\"]}', 'ACTIVE', 0.00, 0.00, NULL, '2026-01-17 14:37:29', '2026-01-17 14:37:29');
INSERT INTO `health_plans` VALUES (3, 10, 4, 1, 'OTHER', '肝功能保护和体质调理', '针对22岁男性患者，年龄较轻但存在肝病问题以及QI DEFICIENCY体质，建议定期监测肝功能指标，并通过调整饮食和生活习惯改善体质。', '2026-01-17', '2026-02-17', 'MONTHLY', 4, NULL, '{\"liver_function_test\": \"stable\"}', '{\"time\": \"18:00\", \"channels\": [\"APP\"]}', 'ACTIVE', 0.00, 0.00, NULL, '2026-01-17 14:37:29', '2026-01-17 14:37:29');
INSERT INTO `health_plans` VALUES (4, 10, 6, 1, 'MEDICATION_MANAGEMENT', '糖尿病综合管理计划', '针对21岁男性糖尿病患者的饮食、运动及药物管理，旨在控制血糖水平并提高整体健康状况。', '2026-01-17', '2026-02-17', 'DAILY', 1, NULL, '{\"glucose_level\": \"<7.0mmol/L\"}', '{\"time\": \"08:00,12:00,16:00\", \"channels\": [\"APP\"]}', 'ACTIVE', 0.00, 0.00, NULL, '2026-01-17 14:37:30', '2026-01-17 14:37:30');
INSERT INTO `health_plans` VALUES (5, 10, 5, 1, 'DIET_MANAGEMENT', '痰湿体质管理计划', '本计划针对患者痰湿体质进行干预，旨在改善体内湿气过重的情况。', '2026-01-17', '2026-02-17', 'DAILY', 1, NULL, '{\"dietary_habits\": \"清淡易消化\"}', '{\"time\": \"08:00\", \"channels\": [\"APP\"]}', 'ACTIVE', 0.00, 0.00, NULL, '2026-01-17 14:37:30', '2026-01-17 14:37:30');

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
) ENGINE = InnoDB AUTO_INCREMENT = 209 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '智能健康提醒' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of health_reminders
-- ----------------------------
INSERT INTO `health_reminders` VALUES (26, 3, NULL, NULL, 'LIFESTYLE', '饮食记录提醒', '请记录今天的饮食情况，有助于了解营养摄入和健康管理。', NULL, '2025-12-28 16:50:15', '2025-12-28 16:50:39', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 15:50:15', '2025-12-28 19:04:28', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (27, 3, NULL, NULL, 'LIFESTYLE', '运动记录提醒', '请记录今天的运动情况，保持适量运动有益健康。', NULL, '2025-12-28 16:50:15', '2025-12-28 16:50:39', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 15:50:15', '2025-12-28 19:04:29', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (28, 3, NULL, NULL, 'LIFESTYLE', '情绪记录提醒', '请记录今天的情绪状态，有助于心理健康管理。', NULL, '2025-12-28 16:50:15', '2025-12-28 16:50:39', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 15:50:15', '2025-12-28 19:04:29', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (29, 3, NULL, NULL, 'LIFESTYLE', '睡眠记录提醒', '请记录今天的睡眠情况，良好睡眠对健康至关重要。', NULL, '2025-12-28 16:50:15', '2025-12-28 16:50:39', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 15:50:15', '2025-12-28 19:04:30', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (30, 3, NULL, NULL, 'ROUTINE', '定期健康评估提醒', '建议进行体质测评或健康评估，了解当前健康状况。', NULL, '2026-01-04 15:50:15', NULL, 'ACKNOWLEDGED', 'LOW', 'APP', NULL, '2025-12-28 15:50:15', '2025-12-28 19:04:36', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (32, 3, NULL, NULL, 'LIFESTYLE', '饮食记录提醒', '请记录今天的饮食情况，有助于了解营养摄入和健康管理。', NULL, '2025-12-28 18:26:01', '2025-12-28 18:26:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:26:01', '2025-12-28 19:04:30', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (33, 3, NULL, NULL, 'LIFESTYLE', '运动记录提醒', '请记录今天的运动情况，保持适量运动有益健康。', NULL, '2025-12-28 18:26:01', '2025-12-28 18:26:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:26:01', '2025-12-28 19:04:30', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (34, 3, NULL, NULL, 'LIFESTYLE', '情绪记录提醒', '请记录今天的情绪状态，有助于心理健康管理。', NULL, '2025-12-28 18:26:01', '2025-12-28 18:26:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:26:01', '2025-12-28 19:04:30', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (35, 3, NULL, NULL, 'LIFESTYLE', '睡眠记录提醒', '请记录今天的睡眠情况，良好睡眠对健康至关重要。', NULL, '2025-12-28 18:26:01', '2025-12-28 18:26:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:26:01', '2025-12-28 19:04:30', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (37, 5, NULL, NULL, 'LIFESTYLE', '饮食记录提醒', '请记录今天的饮食情况，有助于了解营养摄入和健康管理。', NULL, '2025-12-28 18:31:10', '2025-12-28 18:31:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:31:10', '2026-01-16 12:33:52', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (38, 5, NULL, NULL, 'LIFESTYLE', '运动记录提醒', '请记录今天的运动情况，保持适量运动有益健康。', NULL, '2025-12-28 18:31:10', '2025-12-28 18:31:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:31:10', '2026-01-16 12:33:53', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (39, 5, NULL, NULL, 'LIFESTYLE', '情绪记录提醒', '请记录今天的情绪状态，有助于心理健康管理。', NULL, '2025-12-28 18:31:10', '2025-12-28 18:31:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:31:10', '2026-01-16 12:33:52', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (40, 5, NULL, NULL, 'MEASUREMENT', '体征测量提醒', '请测量并记录血压、血糖等体征数据，有助于健康监测。', NULL, '2025-12-28 19:31:10', '2025-12-29 11:55:31', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:31:10', '2026-01-16 12:33:55', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (41, 5, NULL, NULL, 'LIFESTYLE', '睡眠记录提醒', '请记录今天的睡眠情况，良好睡眠对健康至关重要。', NULL, '2025-12-28 18:31:10', '2025-12-28 18:31:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:31:10', '2026-01-16 12:33:53', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (42, 5, NULL, NULL, 'ROUTINE', '定期健康评估提醒', '建议进行体质测评或健康评估，了解当前健康状况。', NULL, '2026-01-04 17:31:10', '2026-01-14 11:38:22', 'ACKNOWLEDGED', 'LOW', 'APP', NULL, '2025-12-28 17:31:10', '2026-01-16 12:33:56', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (43, 3, NULL, NULL, 'LIFESTYLE', '饮食记录提醒', '请记录今天的饮食情况，有助于了解营养摄入和健康管理。', NULL, '2025-12-28 18:31:38', '2025-12-28 18:31:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:31:38', '2025-12-28 19:04:30', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (44, 3, NULL, NULL, 'LIFESTYLE', '运动记录提醒', '请记录今天的运动情况，保持适量运动有益健康。', NULL, '2025-12-28 18:31:38', '2025-12-28 18:31:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:31:38', '2025-12-28 19:04:30', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (45, 3, NULL, NULL, 'LIFESTYLE', '情绪记录提醒', '请记录今天的情绪状态，有助于心理健康管理。', NULL, '2025-12-28 18:31:38', '2025-12-28 18:31:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:31:38', '2025-12-28 19:04:31', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (46, 3, NULL, NULL, 'LIFESTYLE', '睡眠记录提醒', '请记录今天的睡眠情况，良好睡眠对健康至关重要。', NULL, '2025-12-28 18:31:38', '2025-12-28 18:31:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:31:38', '2025-12-28 19:04:31', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (47, 3, NULL, NULL, 'LIFESTYLE', '饮食记录提醒', '请记录今天的饮食情况，有助于了解营养摄入和健康管理。', NULL, '2025-12-28 18:34:37', '2025-12-28 18:34:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:34:37', '2025-12-28 19:04:31', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (48, 3, NULL, NULL, 'LIFESTYLE', '运动记录提醒', '请记录今天的运动情况，保持适量运动有益健康。', NULL, '2025-12-28 18:34:37', '2025-12-28 18:34:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:34:37', '2025-12-28 19:04:31', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (49, 3, NULL, NULL, 'LIFESTYLE', '情绪记录提醒', '请记录今天的情绪状态，有助于心理健康管理。', NULL, '2025-12-28 18:34:37', '2025-12-28 18:34:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:34:37', '2025-12-28 19:04:31', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (50, 3, NULL, NULL, 'LIFESTYLE', '睡眠记录提醒', '请记录今天的睡眠情况，良好睡眠对健康至关重要。', NULL, '2025-12-28 18:34:37', '2025-12-28 18:34:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:34:37', '2025-12-28 19:04:31', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (51, 3, NULL, NULL, 'LIFESTYLE', '饮食记录提醒', '请记录今天的饮食情况，有助于了解营养摄入和健康管理。', NULL, '2025-12-28 18:34:42', '2025-12-28 18:34:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:34:42', '2025-12-28 19:04:32', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (52, 3, NULL, NULL, 'LIFESTYLE', '运动记录提醒', '请记录今天的运动情况，保持适量运动有益健康。', NULL, '2025-12-28 18:34:42', '2025-12-28 18:34:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:34:42', '2025-12-28 19:04:32', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (53, 3, NULL, NULL, 'LIFESTYLE', '情绪记录提醒', '请记录今天的情绪状态，有助于心理健康管理。', NULL, '2025-12-28 18:34:42', '2025-12-28 18:34:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:34:42', '2025-12-28 19:04:32', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (54, 3, NULL, NULL, 'LIFESTYLE', '睡眠记录提醒', '请记录今天的睡眠情况，良好睡眠对健康至关重要。', NULL, '2025-12-28 18:34:42', '2025-12-28 18:34:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:34:42', '2025-12-28 19:04:31', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (55, 3, NULL, 2, 'LIFESTYLE', '饮食记录提醒', '请记录今天的饮食情况，有助于了解营养摄入和健康管理。', NULL, '2025-12-28 18:39:31', '2025-12-28 18:39:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:39:31', '2025-12-28 19:04:32', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (56, 3, NULL, 2, 'LIFESTYLE', '运动记录提醒', '请记录今天的运动情况，保持适量运动有益健康。', NULL, '2025-12-28 18:39:31', '2025-12-28 18:39:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:39:31', '2025-12-28 19:04:32', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (57, 3, NULL, 2, 'LIFESTYLE', '情绪记录提醒', '请记录今天的情绪状态，有助于心理健康管理。', NULL, '2025-12-28 18:39:31', '2025-12-28 18:39:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:39:31', '2025-12-28 19:04:32', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (58, 3, NULL, 2, 'LIFESTYLE', '睡眠记录提醒', '请记录今天的睡眠情况，良好睡眠对健康至关重要。', NULL, '2025-12-28 18:39:31', '2025-12-28 18:39:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:39:31', '2025-12-28 19:04:33', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (60, 5, NULL, 3, 'LIFESTYLE', '饮食记录提醒', '请记录今天的饮食情况，有助于了解营养摄入和健康管理。', NULL, '2025-12-28 18:45:04', '2025-12-28 18:45:19', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:45:04', '2026-01-16 12:33:54', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (61, 5, NULL, 3, 'LIFESTYLE', '运动记录提醒', '请记录今天的运动情况，保持适量运动有益健康。', NULL, '2025-12-28 18:45:04', '2025-12-28 18:45:19', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:45:04', '2026-01-16 12:33:54', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (62, 5, NULL, 3, 'LIFESTYLE', '情绪记录提醒', '请记录今天的情绪状态，有助于心理健康管理。', NULL, '2025-12-28 18:45:04', '2025-12-28 18:45:19', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:45:04', '2026-01-16 12:33:54', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (63, 5, NULL, 3, 'MEASUREMENT', '体征测量提醒', '请测量并记录血压、血糖等体征数据，有助于健康监测。', NULL, '2025-12-28 19:45:04', '2025-12-29 11:55:31', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:45:04', '2026-01-16 12:33:56', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (64, 5, NULL, 3, 'LIFESTYLE', '睡眠记录提醒', '请记录今天的睡眠情况，良好睡眠对健康至关重要。', NULL, '2025-12-28 18:45:04', '2025-12-28 18:45:19', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:45:04', '2026-01-16 12:33:54', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (66, 5, NULL, 3, 'LIFESTYLE', '饮食记录提醒', '请记录今天的饮食情况，有助于了解营养摄入和健康管理。', NULL, '2025-12-28 18:51:34', '2025-12-28 18:52:19', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:51:34', '2026-01-16 12:33:54', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (67, 5, NULL, 3, 'LIFESTYLE', '运动记录提醒', '请记录今天的运动情况，保持适量运动有益健康。', NULL, '2025-12-28 18:51:34', '2025-12-28 18:52:19', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:51:34', '2026-01-16 12:33:54', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (68, 5, NULL, 3, 'LIFESTYLE', '情绪记录提醒', '请记录今天的情绪状态，有助于心理健康管理。', NULL, '2025-12-28 18:51:34', '2025-12-28 18:52:19', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:51:34', '2026-01-16 12:33:55', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (69, 5, NULL, 3, 'MEASUREMENT', '体征测量提醒', '请测量并记录血压、血糖等体征数据，有助于健康监测。', NULL, '2025-12-28 19:51:34', '2025-12-29 11:55:31', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:51:34', '2026-01-16 12:33:56', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (70, 5, NULL, 3, 'LIFESTYLE', '睡眠记录提醒', '请记录今天的睡眠情况，良好睡眠对健康至关重要。', NULL, '2025-12-28 18:51:34', '2025-12-28 18:52:19', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:51:34', '2026-01-16 12:33:55', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (71, 3, NULL, 2, 'LIFESTYLE', '饮食记录提醒', '请记录今天的饮食情况，有助于了解营养摄入和健康管理。', NULL, '2025-12-28 18:59:11', '2025-12-28 19:00:08', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:59:11', '2025-12-28 19:04:33', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (72, 3, NULL, 2, 'LIFESTYLE', '运动记录提醒', '请记录今天的运动情况，保持适量运动有益健康。', NULL, '2025-12-28 18:59:11', '2025-12-28 19:00:08', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:59:11', '2025-12-28 19:04:33', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (73, 3, NULL, 2, 'LIFESTYLE', '情绪记录提醒', '请记录今天的情绪状态，有助于心理健康管理。', NULL, '2025-12-28 18:59:11', '2025-12-28 19:00:08', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:59:11', '2025-12-28 19:04:33', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (74, 3, NULL, 2, 'LIFESTYLE', '睡眠记录提醒', '请记录今天的睡眠情况，良好睡眠对健康至关重要。', NULL, '2025-12-28 18:59:11', '2025-12-28 19:00:08', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 17:59:11', '2025-12-28 19:04:33', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (75, 3, NULL, 1, 'LIFESTYLE', '饮食记录提醒', '请记录今天的饮食情况，有助于了解营养摄入和健康管理。', NULL, '2025-12-28 19:04:02', '2025-12-28 19:04:16', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:04:02', '2025-12-28 19:04:33', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (76, 3, NULL, 1, 'LIFESTYLE', '运动记录提醒', '请记录今天的运动情况，保持适量运动有益健康。', NULL, '2025-12-28 19:04:02', '2025-12-28 19:04:16', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:04:02', '2025-12-28 19:04:34', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (77, 3, NULL, 1, 'LIFESTYLE', '情绪记录提醒', '请记录今天的情绪状态，有助于心理健康管理。', NULL, '2025-12-28 19:04:02', '2025-12-28 19:04:16', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:04:02', '2025-12-28 19:04:34', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (78, 3, NULL, 1, 'LIFESTYLE', '睡眠记录提醒', '请记录今天的睡眠情况，良好睡眠对健康至关重要。', NULL, '2025-12-28 19:04:02', '2025-12-28 19:04:16', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:04:02', '2025-12-28 19:04:34', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (79, 6, 6, 1, 'MEDICATION', '吃鸡', '好勒', NULL, '2025-12-28 00:00:00', '2025-12-28 18:04:43', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:04:16', '2025-12-28 18:21:10', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (80, 6, NULL, 1, 'ROUTINE', '定期健康评估提醒', '建议进行体质测评或健康评估，了解当前健康状况。', NULL, '2026-01-04 18:06:44', NULL, 'ACKNOWLEDGED', 'LOW', 'APP', NULL, '2025-12-28 18:06:44', '2025-12-28 19:05:25', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (81, 5, NULL, 1, 'LIFESTYLE', '饮食记录提醒', '请记录今天的饮食情况，有助于了解营养摄入和健康管理。', NULL, '2025-12-28 19:12:22', '2025-12-28 19:04:47', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:12:22', '2026-01-16 12:33:55', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (82, 5, NULL, 1, 'LIFESTYLE', '运动记录提醒', '请记录今天的运动情况，保持适量运动有益健康。', NULL, '2025-12-28 19:12:22', '2025-12-28 19:04:48', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:12:22', '2026-01-16 12:33:55', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (83, 5, NULL, 1, 'LIFESTYLE', '情绪记录提醒', '请记录今天的情绪状态，有助于心理健康管理。', NULL, '2025-12-28 19:12:22', '2025-12-28 19:04:48', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:12:22', '2026-01-16 12:33:55', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (84, 5, NULL, 1, 'MEASUREMENT', '体征测量提醒', '请测量并记录血压、血糖等体征数据，有助于健康监测。', NULL, '2025-12-28 20:12:22', '2025-12-29 11:55:31', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:12:22', '2026-01-16 12:33:56', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (85, 5, NULL, 1, 'LIFESTYLE', '睡眠记录提醒', '请记录今天的睡眠情况，良好睡眠对健康至关重要。', NULL, '2025-12-28 19:12:22', '2025-12-28 19:04:49', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:12:22', '2026-01-16 12:33:55', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (86, 3, NULL, 1, 'LIFESTYLE', '饮食记录提醒', '请记录今天的饮食情况，有助于了解营养摄入和健康管理。', NULL, '2025-12-28 19:15:41', NULL, 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:15:41', '2025-12-28 19:04:34', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (87, 3, NULL, 1, 'LIFESTYLE', '运动记录提醒', '请记录今天的运动情况，保持适量运动有益健康。', NULL, '2025-12-28 19:15:41', NULL, 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:15:41', '2025-12-28 19:04:34', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (88, 3, NULL, 1, 'LIFESTYLE', '情绪记录提醒', '请记录今天的情绪状态，有助于心理健康管理。', NULL, '2025-12-28 19:15:41', NULL, 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:15:41', '2025-12-28 19:04:34', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (89, 3, NULL, 1, 'LIFESTYLE', '睡眠记录提醒', '请记录今天的睡眠情况，良好睡眠对健康至关重要。', NULL, '2025-12-28 19:15:41', NULL, 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:15:41', '2025-12-28 19:04:35', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (90, 3, NULL, 1, 'LIFESTYLE', '饮食记录提醒', '请记录今天的饮食情况，有助于了解营养摄入和健康管理。', NULL, '2025-12-28 19:17:35', NULL, 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:17:35', '2025-12-28 19:04:35', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (91, 3, NULL, 1, 'LIFESTYLE', '运动记录提醒', '请记录今天的运动情况，保持适量运动有益健康。', NULL, '2025-12-28 19:17:35', NULL, 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:17:35', '2025-12-28 19:04:35', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (92, 3, NULL, 1, 'LIFESTYLE', '情绪记录提醒', '请记录今天的情绪状态，有助于心理健康管理。', NULL, '2025-12-28 19:17:35', NULL, 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:17:35', '2025-12-28 19:04:35', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (93, 3, NULL, 1, 'LIFESTYLE', '睡眠记录提醒', '请记录今天的睡眠情况，良好睡眠对健康至关重要。', NULL, '2025-12-28 19:17:35', NULL, 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:17:36', '2025-12-28 19:04:35', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (94, 6, 6, 1, 'MEDICATION', '吃点好的', '麻辣串', NULL, '2025-12-28 00:00:00', '2025-12-28 18:21:50', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:21:43', '2025-12-28 18:21:59', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (95, 6, 6, 1, 'MEDICATION', '11', '1111', NULL, '2025-12-28 00:00:00', '2025-12-28 18:45:19', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:44:25', '2025-12-28 19:05:24', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (96, 6, 6, 1, 'ROUTINE', '111', '111', NULL, '2025-12-28 00:00:00', '2025-12-28 18:45:19', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2025-12-28 18:44:35', '2025-12-28 19:05:24', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (97, 3, 3, 1, 'MEDICATION', '吃点好的', '打搅', NULL, '2026-01-16 00:00:00', NULL, 'ACKNOWLEDGED', 'HIGH', 'APP', NULL, '2026-01-16 12:24:10', '2026-01-16 12:24:19', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (98, 6, 6, 1, 'ROUTINE', '协作提醒：饮食记录', '请记得记录今天的饮食，便于后续分析。', NULL, '2026-01-16 12:34:34', '2026-01-16 12:34:40', 'SENT', 'MEDIUM', 'APP', NULL, '2026-01-16 12:24:34', '2026-01-16 12:34:40', 6, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (99, 5, 5, 1, 'MEASUREMENT', '协作提醒：体征记录', '请记得测量并记录体征数据，便于健康监测。', NULL, '2026-01-16 12:34:34', NULL, 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2026-01-16 12:24:34', '2026-01-16 12:33:56', 5, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (100, 6, 6, 1, 'ROUTINE', '协作提醒：饮食记录', '请记得记录今天的饮食，便于后续分析。', NULL, '2026-01-16 12:34:34', '2026-01-16 12:34:40', 'SENT', 'MEDIUM', 'APP', NULL, '2026-01-16 12:24:34', '2026-01-16 12:34:40', 6, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (101, 3, NULL, 1, 'LIFESTYLE', '饮食记录提醒', '请记录今天的饮食情况，有助于了解营养摄入和健康管理。', NULL, '2026-01-16 13:25:25', '2026-01-16 13:25:41', 'SENT', 'MEDIUM', 'APP', NULL, '2026-01-16 12:25:25', '2026-01-16 13:25:41', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (102, 3, 3, 1, 'LIFESTYLE', '运动记录提醒', '请记录今天的运动情况，保持适量运动有益健康。', NULL, '2026-01-16 13:25:25', '2026-01-16 13:25:41', 'SENT', 'MEDIUM', 'APP', NULL, '2026-01-16 12:25:25', '2026-01-16 13:25:41', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (103, 3, NULL, 1, 'LIFESTYLE', '情绪记录提醒', '请记录今天的情绪状态，有助于心理健康管理。', NULL, '2026-01-16 13:25:25', '2026-01-16 12:33:45', 'SENT', 'MEDIUM', 'APP', NULL, '2026-01-16 12:25:25', '2026-01-16 12:33:45', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (104, 3, NULL, 1, 'MEASUREMENT', '体征测量提醒', '请测量并记录血压、血糖等体征数据，有助于健康监测。', NULL, '2026-01-16 14:25:25', '2026-01-16 12:33:41', 'SENT', 'MEDIUM', 'APP', NULL, '2026-01-16 12:25:25', '2026-01-16 12:33:41', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (105, 3, NULL, 1, 'LIFESTYLE', '睡眠记录提醒', '请记录今天的睡眠情况，良好睡眠对健康至关重要。', NULL, '2026-01-16 13:25:25', '2026-01-16 12:33:45', 'SENT', 'MEDIUM', 'APP', NULL, '2026-01-16 12:25:25', '2026-01-16 12:33:45', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (106, 5, 5, 1, 'LIFESTYLE', '饮食记录提醒', '请记录今天的饮食情况，有助于了解营养摄入和健康管理。', NULL, '2026-01-16 13:03:23', NULL, 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2026-01-16 12:33:23', '2026-01-16 12:33:56', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (107, 5, 5, 1, 'LIFESTYLE', '运动记录提醒', '请记录今天的运动情况，保持适量运动有益健康。', NULL, '2026-01-16 13:33:23', '2026-01-16 12:33:42', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2026-01-16 12:33:23', '2026-01-16 12:33:56', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (108, 5, 5, 1, 'LIFESTYLE', '情绪记录提醒', '请记录今天的情绪状态，有助于心理健康管理。', NULL, '2026-01-16 14:03:23', '2026-01-16 12:33:41', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2026-01-16 12:33:23', '2026-01-16 12:33:57', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (109, 5, 5, 1, 'MEASUREMENT', '体征测量提醒', '请测量并记录血压、血糖等体征数据，有助于健康监测。', NULL, '2026-01-16 14:33:23', '2026-01-16 12:33:42', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2026-01-16 12:33:23', '2026-01-16 12:33:58', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (110, 5, 5, 1, 'LIFESTYLE', '睡眠记录提醒', '请记录昨晚的睡眠情况，良好睡眠对健康至关重要。', NULL, '2026-01-16 15:03:23', '2026-01-16 12:33:43', 'ACKNOWLEDGED', 'MEDIUM', 'APP', NULL, '2026-01-16 12:33:23', '2026-01-16 12:33:58', 4, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (111, 6, NULL, 1, 'ABNORMAL', '健康异常提醒', '您的血压值异常（135），请立即测量并记录，如有不适请及时就医。', NULL, '2026-01-16 14:18:48', '2026-01-16 14:19:28', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 14:18:48', '2026-01-16 14:19:28', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (112, 6, NULL, 1, 'ROUTINE', '异常指标随访跟踪', '针对异常指标(血压)的后续跟踪：您的血压值异常（135），请立即测量并记录，如有不适请及时就医。', NULL, '2026-01-15 16:00:00', '2026-01-16 14:19:28', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 14:18:48', '2026-01-16 14:19:28', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (113, 6, NULL, 1, 'ABNORMAL', '健康异常提醒', '您的血压值异常（135），请立即测量并记录，如有不适请及时就医。', NULL, '2026-01-16 14:26:21', '2026-01-16 14:26:28', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 14:26:21', '2026-01-16 14:26:28', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (114, 6, NULL, 1, 'ABNORMAL', '健康异常提醒', '您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', NULL, '2026-01-16 14:33:02', '2026-01-16 14:33:28', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 14:33:02', '2026-01-16 14:33:28', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (115, 6, NULL, 1, 'ROUTINE', '异常指标随访跟踪', '针对异常指标(血压)的后续跟踪：您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', NULL, '2026-01-16 16:00:00', '2026-01-16 16:00:22', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 14:33:02', '2026-01-16 16:00:22', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (116, 6, NULL, 1, 'ABNORMAL', '健康异常提醒', '您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', NULL, '2026-01-16 14:50:28', '2026-01-16 14:50:48', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 14:50:28', '2026-01-16 14:50:48', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (117, 3, NULL, 1, 'ABNORMAL', '健康异常提醒', '您的血压值异常（135），请立即测量并记录，如有不适请及时就医。', NULL, '2026-01-16 14:50:31', '2026-01-16 14:50:48', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 14:50:31', '2026-01-16 14:50:48', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (118, 6, NULL, 1, 'ABNORMAL', '健康异常提醒', '您的血压值异常（135），请立即测量并记录，如有不适请及时就医。', NULL, '2026-01-16 14:50:33', '2026-01-16 14:50:48', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 14:50:33', '2026-01-16 14:50:48', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (119, 6, NULL, 1, 'ABNORMAL', '健康异常提醒', '您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', NULL, '2026-01-16 14:51:14', '2026-01-16 14:51:48', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 14:51:14', '2026-01-16 14:51:48', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (120, 6, NULL, 1, 'ROUTINE', '异常指标随访跟踪', '针对异常指标(血压)的后续跟踪：您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', NULL, '2026-01-15 16:00:00', '2026-01-16 14:51:48', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 14:51:14', '2026-01-16 14:51:48', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (121, 6, NULL, 1, 'ABNORMAL', '健康异常提醒', '您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', NULL, '2026-01-16 14:58:29', '2026-01-16 14:58:33', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 14:58:29', '2026-01-16 14:58:33', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (123, 6, NULL, 1, 'ABNORMAL', '健康异常提醒', '您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', NULL, '2026-01-16 15:15:58', '2026-01-16 15:16:42', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 15:15:58', '2026-01-16 15:16:42', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (125, 6, NULL, 1, 'ABNORMAL', '健康异常提醒', '您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', NULL, '2026-01-16 15:18:55', '2026-01-16 15:19:36', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 15:18:55', '2026-01-16 15:19:36', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (127, 6, NULL, 1, 'ABNORMAL', '健康异常提醒', '您的血糖值异常（130），请注意饮食控制，按医嘱用药。', NULL, '2026-01-16 16:03:16', '2026-01-16 16:03:44', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 16:03:16', '2026-01-16 16:03:44', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (129, 6, NULL, 1, 'ABNORMAL', '健康异常提醒', '您的血糖值异常（130），请注意饮食控制，按医嘱用药。', NULL, '2026-01-16 16:03:57', '2026-01-16 16:04:44', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 16:03:57', '2026-01-16 16:04:44', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (130, 6, NULL, 1, 'ABNORMAL', '健康异常提醒', '您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', NULL, '2026-01-16 16:05:55', '2026-01-16 16:06:44', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 16:05:55', '2026-01-16 16:06:44', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (131, 6, NULL, 1, 'FOLLOW_UP', '异常指标随访跟踪', '针对异常指标(血压)的后续跟踪：您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', NULL, '2026-01-15 16:00:00', '2026-01-16 16:40:11', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 16:05:55', '2026-01-16 16:40:11', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (134, 6, NULL, 1, 'ABNORMAL', '健康异常提醒', '您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', NULL, '2026-01-16 16:39:21', '2026-01-16 16:40:11', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 16:39:21', '2026-01-16 16:40:11', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (135, 6, NULL, 1, 'FOLLOW_UP', '异常指标随访跟踪', '针对异常指标(血压)的后续跟踪：您的血压值异常（130），请立即测量并记录，如有不适请及时就医。', NULL, '2026-01-16 00:00:00', '2026-01-16 16:40:11', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 16:39:21', '2026-01-16 16:40:11', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (139, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-01-19 09:00:00', '2026-01-19 11:32:18', 'SENT', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-19 11:32:18', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (140, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-01-20 09:00:00', '2026-01-20 13:12:26', 'SENT', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-20 13:12:26', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (141, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-01-21 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (142, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-01-22 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (143, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-01-23 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (144, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-01-24 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (145, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-01-25 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (146, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-01-26 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (147, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-01-27 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (148, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-01-28 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (149, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-01-29 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (150, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-01-30 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (151, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-01-31 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (152, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-02-01 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (153, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-02-02 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (154, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-02-03 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (155, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-02-04 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (156, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-02-05 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (157, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-02-06 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (158, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-02-07 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (159, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-02-08 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (160, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-02-09 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (161, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-02-10 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (162, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-02-11 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (163, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-02-12 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (164, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-02-13 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (165, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-02-14 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (166, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-02-15 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (167, 3, NULL, 1, 'FOLLOW_UP', '111 - 提醒', '请按时执行健康计划：111', NULL, '2026-02-16 09:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 1, \"planTitle\": \"111\"}', '2026-01-16 16:42:32', '2026-01-16 16:42:32', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (168, 4, NULL, 1, 'ABNORMAL', '健康异常提醒', '您的血压值异常（200），请立即测量并记录，如有不适请及时就医。', NULL, '2026-01-16 18:39:47', '2026-01-16 18:40:37', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 18:39:47', '2026-01-16 18:40:37', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (169, 4, NULL, 1, 'ABNORMAL', '健康异常提醒', '您的血压值异常（155），请立即测量并记录，如有不适请及时就医。', NULL, '2026-01-16 18:39:52', '2026-01-16 18:40:37', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 18:39:52', '2026-01-16 18:40:37', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (170, 4, NULL, 1, 'FOLLOW_UP', '异常指标随访跟踪', '针对异常指标(血压)的后续跟踪：您的血压值异常（155），请立即测量并记录，如有不适请及时就医。', NULL, '2026-01-16 00:00:00', '2026-01-16 18:40:37', 'SENT', 'HIGH', 'APP', NULL, '2026-01-16 18:39:52', '2026-01-16 18:40:37', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (171, 3, NULL, 1, 'FOLLOW_UP', '肝病管理与体重维持计划 - 提醒', '请按时执行健康计划：肝病管理与体重维持计划', NULL, '2026-01-17 18:00:00', '2026-01-17 21:31:50', 'SENT', 'MEDIUM', 'APP', '{\"planId\": 2, \"planTitle\": \"肝病管理与体重维持计划\"}', '2026-01-17 14:37:29', '2026-01-17 21:31:50', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (172, 3, NULL, 1, 'FOLLOW_UP', '肝病管理与体重维持计划 - 提醒', '请按时执行健康计划：肝病管理与体重维持计划', NULL, '2026-01-24 18:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 2, \"planTitle\": \"肝病管理与体重维持计划\"}', '2026-01-17 14:37:29', '2026-01-17 14:37:29', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (173, 3, NULL, 1, 'FOLLOW_UP', '肝病管理与体重维持计划 - 提醒', '请按时执行健康计划：肝病管理与体重维持计划', NULL, '2026-01-31 18:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 2, \"planTitle\": \"肝病管理与体重维持计划\"}', '2026-01-17 14:37:29', '2026-01-17 14:37:29', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (174, 3, NULL, 1, 'FOLLOW_UP', '肝病管理与体重维持计划 - 提醒', '请按时执行健康计划：肝病管理与体重维持计划', NULL, '2026-02-07 18:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 2, \"planTitle\": \"肝病管理与体重维持计划\"}', '2026-01-17 14:37:29', '2026-01-17 14:37:29', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (175, 3, NULL, 1, 'FOLLOW_UP', '肝病管理与体重维持计划 - 提醒', '请按时执行健康计划：肝病管理与体重维持计划', NULL, '2026-02-14 18:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 2, \"planTitle\": \"肝病管理与体重维持计划\"}', '2026-01-17 14:37:29', '2026-01-17 14:37:29', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (176, 4, NULL, 1, 'FOLLOW_UP', '肝功能保护和体质调理 - 提醒', '请按时执行健康计划：肝功能保护和体质调理', NULL, '2026-01-17 18:00:00', '2026-01-17 21:31:50', 'SENT', 'MEDIUM', 'APP', '{\"planId\": 3, \"planTitle\": \"肝功能保护和体质调理\"}', '2026-01-17 14:37:29', '2026-01-17 21:31:50', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (177, 4, NULL, 1, 'FOLLOW_UP', '肝功能保护和体质调理 - 提醒', '请按时执行健康计划：肝功能保护和体质调理', NULL, '2026-02-17 18:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 3, \"planTitle\": \"肝功能保护和体质调理\"}', '2026-01-17 14:37:29', '2026-01-17 14:37:29', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (178, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-01-17 08:00:00', '2026-01-17 14:38:22', 'SENT', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:38:22', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (179, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-01-18 08:00:00', '2026-01-18 10:27:38', 'SENT', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-18 10:27:38', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (180, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-01-19 08:00:00', '2026-01-19 11:32:18', 'SENT', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-19 11:32:18', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (181, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-01-20 08:00:00', '2026-01-20 13:12:26', 'SENT', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-20 13:12:26', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (182, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-01-21 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (183, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-01-22 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (184, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-01-23 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (185, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-01-24 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (186, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-01-25 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (187, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-01-26 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (188, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-01-27 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (189, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-01-28 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (190, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-01-29 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (191, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-01-30 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (192, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-01-31 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (193, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-02-01 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (194, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-02-02 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (195, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-02-03 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (196, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-02-04 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (197, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-02-05 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (198, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-02-06 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (199, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-02-07 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (200, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-02-08 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (201, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-02-09 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (202, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-02-10 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (203, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-02-11 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (204, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-02-12 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (205, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-02-13 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (206, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-02-14 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (207, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-02-15 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (208, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-02-16 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);
INSERT INTO `health_reminders` VALUES (209, 5, NULL, 1, 'FOLLOW_UP', '痰湿体质管理计划 - 提醒', '请按时执行健康计划：痰湿体质管理计划', NULL, '2026-02-17 08:00:00', NULL, 'PENDING', 'MEDIUM', 'APP', '{\"planId\": 5, \"planTitle\": \"痰湿体质管理计划\"}', '2026-01-17 14:37:30', '2026-01-17 14:37:30', 10, 'PRIVATE', NULL);

-- ----------------------------
-- Table structure for health_reports
-- ----------------------------
DROP TABLE IF EXISTS `health_reports`;
CREATE TABLE `health_reports`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `error_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `image_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `interpretation` json NULL,
  `ocr_data` json NULL,
  `report_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `report_type` enum('EXAM_REPORT','LAB_REPORT','OTHER','PRESCRIPTION') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` enum('COMPLETED','FAILED','PENDING','PROCESSING') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `doctor_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `doctor_comment_time` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK1x7pagf8d16jp4flrve7j9xjd`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK1x7pagf8d16jp4flrve7j9xjd` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of health_reports
-- ----------------------------
INSERT INTO `health_reports` VALUES (1, '2026-01-19 17:15:35.743184', NULL, '/api/files/20260119/a78a2082-e042-4522-948e-d106a619d87d.png', '{\"details\": {\"空腹血糖\": \"正常范围 (3.9-6.1 mmol/L)，血糖控制良好。\", \"白细胞计数\": \"正常范围 (4.0-10.0 10^9/L)，表明无明显感染。\", \"红细胞计数\": \"正常范围 (4.0-5.5 10^12/L)，无贫血迹象。\"}, \"summary\": \"各项指标基本正常，请继续保持健康生活方式。\"}', '{\"date\": \"2023-10-27\", \"items\": {\"空腹血糖\": \"5.2\", \"血红蛋白\": \"140\", \"白细胞计数\": \"6.5\", \"红细胞计数\": \"4.8\", \"血小板计数\": \"220\"}, \"hospital\": \"模拟综合医院\"}', '114514', 'LAB_REPORT', 'COMPLETED', '2026-01-19 17:15:37.776486', 4, NULL, NULL);
INSERT INTO `health_reports` VALUES (2, '2026-01-19 17:19:06.801862', 'AI Analysis failed: [500] Internal Server Error - {\"error\":\"llama runner process has terminated: CUDA error\"}', '/api/files/20260119/863fa2b8-e78a-4f03-be4a-296409bb7cd3.png', NULL, NULL, '12121', 'LAB_REPORT', 'FAILED', '2026-01-19 17:19:14.780121', 4, NULL, NULL);
INSERT INTO `health_reports` VALUES (3, '2026-01-19 17:27:08.775270', NULL, '/api/files/20260119/f3a1efe3-d6cd-4b24-af61-1a38c64b10aa.png', '{\"details\": {\"D-二聚体\": \"结果为0.97 μg/ml，在<5.0μg/ml的标准范围内，表明患者的凝血功能正常。\", \"抗凝血酶III\": \"结果为97.00%，在80%-120%的标准范围内，表明患者的抗凝血功能正常。\", \"凝血酶原时间\": \"结果为14.0秒，在11.1-14.5秒的参考范围内，表明患者有正常的凝血能力。\", \"国际标准化比率\": \"结果为1.10，在0.75-1.2的标准范围内，表明患者的凝血功能正常。\", \"纤维蛋白原定量\": \"结果为3.47 g/L，在2.0-4.0g/L的参考范围内，表明患者有正常的纤维蛋白原水平。\", \"部分凝血活酶时间\": \"结果为38.4秒，在26.0-40.0秒的标准范围内，表明患者的凝血过程正常。\", \"纤维蛋白原降解产物\": \"结果为2.61 mg/ml，在<5.0mg/ml的参考范围内，表明患者没有明显的纤维蛋白溶解异常。\"}, \"summary\": \"根据报告中的数值，患者的国际标准化比率、凝血酶原时间、部分凝血活酶时间和纤维蛋白原定量在正常范围内。D-二聚体和纤维蛋白原降解产物的值也在参考范围内。\"}', '{\"date\": \"2018-06-30\", \"items\": {\"D-二聚体\": \"0.97 μg/ml\", \"抗凝血酶III\": \"97.00 %\", \"凝血酶原时间\": \"14.0 秒\", \"国际标准化比率\": \"1.10\", \"纤维蛋白原定量\": \"3.47 g/L\", \"部分凝血活酶时间\": \"38.4 秒\", \"纤维蛋白原降解产物\": \"2.61 mg/ml\"}, \"hospital\": \"未知\", \"interpretation\": {\"details\": {\"D-二聚体\": \"结果为0.97 μg/ml，在<5.0μg/ml的标准范围内，表明患者的凝血功能正常。\", \"抗凝血酶III\": \"结果为97.00%，在80%-120%的标准范围内，表明患者的抗凝血功能正常。\", \"凝血酶原时间\": \"结果为14.0秒，在11.1-14.5秒的参考范围内，表明患者有正常的凝血能力。\", \"国际标准化比率\": \"结果为1.10，在0.75-1.2的标准范围内，表明患者的凝血功能正常。\", \"纤维蛋白原定量\": \"结果为3.47 g/L，在2.0-4.0g/L的参考范围内，表明患者有正常的纤维蛋白原水平。\", \"部分凝血活酶时间\": \"结果为38.4秒，在26.0-40.0秒的标准范围内，表明患者的凝血过程正常。\", \"纤维蛋白原降解产物\": \"结果为2.61 mg/ml，在<5.0mg/ml的参考范围内，表明患者没有明显的纤维蛋白溶解异常。\"}, \"summary\": \"根据报告中的数值，患者的国际标准化比率、凝血酶原时间、部分凝血活酶时间和纤维蛋白原定量在正常范围内。D-二聚体和纤维蛋白原降解产物的值也在参考范围内。\"}}', '111', 'LAB_REPORT', 'COMPLETED', '2026-01-19 18:19:46.771613', 4, '太差了', '2026-01-19 18:19:46.770033');

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
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of health_thresholds
-- ----------------------------
INSERT INTO `health_thresholds` VALUES (1, 4, 'HR', 80, 80, '2025-11-14 17:09:14');
INSERT INTO `health_thresholds` VALUES (2, 4, 'BP_SYS', 80, 80, '2025-11-14 17:09:14');
INSERT INTO `health_thresholds` VALUES (3, 4, 'BP_DIA', 80, 80, '2025-11-14 17:09:14');
INSERT INTO `health_thresholds` VALUES (4, 4, 'GLU', 80, 80, '2025-11-14 17:09:14');
INSERT INTO `health_thresholds` VALUES (5, 3, '血糖_空腹', 4.4, 5.6, '2026-01-16 13:40:47');
INSERT INTO `health_thresholds` VALUES (6, 3, '体温', 36.2, 37, '2026-01-16 13:40:47');
INSERT INTO `health_thresholds` VALUES (7, 3, '血压_舒张压', 60, 80, '2026-01-16 13:40:47');
INSERT INTO `health_thresholds` VALUES (8, 3, '血糖_餐后', 4.4, 6.1, '2026-01-16 13:40:47');
INSERT INTO `health_thresholds` VALUES (9, 3, '血压_收缩压', 100, 120, '2026-01-16 13:40:47');
INSERT INTO `health_thresholds` VALUES (10, 3, '心率', 60, 80, '2026-01-16 13:40:47');
INSERT INTO `health_thresholds` VALUES (11, 3, '体重', 53.5, 69.4, '2026-01-16 13:40:47');

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识库文档（RAG）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of knowledge_documents
-- ----------------------------

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '提醒计划' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of plans
-- ----------------------------

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
-- Records of profiles
-- ----------------------------
INSERT INTO `profiles` VALUES (3, 'M', '2002-11-29', 170.00, 50.00, '[\"花粉\"]', '{\"email\": \"114514@163.com\", \"avatar\": \"/api/user/avatar/3/1766663278332.png\", \"aiScopes\": {\"diet\": true, \"sport\": true, \"vitals\": true, \"assessment\": true}, \"shareToAi\": true, \"shareToDoctor\": true, \"shareToFamily\": true, \"currentFamilyId\": 1}', '{\"提高免疫力\": true}', NULL, '[\"肝病\", \"糖尿病\"]', '{\"diet\": \"素食为主\", \"other\": \"吸烟、饮酒、嫖娼\", \"sleep\": \"通常24:00睡觉，经常失眠\", \"exercise\": \"每周跑步3次\"}', '2026-01-19 17:37:09');
INSERT INTO `profiles` VALUES (4, 'M', '2003-11-28', 178.00, 60.00, '[\"药物过敏\"]', '{\"email\": \"3179786731@qq.com\", \"avatar\": \"/api/user/avatar/4/1766911104677.jpg\", \"aiScopes\": {\"diet\": true, \"sport\": true, \"vitals\": true, \"assessment\": true}, \"shareToAi\": true, \"shareToDoctor\": true, \"shareToFamily\": true, \"currentFamilyId\": 3}', '{\"增重\": true, \"增强体质\": true, \"改善睡眠\": true}', NULL, '[\"肝病\"]', '{\"diet\": \"喜欢清淡\", \"other\": \"考公压力、吸烟、喝酒、嫖娼、易怒、喜欢殴打别人\", \"sleep\": \"23:00睡觉，8:00点起\", \"exercise\": \"每周跑步5次，每次5公里\"}', '2026-01-16 10:42:49');
INSERT INTO `profiles` VALUES (5, NULL, NULL, NULL, NULL, NULL, '{\"email\": \"1284542872@qq.com\", \"avatar\": \"/api/user/avatar/5/1766891635370.png\", \"aiScopes\": {\"diet\": true, \"sport\": true, \"vitals\": true, \"assessment\": true}, \"shareToAi\": true, \"shareToDoctor\": true, \"shareToFamily\": true, \"currentFamilyId\": 1}', NULL, NULL, NULL, NULL, '2026-01-16 13:03:53');
INSERT INTO `profiles` VALUES (6, 'M', '2004-03-27', 176.00, 80.00, '[\"药物过敏\"]', '{\"avatar\": \"/api/user/avatar/6/1766656767471.webp\", \"notifications\": {\"sms\": true, \"email\": true, \"inApp\": true}, \"shareToFamily\": true, \"currentFamilyId\": 3}', '{\"减重\": true, \"增强体质\": true, \"控制血糖\": true, \"改善睡眠\": true, \"缓解压力\": true, \"提高免疫力\": true}', NULL, '[\"糖尿病\"]', '{\"diet\": \"素食为主\", \"other\": \"工作压力大\", \"sleep\": \"23点睡觉，8点起\", \"exercise\": \"喜欢跑步，每周天天跑\"}', '2026-01-15 11:22:12');
INSERT INTO `profiles` VALUES (10, NULL, NULL, NULL, NULL, NULL, '{\"avatar\": \"/api/user/avatar/10/1768795298527.webp\", \"currentFamilyId\": 1, \"doctorWorkingHours\": {\"endTime\": \"18:00\", \"workDays\": [\"MON\", \"TUE\", \"WED\", \"THU\", \"FRI\"], \"startTime\": \"09:00\"}}', NULL, NULL, NULL, NULL, '2026-01-19 12:01:55');
INSERT INTO `profiles` VALUES (12, NULL, NULL, NULL, NULL, NULL, '{\"avatar\": \"/api/user/avatar/12/1767010264163.jpg\", \"shareToFamily\": false, \"currentFamilyId\": 1}', NULL, NULL, NULL, NULL, '2025-12-29 21:47:28');
INSERT INTO `profiles` VALUES (13, NULL, NULL, NULL, NULL, NULL, '{\"avatar\": \"/api/user/avatar/13/1768471947659.jpg\"}', NULL, NULL, NULL, NULL, '2026-01-15 18:12:28');

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
) ENGINE = InnoDB AUTO_INCREMENT = 123 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '个性化建议（含可解释证据）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of recommendations
-- ----------------------------
INSERT INTO `recommendations` VALUES (86, 3, '2025-11-14', 'DIET', '[{\"title\": \"情绪日记分析\", \"content\": \"根据2025-11-13日志显示用户有愤怒情绪，建议通过深呼吸、冥想或散步等方式来帮助调节情绪和减压。\", \"priority\": \"HIGH\", \"confidence\": 0.65, \"sourceTags\": [\"ai\", \"logs\"]}]', '[\"2025-11-13 情绪:愤怒,建议调节压力\"]', 'v2', 'READY', 'qwen2.5:7b', 'v1', '{\"raw\": \"{\\n  \\\"title\\\": \\\"情绪管理与压力调节\\\",\\n  \\\"summary\\\": \\\"基于情绪记录和评估结果，建议进行情绪管理和压力调节。\\\",\\n  \\\"items\\\": [\\n    {\\n      \\\"title\\\": \\\"情绪日记分析\\\",\\n      \\\"content\\\": \\\"根据2025-11-13日志显示用户有愤怒情绪，建议通过深呼吸、冥想或散步等方式来帮助调节情绪和减压。\\\",\\n      \\\"priority\\\": \\\"HIGH\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\",\\\"logs\\\"],\\n      \\\"confidence\\\": 0.65\\n    }\\n  ],\\n  \\\"evidence\\\": [\\n    \\\"2025-11-13 情绪:愤怒,建议调节压力\\\"\\n  ],\\n  \\\"safety\\\": {\\n    \\\"refuse\\\": false,\\n    \\\"message\\\": \\\"\\\"\\n  }\\n}\", \"safety\": {\"refuse\": false, \"message\": \"\"}, \"summary\": \"基于情绪记录和评估结果，建议进行情绪管理和压力调节。\"}', b'0', '2025-11-14 16:14:36');
INSERT INTO `recommendations` VALUES (107, 6, '2025-12-25', 'SPORT', '[{\"title\": \"增加中等强度跑步\", \"content\": \"建议每天跑步30分钟至1小时，可以提高心肺功能和耐力。最近一次跑步60分钟较弱，需要调整为中等强度以增强效果。\", \"priority\": \"MEDIUM\", \"confidence\": 0.7, \"sourceTags\": [\"ai\", \"logs\"]}, {\"title\": \"加入力量训练\", \"content\": \"可以每周进行2-3次的力量训练，如哑铃举重或使用自体重训练，提高肌肉力量和新陈代谢率。\", \"priority\": \"LOW\", \"confidence\": 0.6, \"sourceTags\": [\"ai\", \"logs\"]}, {\"title\": \"尝试间歇性高强度训练（HIIT）\", \"content\": \"可以每周安排1-2次，每次20分钟的HIIT训练，提高心率和脂肪燃烧效率。强度要根据个人体能逐渐增加。\", \"priority\": \"LOW\", \"confidence\": 0.6, \"sourceTags\": [\"ai\", \"logs\"]}]', '[\"跑步60分钟较弱\", \"需要改善中等强度的有氧运动\"]', 'v2', 'READY', 'qwen2.5:7b', 'v1', '{\"raw\": \"{\\n  \\\"title\\\": \\\"运动建议\\\",\\n  \\\"summary\\\": \\\"根据徐志永的健康日志，他需要增加适量的有氧运动和力量训练来改善当前状况。\\\",\\n  \\\"items\\\": [\\n    {\\n      \\\"title\\\": \\\"增加中等强度跑步\\\",\\n      \\\"content\\\": \\\"建议每天跑步30分钟至1小时，可以提高心肺功能和耐力。最近一次跑步60分钟较弱，需要调整为中等强度以增强效果。\\\",\\n      \\\"priority\\\": \\\"MEDIUM\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\",\\\"logs\\\"],\\n      \\\"confidence\\\": 0.7\\n    },\\n    {\\n      \\\"title\\\": \\\"加入力量训练\\\",\\n      \\\"content\\\": \\\"可以每周进行2-3次的力量训练，如哑铃举重或使用自体重训练，提高肌肉力量和新陈代谢率。\\\",\\n      \\\"priority\\\": \\\"LOW\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\",\\\"logs\\\"],\\n      \\\"confidence\\\": 0.6\\n    },\\n    {\\n      \\\"title\\\": \\\"尝试间歇性高强度训练（HIIT）\\\",\\n      \\\"content\\\": \\\"可以每周安排1-2次，每次20分钟的HIIT训练，提高心率和脂肪燃烧效率。强度要根据个人体能逐渐增加。\\\",\\n      \\\"priority\\\": \\\"LOW\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\",\\\"logs\\\"],\\n      \\\"confidence\\\": 0.6\\n    }\\n  ],\\n  \\\"evidence\\\": [\\\"跑步60分钟较弱\\\", \\\"需要改善中等强度的有氧运动\\\"],\\n  \\\"safety\\\": { \\\"refuse\\\": false, \\\"message\\\": \\\"\\\" }\\n}\", \"safety\": {\"refuse\": false, \"message\": \"\"}, \"summary\": \"根据徐志永的健康日志，他需要增加适量的有氧运动和力量训练来改善当前状况。\"}', b'0', '2025-12-25 19:23:26');
INSERT INTO `recommendations` VALUES (118, 6, '2025-12-27', 'DIET', '[{\"title\": \"增加蔬菜和水果摄入量\", \"content\": \"在每餐中加入更多绿叶蔬菜（如菠菜、油麦菜）和新鲜水果（如苹果、香蕉），以提高纤维素摄入，帮助控制血糖，并提供必要的维生素。\", \"priority\": \"HIGH\", \"confidence\": 0.65, \"sourceTags\": [\"ai\", \"logs\"]}, {\"title\": \"减少油脂高盐食品\", \"content\": \"避免食用油炸或高盐分的食品（如煎饺、辣条），选择蒸煮方式烹饪，以降低热量和钠摄入。\", \"priority\": \"HIGH\", \"confidence\": 0.65, \"sourceTags\": [\"ai\", \"logs\"]}, {\"title\": \"合理搭配主食\", \"content\": \"控制米饭等碳水化合物的分量（如1碗/餐），增加全谷物的比例，减少精白米面的摄入。\", \"priority\": \"HIGH\", \"confidence\": 0.65, \"sourceTags\": [\"ai\", \"logs\"]}, {\"title\": \"均衡蛋白质来源\", \"content\": \"确保每餐都有豆制品或豆腐作为优质蛋白补充（如100g/餐），减少肉类分量，以减低饱和脂肪摄入。\", \"priority\": \"HIGH\", \"confidence\": 0.65, \"sourceTags\": [\"ai\", \"logs\"]}]', '[\"2025-12-27 饮食:重盐,建议控盐\", \"2025-12-27 饮食:高热量食物摄入过多，推荐增加蔬菜水果比例\"]', 'v2', 'READY', 'qwen2.5:7b', 'v1', '{\"raw\": \"{\\n  \\\"title\\\": \\\"徐志永饮食建议\\\",\\n  \\\"summary\\\": \\\"根据近期日志，建议减少高热量食物摄入，增加蔬菜水果比例，保持血糖稳定。\\\",\\n  \\\"items\\\": [\\n    {\\n      \\\"title\\\": \\\"增加蔬菜和水果摄入量\\\",\\n      \\\"content\\\": \\\"在每餐中加入更多绿叶蔬菜（如菠菜、油麦菜）和新鲜水果（如苹果、香蕉），以提高纤维素摄入，帮助控制血糖，并提供必要的维生素。\\\",\\n      \\\"priority\\\": \\\"HIGH\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\",\\\"logs\\\"],\\n      \\\"confidence\\\": 0.65\\n    },\\n    {\\n      \\\"title\\\": \\\"减少油脂高盐食品\\\",\\n      \\\"content\\\": \\\"避免食用油炸或高盐分的食品（如煎饺、辣条），选择蒸煮方式烹饪，以降低热量和钠摄入。\\\",\\n      \\\"priority\\\": \\\"HIGH\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\",\\\"logs\\\"],\\n      \\\"confidence\\\": 0.65\\n    },\\n    {\\n      \\\"title\\\": \\\"合理搭配主食\\\",\\n      \\\"content\\\": \\\"控制米饭等碳水化合物的分量（如1碗/餐），增加全谷物的比例，减少精白米面的摄入。\\\",\\n      \\\"priority\\\": \\\"HIGH\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\",\\\"logs\\\"],\\n      \\\"confidence\\\": 0.65\\n    },\\n    {\\n      \\\"title\\\": \\\"均衡蛋白质来源\\\",\\n      \\\"content\\\": \\\"确保每餐都有豆制品或豆腐作为优质蛋白补充（如100g/餐），减少肉类分量，以减低饱和脂肪摄入。\\\",\\n      \\\"priority\\\": \\\"HIGH\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\",\\\"logs\\\"],\\n      \\\"confidence\\\": 0.65\\n    }\\n  ],\\n  \\\"evidence\\\": [\\n    \\\"2025-12-27 饮食:重盐,建议控盐\\\",\\n    \\\"2025-12-27 饮食:高热量食物摄入过多，推荐增加蔬菜水果比例\\\"\\n  ],\\n  \\\"safety\\\": { \\\"refuse\\\": false, \\\"message\\\": \\\"\\\" }\\n}\", \"safety\": {\"refuse\": false, \"message\": \"\"}, \"summary\": \"根据近期日志，建议减少高热量食物摄入，增加蔬菜水果比例，保持血糖稳定。\"}', b'0', '2025-12-27 21:14:06');
INSERT INTO `recommendations` VALUES (119, 6, '2025-12-27', 'REST', '[{\"title\": \"增加早餐摄入量\", \"content\": \"2025-12-27 的记录显示早餐较为简单。建议增加一份燕麦或全谷物面包，约100克/份，并搭配一杯低糖牛奶或豆浆以补充营养和控制血糖。\", \"priority\": \"HIGH\", \"confidence\": 0.65, \"sourceTags\": [\"ai\", \"logs\"]}, {\"title\": \"减少高盐食物摄入\", \"content\": \"2025-12-25 日志中的炒河粉和辣条含有较高钠。应选择低盐或无添加盐的食物，每日食用分量不超过1份（约6克）。\", \"priority\": \"HIGH\", \"confidence\": 0.65, \"sourceTags\": [\"ai\", \"logs\"]}, {\"title\": \"改善睡眠环境与习惯\", \"content\": \"2025-12-27 和 2025-12-25 的睡眠时间不规律且质量差。建议每天晚上保持在23点前入睡，减少使用电子设备的时间，确保卧室安静、舒适。\", \"priority\": \"MEDIUM\", \"confidence\": 0.65, \"sourceTags\": [\"ai\", \"logs\"]}, {\"title\": \"缓解压力并提高睡眠质量\", \"content\": \"2025-12-27 和 2025-12-25 的日志显示压力大且睡眠差。建议每天进行深呼吸或冥想练习，减少工作带来的焦虑。\", \"priority\": \"MEDIUM\", \"confidence\": 0.65, \"sourceTags\": [\"ai\", \"logs\"]}]', '[\"2025-12-27 饮食:重盐,建议控盐\", \"2025-12-27 睡眠差,需改善睡眠习惯\"]', 'v2', 'READY', 'qwen2.5:7b', 'v1', '{\"raw\": \"{\\n  \\\"title\\\": \\\"作息调整建议\\\",\\n  \\\"summary\\\": \\\"基于近期日志，徐志永需优化睡眠质量和饮食习惯以减轻压力和控制血糖。\\\",\\n  \\\"items\\\": [\\n    {\\n      \\\"title\\\": \\\"增加早餐摄入量\\\",\\n      \\\"content\\\": \\\"2025-12-27 的记录显示早餐较为简单。建议增加一份燕麦或全谷物面包，约100克/份，并搭配一杯低糖牛奶或豆浆以补充营养和控制血糖。\\\",\\n      \\\"priority\\\": \\\"HIGH\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\", \\\"logs\\\"],\\n      \\\"confidence\\\": 0.65\\n    },\\n    {\\n      \\\"title\\\": \\\"减少高盐食物摄入\\\",\\n      \\\"content\\\": \\\"2025-12-25 日志中的炒河粉和辣条含有较高钠。应选择低盐或无添加盐的食物，每日食用分量不超过1份（约6克）。\\\",\\n      \\\"priority\\\": \\\"HIGH\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\", \\\"logs\\\"],\\n      \\\"confidence\\\": 0.65\\n    },\\n    {\\n      \\\"title\\\": \\\"改善睡眠环境与习惯\\\",\\n      \\\"content\\\": \\\"2025-12-27 和 2025-12-25 的睡眠时间不规律且质量差。建议每天晚上保持在23点前入睡，减少使用电子设备的时间，确保卧室安静、舒适。\\\",\\n      \\\"priority\\\": \\\"MEDIUM\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\", \\\"logs\\\"],\\n      \\\"confidence\\\": 0.65\\n    },\\n    {\\n      \\\"title\\\": \\\"缓解压力并提高睡眠质量\\\",\\n      \\\"content\\\": \\\"2025-12-27 和 2025-12-25 的日志显示压力大且睡眠差。建议每天进行深呼吸或冥想练习，减少工作带来的焦虑。\\\",\\n      \\\"priority\\\": \\\"MEDIUM\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\", \\\"logs\\\"],\\n      \\\"confidence\\\": 0.65\\n    }\\n  ],\\n  \\\"evidence\\\": [\\n    \\\"2025-12-27 饮食:重盐,建议控盐\\\",\\n    \\\"2025-12-27 睡眠差,需改善睡眠习惯\\\"\\n  ],\\n  \\\"safety\\\": { \\\"refuse\\\": false, \\\"message\\\": \\\"\\\" }\\n}\", \"safety\": {\"refuse\": false, \"message\": \"\"}, \"summary\": \"基于近期日志，徐志永需优化睡眠质量和饮食习惯以减轻压力和控制血糖。\"}', b'0', '2025-12-27 21:16:16');
INSERT INTO `recommendations` VALUES (120, 6, '2025-12-27', 'SPORT', '[{\"title\": \"增加跑步强度\", \"content\": \"当前记录显示跑步强度较弱且跑步距离不够。增加跑步强度至中等强度，并适当延长跑步时间。例如，每天可以将跑步时长从30分钟增加到45-60分钟。\", \"priority\": \"HIGH\", \"confidence\": 0.7, \"sourceTags\": [\"ai\", \"logs\"]}, {\"title\": \"调整跑步频率\", \"content\": \"虽然用户目前每天都在跑步，但可以考虑将跑步频率从天天跑调整为隔天跑。这样既可保持运动习惯，又能避免过度疲劳。\", \"priority\": \"MEDIUM\", \"confidence\": 0.65, \"sourceTags\": [\"ai\", \"logs\"]}]', '[\"2025-12-27 运动:跑步 30分钟 距离 5 公里 轻, 2025-12-25 运动:跑步 60分钟 距离 5 公里 弱\", \"慢性疾病:糖尿病\"]', 'v2', 'READY', 'qwen2.5:7b', 'v1', '{\"raw\": \"{\\n  \\\"title\\\": \\\"运动建议\\\",\\n  \\\"summary\\\": \\\"根据用户日志，优化跑步强度和时间以更好地控制血糖、减重并缓解压力。\\\",\\n  \\\"items\\\": [\\n    {\\n      \\\"title\\\": \\\"增加跑步强度\\\",\\n      \\\"content\\\": \\\"当前记录显示跑步强度较弱且跑步距离不够。增加跑步强度至中等强度，并适当延长跑步时间。例如，每天可以将跑步时长从30分钟增加到45-60分钟。\\\",\\n      \\\"priority\\\": \\\"HIGH\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\",\\\"logs\\\"],\\n      \\\"confidence\\\": 0.7\\n    },\\n    {\\n      \\\"title\\\": \\\"调整跑步频率\\\",\\n      \\\"content\\\": \\\"虽然用户目前每天都在跑步，但可以考虑将跑步频率从天天跑调整为隔天跑。这样既可保持运动习惯，又能避免过度疲劳。\\\",\\n      \\\"priority\\\": \\\"MEDIUM\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\",\\\"logs\\\"],\\n      \\\"confidence\\\": 0.65\\n    }\\n  ],\\n  \\\"evidence\\\": [\\n    \\\"2025-12-27 运动:跑步 30分钟 距离 5 公里 轻, 2025-12-25 运动:跑步 60分钟 距离 5 公里 弱\\\",\\n    \\\"慢性疾病:糖尿病\\\"\\n  ],\\n  \\\"safety\\\": { \\\"refuse\\\": false, \\\"message\\\": \\\"\\\" }\\n}\", \"safety\": {\"refuse\": false, \"message\": \"\"}, \"summary\": \"根据用户日志，优化跑步强度和时间以更好地控制血糖、减重并缓解压力。\"}', b'0', '2025-12-27 21:17:58');
INSERT INTO `recommendations` VALUES (121, 6, '2025-12-27', 'EMOTION', '[{\"title\": \"保持良好睡眠\", \"content\": \"保证充足且高质量的睡眠对缓解压力至关重要。尽量避免过度劳累，确保每晚8小时的有效睡眠时间。\", \"priority\": \"HIGH\", \"confidence\": 0.65, \"sourceTags\": [\"ai\", \"logs\"]}, {\"title\": \"增加蛋白质摄入\", \"content\": \"最近饮食中蛋白质摄入不足。建议每天增加1-2个鸡蛋，每餐适量食用，有助于提升饱腹感和情绪稳定。\", \"priority\": \"HIGH\", \"confidence\": 0.65, \"sourceTags\": [\"ai\", \"logs\"]}, {\"title\": \"均衡饮食摄入\", \"content\": \"近期饮食以素食为主但缺乏多样性和营养平衡。建议每餐搭配绿叶蔬菜、全谷物和豆类，增加膳食纤维和微量元素的摄入。\", \"priority\": \"HIGH\", \"confidence\": 0.65, \"sourceTags\": [\"ai\", \"logs\"]}]', '[\"2025-12-27 饮食:重盐,建议控盐\", \"2025-12-25 情绪很差，压力强度5\"]', 'v2', 'READY', 'qwen2.5:7b', 'v1', '{\"raw\": \"{\\n  \\\"title\\\": \\\"情绪管理与健康饮食建议\\\",\\n  \\\"summary\\\": \\\"基于用户最近情绪状况及饮食记录，提出改善压力、均衡饮食和增加蛋白质摄入的具体措施。\\\",\\n  \\\"items\\\": [\\n    {\\n      \\\"title\\\": \\\"保持良好睡眠\\\",\\n      \\\"content\\\": \\\"保证充足且高质量的睡眠对缓解压力至关重要。尽量避免过度劳累，确保每晚8小时的有效睡眠时间。\\\",\\n      \\\"priority\\\": \\\"HIGH\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\", \\\"logs\\\"],\\n      \\\"confidence\\\": 0.65\\n    },\\n    {\\n      \\\"title\\\": \\\"增加蛋白质摄入\\\",\\n      \\\"content\\\": \\\"最近饮食中蛋白质摄入不足。建议每天增加1-2个鸡蛋，每餐适量食用，有助于提升饱腹感和情绪稳定。\\\",\\n      \\\"priority\\\": \\\"MEDIUM\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\", \\\"logs\\\"],\\n      \\\"confidence\\\": 0.65\\n    },\\n    {\\n      \\\"title\\\": \\\"均衡饮食摄入\\\",\\n      \\\"content\\\": \\\"近期饮食以素食为主但缺乏多样性和营养平衡。建议每餐搭配绿叶蔬菜、全谷物和豆类，增加膳食纤维和微量元素的摄入。\\\",\\n      \\\"priority\\\": \\\"MEDIUM\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\", \\\"logs\\\"],\\n      \\\"confidence\\\": 0.65\\n    }\\n  ],\\n  \\\"evidence\\\": [\\n    \\\"2025-12-27 饮食:重盐,建议控盐\\\",\\n    \\\"2025-12-25 情绪很差，压力强度5\\\"\\n  ],\\n  \\\"safety\\\": { \\\"refuse\\\": false, \\\"message\\\": \\\"\\\" }\\n}\", \"safety\": {\"refuse\": false, \"message\": \"\"}, \"summary\": \"基于用户最近情绪状况及饮食记录，提出改善压力、均衡饮食和增加蛋白质摄入的具体措施。\"}', b'0', '2025-12-27 21:18:40');
INSERT INTO `recommendations` VALUES (122, 4, '2025-12-28', 'DIET', '[{\"title\": \"增加主食分量\", \"content\": \"由于用户BMI较低且有增重需求，建议每日增加一碗（约150克）面条或米饭的摄入量。这些高热量、易消化的食物有助于提高总能量摄入。\", \"priority\": \"HIGH\", \"confidence\": 0.65, \"sourceTags\": [\"ai\", \"logs\"]}, {\"title\": \"增加高蛋白食物\", \"content\": \"为增强体质，建议每日在主餐中加入一份（约50克）鸡胸肉或鱼肉。这些富含优质蛋白质的食物有助于肌肉修复和增长。\", \"priority\": \"HIGH\", \"confidence\": 0.65, \"sourceTags\": [\"ai\", \"logs\"]}, {\"title\": \"晚餐避免辛辣刺激性食物\", \"content\": \"鉴于用户有改善睡眠的目标，建议减少辛辣、油腻等可能影响睡眠的食物摄入。例如，在晚餐时可以改吃一份（约100克）蔬菜沙拉代替重口味的辣酱炒菜。\", \"priority\": \"MEDIUM\", \"confidence\": 0.65, \"sourceTags\": [\"ai\", \"logs\"]}]', '[\"2025-12-28 饮食:面条1碗；总热量：100千卡\"]', 'v2', 'READY', 'qwen2.5:7b', 'v1', '{\"raw\": \"{\\n  \\\"title\\\": \\\"增重与改善睡眠的饮食建议\\\",\\n  \\\"summary\\\": \\\"根据当前体质和目标，建议增加热量摄入并选择高蛋白质食物以增强体质；同时注意避免辛辣刺激性食物帮助改善睡眠。\\\",\\n  \\\"items\\\": [\\n    {\\n      \\\"title\\\": \\\"增加主食分量\\\",\\n      \\\"content\\\": \\\"由于用户BMI较低且有增重需求，建议每日增加一碗（约150克）面条或米饭的摄入量。这些高热量、易消化的食物有助于提高总能量摄入。\\\",\\n      \\\"priority\\\": \\\"HIGH\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\", \\\"logs\\\"],\\n      \\\"confidence\\\": 0.65\\n    },\\n    {\\n      \\\"title\\\": \\\"增加高蛋白食物\\\",\\n      \\\"content\\\": \\\"为增强体质，建议每日在主餐中加入一份（约50克）鸡胸肉或鱼肉。这些富含优质蛋白质的食物有助于肌肉修复和增长。\\\",\\n      \\\"priority\\\": \\\"HIGH\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\", \\\"logs\\\"],\\n      \\\"confidence\\\": 0.65\\n    },\\n    {\\n      \\\"title\\\": \\\"晚餐避免辛辣刺激性食物\\\",\\n      \\\"content\\\": \\\"鉴于用户有改善睡眠的目标，建议减少辛辣、油腻等可能影响睡眠的食物摄入。例如，在晚餐时可以改吃一份（约100克）蔬菜沙拉代替重口味的辣酱炒菜。\\\",\\n      \\\"priority\\\": \\\"MEDIUM\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\", \\\"logs\\\"],\\n      \\\"confidence\\\": 0.65\\n    }\\n  ],\\n  \\\"evidence\\\": [\\n    \\\"2025-12-28 饮食:面条1碗；总热量：100千卡\\\"\\n  ],\\n  \\\"safety\\\": {\\n    \\\"refuse\\\": false,\\n    \\\"message\\\": \\\"\\\"\\n  }\\n}\", \"safety\": {\"refuse\": false, \"message\": \"\"}, \"summary\": \"根据当前体质和目标，建议增加热量摄入并选择高蛋白质食物以增强体质；同时注意避免辛辣刺激性食物帮助改善睡眠。\"}', b'0', '2025-12-28 11:32:01');
INSERT INTO `recommendations` VALUES (123, 4, '2026-01-15', 'DIET', '[{\"title\": \"增加蛋白质摄入\", \"content\": \"每日额外食用50克瘦肉或豆制品，有助于提供必要的氨基酸，增强体质并支持增重目标。同时保持原有清淡饮食习惯。\", \"priority\": \"HIGH\", \"confidence\": 0.65, \"sourceTags\": [\"ai\", \"logs\"]}, {\"title\": \"晚餐适量加餐\", \"content\": \"每日晚餐后可以额外食用一份（约150克）杂粮粥或小米粥，以补充能量和膳食纤维，帮助改善睡眠质量。\", \"priority\": \"MEDIUM\", \"confidence\": 0.65, \"sourceTags\": [\"ai\", \"logs\"]}, {\"title\": \"早餐增加蛋类摄入\", \"content\": \"每日早餐时可额外食用1个鸡蛋或等量的蛋白粉，补充优质蛋白质，有助于增重和提高能量水平。\", \"priority\": \"LOW\", \"confidence\": 0.65, \"sourceTags\": [\"ai\", \"logs\"]}]', '[]', 'v2', 'READY', 'qwen2.5:7b', 'v1', '{\"raw\": \"{\\n  \\\"title\\\": \\\"清淡饮食增强体质\\\",\\n  \\\"summary\\\": \\\"根据用户当前体质及生活习惯，建议继续保持清淡饮食习惯，并适当增加蛋白质摄入以促进体重增长。\\\",\\n  \\\"items\\\": [\\n    {\\n      \\\"title\\\": \\\"增加蛋白质摄入\\\",\\n      \\\"content\\\": \\\"每日额外食用50克瘦肉或豆制品，有助于提供必要的氨基酸，增强体质并支持增重目标。同时保持原有清淡饮食习惯。\\\",\\n      \\\"priority\\\": \\\"HIGH\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\",\\\"logs\\\"],\\n      \\\"confidence\\\": 0.65\\n    },\\n    {\\n      \\\"title\\\": \\\"晚餐适量加餐\\\",\\n      \\\"content\\\": \\\"每日晚餐后可以额外食用一份（约150克）杂粮粥或小米粥，以补充能量和膳食纤维，帮助改善睡眠质量。\\\",\\n      \\\"priority\\\": \\\"MEDIUM\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\",\\\"logs\\\"],\\n      \\\"confidence\\\": 0.65\\n    },\\n    {\\n      \\\"title\\\": \\\"早餐增加蛋类摄入\\\",\\n      \\\"content\\\": \\\"每日早餐时可额外食用1个鸡蛋或等量的蛋白粉，补充优质蛋白质，有助于增重和提高能量水平。\\\",\\n      \\\"priority\\\": \\\"LOW\\\",\\n      \\\"sourceTags\\\": [\\\"ai\\\",\\\"logs\\\"],\\n      \\\"confidence\\\": 0.65\\n    }\\n  ],\\n  \\\"evidence\\\": [],\\n  \\\"safety\\\": { \\\"refuse\\\": false, \\\"message\\\": \\\"\\\" }\\n}\", \"safety\": {\"refuse\": false, \"message\": \"\"}, \"summary\": \"根据用户当前体质及生活习惯，建议继续保持清淡饮食习惯，并适当增加蛋白质摄入以促进体重增长。\"}', b'0', '2026-01-15 13:13:45');

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '个性化建议V2' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of recommendations_v2
-- ----------------------------

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '建议规则库' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of rules
-- ----------------------------

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '规则库V2' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of rules_v2
-- ----------------------------

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '建议反馈' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of suggestion_feedback
-- ----------------------------

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of system_logs
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统配置历史记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_setting_histories
-- ----------------------------
INSERT INTO `system_setting_histories` VALUES (1, 'security.login.max_retries', '\"5\"', 'v1768396907540', NULL, '2026-01-14 21:21:48');
INSERT INTO `system_setting_histories` VALUES (2, 'security.login.lock_minutes', '\"30\"', 'v1768396907540', NULL, '2026-01-14 21:21:48');
INSERT INTO `system_setting_histories` VALUES (3, 'security.password.min_length', '\"8\"', 'v1768396907540', NULL, '2026-01-14 21:21:48');
INSERT INTO `system_setting_histories` VALUES (4, 'security.password.expiry_enabled', '\"false\"', 'v1768396907540', NULL, '2026-01-14 21:21:48');

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of system_settings
-- ----------------------------
INSERT INTO `system_settings` VALUES (1, 'security.login.max_retries', '\"5\"', '2026-01-14 21:21:48');
INSERT INTO `system_settings` VALUES (2, 'security.login.lock_minutes', '\"30\"', '2026-01-14 21:21:48');
INSERT INTO `system_settings` VALUES (3, 'security.password.min_length', '\"8\"', '2026-01-14 21:21:48');
INSERT INTO `system_settings` VALUES (4, 'security.password.expiry_enabled', '\"false\"', '2026-01-14 21:21:48');

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
) ENGINE = InnoDB AUTO_INCREMENT = 855 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '中医养生知识库' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tcm_knowledge_base
-- ----------------------------
INSERT INTO `tcm_knowledge_base` VALUES (847, 'DIET', 'QI_DEFICIENCY', '黄芪炖鸡', '取黄芪30克，母鸡1只。将母鸡洗净，黄芪塞入鸡腹中，加水适量，炖至鸡肉烂熟，加盐调味。具有补气固表功效。', NULL, NULL, 'MEDIUM', NULL, NULL, 'HIGH', '2026-01-20 13:12:18', '2026-01-20 13:12:18');
INSERT INTO `tcm_knowledge_base` VALUES (848, 'DIET', 'QI_DEFICIENCY', '山药粥', '山药30克，大米100克。将山药去皮切块，与大米同煮成粥。具有益气养阴、补脾肺肾的功效。', NULL, NULL, 'EASY', NULL, NULL, 'HIGH', '2026-01-20 13:12:18', '2026-01-20 13:12:18');
INSERT INTO `tcm_knowledge_base` VALUES (849, 'EXERCISE', 'QI_DEFICIENCY', '八段锦', '练习八段锦，重点练习“调理脾胃须单举”一式。每日1-2次，每次15-20分钟。', NULL, NULL, 'EASY', NULL, NULL, 'HIGH', '2026-01-20 13:12:18', '2026-01-20 13:12:18');
INSERT INTO `tcm_knowledge_base` VALUES (850, 'EXERCISE', 'QI_DEFICIENCY', '太极拳', '练习太极拳，动作柔和缓慢，有助于气血流通。建议每日清晨练习30分钟。', NULL, NULL, 'MEDIUM', NULL, NULL, 'HIGH', '2026-01-20 13:12:18', '2026-01-20 13:12:18');
INSERT INTO `tcm_knowledge_base` VALUES (851, 'ACUPUNCTURE', 'QI_DEFICIENCY', '足三里按摩', '每日按揉足三里穴（膝盖外侧凹陷下3寸）10-15分钟，有酸胀感为宜。可补中益气。', NULL, NULL, 'EASY', NULL, NULL, 'MEDIUM', '2026-01-20 13:12:18', '2026-01-20 13:12:18');
INSERT INTO `tcm_knowledge_base` VALUES (852, 'ACUPUNCTURE', 'QI_DEFICIENCY', '关元穴艾灸', '使用艾条温和灸关元穴（脐下3寸），每日1次，每次15分钟。可培元固本。', NULL, NULL, 'MEDIUM', NULL, NULL, 'MEDIUM', '2026-01-20 13:12:18', '2026-01-20 13:12:18');
INSERT INTO `tcm_knowledge_base` VALUES (853, 'TEA', 'QI_DEFICIENCY', '黄芪红枣茶', '黄芪10克，红枣5枚。开水冲泡代茶饮。具有补气生血功效。', NULL, NULL, 'EASY', NULL, NULL, 'HIGH', '2026-01-20 13:12:18', '2026-01-20 13:12:18');
INSERT INTO `tcm_knowledge_base` VALUES (854, 'LIFESTYLE', 'QI_DEFICIENCY', '生活起居', '注意保暖，避免受风寒。避免过度劳累，保证充足睡眠。', NULL, NULL, 'EASY', NULL, NULL, 'HIGH', '2026-01-20 13:12:18', '2026-01-20 13:12:18');
INSERT INTO `tcm_knowledge_base` VALUES (855, 'EMOTION', 'QI_DEFICIENCY', '情志调节', '保持心情舒畅，避免过度思虑。多听轻柔音乐，放松心情。', NULL, NULL, 'EASY', NULL, NULL, 'HIGH', '2026-01-20 13:12:18', '2026-01-20 13:12:18');

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '个性化中医养生方案' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tcm_personalized_plans
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 361 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户登录日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_login_logs
-- ----------------------------
INSERT INTO `user_login_logs` VALUES (1, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 17:57:16', 'SUCCESS', 'APP', '2025-12-30 17:57:16');
INSERT INTO `user_login_logs` VALUES (2, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 17:57:45', 'SUCCESS', 'APP', '2025-12-30 17:57:45');
INSERT INTO `user_login_logs` VALUES (3, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 17:59:46', 'SUCCESS', 'APP', '2025-12-30 17:59:46');
INSERT INTO `user_login_logs` VALUES (4, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 18:02:41', 'SUCCESS', 'APP', '2025-12-30 18:02:41');
INSERT INTO `user_login_logs` VALUES (5, 12, '13800138011', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 18:03:14', 'SUCCESS', 'APP', '2025-12-30 18:03:14');
INSERT INTO `user_login_logs` VALUES (6, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 18:03:20', 'SUCCESS', 'APP', '2025-12-30 18:03:20');
INSERT INTO `user_login_logs` VALUES (7, 12, '13800138011', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 18:03:27', 'SUCCESS', 'APP', '2025-12-30 18:03:27');
INSERT INTO `user_login_logs` VALUES (8, 12, '13800138011', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 18:06:16', 'SUCCESS', 'APP', '2025-12-30 18:06:16');
INSERT INTO `user_login_logs` VALUES (9, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 19:02:39', 'SUCCESS', 'APP', '2025-12-30 19:02:39');
INSERT INTO `user_login_logs` VALUES (10, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 19:02:45', 'SUCCESS', 'APP', '2025-12-30 19:02:45');
INSERT INTO `user_login_logs` VALUES (11, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 19:02:58', 'SUCCESS', 'APP', '2025-12-30 19:02:58');
INSERT INTO `user_login_logs` VALUES (12, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 19:03:05', 'SUCCESS', 'APP', '2025-12-30 19:03:05');
INSERT INTO `user_login_logs` VALUES (13, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 19:05:21', 'SUCCESS', 'APP', '2025-12-30 19:05:21');
INSERT INTO `user_login_logs` VALUES (14, 12, '13800138011', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 19:05:29', 'SUCCESS', 'APP', '2025-12-30 19:05:29');
INSERT INTO `user_login_logs` VALUES (15, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 19:08:07', 'SUCCESS', 'APP', '2025-12-30 19:08:07');
INSERT INTO `user_login_logs` VALUES (16, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 19:08:30', 'SUCCESS', 'APP', '2025-12-30 19:08:30');
INSERT INTO `user_login_logs` VALUES (17, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 21:53:31', 'SUCCESS', 'APP', '2025-12-30 21:53:31');
INSERT INTO `user_login_logs` VALUES (18, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 21:56:53', 'SUCCESS', 'APP', '2025-12-30 21:56:53');
INSERT INTO `user_login_logs` VALUES (19, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 22:00:46', 'SUCCESS', 'APP', '2025-12-30 22:00:46');
INSERT INTO `user_login_logs` VALUES (20, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 22:26:02', 'SUCCESS', 'APP', '2025-12-30 22:26:02');
INSERT INTO `user_login_logs` VALUES (21, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 22:38:48', 'SUCCESS', 'APP', '2025-12-30 22:38:49');
INSERT INTO `user_login_logs` VALUES (22, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 23:03:18', 'SUCCESS', 'APP', '2025-12-30 23:03:18');
INSERT INTO `user_login_logs` VALUES (23, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 23:09:00', 'SUCCESS', 'APP', '2025-12-30 23:09:00');
INSERT INTO `user_login_logs` VALUES (24, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 23:49:54', 'SUCCESS', 'APP', '2025-12-30 23:49:54');
INSERT INTO `user_login_logs` VALUES (25, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2025-12-30 23:52:04', 'SUCCESS', 'APP', '2025-12-30 23:52:04');
INSERT INTO `user_login_logs` VALUES (26, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 11:38:43', 'SUCCESS', 'APP', '2026-01-14 11:38:43');
INSERT INTO `user_login_logs` VALUES (27, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 11:38:53', 'SUCCESS', 'APP', '2026-01-14 11:38:53');
INSERT INTO `user_login_logs` VALUES (28, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 11:39:13', 'SUCCESS', 'APP', '2026-01-14 11:39:13');
INSERT INTO `user_login_logs` VALUES (29, 12, '13800138011', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 11:39:17', 'SUCCESS', 'APP', '2026-01-14 11:39:17');
INSERT INTO `user_login_logs` VALUES (30, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 11:39:23', 'SUCCESS', 'APP', '2026-01-14 11:39:23');
INSERT INTO `user_login_logs` VALUES (31, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 13:31:15', 'SUCCESS', 'APP', '2026-01-14 13:31:15');
INSERT INTO `user_login_logs` VALUES (32, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 16:39:07', 'SUCCESS', 'APP', '2026-01-14 16:39:07');
INSERT INTO `user_login_logs` VALUES (33, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 16:44:07', 'SUCCESS', 'APP', '2026-01-14 16:44:07');
INSERT INTO `user_login_logs` VALUES (34, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 18:50:13', 'SUCCESS', 'APP', '2026-01-14 18:50:13');
INSERT INTO `user_login_logs` VALUES (35, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 19:06:01', 'SUCCESS', 'APP', '2026-01-14 19:06:01');
INSERT INTO `user_login_logs` VALUES (36, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 19:06:11', 'SUCCESS', 'APP', '2026-01-14 19:06:11');
INSERT INTO `user_login_logs` VALUES (37, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 19:07:12', 'SUCCESS', 'APP', '2026-01-14 19:07:12');
INSERT INTO `user_login_logs` VALUES (38, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 19:07:26', 'SUCCESS', 'APP', '2026-01-14 19:07:26');
INSERT INTO `user_login_logs` VALUES (39, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 19:07:41', 'SUCCESS', 'APP', '2026-01-14 19:07:41');
INSERT INTO `user_login_logs` VALUES (40, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 19:14:22', 'SUCCESS', 'APP', '2026-01-14 19:14:22');
INSERT INTO `user_login_logs` VALUES (41, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 19:14:29', 'SUCCESS', 'APP', '2026-01-14 19:14:29');
INSERT INTO `user_login_logs` VALUES (42, 6, '15163457180', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 19:15:08', 'SUCCESS', 'APP', '2026-01-14 19:15:08');
INSERT INTO `user_login_logs` VALUES (43, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 19:15:24', 'SUCCESS', 'APP', '2026-01-14 19:15:24');
INSERT INTO `user_login_logs` VALUES (44, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 19:15:30', 'SUCCESS', 'APP', '2026-01-14 19:15:30');
INSERT INTO `user_login_logs` VALUES (45, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 19:59:43', 'SUCCESS', 'APP', '2026-01-14 19:59:43');
INSERT INTO `user_login_logs` VALUES (46, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 20:00:54', 'SUCCESS', 'APP', '2026-01-14 20:00:54');
INSERT INTO `user_login_logs` VALUES (47, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 20:17:22', 'SUCCESS', 'APP', '2026-01-14 20:17:22');
INSERT INTO `user_login_logs` VALUES (48, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 20:19:01', 'SUCCESS', 'APP', '2026-01-14 20:19:01');
INSERT INTO `user_login_logs` VALUES (49, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 20:20:43', 'SUCCESS', 'APP', '2026-01-14 20:20:43');
INSERT INTO `user_login_logs` VALUES (50, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 20:22:40', 'SUCCESS', 'APP', '2026-01-14 20:22:40');
INSERT INTO `user_login_logs` VALUES (51, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 20:51:32', 'SUCCESS', 'APP', '2026-01-14 20:51:32');
INSERT INTO `user_login_logs` VALUES (52, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 21:07:44', 'SUCCESS', 'APP', '2026-01-14 21:07:44');
INSERT INTO `user_login_logs` VALUES (53, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 21:21:35', 'SUCCESS', 'APP', '2026-01-14 21:21:35');
INSERT INTO `user_login_logs` VALUES (54, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-14 21:24:30', 'SUCCESS', 'APP', '2026-01-14 21:24:30');
INSERT INTO `user_login_logs` VALUES (55, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 08:47:20', 'SUCCESS', 'APP', '2026-01-15 08:47:20');
INSERT INTO `user_login_logs` VALUES (56, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 08:49:04', 'SUCCESS', 'APP', '2026-01-15 08:49:04');
INSERT INTO `user_login_logs` VALUES (57, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 08:57:35', 'SUCCESS', 'APP', '2026-01-15 08:57:35');
INSERT INTO `user_login_logs` VALUES (58, 12, '13800138011', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 08:57:39', 'SUCCESS', 'APP', '2026-01-15 08:57:39');
INSERT INTO `user_login_logs` VALUES (59, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 08:57:45', 'SUCCESS', 'APP', '2026-01-15 08:57:45');
INSERT INTO `user_login_logs` VALUES (60, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 08:57:49', 'SUCCESS', 'APP', '2026-01-15 08:57:49');
INSERT INTO `user_login_logs` VALUES (61, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 08:59:14', 'SUCCESS', 'APP', '2026-01-15 08:59:14');
INSERT INTO `user_login_logs` VALUES (62, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 08:59:31', 'SUCCESS', 'APP', '2026-01-15 08:59:31');
INSERT INTO `user_login_logs` VALUES (63, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 09:14:55', 'SUCCESS', 'APP', '2026-01-15 09:14:55');
INSERT INTO `user_login_logs` VALUES (64, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 09:19:26', 'SUCCESS', 'APP', '2026-01-15 09:19:26');
INSERT INTO `user_login_logs` VALUES (65, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 11:21:40', 'SUCCESS', 'APP', '2026-01-15 11:21:40');
INSERT INTO `user_login_logs` VALUES (66, 6, '15163457180', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 11:22:06', 'SUCCESS', 'APP', '2026-01-15 11:22:06');
INSERT INTO `user_login_logs` VALUES (67, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 11:22:17', 'SUCCESS', 'APP', '2026-01-15 11:22:17');
INSERT INTO `user_login_logs` VALUES (68, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 13:27:17', 'SUCCESS', 'APP', '2026-01-15 13:27:17');
INSERT INTO `user_login_logs` VALUES (69, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 13:29:48', 'SUCCESS', 'APP', '2026-01-15 13:29:48');
INSERT INTO `user_login_logs` VALUES (70, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 13:30:18', 'SUCCESS', 'APP', '2026-01-15 13:30:18');
INSERT INTO `user_login_logs` VALUES (71, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 13:30:23', 'SUCCESS', 'APP', '2026-01-15 13:30:23');
INSERT INTO `user_login_logs` VALUES (72, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 13:59:58', 'SUCCESS', 'APP', '2026-01-15 13:59:58');
INSERT INTO `user_login_logs` VALUES (73, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 14:00:03', 'SUCCESS', 'APP', '2026-01-15 14:00:03');
INSERT INTO `user_login_logs` VALUES (74, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 14:03:47', 'SUCCESS', 'APP', '2026-01-15 14:03:47');
INSERT INTO `user_login_logs` VALUES (75, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 14:35:59', 'SUCCESS', 'APP', '2026-01-15 14:35:59');
INSERT INTO `user_login_logs` VALUES (76, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 14:53:03', 'SUCCESS', 'APP', '2026-01-15 14:53:03');
INSERT INTO `user_login_logs` VALUES (77, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 15:04:42', 'SUCCESS', 'APP', '2026-01-15 15:04:42');
INSERT INTO `user_login_logs` VALUES (78, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 18:08:05', 'SUCCESS', 'APP', '2026-01-15 18:08:05');
INSERT INTO `user_login_logs` VALUES (79, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 18:13:08', 'SUCCESS', 'APP', '2026-01-15 18:13:08');
INSERT INTO `user_login_logs` VALUES (80, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 18:17:53', 'SUCCESS', 'APP', '2026-01-15 18:17:53');
INSERT INTO `user_login_logs` VALUES (81, 18, '13675488008', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 18:24:02', 'SUCCESS', 'APP', '2026-01-15 18:24:02');
INSERT INTO `user_login_logs` VALUES (82, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 18:25:59', 'SUCCESS', 'APP', '2026-01-15 18:25:59');
INSERT INTO `user_login_logs` VALUES (83, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 18:26:03', 'SUCCESS', 'APP', '2026-01-15 18:26:03');
INSERT INTO `user_login_logs` VALUES (84, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 18:26:09', 'SUCCESS', 'APP', '2026-01-15 18:26:09');
INSERT INTO `user_login_logs` VALUES (85, 18, '13675488008', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 18:26:43', 'SUCCESS', 'APP', '2026-01-15 18:26:43');
INSERT INTO `user_login_logs` VALUES (86, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 18:33:10', 'SUCCESS', 'APP', '2026-01-15 18:33:10');
INSERT INTO `user_login_logs` VALUES (87, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 18:33:39', 'SUCCESS', 'APP', '2026-01-15 18:33:39');
INSERT INTO `user_login_logs` VALUES (88, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 18:40:03', 'SUCCESS', 'APP', '2026-01-15 18:40:03');
INSERT INTO `user_login_logs` VALUES (89, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 18:40:40', 'SUCCESS', 'APP', '2026-01-15 18:40:40');
INSERT INTO `user_login_logs` VALUES (90, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 18:41:05', 'SUCCESS', 'APP', '2026-01-15 18:41:05');
INSERT INTO `user_login_logs` VALUES (91, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 18:44:23', 'SUCCESS', 'APP', '2026-01-15 18:44:23');
INSERT INTO `user_login_logs` VALUES (92, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 18:44:56', 'SUCCESS', 'APP', '2026-01-15 18:44:56');
INSERT INTO `user_login_logs` VALUES (93, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 19:04:24', 'SUCCESS', 'APP', '2026-01-15 19:04:24');
INSERT INTO `user_login_logs` VALUES (94, 20, '13675488008', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 19:04:38', 'SUCCESS', 'APP', '2026-01-15 19:04:38');
INSERT INTO `user_login_logs` VALUES (95, 20, '13675488008', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 19:19:36', 'SUCCESS', 'APP', '2026-01-15 19:19:36');
INSERT INTO `user_login_logs` VALUES (96, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 19:19:41', 'SUCCESS', 'APP', '2026-01-15 19:19:41');
INSERT INTO `user_login_logs` VALUES (97, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 19:21:18', 'SUCCESS', 'APP', '2026-01-15 19:21:18');
INSERT INTO `user_login_logs` VALUES (98, 21, '13675481145', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 19:21:29', 'SUCCESS', 'APP', '2026-01-15 19:21:29');
INSERT INTO `user_login_logs` VALUES (99, 21, '13675481145', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 19:30:50', 'SUCCESS', 'APP', '2026-01-15 19:30:50');
INSERT INTO `user_login_logs` VALUES (100, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 19:30:55', 'SUCCESS', 'APP', '2026-01-15 19:30:55');
INSERT INTO `user_login_logs` VALUES (101, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 19:32:24', 'SUCCESS', 'APP', '2026-01-15 19:32:24');
INSERT INTO `user_login_logs` VALUES (102, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 19:51:19', 'SUCCESS', 'APP', '2026-01-15 19:51:19');
INSERT INTO `user_login_logs` VALUES (103, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 23:29:06', 'SUCCESS', 'APP', '2026-01-15 23:29:06');
INSERT INTO `user_login_logs` VALUES (104, 12, '13800138011', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 23:29:13', 'SUCCESS', 'APP', '2026-01-15 23:29:13');
INSERT INTO `user_login_logs` VALUES (105, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 23:29:23', 'SUCCESS', 'APP', '2026-01-15 23:29:23');
INSERT INTO `user_login_logs` VALUES (106, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-15 23:37:40', 'SUCCESS', 'APP', '2026-01-15 23:37:40');
INSERT INTO `user_login_logs` VALUES (107, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 09:21:00', 'SUCCESS', 'APP', '2026-01-16 09:21:00');
INSERT INTO `user_login_logs` VALUES (108, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 09:24:19', 'SUCCESS', 'APP', '2026-01-16 09:24:19');
INSERT INTO `user_login_logs` VALUES (109, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 09:26:58', 'SUCCESS', 'APP', '2026-01-16 09:26:58');
INSERT INTO `user_login_logs` VALUES (110, 20, '13675488008', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 09:32:05', 'SUCCESS', 'APP', '2026-01-16 09:32:05');
INSERT INTO `user_login_logs` VALUES (111, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 09:32:13', 'SUCCESS', 'APP', '2026-01-16 09:32:13');
INSERT INTO `user_login_logs` VALUES (112, 5, '15336646648', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 09:32:18', 'SUCCESS', 'APP', '2026-01-16 09:32:18');
INSERT INTO `user_login_logs` VALUES (114, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 10:08:27', 'SUCCESS', 'APP', '2026-01-16 10:08:27');
INSERT INTO `user_login_logs` VALUES (115, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 10:31:49', 'SUCCESS', 'APP', '2026-01-16 10:31:49');
INSERT INTO `user_login_logs` VALUES (116, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 10:32:19', 'SUCCESS', 'APP', '2026-01-16 10:32:19');
INSERT INTO `user_login_logs` VALUES (117, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 10:33:19', 'SUCCESS', 'APP', '2026-01-16 10:33:19');
INSERT INTO `user_login_logs` VALUES (118, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 10:42:41', 'SUCCESS', 'APP', '2026-01-16 10:42:41');
INSERT INTO `user_login_logs` VALUES (119, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 10:49:25', 'SUCCESS', 'APP', '2026-01-16 10:49:25');
INSERT INTO `user_login_logs` VALUES (120, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 10:57:16', 'SUCCESS', 'APP', '2026-01-16 10:57:16');
INSERT INTO `user_login_logs` VALUES (121, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 11:07:54', 'SUCCESS', 'APP', '2026-01-16 11:07:54');
INSERT INTO `user_login_logs` VALUES (122, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 11:08:01', 'SUCCESS', 'APP', '2026-01-16 11:08:01');
INSERT INTO `user_login_logs` VALUES (123, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 12:24:16', 'SUCCESS', 'APP', '2026-01-16 12:24:16');
INSERT INTO `user_login_logs` VALUES (124, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 12:24:27', 'SUCCESS', 'APP', '2026-01-16 12:24:27');
INSERT INTO `user_login_logs` VALUES (125, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 12:25:45', 'SUCCESS', 'APP', '2026-01-16 12:25:45');
INSERT INTO `user_login_logs` VALUES (126, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 12:33:13', 'SUCCESS', 'APP', '2026-01-16 12:33:13');
INSERT INTO `user_login_logs` VALUES (127, 5, '15336646648', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 12:33:50', 'SUCCESS', 'APP', '2026-01-16 12:33:50');
INSERT INTO `user_login_logs` VALUES (128, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 12:42:42', 'SUCCESS', 'APP', '2026-01-16 12:42:42');
INSERT INTO `user_login_logs` VALUES (129, 5, '15336646648', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 12:43:08', 'SUCCESS', 'APP', '2026-01-16 12:43:08');
INSERT INTO `user_login_logs` VALUES (130, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 12:59:25', 'SUCCESS', 'APP', '2026-01-16 12:59:25');
INSERT INTO `user_login_logs` VALUES (131, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 12:59:31', 'SUCCESS', 'APP', '2026-01-16 12:59:31');
INSERT INTO `user_login_logs` VALUES (132, 12, '13800138011', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 12:59:39', 'SUCCESS', 'APP', '2026-01-16 12:59:39');
INSERT INTO `user_login_logs` VALUES (133, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 12:59:46', 'SUCCESS', 'APP', '2026-01-16 12:59:46');
INSERT INTO `user_login_logs` VALUES (134, 5, '15336646648', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 13:00:35', 'SUCCESS', 'APP', '2026-01-16 13:00:35');
INSERT INTO `user_login_logs` VALUES (135, 5, '15336646648', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 13:03:49', 'SUCCESS', 'APP', '2026-01-16 13:03:49');
INSERT INTO `user_login_logs` VALUES (136, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 13:04:02', 'SUCCESS', 'APP', '2026-01-16 13:04:02');
INSERT INTO `user_login_logs` VALUES (137, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 13:04:50', 'SUCCESS', 'APP', '2026-01-16 13:04:50');
INSERT INTO `user_login_logs` VALUES (138, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 13:05:00', 'SUCCESS', 'APP', '2026-01-16 13:05:00');
INSERT INTO `user_login_logs` VALUES (139, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 13:20:52', 'SUCCESS', 'APP', '2026-01-16 13:20:52');
INSERT INTO `user_login_logs` VALUES (140, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 13:24:05', 'SUCCESS', 'APP', '2026-01-16 13:24:05');
INSERT INTO `user_login_logs` VALUES (141, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 13:29:08', 'SUCCESS', 'APP', '2026-01-16 13:29:08');
INSERT INTO `user_login_logs` VALUES (142, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 13:38:57', 'SUCCESS', 'APP', '2026-01-16 13:38:57');
INSERT INTO `user_login_logs` VALUES (143, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 13:41:07', 'SUCCESS', 'APP', '2026-01-16 13:41:07');
INSERT INTO `user_login_logs` VALUES (144, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 13:41:50', 'SUCCESS', 'APP', '2026-01-16 13:41:50');
INSERT INTO `user_login_logs` VALUES (145, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 13:53:28', 'SUCCESS', 'APP', '2026-01-16 13:53:28');
INSERT INTO `user_login_logs` VALUES (146, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 13:53:51', 'SUCCESS', 'APP', '2026-01-16 13:53:51');
INSERT INTO `user_login_logs` VALUES (147, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 14:05:53', 'SUCCESS', 'APP', '2026-01-16 14:05:53');
INSERT INTO `user_login_logs` VALUES (148, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 14:06:04', 'SUCCESS', 'APP', '2026-01-16 14:06:04');
INSERT INTO `user_login_logs` VALUES (149, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 14:10:35', 'SUCCESS', 'APP', '2026-01-16 14:10:35');
INSERT INTO `user_login_logs` VALUES (150, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 14:18:37', 'SUCCESS', 'APP', '2026-01-16 14:18:37');
INSERT INTO `user_login_logs` VALUES (151, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 14:26:14', 'SUCCESS', 'APP', '2026-01-16 14:26:14');
INSERT INTO `user_login_logs` VALUES (152, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 14:32:49', 'SUCCESS', 'APP', '2026-01-16 14:32:49');
INSERT INTO `user_login_logs` VALUES (153, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 14:38:27', 'SUCCESS', 'APP', '2026-01-16 14:38:27');
INSERT INTO `user_login_logs` VALUES (154, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 14:50:19', 'SUCCESS', 'APP', '2026-01-16 14:50:19');
INSERT INTO `user_login_logs` VALUES (155, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 14:58:20', 'SUCCESS', 'APP', '2026-01-16 14:58:20');
INSERT INTO `user_login_logs` VALUES (156, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 15:15:50', 'SUCCESS', 'APP', '2026-01-16 15:15:50');
INSERT INTO `user_login_logs` VALUES (157, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 15:18:44', 'SUCCESS', 'APP', '2026-01-16 15:18:44');
INSERT INTO `user_login_logs` VALUES (158, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 15:33:05', 'SUCCESS', 'APP', '2026-01-16 15:33:05');
INSERT INTO `user_login_logs` VALUES (159, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 15:37:19', 'SUCCESS', 'APP', '2026-01-16 15:37:19');
INSERT INTO `user_login_logs` VALUES (160, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 15:40:31', 'SUCCESS', 'APP', '2026-01-16 15:40:31');
INSERT INTO `user_login_logs` VALUES (161, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 16:02:55', 'SUCCESS', 'APP', '2026-01-16 16:02:55');
INSERT INTO `user_login_logs` VALUES (162, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 16:17:43', 'SUCCESS', 'APP', '2026-01-16 16:17:43');
INSERT INTO `user_login_logs` VALUES (163, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 16:36:22', 'SUCCESS', 'APP', '2026-01-16 16:36:22');
INSERT INTO `user_login_logs` VALUES (164, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 18:02:49', 'SUCCESS', 'APP', '2026-01-16 18:02:49');
INSERT INTO `user_login_logs` VALUES (165, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 18:03:33', 'SUCCESS', 'APP', '2026-01-16 18:03:33');
INSERT INTO `user_login_logs` VALUES (166, 5, '15336646648', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 18:03:39', 'SUCCESS', 'APP', '2026-01-16 18:03:39');
INSERT INTO `user_login_logs` VALUES (167, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 18:03:44', 'SUCCESS', 'APP', '2026-01-16 18:03:44');
INSERT INTO `user_login_logs` VALUES (168, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 18:04:27', 'SUCCESS', 'APP', '2026-01-16 18:04:27');
INSERT INTO `user_login_logs` VALUES (169, 5, '15336646648', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 18:04:37', 'SUCCESS', 'APP', '2026-01-16 18:04:37');
INSERT INTO `user_login_logs` VALUES (170, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 18:04:51', 'SUCCESS', 'APP', '2026-01-16 18:04:51');
INSERT INTO `user_login_logs` VALUES (171, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 18:05:03', 'SUCCESS', 'APP', '2026-01-16 18:05:03');
INSERT INTO `user_login_logs` VALUES (172, 6, '15163457180', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 18:05:19', 'SUCCESS', 'APP', '2026-01-16 18:05:19');
INSERT INTO `user_login_logs` VALUES (173, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 18:05:35', 'SUCCESS', 'APP', '2026-01-16 18:05:35');
INSERT INTO `user_login_logs` VALUES (174, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 18:05:53', 'SUCCESS', 'APP', '2026-01-16 18:05:53');
INSERT INTO `user_login_logs` VALUES (175, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 18:07:59', 'SUCCESS', 'APP', '2026-01-16 18:07:59');
INSERT INTO `user_login_logs` VALUES (176, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 18:08:02', 'SUCCESS', 'APP', '2026-01-16 18:08:02');
INSERT INTO `user_login_logs` VALUES (177, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 18:08:55', 'SUCCESS', 'APP', '2026-01-16 18:08:55');
INSERT INTO `user_login_logs` VALUES (178, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 18:22:03', 'SUCCESS', 'APP', '2026-01-16 18:22:03');
INSERT INTO `user_login_logs` VALUES (179, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 18:22:21', 'SUCCESS', 'APP', '2026-01-16 18:22:21');
INSERT INTO `user_login_logs` VALUES (180, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 18:23:48', 'SUCCESS', 'APP', '2026-01-16 18:23:48');
INSERT INTO `user_login_logs` VALUES (181, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 18:24:20', 'SUCCESS', 'APP', '2026-01-16 18:24:20');
INSERT INTO `user_login_logs` VALUES (182, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 18:33:04', 'SUCCESS', 'APP', '2026-01-16 18:33:04');
INSERT INTO `user_login_logs` VALUES (183, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 19:02:28', 'SUCCESS', 'APP', '2026-01-16 19:02:28');
INSERT INTO `user_login_logs` VALUES (184, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 19:17:15', 'SUCCESS', 'APP', '2026-01-16 19:17:15');
INSERT INTO `user_login_logs` VALUES (185, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 19:17:29', 'SUCCESS', 'APP', '2026-01-16 19:17:29');
INSERT INTO `user_login_logs` VALUES (186, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 19:20:47', 'SUCCESS', 'APP', '2026-01-16 19:20:47');
INSERT INTO `user_login_logs` VALUES (187, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 19:20:53', 'SUCCESS', 'APP', '2026-01-16 19:20:53');
INSERT INTO `user_login_logs` VALUES (188, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 19:40:29', 'SUCCESS', 'APP', '2026-01-16 19:40:29');
INSERT INTO `user_login_logs` VALUES (189, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', '2026-01-16 20:26:40', 'SUCCESS', 'APP', '2026-01-16 20:26:40');
INSERT INTO `user_login_logs` VALUES (190, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 11:23:34', 'SUCCESS', 'APP', '2026-01-17 11:23:34');
INSERT INTO `user_login_logs` VALUES (191, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 11:24:15', 'SUCCESS', 'APP', '2026-01-17 11:24:15');
INSERT INTO `user_login_logs` VALUES (192, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 11:28:01', 'SUCCESS', 'APP', '2026-01-17 11:28:01');
INSERT INTO `user_login_logs` VALUES (193, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 11:28:08', 'SUCCESS', 'APP', '2026-01-17 11:28:08');
INSERT INTO `user_login_logs` VALUES (194, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 11:42:58', 'SUCCESS', 'APP', '2026-01-17 11:42:58');
INSERT INTO `user_login_logs` VALUES (195, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 11:43:02', 'SUCCESS', 'APP', '2026-01-17 11:43:02');
INSERT INTO `user_login_logs` VALUES (196, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 11:50:56', 'SUCCESS', 'APP', '2026-01-17 11:50:56');
INSERT INTO `user_login_logs` VALUES (197, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 11:59:45', 'SUCCESS', 'APP', '2026-01-17 11:59:45');
INSERT INTO `user_login_logs` VALUES (198, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 12:00:00', 'SUCCESS', 'APP', '2026-01-17 12:00:00');
INSERT INTO `user_login_logs` VALUES (199, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 12:05:11', 'SUCCESS', 'APP', '2026-01-17 12:05:11');
INSERT INTO `user_login_logs` VALUES (200, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 12:11:15', 'SUCCESS', 'APP', '2026-01-17 12:11:15');
INSERT INTO `user_login_logs` VALUES (201, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 12:13:02', 'SUCCESS', 'APP', '2026-01-17 12:13:02');
INSERT INTO `user_login_logs` VALUES (202, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 13:05:29', 'SUCCESS', 'APP', '2026-01-17 13:05:29');
INSERT INTO `user_login_logs` VALUES (203, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 13:35:40', 'SUCCESS', 'APP', '2026-01-17 13:35:40');
INSERT INTO `user_login_logs` VALUES (204, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 13:50:59', 'SUCCESS', 'APP', '2026-01-17 13:50:59');
INSERT INTO `user_login_logs` VALUES (205, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 13:55:34', 'SUCCESS', 'APP', '2026-01-17 13:55:34');
INSERT INTO `user_login_logs` VALUES (206, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 13:58:56', 'SUCCESS', 'APP', '2026-01-17 13:58:56');
INSERT INTO `user_login_logs` VALUES (207, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 14:03:54', 'SUCCESS', 'APP', '2026-01-17 14:03:54');
INSERT INTO `user_login_logs` VALUES (208, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 14:13:23', 'SUCCESS', 'APP', '2026-01-17 14:13:23');
INSERT INTO `user_login_logs` VALUES (209, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 14:24:47', 'SUCCESS', 'APP', '2026-01-17 14:24:47');
INSERT INTO `user_login_logs` VALUES (210, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 14:26:32', 'SUCCESS', 'APP', '2026-01-17 14:26:32');
INSERT INTO `user_login_logs` VALUES (211, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 14:30:52', 'SUCCESS', 'APP', '2026-01-17 14:30:52');
INSERT INTO `user_login_logs` VALUES (212, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 14:35:21', 'SUCCESS', 'APP', '2026-01-17 14:35:21');
INSERT INTO `user_login_logs` VALUES (213, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 14:39:45', 'SUCCESS', 'APP', '2026-01-17 14:39:45');
INSERT INTO `user_login_logs` VALUES (214, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 15:31:32', 'SUCCESS', 'APP', '2026-01-17 15:31:32');
INSERT INTO `user_login_logs` VALUES (215, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 21:32:01', 'SUCCESS', 'APP', '2026-01-17 21:32:01');
INSERT INTO `user_login_logs` VALUES (216, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 21:42:17', 'SUCCESS', 'APP', '2026-01-17 21:42:17');
INSERT INTO `user_login_logs` VALUES (217, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 21:52:05', 'SUCCESS', 'APP', '2026-01-17 21:52:05');
INSERT INTO `user_login_logs` VALUES (218, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 22:14:13', 'SUCCESS', 'APP', '2026-01-17 22:14:13');
INSERT INTO `user_login_logs` VALUES (219, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 22:16:04', 'SUCCESS', 'APP', '2026-01-17 22:16:04');
INSERT INTO `user_login_logs` VALUES (220, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 22:20:04', 'SUCCESS', 'APP', '2026-01-17 22:20:04');
INSERT INTO `user_login_logs` VALUES (221, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 22:21:10', 'SUCCESS', 'APP', '2026-01-17 22:21:10');
INSERT INTO `user_login_logs` VALUES (222, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 22:27:04', 'SUCCESS', 'APP', '2026-01-17 22:27:04');
INSERT INTO `user_login_logs` VALUES (223, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 22:27:22', 'SUCCESS', 'APP', '2026-01-17 22:27:22');
INSERT INTO `user_login_logs` VALUES (224, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 22:29:00', 'SUCCESS', 'APP', '2026-01-17 22:29:00');
INSERT INTO `user_login_logs` VALUES (225, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 22:31:19', 'SUCCESS', 'APP', '2026-01-17 22:31:19');
INSERT INTO `user_login_logs` VALUES (226, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 22:34:34', 'SUCCESS', 'APP', '2026-01-17 22:34:34');
INSERT INTO `user_login_logs` VALUES (227, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 22:34:49', 'SUCCESS', 'APP', '2026-01-17 22:34:49');
INSERT INTO `user_login_logs` VALUES (228, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 22:38:56', 'SUCCESS', 'APP', '2026-01-17 22:38:56');
INSERT INTO `user_login_logs` VALUES (229, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 22:39:06', 'SUCCESS', 'APP', '2026-01-17 22:39:06');
INSERT INTO `user_login_logs` VALUES (230, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 22:57:40', 'SUCCESS', 'APP', '2026-01-17 22:57:40');
INSERT INTO `user_login_logs` VALUES (231, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 23:11:01', 'SUCCESS', 'APP', '2026-01-17 23:11:01');
INSERT INTO `user_login_logs` VALUES (232, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 23:11:54', 'SUCCESS', 'APP', '2026-01-17 23:11:54');
INSERT INTO `user_login_logs` VALUES (233, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 23:13:21', 'SUCCESS', 'APP', '2026-01-17 23:13:21');
INSERT INTO `user_login_logs` VALUES (234, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 23:16:36', 'SUCCESS', 'APP', '2026-01-17 23:16:36');
INSERT INTO `user_login_logs` VALUES (235, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-17 23:21:11', 'SUCCESS', 'APP', '2026-01-17 23:21:11');
INSERT INTO `user_login_logs` VALUES (236, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 10:31:42', 'SUCCESS', 'APP', '2026-01-18 10:31:42');
INSERT INTO `user_login_logs` VALUES (237, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 10:32:15', 'SUCCESS', 'APP', '2026-01-18 10:32:15');
INSERT INTO `user_login_logs` VALUES (238, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 10:35:46', 'SUCCESS', 'APP', '2026-01-18 10:35:46');
INSERT INTO `user_login_logs` VALUES (239, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 10:36:07', 'SUCCESS', 'APP', '2026-01-18 10:36:07');
INSERT INTO `user_login_logs` VALUES (240, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 10:40:52', 'SUCCESS', 'APP', '2026-01-18 10:40:52');
INSERT INTO `user_login_logs` VALUES (241, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 10:53:22', 'SUCCESS', 'APP', '2026-01-18 10:53:22');
INSERT INTO `user_login_logs` VALUES (242, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 11:05:12', 'SUCCESS', 'APP', '2026-01-18 11:05:12');
INSERT INTO `user_login_logs` VALUES (243, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 11:18:03', 'SUCCESS', 'APP', '2026-01-18 11:18:03');
INSERT INTO `user_login_logs` VALUES (244, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 11:32:36', 'SUCCESS', 'APP', '2026-01-18 11:32:36');
INSERT INTO `user_login_logs` VALUES (245, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 11:32:56', 'SUCCESS', 'APP', '2026-01-18 11:32:56');
INSERT INTO `user_login_logs` VALUES (246, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 11:37:29', 'SUCCESS', 'APP', '2026-01-18 11:37:29');
INSERT INTO `user_login_logs` VALUES (247, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 11:37:49', 'SUCCESS', 'APP', '2026-01-18 11:37:49');
INSERT INTO `user_login_logs` VALUES (248, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 11:38:06', 'SUCCESS', 'APP', '2026-01-18 11:38:06');
INSERT INTO `user_login_logs` VALUES (249, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 13:12:44', 'SUCCESS', 'APP', '2026-01-18 13:12:44');
INSERT INTO `user_login_logs` VALUES (250, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 13:19:28', 'SUCCESS', 'APP', '2026-01-18 13:19:28');
INSERT INTO `user_login_logs` VALUES (251, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 14:00:50', 'SUCCESS', 'APP', '2026-01-18 14:00:50');
INSERT INTO `user_login_logs` VALUES (252, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 14:45:51', 'SUCCESS', 'APP', '2026-01-18 14:45:51');
INSERT INTO `user_login_logs` VALUES (253, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 15:30:51', 'SUCCESS', 'APP', '2026-01-18 15:30:51');
INSERT INTO `user_login_logs` VALUES (254, 21, '13675481145', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 15:33:52', 'SUCCESS', 'APP', '2026-01-18 15:33:52');
INSERT INTO `user_login_logs` VALUES (255, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 15:33:57', 'SUCCESS', 'APP', '2026-01-18 15:33:57');
INSERT INTO `user_login_logs` VALUES (256, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 15:34:11', 'SUCCESS', 'APP', '2026-01-18 15:34:11');
INSERT INTO `user_login_logs` VALUES (257, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 15:34:19', 'SUCCESS', 'APP', '2026-01-18 15:34:19');
INSERT INTO `user_login_logs` VALUES (258, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 15:35:34', 'SUCCESS', 'APP', '2026-01-18 15:35:34');
INSERT INTO `user_login_logs` VALUES (259, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 15:36:15', 'SUCCESS', 'APP', '2026-01-18 15:36:15');
INSERT INTO `user_login_logs` VALUES (260, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 15:36:38', 'SUCCESS', 'APP', '2026-01-18 15:36:38');
INSERT INTO `user_login_logs` VALUES (261, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 15:46:02', 'SUCCESS', 'APP', '2026-01-18 15:46:02');
INSERT INTO `user_login_logs` VALUES (262, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 15:49:54', 'SUCCESS', 'APP', '2026-01-18 15:49:54');
INSERT INTO `user_login_logs` VALUES (263, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 15:52:14', 'SUCCESS', 'APP', '2026-01-18 15:52:14');
INSERT INTO `user_login_logs` VALUES (264, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 16:00:56', 'SUCCESS', 'APP', '2026-01-18 16:00:56');
INSERT INTO `user_login_logs` VALUES (265, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 16:08:04', 'SUCCESS', 'APP', '2026-01-18 16:08:04');
INSERT INTO `user_login_logs` VALUES (266, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 16:15:46', 'SUCCESS', 'APP', '2026-01-18 16:15:46');
INSERT INTO `user_login_logs` VALUES (267, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 16:18:14', 'SUCCESS', 'APP', '2026-01-18 16:18:14');
INSERT INTO `user_login_logs` VALUES (268, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 16:21:19', 'SUCCESS', 'APP', '2026-01-18 16:21:19');
INSERT INTO `user_login_logs` VALUES (269, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 16:21:59', 'SUCCESS', 'APP', '2026-01-18 16:21:59');
INSERT INTO `user_login_logs` VALUES (270, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 16:53:14', 'SUCCESS', 'APP', '2026-01-18 16:53:14');
INSERT INTO `user_login_logs` VALUES (271, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 16:58:22', 'SUCCESS', 'APP', '2026-01-18 16:58:22');
INSERT INTO `user_login_logs` VALUES (272, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 17:01:46', 'SUCCESS', 'APP', '2026-01-18 17:01:46');
INSERT INTO `user_login_logs` VALUES (273, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 17:06:15', 'SUCCESS', 'APP', '2026-01-18 17:06:15');
INSERT INTO `user_login_logs` VALUES (274, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 17:09:49', 'SUCCESS', 'APP', '2026-01-18 17:09:49');
INSERT INTO `user_login_logs` VALUES (275, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 17:21:00', 'SUCCESS', 'APP', '2026-01-18 17:21:00');
INSERT INTO `user_login_logs` VALUES (276, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 17:25:44', 'SUCCESS', 'APP', '2026-01-18 17:25:44');
INSERT INTO `user_login_logs` VALUES (277, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 17:31:47', 'SUCCESS', 'APP', '2026-01-18 17:31:47');
INSERT INTO `user_login_logs` VALUES (278, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 17:36:19', 'SUCCESS', 'APP', '2026-01-18 17:36:19');
INSERT INTO `user_login_logs` VALUES (279, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 17:39:38', 'SUCCESS', 'APP', '2026-01-18 17:39:38');
INSERT INTO `user_login_logs` VALUES (280, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 17:45:40', 'SUCCESS', 'APP', '2026-01-18 17:45:40');
INSERT INTO `user_login_logs` VALUES (281, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 17:45:49', 'SUCCESS', 'APP', '2026-01-18 17:45:49');
INSERT INTO `user_login_logs` VALUES (282, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 17:49:40', 'SUCCESS', 'APP', '2026-01-18 17:49:40');
INSERT INTO `user_login_logs` VALUES (283, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 17:55:32', 'SUCCESS', 'APP', '2026-01-18 17:55:32');
INSERT INTO `user_login_logs` VALUES (284, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 17:55:42', 'SUCCESS', 'APP', '2026-01-18 17:55:42');
INSERT INTO `user_login_logs` VALUES (285, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 18:16:57', 'SUCCESS', 'APP', '2026-01-18 18:16:57');
INSERT INTO `user_login_logs` VALUES (286, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 18:47:58', 'SUCCESS', 'APP', '2026-01-18 18:47:58');
INSERT INTO `user_login_logs` VALUES (287, 5, '15336646648', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 18:48:32', 'SUCCESS', 'APP', '2026-01-18 18:48:32');
INSERT INTO `user_login_logs` VALUES (288, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 18:48:41', 'SUCCESS', 'APP', '2026-01-18 18:48:41');
INSERT INTO `user_login_logs` VALUES (289, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 18:49:41', 'SUCCESS', 'APP', '2026-01-18 18:49:41');
INSERT INTO `user_login_logs` VALUES (290, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 18:58:28', 'SUCCESS', 'APP', '2026-01-18 18:58:28');
INSERT INTO `user_login_logs` VALUES (291, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 19:25:52', 'SUCCESS', 'APP', '2026-01-18 19:25:52');
INSERT INTO `user_login_logs` VALUES (292, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-18 19:30:44', 'SUCCESS', 'APP', '2026-01-18 19:30:44');
INSERT INTO `user_login_logs` VALUES (293, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 11:49:50', 'SUCCESS', 'APP', '2026-01-19 11:49:50');
INSERT INTO `user_login_logs` VALUES (294, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 11:57:08', 'SUCCESS', 'APP', '2026-01-19 11:57:08');
INSERT INTO `user_login_logs` VALUES (295, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 11:57:54', 'SUCCESS', 'APP', '2026-01-19 11:57:54');
INSERT INTO `user_login_logs` VALUES (296, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 12:01:48', 'SUCCESS', 'APP', '2026-01-19 12:01:48');
INSERT INTO `user_login_logs` VALUES (297, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 12:02:00', 'SUCCESS', 'APP', '2026-01-19 12:02:00');
INSERT INTO `user_login_logs` VALUES (298, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 12:42:41', 'SUCCESS', 'APP', '2026-01-19 12:42:41');
INSERT INTO `user_login_logs` VALUES (299, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 12:55:57', 'SUCCESS', 'APP', '2026-01-19 12:55:57');
INSERT INTO `user_login_logs` VALUES (300, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 12:57:01', 'SUCCESS', 'APP', '2026-01-19 12:57:01');
INSERT INTO `user_login_logs` VALUES (301, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 13:00:46', 'SUCCESS', 'APP', '2026-01-19 13:00:46');
INSERT INTO `user_login_logs` VALUES (302, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 13:01:15', 'SUCCESS', 'APP', '2026-01-19 13:01:15');
INSERT INTO `user_login_logs` VALUES (303, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 13:01:30', 'SUCCESS', 'APP', '2026-01-19 13:01:30');
INSERT INTO `user_login_logs` VALUES (304, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 13:43:43', 'SUCCESS', 'APP', '2026-01-19 13:43:43');
INSERT INTO `user_login_logs` VALUES (305, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 13:44:06', 'SUCCESS', 'APP', '2026-01-19 13:44:06');
INSERT INTO `user_login_logs` VALUES (306, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 13:44:23', 'SUCCESS', 'APP', '2026-01-19 13:44:23');
INSERT INTO `user_login_logs` VALUES (307, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 13:48:19', 'SUCCESS', 'APP', '2026-01-19 13:48:19');
INSERT INTO `user_login_logs` VALUES (308, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 13:51:52', 'SUCCESS', 'APP', '2026-01-19 13:51:52');
INSERT INTO `user_login_logs` VALUES (309, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 13:52:00', 'SUCCESS', 'APP', '2026-01-19 13:52:00');
INSERT INTO `user_login_logs` VALUES (310, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 13:52:17', 'SUCCESS', 'APP', '2026-01-19 13:52:17');
INSERT INTO `user_login_logs` VALUES (311, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 13:52:43', 'SUCCESS', 'APP', '2026-01-19 13:52:43');
INSERT INTO `user_login_logs` VALUES (312, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 13:56:58', 'SUCCESS', 'APP', '2026-01-19 13:56:58');
INSERT INTO `user_login_logs` VALUES (313, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 14:04:40', 'SUCCESS', 'APP', '2026-01-19 14:04:41');
INSERT INTO `user_login_logs` VALUES (314, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 14:09:53', 'SUCCESS', 'APP', '2026-01-19 14:09:53');
INSERT INTO `user_login_logs` VALUES (315, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 14:19:43', 'SUCCESS', 'APP', '2026-01-19 14:19:43');
INSERT INTO `user_login_logs` VALUES (316, 12, '13800138011', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 14:19:57', 'SUCCESS', 'APP', '2026-01-19 14:19:57');
INSERT INTO `user_login_logs` VALUES (317, 6, '15163457180', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 14:20:04', 'SUCCESS', 'APP', '2026-01-19 14:20:04');
INSERT INTO `user_login_logs` VALUES (318, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 15:50:53', 'SUCCESS', 'APP', '2026-01-19 15:50:53');
INSERT INTO `user_login_logs` VALUES (319, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 16:06:25', 'SUCCESS', 'APP', '2026-01-19 16:06:25');
INSERT INTO `user_login_logs` VALUES (320, 13, '13800138019', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 16:35:00', 'SUCCESS', 'APP', '2026-01-19 16:35:00');
INSERT INTO `user_login_logs` VALUES (321, 6, '15163457180', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 16:35:09', 'SUCCESS', 'APP', '2026-01-19 16:35:09');
INSERT INTO `user_login_logs` VALUES (322, 12, '13800138011', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 16:35:22', 'SUCCESS', 'APP', '2026-01-19 16:35:22');
INSERT INTO `user_login_logs` VALUES (323, 3, '13800138000', 'MEMBER', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 16:35:26', 'SUCCESS', 'APP', '2026-01-19 16:35:26');
INSERT INTO `user_login_logs` VALUES (324, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 16:35:43', 'SUCCESS', 'APP', '2026-01-19 16:35:43');
INSERT INTO `user_login_logs` VALUES (325, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 16:35:54', 'SUCCESS', 'APP', '2026-01-19 16:35:54');
INSERT INTO `user_login_logs` VALUES (326, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 16:49:24', 'SUCCESS', 'APP', '2026-01-19 16:49:24');
INSERT INTO `user_login_logs` VALUES (327, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 17:05:35', 'SUCCESS', 'APP', '2026-01-19 17:05:35');
INSERT INTO `user_login_logs` VALUES (328, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 17:13:34', 'SUCCESS', 'APP', '2026-01-19 17:13:34');
INSERT INTO `user_login_logs` VALUES (329, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 17:15:23', 'SUCCESS', 'APP', '2026-01-19 17:15:23');
INSERT INTO `user_login_logs` VALUES (330, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 17:18:47', 'SUCCESS', 'APP', '2026-01-19 17:18:47');
INSERT INTO `user_login_logs` VALUES (331, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 17:26:59', 'SUCCESS', 'APP', '2026-01-19 17:26:59');
INSERT INTO `user_login_logs` VALUES (332, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 17:33:53', 'SUCCESS', 'APP', '2026-01-19 17:33:53');
INSERT INTO `user_login_logs` VALUES (333, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 17:35:26', 'SUCCESS', 'APP', '2026-01-19 17:35:26');
INSERT INTO `user_login_logs` VALUES (334, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 17:36:46', 'SUCCESS', 'APP', '2026-01-19 17:36:46');
INSERT INTO `user_login_logs` VALUES (335, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 18:03:05', 'SUCCESS', 'APP', '2026-01-19 18:03:05');
INSERT INTO `user_login_logs` VALUES (336, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 18:18:50', 'SUCCESS', 'APP', '2026-01-19 18:18:50');
INSERT INTO `user_login_logs` VALUES (337, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 18:19:52', 'SUCCESS', 'APP', '2026-01-19 18:19:52');
INSERT INTO `user_login_logs` VALUES (338, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 18:42:26', 'SUCCESS', 'APP', '2026-01-19 18:42:26');
INSERT INTO `user_login_logs` VALUES (339, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 18:45:37', 'SUCCESS', 'APP', '2026-01-19 18:45:37');
INSERT INTO `user_login_logs` VALUES (340, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 18:45:52', 'SUCCESS', 'APP', '2026-01-19 18:45:52');
INSERT INTO `user_login_logs` VALUES (341, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 19:00:32', 'SUCCESS', 'APP', '2026-01-19 19:00:32');
INSERT INTO `user_login_logs` VALUES (342, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 19:18:16', 'SUCCESS', 'APP', '2026-01-19 19:18:16');
INSERT INTO `user_login_logs` VALUES (343, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 19:19:02', 'SUCCESS', 'APP', '2026-01-19 19:19:02');
INSERT INTO `user_login_logs` VALUES (344, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 19:22:57', 'SUCCESS', 'APP', '2026-01-19 19:22:57');
INSERT INTO `user_login_logs` VALUES (345, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 19:38:29', 'SUCCESS', 'APP', '2026-01-19 19:38:29');
INSERT INTO `user_login_logs` VALUES (346, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 19:57:44', 'SUCCESS', 'APP', '2026-01-19 19:57:44');
INSERT INTO `user_login_logs` VALUES (347, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 20:01:31', 'SUCCESS', 'APP', '2026-01-19 20:01:31');
INSERT INTO `user_login_logs` VALUES (348, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 20:19:53', 'SUCCESS', 'APP', '2026-01-19 20:19:53');
INSERT INTO `user_login_logs` VALUES (349, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 20:24:40', 'SUCCESS', 'APP', '2026-01-19 20:24:40');
INSERT INTO `user_login_logs` VALUES (350, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Trae/1.104.3 Chrome/138.0.7204.251 Electron/37.6.1 Safari/537.36', '2026-01-19 20:53:59', 'SUCCESS', 'APP', '2026-01-19 20:53:59');
INSERT INTO `user_login_logs` VALUES (351, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Trae/1.104.3 Chrome/138.0.7204.251 Electron/37.6.1 Safari/537.36', '2026-01-19 21:37:19', 'SUCCESS', 'APP', '2026-01-19 21:37:19');
INSERT INTO `user_login_logs` VALUES (352, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 21:38:17', 'SUCCESS', 'APP', '2026-01-19 21:38:17');
INSERT INTO `user_login_logs` VALUES (353, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 21:38:37', 'SUCCESS', 'APP', '2026-01-19 21:38:37');
INSERT INTO `user_login_logs` VALUES (354, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 21:40:10', 'SUCCESS', 'APP', '2026-01-19 21:40:10');
INSERT INTO `user_login_logs` VALUES (355, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 21:49:26', 'SUCCESS', 'APP', '2026-01-19 21:49:26');
INSERT INTO `user_login_logs` VALUES (356, 12, '13800138011', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 22:10:49', 'SUCCESS', 'APP', '2026-01-19 22:10:49');
INSERT INTO `user_login_logs` VALUES (357, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-19 23:18:01', 'SUCCESS', 'APP', '2026-01-19 23:18:01');
INSERT INTO `user_login_logs` VALUES (358, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-20 13:12:35', 'SUCCESS', 'APP', '2026-01-20 13:12:35');
INSERT INTO `user_login_logs` VALUES (359, 10, '13800138013', 'DOCTOR', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-20 13:13:32', 'SUCCESS', 'APP', '2026-01-20 13:13:32');
INSERT INTO `user_login_logs` VALUES (360, 12, '13800138011', 'ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-20 14:06:42', 'SUCCESS', 'APP', '2026-01-20 14:06:42');
INSERT INTO `user_login_logs` VALUES (361, 4, '18865488267', 'FAMILY_ADMIN', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36', '2026-01-20 14:07:17', 'SUCCESS', 'APP', '2026-01-20 14:07:17');

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
) ENGINE = InnoDB AUTO_INCREMENT = 115 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, '13800000000', NULL, '123456', '测试用户', 'MEMBER', 1, NULL, '2025-11-12 21:05:13', '2026-01-15 14:37:48', NULL, NULL, NULL);
INSERT INTO `users` VALUES (3, '13800138000', NULL, '$2a$10$24B5VtJVJv9DDAX/rimgpeBZOGB3I0YsmjuuYzJNxqvTLaAlwFrbC', '王士鑫', 'MEMBER', 1, '2026-01-19 16:35:26', '2025-11-13 08:40:55', '2026-01-19 16:35:26', 0, NULL, NULL);
INSERT INTO `users` VALUES (4, '18865488267', NULL, '$2a$10$2npnEu8AFTQOVQ.4JNDFMeg4gZMu3pB6PCNxDZmWrbMdkTbnM.kmm', '李骁勐', 'FAMILY_ADMIN', 1, '2026-01-20 14:07:17', '2025-11-13 09:00:53', '2026-01-20 14:07:17', 0, NULL, NULL);
INSERT INTO `users` VALUES (5, '15336646648', NULL, '$2a$10$aqtba5IIDfYIgICYBC0FvuOQ2DYyNOLLxrq0UgvRB3XkdwskrIujq', '郭兆腾', 'MEMBER', 1, '2026-01-18 18:48:32', '2025-11-13 11:43:08', '2026-01-18 18:48:32', 0, NULL, NULL);
INSERT INTO `users` VALUES (6, '15163457180', NULL, '$2a$10$ZL01s0pHCLM0IN4phmKDbOsT4DxGgtRERQbdrQkvKFq1Wo10lPc5W', '徐志永', 'MEMBER', 1, '2026-01-19 16:35:09', '2025-11-13 20:16:15', '2026-01-19 16:35:09', 0, NULL, NULL);
INSERT INTO `users` VALUES (7, '11451419198', NULL, '$2a$10$7EqJtq98hPqEX7fNZaFWoO.V8G/5hAq1XMG8wEfr2E1VY8IQ7vGDa', '淳平', 'DOCTOR', 1, NULL, '2025-11-14 16:31:38', '2025-11-14 16:41:49', NULL, NULL, NULL);
INSERT INTO `users` VALUES (8, '13800000002', NULL, '$2a$10$7EqJtq98hPqEX7fNZaFWoO.V8G/5hAq1XMG8wEfr2E1VY8IQ7vGDa', '李田所', 'DOCTOR', 1, NULL, '2025-11-14 16:31:48', '2025-11-14 16:31:48', NULL, NULL, NULL);
INSERT INTO `users` VALUES (9, '13800138012', NULL, '$2a$10$hS32iD241gxZbPbhvN3bi.KF9ypCIGlRhQicHHADuKafAChTg4q/6', '王爷', 'DOCTOR', 1, '2025-12-28 15:50:51', '2025-11-14 18:53:15', '2025-12-28 15:50:51', NULL, NULL, NULL);
INSERT INTO `users` VALUES (10, '13800138013', NULL, '$2a$10$so4gn9wZsbCtjqTUTLYL1.fDqnGf8YLqsu8ykf3Tpa0ElXoptW0m.', '李田所', 'DOCTOR', 1, '2026-01-20 13:13:32', '2025-11-14 19:13:12', '2026-01-20 13:13:32', 0, NULL, NULL);
INSERT INTO `users` VALUES (11, '13800138014', NULL, '$2a$10$md10To23n3YpdwhPXF//3ODajNYzlYuLxs.l.CJaK/pl2S7MFsH9.', '辛吉飞', 'DOCTOR', 1, '2025-11-14 19:46:35', '2025-11-14 19:17:28', '2025-11-14 19:46:35', NULL, NULL, NULL);
INSERT INTO `users` VALUES (12, '13800138011', NULL, '$2a$10$RZDfRIBXhXZrRSRwTw1y8eK/YMSHoaxUCQgmMAsve2Eiq3toXHqsa', '王文卓', 'ADMIN', 1, '2026-01-20 14:06:42', '2025-12-29 20:10:14', '2026-01-20 14:06:42', 0, NULL, NULL);
INSERT INTO `users` VALUES (13, '13800138019', NULL, '$2a$10$g1oaOba5zPBHJBVxh2zni.6YEg41YqJfH304GFEE7uGMt7YY8xR0S', '你哥', 'ADMIN', 1, '2026-01-19 16:35:00', '2025-12-30 15:11:15', '2026-01-19 16:35:00', 0, NULL, NULL);
INSERT INTO `users` VALUES (20, '13675488008', NULL, '$2a$10$L9I7gDvZFgAS7b6poLq7be/o5H/yKy8kb0Wr7phPDAdJZ6WjWJ00K', '海鲜', 'MEMBER', 1, '2026-01-16 09:32:05', '2026-01-15 18:40:36', '2026-01-16 09:32:05', 0, NULL, NULL);
INSERT INTO `users` VALUES (21, '13675481145', NULL, '$2a$10$RjmTWYwuj/VJMqoIFY0W9uYFWIcaHMAC0w4a3QxkxmjOJK2KgdVYO', '赛文', 'DOCTOR', 1, '2026-01-18 15:33:52', '2026-01-15 19:20:55', '2026-01-18 15:33:52', 0, NULL, NULL);
INSERT INTO `users` VALUES (22, '13900000001', NULL, '$2a$10$7VpBsxSLB5ABJ9jbuq.tIutGyN0DfIy4YG8QoMk/QK6nfOYHWdiv2', 'temp-admin', 'ADMIN', 1, NULL, '2026-01-16 09:44:22', '2026-01-16 09:44:22', NULL, NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
