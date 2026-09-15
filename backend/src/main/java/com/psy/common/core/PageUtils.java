package com.psy.common.core;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.psy.common.utils.ServletUtils;

import java.util.List;

/**
 * 分页工具类（参照若依 PageUtils，基于 PageHelper）
 */
public class PageUtils {

    /**
     * 设置分页参数（从请求参数 pageNum / pageSize 读取）
     */
    public static void startPage() {
        int pageNum = ServletUtils.getParameterToInt("pageNum", 1);
        int pageSize = ServletUtils.getParameterToInt("pageSize", 10);
        PageHelper.startPage(pageNum, pageSize);
    }

    /**
     * 获取分页结果（total + rows）
     */
    public static TableDataInfo getDataTable(List<?> list) {
        PageInfo<?> pageInfo = new PageInfo<>(list);
        return TableDataInfo.success(list, pageInfo.getTotal());
    }
}
