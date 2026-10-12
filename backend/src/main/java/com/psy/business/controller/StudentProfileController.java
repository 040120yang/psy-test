package com.psy.business.controller;

import com.psy.common.core.AjaxResult;
import com.psy.common.exception.ServiceException;
import com.psy.framework.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生个人案档案
 *
 * 将分散在「测评记录 / 随访管理 / 沟通中心」的信息按学生聚合，供心理教师整体判断。
 * 权限：心理教师仅可查看自己负责的学生（含尚未指派负责教师的学生）；系统管理员为全部。
 */
@RestController
@RequestMapping("/student")
public class StudentProfileController {

    /** 沟通记录最多返回条数 */
    private static final int CHAT_LIMIT = 50;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 学生个案档案：基本信息 + 测评记录 + 评估结论 + 随访记录 + 沟通记录
     */
    @GetMapping("/profile")
    public AjaxResult profile(@RequestParam Long studentId) {
        requireStaff();
        Map<String, Object> student = findStudent(studentId);
        checkAccess(student);

        List<Map<String, Object>> records = jdbcTemplate.queryForList(
                "SELECT tr.record_id AS recordId, tr.scale_id AS scaleId, ps.scale_name AS scaleName, "
                + "ps.scale_code AS scaleCode, tr.raw_score AS rawScore, tr.std_score AS stdScore, tr.level, "
                + "tr.suggestion, tr.doctor_conclusion AS doctorConclusion, tr.doctor_advice AS doctorAdvice, "
                + "tr.create_time AS createTime "
                + "FROM psy_test_record tr LEFT JOIN psy_scale ps ON tr.scale_id = ps.scale_id "
                + "WHERE tr.user_id = ? ORDER BY tr.create_time DESC", studentId);

        List<Map<String, Object>> followUps = jdbcTemplate.queryForList(
                "SELECT id, record_id AS recordId, follow_date AS followDate, follow_type AS followType, status, "
                + "symptom_score AS symptomScore, doctor_note AS doctorNote, next_follow_date AS nextFollowDate, "
                + "create_time AS createTime FROM psy_follow_up WHERE user_id = ? ORDER BY follow_date DESC", studentId);

        List<Map<String, Object>> chats = jdbcTemplate.queryForList(
                "SELECT t.id, t.senderRole, t.content, t.createTime, t.senderName FROM ("
                + "  SELECT c.id, c.sender_role AS senderRole, c.content, c.create_time AS createTime, "
                + "         COALESCE(NULLIF(u.nickname, ''), u.username) AS senderName "
                + "  FROM psy_chat_message c LEFT JOIN sys_user u ON c.sender_id = u.user_id "
                + "  WHERE c.student_id = ? ORDER BY c.id DESC LIMIT " + CHAT_LIMIT
                + ") t ORDER BY t.id ASC", studentId);

        int riskCount = 0;
        for (Map<String, Object> r : records) {
            Object level = r.get("level");
            String text = level == null ? "" : level.toString();
            if (text.contains("中度") || text.contains("重度")) {
                riskCount++;
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("recordCount", records.size());
        summary.put("riskCount", riskCount);
        summary.put("followCount", followUps.size());
        summary.put("chatCount", chats.size());
        summary.put("lastTestTime", records.isEmpty() ? null : records.get(0).get("createTime"));

        Map<String, Object> data = new HashMap<>();
        data.put("student", student);
        data.put("summary", summary);
        data.put("records", records);
        data.put("followUps", followUps);
        data.put("chats", chats);
        return AjaxResult.success(data);
    }

    // ==================== 内部方法 ====================

    private Map<String, Object> findStudent(Long studentId) {
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT p.user_id AS studentId, COALESCE(NULLIF(u.nickname, ''), u.username) AS studentName, "
                + "u.username AS studentUsername, p.patient_name AS patientName, p.sex, p.age, p.phone, "
                + "p.id_card AS idCard, p.address, p.medical_history AS medicalHistory, p.tag, "
                + "p.teacher_id AS teacherId, COALESCE(NULLIF(tu.nickname, ''), tu.username) AS teacherName, "
                + "p.create_time AS createTime "
                + "FROM psy_patient p "
                + "LEFT JOIN sys_user u ON p.user_id = u.user_id "
                + "LEFT JOIN sys_user tu ON p.teacher_id = tu.user_id "
                + "WHERE p.user_id = ? LIMIT 1", studentId);
        if (list.isEmpty()) {
            throw new ServiceException("学生不存在或未关联登录账号");
        }
        return list.get(0);
    }

    /** 心理教师仅可查看自己负责的学生（含未指派负责教师的学生），管理员不受限 */
    private void checkAccess(Map<String, Object> student) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        Object teacherId = student.get("teacherId");
        if (teacherId == null) {
            return;
        }
        if (!SecurityUtils.getUserId().equals(Long.valueOf(teacherId.toString()))) {
            throw new ServiceException("该学生由其他心理教师负责，无权查看其个案档案");
        }
    }

    private void requireStaff() {
        if (!SecurityUtils.isDoctor() && !SecurityUtils.isAdmin()) {
            throw new ServiceException("无权限查看，仅心理教师或系统管理员可查看个案档案");
        }
    }
}
