package com.liuyanzhao.sens.web.controller.admin;

import com.baomidou.mybatisplus.plugins.Page;
import com.liuyanzhao.sens.entity.Comment;
import com.liuyanzhao.sens.entity.Post;
import com.liuyanzhao.sens.entity.User;
import com.liuyanzhao.sens.model.dto.JsonResult;
import com.liuyanzhao.sens.model.dto.SensConst;
import com.liuyanzhao.sens.model.enums.*;
import com.liuyanzhao.sens.service.CommentService;
import com.liuyanzhao.sens.service.MailService;
import com.liuyanzhao.sens.service.PostService;
import com.liuyanzhao.sens.service.UserService;
import com.liuyanzhao.sens.utils.LocaleMessageUtil;
import com.liuyanzhao.sens.utils.OwoUtil;
import com.liuyanzhao.sens.web.controller.core.BaseController;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.HtmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <pre>
 *     后台评论管理控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2017/12/10
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/comment")
public class CommentController extends BaseController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private MailService mailService;

    @Autowired
    private PostService postService;

    @Autowired
    private LocaleMessageUtil localeMessageUtil;


    /**
     * 渲染评论管理页面
     *
     * @param model  model
     * @param status status 评论状态
     * @param page   page 当前页码
     * @param size   size 每页显示条数
     * @return 模板路径admin/admin_comment
     */
    @GetMapping
    @RequiresPermissions("comment:list")
    public String comments(Model model,
                           @RequestParam(value = "status", defaultValue = "0") Integer status,
                           @RequestParam(value = "page", defaultValue = "0") Integer page,
                           @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page pageable = new Page(page, size);
        Page<Comment> comments;
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        Boolean isAdmin = subject.hasRole(RoleEnum.ADMIN.getDesc());
        if (isAdmin) {
            comments = commentService.pagingByStatus(status, pageable);
        } else {
            comments = commentService.pagingByAcceptUserAndStatus(user.getUserId(), status, pageable);
        }

        List<Comment> commentList = comments.getRecords();
        commentList.forEach(comment -> comment.setPost(postService.findByPostId(comment.getPostId())));
        comments.setRecords(commentList);
        model.addAttribute("comments", comments);
        if (isAdmin) {
            model.addAttribute("publicCount", commentService.countByStatus(CommentStatusEnum.PUBLISHED.getCode()));
            model.addAttribute("checkCount", commentService.countByStatus(CommentStatusEnum.CHECKING.getCode()));
            model.addAttribute("trashCount", commentService.countByStatus(CommentStatusEnum.RECYCLE.getCode()));
        } else {
            model.addAttribute("publicCount", commentService.countByReceiveUserAndStatus(user.getUserId(), CommentStatusEnum.PUBLISHED.getCode()));
            model.addAttribute("checkCount", commentService.countByReceiveUserAndStatus(user.getUserId(), CommentStatusEnum.CHECKING.getCode()));
            model.addAttribute("trashCount", commentService.countByReceiveUserAndStatus(user.getUserId(), CommentStatusEnum.RECYCLE.getCode()));
        }
        model.addAttribute("status", status);
        return "admin/admin_comment";
    }

    /**
     * 将评论改变为发布状态
     * 评论状态有两种：待审核1，回收站2
     * 对待审核转发布的，发邮件
     *
     * @param commentId 评论编号
     * @return 重定向到/admin/comment
     */
    @PostMapping(value = "/revert")
    @ResponseBody
    @RequiresPermissions("comment:revert")
    public JsonResult moveToPublish(@RequestParam("commentId") Long commentId) {
        try {
            Subject subject = SecurityUtils.getSubject();
            User user = (User) subject.getPrincipal();
            //评论
            Comment comment = commentService.findCommentById(commentId);
            Integer status = comment.getCommentStatus();
            if (comment == null) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.post-not-exist"));
            }
            //文章
            Post post = postService.findByPostId(comment.getPostId());
            if (post == null) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.post-not-exist"));
            }
            //检查权限，文章的作者有权删除
            if (subject.hasRole(RoleEnum.ADMIN.getDesc()) || Objects.equals(post.getUserId(), user.getUserId())) {
                Comment result = commentService.updateCommentStatus(commentId, CommentStatusEnum.PUBLISHED.getCode());
                //判断是否启用邮件服务
                new NoticeToAuthor(result, post, user, status).start();
            } else {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.permission-denied"));
            }
        } catch (Exception e) {
            log.error("将评论改变为发布状态失败：{}", e);
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.operation-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.operation-success"));
    }

    /**
     * 删除评论
     *
     * @param commentId commentId
     * @return string 重定向到/admin/comment
     */
    @PostMapping(value = "/remove")
    @ResponseBody
    @RequiresPermissions("comment:remove")
    public JsonResult moveToAway(@RequestParam("commentId") Long commentId) {
        try {
            Subject subject = SecurityUtils.getSubject();
            User user = (User) subject.getPrincipal();
            //评论
            Comment comment = commentService.findCommentById(commentId);
            if (comment == null) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.comment-not-exist"));
            }
            //文章
            Post post = postService.findByPostId(comment.getPostId());
            if (post == null) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.post-not-exist"));
            }
            //检查权限，文章的作者和评论的作者有权删除
            if (subject.hasRole(RoleEnum.ADMIN.getDesc()) || Objects.equals(post.getUserId(), user.getUserId()) || Objects.equals(comment.getUserId(), user.getUserId())) {
                if (Objects.equals(comment.getCommentStatus(), CommentStatusEnum.RECYCLE.getCode())) {
                    commentService.removeByCommentId(commentId);
                } else {
                    commentService.updateCommentStatus(commentId, CommentStatusEnum.RECYCLE.getCode());
                }
            } else {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.permission-denied"));
            }
        } catch (Exception e) {
            log.error("删除评论失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-success"));
    }


    /**
     * 管理员回复评论，并通过评论
     *
     * @param commentId      被回复的评论
     * @param commentContent 回复的内容
     * @return 重定向到/admin/comment
     */
    @PostMapping(value = "/reply")
    @RequiresPermissions("comment:reply")
    @ResponseBody
    public JsonResult replyComment(@RequestParam("commentId") Long commentId,
                                   @RequestParam("commentContent") String commentContent,
                                   @RequestParam("userAgent") String userAgent,
                                   HttpServletRequest request) {
        try {
            //博主信息
            Subject subject = SecurityUtils.getSubject();
            User user = (User) subject.getPrincipal();
            //被回复的评论
            Comment lastComment = commentService.findCommentById(commentId);
            if (lastComment == null) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.comment-not-exist"));
            }

            Post post = postService.findByPostId(lastComment.getPostId());
            if (post == null) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.post-not-exist"));
            }
            //修改被回复的评论的状态
            if (Objects.equals(lastComment.getCommentStatus(), CommentStatusEnum.CHECKING.getCode())) {
                lastComment.setCommentStatus(CommentStatusEnum.PUBLISHED.getCode());
                commentService.saveByComment(lastComment);
            }

            //保存评论
            Comment comment = new Comment();
            comment.setUserId(user.getUserId());
            comment.setPostId(lastComment.getPostId());
            comment.setCommentAuthor(user.getUserDisplayName());
            comment.setCommentAuthorEmail(user.getUserEmail());
            comment.setCommentAuthorUrl(SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()));
            comment.setCommentAuthorIp(ServletUtil.getClientIP(request));
            comment.setCommentAuthorAvatar(user.getUserAvatar());
            comment.setCommentDate(DateUtil.date());
            String lastContent = "<a href='#comment-id-" + lastComment.getCommentId() + "'>@" + lastComment.getCommentAuthor() + "</a> ";
            comment.setCommentContent(lastContent + OwoUtil.markToImg(HtmlUtil.escape(commentContent)));
            comment.setCommentAgent(userAgent);
            comment.setCommentParent(commentId);
            comment.setCommentStatus(CommentStatusEnum.PUBLISHED.getCode());
            //判断是否是博主
            if(Objects.equals(user.getUserId(), post.getUserId())) {
                comment.setIsAdmin(1);
            } else {
                comment.setIsAdmin(0);
            }
            comment.setAcceptUserId(lastComment.getUserId());
            comment.setPathTrace(lastComment.getPathTrace() + lastComment.getCommentId() + "/");
            commentService.saveByComment(comment);
            //邮件通知
            new EmailToAuthor(comment, lastComment, post, user, commentContent).start();
        } catch (Exception e) {
            log.error("回复评论失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.reply-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.reply-success"));

    }

    /**
     * 异步发送邮件回复给评论者
     */
    class EmailToAuthor extends Thread {

        private Comment comment;
        private Comment lastComment;
        private Post post;
        private User user;
        private String commentContent;

        private EmailToAuthor(Comment comment, Comment lastComment, Post post, User user, String commentContent) {
            this.comment = comment;
            this.lastComment = lastComment;
            this.post = post;
            this.user = user;
            this.commentContent = commentContent;
        }

        @Override
        public void run() {
            if (StringUtils.equals(SensConst.OPTIONS.get(BlogPropertiesEnum.SMTP_EMAIL_ENABLE.getProp()), TrueFalseEnum.TRUE.getDesc()) && StringUtils.equals(SensConst.OPTIONS.get(BlogPropertiesEnum.COMMENT_REPLY_NOTICE.getProp()), TrueFalseEnum.TRUE.getDesc())) {
                if (Validator.isEmail(lastComment.getCommentAuthorEmail())) {
                    Map<String, Object> map = new HashMap<>(8);
                    map.put("blogTitle", SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_TITLE.getProp()));
                    map.put("commentAuthor", lastComment.getCommentAuthor());
                    map.put("pageName", post.getPostTitle());
                    if (StringUtils.equals(post.getPostType(), PostTypeEnum.POST_TYPE_POST.getDesc())) {
                        map.put("pageUrl", SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()) + "/article/" + post.getPostId() + "#comment-id-" + comment.getCommentId());
                    } else if(StringUtils.equals(post.getPostType(), PostTypeEnum.POST_TYPE_NOTICE.getDesc())) {
                        map.put("pageUrl", SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()) + "/notice/" + post.getPostId() + "#comment-id-" + comment.getCommentId());
                    } else {
                        map.put("pageUrl", SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()) + "/p/" + post.getPostUrl() + "#comment-id-" + comment.getCommentId());
                    }
                    map.put("commentContent", lastComment.getCommentContent());
                    map.put("replyAuthor", user.getUserDisplayName());
                    map.put("replyContent", commentContent);
                    map.put("blogUrl", SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()));
                    mailService.sendTemplateMail(
                            lastComment.getCommentAuthorEmail(), "您在" + SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_TITLE.getProp()) + "的评论有了新回复", map, "common/mail_template/mail_reply.ftl");
                }
            }
        }
    }

    /**
     * 异步通知评论者审核通过
     */
    class NoticeToAuthor extends Thread {

        private Comment comment;
        private Post post;
        private User user;
        private Integer status;

        private NoticeToAuthor(Comment comment, Post post, User user, Integer status) {
            this.comment = comment;
            this.post = post;
            this.user = user;
            this.status = status;
        }

        @Override
        public void run() {
            if (StringUtils.equals(SensConst.OPTIONS.get(BlogPropertiesEnum.SMTP_EMAIL_ENABLE.getProp()), TrueFalseEnum.TRUE.getDesc()) && StringUtils.equals(SensConst.OPTIONS.get(BlogPropertiesEnum.COMMENT_REPLY_NOTICE.getProp()), TrueFalseEnum.TRUE.getDesc())) {
                try {
                    //待审核的评论变成已通过，发邮件
                    if (status == 1 && Validator.isEmail(comment.getCommentAuthorEmail())) {
                        Map<String, Object> map = new HashMap<>(6);
                        if (StringUtils.equals(post.getPostType(), PostTypeEnum.POST_TYPE_POST.getDesc())) {
                            map.put("pageUrl", SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()) + "/article/" + post.getPostId() + "#comment-id-" + comment.getCommentId());
                        } else if(StringUtils.equals(post.getPostType(), PostTypeEnum.POST_TYPE_NOTICE.getDesc())) {
                            map.put("pageUrl", SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()) + "/notice/" + post.getPostId() + "#comment-id-" + comment.getCommentId());
                        } else {
                            map.put("pageUrl", SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()) + "/p/" + post.getPostUrl() + "#comment-id-" + comment.getCommentId());
                        }
                        map.put("pageName", post.getPostTitle());
                        map.put("commentContent", comment.getCommentContent());
                        map.put("blogUrl", SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()));
                        map.put("blogTitle", SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_TITLE.getProp()));
                        map.put("author", user.getUserDisplayName());
                        mailService.sendTemplateMail(
                                comment.getCommentAuthorEmail(),
                                "您在" + SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_TITLE.getProp()) + "的评论已审核通过！", map, "common/mail_template/mail_passed.ftl");
                    }
                } catch (Exception e) {
                    log.error("邮件服务器未配置：{}", e.getMessage());
                }
            }
        }
    }
}
