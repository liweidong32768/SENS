package com.liuyanzhao.sens.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.liuyanzhao.sens.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * @author liuyanzhao
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据角色Id获得权限列表
     *
     * @param roleId 角色Id
     * @return 权限列表
     */
    List<Permission> findByRoleId(Integer roleId);

    /**
     * 获得权限列表
     *
     * @return 权限列表
     */
    List<Permission> findAll();

    /**
     * 获得后台菜单（侧边栏）
     *
     * @param roleId 角色Id
     * @return 权限
     */
    List<Permission> getMenuByRoleId(Integer roleId);
}

