package com.psy.business.controller;

import com.psy.business.domain.Knowledge;
import com.psy.business.domain.Message;
import com.psy.common.core.AjaxResult;
import com.psy.common.core.TableDataInfo;
import com.psy.framework.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

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

    /** 用户端：我的消息 */
    @GetMapping("/message/my")
    public TableDataInfo myMessages() {
        Long userId = SecurityUtils.getUserId();
        String sql = "SELECT * FROM psy_message WHERE user_id = ? OR user_id IS NULL ORDER BY create_time DESC";
        List<Message> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Message.class), userId);
        return TableDataInfo.success(list, list.size());
    }

    /** 用户端：标记已读 */
    @PutMapping("/message/read/{id}")
    public AjaxResult readMessage(@PathVariable Long id) {
        jdbcTemplate.update("UPDATE psy_message SET is_read = 1 WHERE id = ?", id);
        return AjaxResult.success();
    }

    /** 用户端：未读消息数 */
    @GetMapping("/message/unread")
    public AjaxResult unreadCount() {
        Long userId = SecurityUtils.getUserId();
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM psy_message WHERE (user_id = ? OR user_id IS NULL) AND is_read = 0",
            Integer.class, userId);
        return AjaxResult.success(count);
    }

    /** 管理端：发送消息 */
    @PostMapping("/message/send")
    public AjaxResult sendMessage(@RequestBody Message message) {
        String sql = "INSERT INTO psy_message (user_id, title, content, type) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, message.getUserId(), message.getTitle(), message.getContent(), message.getType());
        return AjaxResult.success();
    }

    // ========== 心理知识库 ==========

    /** 文章列表 */
    @GetMapping("/knowledge/list")
    public TableDataInfo knowledgeList(@RequestParam(required = false) String category) {
        String sql = "SELECT * FROM psy_knowledge WHERE status = 1";
        if (category != null && !category.isEmpty()) {
            sql += " AND category = '" + category + "'";
        }
        sql += " ORDER BY create_time DESC";
        List<Knowledge> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Knowledge.class));
        return TableDataInfo.success(list, list.size());
    }

    /** 文章详情 */
    @GetMapping("/knowledge/{id}")
    public AjaxResult knowledgeDetail(@PathVariable Long id) {
        jdbcTemplate.update("UPDATE psy_knowledge SET view_count = view_count + 1 WHERE id = ?", id);
        Knowledge k = jdbcTemplate.queryForObject(
            "SELECT * FROM psy_knowledge WHERE id = ?",
            new BeanPropertyRowMapper<>(Knowledge.class), id);
        return AjaxResult.success(k);
    }

    /** 管理端：新增文章 */
    @PostMapping("/knowledge")
    public AjaxResult addKnowledge(@RequestBody Knowledge k) {
        jdbcTemplate.update(
            "INSERT INTO psy_knowledge (title, category, summary, content, author) VALUES (?, ?, ?, ?, ?)",
            k.getTitle(), k.getCategory(), k.getSummary(), k.getContent(), k.getAuthor());
        return AjaxResult.success();
    }

    /** 管理端：修改文章 */
    @PutMapping("/knowledge")
    public AjaxResult updateKnowledge(@RequestBody Knowledge k) {
        jdbcTemplate.update(
            "UPDATE psy_knowledge SET title = ?, category = ?, summary = ?, content = ? WHERE id = ?",
            k.getTitle(), k.getCategory(), k.getSummary(), k.getContent(), k.getId());
        return AjaxResult.success();
    }

    /** 管理端：删除文章 */
    @DeleteMapping("/knowledge/{id}")
    public AjaxResult deleteKnowledge(@PathVariable Long id) {
        jdbcTemplate.update("DELETE FROM psy_knowledge WHERE id = ?", id);
        return AjaxResult.success();
    }

    // ========== 诊断结论/处方建议 ==========

    /** 管理端：填写诊断结论 */
    @PutMapping("/record/diagnosis")
    public AjaxResult saveDiagnosis(@RequestBody Map<String, Object> body) {
        Long recordId = Long.valueOf(body.get("recordId").toString());
        String conclusion = (String) body.get("conclusion");
        String advice = (String) body.get("advice");
        jdbcTemplate.update(
            "UPDATE psy_test_record SET doctor_conclusion = ?, doctor_advice = ? WHERE record_id = ?",
            conclusion, advice, recordId);
        return AjaxResult.success();
    }

    // ========== 历史对比 ==========

    /** 用户端：历史对比数据 */
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
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
            "SELECT oper_id as operId, title, oper_name as operName, oper_url as operUrl, " +
            "oper_ip as operIp, status, error_msg as errorMsg, oper_time as operTime " +
            "FROM sys_oper_log ORDER BY oper_time DESC LIMIT 200");
        return TableDataInfo.success(list, list.size());
    }

    /** 登录日志列表 */
    @GetMapping("/log/login")
    public TableDataInfo loginLog() {
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
            "SELECT info_id as infoId, user_name as userName, ipaddr, status, msg, " +
            "login_time as loginTime FROM sys_login_log ORDER BY login_time DESC LIMIT 200");
        return TableDataInfo.success(list, list.size());
    }
}
