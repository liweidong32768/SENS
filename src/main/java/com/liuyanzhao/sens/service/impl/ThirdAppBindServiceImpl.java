package com.liuyanzhao.sens.service.impl;

import com.liuyanzhao.sens.entity.ThirdAppBind;
import com.liuyanzhao.sens.mapper.ThirdAppBindMapper;
import com.liuyanzhao.sens.service.ThirdAppBindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <pre>
 *     第三方应用绑定业务逻辑实现类
 * </pre>
 *
 * @author : saysky
 * @date : 2017/11/14
 */
@Service
public class ThirdAppBindServiceImpl implements ThirdAppBindService {

    private static final String BINDS_CACHE_NAME = "app_binds";

    @Autowired(required = false)
    private ThirdAppBindMapper thirdAppBindMapper;

    @Override
    @CacheEvict(value = BINDS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public ThirdAppBind saveByThirdAppBind(ThirdAppBind thirdAppBind) {
        if (thirdAppBind != null && thirdAppBind.getId() != null) {
            thirdAppBindMapper.updateById(thirdAppBind);
        } else {
            thirdAppBindMapper.insert(thirdAppBind);
        }
        return thirdAppBind;
    }

    @Override
    @CacheEvict(value = BINDS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void removeById(Long thirdAppBindId) {
        thirdAppBindMapper.deleteById(thirdAppBindId);
    }

    @Override
    @Cacheable(value = BINDS_CACHE_NAME, key = "'app_bind_type_'+#appType+'_openid_'+#openId", unless = "#result == null")
    public ThirdAppBind findByAppTypeAndOpenId(String appType, String openId) {
        return thirdAppBindMapper.findByAppTypeAndOpenId(appType, openId);
    }

    @Override
    @Cacheable(value = BINDS_CACHE_NAME, key = "'app_bind_id_'+#thirdAppBindId")
    public ThirdAppBind findByThirdAppBindId(Long thirdAppBindId) {
        return thirdAppBindMapper.selectById(thirdAppBindId);
    }

    @Override
    @Cacheable(value = BINDS_CACHE_NAME, key = "'app_bind_uid_'+#userId")
    public List<ThirdAppBind> findByUserId(Long userId) {
        return thirdAppBindMapper.findByUserId(userId);
    }

}
