package com.psy.system.mapper;

import com.psy.system.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户表数据层
 */
@Mapper
public interface SysUserMapper {

    /** 根据用户名查询用户 */
    @Select("select * from sys_user where username = #{username} and del_flag = '0'")
    SysUser selectUserByUsername(@Param("username") String username);

    @Select("select * from sys_user where username = #{username} limit 1")
    SysUser selectUserByUsernameIncludingDeleted(@Param("username") String username);

    /** 根据ID查询用户 */
    @Select("select * from sys_user where user_id = #{userId} and del_flag = '0'")
    SysUser selectUserById(@Param("userId") Long userId);

    /** 分页查询用户列表（含角色名称） */
    List<SysUser> selectUserList(@Param("user") SysUser user);

    /** 新增用户 */
    int insertUser(SysUser user);

    /** 修改用户 */
    int updateUser(SysUser user);

    @Update("update sys_user set password = #{password}, nickname = #{nickname}, role_id = #{roleId}, " +
            "sex = #{sex}, age = #{age}, phone = #{phone}, status = #{status}, del_flag = '0', " +
            "remark = #{remark} where user_id = #{userId}")
    int restoreUser(SysUser user);

    /** 删除用户（逻辑删除） */
    int deleteUserById(@Param("userId") Long userId);

    /** 校验用户名唯一 */
    @Select("select count(1) from sys_user where username = #{username} and del_flag = '0'")
    int countByUsername(@Param("username") String username);
}
