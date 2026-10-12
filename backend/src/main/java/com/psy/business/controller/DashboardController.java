package com.psy.business.controller;

import com.psy.business.service.DashboardService;
import com.psy.common.core.AjaxResult;
import com.psy.common.exception.ServiceException;
import com.psy.framework.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统概览（统计面板）+ 待办工作台（J-01）
 *
 * 待办口径：心理教师仅统计自己负责的学生（沿用沟通模块规则，含尚未指派负责教师的学生）；
 * 系统管理员为全局口径。
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    /** 各待办列表最多返回条数 */
    private static final int LIST_LIMIT = 10;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 统计概览数据
     */
    @GetMapping("/stats")
    public AjaxResult stats() {
        requireStaff();
        Map<String, Object> stats = dashboardService.stats();
        return AjaxResult.success(stats);
    }

    /**
     * 待办工作台：待处理沟通、逾期随访、待填写评估结论、本周新增中/重度测评
     */
    @GetMapping("/workbench")
    public AjaxResult workbench() {
        requireStaff();
        Map<String, Object> data = new HashMap<>();
        data.put("scope", SecurityUtils.isAdmin() ? "all" : "teacher");
        data.put("pendingChat", pendingChat());
        data.put("overdueFollow", overdueFollow());
        data.put("pendingEvaluation", pendingEvaluation());
        data.put("weekRisk", weekRisk());
        return AjaxResult.success(data);
    }

    // ==================== 各待办区块 ====================

    /** 待处理沟通：学生发来且未读的消息，按学生聚合 */
    private Map<String, Object> pendingChat() {
        List<Object> countParams = new ArrayList<>();
        String countSql = "SELECT COUNT(*) FROM psy_chat_message c WHERE c.sender_role = 'student' AND c.is_read = 0"
                + scopeOf("c.student_id", countParams);
        Integer total = jdbcTemplate.queryForObject(countSql, Integer.class, countParams.toArray());

        List<Object> listParams = new ArrayList<>();
        String listSql = "SELECT g.studentId, g.unread, g.lastTime, u.nickname AS studentName, u.username AS studentUsername, "
                + "(SELECT m.content FROM psy_chat_message m WHERE m.student_id = g.studentId ORDER BY m.id DESC LIMIT 1) AS lastContent "
                + "FROM (SELECT c.student_id AS studentId, COUNT(*) AS unread, MAX(c.create_time) AS lastTime "
                + "      FROM psy_chat_message c WHERE c.sender_role = 'student' AND c.is_read = 0 "
                + "      GROUP BY c.student_id) g "
                + "LEFT JOIN sys_user u ON g.studentId = u.user_id "
                + "WHERE 1 = 1" + scopeOf("g.studentId", listParams)
                + " ORDER BY g.lastTime DESC LIMIT " + LIST_LIMIT;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(listSql, listParams.toArray());

        Map<String, Object> result = new HashMap<>();
        result.put("total", total == null ? 0 : total);
        result.put("rows", rows);
        return result;
    }

    /** 逾期随访：随访日期已过且未完成 */
    private Map<String, Object> overdueFollow() {
        List<Object> countParams = new ArrayList<>();
        String countSql = "SELECT COUNT(*) FROM psy_follow_up f WHERE f.status <> '已完成' AND f.follow_date < CURDATE()"
                + scopeOf("f.user_id", countParams);
        Integer total = jdbcTemplate.queryForObject(countSql, Integer.class, countParams.toArray());

        List<Object> listParams = new ArrayList<>();
        String listSql = "SELECT f.id, f.user_id AS userId, f.record_id AS recordId, f.follow_date AS followDate, "
                + "f.follow_type AS followType, f.status, u.nickname AS studentName, u.username AS studentUsername "
                + "FROM psy_follow_up f LEFT JOIN sys_user u ON f.user_id = u.user_id "
                + "WHERE f.status <> '已完成' AND f.follow_date < CURDATE()" + scopeOf("f.user_id", listParams)
                + " ORDER BY f.follow_date ASC LIMIT " + LIST_LIMIT;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(listSql, listParams.toArray());

        Map<String, Object> result = new HashMap<>();
        result.put("total", total == null ? 0 : total);
        result.put("rows", rows);
        return result;
    }

    /** 待填写评估结论：中/重度测评但心理教师尚未填写评估结论 */
    private Map<String, Object> pendingEvaluation() {
        String condition = " WHERE (tr.doctor_conclusion IS NULL OR tr.doctor_conclusion = '') "
                + "AND (tr.level LIKE '%中度%' OR tr.level LIKE '%重度%')";

        List<Object> countParams = new ArrayList<>();
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM psy_test_record tr" + condition + scopeOf("tr.user_id", countParams),
                Integer.class, countParams.toArray());

        List<Object> listParams = new ArrayList<>();
        String listSql = "SELECT tr.record_id AS recordId, tr.user_id AS userId, tr.level, tr.std_score AS stdScore, "
                + "tr.create_time AS createTime, ps.scale_name AS scaleName, u.nickname AS studentName, u.username AS studentUsername "
                + "FROM psy_test_record tr "
                + "LEFT JOIN psy_scale ps ON tr.scale_id = ps.scale_id "
                + "LEFT JOIN sys_user u ON tr.user_id = u.user_id" + condition + scopeOf("tr.user_id", listParams)
                + " ORDER BY tr.create_time DESC LIMIT " + LIST_LIMIT;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(listSql, listParams.toArray());

        Map<String, Object> result = new HashMap<>();
        result.put("total", total == null ? 0 : total);
        result.put("rows", rows);
        return result;
    }

    /** 本周新增中/重度测评 */
    private Map<String, Object> weekRisk() {
        LocalDateTime weekStart = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        String condition = " WHERE (tr.level LIKE '%中度%' OR tr.level LIKE '%重度%') AND tr.create_time >= ?";

        List<Object> countParams = new ArrayList<>();
        countParams.add(weekStart);
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM psy_test_record tr" + condition + scopeOf("tr.user_id", countParams),
                Integer.class, countParams.toArray());

        List<Object> listParams = new ArrayList<>();
        listParams.add(weekStart);
        String listSql = "SELECT tr.record_id AS recordId, tr.user_id AS userId, tr.level, tr.std_score AS stdScore, "
                + "tr.create_time AS createTime, ps.scale_name AS scaleName, u.nickname AS studentName, u.username AS studentUsername "
                + "FROM psy_test_record tr "
                + "LEFT JOIN psy_scale ps ON tr.scale_id = ps.scale_id "
                + "LEFT JOIN sys_user u ON tr.user_id = u.user_id" + condition + scopeOf("tr.user_id", listParams)
                + " ORDER BY tr.create_time DESC LIMIT " + LIST_LIMIT;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(listSql, listParams.toArray());

        Map<String, Object> result = new HashMap<>();
        result.put("total", total == null ? 0 : total);
        result.put("rows", rows);
        return result;
    }

    // ==================== 内部方法 ====================

    /**
     * 生成可见范围过滤片段：心理教师仅可见自己负责的学生（含未指派负责教师的学生），管理员不过滤。
     * 传入的 params 会按 SQL 中占位符出现的顺序追加参数。
     */
    private String scopeOf(String userColumn, List<Object> params) {
        if (SecurityUtils.isAdmin()) {
            return "";
        }
        params.add(SecurityUtils.getUserId());
        return " AND " + userColumn + " IN (SELECT p.user_id FROM psy_patient p "
                + "WHERE p.user_id IS NOT NULL AND (p.teacher_id = ? OR p.teacher_id IS NULL))";
    }

    /** 校验当前用户是否为心理教师或系统管理员 */
    private void requireStaff() {
        if (!SecurityUtils.isDoctor() && !SecurityUtils.isAdmin()) {
            throw new ServiceException("无权限查看，仅心理教师或系统管理员可访问");
        }
    }
}
