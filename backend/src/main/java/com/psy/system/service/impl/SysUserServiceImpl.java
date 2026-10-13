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

    @Override
    @Transactional
    public java.util.Map<String, Object> batchImport(String csvText) {
        if (StringUtils.isEmpty(csvText)) {
            throw new ServiceException("导入内容为空");
        }
        java.util.List<SysRole> roleList = roleMapper.selectRoleList();
        if (roleList == null || roleList.isEmpty()) {
            throw new ServiceException("系统中不存在可用的角色，无法导入");
        }
        java.util.List<String[]> rows = parseCsv(csvText);
        if (rows.isEmpty()) {
            throw new ServiceException("未解析到有效数据");
        }
        java.util.List<String> errors = new java.util.ArrayList<>();
        int success = 0;
        int lineNo = 0;
        for (String[] cols : rows) {
            lineNo++;
            // 列：用户名,姓名,性别,角色,手机号,初始密码
            String username = cols.length > 0 ? trim(cols[0]) : "";
            String nickname = cols.length > 1 ? trim(cols[1]) : "";
            String sexText = cols.length > 2 ? trim(cols[2]) : "";
            String roleText = cols.length > 3 ? trim(cols[3]) : "";
            String phone = cols.length > 4 ? trim(cols[4]) : "";
            String password = cols.length > 5 ? trim(cols[5]) : "";
            if (StringUtils.isEmpty(username)) {
                errors.add("第 " + lineNo + " 行：用户名为空");
                continue;
            }
            if (userMapper.selectUserByUsernameIncludingDeleted(username) != null) {
                errors.add("第 " + lineNo + " 行：账号 " + username + " 已存在");
                continue;
            }
            SysRole role = resolveRole(roleList, roleText);
            if (role == null) {
                errors.add("第 " + lineNo + " 行：角色「" + roleText + "」无法识别");
                continue;
            }
            if (StringUtils.isNotEmpty(phone) && phone.length() > 11) {
                errors.add("第 " + lineNo + " 行：手机号 " + phone + " 长度超出限制");
                continue;
            }
            if (StringUtils.isEmpty(password)) {
                password = "123456";
            }
            SysUser user = new SysUser();
            user.setUsername(username);
            user.setNickname(StringUtils.isEmpty(nickname) ? username : nickname);
            user.setPassword(SecurityUtils.encryptPassword(password));
            user.setRoleId(role.getRoleId());
            user.setSex(resolveSex(sexText));
            user.setPhone(StringUtils.isEmpty(phone) ? null : phone);
            user.setStatus("0");
            userMapper.insertUser(user);
            // 仅学生角色同步生成学生档案
            syncStudentProfile(user);
            success++;
        }
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("total", success + errors.size());
        result.put("success", success);
        result.put("fail", errors.size());
        result.put("errors", errors);
        return result;
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> batchResetPwd(java.util.List<Long> userIds, String password) {
        if (StringUtils.isEmpty(password)) {
            throw new ServiceException("新密码不能为空");
        }
        if (userIds == null || userIds.isEmpty()) {
            throw new ServiceException("请选择要重置密码的用户");
        }
        int count = 0;
        for (Long userId : userIds) {
            SysUser user = new SysUser();
            user.setUserId(userId);
            user.setPassword(SecurityUtils.encryptPassword(password));
            count += userMapper.updateUser(user);
        }
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("count", count);
        return result;
    }

    @Override
    public int batchAssignTeacher(java.util.List<Long> userIds, Long teacherId) {
        if (userIds == null || userIds.isEmpty()) {
            throw new ServiceException("请选择要分配的学生");
        }
        return patientMapper.batchAssignTeacher(userIds, teacherId);
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

    /** 性别文本归一：男/0/male → 0；女/1/female → 1；其余 → 2（未知）。数据库约定：0男 1女 2未知 */
    private String resolveSex(String text) {
        if ("男".equals(text) || "0".equals(text) || "male".equalsIgnoreCase(text)) {
            return "0";
        }
        if ("女".equals(text) || "1".equals(text) || "female".equalsIgnoreCase(text)) {
            return "1";
        }
        return "2";
    }

    /**
     * 角色文本归一：支持「角色名称」「role_key（admin/doctor/user）」直接匹配，
     * 其次模糊包含匹配，最后按关键词兜底；空值默认学生角色。无法识别返回 null。
     */
    private SysRole resolveRole(java.util.List<SysRole> roleList, String text) {
        if (StringUtils.isEmpty(text)) {
            return pickRole(roleList, "user");
        }
        String t = text.trim();
        for (SysRole r : roleList) {
            if (t.equalsIgnoreCase(r.getRoleKey()) || t.equals(r.getRoleName())) {
                return r;
            }
        }
        for (SysRole r : roleList) {
            String name = r.getRoleName() == null ? "" : r.getRoleName();
            if (!name.isEmpty() && (name.contains(t) || t.contains(name))) {
                return r;
            }
        }
        if (t.contains("管理员")) {
            return pickRole(roleList, "admin");
        }
        if (t.contains("教师") || t.contains("医生") || t.contains("辅导员") || t.contains("咨询")) {
            return pickRole(roleList, "doctor");
        }
        if (t.contains("学生")) {
            return pickRole(roleList, "user");
        }
        return null;
    }

    /** 按 role_key 取角色 */
    private SysRole pickRole(java.util.List<SysRole> roleList, String roleKey) {
        for (SysRole r : roleList) {
            if (roleKey.equalsIgnoreCase(r.getRoleKey())) {
                return r;
            }
        }
        return null;
    }

    /** 解析 CSV 文本为行列（支持引号包裹与逗号转义），自动跳过表头 */
    private java.util.List<String[]> parseCsv(String text) {
        java.util.List<String[]> result = new java.util.ArrayList<>();
        java.util.List<String> row = new java.util.ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        boolean fieldStarted = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
            } else if (c == '"' && !fieldStarted) {
                inQuotes = true;
                fieldStarted = true;
            } else if (c == ',') {
                row.add(field.toString());
                field.setLength(0);
                fieldStarted = false;
            } else if (c == '\r') {
                // 忽略
            } else if (c == '\n') {
                row.add(field.toString());
                field.setLength(0);
                fieldStarted = false;
                result.add(row.toArray(new String[0]));
                row = new java.util.ArrayList<>();
            } else {
                field.append(c);
                fieldStarted = true;
            }
        }
        if (fieldStarted || field.length() > 0) {
            row.add(field.toString());
        }
        if (!row.isEmpty()) {
            result.add(row.toArray(new String[0]));
        }
        // 跳过表头（首列含「用户名」「学号」「账号」之一）
        if (!result.isEmpty()) {
            String[] head = result.get(0);
            if (head.length > 0 && head[0] != null
                    && (head[0].contains("用户名") || head[0].contains("学号") || head[0].contains("账号"))) {
                result.remove(0);
            }
        }
        return result;
    }
}
