package com.psy.business.controller;

import com.psy.business.domain.TestRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CozeControllerTest {

    @Test
    void prefersDeltaContentOverCompletedMessage() throws Exception {
        String stream = "event: conversation.message.delta\n"
                + "data: {\"conversation_id\":\"conv-1\",\"role\":\"assistant\",\"type\":\"answer\",\"content\":\"first \"}\n\n"
                + "event: conversation.message.delta\n"
                + "data: {\"conversation_id\":\"conv-1\",\"role\":\"assistant\",\"type\":\"answer\",\"content\":\"second\"}\n\n"
                + "event: conversation.message.completed\n"
                + "data: {\"conversation_id\":\"conv-1\",\"role\":\"assistant\",\"type\":\"answer\",\"content\":\"first second\"}\n\n";

        Object reply = parseStream(stream);

        assertEquals("first second", field(reply, "reply"));
        assertEquals("conv-1", field(reply, "conversationId"));
    }

    @Test
    void usesCompletedMessageWhenNoDeltaContentExists() throws Exception {
        String stream = "event: conversation.message.completed\n"
                + "data: {\"conversation_id\":\"conv-2\",\"role\":\"assistant\",\"type\":\"answer\",\"content\":\"completed answer\"}\n\n";

        Object reply = parseStream(stream);

        assertEquals("completed answer", field(reply, "reply"));
        assertEquals("conv-2", field(reply, "conversationId"));
    }

    @Test
    void reportPromptContainsOnlyAssessmentSummaryAndSafetyRules() {
        TestRecord record = new TestRecord();
        record.setScaleName("焦虑自评量表");
        record.setScaleCode("SAS");
        record.setRawScore(52);
        record.setStdScore(new BigDecimal("65.00"));
        record.setLevel("中度焦虑");
        record.setSuggestion("建议联系心理教师");
        record.setNickname("不应发送的姓名");
        record.setUsername("student-001");

        String prompt = CozeController.buildReportPrompt(record);

        assertTrue(prompt.contains("焦虑自评量表"));
        assertTrue(prompt.contains("65.00"));
        assertTrue(prompt.contains("不作医学诊断"));
        assertTrue(prompt.contains("12356"));
        assertFalse(prompt.contains("不应发送的姓名"));
        assertFalse(prompt.contains("student-001"));
    }

    private Object parseStream(String stream) throws Exception {
        Method method = CozeController.class.getDeclaredMethod("parseStream", String.class, String.class);
        method.setAccessible(true);
        return method.invoke(new CozeController(), stream, null);
    }

    private Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }
}
