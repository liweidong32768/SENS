package com.liuyanzhao.sens.web.controller.admin;

import com.liuyanzhao.sens.entity.Slide;
import com.liuyanzhao.sens.model.dto.JsonResult;
import com.liuyanzhao.sens.model.enums.SlideTypeEnum;
import com.liuyanzhao.sens.model.enums.ResultCodeEnum;
import com.liuyanzhao.sens.service.SlideService;
import com.liuyanzhao.sens.utils.LocaleMessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <pre>
 *     后台幻灯片管理控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2018/1/30
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/slide")
@RequiresPermissions("appearence:slide*")
public class SlideController {

    @Autowired
    private SlideService slideService;


    /**
     * 渲染幻灯片设置页面
     *
     * @return 模板路径/admin/admin_slide
     */
    @GetMapping
    public String slides(Model model) {
        //前台主要幻灯片
        List<Slide> slides = slideService.findBySlideType(SlideTypeEnum.INDEX_SLIDE.getCode());
        model.addAttribute("slides", slides);
        return "/admin/admin_slide";
    }

    /**
     * 新增/修改幻灯片
     *
     * @param slide slide
     * @return 重定向到/admin/slide
     */
    @PostMapping(value = "/save")
    public String saveSlide(@ModelAttribute Slide slide) {
        try {
            slideService.saveBySlide(slide);
        } catch (Exception e) {
            log.error("保存幻灯片失败：{}" + e.getMessage());
        }
        return "redirect:/admin/slide";
    }

    /**
     * 跳转到修改页面
     *
     * @param slideId 幻灯片编号
     * @param model   model
     * @return 模板路径/admin/admin_slide
     */
    @GetMapping(value = "/edit")
    public String updateSlide(@RequestParam("slideId") Long slideId, Model model) {
        Slide slide = slideService.findBySlideId(slideId);
        model.addAttribute("updateSlide", slide);

        List<Slide> slides = slideService.findBySlideType(SlideTypeEnum.INDEX_SLIDE.getCode());
        model.addAttribute("slides", slides);
        return "/admin/admin_slide";
    }

    /**
     * 删除幻灯片
     *
     * @param slideId 幻灯片编号
     * @return 重定向到/admin/slide
     */
    @GetMapping(value = "/remove")
    public String removeSlide(@RequestParam("slideId") Long slideId) {
        try {
            slideService.removeBySlideId(slideId);
        } catch (Exception e) {
            log.error("删除幻灯片失败：{}", e.getMessage());
        }
        return "redirect:/admin/slide";
    }

}
