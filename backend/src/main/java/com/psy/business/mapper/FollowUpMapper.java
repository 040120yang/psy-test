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
}
