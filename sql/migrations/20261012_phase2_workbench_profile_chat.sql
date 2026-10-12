-- 2026-10-12 二期：教师端沟通增强
-- 学生会话的处理状态、置顶与备注（会话以学生用户ID为归属键，与 psy_chat_message 对应）

CREATE TABLE IF NOT EXISTS psy_chat_conversation (
  student_id   BIGINT       NOT NULL COMMENT '学生会话归属（学生用户ID）',
  status       VARCHAR(16)  NOT NULL DEFAULT '待处理' COMMENT '处理状态（待处理/已处理/需转介）',
  pinned       TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶（0否 1是）',
  remark       VARCHAR(500) NULL COMMENT '处理备注',
  update_by    BIGINT       NULL COMMENT '最后更新人（sys_user.user_id）',
  update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生与教师沟通会话状态';
