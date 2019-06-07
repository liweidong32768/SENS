package com.liuyanzhao.sens.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <pre>
 *     文章／页面
 * </pre>
 *
 * @author : saysky
 * @date : 2017/11/14
 */
@Data
@TableName("sens_post")
public class Post implements Serializable {

    private static final long serialVersionUID = -6019684584665869629L;

    /**
     * 文章编号
     */
    @TableId(type = IdType.AUTO)
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
    private String postType = "post";

    /**
     * 文章内容 html格式
     */
    private String postContent;

    /**
     * 文章路径
     */
    private String postUrl;

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
     * 最后一次更新时间
     */
    private Date postUpdate;

    /**
     * 0 已发布
     * 1 草稿
     * 2 回收站
     */
    private Integer postStatus = 0;

    /**
     * 文章访问量
     */
    private Long postViews = 0L;

    /**
     * 点赞访问量
     */
    private Long postLikes = 0L;

    /**
     * 评论数量(冗余字段，加快查询速度)
     */
    private Long commentSize = 0L;

    /**
     * 文章来源（原创1，转载2，翻译3）
     */
    private Integer postSource = 1;

    /**
     * 是否允许评论（允许1，禁止0）
     */
    private Integer allowComment = 1;
//
//    /**
//     * 文章访问密码
//     */
//    private String postPassword;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    public Date getPostDate() {
        return postDate;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    public Date getPostUpdate() {
        return postUpdate;
    }

    /**
     * 发表用户 多对一
     */
    @TableField(exist = false)
    private User user;

    /**
     * 文章所属分类
     */
    @TableField(exist = false)
    private List<Category> categories = new ArrayList<>();

    /**
     * 文章所属标签
     */
    @TableField(exist = false)
    private List<Tag> tags = new ArrayList<>();

    /**
     * 文章的评论
     */
    @TableField(exist = false)
    private List<Comment> comments = new ArrayList<>();

}
