package com.psy.business.mapper;

import com.psy.business.domain.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 量表题目数据层
 */
@Mapper
public interface QuestionMapper {

    /** 根据量表ID查询题目（按题号排序） */
    @Select("select * from psy_question where scale_id = #{scaleId} order by sort_no")
    List<Question> selectByScaleId(@Param("scaleId") Long scaleId);

    /** 分页/条件查询题目列表（含量表名称） */
    List<Question> selectQuestionList(@Param("question") Question question);

    /** 根据ID查询题目 */
    @Select("select * from psy_question where question_id = #{questionId}")
    Question selectQuestionById(@Param("questionId") Long questionId);

    /** 新增题目 */
    int insertQuestion(Question question);

    /** 修改题目 */
    int updateQuestion(Question question);

    /** 删除题目 */
    @Delete("delete from psy_question where question_id = #{questionId}")
    int deleteQuestionById(@Param("questionId") Long questionId);

    /** 删除量表下全部题目 */
    @Delete("delete from psy_question where scale_id = #{scaleId}")
    int deleteByScaleId(@Param("scaleId") Long scaleId);
}
