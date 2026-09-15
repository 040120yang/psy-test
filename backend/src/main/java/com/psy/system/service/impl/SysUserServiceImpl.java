package com.psy.system.service.impl;

import com.psy.common.core.PageUtils;
import com.psy.common.core.TableDataInfo;
import com.psy.common.exception.ServiceException;
import com.psy.common.utils.StringUtils;
import com.psy.framework.security.SecurityUtils;
import com.psy.system.domain.SysUser;
import com.psy.system.mapper.SysUserMapper;
import com.psy.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户业务实现
 */
@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public TableDataInfo list(SysUser user) {
        PageUtils.startPage();
        List<SysUser> list = userMapper.selectUserList(user);
        return PageUtils.getDataTable(list);
    }

    @Override
    public int add(SysUser user) {
        if (userMapper.countByUsername(user.getUsername()) > 0) {
            throw new ServiceException("登录账号已存在");
        }
        if (StringUtils.isEmpty(user.getPassword())) {
            throw new ServiceException("密码不能为空");
        }
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        if (StringUtils.isEmpty(user.getStatus())) {
            user.setStatus("0");
        }
        return userMapper.insertUser(user);
    }

    @Override
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
        return userMapper.updateUser(user);
    }

    @Override
    public int remove(Long userId) {
        if (SecurityUtils.getUserId().equals(userId)) {
            throw new ServiceException("当前登录用户不允许删除");
        }
        return userMapper.deleteUserById(userId);
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
}
