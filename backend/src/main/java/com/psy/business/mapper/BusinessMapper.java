package com.psy.business.mapper;

import com.psy.business.domain.Knowledge;
import com.psy.business.domain.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 消息中心、心理知识库、心理评估与系统日志数据层
 *

 */
@Mapper
public interface BusinessMapper {

    //  消息中心

    /**
     * 查询学生可见的消息
     *
     * @param userId 学生用户ID；同时包含 user_id 为空的全局广播
     * @return 消息列表，按创建时间倒序
     */
    List<Message> selectMyMessages(@Param("userId") Long userId);

    /**
     * 将单条消息标记为已读
     *
     * @param id 消息ID
     * @return 影响行数
     */
    int markMessageRead(@Param("id") Long id);

    /**
     * 统计学生未读消息数
     *
     * @param userId 学生用户ID
     * @return 未读条数
     */
    int countUnreadMessages(@Param("userId") Long userId);

    /**
     * 写入一条消息
     *
     * @param message 消息对象，含接收人、标题、内容与类型
     * @return 影响行数
     */
    int insertMessage(Message message);

    //  心理知识库

    /**
     * 查询已发布文章
     *
     * @param category 文章分类，为空表示不限分类
     * @return 文章列表，按创建时间倒序
     */
    List<Knowledge> selectKnowledgeList(@Param("category") String category);

    /**
     * 按主键查询文章
     *
     * <p>不过滤发布状态，是否允许查看由业务层判断。</p>
     *
     * @param id 文章ID
     * @return 文章，不存在时返回 null
     */
    Knowledge selectKnowledgeById(@Param("id") Long id);

    /**
     * 浏览量自增
     *
     * @param id 文章ID
     * @return 影响行数
     */
    int increaseKnowledgeView(@Param("id") Long id);

    /**
     * 新增文章
     *
     * @param knowledge 文章对象，发布状态与浏览量取表默认值
     * @return 影响行数
     */
    int insertKnowledge(Knowledge knowledge);

    /**
     * 修改文章正文信息
     *
     * @param knowledge 文章对象，需含主键
     * @return 影响行数
     */
    int updateKnowledge(Knowledge knowledge);

    /**
     * 删除文章
     *
     * @param id 文章ID
     * @return 影响行数
     */
    int deleteKnowledge(@Param("id") Long id);

    // ==================== 心理评估 ====================

    /**
     * 查询测评记录的归属人
     *
     * @param recordId 测评记录ID
     * @return recordId、userId；记录不存在时返回 null
     */
    Map<String, Object> selectRecordOwner(@Param("recordId") Long recordId);

    /**
     * 保存心理评估结论与关怀建议
     *
     * @param recordId   测评记录ID
     * @param conclusion 评估结论
     * @param advice     关怀建议
     * @return 影响行数
     */
    int updateDiagnosis(@Param("recordId") Long recordId,
                        @Param("conclusion") String conclusion,
                        @Param("advice") String advice);

    // ==================== 历史对比 ====================

    /**
     * 查询学生最近若干次测评结果，用于折线对比
     *
     * @param userId 学生用户ID
     * @param limit  返回条数上限
     * @return 测评结果列表，按创建时间倒序
     */
    List<Map<String, Object>> selectCompareHistory(@Param("userId") Long userId,
                                                   @Param("limit") int limit);

    // ==================== 系统日志 ====================

    /**
     * 查询操作日志
     *
     * @param limit 返回条数上限
     * @return 操作日志列表，按操作时间倒序
     */
    List<Map<String, Object>> selectOperLog(@Param("limit") int limit);

    /**
     * 查询登录日志
     *
     * @param limit 返回条数上限
     * @return 登录日志列表，按登录时间倒序
     */
    List<Map<String, Object>> selectLoginLog(@Param("limit") int limit);
}
