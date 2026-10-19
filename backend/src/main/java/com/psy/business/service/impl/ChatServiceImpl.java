package com.psy.business.service.impl;

import com.psy.business.mapper.ChatMapper;
import com.psy.business.service.ChatService;
import com.psy.common.exception.ServiceException;
import com.psy.common.utils.StringUtils;
import com.psy.framework.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生与教师沟通业务实现
 *

 */
@Service
public class ChatServiceImpl implements ChatService {

    /** 单条消息最大长度 */
    private static final int MAX_CONTENT_LENGTH = 2000;

    /** 默认返回的最近消息条数 */
    private static final int DEFAULT_LIMIT = 200;

    /** 单次查询消息条数上限 */
    private static final int MAX_LIMIT = 500;

    private static final String ROLE_STUDENT = "student";
    private static final String ROLE_TEACHER = "teacher";

    /** 会话默认处理状态 */
    private static final String STATUS_PENDING = "待处理";
    private static final String STATUS_HANDLED = "已处理";
    private static final String STATUS_REFERRAL = "需转介";

    @Autowired
    private ChatMapper chatMapper;

    @Override
    public Map<String, Object> my(Integer limit) {
        if (!SecurityUtils.isUser()) {
            throw new ServiceException("仅学生用户可查看自己的沟通会话");
        }
        Long studentId = SecurityUtils.getUserId();
        Map<String, Object> data = new HashMap<>();
        data.put("teacher", chatMapper.selectTeacher(studentId));
        data.put("messages", listMessages(studentId, limit));
        return data;
    }

    @Override
    public List<Map<String, Object>> conversations() {
        requireTeacher();
        Long teacherId = SecurityUtils.isAdmin() ? null : SecurityUtils.getUserId();
        List<Map<String, Object>> rows = chatMapper.selectConversations(teacherId);
        // 同一学生可能存在多条档案，按学生去重（保留首条，即未读较多或较新的那条）
        Map<Long, Map<String, Object>> unique = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Object studentId = row.get("studentId");
            if (studentId == null) {
                continue;
            }
            unique.putIfAbsent(Long.valueOf(studentId.toString()), row);
        }
        return new ArrayList<>(unique.values());
    }

    @Override
    public Map<String, Object> detail(Long studentId) {
        requireTeacher();
        Map<String, Object> student = findStudent(studentId);
        requireStudentAccess(student);
        Map<String, Object> data = new HashMap<>();
        data.put("student", student);
        data.put("messages", listMessages(studentId, null));
        return data;
    }

    @Override
    public void updateConversation(Map<String, Object> body) {
        requireTeacher();
        Long studentId = parseStudentId(body == null ? null : body.get("studentId"), "请指定学生");
        requireStudentAccess(findStudent(studentId));

        Map<String, Object> current = chatMapper.selectConversation(studentId);
        String status = body.get("status") != null
                ? body.get("status").toString().trim()
                : (current == null ? STATUS_PENDING : String.valueOf(current.get("status")));
        if (!STATUS_PENDING.equals(status) && !STATUS_HANDLED.equals(status) && !STATUS_REFERRAL.equals(status)) {
            throw new ServiceException("会话状态取值不正确，仅支持待处理/已处理/需转介");
        }

        Integer pinned = resolvePinned(body.get("pinned"), current);
        String remark = body.get("remark") != null
                ? body.get("remark").toString()
                : (current == null ? null : (String) current.get("remark"));

        chatMapper.saveConversation(studentId, status, pinned, remark, SecurityUtils.getUserId());
    }

    @Override
    public Map<String, Object> send(Map<String, Object> body) {
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
            studentId = parseStudentId(body.get("studentId"), "请指定接收消息的学生");
            Map<String, Object> student = findStudent(studentId);
            requireStudentAccess(student);
            // 心理教师回复待分配会话时自动接手，成为该学生的负责教师
            if (!SecurityUtils.isAdmin() && student.get("teacherId") == null) {
                chatMapper.updateStudentTeacher(studentId, SecurityUtils.getUserId());
            }
            senderRole = ROLE_TEACHER;
        }

        chatMapper.insertMessage(studentId, SecurityUtils.getUserId(), senderRole, content);

        Map<String, Object> result = new HashMap<>();
        result.put("studentId", studentId);
        result.put("senderRole", senderRole);
        return result;
    }

    @Override
    public int unread() {
        if (SecurityUtils.isUser()) {
            return chatMapper.countUnreadForStudent(SecurityUtils.getUserId());
        }
        requireTeacher();
        if (SecurityUtils.isAdmin()) {
            return chatMapper.countUnreadForAdmin();
        }
        return chatMapper.countUnreadForTeacher(SecurityUtils.getUserId());
    }

    @Override
    public void read(Map<String, Object> body) {
        if (SecurityUtils.isUser()) {
            chatMapper.markReadForStudent(SecurityUtils.getUserId());
            return;
        }
        requireTeacher();
        Long studentId = parseStudentId(body == null ? null : body.get("studentId"), "请指定学生");
        requireStudentAccess(findStudent(studentId));
        chatMapper.markReadForTeacher(studentId);
    }

    // ==================== 内部方法 ====================

    /**
     * 查询会话消息
     *
     * @param studentId 学生用户ID
     * @param limit     请求条数，空或越界时使用默认值
     * @return 消息列表
     */
    private List<Map<String, Object>> listMessages(Long studentId, Integer limit) {
        int size = (limit == null || limit <= 0 || limit > MAX_LIMIT) ? DEFAULT_LIMIT : limit;
        return chatMapper.selectMessages(studentId, size);
    }

    /**
     * 查询学生，不存在时抛出业务异常
     *
     * @param studentId 学生用户ID
     * @return 学生信息
     */
    private Map<String, Object> findStudent(Long studentId) {
        Map<String, Object> student = chatMapper.selectStudent(studentId);
        if (student == null) {
            throw new ServiceException("学生不存在");
        }
        return student;
    }

    /**
     * 校验当前教师是否有权访问该学生会话
     *
     * @param student 学生信息（含 teacherId）
     */
    private void requireStudentAccess(Map<String, Object> student) {
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

    /**
     * 解析请求中的学生ID
     *
     * @param value       原始值
     * @param emptyMessage 为空时的提示语
     * @return 学生用户ID
     */
    private Long parseStudentId(Object value, String emptyMessage) {
        if (value == null || StringUtils.isEmpty(value.toString())) {
            throw new ServiceException(emptyMessage);
        }
        try {
            return Long.valueOf(value.toString().trim());
        } catch (NumberFormatException e) {
            throw new ServiceException("学生ID格式不正确");
        }
    }

    /**
     * 解析置顶标记：未传时保持原值
     *
     * @param value   请求值
     * @param current 当前会话状态记录，可为空
     * @return 0 或 1
     */
    private Integer resolvePinned(Object value, Map<String, Object> current) {
        if (value == null) {
            return current == null ? 0 : ((Number) current.get("pinned")).intValue();
        }
        String text = value.toString();
        return ("1".equals(text) || "true".equalsIgnoreCase(text)) ? 1 : 0;
    }

    /** 校验当前用户是否为教师侧（心理教师或系统管理员） */
    private void requireTeacher() {
        if (!SecurityUtils.isDoctor() && !SecurityUtils.isAdmin()) {
            throw new ServiceException("无权限操作，仅心理教师或系统管理员可使用沟通中心");
        }
    }
}
