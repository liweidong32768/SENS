package com.liuyanzhao.sens.service;

import com.liuyanzhao.sens.entity.Tag;

import java.util.List;

/**
 * <pre>
 *     标签业务逻辑接口
 * </pre>
 *
 * @author : saysky
 * @date : 2018/1/12
 */
public interface TagService {

    /**
     * 新增/修改标签
     *
     * @param tag tag
     * @return Tag
     */
    Tag saveByTag(Tag tag);

    /**
     * 根据编号移除标签
     *
     * @param tagId tagId
     * @return Tag
     */
    void removeByTagId(Long tagId);

    /**
     * 获取所有标签
     *
     * @return List
     */
    List<Tag> findAllTags();

    /**
     * 热门标签
     *
     * @return 标签列表
     */
    List<Tag> findHotTags(Integer limit);


    /**
     * 根据编号查询标签
     *
     * @param tagId tagId
     * @return Optional
     */
    Tag findByTagId(Long tagId);

    /**
     * 根据标签路径查询
     *
     * @param tagUrl tagUrl
     * @return Tag
     */
    Tag findByTagUrl(String tagUrl);

    /**
     * 根据标签名称查询
     *
     * @param tagName tagName
     * @return Tag
     */
    Tag findTagByTagName(String tagName);

    /**
     * 转换标签字符串为实体集合
     *
     * @param tagList tagList
     * @return List
     */
    List<Tag> strListToTagList(String tagList);

    /**
     * 根据文章Id获得标签列表
     *
     * @param postId 文章id
     * @return 分类列表
     */
    List<Tag> findByPostId(Long postId);

    /**
     * 查询数量
     *
     * @return 标签数量
     */
    Integer getCount();
}
