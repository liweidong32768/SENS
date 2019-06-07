package com.liuyanzhao.sens.web.controller.front;

import com.liuyanzhao.sens.entity.*;
import com.liuyanzhao.sens.model.dto.Archive;
import com.liuyanzhao.sens.model.dto.SensConst;
import com.liuyanzhao.sens.model.dto.ListPage;
import com.liuyanzhao.sens.model.enums.BlogPropertiesEnum;
import com.liuyanzhao.sens.model.enums.CommentStatusEnum;
import com.liuyanzhao.sens.model.enums.PostTypeEnum;
import com.liuyanzhao.sens.model.enums.TrueFalseEnum;
import com.liuyanzhao.sens.service.*;
import com.liuyanzhao.sens.utils.CommentUtil;
import com.liuyanzhao.sens.web.controller.core.BaseController;
import cn.hutool.core.util.PageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * <pre>
 *     前台内置页面，自定义页面控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2018/4/26
 */
@Controller
public class FrontPageController extends BaseController {

    @Autowired
    private GalleryService galleryService;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private TagService tagService;

    /**
     * 渲染自定义页面
     *
     * @param postUrl 页面路径
     * @param model   model
     * @return 模板路径/themes/{theme}/post
     */
    @GetMapping(value = "/p/{postUrl}")
    public String getPage(@PathVariable(value = "postUrl") String postUrl,
                          @RequestParam(value = "page", defaultValue = "1") Integer page,
                          Model model) {
        Post post = postService.findByPostUrl(postUrl, PostTypeEnum.POST_TYPE_PAGE.getDesc());
        if (null == post) {
            return this.renderNotFound();
        }
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
        model.addAttribute("is_page", true);
        model.addAttribute("post", post);
        model.addAttribute("comments", commentsPage);
        model.addAttribute("commentsCount", comments.size());
        model.addAttribute("rainbow", rainbow);
        //5.文章访问量
        post.setPostViews(postService.updatePostView(post.getPostId()));
        return this.render("page");
    }

    /**
     * 跳转到图库页面
     *
     * @return 模板路径/themes/{theme}/gallery
     */
    @GetMapping(value = "/gallery")
    public String gallery(Model model) {
        List<Gallery> galleries = galleryService.findAllGalleries();
        model.addAttribute("galleries", galleries);
        return this.render("gallery");
    }

    /**
     * 跳转到代码高亮页面
     *
     * @return 模板路径/themes/{theme}/highlight
     */
    @GetMapping(value = "/highlight")
    public String highlight() {
        return this.render("highlight");
    }


    //文章归档页面
    @GetMapping("/archive")
    public String archive(Model model) {
        List<Archive> archives = postService.findPostGroupByYearAndMonth();
        model.addAttribute("archives", archives);
        model.addAttribute("is_page", true);
        return this.render("archive");
    }

    //留言版页面
    @GetMapping("/message")
    public String message(Model model) {
        model.addAttribute("is_page", true);
        return this.render("message");
    }

    //站点地图
    @GetMapping("/sitemap")
    public String siteMap(Model model) {
        model.addAttribute("is_page", true);
        return this.render("sitemap");
    }

    //标签库
    @GetMapping("/tags")
    public String tags(Model model) {
        model.addAttribute("is_page", true);
        List<Tag> tags = tagService.findAllTags();
        model.addAttribute("tags", tags);
        return this.render("tags");
    }

    //最新评论
    @GetMapping("/recent-comments")
    public String recentComments(Model model) {
        model.addAttribute("is_page", true);
        List<Comment> comments = commentService.findCommentsTop50();
        model.addAttribute("comments", comments);
        return this.render("recent-comments");
    }

}
