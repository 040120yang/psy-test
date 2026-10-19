package com.psy.business.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 系统概览与待办工作台数据层
 *

 */
@Mapper
public interface DashboardMapper {

    //  概览统计

    /** 有效用户数 */
    int countUser();

    /** 测评记录总数 */
    int countRecord();

    /** 启用量表数 */
    int countScale();

    /** 学生档案数 */
    int countPatient();

    /** 今日测评数 */
    int countToday();

    /** 各量表测评次数分布 */
    List<Map<String, Object>> scaleDist();

    /** 结果等级分布 */
    List<Map<String, Object>> levelDist();

    /** 近 7 天测评趋势 */
    List<Map<String, Object>> weekTrend();

    /** 随访总数 */
    int followTotal();

    /** 已完成随访数 */
    int followDone();

    /** 逾期随访数（概览口径） */
    int followOverdue();

    /** 学生标签分布 */
    List<Map<String, Object>> tagDist();

    // ==================== 待办工作台 ====================

    /**
     * 待处理沟通条数（学生发来且未读）
     *
     * @param teacherId 教师ID；为空表示全局口径（管理员）
     * @return 条数
     */
    int countPendingChat(@Param("teacherId") Long teacherId);

    /**
     * 待处理沟通明细，按学生聚合
     *
     * @param teacherId 教师ID；为空表示全局口径
     * @param limit     返回条数上限
     * @return 每个学生一行，含未读条数与最后一条消息
     */
    List<Map<String, Object>> selectPendingChat(@Param("teacherId") Long teacherId,
                                                @Param("limit") int limit);

    /**
     * 逾期随访条数（未完成且随访日期早于当天）
     *
     * @param teacherId 教师ID；为空表示全局口径
     * @return 条数
     */
    int countOverdueFollow(@Param("teacherId") Long teacherId);

    /**
     * 逾期随访明细，按随访日期升序
     *
     * @param teacherId 教师ID；为空表示全局口径
     * @param limit     返回条数上限
     * @return 随访记录列表
     */
    List<Map<String, Object>> selectOverdueFollow(@Param("teacherId") Long teacherId,
                                                  @Param("limit") int limit);

    /**
     * 待填写评估结论条数（中/重度测评且未填写评估结论）
     *
     * @param teacherId 教师ID；为空表示全局口径
     * @return 条数
     */
    int countPendingEvaluation(@Param("teacherId") Long teacherId);

    /**
     * 待填写评估结论明细，按测评时间倒序
     *
     * @param teacherId 教师ID；为空表示全局口径
     * @param limit     返回条数上限
     * @return 测评记录列表
     */
    List<Map<String, Object>> selectPendingEvaluation(@Param("teacherId") Long teacherId,
                                                      @Param("limit") int limit);

    /**
     * 本周新增中/重度测评条数
     *
     * @param teacherId 教师ID；为空表示全局口径
     * @param weekStart 本周一 00:00:00
     * @return 条数
     */
    int countWeekRisk(@Param("teacherId") Long teacherId,
                      @Param("weekStart") Date weekStart);

    /**
     * 本周新增中/重度测评明细，按测评时间倒序
     *
     * @param teacherId 教师ID；为空表示全局口径
     * @param weekStart 本周一 00:00:00
     * @param limit     返回条数上限
     * @return 测评记录列表
     */
    List<Map<String, Object>> selectWeekRisk(@Param("teacherId") Long teacherId,
                                             @Param("weekStart") Date weekStart,
                                             @Param("limit") int limit);
}
