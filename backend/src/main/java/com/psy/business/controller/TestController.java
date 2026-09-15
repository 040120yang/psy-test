package com.psy.business.controller;

import com.psy.business.domain.Scale;
import com.psy.business.domain.Question;
import com.psy.business.service.QuestionService;
import com.psy.business.service.ScaleService;
import com.psy.business.service.TestService;
import com.psy.common.core.AjaxResult;
import com.psy.framework.security.SecurityUtils;
import com.psy.system.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 心理测评（公众用户答题端）
 */
@RestController
@RequestMapping("/test")
public class TestController extends BaseController {

    @Autowired
    private ScaleService scaleService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private TestService testService;

    /**
     * 获取全部启用的量表（答题入口）
     */
    @GetMapping("/scales")
    public AjaxResult scales() {
        return AjaxResult.success(scaleService.listEnabled());
    }

    /**
     * 获取量表题目（按题号排序）
     */
    @GetMapping("/questions")
    public AjaxResult questions(@RequestParam Long scaleId) {
        List<Question> questions = questionService.listByScale(scaleId);
        return AjaxResult.success(questions);
    }

    /**
     * 提交答卷，返回测评结果
     */
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody SubmitBody body) {
        Map<String, Object> result = testService.submit(SecurityUtils.getUserId(),
                body.getScaleId(), body.getAnswers());
        return AjaxResult.success("测评完成", result);
    }

    /**
     * 提交请求体
     */
    public static class SubmitBody {
        private Long scaleId;
        private List<TestService.AnswerItem> answers;

        public Long getScaleId() {
            return scaleId;
        }

        public void setScaleId(Long scaleId) {
            this.scaleId = scaleId;
        }

        public List<TestService.AnswerItem> getAnswers() {
            return answers;
        }

        public void setAnswers(List<TestService.AnswerItem> answers) {
            this.answers = answers;
        }
    }
}
