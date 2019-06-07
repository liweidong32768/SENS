package com.liuyanzhao.sens.service;

import com.baomidou.mybatisplus.plugins.Page;
import com.liuyanzhao.sens.entity.Category;
import com.liuyanzhao.sens.entity.Post;
import com.liuyanzhao.sens.entity.Tag;
import com.liuyanzhao.sens.model.dto.Archive;
import com.liuyanzhao.sens.model.dto.PostBriefDto;
import com.liuyanzhao.sens.model.dto.SimplePost;
import com.liuyanzhao.sens.utils.Response;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * <pre>
 *     记录/页面业务逻辑接口
 * </pre>
 *
 * @author : saysky
 * @date : 2017/11/14
 */
public interface PostService {

    /**
     * 新增记录
     *
     * @param post Post
     * @return Post
     */
    Post saveByPost(Post post);

    /**
     * 根据编号删除记录
     *
     * @param postId postId
     * @return Post
     */
    Post removeByPostId(Long postId);

    /**
     * 修改记录状态
     *
     * @param postId postId
     * @param status status
     * @return Post
     */
    Post updatePostStatus(Long postId, Integer status);

    /**
     * 修改记录阅读量
     *
     * @param postId 记录Id
     * @return 记录访问量
     */
    Long updatePostView(Long postId);

    /**
     * 批量修改摘要
     *
     * @param postSummary postSummary
     */
    void updateAllSummary(Integer postSummary);


    /**
     * 获取记录列表 不分页
     *
     * @param postType post or page
     * @return List
     */
    List<Post> findAllPosts(String postType);

    /**
     * 模糊查询记录
     *
     * @param keyWord keyword
     * @param page    page
     * @return List
     */
    Page<Post> searchPosts(String keyWord, Page<Post> page);

    /**
     * 根据记录状态查询 分页，用于后台管理
     *
     * @param status   0，1，2
     * @param postType post or page
     * @param page     分页信息
     * @return Page
     */
    Page<Post> findPostByStatus(Integer status, String postType, Page<Post> page);

    /**
     * 根据记录状态查询 分页，用于后台管理
     *
     * @param userId   用户Id
     * @param status   0，1，2
     * @param postType post or page
     * @param page     分页信息
     * @return Page
     */
    Page<Post> findPostByUserIdAndStatus(Long userId, Integer status, String postType, Page<Post> page);

    /**
     * 根据记录状态查询 分页，用于后台管理
     *
     * @param status   0，1，2
     * @param postType post or page
     * @param page     分页信息
     * @return Page
     */
    Page<Post> findPostByStatusWithContent(Integer status, String postType, Page<Post> page);

    /**
     * 根据记录状态查询 分页，首页分页
     *
     * @param page page
     * @return Page
     */
    Page<Post> findPostByStatus(Page<Post> page);

    /**
     * 根据记录状态查询，不分页，用于站点地图
     *
     * @param status   0，1，2
     * @param postType post or page
     * @return List
     */
    List<Post> findPostByStatus(Integer status, String postType);

    /**
     * 根据编号查询记录
     *
     * @param postId postId
     * @return Post
     */
    Post findByPostId(Long postId);

    /**
     * 根据编号和类型查询记录
     *
     * @param postId   postId
     * @param postType postType
     * @return Post
     */
    Post findByPostId(Long postId, String postType);

    /**
     * 根据Post路径查询
     *
     * @param postUrl  路径
     * @return Post
     */
    Post findByPostUrl(String postUrl);

    /**
     * 根据记录路径查询
     *
     * @param postUrl  路径
     * @param postType post or page
     * @return Post
     */
    Post findByPostUrl(String postUrl, String postType);

    /**
     * 查询前五条数据
     *
     * @return List
     */
    List<Post> findPostLatest();


    /**
     * 查询Id之后的记录
     *
     * @param postId 记录ID
     * @return 记录
     */
    Post findNextPost(Long postId, String postType);

    /**
     * 查询Id之前的记录
     *
     * @param postId 记录ID
     * @return 记录
     */
    Post findPreciousPost(Long postId, String postType);


    /**
     * 查询归档信息 根据年份和月份
     *
     * @return List
     */
    List<Archive> findPostGroupByYearAndMonth();

    /**
     * 查询归档信息 根据年份
     *
     * @return List
     */
    List<Archive> findPostGroupByYear();

    /**
     * 根据年份和月份查询记录
     *
     * @param year  year
     * @param month month
     * @return List
     */
    List<PostBriefDto> findPostByYearAndMonth(String year, String month);


    /**
     * 根据年份和月份查询记录 分页
     *
     * @param year  year
     * @param month month
     * @param page  page
     * @return Page
     */
    Page<Post> findPostByYearAndMonth(String year, String month, Page<Post> page);

    /**
     * 根据年份查询记录
     *
     * @param year year
     * @return List
     */
    List<PostBriefDto> findPostByYear(String year);

    /**
     * 根据分类目录查询记录
     *
     * @param category category
     * @param page     page
     * @return Page
     */
    Page<Post> findPostByCategories(Category category, Page<Post> page);

    /**
     * 根据标签查询记录
     *
     * @param tag  tag
     * @param page page
     * @return Page
     */
    Page<Post> findPostsByTags(Tag tag, Page<Post> page);

       /**
     * 搜索记录
     *
     * @param keyword 关键词
     * @param page    分页信息
     * @return Page
     */
    Response<Page<SimplePost>> searchByEs(String keyword, Page<SimplePost> page);

    /**
     * 搜索记录
     *
     * @param criteria map
     * @param page    分页信息
     * @param order   排序
     * @return Page
     */
    Response<Page<SimplePost>> findPostsByEs(HashMap<String, Object> criteria,
                                             String order,
                                             Page<SimplePost> page);


    /**
     * 热门文章
     *
     * @return List
     */
    List<Post> hotPosts();

    /**
     * 当前记录的相同标签的记录
     *
     * @param post post
     * @return List
     */
    List<Post> listSameTagPosts(Post post);


    /**
     * 当前记录的相同分类的记录
     *
     * @param post post
     * @return List
     */
    List<Post> listSameCategoryPosts(Post post);

    /**
     * 获取所有记录的阅读量
     *
     * @return Long
     */
    Long getSumPostViews();

    /**
     * 获得某篇记录的访问量
     *
     * @return Long
     * @Param postId 记录Id
     */
    Long getPostViewsByPostId(Long postId);

    /**
     * 根据状态和类型查询post数量
     *
     * @param postType 类型
     * @param status 记录状态
     * @return 记录数量
     */
    Integer countByPostTypeAndStatus(String postType, Integer status);

    /**
     * 根据状态和类型查询post,不分页
     *
     * @param postType 类型
     * @param status 记录状态
     * @return 记录数量
     */
    List<Post> findByPostTypeAndStatus(String postType, Integer status);

    /**
     * 根据记录状态查询文章数量
     *
     * @param userId 用户Id
     * @param status 记录状态
     * @return 记录数量
     */
    Integer countArticleByUserIdAndStatus(Long userId, Integer status);

    /**
     * 生成rss
     *
     * @param posts posts
     * @return String
     */
    String buildRss(List<Post> posts);

    /**
     * 生成sitemap
     *
     * @return String
     */
    String buildArticleSiteMap();


    /**
     * 同步所有记录的访问量
     *
     * @return 耗时：毫秒数
     */
    String syncAllPostView();

    /**
     * 获得最后更新时间
     *
     * @return 最后更新时间
     */
    Date getLastUpdateTime();


    /**
     * 重置所有记录的评论数量
     */
    @Deprecated
    void resetAllPostCommentSize();

    /**
     * 更新记录评论数
     *
     * @param postId 记录Id
     */
    void updateCommentSize(Long postId);

    /**
     * 更新记录点赞量
     *
     * @param postId 记录Id
     */
    void updatePostLikes(Long postId);

    /**
     * 删除用户的记录
     *
     * @param userId 用户Id
     */
    void removeByUserId(Long userId);

    /**
     * 获得某个用户已发布的记录，用于前台
     *
     * @param userId 用户Id
     * @param status 状态
     * @param page 分页信息
     * @return
     */
    @Deprecated
    Page<Post> findByUserIdAndStatus(Long userId, Integer status, Page<Post> page);


    /**
     * 统计某个用户的记录数
     * @param userId 用户Id
     * @return
     */
    Integer countByUserId(Long userId);

    /**
     * 获得一个用户发布的所有记录的Id
     *
     * @param userId 用户Id
     * @return 记录Id列表
     */
    List<Long> selectIdsByUserId(Long userId);


}
