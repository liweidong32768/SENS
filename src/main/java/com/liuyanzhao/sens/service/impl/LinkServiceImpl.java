package com.liuyanzhao.sens.service.impl;

import com.liuyanzhao.sens.entity.Link;
import com.liuyanzhao.sens.mapper.LinkMapper;
import com.liuyanzhao.sens.service.LinkService;
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
public class LinkServiceImpl implements LinkService {

    private static final String LINKS_CACHE_NAME = "links";

    @Autowired(required = false)
    private LinkMapper linkMapper;

    @Override
    @CacheEvict(value = LINKS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Link saveByLink(Link link) {
        if (link != null && link.getLinkId() != null) {
            linkMapper.updateById(link);
        } else {
            linkMapper.insert(link);
        }
        return link;
    }

    @Override
    @CacheEvict(value = LINKS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void removeByLinkId(Long linkId) {
        linkMapper.deleteById(linkId);
    }

    @Override
    @Cacheable(value = LINKS_CACHE_NAME, key = "'links_all'")
    public List<Link> findAllLinks() {
        return linkMapper.findAll();
    }

    @Override
    @Cacheable(value = LINKS_CACHE_NAME, key = "'links_id_'+#linkId", unless = "#result == null")
    public Link findByLinkId(Long linkId) {
        return linkMapper.selectById(linkId);
    }

    @Override
    @Cacheable(value = LINKS_CACHE_NAME, key = "'links_count'")
    public Integer getCount() {
        return linkMapper.selectCount(null);
    }
}
