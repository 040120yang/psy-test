package com.psy.framework.aspectj;

import com.psy.framework.security.LoginUser;
import com.psy.framework.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class OperationLogAspectTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void recordsSuccessfulManagementOperation() throws Exception {
        int before = logCount();

        mockMvc.perform(put("/business/knowledge")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":-99999,\"title\":\"audit-test\",\"category\":\"test\",\"summary\":\"\",\"content\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        assertEquals(before + 1, logCount());
        Map<String, Object> log = latestLog();
        assertEquals("知识库管理", log.get("title"));
        assertEquals(2, ((Number) log.get("business_type")).intValue());
        assertEquals("admin", log.get("oper_name"));
        assertEquals(0, ((Number) log.get("status")).intValue());
    }

    @Test
    void recordsFailedManagementOperation() throws Exception {
        int before = logCount();

        mockMvc.perform(delete("/system/user/1")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));

        assertEquals(before + 1, logCount());
        Map<String, Object> log = latestLog();
        assertEquals("用户管理", log.get("title"));
        assertEquals(3, ((Number) log.get("business_type")).intValue());
        assertEquals(1, ((Number) log.get("status")).intValue());
        assertTrue(!log.get("error_msg").toString().trim().isEmpty());
    }

    @Test
    void doctorCanManageKnowledge() throws Exception {
        mockMvc.perform(post("/business/knowledge")
                        .header("Authorization", "Bearer " + token(2L, "doctor", "doctor"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"doctor-knowledge-test\",\"category\":\"test\",\"summary\":\"\",\"content\":\"test\",\"author\":\"doctor\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM psy_knowledge WHERE title = 'doctor-knowledge-test'", Integer.class);
        assertEquals(1, count);
    }

    @Test
    void studentCannotManageKnowledge() throws Exception {
        mockMvc.perform(post("/business/knowledge")
                        .header("Authorization", "Bearer " + token(3L, "user", "user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"forbidden-test\",\"category\":\"test\",\"content\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void creatingPatientAlsoCreatesStudentAccount() throws Exception {
        mockMvc.perform(post("/patient")
                        .header("Authorization", "Bearer " + token(2L, "doctor", "doctor"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patientName\":\"sync-test\",\"username\":\"patient-sync-test\",\"password\":\"123456\",\"sex\":\"2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Map<String, Object> linked = jdbcTemplate.queryForMap(
                "SELECT su.user_id, sr.role_key, pp.patient_name FROM sys_user su " +
                "JOIN sys_role sr ON sr.role_id = su.role_id " +
                "JOIN psy_patient pp ON pp.user_id = su.user_id WHERE su.username = 'patient-sync-test'");
        assertEquals("user", linked.get("role_key"));
        assertEquals("sync-test", linked.get("patient_name"));

        Long userId = ((Number) linked.get("user_id")).longValue();
        mockMvc.perform(delete("/system/user/" + userId)
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM psy_patient WHERE user_id = ?", Integer.class, userId));

        mockMvc.perform(post("/patient")
                        .header("Authorization", "Bearer " + token(2L, "doctor", "doctor"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patientName\":\"sync-test-restored\",\"username\":\"patient-sync-test\",\"password\":\"654321\",\"sex\":\"1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE username = 'patient-sync-test'", Integer.class));
        Map<String, Object> restored = jdbcTemplate.queryForMap(
                "SELECT su.user_id, su.del_flag, su.status, pp.patient_name FROM sys_user su " +
                "JOIN psy_patient pp ON pp.user_id = su.user_id WHERE su.username = 'patient-sync-test'");
        assertEquals(userId, ((Number) restored.get("user_id")).longValue());
        assertEquals("0", restored.get("del_flag"));
        assertEquals("0", restored.get("status"));
        assertEquals("sync-test-restored", restored.get("patient_name"));
    }

    @Test
    void deletingPatientAlsoDisablesStudentAccount() throws Exception {
        mockMvc.perform(post("/patient")
                        .header("Authorization", "Bearer " + token(2L, "doctor", "doctor"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patientName\":\"delete-sync-test\",\"username\":\"patient-delete-test\",\"password\":\"123456\",\"sex\":\"2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Map<String, Object> linked = jdbcTemplate.queryForMap(
                "SELECT su.user_id, pp.patient_id FROM sys_user su " +
                "JOIN psy_patient pp ON pp.user_id = su.user_id WHERE su.username = 'patient-delete-test'");
        Long patientId = ((Number) linked.get("patient_id")).longValue();
        mockMvc.perform(delete("/patient/" + patientId)
                        .header("Authorization", "Bearer " + token(2L, "doctor", "doctor")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        assertEquals("2", jdbcTemplate.queryForObject(
                "SELECT del_flag FROM sys_user WHERE user_id = ?", String.class, linked.get("user_id")));
    }

    private String adminToken() {
        return token(1L, "admin", "admin");
    }

    private String token(Long userId, String username, String roleKey) {
        LoginUser user = new LoginUser();
        user.setUserId(userId);
        user.setUsername(username);
        user.setRoleKey(roleKey);
        return tokenService.createToken(user);
    }

    private int logCount() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_oper_log", Integer.class);
    }

    private Map<String, Object> latestLog() {
        return jdbcTemplate.queryForMap(
                "SELECT title, business_type, oper_name, status, error_msg " +
                "FROM sys_oper_log ORDER BY oper_id DESC LIMIT 1");
    }
}
