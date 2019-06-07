package com.liuyanzhao.sens.web.controller.admin;

import com.liuyanzhao.sens.entity.Tag;
import com.liuyanzhao.sens.model.dto.JsonResult;
import com.liuyanzhao.sens.model.enums.ResultCodeEnum;
import com.liuyanzhao.sens.service.TagService;
import com.liuyanzhao.sens.utils.LocaleMessageUtil;
import com.liuyanzhao.sens.web.controller.core.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * <pre>
 *     后台标签管理控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2017/12/10
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/tag")
public class TagController extends BaseController {

    @Autowired
    private TagService tagService;

    @Autowired
    private LocaleMessageUtil localeMessageUtil;

    /**
     * 渲染标签管理页面
     *
     * @return 模板路径admin/admin_tag
     */
    @GetMapping
    @RequiresPermissions("post:tag:list")
    public String tags(Model model) {
        List<Tag> tags = tagService.findAllTags();
        model.addAttribute("tags", tags);
        return "admin/admin_tag";
    }

    /**
     * 新增/修改标签
     *
     * @param tag tag
     * @return 重定向到/admin/tag
     */
    @PostMapping(value = "/save")
    @RequiresPermissions("post:tag:save")
    @ResponseBody
    public JsonResult saveTag(@ModelAttribute Tag tag) {
        try {
            //1.检查标签路径是否存在
            Tag checkUrl = tagService.findByTagUrl(tag.getTagUrl());
            if ((tag.getTagId() == null && checkUrl != null) || (tag.getTagId() != null && !Objects.equals(tag.getTagId(), checkUrl.getTagId()))) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.url-is-exists"));
            }
            tagService.saveByTag(tag);
        } catch (Exception e) {
            log.error("新增/修改标签失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.save-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.save-success"));

    }

    /**
     * 删除标签
     *
     * @param tagId 标签Id
     * @return JsonResult
     */
    @GetMapping(value = "/remove")
    @ResponseBody
    @RequiresPermissions("post:tag:delete")
    public JsonResult checkDelete(@RequestParam("tagId") Long tagId) {
        try {
            tagService.removeByTagId(tagId);
        } catch (Exception e) {
            log.error("删除标签失败, tagId:{}, cause:{}", tagId, e);
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-success"));

    }

    /**
     * 跳转到修改标签页面
     *
     * @param model model
     * @param tagId 标签编号
     * @return 模板路径admin/admin_tag
     */
    @GetMapping(value = "/edit")
    @RequiresPermissions("post:tag:edit")
    public String toEditTag(Model model, @RequestParam("tagId") Long tagId) {
        //当前修改的标签
        Tag tag = tagService.findByTagId(tagId);
        if (tag == null) {
            return this.renderNotFound();
        }
        model.addAttribute("updateTag", tag);

        //所有标签
        List<Tag> tags = tagService.findAllTags();
        model.addAttribute("tags", tags);
        return "admin/admin_tag";
    }
}
