package com.liuyanzhao.sens.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.liuyanzhao.sens.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author liuyanzhao
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 查询所有
     *
     * @return
     */
    List<Comment> findAll();

    /**
     * 根据评论状态查询所有评论 分页
     *
     * @param status 文章状态
     * @param page   分页信息
     * @return Page
     */
    List<Comment> pagingByCommentStatus(@Param("status") Integer status,
                                        Pagination page);

    /**
     * 根据评论状态查询所有评论 分页
     *
     * @param status 文章状态
     * @param page   分页信息
     * @return Page
     */
    List<Comment> pagingByUserIdAndCommentStatus(@Param("userId") Long userId,
                                                 @Param("status") Integer status,
                                                 Pagination page);

    /**
     * 根据评论状态查询所有评论 不分页
     *
     * @param status 文章状态
     * @return List
     */
    List<Comment> findByCommentStatus(@Param("status") Integer status);

    /**
     * 根据文章查询评论
     *
     * @param postId post
     * @param page   分页信息
     * @return Page
     */
    List<Comment> findByPostId(@Param("postId") Long postId, Pagination page);

    /**
     * 根据文章和评论状态查询评论 分页
     *
     * @param postId 文章ID
     * @param status status
     * @param page   分页信息
     * @return Page
     */
    List<Comment> pagingByPostIdAndCommentStatus(@Param("postId") Long postId,
                                                 @Param("status") Integer status,
                                                 Pagination page);

    /**
     * 根据文章和评论状态查询评论 不分页
     *
     * @param postId 文章ID
     * @param status status
     * @return List
     */
    List<Comment> findByPostIdAndCommentStatus(@Param("postId") Long postId,
                                               @Param("status") Integer status);

    /**
     * 根据文章和评论状态不包括查询评论 分页
     *
     * @param postId 文章ID
     * @param status status
     * @param page   分页信息
     * @return Page
     */
    List<Comment> pagingByPostIdAndCommentStatusNot(@Param("postId") Long postId,
                                                    @Param("status") Integer status,
                                                    Pagination page);

    /**
     * 根据文章和评论状态不包括查询评论 不分页
     *
     * @param postId 文章ID
     * @param status status
     * @return List
     */
    List<Comment> findByPostIdAndCommentStatusNot(@Param("postId") Long postId,
                                                  @Param("status") Integer status);


    /**
     * 查询十条评论，状态不等于这个的
     *
     * @return List
     */
    List<Comment> findTopTenByStatusNot(@Param("status") Integer status,
                                        @Param("limit") Integer limit);

    /**
     * 根据状态查询十条评论
     *
     * @return List
     */
    List<Comment> findTopTenByStatus(@Param("status") Integer status,
                                     @Param("limit") Integer limit);

    /**
     * 根据评论状态查询数量
     *
     * @param status 评论状态
     * @return 评论数量
     */
    Integer countByStatus(@Param("status") Integer status);

    /**
     * 获得子评论Id列表
     *
     * @param pathTrace 评论pathTrace封装
     * @return 评论Id列表
     */
    List<Long> selectChildCommentIds(@Param("pathTrace") String pathTrace);

    /**
     * 统计某篇文章的评论数量
     *
     * @param postId 文章Id
     * @return 数量
     */
    Integer countByPostId(Long postId);

    /**
     * 更新评论状态
     *
     * @param commentId 评论Id
     * @param status    状态
     * @return 影响行数
     */
    Integer updateCommentStatus(@Param("commentId") Long commentId,
                                @Param("status") Integer status);

    /**
     * 查询前limit条评论
     *
     * @param limit 查询数量
     * @return 评论列表
     */
    List<Comment> findCommentByLimit(Integer limit);

    /**
     * 根据用户Id删除
     *
     * @param userId 用户Id
     * @return 影响行数
     */
    Integer deleteByUserId(Long userId);

    /**
     * 根据用户Id获得评论数
     *
     * @param userId 用户Id
     * @return 数量
     */
    Integer countByUserId(Long userId);

    /**
     * 根据文章Id列表获得评论
     *
     * @param postIds 文章Id列表
     * @return 评论列表
     */
    List<Comment> findByPostIds(@Param("postIds") List<Long> postIds,
                                @Param("status") Integer status);

    /**
     * 根据文章Id列表获得评论
     *
     * @param postIds 文章Id列表
     * @return 评论列表
     */
    Integer countByPostIds(@Param("postIds") List<Long> postIds,
                           @Param("status") Integer status);

    /**
     * 根据文章和评论状态不包括查询评论 分页
     *
     * @param acceptUserId 接受人Id
     * @param status       status
     * @param page         分页信息
     * @return Page
     */
    List<Comment> pagingByAcceptUserIdAndCommentStatus(@Param("acceptUserId") Long acceptUserId,
                                                       @Param("status") Integer status,
                                                       Pagination page);

    /**
     * 获得某个ip用户最新的评论
     *
     * @param ip IP地址
     * @return 评论
     */
    Comment getLastestComment(String ip);
}

