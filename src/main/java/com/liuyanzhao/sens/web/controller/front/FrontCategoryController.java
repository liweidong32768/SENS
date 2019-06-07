package com.liuyanzhao.sens.web.controller.front;

import com.baomidou.mybatisplus.plugins.Page;
import com.liuyanzhao.sens.entity.Category;
import com.liuyanzhao.sens.entity.Post;
import com.liuyanzhao.sens.model.dto.SensConst;
import com.liuyanzhao.sens.model.enums.BlogPropertiesEnum;
import com.liuyanzhao.sens.service.CategoryService;
import com.liuyanzhao.sens.service.PostService;
import com.liuyanzhao.sens.web.controller.core.BaseController;
import cn.hutool.core.util.PageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * <pre>
 *     前台文章分类控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2018/4/26
 */
@Controller
@RequestMapping(value = "/category")
public class FrontCategoryController extends BaseController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private PostService postService;


    /**
     * 根据分类路径查询文章
     *
     * @param model   model
     * @param cateUrl cateUrl
     * @return string
     */
    @GetMapping(value = "/{cateUrl}")
    public String categories(Model model,
                             @PathVariable("cateUrl") String cateUrl) {
        return this.categories(model, cateUrl, 1);
    }

    /**
     * 根据分类目录查询所有文章 分页
     *
     * @param model   model
     * @param cateUrl 分类目录路径
     * @param page    页码
     * @return String
     */
    @GetMapping("/{cateUrl}/page/{page}")
    public String categories(Model model,
                             @PathVariable("cateUrl") String cateUrl,
                             @PathVariable("page") Integer page) {
        Category category = categoryService.findByCateUrl(cateUrl);
        if (null == category) {
            return this.renderNotFound();
        }
        Integer size = 15;
        if (!StringUtils.isBlank(SensConst.OPTIONS.get(BlogPropertiesEnum.INDEX_POSTS.getProp()))) {
            size = Integer.parseInt(SensConst.OPTIONS.get(BlogPropertiesEnum.INDEX_POSTS.getProp()));
        }
        Page pagination = new Page(page, size);
        Page<Post> posts = postService.findPostByCategories(category, pagination);
        List<Post> postList = posts.getRecords();
        postList.forEach(post ->
                post.setPostViews(postService.getPostViewsByPostId(post.getPostId()))
        );
        posts.setRecords(postList);
        model.addAttribute("posts", posts);
        model.addAttribute("category", category);
        model.addAttribute("prefix", "/category/" + cateUrl);
        return this.render("category");
    }
}
