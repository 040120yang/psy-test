package com.psy.system.controller;

import com.psy.common.core.AjaxResult;

/**
 * 控制器基类（参照若依 BaseController）
 */
public class BaseController {

    /** 响应返回结果（int 行数 -> AjaxResult） */
    protected AjaxResult toAjax(int rows) {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error("操作失败");
    }
}
