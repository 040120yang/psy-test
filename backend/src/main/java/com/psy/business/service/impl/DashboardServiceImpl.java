package com.psy.business.service.impl;

import com.psy.business.mapper.DashboardMapper;
import com.psy.business.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统概览统计业务实现
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Override
    public Map<String, Object> stats() {
        Map<String, Object> result = new HashMap<>();
        result.put("userCount", dashboardMapper.countUser());
        result.put("recordCount", dashboardMapper.countRecord());
        result.put("scaleCount", dashboardMapper.countScale());
        result.put("patientCount", dashboardMapper.countPatient());
        result.put("todayCount", dashboardMapper.countToday());
        result.put("scaleDist", dashboardMapper.scaleDist());
        result.put("levelDist", dashboardMapper.levelDist());
        result.put("weekTrend", dashboardMapper.weekTrend());
        result.put("followTotal", dashboardMapper.followTotal());
        result.put("followDone", dashboardMapper.followDone());
        result.put("followOverdue", dashboardMapper.followOverdue());
        result.put("tagDist", dashboardMapper.tagDist());
        return result;
    }
}
