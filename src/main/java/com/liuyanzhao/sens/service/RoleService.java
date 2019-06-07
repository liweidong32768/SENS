package com.liuyanzhao.sens.service;

import com.liuyanzhao.sens.entity.Role;
import com.liuyanzhao.sens.entity.User;

import java.util.List;

/**
 * <pre>
 *     角色逻辑接口
 * </pre>
 *
 * @author : saysky
 * @date : 2017/11/14
 */
public interface RoleService {

    /**
     * 新增/修改角色
     *
     * @param role role
     * @return Role
     */
    Role saveByRole(Role role);

    /**
     * 根据编号删除
     *
     * @param roleId roleId
     * @return Role
     */
    void removeByRoleId(Integer roleId);

    /**
     * 删除某个用户的所有关联
     *
     * @param userId 用户Id
     */
    void removeByUserId(Long userId);
    /**
     * 根据编号查询单个权限
     *
     * @param roleId roleId
     * @return Role
     */
    Role findByRoleId(Integer roleId);

    /**
     * 根据编号查询单个权限
     *
     * @param roleName roleName
     * @return Role
     */
    Role findByRoleName(String roleName);

    /**
     * 根据用户Id获得角色列表
     *
     * @param userId 用户Id
     * @return 角色列表
     */
    List<Role> listRolesByUserId(Long userId);

    /**
     * 获得所有的角色
     *
     * @return 角色列表
     */
    List<Role> findAll();

    /**
     * 统计这个角色的用户数
     *
     * @param roleId 角色Id
     */
    Integer countUserByRoleId(Integer roleId);

}
