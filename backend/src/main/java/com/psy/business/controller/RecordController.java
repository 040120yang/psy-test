package com.psy.business.controller;

import com.psy.business.domain.TestRecord;
import com.psy.business.service.RecordService;
import com.psy.common.core.AjaxResult;
import com.psy.common.core.TableDataInfo;
import com.psy.system.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 测评记录
 */
@RestController
@RequestMapping("/test/record")
public class RecordController extends BaseController {

    @Autowired
    private RecordService recordService;

    /**
     * 我的测评记录
     */
    @GetMapping("/my")
    public TableDataInfo my() {
        return recordService.myRecords();
    }

    /**
     * 全部测评记录（管理端）
     */
    @GetMapping("/list")
    public TableDataInfo list(TestRecord record) {
        return recordService.allRecords(record);
    }

    /**
     * 记录详情（含答题明细）
     */
    @GetMapping("/{recordId}")
    public AjaxResult detail(@PathVariable Long recordId) {
        Map<String, Object> detail = recordService.detail(recordId);
        return AjaxResult.success(detail);
    }

    /**
     * 删除记录
     */
    @DeleteMapping("/{recordId}")
    public AjaxResult remove(@PathVariable Long recordId) {
        return toAjax(recordService.remove(recordId));
    }
}
