package com.psy.system.controller;

import com.psy.common.core.AjaxResult;
import com.psy.common.core.TableDataInfo;
import com.psy.common.exception.ServiceException;
import com.psy.framework.aspectj.annotation.OperLog;
import com.psy.framework.security.SecurityUtils;
import com.psy.system.domain.SysRole;
import com.psy.system.domain.SysUser;
import com.psy.system.mapper.SysRoleMapper;
import com.psy.system.mapper.SysUserMapper;
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

    @Autowired
    private SysUserMapper userMapper;

    /**
     * 查询用户列表
     */
    @GetMapping("/list")
    public TableDataInfo list(SysUser user) {
        requireAdmin();
        return userService.list(user);
    }

    /**
     * 获取角色列表（用户管理下拉框）
     */
    @GetMapping("/roles")
    public AjaxResult roles() {
        requireAdmin();
        return AjaxResult.success(roleMapper.selectRoleList());
    }

    /**
     * 获取心理教师列表（学生管理-负责教师下拉框）
     */
    @GetMapping("/doctors")
    public AjaxResult doctors() {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.isDoctor()) {
            throw new ServiceException("无权限查看心理教师列表");
        }
        return AjaxResult.success(userMapper.selectUsersByRoleKey("doctor"));
    }

    /**
     * 新增用户
     */
    @OperLog(title = "用户管理", businessType = OperLog.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysUser user) {
        requireAdmin();
        return toAjax(userService.add(user));
    }

    /**
     * 修改用户
     */
    @OperLog(title = "用户管理", businessType = OperLog.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysUser user) {
        requireAdmin();
        return toAjax(userService.edit(user));
    }

    /**
     * 删除用户
     */
    @OperLog(title = "用户管理", businessType = OperLog.DELETE)
    @DeleteMapping("/{userId}")
    public AjaxResult remove(@PathVariable Long userId) {
        requireAdmin();
        return toAjax(userService.remove(userId));
    }

    /**
     * 重置密码
     */
    @OperLog(title = "用户管理-重置密码", businessType = OperLog.UPDATE)
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody SysUser user) {
        requireAdmin();
        return toAjax(userService.resetPwd(user.getUserId(), user.getPassword()));
    }

    /**
     * 下载批量导入建号模板 CSV
     */
    @GetMapping("/importTemplate")
    public void importTemplate(javax.servlet.http.HttpServletResponse response) throws Exception {
        requireAdmin();
        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=user_import_template.csv");
        response.getWriter().write("\ufeff用户名,姓名,性别,角色,手机号,初始密码\n"
                + "2026001,张三,男,学生用户,13800000001,123456\n"
                + "2026002,李四,女,学生用户,13800000002,123456\n"
                + "doctor02,王医生,女,心理教师/辅导员,13800000003,123456\n");
    }

    /**
     * 批量导入建号（CSV 文件）
     */
    @OperLog(title = "用户管理-批量导入", businessType = OperLog.INSERT)
    @PostMapping("/batchImport")
    public AjaxResult batchImport(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        requireAdmin();
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("请选择要导入的 CSV 文件");
        }
        try {
            String content = new String(file.getBytes(), "UTF-8");
            return AjaxResult.success(userService.batchImport(content));
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            return AjaxResult.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 批量重置密码
     */
    @OperLog(title = "用户管理-批量重置密码", businessType = OperLog.UPDATE)
    @PutMapping("/batchResetPwd")
    public AjaxResult batchResetPwd(@RequestBody java.util.Map<String, Object> body) {
        requireAdmin();
        String password = body.get("password") == null ? null : body.get("password").toString();
        return AjaxResult.success(userService.batchResetPwd(toUserIdList(body.get("userIds")), password));
    }

    /**
     * 批量分配负责教师
     */
    @OperLog(title = "用户管理-批量分配教师", businessType = OperLog.UPDATE)
    @PutMapping("/batchAssignTeacher")
    public AjaxResult batchAssignTeacher(@RequestBody java.util.Map<String, Object> body) {
        requireAdmin();
        Object teacherRaw = body.get("teacherId");
        Long teacherId = teacherRaw == null ? null : Long.valueOf(teacherRaw.toString());
        return toAjax(userService.batchAssignTeacher(toUserIdList(body.get("userIds")), teacherId));
    }

    /** 将请求体中的 userIds（JSON 数组）安全转为 Long 列表 */
    private java.util.List<Long> toUserIdList(Object raw) {
        java.util.List<Long> userIds = new java.util.ArrayList<>();
        if (raw instanceof java.util.List) {
            for (Object item : (java.util.List<?>) raw) {
                if (item instanceof Number) {
                    userIds.add(((Number) item).longValue());
                } else if (item != null) {
                    userIds.add(Long.valueOf(item.toString()));
                }
            }
        }
        return userIds;
    }

    /** 校验当前用户是否为系统管理员 */
    private void requireAdmin() {
        if (!SecurityUtils.isAdmin()) {
            throw new ServiceException("无权限操作，仅系统管理员可管理用户");
        }
    }
}
