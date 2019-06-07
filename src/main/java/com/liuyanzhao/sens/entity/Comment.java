package com.liuyanzhao.sens.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <pre>
 *     评论
 * </pre>
 *
 * @author : saysky
 * @date : 2018/1/22
 */
@Data
@TableName("sens_comment")
public class Comment implements Serializable {

    private static final long serialVersionUID = -5292578270152491355L;

    /**
     * 评论id 自增
     */
    @TableId(type = IdType.AUTO)
    private Long commentId;

    /**
     * 文章ID
     */
    private Long postId;

    /**
     * 评论人
     */
    @NotBlank(message = "评论用户名不能为空")
    private String commentAuthor;

    /**
     * 评论人的邮箱
     */
    @Email(message = "邮箱格式不正确")
//    @JsonIgnore
    private String commentAuthorEmail;

    /**
     * 评论人的主页
     */
    private String commentAuthorUrl;

    /**
     * 评论人的ip
     */
//    @JsonIgnore
    private String commentAuthorIp;

    /**
     * Email的md5，用于gavatar
     */
    private String commentAuthorEmailMd5;

    /**
     * 评论人头像
     */
    private String commentAuthorAvatar;


    /**
     * 评论时间
     */
    private Date commentDate;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String commentContent;

    /**
     * 评论者ua信息
     */
    private String commentAgent;

    /**
     * 上一级
     */
    private Long commentParent = 0L;

    /**
     * 评论状态，0：正常，1：待审核，2：回收站
     */
    private Integer commentStatus = 1;

    /**
     * 是否是博主的评论 0:不是 1:是
     */
    private Integer isAdmin;

    /**
     * 0匿名评论
     */
    private Long userId;
    /**
     * 关系路径
     */
    private String pathTrace;

    /**
     * 接受者用户Id
     */
    private Long acceptUserId;
    /**
     * 评论文章
     */
    @TableField(exist = false)
    private Post post;

    /**
     * 当前评论下的所有子评论
     */
    @TableField(exist = false)
    private List<Comment> childComments;

}
