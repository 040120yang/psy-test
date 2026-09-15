package com.psy.business.controller;

import com.psy.business.service.DashboardService;
import com.psy.common.core.AjaxResult;
import com.psy.common.exception.ServiceException;
import com.psy.framework.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 系统概览（统计面板）
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
        if (!SecurityUtils.isAdmin() && !SecurityUtils.isDoctor()) {
            throw new ServiceException("无权限查看系统概览");
        }
        Map<String, Object> stats = dashboardService.stats();
        return AjaxResult.success(stats);
    }
}
