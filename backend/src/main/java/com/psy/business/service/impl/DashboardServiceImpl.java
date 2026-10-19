package com.psy.business.service.impl;

import com.psy.business.mapper.DashboardMapper;
import com.psy.business.service.DashboardService;
import com.psy.framework.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统概览与待办工作台业务实现
 *
 * <p>统计口径：心理教师仅统计本人负责以及尚未指派负责教师的学生，系统管理员为全局口径。</p>
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    /** 各待办列表最多返回条数 */
    private static final int LIST_LIMIT = 10;

    @Autowired
    private DashboardMapper dashboardMapper;

    @Override
    public Map<String, Object> stats() {
        Map<String, Object> result = new HashMap<>();
        result.put("userCount", dashboardMapper.countUser());
        result.put("recordCount", dashboardMapper.countRecord());
        result.put("scaleCount", dashboardMapper.countScale());
        result.put("patientCount", dashboardMapper.countPatient());
        result.put("todayCount", dashboardMapper.countToday());
        result.put("scaleDist", dashboardMapper.scaleDist());
        result.put("levelDist", dashboardMapper.levelDist());
        result.put("weekTrend", dashboardMapper.weekTrend());
        result.put("followTotal", dashboardMapper.followTotal());
        result.put("followDone", dashboardMapper.followDone());
        result.put("followOverdue", dashboardMapper.followOverdue());
        result.put("tagDist", dashboardMapper.tagDist());
        return result;
    }

    @Override
    public Map<String, Object> workbench() {
        boolean admin = SecurityUtils.isAdmin();
        Long teacherId = admin ? null : SecurityUtils.getUserId();

        Map<String, Object> data = new HashMap<>();
        data.put("scope", admin ? "all" : "teacher");
        data.put("pendingChat", pendingChat(teacherId));
        data.put("overdueFollow", overdueFollow(teacherId));
        data.put("pendingEvaluation", pendingEvaluation(teacherId));
        data.put("weekRisk", weekRisk(teacherId));
        return data;
    }

    /**
     * 待处理沟通：学生发来且未读的消息，按学生聚合
     *
     * @param teacherId 教师ID；为空表示全局口径
     * @return total 与 rows
     */
    private Map<String, Object> pendingChat(Long teacherId) {
        Map<String, Object> block = new HashMap<>();
        block.put("total", dashboardMapper.countPendingChat(teacherId));
        block.put("rows", dashboardMapper.selectPendingChat(teacherId, LIST_LIMIT));
        return block;
    }

    /**
     * 逾期随访：随访日期已过且未完成
     *
     * @param teacherId 教师ID；为空表示全局口径
     * @return total 与 rows
     */
    private Map<String, Object> overdueFollow(Long teacherId) {
        Map<String, Object> block = new HashMap<>();
        block.put("total", dashboardMapper.countOverdueFollow(teacherId));
        block.put("rows", dashboardMapper.selectOverdueFollow(teacherId, LIST_LIMIT));
        return block;
    }

    /**
     * 待填写评估结论：中/重度测评且尚未填写评估结论
     *
     * @param teacherId 教师ID；为空表示全局口径
     * @return total 与 rows
     */
    private Map<String, Object> pendingEvaluation(Long teacherId) {
        Map<String, Object> block = new HashMap<>();
        block.put("total", dashboardMapper.countPendingEvaluation(teacherId));
        block.put("rows", dashboardMapper.selectPendingEvaluation(teacherId, LIST_LIMIT));
        return block;
    }

    /**
     * 本周新增中/重度测评：以本周一 00:00 为界
     *
     * @param teacherId 教师ID；为空表示全局口径
     * @return total 与 rows
     */
    private Map<String, Object> weekRisk(Long teacherId) {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        Date weekStart = Date.from(monday.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Map<String, Object> block = new HashMap<>();
        block.put("total", dashboardMapper.countWeekRisk(teacherId, weekStart));
        block.put("rows", dashboardMapper.selectWeekRisk(teacherId, weekStart, LIST_LIMIT));
        return block;
    }
}
