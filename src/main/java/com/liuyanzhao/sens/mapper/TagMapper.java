package com.liuyanzhao.sens.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.liuyanzhao.sens.entity.Category;
import com.liuyanzhao.sens.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author liuyanzhao
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 查询所有
     * @return
     */
    List<Tag> findAll();


    /**
     * 根据标签路径查询，用于验证是否已经存在该路径
     *
     * @param tagUrl tagUrl
     * @return Tag
     */
    Tag findTagByTagUrl(String tagUrl);

    /**
     * 根据标签名称查询
     *
     * @param tagName 标签名
     * @return Tag
     */
    Tag findTagByTagName(String tagName);

    /**
     * 获得某篇文章的标签列表
     *
     * @param postId 文章Id
     * @return List
     */
    List<Tag> findByPostId(Long postId);

    /**
     * 获得所有包括统计文章数
     *
     * @return 标签列表
     */
    List<Tag> findAllWithCount();

    /**
     * 查询没有用过的标签
     *
     * @return 标签列表
     */
    List<Tag> findTagNotUse();
}

