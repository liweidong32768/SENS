package com.liuyanzhao.sens.web.controller.admin;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.baomidou.mybatisplus.plugins.Page;
import com.liuyanzhao.sens.entity.*;
import com.liuyanzhao.sens.model.dto.JsonResult;
import com.liuyanzhao.sens.model.dto.LogsRecord;
import com.liuyanzhao.sens.model.enums.*;
import com.liuyanzhao.sens.service.GalleryService;
import com.liuyanzhao.sens.service.LinkService;
import com.liuyanzhao.sens.service.LogService;
import com.liuyanzhao.sens.service.PostService;
import com.liuyanzhao.sens.utils.LocaleMessageUtil;
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
import java.util.List;
import java.util.Objects;

/**
 * <pre>
 *     后台公告管理控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2017/12/10
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/notice")
public class NoticeController {


    @Autowired
    private PostService postService;

    @Autowired
    private LogService logService;

    @Autowired(required = false)
    private HttpServletRequest request;

    @Autowired
    LocaleMessageUtil localeMessageUtil;

    /**
     * 处理后台获取公告列表的请求
     *
     * @param model model
     * @param page  当前页码
     * @param size  每页显示的条数
     * @return 模板路径admin/admin_post
     */
    @GetMapping
    @RequiresPermissions("notice:list")
    public String posts(Model model,
                        @RequestParam(value = "status", defaultValue = "0") Integer status,
                        @RequestParam(value = "page", defaultValue = "0") Integer page,
                        @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page pageable = new Page(page, size, "post_date", false);
        Page<Post> posts;
        posts = postService.findPostByStatus(status, PostTypeEnum.POST_TYPE_NOTICE.getDesc(), pageable);
        List<Post> postList = posts.getRecords();
        postList.forEach(post -> post.setPostViews(postService.getPostViewsByPostId(post.getPostId())));
        posts.setRecords(postList);
        model.addAttribute("posts", posts);
        model.addAttribute("publishCount", postService.countByPostTypeAndStatus(PostTypeEnum.POST_TYPE_NOTICE.getDesc(), PostStatusEnum.PUBLISHED.getCode()));
        model.addAttribute("draftCount", postService.countByPostTypeAndStatus(PostTypeEnum.POST_TYPE_NOTICE.getDesc(), PostStatusEnum.DRAFT.getCode()));
        model.addAttribute("trashCount", postService.countByPostTypeAndStatus(PostTypeEnum.POST_TYPE_NOTICE.getDesc(), PostStatusEnum.RECYCLE.getCode()));
        model.addAttribute("status", status);
        return "admin/admin_notice";
    }

    /**
     * 跳转到新建公告
     *
     * @return 模板路径admin/admin_notice_editor
     */
    @RequiresPermissions("notice:others")
    @GetMapping(value = "/new")
    public String newPage() {
        return "admin/admin_notice_editor";
    }

    /**
     * 发表公告
     *
     * @param post post
     */
    @PostMapping(value = "/new/push")
    @ResponseBody
    @RequiresPermissions("notice:others")
    public JsonResult pushPage(@ModelAttribute Post post) {

        String msg = localeMessageUtil.getMessage("code.admin.common.save-success");
        try {
            post.setPostDate(DateUtil.date());
            //发表用户
            Subject subject = SecurityUtils.getSubject();
            User user = (User) subject.getPrincipal();
            post.setUserId(user.getUserId());
            post.setPostType(PostTypeEnum.POST_TYPE_NOTICE.getDesc());
            if (null != post.getPostId()) {
                post.setPostViews(postService.findByPostId(post.getPostId()).getPostViews());
                post.setPostDate(postService.findByPostId(post.getPostId()).getPostDate());
                msg = localeMessageUtil.getMessage("code.admin.common.update-success");
            } else {
                post.setPostDate(DateUtil.date());
            }

            postService.saveByPost(post);
            logService.saveByLog(new Log(LogsRecord.PUSH_NOTICE, post.getPostTitle(), ServletUtil.getClientIP(request), DateUtil.date()));
            return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), msg);
        } catch (Exception e) {
            log.error("保存公告失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.save-failed"));
        }
    }

    /**
     * 跳转到修改公告
     *
     * @param postId 公告编号
     * @param model  model
     * @return admin/admin_page_editor
     */
    @GetMapping(value = "/edit")
    @RequiresPermissions("notice:others")
    public String editPage(@RequestParam("postId") Long postId, Model model) {
        Post post = postService.findByPostId(postId);
        model.addAttribute("post", post);
        return "admin/admin_notice_editor";
    }

    /**
     * 处理移至回收站的请求
     *
     * @param postId 公告编号
     * @return 重定向到/admin/post
     */
    @PostMapping(value = "/throw")
    @ResponseBody
    @RequiresPermissions("notice:others")
    public JsonResult moveToTrash(@RequestParam("postId") Long postId) {
        try {
            Post post = postService.findByPostId(postId);
            if (post == null) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.post-not-exist"));
            }

            postService.updatePostStatus(postId, PostStatusEnum.RECYCLE.getCode());
        } catch (Exception e) {
            log.error("删除公告到回收站失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.operation-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.operation-success"));
    }

    /**
     * 处理公告为发布的状态
     *
     * @param postId 公告编号
     * @return 重定向到/admin/post
     */
    @PostMapping(value = "/revert")
    @ResponseBody
    @RequiresPermissions("notice:others")
    public JsonResult moveToPublish(@RequestParam("postId") Long postId) {
        try {
            Post post = postService.findByPostId(postId);
            if (post == null) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.post-not-exist"));
            }

            postService.updatePostStatus(postId, PostStatusEnum.PUBLISHED.getCode());
        } catch (Exception e) {
            log.error("发布公告失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.operation-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.operation-success"));
    }

    /**
     * 处理删除公告的请求
     *
     * @param postId 公告编号
     * @return 重定向到/admin/post
     */
    @PostMapping(value = "/remove")
    @ResponseBody
    @RequiresPermissions("notice:others")
    public JsonResult removePost(@RequestParam("postId") Long postId) {
        try {
            Post post = postService.findByPostId(postId);
            if (post == null) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.post-not-exist"));
            }

            postService.removeByPostId(postId);
        } catch (Exception e) {
            log.error("删除公告失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-success"));
    }


}
