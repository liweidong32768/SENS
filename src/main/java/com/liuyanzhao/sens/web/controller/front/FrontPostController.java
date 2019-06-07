package com.liuyanzhao.sens.web.controller.front;

import com.baomidou.mybatisplus.plugins.Page;
import com.liuyanzhao.sens.entity.*;
import com.liuyanzhao.sens.model.dto.JsonResult;
import com.liuyanzhao.sens.model.dto.SensConst;
import com.liuyanzhao.sens.model.dto.ListPage;
import com.liuyanzhao.sens.model.enums.*;
import com.liuyanzhao.sens.service.*;
import com.liuyanzhao.sens.utils.CommentUtil;
import com.liuyanzhao.sens.web.controller.core.BaseController;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * <pre>
 *     前台文章归档控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2018/4/26
 */
@Slf4j
@Controller
@RequestMapping("/article")
public class FrontPostController extends BaseController {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private TagService tagService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 文章归档
     *
     * @param model model
     * @return 模板路径
     */
    @GetMapping
    public String article(Model model) {
        return this.render("article");
    }


    /**
     * 文章归档，根据年月
     *
     * @param model model
     * @param year  year 年份
     * @param month month 月份
     * @return 模板路径/themes/{theme}/article
     */
    @GetMapping(value = "{year}/{month}")
    public String article(Model model,
                          @PathVariable(value = "year") String year,
                          @PathVariable(value = "month") String month) {
        Page<Post> posts = postService.findPostByYearAndMonth(year, month, null);
        if (null == posts) {
            return this.renderNotFound();
        }
        model.addAttribute("is_article", true);
        model.addAttribute("posts", posts);
        return this.render("article");
    }

    /**
     * 渲染文章详情
     *
     * @param postUrl 文章路径名
     * @param model   model
     * @return 模板路径/themes/{theme}/post
     */
    @GetMapping(value = {"{postUrl}.html", "{postUrl}"})
    public String getPost(@PathVariable String postUrl,
                          @RequestParam(value = "page", defaultValue = "1") Integer page,
                          Model model) {
        Subject subject = SecurityUtils.getSubject();
        //1、查询文章
        Boolean isNum = StringUtils.isNumeric(postUrl);
        Post post;
        if (isNum) {
            post = postService.findByPostId(Long.valueOf(postUrl), PostTypeEnum.POST_TYPE_POST.getDesc());
            if (post == null) {
                post = postService.findByPostUrl(postUrl, PostTypeEnum.POST_TYPE_POST.getDesc());
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


        //标签
        List<Tag> tags = tagService.findByPostId(post.getPostId());
        post.setTags(tags);
        //分类
        List<Category> categories = categoryService.findByPostId(post.getPostId());
        post.setCategories(categories);

        //2、上一篇下一篇
        Post beforePost = postService.findPreciousPost(post.getPostId(), PostTypeEnum.POST_TYPE_POST.getDesc());
        Post afterPost = postService.findNextPost(post.getPostId(), PostTypeEnum.POST_TYPE_POST.getDesc());
        model.addAttribute("beforePost", beforePost);
        model.addAttribute("afterPost", afterPost);

        //3、评论列表
        List<Comment> comments = null;
        if (StringUtils.equals(SensConst.OPTIONS.get(BlogPropertiesEnum.NEW_COMMENT_NEED_CHECK.getProp()), TrueFalseEnum.TRUE.getDesc()) || SensConst.OPTIONS.get(BlogPropertiesEnum.NEW_COMMENT_NEED_CHECK.getProp()) == null) {
            comments = commentService.findCommentsByPostAndCommentStatus(post.getPostId(), CommentStatusEnum.PUBLISHED.getCode());
        } else {
            comments = commentService.findCommentsByPostAndCommentStatusNot(post.getPostId(), CommentStatusEnum.RECYCLE.getCode());
        }
        //默认显示10条
        Integer size = 10;
        //获取每页评论条数
        if (!StringUtils.isBlank(SensConst.OPTIONS.get(BlogPropertiesEnum.INDEX_COMMENTS.getProp()))) {
            size = Integer.parseInt(SensConst.OPTIONS.get(BlogPropertiesEnum.INDEX_COMMENTS.getProp()));
        }
        //评论分页
        ListPage<Comment> commentsPage = new ListPage<Comment>(CommentUtil.getComments(comments), page, size);
        int[] rainbow = PageUtil.rainbow(page, commentsPage.getTotalPage(), 10);

        //4、获取文章的标签用作keywords
        List<String> tagWords = new ArrayList<>();
        if (tags != null) {
            for (Tag tag : tags) {
                tagWords.add(tag.getTagName());
            }
        }

        //5.文章访问量
        post.setPostViews(postService.updatePostView(post.getPostId()));

        //6.相同标签的文章
        List<Post> sameTagPosts = postService.listSameTagPosts(post);
        if (sameTagPosts.size() > 4) {
            sameTagPosts = sameTagPosts.subList(0, 4);
        } else {
            sameTagPosts = sameTagPosts.subList(0, sameTagPosts.size());
        }

        //7.相同分类的文章
        List<Post> sameCategoryPosts = postService.listSameCategoryPosts(post);
        if (sameCategoryPosts.size() > 10) {
            sameCategoryPosts = sameCategoryPosts.subList(0, 10);
        } else {
            sameCategoryPosts = sameCategoryPosts.subList(0, sameCategoryPosts.size());
        }

        //7.作者
        User user = userService.findByUserId(post.getUserId());
        post.setUser(user);

        //8.是否是作者
        User principal = (User) subject.getPrincipal();
        if (principal != null && Objects.equals(principal.getUserId(), post.getUserId())) {
            model.addAttribute("isAuthor", Boolean.TRUE);
        }

        model.addAttribute("sameTagPosts", sameTagPosts);
        model.addAttribute("sameCategoryPosts", sameCategoryPosts);
        model.addAttribute("is_post", true);
        model.addAttribute("post", post);
        model.addAttribute("comments", commentsPage);
        model.addAttribute("rainbow", rainbow);
        model.addAttribute("tagWords", CollUtil.join(tagWords, ","));
        return this.render("post");
    }

    @PostMapping("/like")
    @ResponseBody
    public JsonResult like(@RequestParam("postId") Long postId) {
        try {
            Post post = postService.findByPostId(postId);
            if (post == null) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), "文章不存在");
            }
            postService.updatePostLikes(postId);
        } catch (Exception e) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), "操作失败");
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), "操作成功");
    }


}
