package com.liuyanzhao.sens.web.controller.front;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.PageUtil;
import com.baomidou.mybatisplus.plugins.Page;
import com.liuyanzhao.sens.entity.*;
import com.liuyanzhao.sens.model.dto.JsonResult;
import com.liuyanzhao.sens.model.dto.ListPage;
import com.liuyanzhao.sens.model.dto.SensConst;
import com.liuyanzhao.sens.model.enums.*;
import com.liuyanzhao.sens.service.*;
import com.liuyanzhao.sens.utils.CommentUtil;
import com.liuyanzhao.sens.web.controller.core.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <pre>
 *     前台公告归档控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2018/4/26
 */
@Slf4j
@Controller
@RequestMapping("/notice")
public class FrontNoticeController extends BaseController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    /**
     * 渲染公告详情
     *
     * @param postId 公告Id
     * @param model   model
     * @return 模板路径/themes/{theme}/notice
     */
    @GetMapping(value = {"{postId}"})
    public String getNotice(@PathVariable Long postId,
                            @RequestParam(value = "page", defaultValue = "1") Integer page,
                            Model model) {
        //1、查询公告
        Post post = postService.findByPostId(postId);


        if (null == post || !post.getPostStatus().equals(PostStatusEnum.PUBLISHED.getCode())) {
            return this.renderNotFound();
        }

        //2、上一篇下一篇
        Post beforePost = postService.findPreciousPost(post.getPostId(), PostTypeEnum.POST_TYPE_NOTICE.getDesc());
        Post afterPost = postService.findNextPost(post.getPostId(), PostTypeEnum.POST_TYPE_NOTICE.getDesc());
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

        //5.公告访问量
        post.setPostViews(postService.updatePostView(post.getPostId()));

        //6、作者
        User user = userService.findByUserId(post.getUserId());
        post.setUser(user);

        //7、是否是作者
        Subject subject = SecurityUtils.getSubject();
        User principal = (User) subject.getPrincipal();
        if (principal != null && Objects.equals(principal.getUserId(), post.getUserId())) {
            model.addAttribute("isAuthor", Boolean.TRUE);
        }

        model.addAttribute("is_page", true);
        model.addAttribute("post", post);
        model.addAttribute("comments", commentsPage);
        model.addAttribute("commentsCount", comments.size());
        model.addAttribute("rainbow", rainbow);
        return this.render("notice");
    }


}
