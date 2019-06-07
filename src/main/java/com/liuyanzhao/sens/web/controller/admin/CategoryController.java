package com.liuyanzhao.sens.web.controller.admin;

import com.liuyanzhao.sens.entity.Category;
import com.liuyanzhao.sens.model.dto.JsonResult;
import com.liuyanzhao.sens.model.enums.ResultCodeEnum;
import com.liuyanzhao.sens.service.CategoryService;
import com.liuyanzhao.sens.service.PostService;
import com.liuyanzhao.sens.utils.LocaleMessageUtil;
import com.liuyanzhao.sens.web.controller.core.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * <pre>
 *     后台分类管理控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2017/12/10
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/category")
public class CategoryController extends BaseController{

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private LocaleMessageUtil localeMessageUtil;

    /**
     * 查询所有分类并渲染category页面
     *
     * @return 模板路径admin/admin_category
     */
    @GetMapping
    @RequiresPermissions("post:category:list")
    public String categories(Model model) {
        List<Category> categories = categoryService.findAllCategoriesWithLevel();
        model.addAttribute("categories", categories);
        return "admin/admin_category";
    }

    /**
     * 新增/修改分类目录
     *
     * @param category category对象
     * @return 重定向到/admin/category
     */
    @PostMapping(value = "/save")
    @RequiresPermissions("post:category:save")
    @ResponseBody
    public JsonResult saveCategory(@ModelAttribute Category category) {
        try {
            //1.检查标签路径是否存在
            Category checkUrl = categoryService.findByCateUrl(category.getCateUrl());
            if(checkUrl != null) {
                if((category.getCateId() == null) || !Objects.equals(category.getCateId(), checkUrl.getCateId())) {
                    return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.url-is-exists"));
                }
            }
            categoryService.saveByCategory(category);
        } catch (Exception e) {
            log.error("修改分类失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.save-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.save-success"));
    }

    /**
     * 删除分类
     *
     * @param cateId 分类Id
     * @return JsonResult
     */
    @GetMapping(value = "/remove")
    @ResponseBody
    @RequiresPermissions("post:category:delete")
    public JsonResult checkDelete(@RequestParam("cateId") Long cateId) {
        //判断这个分类有没有文章
        Integer postCount = categoryService.countPostByCateId(cateId);
        if(postCount != 0) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.category.first-delete-child"));
        }
        Integer childCount = categoryService.selectChildCateId(cateId).size();
        if(childCount != 0) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.category.first-delete-post"));
        }
        categoryService.removeByCateId(cateId);
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-success"));
    }


    /**
     * 跳转到修改页面
     *
     * @param cateId cateId
     * @param model  model
     * @return 模板路径admin/admin_category
     */
    @GetMapping(value = "/edit")
    @RequiresPermissions("post:category:edit")
    public String toEditCategory(Model model, @RequestParam("cateId") Long cateId) {
        //更新的分类
        Category category = categoryService.findByCateId(cateId);
        if(category == null) {
            return this.renderNotFound();
        }
        model.addAttribute("updateCategory", category);

        // 所有分类
        List<Category> categories = categoryService.findAllCategoriesWithLevel();
        model.addAttribute("categories", categories);
        return "admin/admin_category";
    }
}
