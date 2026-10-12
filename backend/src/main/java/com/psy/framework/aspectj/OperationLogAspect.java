package com.psy.framework.aspectj;

import com.psy.common.core.AjaxResult;
import com.psy.framework.aspectj.annotation.OperLog;
import com.psy.framework.security.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Persists audit records for management operations without affecting the
 * original request when logging itself fails.
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Around("@annotation(operLog)")
    public Object recordOperation(ProceedingJoinPoint joinPoint, OperLog operLog) throws Throwable {
        HttpServletRequest request = currentRequest();
        try {
            Object result = joinPoint.proceed();
            int status = responseSucceeded(result) ? 0 : 1;
            String errorMessage = status == 0 ? "" : responseMessage(result);
            saveLog(operLog, request, status, errorMessage);
            return result;
        } catch (Throwable throwable) {
            saveLog(operLog, request, 1, throwable.getMessage());
            throw throwable;
        }
    }

    private void saveLog(OperLog operLog, HttpServletRequest request, int status, String errorMessage) {
        try {
            String username = SecurityUtils.getUsername();
            String requestUrl = request == null ? "" : request.getRequestURI();
            String requestParams = request == null ? "" : request.getQueryString();
            jdbcTemplate.update(
                    "INSERT INTO sys_oper_log " +
                    "(title, business_type, oper_name, oper_url, oper_ip, oper_param, status, error_msg) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    limit(operLog.title(), 50),
                    operLog.businessType(),
                    limit(username == null || username.isEmpty() ? "未登录" : username, 50),
                    limit(requestUrl, 255),
                    limit(clientIp(request), 50),
                    requestParams == null ? "" : requestParams,
                    status,
                    limit(errorMessage, 2000));
        } catch (Exception loggingError) {
            log.error("写入操作日志失败", loggingError);
        }
    }

    private boolean responseSucceeded(Object result) {
        if (!(result instanceof Map)) {
            return true;
        }
        Object code = ((Map<?, ?>) result).get(AjaxResult.CODE_TAG);
        return !(code instanceof Number) || ((Number) code).intValue() == AjaxResult.SUCCESS;
    }

    private String responseMessage(Object result) {
        if (!(result instanceof Map)) {
            return "操作失败";
        }
        Object message = ((Map<?, ?>) result).get(AjaxResult.MSG_TAG);
        return message == null ? "操作失败" : message.toString();
    }

    private HttpServletRequest currentRequest() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes)) {
            return null;
        }
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    private String clientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return realIp == null || realIp.trim().isEmpty() ? request.getRemoteAddr() : realIp.trim();
    }

    private String limit(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
