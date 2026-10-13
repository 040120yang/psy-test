package com.psy.business.controller;

import com.psy.business.domain.Question;
import com.psy.business.service.QuestionService;
import com.psy.common.core.AjaxResult;
import com.psy.common.core.TableDataInfo;
import com.psy.common.exception.ServiceException;
import com.psy.framework.aspectj.annotation.OperLog;
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
     * 分页查询题目列表（管理端题目管理使用）
     */
    @GetMapping("/list")
    public TableDataInfo list(Question question) {
        requireAdmin();
        return questionService.list(question);
    }

    /**
     * 根据量表ID查询全部题目（管理端题目管理使用）
     */
    @GetMapping("/listByScale")
    public AjaxResult listByScale(@RequestParam Long scaleId) {
        requireAdmin();
        return AjaxResult.success(questionService.listByScale(scaleId));
    }

    /**
     * 新增题目
     */
    @OperLog(title = "题目管理", businessType = OperLog.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Question question) {
        requireAdmin();
        return toAjax(questionService.add(question));
    }

    /**
     * 修改题目
     */
    @OperLog(title = "题目管理", businessType = OperLog.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Question question) {
        requireAdmin();
        return toAjax(questionService.edit(question));
    }

    /**
     * 删除题目
     */
    @OperLog(title = "题目管理", businessType = OperLog.DELETE)
    @DeleteMapping("/{questionId}")
    public AjaxResult remove(@PathVariable Long questionId) {
        requireAdmin();
        return toAjax(questionService.remove(questionId));
    }

    /**
     * 导出量表下全部题目为 CSV 文件
     */
    @OperLog(title = "题目管理-导出", businessType = OperLog.OTHER)
    @GetMapping("/export")
    public void export(@RequestParam Long scaleId, javax.servlet.http.HttpServletResponse response) throws Exception {
        requireAdmin();
        String csv = questionService.exportCsv(scaleId);
        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=questions_" + scaleId + ".csv");
        response.getWriter().write(csv);
    }

    /**
     * 下载导入模板 CSV
     */
    @GetMapping("/template")
    public void template(javax.servlet.http.HttpServletResponse response) throws Exception {
        requireAdmin();
        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=question_template.csv");
        response.getWriter().write("\ufeff题号,题目内容,反向计分\n1,示例题目：我感到紧张不安,0\n2,示例题目：我容易感到疲劳,1\n");
    }

    /**
     * 批量导入题目 CSV（multipart 文件，mode=overwrite|append）
     */
    @OperLog(title = "题目管理-导入", businessType = OperLog.INSERT)
    @PostMapping("/import")
    public AjaxResult importCsv(@RequestParam Long scaleId,
                                @RequestParam(defaultValue = "append") String mode,
                                @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        requireAdmin();
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("请选择要导入的 CSV 文件");
        }
        try {
            String content = new String(file.getBytes(), "UTF-8");
            boolean overwrite = "overwrite".equalsIgnoreCase(mode);
            int count = questionService.importCsv(scaleId, content, overwrite);
            return AjaxResult.success("成功导入 " + count + " 道题目");
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            return AjaxResult.error("导入失败：" + e.getMessage());
        }
    }

    private void requireAdmin() {
        if (!SecurityUtils.isAdmin()) {
            throw new ServiceException("无权限操作，仅系统管理员可管理题目");
        }
    }
}
