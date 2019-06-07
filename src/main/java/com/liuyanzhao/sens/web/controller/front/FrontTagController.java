package com.liuyanzhao.sens.web.controller.front;

import com.baomidou.mybatisplus.plugins.Page;
import com.liuyanzhao.sens.entity.Post;
import com.liuyanzhao.sens.entity.Tag;
import com.liuyanzhao.sens.model.dto.SensConst;
import com.liuyanzhao.sens.model.enums.BlogPropertiesEnum;
import com.liuyanzhao.sens.service.CategoryService;
import com.liuyanzhao.sens.service.PostService;
import com.liuyanzhao.sens.service.TagService;
import com.liuyanzhao.sens.web.controller.core.BaseController;
import cn.hutool.core.util.PageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * <pre>
 *     前台标签控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2018/4/26
 */
@Controller
@RequestMapping(value = "/tag")
public class FrontTagController extends BaseController {

    @Autowired
    private TagService tagService;

    @Autowired
    private PostService postService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 标签
     *
     * @return 模板路径/themes/{theme}/tags
     */
    @GetMapping
    public String tags() {
        return this.render("tags");
    }

    /**
     * 根据标签路径查询所有文章
     *
     * @param tagUrl 标签路径
     * @param model  model
     * @return String
     */
    @GetMapping(value = "{tagUrl}")
    public String tags(Model model,
                       @PathVariable("tagUrl") String tagUrl) {
        return this.tags(model, tagUrl, 1);
    }

    /**
     * 根据标签路径查询所有文章 分页
     *
     * @param model  model
     * @param tagUrl 标签路径
     * @param page   页码
     * @return String
     */
    @GetMapping(value = "{tagUrl}/page/{page}")
    public String tags(Model model,
                       @PathVariable("tagUrl") String tagUrl,
                       @PathVariable("page") Integer page) {
        Tag tag = tagService.findByTagUrl(tagUrl);
        if (null == tag) {
            return this.renderNotFound();
        }
        Integer size = 15;
        if (!StringUtils.isBlank(SensConst.OPTIONS.get(BlogPropertiesEnum.INDEX_POSTS.getProp()))) {
            size = Integer.parseInt(SensConst.OPTIONS.get(BlogPropertiesEnum.INDEX_POSTS.getProp()));
        }
        Page pageable = new Page(page, size);
        Page<Post> posts = postService.findPostsByTags(tag, pageable);
        List<Post> postList = posts.getRecords();
        postList.forEach(post -> {
                    post.setCategories(categoryService.findByPostId(post.getPostId()));
                    post.setPostViews(postService.getPostViewsByPostId(post.getPostId()));
                }
        );
        posts.setRecords(postList);
        model.addAttribute("posts", posts);
        model.addAttribute("tag", tag);
        model.addAttribute("prefix", "/tag/" + tagUrl);
        return this.render("tag");
    }
}
