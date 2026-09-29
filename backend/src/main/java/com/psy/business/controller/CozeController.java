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
        public java.util.List<MessageItem> history;
        public String mode;
    }

    public static class MessageItem {
        public String role;
        public String content;
    }

    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody ChatReq req) {
        if (req == null || StringUtils.isEmpty(req.message)) {
            return AjaxResult.error("消息内容不能为空");
        }
        if (StringUtils.isEmpty(apiToken) || StringUtils.isEmpty(botId)) {
            return localResult(req.message, req.conversationId, req.history, req.mode);
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
            chatBody.put("additional_messages", buildAdditionalMessages(req.message, req.history));

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
                return localResult(req.message, req.conversationId, req.history, req.mode);
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
                    return localResult(req.message, conversationId, req.history, req.mode);
                }
                Thread.sleep(700);
            }
            if (!completed) {
                log.warn("Coze chat timeout, chatId={}", chatId);
                return localResult(req.message, conversationId, req.history, req.mode);
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
                return localResult(req.message, conversationId, req.history, req.mode);
            }
            AjaxResult result = AjaxResult.success("ok");
            result.put("reply", reply);
            result.put("conversationId", conversationId);
            result.put("mode", "cloud");
            return result;
        } catch (Exception e) {
            log.warn("Coze service unavailable, using local assistant: {}", e.getMessage());
            return localResult(req.message, req.conversationId, req.history, req.mode);
        }
    }

private java.util.List<Map<String, Object>> buildAdditionalMessages(String current, java.util.List<MessageItem> history) {
        java.util.List<Map<String, Object>> messages = new ArrayList<>();
        if (history != null) {
            int start = Math.max(0, history.size() - 8);
            for (int i = start; i < history.size(); i++) {
                MessageItem item = history.get(i);
                if (item == null || StringUtils.isEmpty(item.content)) continue;
                Map<String, Object> msg = new HashMap<>();
                msg.put("role", "assistant".equals(item.role) ? "assistant" : "user");
                msg.put("content", item.content);
                msg.put("content_type", "text");
                messages.add(msg);
            }
        }
        Map<String, Object> currentMessage = new HashMap<>();
        currentMessage.put("role", "user");
        currentMessage.put("content", current);
        currentMessage.put("content_type", "text");
        messages.add(currentMessage);
        return messages;
    }

    private AjaxResult localResult(String message, String conversationId, java.util.List<MessageItem> history, String mode) {
        AjaxResult result = AjaxResult.success("本地心理陪伴模式");
        result.put("reply", localReply(message, history, mode));
        result.put("conversationId", StringUtils.isNotEmpty(conversationId)
                ? conversationId : "local-" + UUID.randomUUID().toString().replace("-", ""));
        result.put("mode", "local");
        return result;
    }
private String localReply(String message, java.util.List<MessageItem> history, String mode) {
        String text = message == null ? "" : message.toLowerCase();
        String currentMode = StringUtils.isEmpty(mode) ? "listen" : mode;
        if (containsAny(text, "自杀", "轻生", "不想活", "活不下去", "自残", "伤害自己", "结束生命")) {
            return "我很在意你现在的安全。先不要一个人待着，好吗？请立刻联系一个信任的家人、老师或朋友陪在你身边。\n\n"
                    + "如果伤害自己的想法很强烈，请马上拨打 120 或 110，也可以拨打全国心理援助热线 12356。先远离可能伤害自己的物品，等专业人员来帮助你。";
        }
        String topic = detectTopic(text);
        String previous = previousTopic(history);
        String context = "";
        if (previous != null && !"unknown".equals(previous)
                && containsAny(text, "还是", "又", "一直", "最近")) {
            context = previous.equals(topic)
                    ? "你之前也提到过" + topicName(previous) + "，这次听起来它还在影响你。"
                    : "你之前提到过" + topicName(previous) + "，现在又说起" + topicName(topic) + "，两件事可能有关。";
        }
        if (containsAny(text, "你好", "在吗", "你是谁", "介绍一下")) {
            return "你好，我在这里。你可以慢慢说，不用一下子把问题讲清楚。最近最让你觉得累的，是情绪、睡眠、人际关系，还是学习或工作上的压力？";
        }
        if ("unknown".equals(topic)) {
            String lead = pick(text, "谢谢你愿意告诉我这些。", "我听见了，这件事对你来说应该不轻松。", "你愿意说出来，本身就很不容易。");
            return context + lead + "如果愿意，可以再具体说一点：它是什么时候开始的，最近对你的睡眠、情绪或日常生活影响最大的是什么？";
        }
        return context + empathy(text, currentMode) + topicResponse(topic, currentMode) + followUp(topic, currentMode);
    }

    private String detectTopic(String text) {
        if (containsAny(text, "失眠", "睡不着", "睡眠", "半夜醒", "早醒", "梦多")) return "sleep";
        if (containsAny(text, "焦虑", "紧张", "害怕", "心慌", "烦躁", "担心", "压力")) return "anxiety";
        if (containsAny(text, "抑郁", "难过", "低落", "没兴趣", "无意义", "想哭", "疲惫", "空虚")) return "mood";
        if (containsAny(text, "同学", "朋友", "家人", "人际", "孤独", "矛盾", "恋爱", "被排挤")) return "relationship";
        if (containsAny(text, "没用", "不够好", "自卑", "讨厌自己", "失败", "比不上")) return "self_esteem";
        if (containsAny(text, "头疼", "胸闷", "恶心", "手抖", "身体", "喘不过气")) return "body";
        if (containsAny(text, "考试", "成绩", "作业", "论文", "毕业", "老师", "学习")) return "study";
        return "unknown";
    }

    private String previousTopic(java.util.List<MessageItem> history) {
        if (history == null) return null;
        for (int i = history.size() - 1; i >= 0; i--) {
            MessageItem item = history.get(i);
            if (item != null && "user".equals(item.role) && StringUtils.isNotEmpty(item.content)) {
                String topic = detectTopic(item.content.toLowerCase());
                if (!"unknown".equals(topic)) return topic;
            }
        }
        return null;
    }

    private String topicName(String topic) {
        if ("sleep".equals(topic)) return "睡眠";
        if ("anxiety".equals(topic)) return "紧张和焦虑";
        if ("mood".equals(topic)) return "情绪低落";
        if ("relationship".equals(topic)) return "人际关系";
        if ("self_esteem".equals(topic)) return "自我否定";
        if ("body".equals(topic)) return "身体不适";
        if ("study".equals(topic)) return "学习压力";
        return "最近的困扰";
    }

    private String empathy(String seed, String mode) {
        if ("advice".equals(mode)) return pick(seed, "我们可以一起把问题拆小一点。", "先不急着全部解决，我们先找一个能开始的地方。");
        if ("science".equals(mode)) return pick(seed, "这类反应通常和压力系统持续处于警觉有关。", "身体和心理的反应经常是相互影响的。");
        return pick(seed, "听起来你已经撑了一段时间。", "这种感受确实很消耗人。", "你能注意到自己的状态，这很重要。");
    }

    private String topicResponse(String topic, String mode) {
        if ("sleep".equals(topic)) {
            if ("advice".equals(mode)) return "先看三件事：固定起床时间、睡前减少手机和咖啡因、躺下二十分钟仍睡不着时先离开床做安静的事。不要一直盯着时间，也不要强迫自己马上睡着。";
            if ("science".equals(mode)) return "睡眠受情绪、作息、光照和压力共同影响。越想控制入睡，有时反而越清醒。长期失眠还可能与焦虑或抑郁相互影响。";
            return "比起马上让自己睡着，也许我们可以先看看，是难以入睡、半夜容易醒，还是醒来后很难再睡？";
        }
        if ("anxiety".equals(topic)) {
            if ("advice".equals(mode)) return "可以先把担心写成一句话，再分成能控制和不能控制的部分。现在只做最小的一步，比如整理十分钟资料、喝口水或走一小圈，同时配合缓慢呼吸。";
            if ("science".equals(mode)) return "焦虑会让身体进入警觉状态，出现心跳加快、胸闷和注意力变窄。呼吸和运动能帮助身体先降下来，但持续焦虑仍值得专业评估。";
            return "我听到你好像一直绷着，身体也跟着紧张。此刻最明显的是心慌、担心，还是脑子里停不下来的想法？";
        }
        if ("mood".equals(topic)) {
            if ("advice".equals(mode)) return "低落时不要要求自己立刻振作，先把目标缩小到吃饭、喝水、洗澡、出门十分钟或联系一个人。完成一件小事，就已经是在恢复。";
            if ("science".equals(mode)) return "情绪低落会影响睡眠、食欲、注意力和行动力。如果持续两周以上，或越来越重，通常需要专业评估。";
            return "你不必马上变好。可以先告诉我，这种低落是最近几天，还是已经持续一段时间了？";
        }
        if ("relationship".equals(topic)) {
            if ("advice".equals(mode)) return "沟通时可以用“发生了什么—我感到什么—我希望怎样”来说，先讨论一件事，不要一次翻出所有旧账。";
            if ("science".equals(mode)) return "关系冲突会激活被拒绝和被误解的担忧，所以我们有时会先防御，而不是表达真实需要。";
            return "这段关系里，最让你难受的是被误解、被忽视，还是不知道该怎么开口？";
        }
        if ("self_esteem".equals(topic)) return "你对自己的评价听起来很严厉。偶尔做不到，不等于你这个人没有价值。最近是哪件事让你最强烈地觉得自己不够好？";
        if ("body".equals(topic)) return "身体和心理的压力有时会一起出现。先确认有没有明显、持续或加重的身体症状；如果有，优先去医院检查。排除身体原因后，也值得关注最近的焦虑和睡眠。";
        if ("study".equals(topic)) return "学习压力通常来自任务太多、结果不确定和害怕让别人失望。可以先选出今天最重要的一件事，再把它拆成二十分钟能完成的步骤。你最担心的是来不及，还是结果不理想？";
        return "我们可以慢慢把这件事说清楚。";
    }

    private String followUp(String topic, String mode) {
        if ("advice".equals(mode)) return "\n\n你更希望我先陪你梳理原因，还是直接帮你制定今天的行动步骤？";
        if ("science".equals(mode)) return "\n\n如果你愿意，我可以继续解释它通常怎么发展，以及什么情况下需要专业帮助。";
        if ("sleep".equals(topic)) return "\n\n最近一周，你的睡眠大概是什么样子的？";
        if ("anxiety".equals(topic)) return "\n\n这种紧张通常在什么场景下最明显？";
        if ("mood".equals(topic)) return "\n\n最近有没有哪一刻，你稍微感觉好一点？";
        if ("relationship".equals(topic)) return "\n\n你希望对方理解你的哪一部分？";
        return "\n\n你愿意从最近发生的一件具体事情说起吗？";
    }

    private String pick(String seed, String... options) {
        if (options.length == 0) return "";
        int index = Math.abs((seed == null ? "" : seed).hashCode()) % options.length;
        return options[index];
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
