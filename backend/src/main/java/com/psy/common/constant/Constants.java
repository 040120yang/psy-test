package com.psy.common.constant;

/**
 * 通用常量信息
 */
public class Constants {

    /** 成功标志 */
    public static final String SUCCESS = "0";

    /** 失败标志 */
    public static final String FAIL = "1";

    /** 正常状态 */
    public static final String STATUS_NORMAL = "0";

    /** 停用状态 */
    public static final String STATUS_DISABLE = "1";

    /** 删除标志：存在 */
    public static final String DEL_FLAG_EXIST = "0";

    /** 删除标志：删除 */
    public static final String DEL_FLAG_DELETE = "2";

    /** 令牌请求头 */
    public static final String TOKEN_HEADER = "Authorization";

    /** 令牌前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** 令牌 Claims 中的用户 ID */
    public static final String LOGIN_USER_KEY = "login_user_key";

    /** 角色Key：系统管理员 */
    public static final String ROLE_ADMIN = "admin";

    /** 角色Key：临床医护人员 */
    public static final String ROLE_DOCTOR = "doctor";

    /** 角色Key：公众用户 */
    public static final String ROLE_USER = "user";
}
