package com.psy.business.controller;

import com.psy.common.core.AjaxResult;
import com.psy.common.utils.StringUtils;
import com.psy.framework.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * 扣子（Coze）AI 智能体对接。
 * 云端服务不可用或额度不足时自动切换到本地心理陪伴回复，保证功能可用。
 */
@RestController
@RequestMapping("/coze")
public class CozeController {

    private static final Logger log = LoggerFactory.getLogger(CozeController.class);
    private static final String COZE_BASE = "https://api.coze.cn/v3";

    @Value("${coze.api-token:}")
    private String apiToken;

    @Value("${coze.bot-id:}")
    private String botId;

    private final RestTemplate restTemplate = new RestTemplate();

    public static class ChatReq {
        public String message;
        public String conversationId;
    }

    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody ChatReq req) {
        if (req == null || StringUtils.isEmpty(req.message)) {
            return AjaxResult.error("消息内容不能为空");
        }
        if (StringUtils.isEmpty(apiToken) || StringUtils.isEmpty(botId)) {
            return localResult(req.message, req.conversationId);
        }
        try {
            String userId = "psy-user-" + SecurityUtils.getUserId();
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
                    chatUrl, HttpMethod.POST, new HttpEntity<>(chatBody, headers), Map.class);
            Map chatBodyResult = chatResp.getBody();
            Map chatData = chatBodyResult == null ? null : (Map) chatBodyResult.get("data");
            if (chatBodyResult == null || !"0".equals(String.valueOf(chatBodyResult.get("code"))) || chatData == null) {
                log.warn("Coze chat start failed: {}", chatBodyResult);
                return localResult(req.message, req.conversationId);
            }

            String chatId = String.valueOf(chatData.get("id"));
            String conversationId = String.valueOf(chatData.get("conversation_id"));

            boolean completed = false;
            for (int i = 0; i < 30; i++) {
                String retrieveUrl = UriComponentsBuilder.fromHttpUrl(COZE_BASE + "/chat/retrieve")
                        .queryParam("conversation_id", conversationId)
                        .queryParam("chat_id", chatId)
                        .toUriString();
                ResponseEntity<Map> retrieveResp = restTemplate.exchange(
                        retrieveUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
                Map retrieveBody = retrieveResp.getBody();
                Map retrieveData = retrieveBody == null ? null : (Map) retrieveBody.get("data");
                if (retrieveData != null && "completed".equals(retrieveData.get("status"))) {
                    completed = true;
                    break;
                }
                if (retrieveData != null && "failed".equals(retrieveData.get("status"))) {
                    log.warn("Coze chat failed: {}", retrieveData.get("last_error"));
                    return localResult(req.message, conversationId);
                }
                Thread.sleep(700);
            }
            if (!completed) {
                log.warn("Coze chat timeout, chatId={}", chatId);
                return localResult(req.message, conversationId);
            }

            String listUrl = UriComponentsBuilder.fromHttpUrl(COZE_BASE + "/chat/message/list")
                    .queryParam("conversation_id", conversationId)
                    .queryParam("chat_id", chatId)
                    .toUriString();
            ResponseEntity<Map> listResp = restTemplate.exchange(
                    listUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            Map listBody = listResp.getBody();
            List<Map> messages = listBody == null ? null : (List) listBody.get("data");
            String reply = null;
            if (messages != null) {
                for (Map item : messages) {
                    if ("assistant".equals(item.get("role")) && "answer".equals(item.get("type"))) {
                        reply = String.valueOf(item.get("content"));
                        break;
                    }
                }
            }
            if (StringUtils.isEmpty(reply)) {
                return localResult(req.message, conversationId);
            }
            AjaxResult result = AjaxResult.success("ok");
            result.put("reply", reply);
            result.put("conversationId", conversationId);
            result.put("mode", "cloud");
            return result;
        } catch (Exception e) {
            log.warn("Coze service unavailable, using local assistant: {}", e.getMessage());
            return localResult(req.message, req.conversationId);
        }
    }

    private AjaxResult localResult(String message, String conversationId) {
        AjaxResult result = AjaxResult.success("本地心理陪伴模式");
        result.put("reply", localReply(message));
        result.put("conversationId", StringUtils.isNotEmpty(conversationId)
                ? conversationId : "local-" + UUID.randomUUID().toString().replace("-", ""));
        result.put("mode", "local");
        return result;
    }

    private String localReply(String message) {
        String text = message == null ? "" : message.toLowerCase();
        if (containsAny(text, "自杀", "轻生", "不想活", "活不下去", "自残", "伤害自己", "结束生命")) {
            return "我很在意你现在的安全。请先不要独处，立即联系信任的家人、老师或朋友陪在身边。\n\n"
                    + "如果你有正在实施伤害自己的想法，请马上拨打 120 或 110，也可以拨打全国心理援助热线 12356。\n\n"
                    + "你现在不需要一个人扛着。先远离可能伤害自己的物品，停留在有人陪伴的环境中，等专业人员来帮助你。";
        }
        if (containsAny(text, "失眠", "睡不着", "睡眠", "半夜醒", "早醒")) {
            return "睡眠问题通常和压力、作息及情绪有关，可以先尝试这些方法：\n"
                    + "1. 固定起床时间，即使前晚没睡好也不要赖床过久；\n"
                    + "2. 睡前 1 小时离开手机和电脑，减少咖啡、浓茶和剧烈运动；\n"
                    + "3. 躺下 20 分钟仍睡不着时，先起身做安静的事，困了再回到床上；\n"
                    + "4. 白天进行 20-30 分钟温和运动，午睡不超过 30 分钟。\n\n"
                    + "如果失眠持续两周以上，或已经影响学习、工作和情绪，建议到睡眠医学科或心理科就诊。";
        }
        if (containsAny(text, "焦虑", "紧张", "害怕", "考试", "压力", "心慌", "烦躁")) {
            return "我听到你现在有些紧绷。可以现在做一次简单的“4-7-8”呼吸：\n"
                    + "吸气 4 秒，屏息 7 秒，缓慢呼气 8 秒，重复 4 轮。\n\n"
                    + "然后把让你焦虑的事情写下来，分成“现在能做的”和“暂时不能控制的”两类。先完成最小的一步，例如整理 10 分钟资料或出门走一圈。\n\n"
                    + "如果心慌、胸闷频繁出现，或焦虑持续影响生活，建议尽快咨询心理专业人员。";
        }
        if (containsAny(text, "抑郁", "难过", "低落", "没兴趣", "无意义", "想哭", "疲惫")) {
            return "情绪低落时，先不要急着要求自己立刻变好。可以尝试把今天的目标缩小到：按时吃饭、喝一杯水、出门走 10 分钟、联系一个信任的人。\n\n"
                    + "如果这种状态持续两周以上，伴随睡眠和食欲明显变化，或出现自伤想法，请尽快前往心理科或精神科就诊。你不需要独自承受。";
        }
        if (containsAny(text, "同学", "朋友", "家人", "人际", "孤独", "矛盾", "恋爱")) {
            return "人际关系带来的困扰很消耗人。你可以先区分三件事：对方做了什么、你产生了什么感受、你希望对方以后怎么做。\n"
                    + "沟通时使用“我感到……，我希望……”的句式，比直接指责更容易被听见。\n\n"
                    + "如果关系中出现威胁、羞辱或控制，请优先保护自己的安全，并向可信任的人或老师求助。";
        }
        if (containsAny(text, "你好", "在吗", "你是谁", "介绍一下")) {
            return "你好，我是系统的本地心理陪伴助手，可以和你聊聊情绪、压力、睡眠、人际关系等问题。\n"
                    + "我不会替代医生诊断。如果你想开始，可以告诉我：最近最让你困扰的一件事是什么？";
        }
        return "谢谢你把这件事告诉我。为了更好理解你的感受，可以再说得具体一些：\n"
                + "1. 这件事从什么时候开始？\n"
                + "2. 它对睡眠、食欲、学习或工作有什么影响？\n"
                + "3. 你现在最希望得到的是倾听、方法建议，还是就医参考？\n\n"
                + "如果出现自伤或轻生想法，请立即联系身边可信任的人，并拨打 12356 或前往医院急诊。";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
