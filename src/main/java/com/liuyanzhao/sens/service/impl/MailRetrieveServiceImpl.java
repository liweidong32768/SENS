package com.liuyanzhao.sens.service.impl;

import com.liuyanzhao.sens.entity.MailRetrieve;
import com.liuyanzhao.sens.mapper.MailRetrieveMapper;
import com.liuyanzhao.sens.mapper.MailRetrieveMapper;
import com.liuyanzhao.sens.service.MailRetrieveService;
import com.liuyanzhao.sens.service.MailRetrieveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <pre>
 *     友情链接业务逻辑实现类
 * </pre>
 *
 * @author : saysky
 * @date : 2017/11/14
 */
@Service
public class MailRetrieveServiceImpl implements MailRetrieveService {

    private static final String MAIL_RETRIEVE_CACHE_NAME = "mail_retrieves";

    @Autowired(required = false)
    private MailRetrieveMapper mailRetrieveMapper;

    @Override
    @CacheEvict(value = MAIL_RETRIEVE_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public MailRetrieve saveByMailRetrieve(MailRetrieve mailRetrieve) {
        if (mailRetrieve != null && mailRetrieve.getId() != null) {
            mailRetrieveMapper.updateById(mailRetrieve);
        } else {
            mailRetrieveMapper.insert(mailRetrieve);
        }
        return mailRetrieve;
    }

    @Override
    @CacheEvict(value = MAIL_RETRIEVE_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void removeByMailRetrieveId(Long mailRetrieveId) {
        mailRetrieveMapper.deleteById(mailRetrieveId);
    }


    @Override
    @Cacheable(value = MAIL_RETRIEVE_CACHE_NAME, key = "'mail_retrieves_id_'+#id", unless = "#result == null")
    public MailRetrieve findById(Long id) {
        return mailRetrieveMapper.selectById(id);
    }

    @Override
    @Cacheable(value = MAIL_RETRIEVE_CACHE_NAME, key = "'mail_retrieves_uid_'+#userId", unless = "#result == null")
    public MailRetrieve findLatestByUserId(Long userId) {
        return mailRetrieveMapper.findLatestByUserId(userId);
    }

    @Override
    @Cacheable(value = MAIL_RETRIEVE_CACHE_NAME, key = "'mail_retrieves_email_'+#email", unless = "#result == null")
    public MailRetrieve findLatestByEmail(String email) {
        return mailRetrieveMapper.findLatestByEmail(email);
    }

}
