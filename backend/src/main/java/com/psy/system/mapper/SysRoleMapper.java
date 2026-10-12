package com.psy.system.mapper;

import com.psy.system.domain.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色表数据层
 */
@Mapper
public interface SysRoleMapper {

    /** 根据ID查询角色 */
    @Select("select * from sys_role where role_id = #{roleId}")
    SysRole selectRoleById(@Param("roleId") Long roleId);

    /** 根据权限字符串查询角色 */
    @Select("select * from sys_role where role_key = #{roleKey} limit 1")
    SysRole selectRoleByKey(@Param("roleKey") String roleKey);

    /** 查询全部角色 */
    @Select("select * from sys_role order by sort_no")
    List<SysRole> selectRoleList();
}
