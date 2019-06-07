package com.liuyanzhao.sens.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.plugins.Page;
import com.liuyanzhao.sens.entity.*;
import com.liuyanzhao.sens.mapper.PostCategoryRefMapper;
import com.liuyanzhao.sens.mapper.PostMapper;
import com.liuyanzhao.sens.mapper.PostTagRefMapper;
import com.liuyanzhao.sens.model.dto.Archive;
import com.liuyanzhao.sens.model.dto.PostBriefDto;
import com.liuyanzhao.sens.model.dto.PostViewsDto;
import com.liuyanzhao.sens.model.dto.SimplePost;
import com.liuyanzhao.sens.model.enums.PostStatusEnum;
import com.liuyanzhao.sens.model.enums.PostTypeEnum;
import com.liuyanzhao.sens.service.CategoryService;
import com.liuyanzhao.sens.service.PostService;
import com.liuyanzhao.sens.utils.RedisUtil;
import com.liuyanzhao.sens.utils.Response;
import com.liuyanzhao.sens.utils.SensUtils;
import cn.hutool.http.HtmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <pre>
 *     记录业务逻辑实现类
 * </pre>
 *
 * @author : saysky
 * @date : 2017/11/14
 */
@Service
@Slf4j
public class PostServiceImpl implements PostService {

    private static final String POSTS_CACHE_NAME = "posts";

    private static final String CATEGORIES_CACHE_NAME = "categories";

    private static final String TAGS_CACHE_NAME = "tags";


    @Autowired(required = false)
    private PostMapper postMapper;

    @Autowired(required = false)
    private PostCategoryRefMapper postCategoryRefMapper;

    @Autowired(required = false)
    private PostTagRefMapper postTagRefMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RestHighLevelClient highLevelClient;


    @Override
    @CacheEvict(value = {POSTS_CACHE_NAME, TAGS_CACHE_NAME, CATEGORIES_CACHE_NAME}, allEntries = true, beforeInvocation = true)
    @Transactional(rollbackFor = Exception.class)
    public Post saveByPost(Post post) {
        post.setPostUpdate(new Date());
        //更新记录
        if (post != null && post.getPostId() != null) {
            postMapper.updateById(post);
            if (post.getCategories() != null) {
                //添加分类和记录关联
                postCategoryRefMapper.deleteByPostId(post.getPostId());
                redisUtil.del("posts_categories::posts_categories_postid_" + post.getPostId());
                //删除分类和记录关联
                for (int i = 0; i < post.getCategories().size(); i++) {
                    PostCategoryRef postCategoryRef = new PostCategoryRef(post.getPostId(), post.getCategories().get(i).getCateId());
                    postCategoryRefMapper.insert(postCategoryRef);
                }
            } else {
                //设置未分类
                Category category = categoryService.findByCateUrl("default");
                if (category != null) {
                    postCategoryRefMapper.insert(new PostCategoryRef(post.getPostId(), category.getCateId()));
                }
            }
            if (post.getTags() != null) {
                //删除标签和记录关联
                postTagRefMapper.deleteByPostId(post.getPostId());
                redisUtil.del("posts_tags::posts_tags_postid_" + post.getPostId());
                //添加标签和记录关联
                for (int i = 0; i < post.getTags().size(); i++) {
                    PostTagRef postTagRef = new PostTagRef(post.getPostId(), post.getTags().get(i).getTagId());
                    postTagRefMapper.insert(postTagRef);
                }
            }
        }
        //添加记录
        else {
            post.setPostViews(0L);
            post.setCommentSize(0L);
            post.setPostLikes(0L);
            postMapper.insert(post);
            //添加记录分类关系
            if (post.getCategories() != null) {
                for (int i = 0; i < post.getCategories().size(); i++) {
                    postCategoryRefMapper.insert(new PostCategoryRef(post.getPostId(), post.getCategories().get(i).getCateId()));
                }
            } else {
                //设置未分类
                Category category = categoryService.findByCateUrl("default");
                if (category != null) {
                    postCategoryRefMapper.insert(new PostCategoryRef(post.getPostId(), category.getCateId()));
                }
            }
            //添加记录标签关系
            if (post.getTags() != null) {
                for (int i = 0; i < post.getTags().size(); i++) {
                    postTagRefMapper.insert(new PostTagRef(post.getPostId(), post.getTags().get(i).getTagId()));
                }
            }
        }
        return post;
    }


    @Override
    @CacheEvict(value = {POSTS_CACHE_NAME, TAGS_CACHE_NAME, CATEGORIES_CACHE_NAME}, allEntries = true, beforeInvocation = true)
    @Transactional(rollbackFor = Exception.class)
    public Post removeByPostId(Long postId) {
        Post post = this.findByPostId(postId);
        if (post != null) {
            postTagRefMapper.deleteByPostId(postId);
            postCategoryRefMapper.deleteByPostId(postId);
            postMapper.deleteById(post.getPostId());
        }
        return post;
    }

    @Override
    @CacheEvict(value = POSTS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Post updatePostStatus(Long postId, Integer status) {
        Post post = this.findByPostId(postId);
        post.setPostStatus(status);
        postMapper.updateById(post);
        return post;
    }

    @Override
    public Long updatePostView(Long postId) {
        Long view = this.getPostViewsByPostId(postId);
        if (view == null) {
            return null;
        }
        //总访问量+1
        redisUtil.incr("posts_views::posts_views_sum", 1);
        //文章访问量+1
        return redisUtil.incr("posts_views::posts_views_id_" + postId, 1);
    }

    @Override
    public Long getPostViewsByPostId(Long postId) {
        String str = redisUtil.get("posts_views::posts_views_id_" + postId);
        if (str == null) {
            Post post = this.findByPostId(postId);
            if (post == null) {
                return null;
            }
            redisUtil.set("posts_views::posts_views_id_" + postId, String.valueOf(post.getPostViews()));
            return post.getPostViews();
        }
        return Long.parseLong(str);
    }

    @Override
    @CacheEvict(value = POSTS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void updateAllSummary(Integer postSummary) {
        List<Post> posts = this.findPostByStatus(PostStatusEnum.PUBLISHED.getCode(), PostTypeEnum.POST_TYPE_POST.getDesc());
        for (Post post : posts) {
            String text = HtmlUtil.cleanHtmlTag(post.getPostContent());
            if (text.length() > postSummary) {
                postMapper.updatePostSummary(post.getPostId(), text.substring(0, postSummary));
            } else {
                postMapper.updatePostSummary(post.getPostId(), text);
            }
        }
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_type_'+#postType")
    public List<Post> findAllPosts(String postType) {
        return postMapper.findPostsByPostType(postType);
    }

    @Override
    public Page<Post> searchPosts(String keyWord, Page<Post> page) {
        return page.setRecords(postMapper.pagingByPostTitleLike(keyWord, page));
    }


    @Override
    public Page<Post> findPostByStatus(Integer status, String postType, Page<Post> page) {
        List<Post> posts = postMapper.pagingPostsByPostStatusAndPostType(status, postType, page);
        return page.setRecords(posts);
    }


    @Override
    public Page<Post> findPostByUserIdAndStatus(Long userId, Integer status, String postType, Page<Post> page) {
        List<Post> posts = postMapper.pagingPostsByUserIdAndPostStatusAndPostType(userId, status, postType, page);
        return page.setRecords(posts);
    }

    @Override
    public Page<Post> findPostByStatusWithContent(Integer status, String postType, Page<Post> page) {
        return page.setRecords(postMapper.pagingPostsWithContentByPostStatusAndPostType(status, postType, page));
    }

    @Override
    public Page<Post> findPostByStatus(Page<Post> page) {
        //需要封装分类
        List<Post> postList = postMapper.pagingPostsByPostStatusAndPostType(PostStatusEnum.PUBLISHED.getCode(), PostTypeEnum.POST_TYPE_POST.getDesc(), page);
        return page.setRecords(postList);
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_status_type_'+#status+'_'+#postType")
    public List<Post> findPostByStatus(Integer status, String postType) {
        return postMapper.findPostsByPostStatusAndPostType(status, postType);
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_id_'+#postId", unless = "#result == null")
    public Post findByPostId(Long postId) {
        return postMapper.selectById(postId);
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_id_type_' + #postId + '_' + #postType", unless = "#result == null")
    public Post findByPostId(Long postId, String postType) {
        return postMapper.findPostByPostIdAndPostType(postId, postType);
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_url_'+#postUrl", unless = "#result == null")
    public Post findByPostUrl(String postUrl) {
        return postMapper.findPostByPostUrl(postUrl);
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_url_type_'+#postUrl+'_'+#postType", unless = "#result == null")
    public Post findByPostUrl(String postUrl, String postType) {
        return postMapper.findPostByPostUrlAndPostType(postUrl, postType);
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_latest'")
    public List<Post> findPostLatest() {
        return postMapper.findTopFive();
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_next_id_'+#postId+'_type_'+#postType", unless = "#result == null")
    public Post findNextPost(Long postId, String postType) {
        return postMapper.findByPostIdAfter(postId, postType);
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_precious_id_'+#postId+'_type_'+#postType", unless = "#result == null")
    public Post findPreciousPost(Long postId, String postType) {
        return postMapper.findByPostIdBefore(postId, postType);
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'post_year_month'")
    public List<Archive> findPostGroupByYearAndMonth() {
        List<Archive> archives = postMapper.findPostGroupByYearAndMonth();
        for (int i = 0; i < archives.size(); i++) {
            archives.get(i).setPosts(this.findPostByYearAndMonth(archives.get(i).getYear(), archives.get(i).getMonth()));
        }
        return archives;
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'post_year'")
    public List<Archive> findPostGroupByYear() {
        List<Archive> archives = postMapper.findPostGroupByYear();
        for (int i = 0; i < archives.size(); i++) {
            archives.get(i).setPosts(this.findPostByYear(archives.get(i).getYear()));
        }
        return archives;
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_year_month_'+#year+'_'+#month")
    public List<PostBriefDto> findPostByYearAndMonth(String year, String month) {
        return postMapper.findPostByYearAndMonth(year, month);
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_year_'+#year")
    public List<PostBriefDto> findPostByYear(String year) {
        return postMapper.findPostByYear(year);
    }

    @Override
    public Page<Post> findPostByYearAndMonth(String year, String month, Page<Post> page) {
        return page.setRecords(postMapper.pagingPostByYearAndMonth(year, month, null));
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_category_'+#category.cateId+'_'+#page.current", condition = "#page.current < 5")
    public Page<Post> findPostByCategories(Category category, Page<Post> page) {
        //子分类Id列表
        List<Long> ids = categoryService.selectChildCateId(category.getCateId());
        ids.add(category.getCateId());

        //查询记录和封装分类
        List<Post> postList = postMapper.pagingPostByCategoryIdsAndPostStatus(ids, PostStatusEnum.PUBLISHED.getCode(), page);
        for (int i = 0; i < postList.size(); i++) {
            List<Category> categories = categoryService.findByPostId(postList.get(i).getPostId());
            postList.get(i).setCategories(categories);
        }
        return page.setRecords(postList);
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_tag_'+#tag.tagId+'_'+#page.current", condition = "#page.current < 5")
    public Page<Post> findPostsByTags(Tag tag, Page<Post> page) {
        //查询记录和封装分类
        List<Post> postList = postMapper.pagingPostsByTagIdAndPostStatus(tag.getTagId(), PostStatusEnum.PUBLISHED.getCode(), page);
        for (int i = 0; i < postList.size(); i++) {
            List<Category> categories = categoryService.findByPostId(postList.get(i).getPostId());
            postList.get(i).setCategories(categories);
        }
        return page.setRecords(postList);
    }

    @Override
    public Response<Page<SimplePost>> searchByEs(String keyword, Page<SimplePost> page) {

        //search request
        SearchRequest searchRequest = new SearchRequest("blog");

        //search builder
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
        booleanQueryBuilder.must(QueryBuilders.matchQuery("postTitle", keyword));
        booleanQueryBuilder.must(QueryBuilders.matchQuery("postStatus", PostStatusEnum.PUBLISHED.getCode()));

        sourceBuilder.query(booleanQueryBuilder);
        sourceBuilder.from((page.getCurrent() - 1) * page.getSize());
        sourceBuilder.size(page.getSize());
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //sort
        sourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));

        //highlight
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field("postTitle");
        highlightTitle.preTags("<span class=\"highlight\">");
        highlightTitle.postTags("</span>");
        highlightBuilder.field(highlightTitle);
        sourceBuilder.highlighter(highlightBuilder);

        // add builder into request
        searchRequest.indices("blog");
        searchRequest.source(sourceBuilder);

        //response
        SearchResponse searchResponse = null;
        try {
            searchResponse = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return Response.no("查询失败");
        }
        TimeValue took = searchResponse.getTook();

        //search hits
        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits();

        SearchHit[] searchHits = hits.getHits();
        List<SimplePost> postList = new ArrayList<>();
        page.setTotal((int) totalHits);
        for (SearchHit hit : searchHits) {
            String str = hit.getSourceAsString();
            SimplePost esPost = JSONObject.parseObject(str, SimplePost.class);

            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField highlight = highlightFields.get("postTitle");
            if (highlight != null) {
                Text[] fragments = highlight.fragments();
                String fragmentString = fragments[0].string();
                esPost.setPostTitle(fragmentString);
            }
            postList.add(esPost);
        }
        return Response.yes(took.toString(), page.setRecords(postList));
    }

    @Override
    public Response<Page<SimplePost>> findPostsByEs(HashMap<String, Object> criteria,
                                                    String order,
                                                    Page<SimplePost> page) {

        //处理 order
        if (Objects.equals(order, "postId")) {
            order = "postId";
        } else if (Objects.equals(order, "commentSize")) {
            order = "commentSize";
        } else if (Objects.equals(order, "postLikes")) {
            order = "postLikes";
        } else if (Objects.equals(order, "postViews")) {
            order = "postViews";
        } else {
            order = "postDate";
        }

        //search request
        SearchRequest searchRequest = new SearchRequest("blog");

        //search builder
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
        for (String key : criteria.keySet()) {
            booleanQueryBuilder.must(QueryBuilders.matchQuery(key, criteria.get(key)));
        }


        sourceBuilder.query(booleanQueryBuilder);
        sourceBuilder.from((page.getCurrent() - 1) * page.getSize());
        sourceBuilder.size(page.getSize());
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //sort
        if (order == null || "".equals(order)) {
            sourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
        } else {
            sourceBuilder.sort(new FieldSortBuilder(order).order(SortOrder.DESC));
        }

        // add builder into request
        searchRequest.indices("blog");
        searchRequest.source(sourceBuilder);

        //response
        SearchResponse searchResponse = null;
        try {
            searchResponse = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.no("查询失败");
        }
        TimeValue took = searchResponse.getTook();

        //search hits
        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits();

        SearchHit[] searchHits = hits.getHits();
        List<SimplePost> postList = new ArrayList<>();
        page.setTotal((int) totalHits);
        for (SearchHit hit : searchHits) {
            String str = hit.getSourceAsString();
            SimplePost esPost = JSONObject.parseObject(str, SimplePost.class);
            postList.add(esPost);
        }
        return Response.yes(took.toString(), page.setRecords(postList));
    }


    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_hot'")
    public List<Post> hotPosts() {
        return postMapper.findPostsByPostTypeOrderByPostViewsDesc(PostTypeEnum.POST_TYPE_POST.getDesc());
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_same_tags_postid_'+#post.getPostId()")
    public List<Post> listSameTagPosts(Post post) {
        //获取当前记录的所有标签
        List<Tag> tags = post.getTags();
        List<Post> tempPosts = new ArrayList<>();
        for (Tag tag : tags) {
            tempPosts.addAll(postMapper.findPostsByTagId(tag.getTagId()));
        }
        //去掉当前的记录
        tempPosts.remove(post);
        //去掉重复的记录
        List<Post> allPosts = new ArrayList<>();
        for (int i = 0; i < tempPosts.size(); i++) {
            if (!allPosts.contains(tempPosts.get(i))) {
                allPosts.add(tempPosts.get(i));
            }
        }
        //按照访问量排序
        allPosts = allPosts.stream().sorted(Comparator.comparing(Post::getPostViews).reversed()).collect(Collectors.toList());
        return allPosts;
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_same_categories_postid_'+#post.getPostId()")
    public List<Post> listSameCategoryPosts(Post post) {
        //获取当前记录的所有标签
        List<Category> categories = post.getCategories();
        List<Post> tempPosts = new ArrayList<>();
        for (Category category : categories) {
            tempPosts.addAll(postMapper.findPostsByCategoryId(category.getCateId()));
        }
        //去掉当前的记录
        tempPosts.remove(post);
        //去掉重复的记录
        List<Post> allPosts = new ArrayList<>();
        for (int i = 0; i < tempPosts.size(); i++) {
            if (!allPosts.contains(tempPosts.get(i))) {
                allPosts.add(tempPosts.get(i));
            }
        }
        //按照访问量排序
        allPosts = allPosts.stream().sorted(Comparator.comparing(Post::getPostViews).reversed()).collect(Collectors.toList());
        return allPosts;
    }


    @Override
    public Long getSumPostViews() {
        String str = redisUtil.get("posts_views::posts_views_sum");
        if (Objects.equals(str, "null") || str == null) {
            Long count = postMapper.getPostViewsSum();
            redisUtil.set("posts_views::posts_views_sum", String.valueOf(count));
            return count;
        }
        return Long.valueOf(str);
    }


    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_count_type_'+#postType+'_status_'+#status")
    public Integer countByPostTypeAndStatus(String postType, Integer status) {
        return postMapper.countByPostStatusAndPostType(status, postType);

    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_type_'+#postType+'_status_'+#status")
    public List<Post> findByPostTypeAndStatus(String postType, Integer status) {
        return postMapper.findPostsByPostStatusAndPostType(status, postType);
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_count_uid_'+#userId+'_status_'+#status")
    public Integer countArticleByUserIdAndStatus(Long userId, Integer status) {
        return postMapper.countByUserIdAndPostStatusAndPostType(userId, status, PostTypeEnum.POST_TYPE_POST.getDesc());
    }

    @Override
    public String buildRss(List<Post> posts) {
        String rss = "";
        try {
            rss = SensUtils.getRss(posts);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rss;
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_sitemap'")
    public String buildArticleSiteMap() {
        List<Post> posts = this.findPostByStatus(0, PostTypeEnum.POST_TYPE_POST.getDesc());
        return SensUtils.getSiteMap(posts);
    }

    @Override
    public String syncAllPostView() {
        log.info("======================开始 同步记录访问量======================");
        Long startTime = System.nanoTime();
        List<PostViewsDto> dtoList = new ArrayList<>();
        //从redis取值封装List
        Integer prefixLength = "posts_views::posts_views_id_".length();
        Set<String> keySet = redisUtil.keys("posts_views::posts_views_id_*");
        for (String key : keySet) {
            dtoList.add(new PostViewsDto(Long.parseLong(key.substring(prefixLength)), Long.parseLong(redisUtil.get(key))));
        }
        //更新到数据库中
        postMapper.batchUpdatePostViews(dtoList);
        Long endTime = System.nanoTime();
        String total = (endTime - startTime) / 1000000 + "ms";
        log.info("本次记录访问量同步成功, 总耗时: {}", total);
        log.info("======================结束 记录访问量结束======================");
        return total;
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'last_update_time'")
    public Date getLastUpdateTime() {
        Date date = postMapper.selectMaxPostUpdate();
        if (date == null) {
            return new Date();
        }
        return date;
    }

    @Override
    public void resetAllPostCommentSize() {
        System.out.println(Thread.currentThread().getName() + "开始");
        Long start = System.currentTimeMillis();
        List<Long> ids = postMapper.selectAllPostIds();
        ids.forEach(id -> {
            postMapper.updateCommentSize(id);
        });
        Long end = System.currentTimeMillis();
        System.out.println("重置成功，耗时：" + (end - start) + "ms");
        System.out.println(Thread.currentThread().getName() + "结束");
        log.info("重置成功，耗时：" + (end - start) + "ms");
    }

    @Override
    @CacheEvict(value = POSTS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void updateCommentSize(Long postId) {
        postMapper.updateCommentSize(postId);
    }

    @Override
    @CacheEvict(value = POSTS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void updatePostLikes(Long postId) {
        postMapper.updatePostLikes(postId);
    }

    @Override
    @CacheEvict(value = POSTS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void removeByUserId(Long userId) {
        postMapper.deleteByUserId(userId);
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_uid_'+#userId+'_status_'+#status+'_page_'+#page.current", condition = "#page.current < 5")
    public Page<Post> findByUserIdAndStatus(Long userId, Integer status, Page<Post> page) {
        List<Post> postList = postMapper.findByUserIdAndStatus(userId, status, page);
        for (int i = 0; i < postList.size(); i++) {
            List<Category> categories = categoryService.findByPostId(postList.get(i).getPostId());
            postList.get(i).setCategories(categories);
        }
        return page.setRecords(postList);
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_count_uid_'+#userId")
    public Integer countByUserId(Long userId) {
        return postMapper.countByUserId(userId);
    }

    @Override
    @Cacheable(value = POSTS_CACHE_NAME, key = "'posts_ids_uid'+#userId")
    public List<Long> selectIdsByUserId(Long userId) {
        return postMapper.selectIdsByUserId(userId);
    }


}
