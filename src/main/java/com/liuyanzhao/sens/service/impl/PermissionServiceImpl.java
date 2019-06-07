package com.liuyanzhao.sens.service.impl;

import com.liuyanzhao.sens.entity.Permission;
import com.liuyanzhao.sens.mapper.PermissionMapper;
import com.liuyanzhao.sens.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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
public class PermissionServiceImpl implements PermissionService {

    private static final String PERMISSIONS_CACHE_NAME = "permissions";

    @Autowired(required = false)
    private PermissionMapper permissionMapper;

    @Override
    @CacheEvict(value = PERMISSIONS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Permission saveByPermission(Permission permission) {
        if (permission != null && permission.getId() != null) {
            permissionMapper.updateById(permission);
        } else {
            permissionMapper.insert(permission);
        }
        return permission;
    }

    @Override
    @CacheEvict(value = PERMISSIONS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void removeByPermissionId(Integer permissionId) {
        permissionMapper.deleteById(permissionId);
    }

    @Override
    @Cacheable(value = PERMISSIONS_CACHE_NAME, key = "'permissions_id_'+#permissionId", unless = "#result == null")
    public Permission findByPermissionId(Integer permissionId) {
        return permissionMapper.selectById(permissionId);
    }

    @Override
    @Cacheable(value = PERMISSIONS_CACHE_NAME, key = "'permissions_roleid_'+#roleId", unless = "#result == null")
    public List<Permission> listPermissionsByRoleId(Integer roleId) {
        return permissionMapper.findByRoleId(roleId);
    }

    @Override
    @Cacheable(value = PERMISSIONS_CACHE_NAME, key = "'permissions_all'")
    public List<Permission> findAll() {
        return permissionMapper.findAll();
    }

}
