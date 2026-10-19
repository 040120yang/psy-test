package com.psy.business.mapper;

import com.psy.business.domain.FollowUp;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface FollowUpMapper {
    List<FollowUp> selectList(@Param("userId") Long userId, @Param("status") String status);
    FollowUp selectById(@Param("id") Long id);
    int insert(FollowUp followUp);
    int update(FollowUp followUp);
    int deleteById(@Param("id") Long id);
    List<FollowUp> selectOverdue();

    /**
     * 写入随访提醒消息（站内消息表 psy_message）
     *
     * @param userId  接收学生用户ID
     * @param title   消息标题
     * @param content 消息内容
     * @param type    消息类型
     * @return 影响行数
     */
    int insertNotice(@Param("userId") Long userId,
                     @Param("title") String title,
                     @Param("content") String content,
                     @Param("type") String type);
}
