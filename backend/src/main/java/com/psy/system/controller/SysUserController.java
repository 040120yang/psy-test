package com.psy.system.controller;

import com.psy.common.core.AjaxResult;
import com.psy.common.core.TableDataInfo;
import com.psy.common.exception.ServiceException;
import com.psy.framework.security.SecurityUtils;
import com.psy.system.domain.SysRole;
import com.psy.system.domain.SysUser;
import com.psy.system.mapper.SysRoleMapper;
import com.psy.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息（参照若依 SysUserController，仅系统管理员可管理）
 */
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController {

    @Autowired
    private SysUserService userService;

    @Autowired
    private SysRoleMapper roleMapper;

    /**
     * 查询用户列表
     */
    @GetMapping("/list")
    public TableDataInfo list(SysUser user) {
        return userService.list(user);
    }

    /**
     * 获取角色列表（用户管理下拉框）
     */
    @GetMapping("/roles")
    public AjaxResult roles() {
        return AjaxResult.success(roleMapper.selectRoleList());
    }

    /**
     * 新增用户
     */
    @PostMapping
    public AjaxResult add(@RequestBody SysUser user) {
        requireAdmin();
        return toAjax(userService.add(user));
    }

    /**
     * 修改用户
     */
    @PutMapping
    public AjaxResult edit(@RequestBody SysUser user) {
        requireAdmin();
        return toAjax(userService.edit(user));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public AjaxResult remove(@PathVariable Long userId) {
        requireAdmin();
        return toAjax(userService.remove(userId));
    }

    /**
     * 重置密码
     */
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody SysUser user) {
        requireAdmin();
        return toAjax(userService.resetPwd(user.getUserId(), user.getPassword()));
    }

    /** 校验当前用户是否为系统管理员 */
    private void requireAdmin() {
        if (!SecurityUtils.isAdmin()) {
            throw new ServiceException("无权限操作，仅系统管理员可管理用户");
        }
    }
}
