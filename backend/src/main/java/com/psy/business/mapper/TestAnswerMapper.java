package com.psy.business.mapper;

import com.psy.business.domain.TestAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 答题明细数据层
 */
@Mapper
public interface TestAnswerMapper {

    /** 批量新增答题明细 */
    int batchInsert(@Param("list") List<TestAnswer> answers);

    /** 根据记录ID查询答题明细（联查题目内容） */
    @Select("select ta.*, q.content, q.sort_no " +
            "from psy_test_answer ta " +
            "left join psy_question q on ta.question_id = q.question_id " +
            "where ta.record_id = #{recordId} order by q.sort_no")
    List<TestAnswer> selectByRecordId(@Param("recordId") Long recordId);
}
