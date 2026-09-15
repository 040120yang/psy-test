package com.psy.business.service;

import java.util.Map;

/**
 * 系统概览统计业务层
 */
public interface DashboardService {

    /** 统计概览数据 */
    Map<String, Object> stats();
}
