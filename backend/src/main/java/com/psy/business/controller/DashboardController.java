package com.psy.business.controller;

import com.psy.business.service.DashboardService;
import com.psy.common.core.AjaxResult;
import com.psy.common.exception.ServiceException;
import com.psy.framework.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统概览（统计面板）与待办工作台
 *

 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 统计概览数据
     */
    @GetMapping("/stats")
    public AjaxResult stats() {
        requireStaff();
        return AjaxResult.success(dashboardService.stats());
    }

    /**
     * 待办工作台：待处理沟通、逾期随访、待填写评估结论、本周新增中/重度测评
     */
    @GetMapping("/workbench")
    public AjaxResult workbench() {
        requireStaff();
        return AjaxResult.success(dashboardService.workbench());
    }

    /** 校验当前用户是否为心理教师或系统管理员 */
    private void requireStaff() {
        if (!SecurityUtils.isDoctor() && !SecurityUtils.isAdmin()) {
            throw new ServiceException("无权限查看，仅心理教师或系统管理员可访问");
        }
    }
}
