package com.psy.business.service.impl;

import com.psy.business.domain.Patient;
import com.psy.business.mapper.PatientMapper;
import com.psy.business.service.PatientService;
import com.psy.common.core.PageUtils;
import com.psy.common.core.TableDataInfo;
import com.psy.common.exception.ServiceException;
import com.psy.common.utils.StringUtils;
import com.psy.framework.security.SecurityUtils;
import com.psy.system.domain.SysRole;
import com.psy.system.domain.SysUser;
import com.psy.system.mapper.SysRoleMapper;
import com.psy.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 学生信息业务实现
 */
@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Override
    public TableDataInfo list(Patient patient) {
        // 学生用户只能查看/管理关联到自己的学生
        if (SecurityUtils.isUser()) {
            patient.setUserId(SecurityUtils.getUserId());
        }
        PageUtils.startPage();
        List<Patient> list = patientMapper.selectPatientList(patient);
        return PageUtils.getDataTable(list);
    }

    @Override
    @Transactional
    public int add(Patient patient) {
        if (StringUtils.isEmpty(patient.getPatientName())) {
            throw new ServiceException("学生姓名不能为空");
        }
        if (SecurityUtils.isUser()) {
            patient.setUserId(SecurityUtils.getUserId());
        } else {
            createStudentAccount(patient);
        }
        return patientMapper.insertPatient(patient);
    }

    @Override
    @Transactional
    public int edit(Patient patient) {
        if (patient.getPatientId() == null) {
            throw new ServiceException("学生ID不能为空");
        }
        Patient exist = patientMapper.selectPatientById(patient.getPatientId());
        if (exist == null) {
            throw new ServiceException("学生不存在");
        }
        // 学生用户只能修改自己的学生
        if (SecurityUtils.isUser() && exist.getUserId() != null
                && !SecurityUtils.getUserId().equals(exist.getUserId())) {
            throw new ServiceException("无权限修改该学生信息");
        }
        // 同步更新 sys_user（昵称/性别/年龄/手机号）
        if (exist.getUserId() != null) {
            SysUser userUpdate = new SysUser();
            userUpdate.setUserId(exist.getUserId());
            userUpdate.setNickname(patient.getPatientName());
            userUpdate.setSex(patient.getSex());
            userUpdate.setAge(patient.getAge());
            userUpdate.setPhone(patient.getPhone());
            userMapper.updateUser(userUpdate);
        } else if (!SecurityUtils.isUser()) {
            createStudentAccount(patient);
        }
        return patientMapper.updatePatient(patient);
    }

    @Override
    @Transactional
    public int remove(Long patientId) {
        Patient exist = patientMapper.selectPatientById(patientId);
        if (exist == null) {
            throw new ServiceException("学生不存在");
        }
        if (SecurityUtils.isUser() && exist.getUserId() != null
                && !SecurityUtils.getUserId().equals(exist.getUserId())) {
            throw new ServiceException("无权限删除该学生信息");
        }
        int rows = patientMapper.deletePatientById(patientId);
        if (!SecurityUtils.isUser() && exist.getUserId() != null) {
            userMapper.deleteUserById(exist.getUserId());
        }
        return rows;
    }

    private void createStudentAccount(Patient patient) {
        String username = StringUtils.trim(patient.getUsername());
        if (StringUtils.isEmpty(username)) {
            username = StringUtils.isNotEmpty(patient.getIdCard())
                    ? StringUtils.trim(patient.getIdCard()) : StringUtils.trim(patient.getPhone());
        }
        if (StringUtils.isEmpty(username)) {
            throw new ServiceException("登录账号不能为空，请填写登录账号、学号或手机号");
        }
        SysUser existingUser = userMapper.selectUserByUsernameIncludingDeleted(username);
        if (existingUser != null && !"2".equals(existingUser.getDelFlag())) {
            throw new ServiceException("登录账号已存在，请更换账号");
        }
        SysRole studentRole = roleMapper.selectRoleByKey("user");
        if (studentRole == null) {
            throw new ServiceException("学生角色不存在，请先配置学生角色");
        }

        String password = StringUtils.isEmpty(patient.getPassword()) ? "123456" : patient.getPassword();
        if (password.length() < 6) {
            throw new ServiceException("初始密码长度不能少于6位");
        }
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(SecurityUtils.encryptPassword(password));
        user.setNickname(patient.getPatientName());
        user.setRoleId(studentRole.getRoleId());
        user.setSex(StringUtils.isEmpty(patient.getSex()) ? "2" : patient.getSex());
        user.setAge(patient.getAge());
        user.setPhone(patient.getPhone());
        user.setStatus("0");
        user.setRemark("学生管理创建");
        if (existingUser == null) {
            userMapper.insertUser(user);
        } else {
            user.setUserId(existingUser.getUserId());
            userMapper.restoreUser(user);
        }

        patient.setUserId(user.getUserId());
        patient.setUsername(username);
    }
}
