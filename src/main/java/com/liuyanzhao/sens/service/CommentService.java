package com.liuyanzhao.sens.service;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.liuyanzhao.sens.entity.Comment;

import java.util.List;

/**
 * <pre>
 *     评论业务逻辑接口
 * </pre>
 *
 * @author : saysky
 * @date : 2018/1/22
 */
public interface CommentService {

    /**
     * 新增评论
     *
     * @param comment comment
     */
    void saveByComment(Comment comment);

    /**
     * 删除评论
     *
     * @param commentId commentId
     * @return 评论
     */
    void removeByCommentId(Long commentId);

    /**
     * 根据用户Id删除评论
     *
     * @param userId 用户Id
     */
    void removeByUserId(Long userId);

    /**
     * 查询所有的评论，用于后台管理
     *
     * @param status status
     * @param page   page
     * @return Page
     */
    Page<Comment> pagingByStatus(Integer status, Page<Comment> page);

    /**
     * 获得某个用户发的的评论
     *
     * @param userId 用户Id
     * @param status 状态
     * @param page 分页信息
     * @return 评论
     */
    Page<Comment> pagingBySendUserAndStatus(Long userId, Integer status, Page<Comment> page);

    /**
     * 获得某个用户应该收到的评论
     *
     * @param userId 用户Id
     * @param status 状态
     * @param page 分页信息
     * @return 评论
     */
    Page<Comment> pagingByAcceptUserAndStatus(Long userId, Integer status, Page<Comment> page);

    /**
     * 根据评论状态查询评论
     *
     * @param userId 用户Id
     * @param status 评论状态
     * @return List
     */
    List<Comment> findByAcceptUserAndStatus(Long userId, Integer status);


    /**
     * 更改评论的状态
     *
     * @param commentId commentId
     * @param status    status
     * @return Comment
     */
    Comment updateCommentStatus(Long commentId, Integer status);

    /**
     * 根据评论编号查询评论
     *
     * @param commentId commentId
     * @return 评论
     */
    Comment findCommentById(Long commentId);

    /**
     * 根据文章和评论状态查询评论 分页
     *
     * @param postId 文章id
     * @param page   page
     * @param status status
     * @return Page
     */
    Page<Comment> pagingCommentsByPostAndCommentStatus(Long postId, Integer status, Page<Comment> page);

    /**
     * 根据文章和评论状态查询评论 不分页
     *
     * @param postId 文章id
     * @param status status
     * @return List
     */
    List<Comment> findCommentsByPostAndCommentStatus(Long postId, Integer status);

    /**
     * 根据文章和评论状态（为不查询的）查询评论 不分页
     *
     * @param postId 文章id
     * @param status status
     * @return List
     */
    List<Comment> findCommentsByPostAndCommentStatusNot(Long postId, Integer status);

    /**
     * 查询最新的前五条评论
     *
     * @return List
     */
    List<Comment> findCommentsLatest(Integer limit);


    /**
     * 根据评论状态查询数量
     *
     * @param status 评论状态
     * @return 评论数量
     */
    Integer countByStatus(Integer status);

    /**
     * 获得前50条评论
     *
     * @return 评论列表
     */
    List<Comment> findCommentsTop50();

    /**
     * 根据评论统计评论数
     *
     * @param userId 用户Id
     * @return 数量
     */
    Integer countByUserId(Long userId);

    /**
     * 统计评论数量
     *
     * @param userId 用户Id
     * @param status 状态
     * @return 数量
     */
    Integer countByReceiveUserAndStatus(Long userId, Integer status);

    /**
     * 获得某个ip最新评论
     *
     * @param ip ip地址
     * @return 评论
     */
    Comment getLastestComment(String ip);

}
