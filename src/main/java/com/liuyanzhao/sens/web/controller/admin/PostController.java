package com.liuyanzhao.sens.web.controller.admin;

import com.baomidou.mybatisplus.plugins.Page;
import com.liuyanzhao.sens.entity.*;
import com.liuyanzhao.sens.model.dto.SensConst;
import com.liuyanzhao.sens.model.dto.JsonResult;
import com.liuyanzhao.sens.model.dto.LogsRecord;
import com.liuyanzhao.sens.model.enums.*;
import com.liuyanzhao.sens.service.*;
import com.liuyanzhao.sens.utils.SensUtils;
import com.liuyanzhao.sens.utils.LocaleMessageUtil;
import com.liuyanzhao.sens.web.controller.core.BaseController;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
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
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

/**
 * <pre>
 *     后台文章管理控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2017/12/10
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/post")
public class PostController extends BaseController {

    @Autowired
    private PostService postService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private LogService logService;

    @Autowired
    private UserService userService;

    @Autowired(required = false)
    private HttpServletRequest request;

    @Autowired
    private LocaleMessageUtil localeMessageUtil;


    /**
     * 去除html，htm后缀，以及将空格替换成-
     *
     * @param url url
     * @return String
     */
    private static String urlFilter(String url) {
        if (null != url) {
            final boolean urlEndsWithHtmlPostFix = url.endsWith(".html") || url.endsWith(".htm");
            if (urlEndsWithHtmlPostFix) {
                return url.substring(0, url.lastIndexOf("."));
            }
        }
        return StringUtils.replaceAll(url, " ", "-");
    }

    /**
     * 处理后台获取文章列表的请求
     *
     * @param model model
     * @param page  当前页码
     * @param size  每页显示的条数
     * @return 模板路径admin/admin_post
     */
    @GetMapping
    @RequiresPermissions("post:list")
    public String posts(Model model,
                        @RequestParam(value = "status", defaultValue = "0") Integer status,
                        @RequestParam(value = "page", defaultValue = "0") Integer page,
                        @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page pageable = new Page(page, size, "post_date", false);
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        Boolean isAdmin = subject.hasRole(RoleEnum.ADMIN.getDesc());
        Page<Post> posts;
        if (isAdmin) {
            posts = postService.findPostByStatus(status, PostTypeEnum.POST_TYPE_POST.getDesc(), pageable);
        } else {
            posts = postService.findPostByUserIdAndStatus(user.getUserId(), status, PostTypeEnum.POST_TYPE_POST.getDesc(), pageable);
        }
        //封装分类和标签
        List<Post> postList = posts.getRecords();
        postList.forEach(post -> {
            List<Category> categories = categoryService.findByPostId(post.getPostId());
            List<Tag> tags = tagService.findByPostId(post.getPostId());
            post.setCategories(categories);
            post.setTags(tags);
            post.setPostViews(postService.getPostViewsByPostId(post.getPostId()));
            post.setUser(userService.findByUserId(post.getUserId()));
        });
        posts.setRecords(postList);
        model.addAttribute("posts", posts);
        if (isAdmin) {
            model.addAttribute("publishCount", postService.countByPostTypeAndStatus(PostTypeEnum.POST_TYPE_POST.getDesc(), PostStatusEnum.PUBLISHED.getCode()));
            model.addAttribute("checkingCount", postService.countByPostTypeAndStatus(PostTypeEnum.POST_TYPE_POST.getDesc(), PostStatusEnum.CHECKING.getCode()));
            model.addAttribute("draftCount", postService.countByPostTypeAndStatus(PostTypeEnum.POST_TYPE_POST.getDesc(), PostStatusEnum.DRAFT.getCode()));
            model.addAttribute("trashCount", postService.countByPostTypeAndStatus(PostTypeEnum.POST_TYPE_POST.getDesc(), PostStatusEnum.RECYCLE.getCode()));
        } else {
            model.addAttribute("publishCount", postService.countArticleByUserIdAndStatus(user.getUserId(), PostStatusEnum.PUBLISHED.getCode()));
            model.addAttribute("checkingCount", postService.countArticleByUserIdAndStatus(user.getUserId(), PostStatusEnum.CHECKING.getCode()));
            model.addAttribute("draftCount", postService.countArticleByUserIdAndStatus(user.getUserId(), PostStatusEnum.DRAFT.getCode()));
            model.addAttribute("trashCount", postService.countArticleByUserIdAndStatus(user.getUserId(), PostStatusEnum.RECYCLE.getCode()));
        }
        model.addAttribute("status", status);
        return "admin/admin_post";
    }

    /**
     * 模糊查询文章
     *
     * @param model   Model
     * @param keyword keyword 关键字
     * @param page    page 当前页码
     * @param size    size 每页显示条数
     * @return 模板路径admin/admin_post
     */
    @GetMapping(value = "/search")
    @RequiresPermissions("post:search")
    @Deprecated
    public String searchPost(Model model,
                             @RequestParam(value = "keyword") String keyword,
                             @RequestParam(value = "page", defaultValue = "0") Integer page,
                             @RequestParam(value = "size", defaultValue = "10") Integer size) {
        try {
            //排序规则
            Page pageable = new Page(page, size);
            model.addAttribute("posts", postService.searchPosts(keyword, pageable));
        } catch (Exception e) {
            log.error("未知错误：{}", e.getMessage());
        }
        return "admin/admin_post";
    }


    /**
     * 处理跳转到新建文章页面
     *
     * @return 模板路径admin/admin_editor
     */
    @GetMapping(value = "/new")
    @RequiresPermissions("post:new")
    public String newPost() {
        return "admin/admin_post_editor";
    }

    /**
     * 添加文章
     *
     * @param post     Post实体
     * @param cateList 分类列表
     * @param tagList  标签列表
     */
    @PostMapping(value = "/new/push")
    @ResponseBody
    @RequiresPermissions("post:save")
    public JsonResult pushPost(@ModelAttribute Post post,
                               @RequestParam("cateList") List<String> cateList,
                               @RequestParam("tagList") String tagList) {
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        //如果开启了文章审核
        if (StringUtils.equals(SensConst.OPTIONS.get(BlogPropertiesEnum.OPEN_POST_CHECK.getProp()), TrueFalseEnum.TRUE.getDesc()) &&
                subject.hasRole(RoleEnum.CONTRIBUTOR.getDesc()) && Objects.equals(post.getPostStatus(), PostStatusEnum.PUBLISHED.getCode())) {
            post.setPostStatus(PostStatusEnum.CHECKING.getCode());
        }

        String msg = localeMessageUtil.getMessage("code.admin.common.save-success");
        //发布
        boolean isNew = post.getPostId() == null;
        try {
            //检查用户
            if (post.getPostId() != null) {
                //管理员和文章作者可以删除
                Boolean isAdmin = subject.hasRole(RoleEnum.ADMIN.getDesc());
                Post checkPost = postService.findByPostId(post.getPostId());
                if (!Objects.equals(checkPost.getUserId(), user.getUserId()) && !isAdmin) {
                    return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.permission-denied"));
                }
            }
            //提取摘要
            int postSummary = 100;
            if (StringUtils.isNotEmpty(SensConst.OPTIONS.get(BlogPropertiesEnum.POST_SUMMARY.getProp()))) {
                postSummary = Integer.parseInt(SensConst.OPTIONS.get(BlogPropertiesEnum.POST_SUMMARY.getProp()));
            }
            //文章摘要
            String summaryText = HtmlUtil.cleanHtmlTag(post.getPostContent());
            if (summaryText.length() > postSummary) {
                String summary = summaryText.substring(0, postSummary);
                post.setPostSummary(summary);
            } else {
                post.setPostSummary(summaryText);
            }
            //添加文章时，添加文章时间和修改文章时间为当前时间，修改文章时，只更新修改文章时间
            if (null != post.getPostId()) {
                Post oldPost = postService.findByPostId(post.getPostId());
                post.setPostDate(oldPost.getPostDate());
                post.setPostViews(postService.getPostViewsByPostId(post.getPostId()));
                msg = localeMessageUtil.getMessage("code.admin.common.update-success");
            } else {
                post.setUserId(user.getUserId());
                post.setPostDate(DateUtil.date());
            }
            List<Category> categories = categoryService.strListToCateList(cateList);
            post.setCategories(categories);
            if (StringUtils.isNotEmpty(tagList)) {
                List<Tag> tags = tagService.strListToTagList(StringUtils.deleteWhitespace(tagList));
                post.setTags(tags);
            }
            post.setPostUrl(urlFilter(post.getPostUrl()));
            //当没有选择文章缩略图的时候，自动分配一张内置的缩略图
            if (StringUtils.equals(post.getPostThumbnail(), BlogPropertiesEnum.DEFAULT_THUMBNAIL.getProp())) {
                post.setPostThumbnail("/static/images/thumbnail/img_" + RandomUtil.randomInt(0, 14) + ".jpg");
            }
            postService.saveByPost(post);
            //发布文章的时候才记录日志
            if (isNew) {
                logService.saveByLog(new Log(LogsRecord.PUSH_POST, post.getPostTitle(), ServletUtil.getClientIP(request), DateUtil.date()));
            }
            return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), msg);
        } catch (Exception e) {
            log.error("保存文章失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.save-failed"));
        }
    }


    /**
     * 自动保存文章为草稿
     *
     * @param post     文章
     * @param cateList 分类列表
     * @param tagList  标签列表
     */
    @PostMapping(value = "/new/autoPush")
    @ResponseBody
    @RequiresPermissions("post:save-auto")
    public JsonResult autoPushPost(@ModelAttribute Post post,
                                   @RequestParam("cateList") List<String> cateList,
                                   @RequestParam("tagList") String tagList) {

        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        try {
            //检查用户
            if (post.getPostId() != null) {
                Post checkPost = postService.findByPostId(post.getPostId());
                if (!Objects.equals(checkPost.getUserId(), user.getUserId())) {
                    return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.permission-denied"));
                }
            }
            if (StringUtils.isEmpty(post.getPostTitle())) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                post.setPostTitle("草稿：" + dateFormat.format(DateUtil.date()));
            }
            if (StringUtils.isEmpty(post.getPostUrl())) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                post.setPostUrl(dateFormat.format(DateUtil.date()));
            }
            if (post.getPostStatus() == null) {
                post.setPostStatus(PostStatusEnum.DRAFT.getCode());
            }
            post.setPostDate(DateUtil.date());
            post.setPostUpdate(DateUtil.date());
            post.setUserId(user.getUserId());
            List<Category> categories = categoryService.strListToCateList(cateList);
            post.setCategories(categories);
            if (StringUtils.isNotEmpty(tagList)) {
                List<Tag> tags = tagService.strListToTagList(StringUtils.deleteWhitespace(tagList));
                post.setTags(tags);
            }
            post.setPostUrl(urlFilter(post.getPostUrl()));
            //当没有选择文章缩略图的时候，自动分配一张内置的缩略图
            if (StringUtils.equals(post.getPostThumbnail(), BlogPropertiesEnum.DEFAULT_THUMBNAIL.getProp())) {
                post.setPostThumbnail("/static/images/thumbnail/img_" + RandomUtil.randomInt(0, 14) + ".jpg");
            }
        } catch (Exception e) {
            log.error("未知错误：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.save-auto-failed"));
        }
        Post result = postService.saveByPost(post);
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.save-auto-success"), result);
    }


    /**
     * 处理移至回收站的请求
     *
     * @param postId 文章编号
     * @return 重定向到/admin/post
     */
    @PostMapping(value = "/throw")
    @RequiresPermissions("post:throw")
    @ResponseBody
    public JsonResult moveToTrash(@RequestParam("postId") Long postId) {
        try {
            Post post = postService.findByPostId(postId);
            if (post == null) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.post-not-exist"));
            }
            Subject subject = SecurityUtils.getSubject();
            User user = (User) subject.getPrincipal();

            //管理员和文章作者可以操作
            Boolean isAdmin = subject.hasRole(RoleEnum.ADMIN.getDesc());
            if (!Objects.equals(post.getUserId(), user.getUserId()) && !isAdmin) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.permission-denied"));
            }
            postService.updatePostStatus(postId, PostStatusEnum.RECYCLE.getCode());
            log.info("编号为" + postId + "的文章已被移到回收站");
        } catch (Exception e) {
            log.error("删除文章到回收站失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.operation-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.operation-success"));
    }

    /**
     * 处理文章为发布的状态
     *
     * @param postId 文章编号
     * @return 重定向到/admin/post
     */
    @PostMapping(value = "/revert")
    @RequiresPermissions("post:revert")
    @ResponseBody
    public JsonResult moveToPublish(@RequestParam("postId") Long postId) {
        try {
            Post post = postService.findByPostId(postId);
            if (post == null) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.post-not-exist"));
            }
            Subject subject = SecurityUtils.getSubject();
            User user = (User) subject.getPrincipal();
            //管理员和文章作者可以操作
            Boolean isAdmin = subject.hasRole(RoleEnum.ADMIN.getDesc());
            if (!Objects.equals(post.getUserId(), user.getUserId()) && !isAdmin) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.permission-denied"));
            }
            postService.updatePostStatus(postId, PostStatusEnum.PUBLISHED.getCode());
            log.info("编号为" + postId + "的文章已改变为发布状态");
        } catch (Exception e) {
            log.error("发布文章失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.operation-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.operation-success"));
    }

    /**
     * 处理删除文章的请求
     *
     * @param postId 文章编号
     * @return 重定向到/admin/post
     */
    @PostMapping(value = "/remove")
    @RequiresPermissions("post:delete")
    @ResponseBody
    public JsonResult removePost(@RequestParam("postId") Long postId) {
        try {
            Post post = postService.findByPostId(postId);
            if (post == null) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.post-not-exist"));
            }
            //只有创建者有权删除
            Subject subject = SecurityUtils.getSubject();
            User user = (User) subject.getPrincipal();
            //管理员和文章作者可以删除
            Boolean isAdmin = subject.hasRole(RoleEnum.ADMIN.getDesc());
            if (!Objects.equals(post.getUserId(), user.getUserId()) && !isAdmin) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.permission-denied"));
            }
            postService.removeByPostId(postId);
            logService.saveByLog(new Log(LogsRecord.REMOVE_POST, post.getPostTitle(), ServletUtil.getClientIP(request), DateUtil.date()));
        } catch (Exception e) {
            log.error("删除文章/页面失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-success"));
    }

    /**
     * 跳转到编辑文章页面
     *
     * @param postId 文章编号
     * @param model  model
     * @return 模板路径admin/admin_editor
     */
    @GetMapping(value = "/edit")
    @RequiresPermissions("post:edit")
    public String editPost(@RequestParam("postId") Long postId, Model model) {
        Post post = postService.findByPostId(postId);
        if (post == null) {
            return this.renderNotFound();
        }
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        //管理员和文章作者编辑
        Boolean isAdmin = subject.hasRole(RoleEnum.ADMIN.getDesc());
        if (!Objects.equals(post.getUserId(), user.getUserId()) && !isAdmin) {
            return this.renderNotAllowAccess();
        }
        //标签
        List<Tag> tags = tagService.findByPostId(postId);
        post.setTags(tags);
        //分类
        List<Category> categories = categoryService.findByPostId(postId);
        post.setCategories(categories);
        model.addAttribute("post", post);
        return "admin/admin_post_editor";
    }


    /**
     * 验证文章路径是否已经存在
     *
     * @param postUrl 文章路径
     * @return JsonResult
     */
    @GetMapping(value = "/checkUrl")
    @ResponseBody
    @RequiresPermissions("post:url-check")
    public JsonResult checkUrlExists(@RequestParam("postUrl") String postUrl) {
        postUrl = urlFilter(postUrl);
        Post post = postService.findByPostUrl(postUrl, PostTypeEnum.POST_TYPE_POST.getDesc());
        if (null != post) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.url-is-exists"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), "");
    }


    /**
     * 更新所有摘要
     *
     * @param postSummary 文章摘要字数
     * @return JsonResult
     */
    @PostMapping(value = "/updateSummary")
    @ResponseBody
    @RequiresPermissions("post:summary-update")
    public JsonResult updateSummary(@RequestParam("postSummary") Integer postSummary) {
        try {
            postService.updateAllSummary(postSummary);
        } catch (Exception e) {
            log.error("更新摘要失败：{}", e.getMessage());
            e.printStackTrace();
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.update-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.update-success"));
    }

    /**
     * 将所有文章推送到百度
     *
     * @param baiduToken baiduToken
     * @return JsonResult
     */
    @PostMapping(value = "/pushAllToBaidu")
    @ResponseBody
    @RequiresPermissions("post:baidu-push")
    public JsonResult pushAllToBaidu(@RequestParam("baiduToken") String baiduToken) {
        if (StringUtils.isEmpty(baiduToken)) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.post.no-baidu-token"));
        }
        String blogUrl = SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp());
        List<Post> posts = postService.findAllPosts(PostTypeEnum.POST_TYPE_POST.getDesc());
        StringBuilder urls = new StringBuilder();
        for (Post post : posts) {
            urls.append(blogUrl);
            urls.append("/article/");
            urls.append(post.getPostUrl());
            urls.append("\n");
        }
        String result = SensUtils.baiduPost(blogUrl, baiduToken, urls.toString());
        if (StringUtils.isEmpty(result)) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.post.push-to-baidu-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.post.push-to-baidu-success"));
    }

    /**
     * 同步所有文章访问量
     *
     * @return JsonResult
     */
    @PostMapping(value = "/syncAllPostViews")
    @ResponseBody
    @RequiresPermissions("post:view-sync")
    public JsonResult syncAllPostViews() {
        String time = "";
        try {
            time = postService.syncAllPostView();
        } catch (Exception e) {
            log.error("同步所有文章访问量失败, cause:{}", e);
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.post-views-sync-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), "同步成功, 总耗时：" + time);
    }
}
