package com.liuyanzhao.sens.service.impl;

import com.liuyanzhao.sens.entity.Permission;
import com.liuyanzhao.sens.entity.Role;
import com.liuyanzhao.sens.entity.RolePermissionRef;
import com.liuyanzhao.sens.mapper.RoleMapper;
import com.liuyanzhao.sens.service.RolePermissionRefService;
import com.liuyanzhao.sens.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     角色业务逻辑实现类
 * </pre>
 *
 * @author : saysky
 * @date : 2017/11/14
 */
@Service
public class RoleServiceImpl implements RoleService {

    private static final String ROLES_CACHE_NAME = "roles";

    private static final String ROLES_COUNT_CACHE_NAME = "roles_count";

    private static final String MENUS_ADMIN_CACHE_NAME = "menus_admin";

    private static final String PERMISSIONS_CACHE_NAME = "permissions";


    @Autowired(required = false)
    private RoleMapper roleMapper;

    @Autowired
    private RolePermissionRefService rolePermissionRefService;

    @Override
    @CacheEvict(value = {ROLES_CACHE_NAME, MENUS_ADMIN_CACHE_NAME, PERMISSIONS_CACHE_NAME}, allEntries = true, beforeInvocation = true)
    @Transactional(rollbackFor = Exception.class)
    public Role saveByRole(Role role) {
        if (role != null && role.getId() != null) {
            roleMapper.updateById(role);
            if (role.getPermissions() != null && role.getPermissions().size() != 0) {
                rolePermissionRefService.deleteRefByRoleId(role.getId());
                List<RolePermissionRef> rolePermissionRefs = new ArrayList<>(role.getPermissions().size());
                for (Permission permission : role.getPermissions()) {
                    rolePermissionRefs.add(new RolePermissionRef(role.getId(), permission.getId()));
                }
                rolePermissionRefService.batchSaveByRolePermissionRef(rolePermissionRefs);
            }
        } else {
            roleMapper.insert(role);
            if (role.getPermissions() != null && role.getPermissions().size() != 0) {
                List<RolePermissionRef> rolePermissionRefs = new ArrayList<>(role.getPermissions().size());
                for (Permission permission : role.getPermissions()) {
                    rolePermissionRefs.add(new RolePermissionRef(role.getId(), permission.getId()));
                }
                rolePermissionRefService.batchSaveByRolePermissionRef(rolePermissionRefs);
            }
        }
        return role;
    }

    @Override
    @CacheEvict(value = {ROLES_CACHE_NAME, MENUS_ADMIN_CACHE_NAME}, allEntries = true, beforeInvocation = true)
    public void removeByRoleId(Integer roleId) {
        roleMapper.deleteById(roleId);
    }

    @Override
    @CacheEvict(value = {ROLES_CACHE_NAME, MENUS_ADMIN_CACHE_NAME}, allEntries = true, beforeInvocation = true)
    public void removeByUserId(Long userId) {
        roleMapper.deleteByUserId(userId);
    }

    @Override
    @Cacheable(value = ROLES_CACHE_NAME, key = "'roles_id_'+#roleId", unless = "#result == null")
    public Role findByRoleId(Integer roleId) {
        return roleMapper.selectById(roleId);
    }

    @Override
    @Cacheable(value = ROLES_CACHE_NAME, key = "'roles_name_'+#roleName", unless = "#result == null")
    public Role findByRoleName(String roleName) {
        return roleMapper.findByRoleName(roleName);
    }

    @Override
    @Cacheable(value = ROLES_CACHE_NAME, key = "'roles_uid_'+#userId", unless = "#result == null")
    public List<Role> listRolesByUserId(Long userId) {
        return roleMapper.findByUserId(userId);
    }

    @Override
    @Cacheable(value = ROLES_CACHE_NAME, key = "'roles_all'")
    public List<Role> findAll() {
        //获得角色
        List<Role> roles = roleMapper.findAll();
        //封装count
        roles.forEach(role -> role.setCount(countUserByRoleId(role.getId())));
        return roles;
    }

    @Override
    @Cacheable(value = ROLES_COUNT_CACHE_NAME, key = "'roles_count_user_id_'+#roleId")
    public Integer countUserByRoleId(Integer roleId) {
        return roleMapper.countUserByRoleId(roleId);
    }

}
