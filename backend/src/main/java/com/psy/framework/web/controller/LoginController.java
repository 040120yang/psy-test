package com.psy.framework.web.controller;

import com.psy.common.core.AjaxResult;
import com.psy.common.exception.ServiceException;
import com.psy.framework.security.LoginUser;
import com.psy.framework.security.SecurityUtils;
import com.psy.framework.security.TokenService;
import com.psy.framework.web.service.SysLoginService;
import com.psy.system.domain.SysUser;
import com.psy.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录请求处理（参照若依 LoginController）
 */
@RestController
public class LoginController {

    @Autowired
    private SysLoginService loginService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysUserMapper userMapper;

    /**
     * 登录
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody) {
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(),
                loginBody.getCode(), loginBody.getUuid());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        return AjaxResult.success("登录成功", data);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/getInfo")
    public AjaxResult getInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new ServiceException("登录状态已失效，请重新登录");
        }
        SysUser user = userMapper.selectUserById(loginUser.getUserId());
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("roles", java.util.Collections.singletonList(loginUser.getRoleKey()));
        data.put("roleName", loginUser.getRoleName());
        return AjaxResult.success(data);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public AjaxResult logout() {
        return AjaxResult.success("退出成功");
    }

    /**
     * 注册（公众用户自助注册）
     */
    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody registerBody) {
        loginService.register(registerBody.getUsername(), registerBody.getPassword(),
                registerBody.getNickname(), registerBody.getSex(), registerBody.getAge(), registerBody.getPhone());
        return AjaxResult.success("注册成功");
    }

    /**
     * 登录请求体
     */
    public static class LoginBody {
        private String username;
        private String password;
        private String code;
        private String uuid;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }
    }

    /**
     * 注册请求体
     */
    public static class RegisterBody {
        private String username;
        private String password;
        private String nickname;
        private String sex;
        private Integer age;
        private String phone;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
