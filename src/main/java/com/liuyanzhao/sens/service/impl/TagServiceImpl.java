package com.liuyanzhao.sens.service.impl;

import com.liuyanzhao.sens.entity.Tag;
import com.liuyanzhao.sens.mapper.PostTagRefMapper;
import com.liuyanzhao.sens.mapper.TagMapper;
import com.liuyanzhao.sens.service.TagService;
import com.liuyanzhao.sens.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     标签业务逻辑实现类
 * </pre>
 *
 * @author : saysky
 * @date : 2018/1/12
 */
@Service
public class TagServiceImpl implements TagService {

    @Autowired(required = false)
    private TagMapper tagMapper;

    @Autowired(required = false)
    private PostTagRefMapper postTagRefMapper;

    @Autowired
    private RedisUtil redisUtil;

    private static final String TAGS_CACHE_NAME = "tags";

    private static final String POSTS_CACHE_NAME = "posts";

    /**
     * 新增/修改标签
     *
     * @param tag tag
     * @return Tag
     */
    @Override
    @CacheEvict(value = {POSTS_CACHE_NAME, TAGS_CACHE_NAME}, allEntries = true, beforeInvocation = true)
    public Tag saveByTag(Tag tag) {
        if (tag != null && tag.getTagId() != null) {
            tagMapper.updateById(tag);
        } else {
            tagMapper.insert(tag);
        }
        return tag;
    }

    /**
     * 根据编号移除标签
     *
     * @param tagId tagId
     * @return Tag
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {POSTS_CACHE_NAME, TAGS_CACHE_NAME}, allEntries = true, beforeInvocation = true)
    public void removeByTagId(Long tagId) {
        Tag tag = this.findByTagId(tagId);
        if (tag != null) {
            //1.删除标签和文章的关联
            postTagRefMapper.deleteByTagId(tagId);
            //2.删除标签
            tagMapper.deleteById(tagId);
        }
    }

    /**
     * 获取所有标签
     *
     * @return List
     */
    @Override
    @Cacheable(value = TAGS_CACHE_NAME, key = "'tags_all'")
    public List<Tag> findAllTags() {
        //这种情况会出现标签对应文章为0的不显示
        List<Tag> tags = tagMapper.findAllWithCount();
        //查询没被用过的标签
        List<Tag> tagsNotUse = tagMapper.findTagNotUse();
        tagsNotUse.forEach(tag -> tag.setCount(0));
        tags.addAll(tagsNotUse);
        return tags;
    }

    @Override
    @Cacheable(value = TAGS_CACHE_NAME, key = "'tags_hot'")
    public List<Tag> findHotTags(Integer limit) {
        List<Tag> tags = tagMapper.findAllWithCount();
        if (tags.size() > limit) {
            return new ArrayList<>(tags.subList(0, limit - 1));
        }
        return tags;
    }

    /**
     * 根据编号查询标签
     *
     * @param tagId tagId
     * @return Optional
     */
    @Override
    @Cacheable(value = TAGS_CACHE_NAME, key = "'tags_id_'+#tagId", unless = "#result == null")
    public Tag findByTagId(Long tagId) {
        return tagMapper.selectById(tagId);
    }

    /**
     * 根据标签路径查询
     *
     * @param tagUrl tagUrl
     * @return Tag
     */
    @Override
    @Cacheable(value = TAGS_CACHE_NAME, key = "'tags_url_'+#tagUrl", unless = "#result == null")
    public Tag findByTagUrl(String tagUrl) {
        return tagMapper.findTagByTagUrl(tagUrl);
    }

    /**
     * 根据标签名称查询
     *
     * @param tagName tagName
     * @return Tag
     */
    @Override
    @Cacheable(value = TAGS_CACHE_NAME, key = "'tags_name_'+#tagName", unless = "#result == null")
    public Tag findTagByTagName(String tagName) {
        return tagMapper.findTagByTagName(tagName);
    }

    /**
     * 转换标签字符串为实体集合
     *
     * @param tagList tagList
     * @return List
     */
    @Override
    public List<Tag> strListToTagList(String tagList) {
        String[] tags = tagList.split(",");
        List<Tag> tagsList = new ArrayList<>();
        for (String tag : tags) {
            Tag t = findTagByTagName(tag);
            Tag nt = null;
            if (null != t) {
                tagsList.add(t);
            } else {
                nt = new Tag();
                nt.setTagName(tag);
                nt.setTagUrl(tag);
                tagsList.add(saveByTag(nt));
            }
        }
        return tagsList;
    }

    @Override
    @Cacheable(value = TAGS_CACHE_NAME, key = "'tags_postid_'+#postId")
    public List<Tag> findByPostId(Long postId) {
        return tagMapper.findByPostId(postId);
    }

    @Override
    @Cacheable(value = TAGS_CACHE_NAME, key = "'tags_count'")
    public Integer getCount() {
        return tagMapper.selectCount(null);
    }

}
