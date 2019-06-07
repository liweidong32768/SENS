package com.liuyanzhao.sens.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.liuyanzhao.sens.entity.Post;
import com.liuyanzhao.sens.model.dto.Archive;
import com.liuyanzhao.sens.model.dto.PostBriefDto;
import com.liuyanzhao.sens.model.dto.PostViewsDto;
import com.liuyanzhao.sens.model.dto.SimplePost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


import java.util.Date;
import java.util.List;

/**
 * @author liuyanzhao
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 查询所有
     *
     * @return List
     */
    List<Post> findAll();

    /**
     * 查询前五条文章
     *
     * @return List
     */
    List<Post> findTopFive();

    /**
     * 查询所有文章 根据文章类型
     *
     * @param postType post or page
     * @return List
     */
    List<Post> findPostsByPostType(String postType);

    /**
     * 根据URL查询
     *
     * @param postUrl postUrl
     * @return List
     */
    Post findPostByPostUrl(String postUrl);

    /**
     * 获得年列表
     *
     * @return 年列表
     */
    List<Integer> listYears();

    /**
     * 分页查询文章
     *
     * @param postType post or page
     * @param page     分页信息
     * @return Page
     */
    List<Post> pagingPostsByPostType(@Param("postType") String postType, Pagination page);

    /**
     * 模糊查询
     *
     * @param keyword keyword
     * @param page    pageable
     * @return List
     */
    List<Post> pagingByPostTitleLike(@Param("keyword") String keyword, Pagination page);

    /**
     * 根据文章的状态查询 分页
     *
     * @param status   0，1，2
     * @param postType post or page
     * @param page     分页信息
     * @return Page
     */
    List<Post> pagingPostsByPostStatusAndPostType(@Param("status") Integer status,
                                                  @Param("postType") String postType,
                                                  Pagination page);


    /**
     * 根据文章的状态查询 分页
     *
     * @param status   0，1，2
     * @param postType post or page
     * @param page     分页信息
     * @return Page
     */
    List<Post> pagingPostsByUserIdAndPostStatusAndPostType(@Param("userId") Long userId,
                                                           @Param("status") Integer status,
                                                           @Param("postType") String postType, Pagination page);

    /**
     * 根据文章的状态和类型查询,带有文章内容， 分页
     *
     * @param status   0，1，2
     * @param postType post or page
     * @param page     分页信息
     * @return Page
     */
    List<Post> pagingPostsWithContentByPostStatusAndPostType(@Param("status") Integer status,
                                                             @Param("postType") String postType,
                                                             Pagination page);

    /**
     * 根据Post的状态查询
     *
     * @param status   0,1,2
     * @param postType post or page
     * @return List
     */
    List<Post> findPostsByPostStatusAndPostType(@Param("status") Integer status, @Param("postType") String postType);

    /**
     * 根据路径查询文章
     *
     * @param postUrl  路径
     * @param postType post or page
     * @return Post
     */
    Post findPostByPostUrlAndPostType(@Param("postUrl") String postUrl, @Param("postType") String postType);

    /**
     * 根据文章编号查询
     *
     * @param postId   文章编号
     * @param postType post or page
     * @return Post
     */
    Post findPostByPostIdAndPostType(@Param("postId") Long postId, @Param("postType") String postType);


    /**
     * 查询文章归档信息 根据年份和月份
     *
     * @return List
     */
    List<Archive> findPostGroupByYearAndMonth();

    /**
     * 查询文章归档信息 根据年份
     *
     * @return List
     */
    List<Archive> findPostGroupByYear();

    /**
     * 根据年份和月份查询文章
     *
     * @param year  year
     * @param month month
     * @return List
     */
    List<PostBriefDto> findPostByYearAndMonth(@Param("year") String year, @Param("month") String month);

    /**
     * 根据年份查询文章
     *
     * @param year year
     * @return List
     */
    List<PostBriefDto> findPostByYear(@Param("year") String year);

    /**
     * 根据年份和月份查询文章 分页
     *
     * @param year  year
     * @param month month
     * @param page  分页
     * @return Page
     */
    List<Post> pagingPostByYearAndMonth(@Param("year") String year, @Param("month") String month, Pagination page);

    /**
     * 根据分类目录查询文章
     *
     * @param cateIds 分类id集合
     * @param status  status
     * @param page    分页
     * @return Page
     */
    List<Post> pagingPostByCategoryIdsAndPostStatus(@Param("cateIds") List<Long> cateIds,
                                                    @Param("status") Integer status,
                                                    Pagination page);

    /**
     * 根据分类目录查询文章,不分页
     *
     * @param cateId 分类id
     * @param status status
     * @return Page
     */
    List<Post> findPostByCategoryIdAndPostStatus(@Param("cateId") Long cateId,
                                                 @Param("status") Integer status);

    /**
     * 根据标签查询文章，分页
     *
     * @param tagId  tagId
     * @param status status
     * @param page   分页
     * @return Page
     */
    List<Post> pagingPostsByTagIdAndPostStatus(@Param("tagId") Long tagId, @Param("status") Integer status, Pagination page);

    /**
     * 根据标签查询文章
     *
     * @param tagId tagId
     * @return List
     */
    List<Post> findPostsByTagId(Long tagId);

    /**
     * 模糊查询文章
     *
     * @param keyword 关键词
     * @param page    分页信息
     * @return Page
     */
    @Deprecated
    List<Post> pagingPostByPostTitleLikeOrPostContentLikeAndPostTypeAndPostStatus(@Param("keyword") String keyword, Pagination page);

    /**
     * 按热度从大到小排序
     *
     * @param postStatus 文章状态
     * @return List<Post>
     */
    List<Post> findPostsByPostTypeOrderByPostViewsDesc(String postStatus);

    /**
     * 获取所有文章阅读量总和
     *
     * @return Long
     */
    Long getPostViewsSum();

    /**
     * 根据文章状态查询数量
     *
     * @param status   文章状态
     * @param postType 文章类型
     * @return 文章数量
     */
    Integer countByPostStatusAndPostType(@Param("status") Integer status,
                                         @Param("postType") String postType);

    /**
     * 根据文章状态查询数量
     *
     * @param userId   用户Id
     * @param status   文章状态
     * @param postType 文章类型
     * @return 文章数量
     */
    Integer countByUserIdAndPostStatusAndPostType(@Param("userId") Long userId,
                                                  @Param("status") Integer status,
                                                  @Param("postType") String postType);

    /**
     * 根据分类目录查询文章
     *
     * @param cateId 分类id
     * @return 文章列表
     */
    List<Post> findPostsByCategoryId(Long cateId);

    /**
     * 查询之后的一篇文章或公告
     *
     * @param postId Id
     * @param postType 类型
     * @return Post
     */
    Post findByPostIdAfter(@Param("postId") Long postId, @Param("postType") String postType);

    /**
     * 查询之前的一篇文章或公告
     *
     * @param postId Id
     * @param postType 类型
     * @return Post
     */
    Post findByPostIdBefore(@Param("postId") Long postId, @Param("postType") String postType);

    /**
     * 更新文章访问量
     *
     * @param postId 文章Id
     * @param views  访问量
     * @return 影响行数
     */
    @Deprecated
    Integer updatePostViews(@Param("postId") Long postId, @Param("views") Long views);

    /**
     * 批量更新文章访问量
     *
     * @param dtoList List
     * @return 影响行数
     */
    Integer batchUpdatePostViews(List<PostViewsDto> dtoList);

    /**
     * 查询最大的更新
     *
     * @return
     */
    Date selectMaxPostUpdate();

    /**
     * 获得所有的PostId列表
     *
     * @return PostId列表
     */
    List<Long> selectAllPostIds();

    /**
     * 重置评论数量
     *
     * @return 数量
     */
    Integer updateCommentSize(Long postId);

    /**
     * 更新文章状态
     *
     * @param postId 文章Id
     * @param status 状态
     * @return 影响行数
     */
    Integer updatePostStatus(@Param("postId") Long postId,
                             @Param("status") Integer status);

    /**
     * 更新文章状态
     *
     * @param postId 文章Id
     * @return 影响行数
     */
    Integer updatePostLikes(@Param("postId") Long postId);

    /**
     * 更新Summary
     *
     * @param postId  文章Id
     * @param summary 摘要
     * @return 影响行数
     */
    Integer updatePostSummary(@Param("postId") Long postId, @Param("summary") String summary);


    /**
     * 根据用户Id删除
     *
     * @return 影响行数
     */
    Integer deleteByUserId(Long userId);

    /**
     * 根据用户Id和状态获得文章
     *
     * @param userId 用户Id
     * @param status 状态
     * @return
     */
    List<Post> findByUserIdAndStatus(@Param("userId") Long userId,
                                     @Param("status") Integer status,
                                     Pagination pagination);

    /**
     * 统计某个用户的文章数
     *
     * @param userId 用户Id
     * @return 数量
     */
    Integer countByUserId(Long userId);

    /**
     * 获得某个用户的所有文章Id
     *
     * @param userId 用户Id
     * @return Id列表
     */
    List<Long> selectIdsByUserId(Long userId);
}

