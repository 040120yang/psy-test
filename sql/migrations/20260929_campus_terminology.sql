-- 校园化术语更新
-- 保留角色编码和接口路径，仅调整管理端展示名称与数据库说明。
SET NAMES utf8mb4;

UPDATE sys_role SET role_name = '心理教师/辅导员' WHERE role_key = 'doctor';
UPDATE sys_role SET role_name = '学生用户' WHERE role_key = 'user';
UPDATE sys_role SET remark = '可查看学生档案、测评记录、心理评估与随访建议' WHERE role_key = 'doctor';
UPDATE sys_role SET remark = '可进行心理测评并查看自己的测评报告与随访安排' WHERE role_key = 'user';

ALTER TABLE psy_patient COMMENT = '学生心理档案表';
ALTER TABLE psy_follow_up COMMENT = '学生心理随访表';

ALTER TABLE psy_patient
  MODIFY COLUMN patient_id bigint NOT NULL AUTO_INCREMENT COMMENT '学生档案ID',
  MODIFY COLUMN user_id bigint DEFAULT NULL COMMENT '关联学生账号ID',
  MODIFY COLUMN patient_name varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学生姓名',
  MODIFY COLUMN medical_history varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '既往经历/主要困扰',
  MODIFY COLUMN tag varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '学生标签';

ALTER TABLE psy_follow_up
  MODIFY COLUMN user_id bigint NOT NULL COMMENT '学生ID',
  MODIFY COLUMN record_id bigint DEFAULT NULL COMMENT '关联测评记录ID',
  MODIFY COLUMN doctor_id bigint DEFAULT NULL COMMENT '负责心理教师ID',
  MODIFY COLUMN symptom_score int DEFAULT NULL COMMENT '状态自评(0-10)',
  MODIFY COLUMN doctor_note varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '心理教师随访记录';

ALTER TABLE psy_patient
  MODIFY COLUMN id_card varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '学号/证件号',
  MODIFY COLUMN address varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系地址';

ALTER TABLE psy_follow_up
  MODIFY COLUMN id bigint NOT NULL AUTO_INCREMENT COMMENT '随访ID',
  MODIFY COLUMN follow_date date NOT NULL COMMENT '随访日期',
  MODIFY COLUMN follow_type varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '线上' COMMENT '随访方式',
  MODIFY COLUMN status varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '待随访' COMMENT '随访状态',
  MODIFY COLUMN next_follow_date date DEFAULT NULL COMMENT '下次随访日期',
  MODIFY COLUMN create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
