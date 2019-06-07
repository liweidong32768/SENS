package com.liuyanzhao.sens.service;

import com.liuyanzhao.sens.entity.Permission;

import java.util.List;

/**
 * <pre>
 *     权限逻辑接口
 * </pre>
 *
 * @author : saysky
 * @date : 2017/11/14
 */
public interface PermissionService {

    /**
     * 新增/修改权限
     *
     * @param permission permission
     * @return Permission
     */
    Permission saveByPermission(Permission permission);

    /**
     * 根据编号删除
     *
     * @param permissionId permissionId
     * @return Permission
     */
    void removeByPermissionId(Integer permissionId);

    /**
     * 根据编号查询单个权限
     *
     * @param permissionId permissionId
     * @return Permission
     */
    Permission findByPermissionId(Integer permissionId);

    /**
     * 根据角色Id获得权限列表
     *
     * @param roleId 角色Id
     * @return 权限列表
     */
    List<Permission> listPermissionsByRoleId(Integer roleId);

    /**
     * 获得所有的权限
     *
     * @return 权限列表
     */
    List<Permission> findAll();


}
