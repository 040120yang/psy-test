package com.psy.framework.web.controller;

import com.psy.common.core.AjaxResult;
import com.psy.framework.web.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 验证码操作处理
 */
@RestController
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    /**
     * 生成验证码
     */
    @GetMapping("/captchaImage")
    public AjaxResult getCode() {
        Map<String, String> captcha = captchaService.getCaptchaImage();
        return AjaxResult.success(captcha);
    }
}
