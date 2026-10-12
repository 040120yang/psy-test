package com.psy.business.controller;

import com.psy.common.core.AjaxResult;
import com.psy.common.core.TableDataInfo;
import com.psy.common.exception.ServiceException;
import com.psy.common.utils.StringUtils;
import com.psy.framework.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 学生端与教师端沟通（会话式聊天）。
 *
 * 会话以「学生用户ID」为归属键，一名学生对应一个与教师沟通的会话。
 * 权限规则：
 *   1) 学生只能查看与发送自己的会话；
 *   2) 心理教师（doctor）只能查看/回复自己负责的学生；尚未指定负责教师的会话，
 *      对全部心理教师可见并标记为「待分配」，任一教师回复后自动接手成为该学生的负责教师；
 *   3) 系统管理员（admin）可查看全部会话（管理员职责，便于监督与协助）。
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    /** 单条消息最大长度 */
    private static final int MAX_CONTENT_LENGTH = 2000;

    /** 默认返回的最近消息条数 */
    private static final int DEFAULT_LIMIT = 200;

    private static final String ROLE_STUDENT = "student";
    private static final String ROLE_TEACHER = "teacher";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ==================== 学生端 ====================

    /**
     * 学生端：获取我的会话（负责教师信息 + 消息列表）
     */
    @GetMapping("/my")
    public AjaxResult my(@RequestParam(required = false) Integer limit) {
        if (!SecurityUtils.isUser()) {
            throw new ServiceException("仅学生用户可查看自己的沟通会话");
        }
        Long studentId = SecurityUtils.getUserId();
        Map<String, Object> data = new HashMap<>();
        data.put("teacher", findTeacher(studentId));
        data.put("messages", listMessages(studentId, limit));
        return AjaxResult.success(data);
    }

    // ==================== 教师端 ====================

    /**
     * 教师端：我会话列表（含最后一条消息、未读数、是否待分配）
     */
    @GetMapping("/conversations")
    public TableDataInfo conversations() {
        requireTeacher();
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        sql.append("SELECT p.user_id AS studentId, u.nickname AS studentName, u.username AS studentUsername, ")
           .append("p.teacher_id AS teacherId, tu.nickname AS teacherName, ")
           .append("(SELECT m.content FROM psy_chat_message m WHERE m.student_id = p.user_id ORDER BY m.id DESC LIMIT 1) AS lastMessage, ")
           .append("(SELECT m.sender_role FROM psy_chat_message m WHERE m.student_id = p.user_id ORDER BY m.id DESC LIMIT 1) AS lastSenderRole, ")
           .append("(SELECT m.create_time FROM psy_chat_message m WHERE m.student_id = p.user_id ORDER BY m.id DESC LIMIT 1) AS lastTime, ")
           .append("(SELECT COUNT(*) FROM psy_chat_message m WHERE m.student_id = p.user_id AND m.sender_role = 'student' AND m.is_read = 0) AS unread ")
           .append("FROM psy_patient p ")
           .append("LEFT JOIN sys_user u ON p.user_id = u.user_id ")
           .append("LEFT JOIN sys_user tu ON p.teacher_id = tu.user_id ")
           .append("WHERE p.user_id IS NOT NULL ");
        // 心理教师：仅自己负责的学生 + 待分配的会话；管理员：全部
        if (!SecurityUtils.isAdmin()) {
            sql.append("AND (p.teacher_id = ? OR p.teacher_id IS NULL) ");
            params.add(SecurityUtils.getUserId());
        }
        sql.append("ORDER BY unread DESC, COALESCE(lastTime, p.create_time) DESC ");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        // 同一学生可能存在多条档案，按 studentId 去重（保留未读较多/较新的那条）
        Map<Long, Map<String, Object>> unique = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Object sid = row.get("studentId");
            if (sid == null) continue;
            Long studentId = Long.valueOf(sid.toString());
            if (!unique.containsKey(studentId)) {
                unique.put(studentId, row);
            }
        }
        List<Map<String, Object>> list = new ArrayList<>(unique.values());
        return TableDataInfo.success(list, list.size());
    }

    /**
     * 教师端：查看指定学生的会话消息
     */
    @GetMapping("/detail")
    public AjaxResult detail(@RequestParam Long studentId) {
        requireTeacher();
        Map<String, Object> student = findStudent(studentId);
        requireStudentAccess(studentId, student);
        Map<String, Object> data = new HashMap<>();
        data.put("student", student);
        data.put("messages", listMessages(studentId, null));
        return AjaxResult.success(data);
    }

    // ==================== 公共 ====================

    /**
     * 发送消息（学生端与教师端共用）
     */
    @PostMapping("/send")
    public AjaxResult send(@RequestBody Map<String, Object> body) {
        String content = body.get("content") == null ? null : body.get("content").toString().trim();
        if (StringUtils.isEmpty(content)) {
            throw new ServiceException("消息内容不能为空");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new ServiceException("消息内容不能超过 " + MAX_CONTENT_LENGTH + " 个字符");
        }

        Long studentId;
        String senderRole;
        if (SecurityUtils.isUser()) {
            // 学生只能给自己所属的会话发送
            studentId = SecurityUtils.getUserId();
            senderRole = ROLE_STUDENT;
        } else {
            requireTeacher();
            Object sid = body.get("studentId");
            if (sid == null || StringUtils.isEmpty(sid.toString())) {
                throw new ServiceException("请指定接收消息的学生");
            }
            try {
                studentId = Long.valueOf(sid.toString().trim());
            } catch (NumberFormatException e) {
                throw new ServiceException("学生ID格式不正确");
            }
            Map<String, Object> student = findStudent(studentId);
            requireStudentAccess(studentId, student);
            // 心理教师回复待分配会话时自动接手，成为该学生的负责教师
            if (!SecurityUtils.isAdmin() && student.get("teacherId") == null) {
                jdbcTemplate.update("UPDATE psy_patient SET teacher_id = ? WHERE user_id = ?",
                        SecurityUtils.getUserId(), studentId);
            }
            senderRole = ROLE_TEACHER;
        }

        jdbcTemplate.update(
                "INSERT INTO psy_chat_message (student_id, sender_id, sender_role, content, is_read) VALUES (?, ?, ?, ?, 0)",
                studentId, SecurityUtils.getUserId(), senderRole, content);

        AjaxResult result = AjaxResult.success("发送成功");
        result.put("studentId", studentId);
        result.put("senderRole", senderRole);
        return result;
    }

    /**
     * 当前用户的未读消息数（学生：教师发来的未读；教师：学生发来的未读合计）
     */
    @GetMapping("/unread")
    public AjaxResult unread() {
        if (SecurityUtils.isUser()) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM psy_chat_message WHERE student_id = ? AND sender_role = 'teacher' AND is_read = 0",
                    Integer.class, SecurityUtils.getUserId());
            return AjaxResult.success(count == null ? 0 : count);
        }
        requireTeacher();
        Integer count;
        if (SecurityUtils.isAdmin()) {
            count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM psy_chat_message c JOIN psy_patient p ON c.student_id = p.user_id " +
                    "WHERE c.sender_role = 'student' AND c.is_read = 0", Integer.class);
        } else {
            count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM psy_chat_message c JOIN psy_patient p ON c.student_id = p.user_id " +
                    "WHERE c.sender_role = 'student' AND c.is_read = 0 AND (p.teacher_id = ? OR p.teacher_id IS NULL)",
                    Integer.class, SecurityUtils.getUserId());
        }
        return AjaxResult.success(count == null ? 0 : count);
    }

    /**
     * 标记已读（学生：标记教师发来的消息；教师：标记指定学生发来的消息）
     */
    @PutMapping("/read")
    public AjaxResult read(@RequestBody(required = false) Map<String, Object> body) {
        if (SecurityUtils.isUser()) {
            jdbcTemplate.update(
                    "UPDATE psy_chat_message SET is_read = 1 WHERE student_id = ? AND sender_role = 'teacher' AND is_read = 0",
                    SecurityUtils.getUserId());
            return AjaxResult.success();
        }
        requireTeacher();
        Object sid = body == null ? null : body.get("studentId");
        if (sid == null || StringUtils.isEmpty(sid.toString())) {
            throw new ServiceException("请指定学生");
        }
        Long studentId = Long.valueOf(sid.toString().trim());
        requireStudentAccess(studentId, findStudent(studentId));
        jdbcTemplate.update(
                "UPDATE psy_chat_message SET is_read = 1 WHERE student_id = ? AND sender_role = 'student' AND is_read = 0",
                studentId);
        return AjaxResult.success();
    }

    // ==================== 内部方法 ====================

    /** 查询学生的会话消息（按时间正序） */
    private List<Map<String, Object>> listMessages(Long studentId, Integer limit) {
        int size = (limit == null || limit <= 0 || limit > 500) ? DEFAULT_LIMIT : limit;
        return jdbcTemplate.queryForList(
                "SELECT c.id, c.student_id AS studentId, c.sender_id AS senderId, c.sender_role AS senderRole, " +
                "c.content, c.is_read AS isRead, c.create_time AS createTime, " +
                "COALESCE(NULLIF(u.nickname, ''), u.username) AS senderName " +
                "FROM psy_chat_message c LEFT JOIN sys_user u ON c.sender_id = u.user_id " +
                "WHERE c.student_id = ? ORDER BY c.id ASC LIMIT " + size, studentId);
    }

    /** 查询学生的负责教师信息 */
    private Map<String, Object> findTeacher(Long studentId) {
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT p.teacher_id AS teacherId, COALESCE(NULLIF(tu.nickname, ''), tu.username) AS teacherName " +
                "FROM psy_patient p LEFT JOIN sys_user tu ON p.teacher_id = tu.user_id " +
                "WHERE p.user_id = ? LIMIT 1", studentId);
        return list.isEmpty() ? null : list.get(0);
    }

    /** 查询学生（含负责教师）信息 */
    private Map<String, Object> findStudent(Long studentId) {
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT p.user_id AS studentId, u.nickname AS studentName, u.username AS studentUsername, " +
                "p.teacher_id AS teacherId, COALESCE(NULLIF(tu.nickname, ''), tu.username) AS teacherName " +
                "FROM psy_patient p LEFT JOIN sys_user u ON p.user_id = u.user_id " +
                "LEFT JOIN sys_user tu ON p.teacher_id = tu.user_id " +
                "WHERE p.user_id = ? LIMIT 1", studentId);
        if (list.isEmpty()) {
            throw new ServiceException("学生不存在");
        }
        return list.get(0);
    }

    /** 校验当前教师是否有权访问该学生会话 */
    private void requireStudentAccess(Long studentId, Map<String, Object> student) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        Object teacherId = student.get("teacherId");
        // 未指定负责教师时，全部心理教师可查看并接手
        if (teacherId == null) {
            return;
        }
        if (!SecurityUtils.getUserId().equals(Long.valueOf(teacherId.toString()))) {
            throw new ServiceException("该学生由其他心理教师负责，无权查看其沟通记录");
        }
    }

    /** 校验当前用户是否为教师侧（心理教师或系统管理员） */
    private void requireTeacher() {
        if (!SecurityUtils.isDoctor() && !SecurityUtils.isAdmin()) {
            throw new ServiceException("无权限操作，仅心理教师或系统管理员可使用沟通中心");
        }
    }
}
