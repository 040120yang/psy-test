package com.psy.business.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 学生与教师沟通数据层
 *

 */
@Mapper
public interface ChatMapper {

    /**
     * 教师端会话列表
     *
     * @param teacherId 教师ID；为空表示管理员（可见全部）
     * @return 每个学生一行，含最后一条消息、未读数、处理状态与置顶标记
     */
    List<Map<String, Object>> selectConversations(@Param("teacherId") Long teacherId);

    /**
     * 查询学生的负责教师
     *
     * @param studentId 学生用户ID
     * @return teacherId、teacherName；无档案时返回 null
     */
    Map<String, Object> selectTeacher(@Param("studentId") Long studentId);

    /**
     * 查询学生基本信息与负责教师
     *
     * @param studentId 学生用户ID
     * @return studentId、studentName、studentUsername、teacherId、teacherName；不存在时返回 null
     */
    Map<String, Object> selectStudent(@Param("studentId") Long studentId);

    /**
     * 查询会话消息（按时间正序）
     *
     * @param studentId 学生用户ID
     * @param limit     返回条数上限
     * @return 消息列表
     */
    List<Map<String, Object>> selectMessages(@Param("studentId") Long studentId,
                                             @Param("limit") int limit);

    /**
     * 写入一条消息
     *
     * @param studentId  会话归属学生
     * @param senderId   发送人用户ID
     * @param senderRole 发送人角色（student / teacher）
     * @param content    消息内容
     * @return 影响行数
     */
    int insertMessage(@Param("studentId") Long studentId,
                      @Param("senderId") Long senderId,
                      @Param("senderRole") String senderRole,
                      @Param("content") String content);

    /**
     * 更新学生的负责教师（教师回复未分配会话时自动接手）
     *
     * @param studentId 学生用户ID
     * @param teacherId 教师用户ID
     * @return 影响行数
     */
    int updateStudentTeacher(@Param("studentId") Long studentId,
                             @Param("teacherId") Long teacherId);

    /**
     * 学生端标记已读：把教师发来的未读消息置为已读
     *
     * @param studentId 学生用户ID
     * @return 影响行数
     */
    int markReadForStudent(@Param("studentId") Long studentId);

    /**
     * 教师端标记已读：把指定学生发来的未读消息置为已读
     *
     * @param studentId 学生用户ID
     * @return 影响行数
     */
    int markReadForTeacher(@Param("studentId") Long studentId);

    /**
     * 学生端未读数（教师发来的未读消息）
     *
     * @param studentId 学生用户ID
     * @return 未读条数
     */
    int countUnreadForStudent(@Param("studentId") Long studentId);

    /**
     * 教师端未读数（负责范围内学生发来的未读消息）
     *
     * @param teacherId 教师用户ID
     * @return 未读条数
     */
    int countUnreadForTeacher(@Param("teacherId") Long teacherId);

    /**
     * 管理员未读数（全部学生会话中未读的学生消息）
     *
     * @return 未读条数
     */
    int countUnreadForAdmin();

    /**
     * 查询会话状态记录
     *
     * @param studentId 学生用户ID
     * @return status、pinned、remark；未设置过时返回 null
     */
    Map<String, Object> selectConversation(@Param("studentId") Long studentId);

    /**
     * 保存会话状态（存在即更新，不存在则插入）
     *
     * @param studentId 学生用户ID
     * @param status    处理状态
     * @param pinned    是否置顶（0/1）
     * @param remark    处理备注
     * @param updateBy  最后更新人
     * @return 影响行数
     */
    int saveConversation(@Param("studentId") Long studentId,
                         @Param("status") String status,
                         @Param("pinned") Integer pinned,
                         @Param("remark") String remark,
                         @Param("updateBy") Long updateBy);
}
