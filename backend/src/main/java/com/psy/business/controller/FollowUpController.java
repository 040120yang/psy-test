package com.psy.business.controller;

import com.psy.business.domain.FollowUp;
import com.psy.business.service.FollowUpService;
import com.psy.common.core.AjaxResult;
import com.psy.common.core.TableDataInfo;
import com.psy.common.exception.ServiceException;
import com.psy.framework.aspectj.annotation.OperLog;
import com.psy.framework.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 随访管理
 *
 * 权限边界：随访的建立、调整、完成与删除属于心理教师（doctor）与系统管理员（admin）的业务；
 * 学生仅可查看自己的随访记录。
 */
@RestController
@RequestMapping("/follow")
public class FollowUpController {

    @Autowired
    private FollowUpService followUpService;

    /** 管理端：随访列表 */
    @GetMapping("/admin/list")
    public TableDataInfo adminList(@RequestParam(required = false) String status) {
        requireStaff();
        return followUpService.adminList(status);
    }

    /** 学生端：我的随访 */
    @GetMapping("/my")
    public TableDataInfo myList() {
        return followUpService.myList();
    }

    /** 详情 */
    @GetMapping("/{id}")
    public AjaxResult detail(@PathVariable Long id) {
        requireStaff();
        return AjaxResult.success(followUpService.getById(id));
    }

    /** 新增随访任务 */
    @OperLog(title = "随访管理", businessType = OperLog.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FollowUp followUp) {
        requireStaff();
        return AjaxResult.success(followUpService.add(followUp));
    }

    /** 修改随访 */
    @OperLog(title = "随访管理", businessType = OperLog.UPDATE)
    @PutMapping
    public AjaxResult update(@RequestBody FollowUp followUp) {
        requireStaff();
        return AjaxResult.success(followUpService.update(followUp));
    }

    /** 完成随访 */
    @OperLog(title = "随访管理-完成随访", businessType = OperLog.UPDATE)
    @PutMapping("/finish")
    public AjaxResult finish(@RequestBody Map<String, Object> body) {
        requireStaff();
        if (body.get("id") == null) {
            throw new ServiceException("随访ID不能为空");
        }
        Long id = Long.valueOf(body.get("id").toString());
        String doctorNote = (String) body.get("doctorNote");
        Integer symptomScore = body.get("symptomScore") != null ? Integer.valueOf(body.get("symptomScore").toString()) : null;
        String nextFollowDate = (String) body.get("nextFollowDate");
        return AjaxResult.success(followUpService.finish(id, doctorNote, symptomScore, nextFollowDate));
    }

    /** 删除随访 */
    @OperLog(title = "随访管理", businessType = OperLog.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        requireStaff();
        return AjaxResult.success(followUpService.delete(id));
    }

    /** 逾期随访 */
    @GetMapping("/overdue")
    public TableDataInfo overdue() {
        requireStaff();
        return followUpService.overdue();
    }

    /** 校验当前用户是否为心理教师或系统管理员 */
    private void requireStaff() {
        if (!SecurityUtils.isDoctor() && !SecurityUtils.isAdmin()) {
            throw new ServiceException("无权限操作，随访管理仅心理教师或系统管理员可使用");
        }
    }
}
