package com.psy.business.controller;

import com.psy.common.core.AjaxResult;
import com.psy.common.utils.StringUtils;
import com.psy.framework.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

/**
 * 扣子（Coze）AI 智能体对接
 * 通过扣子开放平台 v3 API 发起对话（非流式三步：发起对话 -> 查询状态 -> 获取消息）
 */
@RestController
@RequestMapping("/coze")
public class CozeController {

    private static final String COZE_BASE = "https://api.coze.cn/v3";

    @Value("${coze.api-token:}")
    private String apiToken;

    @Value("${coze.bot-id:}")
    private String botId;

    private final RestTemplate restTemplate = new RestTemplate();

    /** 对话请求体 */
    public static class ChatReq {
        public String message;
        public String conversationId;
    }

    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody ChatReq req) {
        if (StringUtils.isEmpty(apiToken) || StringUtils.isEmpty(botId)) {
            return AjaxResult.error("未配置扣子智能体，请在 application.yml 中填写 coze.api-token（个人访问令牌）与 coze.bot-id（智能体ID）");
        }
        if (req == null || StringUtils.isEmpty(req.message)) {
            return AjaxResult.error("消息内容不能为空");
        }
        try {
            // 用户ID：使用系统用户ID隔离各自的对话上下文
            String userId = "psy-user-" + SecurityUtils.getUserId();

            // 第一步：发起对话
            Map<String, Object> chatBody = new HashMap<>();
            chatBody.put("bot_id", botId);
            chatBody.put("user_id", userId);
            chatBody.put("stream", false);
            chatBody.put("auto_save_history", true);
            Map<String, Object> msg = new HashMap<>();
            msg.put("role", "user");
            msg.put("content", req.message);
            msg.put("content_type", "text");
            chatBody.put("additional_messages", Collections.singletonList(msg));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiToken);

            String chatUrl = COZE_BASE + "/chat";
            if (StringUtils.isNotEmpty(req.conversationId)) {
                chatUrl = UriComponentsBuilder.fromHttpUrl(chatUrl)
                        .queryParam("conversation_id", req.conversationId).toUriString();
            }

            ResponseEntity<Map> chatResp = restTemplate.exchange(
                    chatUrl, HttpMethod.POST,
                    new HttpEntity<>(chatBody, headers), Map.class);
            Map chatData = chatResp.getBody() == null ? null : (Map) chatResp.getBody().get("data");
            if (chatResp.getBody() == null || !"0".equals(String.valueOf(chatResp.getBody().get("code")))
                    || chatData == null) {
                String msgText = chatResp.getBody() == null ? "扣子接口无响应" : String.valueOf(chatResp.getBody().get("msg"));
                return AjaxResult.error("扣子对话发起失败：" + msgText);
            }
            String chatId = String.valueOf(chatData.get("id"));
            String conversationId = String.valueOf(chatData.get("conversation_id"));

            // 第二步：轮询对话状态直至完成
            boolean completed = false;
            for (int i = 0; i < 60; i++) {
                String retrieveUrl = UriComponentsBuilder.fromHttpUrl(COZE_BASE + "/chat/retrieve")
                        .queryParam("conversation_id", conversationId)
                        .queryParam("chat_id", chatId)
                        .toUriString();
                ResponseEntity<Map> retrieveResp = restTemplate.exchange(
                        retrieveUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
                Map retrieveData = retrieveResp.getBody() == null ? null : (Map) retrieveResp.getBody().get("data");
                if (retrieveData != null && "completed".equals(retrieveData.get("status"))) {
                    completed = true;
                    break;
                }
                // 智能体执行失败：透出扣子平台真实原因（如积分额度不足、服务未发布等），避免笼统报超时
                if (retrieveData != null && "failed".equals(retrieveData.get("status"))) {
                    Map lastError = (Map) retrieveData.get("last_error");
                    String failMsg = lastError == null ? "智能体执行失败" : String.valueOf(lastError.get("msg"));
                    if (StringUtils.isEmpty(failMsg) || "null".equals(failMsg)) {
                        failMsg = "智能体执行失败";
                    }
                    return AjaxResult.error("智能体执行失败：" + failMsg);
                }
                Thread.sleep(1000);
            }
            if (!completed) {
                return AjaxResult.error("智能体响应超时，请稍后重试");
            }

            // 第三步：获取智能体回复
            String listUrl = UriComponentsBuilder.fromHttpUrl(COZE_BASE + "/chat/message/list")
                    .queryParam("conversation_id", conversationId)
                    .queryParam("chat_id", chatId)
                    .toUriString();
            ResponseEntity<Map> listResp = restTemplate.exchange(
                    listUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            List<Map> messages = listResp.getBody() == null ? null : (List) listResp.getBody().get("data");
            String reply = null;
            if (messages != null) {
                for (Map m : messages) {
                    if ("assistant".equals(m.get("role")) && "answer".equals(m.get("type"))) {
                        reply = String.valueOf(m.get("content"));
                        break;
                    }
                }
            }
            if (StringUtils.isEmpty(reply)) {
                return AjaxResult.error("未获取到智能体回复");
            }
            AjaxResult result = AjaxResult.success("ok");
            result.put("reply", reply);
            result.put("conversationId", conversationId);
            return result;
        } catch (Exception e) {
            return AjaxResult.error("AI 智能体服务异常：" + e.getMessage());
        }
    }
}
