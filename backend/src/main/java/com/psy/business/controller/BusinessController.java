package com.psy.business.controller;

import com.psy.business.domain.Knowledge;
import com.psy.business.domain.Message;
import com.psy.common.core.AjaxResult;
import com.psy.common.core.TableDataInfo;
import com.psy.common.exception.ServiceException;
import com.psy.common.utils.StringUtils;
import com.psy.framework.aspectj.annotation.OperLog;
import com.psy.framework.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 消息中心 + 心理知识库
 */
@RestController
@RequestMapping("/business")
public class BusinessController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ========== 消息中心 ==========

    /** 学生端：我的消息 */
    @GetMapping("/message/my")
    public TableDataInfo myMessages() {
        Long userId = SecurityUtils.getUserId();
        String sql = "SELECT * FROM psy_message WHERE user_id = ? OR user_id IS NULL ORDER BY create_time DESC";
        List<Message> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Message.class), userId);
        return TableDataInfo.success(list, list.size());
    }

    /** 学生端：标记已读 */
    @PutMapping("/message/read/{id}")
    public AjaxResult readMessage(@PathVariable Long id) {
        jdbcTemplate.update("UPDATE psy_message SET is_read = 1 WHERE id = ?", id);
        return AjaxResult.success();
    }

    /** 学生端：未读消息数 */
    @GetMapping("/message/unread")
    public AjaxResult unreadCount() {
        Long userId = SecurityUtils.getUserId();
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM psy_message WHERE (user_id = ? OR user_id IS NULL) AND is_read = 0",
            Integer.class, userId);
        return AjaxResult.success(count);
    }

    /** 管理端：发送消息（仅心理教师或系统管理员可下发） */
    @OperLog(title = "消息管理", businessType = OperLog.INSERT)
    @PostMapping("/message/send")
    public AjaxResult sendMessage(@RequestBody Message message) {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.isDoctor()) {
            throw new ServiceException("无权限操作，仅心理教师或系统管理员可下发通知");
        }
        if (StringUtils.isEmpty(message.getTitle())) {
            throw new ServiceException("消息标题不能为空");
        }
        if (StringUtils.isEmpty(message.getContent())) {
            throw new ServiceException("消息内容不能为空");
        }
        String sql = "INSERT INTO psy_message (user_id, title, content, type) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, message.getUserId(), message.getTitle(), message.getContent(), message.getType());
        return AjaxResult.success();
    }

    // ========== 心理知识库 ==========

    /** 文章列表 */
    @GetMapping("/knowledge/list")
    public TableDataInfo knowledgeList(@RequestParam(required = false) String category) {
        String sql = "SELECT * FROM psy_knowledge WHERE status = 1";
        List<Object> params = new ArrayList<>();
        if (category != null && !category.isEmpty()) {
            sql += " AND category = ?";
            params.add(category);
        }
        sql += " ORDER BY create_time DESC";
        List<Knowledge> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Knowledge.class), params.toArray());
        return TableDataInfo.success(list, list.size());
    }

    /** 文章详情（学生仅可查看已发布内容，避免读到草稿或已下架文章） */
    @GetMapping("/knowledge/{id}")
    public AjaxResult knowledgeDetail(@PathVariable Long id) {
        List<Knowledge> list = jdbcTemplate.query(
            "SELECT * FROM psy_knowledge WHERE id = ?",
            new BeanPropertyRowMapper<>(Knowledge.class), id);
        if (list.isEmpty()) {
            throw new ServiceException("文章不存在");
        }
        Knowledge k = list.get(0);
        boolean staff = SecurityUtils.isAdmin() || SecurityUtils.isDoctor();
        if (!staff && (k.getStatus() == null || k.getStatus() != 1)) {
            throw new ServiceException("文章不存在或已下架");
        }
        jdbcTemplate.update("UPDATE psy_knowledge SET view_count = view_count + 1 WHERE id = ?", id);
        return AjaxResult.success(k);
    }

    /** 管理端：新增文章 */
    @OperLog(title = "知识库管理", businessType = OperLog.INSERT)
    @PostMapping("/knowledge")
    public AjaxResult addKnowledge(@RequestBody Knowledge k) {
        requireAdminOrDoctor();
        jdbcTemplate.update(
            "INSERT INTO psy_knowledge (title, category, summary, content, author) VALUES (?, ?, ?, ?, ?)",
            k.getTitle(), k.getCategory(), k.getSummary(), k.getContent(), k.getAuthor());
        return AjaxResult.success();
    }

    /** 管理端：修改文章 */
    @OperLog(title = "知识库管理", businessType = OperLog.UPDATE)
    @PutMapping("/knowledge")
    public AjaxResult updateKnowledge(@RequestBody Knowledge k) {
        requireAdminOrDoctor();
        jdbcTemplate.update(
            "UPDATE psy_knowledge SET title = ?, category = ?, summary = ?, content = ? WHERE id = ?",
            k.getTitle(), k.getCategory(), k.getSummary(), k.getContent(), k.getId());
        return AjaxResult.success();
    }

    /** 管理端：删除文章 */
    @OperLog(title = "知识库管理", businessType = OperLog.DELETE)
    @DeleteMapping("/knowledge/{id}")
    public AjaxResult deleteKnowledge(@PathVariable Long id) {
        requireAdminOrDoctor();
        jdbcTemplate.update("DELETE FROM psy_knowledge WHERE id = ?", id);
        return AjaxResult.success();
    }

    // ========== 心理评估/关怀建议 ==========

    /** 管理端：填写心理评估结论与关怀建议（保存后自动通知该记录所属学生） */
    @OperLog(title = "测评记录-心理评估", businessType = OperLog.UPDATE)
    @PutMapping("/record/diagnosis")
    public AjaxResult saveDiagnosis(@RequestBody Map<String, Object> body) {
        // 仅系统管理员或心理教师/医护人员可填写心理评估
        if (!SecurityUtils.isAdmin() && !SecurityUtils.isDoctor()) {
            throw new ServiceException("无权限操作，仅系统管理员或心理教师可填写心理评估");
        }

        Object recordIdObj = body.get("recordId");
        if (recordIdObj == null || StringUtils.isEmpty(recordIdObj.toString())) {
            throw new ServiceException("测评记录ID不能为空");
        }
        Long recordId;
        try {
            recordId = Long.valueOf(recordIdObj.toString().trim());
        } catch (NumberFormatException e) {
            throw new ServiceException("测评记录ID格式不正确");
        }
        String conclusion = body.get("conclusion") == null ? null : body.get("conclusion").toString();
        String advice = body.get("advice") == null ? null : body.get("advice").toString();

        // 反查记录归属用户，确保评估意见推送给正确的学生
        List<Map<String, Object>> records = jdbcTemplate.queryForList(
            "SELECT record_id AS recordId, user_id AS userId FROM psy_test_record WHERE record_id = ?", recordId);
        if (records.isEmpty()) {
            throw new ServiceException("测评记录不存在");
        }
        Object userIdObj = records.get(0).get("userId");
        Long userId = userIdObj == null ? null : Long.valueOf(userIdObj.toString());

        jdbcTemplate.update(
            "UPDATE psy_test_record SET doctor_conclusion = ?, doctor_advice = ? WHERE record_id = ?",
            conclusion, advice, recordId);

        // 自动向该学生推送测评评估结果通知（消息中心可接收）
        if (userId != null) {
            try {
                StringBuilder content = new StringBuilder("您的测评记录已完成心理教师评估。");
                if (StringUtils.isNotEmpty(conclusion)) {
                    content.append("评估结论：").append(conclusion).append("；");
                }
                if (StringUtils.isNotEmpty(advice)) {
                    content.append("关怀建议：").append(advice).append("；");
                }
                content.append("详情可在“我的记录”中查看。");
                jdbcTemplate.update(
                    "INSERT INTO psy_message (user_id, title, content, type) VALUES (?, ?, ?, 'result')",
                    userId, "测评评估结果通知", content.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return AjaxResult.success("评估建议已保存");
    }

    // ========== 历史对比 ==========

    /** 学生端：历史对比数据 */
    @GetMapping("/record/compare")
    public AjaxResult compareHistory() {
        Long userId = SecurityUtils.getUserId();
        String sql = "SELECT r.record_id as recordId, s.scale_name as scaleName, s.scale_code as scaleCode, " +
                     "r.std_score as stdScore, r.level, r.create_time as createTime " +
                     "FROM psy_test_record r LEFT JOIN psy_scale s ON r.scale_id = s.scale_id " +
                     "WHERE r.user_id = ? ORDER BY r.create_time DESC LIMIT 20";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, userId);
        return AjaxResult.success(list);
    }

    // ========== 日志查询 ==========

    /** 操作日志列表 */
    @GetMapping("/log/oper")
    public TableDataInfo operLog() {
        requireAdmin();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
            "SELECT oper_id as operId, title, business_type as businessType, " +
            "oper_name as operName, oper_url as operUrl, " +
            "oper_ip as operIp, status, error_msg as errorMsg, oper_time as operTime " +
            "FROM sys_oper_log ORDER BY oper_time DESC LIMIT 200");
        return TableDataInfo.success(list, list.size());
    }

    /** 登录日志列表 */
    @GetMapping("/log/login")
    public TableDataInfo loginLog() {
        requireAdmin();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
            "SELECT info_id as infoId, user_name as userName, ipaddr, status, msg, " +
            "login_time as loginTime FROM sys_login_log ORDER BY login_time DESC LIMIT 200");
        return TableDataInfo.success(list, list.size());
    }

    private void requireAdmin() {
        if (!SecurityUtils.isAdmin()) {
            throw new ServiceException("无权限查看系统日志");
        }
    }

    private void requireAdminOrDoctor() {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.isDoctor()) {
            throw new ServiceException("无权限操作，仅系统管理员或医护人员可管理知识库");
        }
    }
}
