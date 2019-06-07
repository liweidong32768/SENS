package com.liuyanzhao.sens.service.impl;

import com.liuyanzhao.sens.entity.UserRoleRef;
import com.liuyanzhao.sens.mapper.UserRoleRefMapper;
import com.liuyanzhao.sens.service.UserRoleRefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;


/**
 * @author 言曌
 * @date 2019/1/25 下午8:09
 */
@Service
public class UserRoleRefServiceImpl implements UserRoleRefService {

    private static final String ROLES_CACHE_NAME = "roles";

    @Autowired(required = false)
    private UserRoleRefMapper roleRefMapper;

    @Override
    public void saveByUserRoleRef(UserRoleRef userRoleRef) {
        roleRefMapper.insert(userRoleRef);
    }

    @Override
    @CacheEvict(value = ROLES_CACHE_NAME, key = "'roles_uid_'+#userId")
    public void deleteByUserId(Long userId) {
        roleRefMapper.deleteByUserId(userId);
    }

}
