package com.liuyanzhao.sens.web.controller.front;

import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.plugins.Page;
import com.liuyanzhao.sens.entity.*;
import com.liuyanzhao.sens.model.dto.SensConst;
import com.liuyanzhao.sens.model.enums.*;
import com.liuyanzhao.sens.model.dto.SimplePost;
import com.liuyanzhao.sens.service.*;
import com.liuyanzhao.sens.utils.Response;
import com.liuyanzhao.sens.web.controller.core.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * <pre>
 *     前台首页控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2018/4/26
 */
@Slf4j
@Controller
@RequestMapping(value = {"/", "index"})
public class FrontIndexController extends BaseController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CommentService commentService;

    @GetMapping("/demo")
    public String demo() {
        return "common/demo";
    }

    /**
     * 文章/页面 入口
     * 兼容老版本
     *
     * @param postUrl 文章路径名
     * @return 模板路径/themes/{theme}/post
     */
    @GetMapping(value = {"{postUrl}.html", "{postUrl}", "post/{postUrl}", "post/{postUrl}.html"})
    public String getPost(@PathVariable String postUrl) {
        Subject subject = SecurityUtils.getSubject();

        Boolean isNum = StringUtils.isNumeric(postUrl);
        Post post;
        if (isNum) {
            post = postService.findByPostId(Long.valueOf(postUrl));
            if (post == null) {
                post = postService.findByPostUrl(postUrl);
            }
        } else {
            post = postService.findByPostUrl(postUrl, PostTypeEnum.POST_TYPE_POST.getDesc());
        }

        //文章不存在404，文章存在但是未发布只有作者可以看
        if (null == post) {
            return this.renderNotFound();
        }
        if (!post.getPostStatus().equals(PostStatusEnum.PUBLISHED.getCode())) {
            if (!subject.isAuthenticated()) {
                return this.renderNotFound();
            } else {
                User user = (User) subject.getPrincipal();
                if (!subject.hasRole(RoleEnum.ADMIN.getDesc()) && !user.getUserId().equals(post.getUserId())) {
                    return this.renderNotFound();
                }
            }
        }

        //如果是页面
        if (Objects.equals(post.getPostType(), PostTypeEnum.POST_TYPE_PAGE.getDesc())) {
            return "redirect:/p/" + post.getPostUrl();
        }
        return "redirect:/article/" + post.getPostId();
    }


    /**
     * 请求首页
     *
     * @param model model
     * @return 模板路径
     */
    @GetMapping
    public String index(Model model,
                        @RequestParam(value = "order", defaultValue = "post_id") String order) {
        //调用方法渲染首页
        return this.index(model, 1, order);
    }

    /**
     * 首页分页
     *
     * @param model model
     * @param page  当前页码
     * @return 模板路径/themes/{theme}/index
     */
    @GetMapping(value = "page/{page}")
    public String index(Model model,
                        @PathVariable(value = "page") Integer page,
                        @RequestParam(value = "order", defaultValue = "postDate") String order) {
        //默认显示15条
        Integer size = 15;
        //尝试加载设置选项，用于设置显示条数
        if (!StringUtils.isBlank(SensConst.OPTIONS.get(BlogPropertiesEnum.INDEX_POSTS.getProp()))) {
            size = Integer.parseInt(SensConst.OPTIONS.get(BlogPropertiesEnum.INDEX_POSTS.getProp()));
        }
        //文章分页排序
        Page pageable = new Page(page, size);
        HashMap<String, Object> map = new HashMap<>(2);
        map.put("postType", "post");
        map.put("postStatus", PostStatusEnum.PUBLISHED.getCode());

        Response<Page<SimplePost>> response = postService.findPostsByEs(map, order, pageable);
        if (response.isSuccess()) {
            Page<SimplePost> posts = response.getData();
            if (null == posts) {
                return this.renderNotFound();
            }
            List<SimplePost> postList = posts.getRecords();
            postList.forEach(post -> {
                        post.setCategories(categoryService.findByPostId(post.getPostId()));
                        post.setPostViews(postService.getPostViewsByPostId(post.getPostId()));
                    }
            );
            posts.setRecords(postList);
            model.addAttribute("posts", posts);
        } else {
            Page<Post> posts = postService.findPostByStatus(pageable);
            List<Post> postList = posts.getRecords();
            postList.forEach(post -> {
                        post.setCategories(categoryService.findByPostId(post.getPostId()));
                        post.setPostViews(postService.getPostViewsByPostId(post.getPostId()));
                    }
            );
            posts.setRecords(postList);
            model.addAttribute("posts", posts);
        }

        model.addAttribute("is_index", true);
        model.addAttribute("prefix", "");
        //如果不是默认的，加上后缀
        if (!"postDate".equals(order)) {
            model.addAttribute("suffix", "?order=" + order);
        }

        //首页的公告
        List<Post> notices = postService.findByPostTypeAndStatus(PostTypeEnum.POST_TYPE_NOTICE.getDesc(), PostStatusEnum.PUBLISHED.getCode());
        model.addAttribute("notices", notices);
        return this.render("index");
    }


    /**
     * ajax分页
     *
     * @param page page 当前页码
     * @return List
     */
    @GetMapping(value = "next")
    @ResponseBody
    @Deprecated
    public List<Post> ajaxIndex(@RequestParam(value = "page") Integer page) {
        //默认显示15条
        Integer size = 15;
        //尝试加载设置选项，用于设置显示条数
        if (!StringUtils.isBlank(SensConst.OPTIONS.get(BlogPropertiesEnum.INDEX_POSTS.getProp()))) {
            size = Integer.parseInt(SensConst.OPTIONS.get(BlogPropertiesEnum.INDEX_POSTS.getProp()));
        }

        //文章数据，只获取文章，没有分页
        //TODO
        Page pageable = new Page(page, size);
        Page<Post> postPage = postService.findPostByStatus(PostStatusEnum.PUBLISHED.getCode(), PostTypeEnum.POST_TYPE_POST.getDesc(), pageable);
        List<Post> postList = postPage.getRecords();
        postList.forEach(post -> post.setCategories(categoryService.findByPostId(post.getPostId())));
        return postList;
    }

    /**
     * 搜索文章
     *
     * @param keyword keyword
     * @param model   model
     * @return 模板路径/themes/{theme}/index
     */
    @GetMapping(value = "search")
    public String search(@RequestParam("keyword") String keyword,
                         Model model) {
        return this.searchPage(model, 1, HtmlUtil.escape(keyword));
    }

    /**
     * 搜索
     *
     * @param model model
     * @param page  当前页码
     * @return 模板路径/themes/{theme}/index
     */
    @GetMapping(value = "/search/page/{page}")
    public String searchPage(Model model,
                             @PathVariable(value = "page") Integer page,
                             @RequestParam("keyword") String keyword) {
        //默认显示10条
        Integer size = 10;
        page = page > 0 ? page : 1;
        Page posts = new Page(page, size);
        Response<Page<SimplePost>> response = postService.searchByEs(HtmlUtil.escape(keyword), posts);
        if (response.isSuccess()) {
            model.addAttribute("posts", response.getData());
            model.addAttribute("time", response.getMessage());
        } else {
            model.addAttribute("time", 0);
            model.addAttribute("posts", posts.setRecords(Collections.emptyList()));
        }
        model.addAttribute("is_index", true);
        model.addAttribute("prefix", "/search");
        model.addAttribute("suffix", "?keyword=" + keyword);
        return this.render("search");
    }

    /**
     * 搜索文章
     *
     * @param userName userName
     * @param model    model
     * @return 模板路径/themes/{theme}/index
     */
    @GetMapping(value = "/author/{userName}")
    public String postsByUserName(@PathVariable(value = "userName") String userName,
                                  Model model) {

        return this.postsByUserName(model, HtmlUtil.escape(userName), 1);
    }

    /**
     * 首页分页
     *
     * @param model model
     * @param page  当前页码
     * @return 模板路径/themes/{theme}/index
     */
    @GetMapping(value = "/author/{userName}/page/{page}")
    public String postsByUserName(Model model,
                                  @PathVariable(value = "userName") String userName,
                                  @PathVariable(value = "page") Integer page) {

        User user = userService.findByUserName(userName);
        if (user == null) {
            return this.renderNotFound();
        }
        //默认显示15条
        Integer size = 15;
        //所有数据，分页
        Page pageable = new Page(page, size);
        HashMap<String, Object> map = new HashMap<>();
        map.put("postType", "post");
        map.put("postStatus", PostStatusEnum.PUBLISHED.getCode());
        map.put("userId", user.getUserId());

        Response<Page<SimplePost>> response = postService.findPostsByEs(map, "postDate", pageable);
        if (response.isSuccess()) {
            Page<SimplePost> posts = response.getData();
            if (null == posts) {
                return this.renderNotFound();
            }
            List<SimplePost> postList = posts.getRecords();
            postList.forEach(post -> {
                        post.setCategories(categoryService.findByPostId(post.getPostId()));
                        post.setPostViews(postService.getPostViewsByPostId(post.getPostId()));
                    }
            );
            posts.setRecords(postList);
            model.addAttribute("posts", posts);
        }

        //该用户的文章数
        Integer postCount = postService.countByUserId(user.getUserId());
        user.setPostCount(postCount);
        //该用户的评论数
        Integer commentCount = commentService.countByUserId(user.getUserId());
        user.setCommentCount(commentCount);

        model.addAttribute("is_author", true);
        model.addAttribute("author", user);
        model.addAttribute("prefix", "/author/" + userName);
        return this.render("author");
    }


}
