package com.psy.framework.security;

import com.psy.common.exception.ServiceException;
import com.psy.common.utils.StringUtils;

/**
 * 权限获取工具类（简化版，基于 ThreadLocal 保存当前登录用户）
 */
public class SecurityUtils {

    private static final ThreadLocal<LoginUser> LOGIN_USER_HOLDER = new ThreadLocal<>();

    public static void setLoginUser(LoginUser loginUser) {
        LOGIN_USER_HOLDER.set(loginUser);
    }

    public static LoginUser getLoginUser() {
        return LOGIN_USER_HOLDER.get();
    }

    public static void removeLoginUser() {
        LOGIN_USER_HOLDER.remove();
    }

    /** 获取当前登录用户ID */
    public static Long getUserId() {
        LoginUser user = getLoginUser();
        if (user == null) {
            throw new ServiceException("登录状态已失效，请重新登录");
        }
        return user.getUserId();
    }

    /** 获取当前登录用户名 */
    public static String getUsername() {
        LoginUser user = getLoginUser();
        return user == null ? "" : user.getUsername();
    }

    /** 当前用户是否为系统管理员 */
    public static boolean isAdmin() {
        LoginUser user = getLoginUser();
        return user != null && "admin".equals(user.getRoleKey());
    }

    /** 当前用户是否为临床医护人员 */
    public static boolean isDoctor() {
        LoginUser user = getLoginUser();
        return user != null && "doctor".equals(user.getRoleKey());
    }

    /** 当前用户是否为公众用户 */
    public static boolean isUser() {
        LoginUser user = getLoginUser();
        return user != null && "user".equals(user.getRoleKey());
    }

    /** 校验密码：BCrypt 匹配 */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        if (StringUtils.isEmpty(rawPassword) || StringUtils.isEmpty(encodedPassword)) {
            return false;
        }
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().matches(rawPassword, encodedPassword);
    }

    /** 密码加密 */
    public static String encryptPassword(String rawPassword) {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(rawPassword);
    }
}
