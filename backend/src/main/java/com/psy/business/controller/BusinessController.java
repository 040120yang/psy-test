package com.psy.business.controller;

import com.psy.business.domain.Knowledge;
import com.psy.business.domain.Message;
import com.psy.business.service.BusinessService;
import com.psy.common.core.AjaxResult;
import com.psy.common.core.TableDataInfo;
import com.psy.framework.aspectj.annotation.OperLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 消息中心与心理知识库
 *
 *
 */
@RestController
@RequestMapping("/business")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    // 消息中心

    /**
     * 学生端：我的消息（含全局广播）
     */
    @GetMapping("/message/my")
    public TableDataInfo myMessages() {
        List<Message> list = businessService.myMessages();
        return TableDataInfo.success(list, list.size());
    }

    /**
     * 学生端：标记单条消息已读
     */
    @PutMapping("/message/read/{id}")
    public AjaxResult readMessage(@PathVariable Long id) {
        businessService.readMessage(id);
        return AjaxResult.success();
    }

    /**
     * 学生端：未读消息数
     */
    @GetMapping("/message/unread")
    public AjaxResult unreadCount() {
        return AjaxResult.success(businessService.unreadCount());
    }

    /**
     * 管理端：下发消息（仅心理教师或系统管理员）
     */
    @OperLog(title = "消息管理", businessType = OperLog.INSERT)
    @PostMapping("/message/send")
    public AjaxResult sendMessage(@RequestBody Message message) {
        businessService.sendMessage(message);
        return AjaxResult.success();
    }

    //  心理知识库
    /**
     * 文章列表（仅已发布）
     */
    @GetMapping("/knowledge/list")
    public TableDataInfo knowledgeList(@RequestParam(required = false) String category) {
        List<Knowledge> list = businessService.knowledgeList(category);
        return TableDataInfo.success(list, list.size());
    }

    /**
     * 文章详情（学生仅可查看已发布内容）
     */
    @GetMapping("/knowledge/{id}")
    public AjaxResult knowledgeDetail(@PathVariable Long id) {
        return AjaxResult.success(businessService.knowledgeDetail(id));
    }

    /**
     * 管理端：新增文章
     */
    @OperLog(title = "知识库管理", businessType = OperLog.INSERT)
    @PostMapping("/knowledge")
    public AjaxResult addKnowledge(@RequestBody Knowledge knowledge) {
        businessService.addKnowledge(knowledge);
        return AjaxResult.success();
    }

    /**
     * 管理端：修改文章
     */
    @OperLog(title = "知识库管理", businessType = OperLog.UPDATE)
    @PutMapping("/knowledge")
    public AjaxResult updateKnowledge(@RequestBody Knowledge knowledge) {
        businessService.updateKnowledge(knowledge);
        return AjaxResult.success();
    }

    /**
     * 管理端：删除文章
     */
    @OperLog(title = "知识库管理", businessType = OperLog.DELETE)
    @DeleteMapping("/knowledge/{id}")
    public AjaxResult deleteKnowledge(@PathVariable Long id) {
        businessService.deleteKnowledge(id);
        return AjaxResult.success();
    }

    //  心理评估

    /**
     * 管理端：填写心理评估结论与关怀建议（保存后自动通知该记录所属学生）
     */
    @OperLog(title = "测评记录-心理评估", businessType = OperLog.UPDATE)
    @PutMapping("/record/diagnosis")
    public AjaxResult saveDiagnosis(@RequestBody Map<String, Object> body) {
        businessService.saveDiagnosis(body);
        return AjaxResult.success("评估建议已保存");
    }

    //  历史对比

    /**
     * 学生端：历史测评结果对比数据
     */
    @GetMapping("/record/compare")
    public AjaxResult compareHistory() {
        return AjaxResult.success(businessService.compareHistory());
    }

    //  系统日志

    /**
     * 操作日志列表（仅系统管理员）
     */
    @GetMapping("/log/oper")
    public TableDataInfo operLog() {
        List<Map<String, Object>> list = businessService.operLog();
        return TableDataInfo.success(list, list.size());
    }

    /**
     * 登录日志列表（仅系统管理员）
     */
    @GetMapping("/log/login")
    public TableDataInfo loginLog() {
        List<Map<String, Object>> list = businessService.loginLog();
        return TableDataInfo.success(list, list.size());
    }
}
