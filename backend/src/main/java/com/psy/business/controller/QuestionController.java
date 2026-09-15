package com.psy.business.controller;

import com.psy.business.domain.Question;
import com.psy.business.service.QuestionService;
import com.psy.common.core.AjaxResult;
import com.psy.common.core.TableDataInfo;
import com.psy.common.exception.ServiceException;
import com.psy.framework.security.SecurityUtils;
import com.psy.system.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 量表题目管理
 */
@RestController
@RequestMapping("/system/question")
public class QuestionController extends BaseController {

    @Autowired
    private QuestionService questionService;

    /**
     * 分页查询题目列表
     */
    @GetMapping("/list")
    public TableDataInfo list(Question question) {
        return questionService.list(question);
    }

    /**
     * 根据量表ID查询全部题目
     */
    @GetMapping("/listByScale")
    public AjaxResult listByScale(@RequestParam Long scaleId) {
        return AjaxResult.success(questionService.listByScale(scaleId));
    }

    /**
     * 新增题目
     */
    @PostMapping
    public AjaxResult add(@RequestBody Question question) {
        requireAdmin();
        return toAjax(questionService.add(question));
    }

    /**
     * 修改题目
     */
    @PutMapping
    public AjaxResult edit(@RequestBody Question question) {
        requireAdmin();
        return toAjax(questionService.edit(question));
    }

    /**
     * 删除题目
     */
    @DeleteMapping("/{questionId}")
    public AjaxResult remove(@PathVariable Long questionId) {
        requireAdmin();
        return toAjax(questionService.remove(questionId));
    }

    private void requireAdmin() {
        if (!SecurityUtils.isAdmin()) {
            throw new ServiceException("无权限操作，仅系统管理员可管理题目");
        }
    }
}
