package com.psy.system.service;

import com.psy.common.core.TableDataInfo;
import com.psy.system.domain.SysUser;

/**
 * 用户业务层
 */
public interface SysUserService {

    /** 分页查询用户列表 */
    TableDataInfo list(SysUser user);

    /** 新增用户 */
    int add(SysUser user);

    /** 修改用户 */
    int edit(SysUser user);

    /** 删除用户 */
    int remove(Long userId);

    /** 重置密码 */
    int resetPwd(Long userId, String password);
}
