-- 2026-10-12 学生端与教师端沟通功能
-- 1) 学生档案增加「负责教师」字段
-- 2) 新建学生与教师沟通消息表

ALTER TABLE psy_patient
  ADD COLUMN teacher_id BIGINT NULL COMMENT '负责教师用户ID（sys_user.user_id）' AFTER tag;

CREATE TABLE IF NOT EXISTS psy_chat_message (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  student_id   BIGINT       NOT NULL COMMENT '学生会话归属（学生用户ID）',
  sender_id    BIGINT       NOT NULL COMMENT '发送人用户ID',
  sender_role  VARCHAR(16)  NOT NULL COMMENT '发送人角色（student教师为学生/teacher为教师）',
  content      TEXT         NOT NULL COMMENT '消息内容',
  is_read      TINYINT      NOT NULL DEFAULT 0 COMMENT '对方是否已读（0未读 1已读）',
  create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (id),
  KEY idx_student_time (student_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生与教师沟通消息';
