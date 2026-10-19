package com.psy.business.service;

import com.psy.business.domain.Knowledge;
import com.psy.business.domain.Message;

import java.util.List;
import java.util.Map;

/**
 * 消息中心、心理知识库、心理评估与系统日志业务接口
 *

 */
public interface BusinessService {

    //  消息中心

    /**
     * 学生端：查询我的消息（含全局广播）
     *
     * @return 消息列表，按创建时间倒序
     */
    List<Message> myMessages();

    /**
     * 学生端：标记单条消息已读
     *
     * @param id 消息ID
     */
    void readMessage(Long id);

    /**
     * 学生端：未读消息数
     *
     * @return 未读条数
     */
    int unreadCount();

    /**
     * 管理端：下发消息
     *
     * @param message 消息对象
     */
    void sendMessage(Message message);

    //心理知识库

    /**
     * 文章列表（仅已发布）
     *
     * @param category 分类，为空表示全部
     * @return 文章列表
     */
    List<Knowledge> knowledgeList(String category);

    /**
     * 文章详情，学生仅可查看已发布内容
     *
     * @param id 文章ID
     * @return 文章
     */
    Knowledge knowledgeDetail(Long id);

    /**
     * 管理端：新增文章
     *
     * @param knowledge 文章对象
     */
    void addKnowledge(Knowledge knowledge);

    /**
     * 管理端：修改文章
     *
     * @param knowledge 文章对象，需含主键
     */
    void updateKnowledge(Knowledge knowledge);

    /**
     * 管理端：删除文章
     *
     * @param id 文章ID
     */
    void deleteKnowledge(Long id);

    // 心理评估

    /**
     * 管理端：保存心理评估结论与关怀建议，并通知记录所属学生
     *
     * @param body 请求体，含 recordId、conclusion、advice
     */
    void saveDiagnosis(Map<String, Object> body);

    // 历史对比

    /**
     * 学生端：最近若干次测评结果，用于趋势对比
     *
     * @return 测评结果列表
     */
    List<Map<String, Object>> compareHistory();

    //  系统日志
    /**
     * 操作日志列表，仅系统管理员可查看
     *
     * @return 操作日志列表
     */
    List<Map<String, Object>> operLog();

    /**
     * 登录日志列表，仅系统管理员可查看
     *
     * @return 登录日志列表
     */
    List<Map<String, Object>> loginLog();
}
