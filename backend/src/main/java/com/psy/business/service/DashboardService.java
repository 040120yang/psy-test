package com.psy.business.service;

import java.util.Map;

/**
 * 系统概览与待办工作台业务层
 */
public interface DashboardService {

    /** 统计概览数据 */
    Map<String, Object> stats();

    /**
     * 待办工作台数据
     *
     * @return 包含 scope 与四类待办区块（pendingChat / overdueFollow / pendingEvaluation / weekRisk）
     */
    Map<String, Object> workbench();
}
