package com.psy.business.mapper;

import com.psy.business.domain.TestRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 测评记录数据层
 */
@Mapper
public interface TestRecordMapper {

    /** 新增记录（回填记录ID） */
    int insertRecord(TestRecord record);

    /** 根据ID查询记录详情（联查量表名/用户名） */
    @Select("select tr.*, ps.scale_name, ps.scale_code, su.nickname, su.username " +
            "from psy_test_record tr " +
            "left join psy_scale ps on tr.scale_id = ps.scale_id " +
            "left join sys_user su on tr.user_id = su.user_id " +
            "where tr.record_id = #{recordId}")
    TestRecord selectRecordById(@Param("recordId") Long recordId);

    /** 我的测评记录（分页） */
    List<TestRecord> selectMyRecords(@Param("userId") Long userId);

    /** 全部测评记录（分页，可按量表/等级/用户名筛选） */
    List<TestRecord> selectAllRecords(@Param("record") TestRecord record);

    /** 删除记录 */
    @Delete("delete from psy_test_record where record_id = #{recordId}")
    int deleteRecordById(@Param("recordId") Long recordId);
}
