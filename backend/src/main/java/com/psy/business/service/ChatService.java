package com.psy.business.service;

import java.util.List;
import java.util.Map;

/**
 * 学生与教师沟通业务层
 *

 */
public interface ChatService {

    /**
     * 学生端：本人会话（负责教师 + 消息列表）
     *
     * @param limit 返回消息条数上限，可为空
     * @return teacher 与 messages
     */
    Map<String, Object> my(Integer limit);

    /**
     * 教师端：会话列表（按学生去重）
     *
     * @return 会话列表，含最后消息、未读数、处理状态与置顶标记
     */
    List<Map<String, Object>> conversations();

    /**
     * 教师端：指定学生的会话详情
     *
     * @param studentId 学生用户ID
     * @return student 与 messages
     */
    Map<String, Object> detail(Long studentId);

    /**
     * 设置会话处理状态、置顶与备注（未传字段保持原值）
     *
     * @param body 含 studentId、status、pinned、remark
     */
    void updateConversation(Map<String, Object> body);

    /**
     * 发送消息（学生端与教师端共用）
     *
     * @param body 含 content；教师端还需 studentId
     * @return studentId 与 senderRole
     */
    Map<String, Object> send(Map<String, Object> body);

    /**
     * 当前用户的未读消息数
     *
     * @return 未读条数
     */
    int unread();

    /**
     * 标记已读（教师端需指定 studentId）
     *
     * @param body 可为空
     */
    void read(Map<String, Object> body);
}
