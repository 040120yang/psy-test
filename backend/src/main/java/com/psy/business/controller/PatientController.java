package com.psy.business.controller;

import com.psy.business.domain.Patient;
import com.psy.business.service.PatientService;
import com.psy.common.core.AjaxResult;
import com.psy.common.core.TableDataInfo;
import com.psy.system.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 患者信息管理
 */
@RestController
@RequestMapping("/patient")
public class PatientController extends BaseController {

    @Autowired
    private PatientService patientService;

    /**
     * 分页查询患者列表
     */
    @GetMapping("/list")
    public TableDataInfo list(Patient patient) {
        return patientService.list(patient);
    }

    /**
     * 新增患者
     */
    @PostMapping
    public AjaxResult add(@RequestBody Patient patient) {
        return toAjax(patientService.add(patient));
    }

    /**
     * 修改患者
     */
    @PutMapping
    public AjaxResult edit(@RequestBody Patient patient) {
        return toAjax(patientService.edit(patient));
    }

    /**
     * 删除患者
     */
    @DeleteMapping("/{patientId}")
    public AjaxResult remove(@PathVariable Long patientId) {
        return toAjax(patientService.remove(patientId));
    }
}
