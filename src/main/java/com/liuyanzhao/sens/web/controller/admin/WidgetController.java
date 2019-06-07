package com.liuyanzhao.sens.web.controller.admin;

import com.liuyanzhao.sens.entity.Widget;
import com.liuyanzhao.sens.model.enums.WidgetTypeEnum;
import com.liuyanzhao.sens.service.WidgetService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <pre>
 *     后台小工具管理控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2018/1/30
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/widget")
@RequiresPermissions("appearence:widget*")
public class WidgetController {

    @Autowired
    private WidgetService widgetService;


    /**
     * 渲染小工具设置页面
     *
     * @return 模板路径/admin/admin_widget
     */
    @GetMapping
    public String widgets(Model model) {
        //前台主要小工具
        List<Widget> footerWidgets = widgetService.findByWidgetType(WidgetTypeEnum.FOOTER_WIDGET.getCode());
        List<Widget> postDetailSidebarWidgets = widgetService.findByWidgetType(WidgetTypeEnum.POST_DETAIL_SIDEBAR_WIDGET.getCode());
        model.addAttribute("footerWidgets", footerWidgets);
        model.addAttribute("postDetailSidebarWidgets", postDetailSidebarWidgets);
        return "/admin/admin_widget";
    }

    /**
     * 新增/修改小工具
     *
     * @param widget widget
     * @return 重定向到/admin/widget
     */
    @PostMapping(value = "/save")
    public String saveWidget(@ModelAttribute Widget widget) {
        try {
            widgetService.saveByWidget(widget);
        } catch (Exception e) {
            log.error("保存小工具失败：{}" + e.getMessage());
        }
        return "redirect:/admin/widget";
    }

    /**
     * 跳转到修改页面
     *
     * @param widgetId 小工具编号
     * @param model    model
     * @return 模板路径/admin/admin_widget
     */
    @GetMapping(value = "/edit")
    public String updateWidget(@RequestParam("widgetId") Long widgetId, Model model) {
        Widget widget = widgetService.findByWidgetId(widgetId);
        model.addAttribute("updateWidget", widget);

        //前台主要小工具
        List<Widget> footerWidgets = widgetService.findByWidgetType(WidgetTypeEnum.FOOTER_WIDGET.getCode());
        List<Widget> postDetailSidebarWidgets = widgetService.findByWidgetType(WidgetTypeEnum.POST_DETAIL_SIDEBAR_WIDGET.getCode());
        model.addAttribute("footerWidgets", footerWidgets);
        model.addAttribute("postDetailSidebarWidgets", postDetailSidebarWidgets);
        return "/admin/admin_widget";
    }

    /**
     * 删除小工具
     *
     * @param widgetId 小工具编号
     * @return 重定向到/admin/widget
     */
    @GetMapping(value = "/remove")
    public String removeWidget(@RequestParam("widgetId") Long widgetId) {
        try {
            widgetService.removeByWidgetId(widgetId);
        } catch (Exception e) {
            log.error("删除小工具失败：{}", e.getMessage());
        }
        return "redirect:/admin/widget";
    }

}
