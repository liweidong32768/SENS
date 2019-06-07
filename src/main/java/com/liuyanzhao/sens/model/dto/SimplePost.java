package com.liuyanzhao.sens.model.dto;


import com.liuyanzhao.sens.entity.Category;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author 言曌
 * @date 2019/2/5 下午9:18
 */
@Data
public class SimplePost implements Serializable {

    private static final long serialVersionUID = 6519475879522585036L;

    /**
     * 文章编号
     */
    private Long postId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 文章标题
     */
    private String postTitle;

    /**
     * 文章类型
     * post  文章
     * page  页面
     */
    private String postType;

    /**
     * 文章摘要
     */
    private String postSummary;


    /**
     * 缩略图
     */
    private String postThumbnail;

    /**
     * 发表日期
     */
    private Date postDate;

    /**
     * 0 已发布
     * 1 草稿
     * 2 回收站
     */
    private Integer postStatus;

    /**
     * 文章访问量
     */
    private Long postViews;

    /**
     * 点赞访问量
     */
    private Long postLikes;

    /**
     * 评论数量(冗余字段，加快查询速度)
     */
    private Long commentSize;

    /**
     * 分类列表
     */
    List<Category> categories;

}
