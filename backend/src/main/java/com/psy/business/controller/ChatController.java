package com.psy.business.controller;

import com.psy.business.service.ChatService;
import com.psy.common.core.AjaxResult;
import com.psy.common.core.TableDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 学生端与教师端沟通（会话式聊天）
 *

 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 学生端：获取我的会话（负责教师信息 + 消息列表）
     */
    @GetMapping("/my")
    public AjaxResult my(@RequestParam(required = false) Integer limit) {
        return AjaxResult.success(chatService.my(limit));
    }

    /**
     * 教师端：我会话列表（含最后一条消息、未读数、是否待分配）
     */
    @GetMapping("/conversations")
    public TableDataInfo conversations() {
        List<Map<String, Object>> list = chatService.conversations();
        return TableDataInfo.success(list, list.size());
    }

    /**
     * 教师端：查看指定学生的会话消息
     */
    @GetMapping("/detail")
    public AjaxResult detail(@RequestParam Long studentId) {
        return AjaxResult.success(chatService.detail(studentId));
    }

    /**
     * 教师端：设置会话处理状态（待处理/已处理/需转介）、置顶与备注
     */
    @PutMapping("/conversation")
    public AjaxResult updateConversation(@RequestBody Map<String, Object> body) {
        chatService.updateConversation(body);
        return AjaxResult.success("会话状态已更新");
    }

    /**
     * 发送消息（学生端与教师端共用）
     */
    @PostMapping("/send")
    public AjaxResult send(@RequestBody Map<String, Object> body) {
        AjaxResult result = AjaxResult.success("发送成功");
        result.putAll(chatService.send(body));
        return result;
    }

    /**
     * 当前用户的未读消息数
     */
    @GetMapping("/unread")
    public AjaxResult unread() {
        return AjaxResult.success(chatService.unread());
    }

    /**
     * 标记已读（教师端需传 studentId）
     */
    @PutMapping("/read")
    public AjaxResult read(@RequestBody(required = false) Map<String, Object> body) {
        chatService.read(body);
        return AjaxResult.success();
    }
}
