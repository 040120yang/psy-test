package com.psy.business.controller;

import com.psy.business.domain.Scale;
import com.psy.business.service.ScaleService;
import com.psy.common.core.AjaxResult;
import com.psy.common.core.TableDataInfo;
import com.psy.common.exception.ServiceException;
import com.psy.framework.security.SecurityUtils;
import com.psy.system.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 量表信息（参照若依管理端接口风格）
 */
@RestController
@RequestMapping("/system/scale")
public class ScaleController extends BaseController {

    @Autowired
    private ScaleService scaleService;

    /**
     * 分页查询量表列表
     */
    @GetMapping("/list")
    public TableDataInfo list(Scale scale) {
        return scaleService.list(scale);
    }

    /**
     * 新增量表
     */
    @PostMapping
    public AjaxResult add(@RequestBody Scale scale) {
        requireAdmin();
        return toAjax(scaleService.add(scale));
    }

    /**
     * 修改量表
     */
    @PutMapping
    public AjaxResult edit(@RequestBody Scale scale) {
        requireAdmin();
        return toAjax(scaleService.edit(scale));
    }

    /**
     * 删除量表
     */
    @DeleteMapping("/{scaleId}")
    public AjaxResult remove(@PathVariable Long scaleId) {
        requireAdmin();
        return toAjax(scaleService.remove(scaleId));
    }

    private void requireAdmin() {
        if (!SecurityUtils.isAdmin()) {
            throw new ServiceException("无权限操作，仅系统管理员可管理量表");
        }
    }
}
