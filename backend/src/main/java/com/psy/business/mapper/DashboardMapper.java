package com.psy.business.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 系统概览统计数据层
 */
@Mapper
public interface DashboardMapper {

    @Select("select count(1) from sys_user where del_flag = '0'")
    int countUser();

    @Select("select count(1) from psy_test_record")
    int countRecord();

    @Select("select count(1) from psy_scale where status = '0'")
    int countScale();

    @Select("select count(1) from psy_patient")
    int countPatient();

    @Select("select count(1) from psy_test_record where date(create_time) = curdate()")
    int countToday();

    /** 各量表测评次数分布 */
    @Select("select ps.scale_name as name, count(tr.record_id) as value " +
            "from psy_test_record tr left join psy_scale ps on tr.scale_id = ps.scale_id " +
            "group by tr.scale_id, ps.scale_name order by value desc")
    List<Map<String, Object>> scaleDist();

    /** 结果等级分布 */
    @Select("select ifnull(tr.level, '未知') as name, count(1) as value " +
            "from psy_test_record tr group by tr.level order by value desc")
    List<Map<String, Object>> levelDist();

    /** 近7天测评趋势 */
    @Select("select date(create_time) as day, count(1) as value " +
            "from psy_test_record " +
            "where create_time >= date_sub(curdate(), interval 6 day) " +
            "group by date(create_time) order by day")
    List<Map<String, Object>> weekTrend();

    /** 随访统计 */
    @Select("select count(1) from psy_follow_up")
    int followTotal();

    @Select("select count(1) from psy_follow_up where status = '已完成'")
    int followDone();

    @Select("select count(1) from psy_follow_up where status = '待随访' and follow_date < curdate()")
    int followOverdue();

    /** 患者标签统计 */
    @Select("select ifnull(tag, '未分类') as name, count(1) as value from psy_patient group by tag")
    List<Map<String, Object>> tagDist();
}
