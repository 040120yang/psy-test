/*
 Navicat Premium Data Transfer

 Source Server         : yq
 Source Server Type    : MySQL
 Source Server Version : 80012 (8.0.12)
 Source Host           : localhost:3306
 Source Schema         : psy_test

 Target Server Type    : MySQL
 Target Server Version : 80012 (8.0.12)
 File Encoding         : 65001

 Date: 14/09/2026 20:48:39
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for psy_patient
-- ----------------------------
DROP TABLE IF EXISTS `psy_patient`;
CREATE TABLE `psy_patient`  (
  `patient_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '患者ID',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '关联注册用户ID',
  `patient_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '患者姓名',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '2' COMMENT '性别(0男 1女 2未知)',
  `age` int(11) NULL DEFAULT NULL COMMENT '年龄',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '身份证号',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系地址',
  `medical_history` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '病史/主诉',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`patient_id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '患者信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of psy_patient
-- ----------------------------
INSERT INTO `psy_patient` VALUES (1, 3, '测试用户', '1', 22, '13800000003', NULL, '重庆市渝北区', '近期入睡困难，多梦', '2026-09-14 17:06:33', '2026-09-14 17:06:33');
INSERT INTO `psy_patient` VALUES (2, NULL, '王小明', '0', 45, '13900000003', NULL, '重庆市江北区', '高血压病史，近期情绪焦虑', '2026-09-14 17:06:33', '2026-09-14 20:12:01');
INSERT INTO `psy_patient` VALUES (10, NULL, '测试校验', '0', 54, '13589652368', NULL, '重庆市巴南区', '111111', '2026-09-14 20:12:36', '2026-09-14 20:21:28');

-- ----------------------------
-- Table structure for psy_question
-- ----------------------------
DROP TABLE IF EXISTS `psy_question`;
CREATE TABLE `psy_question`  (
  `question_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `scale_id` bigint(20) NOT NULL COMMENT '所属量表ID',
  `sort_no` int(11) NOT NULL COMMENT '题号',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目内容',
  `reverse_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否反向计分(0否 1是)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`question_id`) USING BTREE,
  INDEX `idx_scale`(`scale_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 84 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '量表题目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of psy_question
-- ----------------------------
INSERT INTO `psy_question` VALUES (1, 1, 1, '我觉得比平时容易紧张或着急', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (2, 1, 2, '我无缘无故地感到害怕', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (3, 1, 3, '我容易心里烦乱或感到惊恐', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (4, 1, 4, '我觉得我可能将要发疯', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (5, 1, 5, '我觉得一切都很好，也不会发生什么不幸', 1, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (6, 1, 6, '我手脚发抖打颤', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (7, 1, 7, '我因为头疼、颈痛和背痛而苦恼', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (8, 1, 8, '我觉得容易衰弱和疲乏', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (9, 1, 9, '我觉得心平气和，并且容易安静坐着', 1, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (10, 1, 10, '我觉得心跳得很快', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (11, 1, 11, '我因为一阵阵头晕而苦恼', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (12, 1, 12, '我有过晕倒发作，或觉得要晕倒似的', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (13, 1, 13, '我呼气吸气都感到很容易', 1, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (14, 1, 14, '我的手脚麻木和刺痛', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (15, 1, 15, '我因为胃痛和消化不良而苦恼', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (16, 1, 16, '我常常要小便', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (17, 1, 17, '我的手脚常常是干燥温暖的', 1, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (18, 1, 18, '我脸红发热', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (19, 1, 19, '我容易入睡并且一夜睡得很好', 1, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (20, 1, 20, '我做恶梦', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (21, 2, 1, '我觉得闷闷不乐，情绪低沉', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (22, 2, 2, '我觉得一天之中早晨最好', 1, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (23, 2, 3, '我一阵阵哭出来或觉得想哭', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (24, 2, 4, '我晚上睡眠不好', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (25, 2, 5, '我吃得跟平常一样多', 1, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (26, 2, 6, '我与异性密切接触时和以往一样感到愉快', 1, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (27, 2, 7, '我发觉我的体重在下降', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (28, 2, 8, '我有便秘的苦恼', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (29, 2, 9, '我心跳比平时快', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (30, 2, 10, '我无缘无故地感到疲乏', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (31, 2, 11, '我的头脑跟平常一样清楚', 1, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (32, 2, 12, '我觉得经常做的事情并没有困难', 1, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (33, 2, 13, '我觉得不安而平静不下来', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (34, 2, 14, '我对将来抱有希望', 1, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (35, 2, 15, '我比平常容易生气激动', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (36, 2, 16, '我觉得作出决定是容易的', 1, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (37, 2, 17, '我觉得自己是个有用的人，有人需要我', 1, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (38, 2, 18, '我的生活过得很有意思', 1, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (39, 2, 19, '我认为如果我死了别人会生活得更好些', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (40, 2, 20, '平常感兴趣的事我仍然照样感兴趣', 1, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (41, 3, 1, '头痛', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (42, 3, 2, '神经过敏，心中不踏实', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (43, 3, 3, '头脑中有不必要的想法或字句盘旋', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (44, 3, 4, '头晕或晕倒', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (45, 3, 5, '对异性的兴趣减退', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (46, 3, 6, '对旁人责备求全', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (47, 3, 7, '感到别人能控制你的思想', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (48, 3, 8, '责怪别人制造麻烦', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (49, 3, 9, '忘性大', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (50, 3, 10, '担心自己的衣饰整齐及仪态的端正', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (51, 3, 11, '容易烦恼和激动', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (52, 3, 12, '胸痛', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (53, 3, 13, '害怕空旷的场所或街道', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (54, 3, 14, '感到自己的精力下降，活动减慢', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (55, 3, 15, '想结束自己的生命', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (56, 3, 16, '听到旁人听不到的声音', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (57, 3, 17, '发抖', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (58, 3, 18, '感到大多数人都不可信任', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (59, 3, 19, '胃口不好', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (60, 3, 20, '容易哭泣', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (61, 4, 1, '您觉得平时睡眠足够吗', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (62, 4, 2, '您睡眠后是否已充分休息、头脑清醒', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (63, 4, 3, '您是否总在白天犯困', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (64, 4, 4, '您是否经常失眠', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (65, 4, 5, '您是否在入睡后易醒或醒后难以再入睡', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (66, 4, 6, '您是否多梦或做噩梦', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (67, 4, 7, '您是否因睡眠问题感到焦虑', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (68, 4, 8, '您是否因睡眠不足影响工作或学习', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (69, 4, 9, '您是否经常服用安眠药', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (70, 4, 10, '您是否因睡眠问题就诊过', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (71, 4, 11, '您是否在睡前长时间使用电子产品', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (72, 4, 12, '您是否经常熬夜到凌晨以后', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (73, 4, 13, '您是否因环境噪音难以入睡', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (74, 4, 14, '您是否早上起床后仍感疲乏', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (75, 4, 15, '您是否在夜间频繁醒来', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (76, 4, 16, '您是否因思虑过多而难以入睡', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (77, 4, 17, '您是否午睡时间过长影响夜间睡眠', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (78, 4, 18, '您是否夜间腿部不适影响睡眠', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (79, 4, 19, '您是否白天靠咖啡或浓茶提神', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (80, 4, 20, '您是否感觉睡眠规律紊乱', 0, '2026-09-14 17:06:33');
INSERT INTO `psy_question` VALUES (82, 5, 1, '测试', 0, '2026-09-14 20:22:03');
INSERT INTO `psy_question` VALUES (83, 5, 1, '111111111111111', 0, '2026-09-14 20:22:23');

-- ----------------------------
-- Table structure for psy_scale
-- ----------------------------
DROP TABLE IF EXISTS `psy_scale`;
CREATE TABLE `psy_scale`  (
  `scale_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '量表ID',
  `scale_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '量表名称',
  `scale_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '量表编码(SAS/SDS/SCL90/SRSS)',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '量表说明',
  `option_type` tinyint(4) NOT NULL DEFAULT 4 COMMENT '选项评分制(4=四级评分 5=五级评分)',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态(0启用 1停用)',
  `sort_no` int(11) NULL DEFAULT 0 COMMENT '显示顺序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`scale_id`) USING BTREE,
  UNIQUE INDEX `uk_scale_code`(`scale_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '心理量表信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of psy_scale
-- ----------------------------
INSERT INTO `psy_scale` VALUES (1, '焦虑自评量表', 'SAS', '由 W.K.Zung 编制，用于评定焦虑症状的严重程度。四级评分共20题，标准分≥50提示存在焦虑倾向。', 4, '1', 1, '2026-09-14 17:06:33', '2026-09-14 17:11:10');
INSERT INTO `psy_scale` VALUES (2, '抑郁自评量表', 'SDS', '由 W.K.Zung 编制，用于评定抑郁症状的严重程度。四级评分共20题，标准分≥53提示存在抑郁倾向。', 4, '0', 2, '2026-09-14 17:06:33', '2026-09-14 17:06:33');
INSERT INTO `psy_scale` VALUES (3, '症状自评量表', 'SCL90', '包含较广泛的精神病症状学内容，从感觉、情感、思维、意识、行为直至生活习惯、人际关系、饮食睡眠等。五级评分。', 5, '0', 3, '2026-09-14 17:06:33', '2026-09-14 17:06:33');
INSERT INTO `psy_scale` VALUES (4, '睡眠状况自评量表', 'SRSS', '用于评定睡眠质量与睡眠障碍情况。五级评分，总分越高提示睡眠问题越突出。', 5, '0', 4, '2026-09-14 17:06:33', '2026-09-14 17:06:33');
INSERT INTO `psy_scale` VALUES (5, '111', 'crss', NULL, 4, '0', 0, '2026-09-14 17:07:05', '2026-09-14 17:07:05');

-- ----------------------------
-- Table structure for psy_test_answer
-- ----------------------------
DROP TABLE IF EXISTS `psy_test_answer`;
CREATE TABLE `psy_test_answer`  (
  `answer_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '答题ID',
  `record_id` bigint(20) NOT NULL COMMENT '测评记录ID',
  `question_id` bigint(20) NOT NULL COMMENT '题目ID',
  `option_value` int(11) NOT NULL COMMENT '选项分值',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '答题时间',
  PRIMARY KEY (`answer_id`) USING BTREE,
  INDEX `idx_record`(`record_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 81 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '答题明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of psy_test_answer
-- ----------------------------
INSERT INTO `psy_test_answer` VALUES (1, 1, 21, 4, '2026-09-14 19:30:32');
INSERT INTO `psy_test_answer` VALUES (2, 1, 22, 3, '2026-09-14 19:30:32');
INSERT INTO `psy_test_answer` VALUES (3, 1, 23, 2, '2026-09-14 19:30:32');
INSERT INTO `psy_test_answer` VALUES (4, 1, 24, 3, '2026-09-14 19:30:32');
INSERT INTO `psy_test_answer` VALUES (5, 1, 25, 4, '2026-09-14 19:30:32');
INSERT INTO `psy_test_answer` VALUES (6, 1, 26, 2, '2026-09-14 19:30:32');
INSERT INTO `psy_test_answer` VALUES (7, 1, 27, 3, '2026-09-14 19:30:32');
INSERT INTO `psy_test_answer` VALUES (8, 1, 28, 3, '2026-09-14 19:30:32');
INSERT INTO `psy_test_answer` VALUES (9, 1, 29, 4, '2026-09-14 19:30:32');
INSERT INTO `psy_test_answer` VALUES (10, 1, 30, 4, '2026-09-14 19:30:32');
INSERT INTO `psy_test_answer` VALUES (11, 1, 31, 2, '2026-09-14 19:30:32');
INSERT INTO `psy_test_answer` VALUES (12, 1, 32, 2, '2026-09-14 19:30:32');
INSERT INTO `psy_test_answer` VALUES (13, 1, 33, 2, '2026-09-14 19:30:32');
INSERT INTO `psy_test_answer` VALUES (14, 1, 34, 2, '2026-09-14 19:30:32');
INSERT INTO `psy_test_answer` VALUES (15, 1, 35, 3, '2026-09-14 19:30:32');
INSERT INTO `psy_test_answer` VALUES (16, 1, 36, 3, '2026-09-14 19:30:32');
INSERT INTO `psy_test_answer` VALUES (17, 1, 37, 4, '2026-09-14 19:30:32');
INSERT INTO `psy_test_answer` VALUES (18, 1, 38, 4, '2026-09-14 19:30:32');
INSERT INTO `psy_test_answer` VALUES (19, 1, 39, 2, '2026-09-14 19:30:32');
INSERT INTO `psy_test_answer` VALUES (20, 1, 40, 3, '2026-09-14 19:30:32');
INSERT INTO `psy_test_answer` VALUES (21, 2, 61, 1, '2026-09-14 19:43:00');
INSERT INTO `psy_test_answer` VALUES (22, 2, 62, 1, '2026-09-14 19:43:00');
INSERT INTO `psy_test_answer` VALUES (23, 2, 63, 1, '2026-09-14 19:43:00');
INSERT INTO `psy_test_answer` VALUES (24, 2, 64, 1, '2026-09-14 19:43:00');
INSERT INTO `psy_test_answer` VALUES (25, 2, 65, 1, '2026-09-14 19:43:00');
INSERT INTO `psy_test_answer` VALUES (26, 2, 66, 1, '2026-09-14 19:43:00');
INSERT INTO `psy_test_answer` VALUES (27, 2, 67, 1, '2026-09-14 19:43:00');
INSERT INTO `psy_test_answer` VALUES (28, 2, 68, 1, '2026-09-14 19:43:00');
INSERT INTO `psy_test_answer` VALUES (29, 2, 69, 1, '2026-09-14 19:43:00');
INSERT INTO `psy_test_answer` VALUES (30, 2, 70, 1, '2026-09-14 19:43:00');
INSERT INTO `psy_test_answer` VALUES (31, 2, 71, 1, '2026-09-14 19:43:00');
INSERT INTO `psy_test_answer` VALUES (32, 2, 72, 1, '2026-09-14 19:43:00');
INSERT INTO `psy_test_answer` VALUES (33, 2, 73, 1, '2026-09-14 19:43:00');
INSERT INTO `psy_test_answer` VALUES (34, 2, 74, 1, '2026-09-14 19:43:00');
INSERT INTO `psy_test_answer` VALUES (35, 2, 75, 1, '2026-09-14 19:43:00');
INSERT INTO `psy_test_answer` VALUES (36, 2, 76, 1, '2026-09-14 19:43:00');
INSERT INTO `psy_test_answer` VALUES (37, 2, 77, 1, '2026-09-14 19:43:00');
INSERT INTO `psy_test_answer` VALUES (38, 2, 78, 1, '2026-09-14 19:43:00');
INSERT INTO `psy_test_answer` VALUES (39, 2, 79, 1, '2026-09-14 19:43:00');
INSERT INTO `psy_test_answer` VALUES (40, 2, 80, 1, '2026-09-14 19:43:00');
INSERT INTO `psy_test_answer` VALUES (41, 3, 41, 5, '2026-09-14 19:43:27');
INSERT INTO `psy_test_answer` VALUES (42, 3, 42, 5, '2026-09-14 19:43:27');
INSERT INTO `psy_test_answer` VALUES (43, 3, 43, 5, '2026-09-14 19:43:27');
INSERT INTO `psy_test_answer` VALUES (44, 3, 44, 5, '2026-09-14 19:43:27');
INSERT INTO `psy_test_answer` VALUES (45, 3, 45, 5, '2026-09-14 19:43:27');
INSERT INTO `psy_test_answer` VALUES (46, 3, 46, 5, '2026-09-14 19:43:27');
INSERT INTO `psy_test_answer` VALUES (47, 3, 47, 5, '2026-09-14 19:43:27');
INSERT INTO `psy_test_answer` VALUES (48, 3, 48, 5, '2026-09-14 19:43:27');
INSERT INTO `psy_test_answer` VALUES (49, 3, 49, 5, '2026-09-14 19:43:27');
INSERT INTO `psy_test_answer` VALUES (50, 3, 50, 5, '2026-09-14 19:43:27');
INSERT INTO `psy_test_answer` VALUES (51, 3, 51, 5, '2026-09-14 19:43:27');
INSERT INTO `psy_test_answer` VALUES (52, 3, 52, 5, '2026-09-14 19:43:27');
INSERT INTO `psy_test_answer` VALUES (53, 3, 53, 5, '2026-09-14 19:43:27');
INSERT INTO `psy_test_answer` VALUES (54, 3, 54, 5, '2026-09-14 19:43:27');
INSERT INTO `psy_test_answer` VALUES (55, 3, 55, 5, '2026-09-14 19:43:27');
INSERT INTO `psy_test_answer` VALUES (56, 3, 56, 5, '2026-09-14 19:43:27');
INSERT INTO `psy_test_answer` VALUES (57, 3, 57, 5, '2026-09-14 19:43:27');
INSERT INTO `psy_test_answer` VALUES (58, 3, 58, 5, '2026-09-14 19:43:27');
INSERT INTO `psy_test_answer` VALUES (59, 3, 59, 5, '2026-09-14 19:43:27');
INSERT INTO `psy_test_answer` VALUES (60, 3, 60, 5, '2026-09-14 19:43:27');
INSERT INTO `psy_test_answer` VALUES (61, 4, 61, 1, '2026-09-14 19:44:44');
INSERT INTO `psy_test_answer` VALUES (62, 4, 62, 1, '2026-09-14 19:44:44');
INSERT INTO `psy_test_answer` VALUES (63, 4, 63, 5, '2026-09-14 19:44:44');
INSERT INTO `psy_test_answer` VALUES (64, 4, 64, 5, '2026-09-14 19:44:44');
INSERT INTO `psy_test_answer` VALUES (65, 4, 65, 5, '2026-09-14 19:44:44');
INSERT INTO `psy_test_answer` VALUES (66, 4, 66, 5, '2026-09-14 19:44:44');
INSERT INTO `psy_test_answer` VALUES (67, 4, 67, 5, '2026-09-14 19:44:44');
INSERT INTO `psy_test_answer` VALUES (68, 4, 68, 5, '2026-09-14 19:44:44');
INSERT INTO `psy_test_answer` VALUES (69, 4, 69, 5, '2026-09-14 19:44:44');
INSERT INTO `psy_test_answer` VALUES (70, 4, 70, 5, '2026-09-14 19:44:44');
INSERT INTO `psy_test_answer` VALUES (71, 4, 71, 5, '2026-09-14 19:44:44');
INSERT INTO `psy_test_answer` VALUES (72, 4, 72, 5, '2026-09-14 19:44:44');
INSERT INTO `psy_test_answer` VALUES (73, 4, 73, 5, '2026-09-14 19:44:44');
INSERT INTO `psy_test_answer` VALUES (74, 4, 74, 5, '2026-09-14 19:44:44');
INSERT INTO `psy_test_answer` VALUES (75, 4, 75, 5, '2026-09-14 19:44:44');
INSERT INTO `psy_test_answer` VALUES (76, 4, 76, 5, '2026-09-14 19:44:44');
INSERT INTO `psy_test_answer` VALUES (77, 4, 77, 5, '2026-09-14 19:44:44');
INSERT INTO `psy_test_answer` VALUES (78, 4, 78, 5, '2026-09-14 19:44:44');
INSERT INTO `psy_test_answer` VALUES (79, 4, 79, 5, '2026-09-14 19:44:44');
INSERT INTO `psy_test_answer` VALUES (80, 4, 80, 5, '2026-09-14 19:44:44');

-- ----------------------------
-- Table structure for psy_test_record
-- ----------------------------
DROP TABLE IF EXISTS `psy_test_record`;
CREATE TABLE `psy_test_record`  (
  `record_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint(20) NOT NULL COMMENT '测评用户ID',
  `scale_id` bigint(20) NOT NULL COMMENT '量表ID',
  `raw_score` int(11) NULL DEFAULT NULL COMMENT '原始粗分',
  `std_score` decimal(5, 1) NULL DEFAULT NULL COMMENT '标准分',
  `level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '结果等级(正常/轻度/中度/重度)',
  `suggestion` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '结果分析与建议',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '状态(1已完成)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '测评时间',
  PRIMARY KEY (`record_id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_scale`(`scale_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '心理测评记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of psy_test_record
-- ----------------------------
INSERT INTO `psy_test_record` VALUES (2, 3, 4, 20, 20.0, '正常', '睡眠状况总体良好，请继续保持规律作息，睡前减少电子产品使用，营造舒适的睡眠环境。', '1', '2026-09-14 19:43:00');
INSERT INTO `psy_test_record` VALUES (3, 3, 3, 100, 100.0, '重度症状', '存在重度心理症状。强烈建议尽快前往正规医疗机构精神心理科就诊，接受系统诊断与治疗；紧急时可拨打全国心理援助热线 12356 求助。', '1', '2026-09-14 19:43:27');
INSERT INTO `psy_test_record` VALUES (4, 3, 4, 92, 92.0, '重度睡眠问题', '存在重度睡眠问题。建议尽快前往医院睡眠医学科或心理精神科就诊，接受专业评估与治疗，避免长期依赖安眠药物。', '1', '2026-09-14 19:44:44');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色权限字符串',
  `sort_no` int(11) NULL DEFAULT 0 COMMENT '显示顺序',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '系统管理员', 'admin', 1, '拥有系统全部管理权限', '2026-09-14 17:06:33');
INSERT INTO `sys_role` VALUES (2, '临床医护人员', 'doctor', 2, '可查看患者、测评记录并给出诊断建议', '2026-09-14 17:06:33');
INSERT INTO `sys_role` VALUES (3, '公众用户', 'user', 3, '可进行心理测评并查看自己的测评报告', '2026-09-14 17:06:33');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码(BCrypt)',
  `nickname` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '2' COMMENT '用户性别(0男 1女 2未知)',
  `age` int(11) NULL DEFAULT NULL COMMENT '年龄',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号码',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '账号状态(0正常 1停用)',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '系统管理员', 1, '2', 28, '13800000001', '0', '0', '2026-09-14 17:06:33', '2026-09-14 17:06:33', '超级管理员');
INSERT INTO `sys_user` VALUES (2, 'doctor', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '张医生', 2, '0', 35, '13800000002', '0', '0', '2026-09-14 17:06:33', '2026-09-14 17:06:33', '临床医护人员');
INSERT INTO `sys_user` VALUES (3, 'user', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '测试用户', 3, '1', 22, '13800000003', '0', '0', '2026-09-14 17:06:33', '2026-09-14 17:06:33', '公众用户');
INSERT INTO `sys_user` VALUES (4, '测试一号', '$2a$10$iILqHbvK.LAnL7d/TbQssOuQQRJu3t5xsiWz/Oi.Tg4g63hsbM0Ni', '测试一号', 3, '1', 24, '13569874539', '0', '0', '2026-09-14 17:17:30', '2026-09-14 20:13:10', '自助注册用户');

SET FOREIGN_KEY_CHECKS = 1;
