package com.liuyanzhao.sens.service;

import com.liuyanzhao.sens.entity.Role;
import com.liuyanzhao.sens.entity.UserRoleRef;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;

/**
 * @author 言曌
 * @date 2019/1/25 下午8:07
 */

public interface UserRoleRefService {


    /**
     * 添加用户和角色关联
     *
     * @param userRoleRef UserRoleRef
     * @return UserRoleRef
     */
    void saveByUserRoleRef(UserRoleRef userRoleRef);

    /**
     * 根据用户Id删除
     *
     * @param userId 用户Id
     */
    void deleteByUserId(Long userId);


}
