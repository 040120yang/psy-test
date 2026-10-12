package com.psy.system.service.impl;

import com.psy.business.domain.Patient;
import com.psy.business.mapper.PatientMapper;
import com.psy.common.core.PageUtils;
import com.psy.common.core.TableDataInfo;
import com.psy.common.exception.ServiceException;
import com.psy.common.utils.StringUtils;
import com.psy.framework.security.SecurityUtils;
import com.psy.system.domain.SysRole;
import com.psy.system.domain.SysUser;
import com.psy.system.mapper.SysRoleMapper;
import com.psy.system.mapper.SysUserMapper;
import com.psy.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户业务实现
 */
@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private PatientMapper patientMapper;

    @Override
    public TableDataInfo list(SysUser user) {
        PageUtils.startPage();
        List<SysUser> list = userMapper.selectUserList(user);
        return PageUtils.getDataTable(list);
    }

    @Override
    @Transactional
    public int add(SysUser user) {
        SysUser existingUser = userMapper.selectUserByUsernameIncludingDeleted(user.getUsername());
        if (existingUser != null && !"2".equals(existingUser.getDelFlag())) {
            throw new ServiceException("登录账号已存在");
        }
        if (StringUtils.isEmpty(user.getPassword())) {
            throw new ServiceException("密码不能为空");
        }
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        if (StringUtils.isEmpty(user.getStatus())) {
            user.setStatus("0");
        }
        int rows;
        if (existingUser == null) {
            rows = userMapper.insertUser(user);
        } else {
            user.setUserId(existingUser.getUserId());
            rows = userMapper.restoreUser(user);
        }
        syncStudentProfile(user);
        return rows;
    }

    @Override
    @Transactional
    public int edit(SysUser user) {
        if (user.getUserId() == null) {
            throw new ServiceException("用户ID不能为空");
        }
        SysUser target = userMapper.selectUserById(user.getUserId());
        if (target == null) {
            throw new ServiceException("用户不存在");
        }
        if (StringUtils.isNotEmpty(user.getPassword())) {
            user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        }
        int rows = userMapper.updateUser(user);
        syncStudentProfile(userMapper.selectUserById(user.getUserId()));
        return rows;
    }

    @Override
    @Transactional
    public int remove(Long userId) {
        if (SecurityUtils.getUserId().equals(userId)) {
            throw new ServiceException("当前录录用户不允许删除");
        }
        int rows = userMapper.deleteUserById(userId);
        patientMapper.deletePatientByUserId(userId);
        return rows;
    }

    @Override
    public int resetPwd(Long userId, String password) {
        if (StringUtils.isEmpty(password)) {
            throw new ServiceException("新密码不能为空");
        }
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setPassword(SecurityUtils.encryptPassword(password));
        return userMapper.updateUser(user);
    }

    private void syncStudentProfile(SysUser user) {
        if (user == null || user.getUserId() == null || user.getRoleId() == null) {
            return;
        }
        SysRole role = roleMapper.selectRoleById(user.getRoleId());
        if (role == null || !"user".equals(role.getRoleKey())) {
            return;
        }

        Patient patient = new Patient();
        patient.setUserId(user.getUserId());
        patient.setPatientName(StringUtils.isEmpty(user.getNickname()) ? user.getUsername() : user.getNickname());
        patient.setSex(StringUtils.isEmpty(user.getSex()) ? "2" : user.getSex());
        patient.setAge(user.getAge());
        patient.setPhone(user.getPhone());

        if (patientMapper.selectPatientByUserId(user.getUserId()) == null) {
            patientMapper.insertPatient(patient);
        } else {
            patientMapper.updatePatientByUserId(patient);
        }
    }
}
