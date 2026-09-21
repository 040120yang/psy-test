package com.psy.framework.web.service;

import com.psy.common.constant.Constants;
import com.psy.common.exception.ServiceException;
import com.psy.common.utils.StringUtils;
import com.psy.framework.security.LoginUser;
import com.psy.framework.security.SecurityUtils;
import com.psy.framework.security.TokenService;
import com.psy.system.domain.SysRole;
import com.psy.system.domain.SysUser;
import com.psy.system.mapper.SysRoleMapper;
import com.psy.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 登录校验方法（参照若依 SysLoginService）
 */
@Service
public class SysLoginService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private com.psy.business.mapper.PatientMapper patientMapper;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    /**
     * 登录校验：验证码 -> 账号密码 -> 生成令牌
     */
    public String login(String username, String password, String code, String uuid) {
        // 1. 校验验证码
        if (!captchaService.verify(uuid, code)) {
            throw new ServiceException("验证码错误或已过期");
        }
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new ServiceException("用户名或密码不能为空");
        }
        // 2. 校验账号密码
        SysUser user = userMapper.selectUserByUsername(username);
        if (user == null || !SecurityUtils.matchesPassword(password, user.getPassword())) {
            throw new ServiceException("用户名或密码错误");
        }
        if (Constants.STATUS_DISABLE.equals(user.getStatus())) {
            throw new ServiceException("账号已停用，请联系管理员");
        }
        // 3. 组装登录用户
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setUsername(user.getUsername());
        loginUser.setPassword(user.getPassword());
        loginUser.setNickname(user.getNickname());
        loginUser.setSex(user.getSex());
        loginUser.setAge(user.getAge());
        loginUser.setRoleId(user.getRoleId());
        loginUser.setLoginTime(new Date());
        SysRole role = roleMapper.selectRoleById(user.getRoleId());
        if (role != null) {
            loginUser.setRoleKey(role.getRoleKey());
            loginUser.setRoleName(role.getRoleName());
        }
        // 4. 生成令牌
        String token = tokenService.createToken(loginUser);
        // 5. 记录登录日志
        try {
            jdbcTemplate.update("INSERT INTO sys_login_log (user_name, ipaddr, status, msg) VALUES (?, ?, 0, '登录成功')",
                username, "127.0.0.1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }

    /**
     * 用户注册：默认注册为公众用户（role_id = 3）
     */
    public void register(String username, String password, String nickname, String sex, Integer age, String phone) {
        if (StringUtils.isEmpty(username)) {
            throw new ServiceException("用户名不能为空");
        }
        if (StringUtils.isEmpty(password)) {
            throw new ServiceException("密码不能为空");
        }
        if (password.length() < 6) {
            throw new ServiceException("密码长度不能少于6位");
        }
        if (userMapper.countByUsername(username) > 0) {
            throw new ServiceException("用户名已存在，请更换");
        }
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(SecurityUtils.encryptPassword(password));
        user.setNickname(StringUtils.isEmpty(nickname) ? username : nickname);
        user.setRoleId(3L);
        user.setSex(StringUtils.isEmpty(sex) ? "2" : sex);
        user.setAge(age);
        user.setPhone(phone);
        user.setStatus(Constants.STATUS_NORMAL);
        user.setRemark("自助注册用户");
        userMapper.insertUser(user);

        // 自动创建患者档案（仅公众用户）
        com.psy.business.domain.Patient patient = new com.psy.business.domain.Patient();
        patient.setUserId(user.getUserId());
        patient.setPatientName(StringUtils.isEmpty(nickname) ? username : nickname);
        patient.setSex(StringUtils.isEmpty(sex) ? "2" : sex);
        patient.setAge(age);
        patient.setPhone(phone);
        patientMapper.insertPatient(patient);
    }
}
