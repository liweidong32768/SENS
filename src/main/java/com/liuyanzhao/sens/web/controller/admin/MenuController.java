package com.liuyanzhao.sens.web.controller.admin;

import com.liuyanzhao.sens.entity.Menu;
import com.liuyanzhao.sens.model.dto.JsonResult;
import com.liuyanzhao.sens.model.enums.MenuTypeEnum;
import com.liuyanzhao.sens.model.enums.ResultCodeEnum;
import com.liuyanzhao.sens.service.MenuService;
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
 *     后台菜单管理控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2018/1/30
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/menu")
@RequiresPermissions("appearence:menu*")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @Autowired
    LocaleMessageUtil localeMessageUtil;

    /**
     * 渲染菜单设置页面
     *
     * @return 模板路径/admin/admin_menu
     */
    @GetMapping
    public String menus(Model model) {
        //前台主要菜单
        List<Menu> frontMainMenus = menuService.findMenuList(MenuTypeEnum.FRONT_MAIN_MENU.getCode());
        //前台顶部菜单
        List<Menu> frontTopMenus = menuService.findMenuList(MenuTypeEnum.FRONT_TOP_MENU.getCode());
        model.addAttribute("frontMainMenus", frontMainMenus);
        model.addAttribute("frontTopMenus", frontTopMenus);
        return "/admin/admin_menu";
    }

    /**
     * 新增/修改菜单
     *
     * @param menu menu
     * @return 重定向到/admin/menu
     */
    @PostMapping(value = "/save")
    public String saveMenu(@ModelAttribute Menu menu) {
        try {
            menuService.saveByMenu(menu);
        } catch (Exception e) {
            log.error("保存菜单失败：{}" + e.getMessage());
        }
        return "redirect:/admin/menu";
    }

    /**
     * 跳转到修改页面
     *
     * @param menuId 菜单编号
     * @param model  model
     * @return 模板路径/admin/admin_menu
     */
    @GetMapping(value = "/edit")
    public String updateMenu(@RequestParam("menuId") Integer menuId, Model model) {
        Menu menu = menuService.findByMenuId(menuId);
        model.addAttribute("updateMenu", menu);

        //前台主要菜单
        List<Menu> frontMainMenus = menuService.findMenuList(MenuTypeEnum.FRONT_MAIN_MENU.getCode());
        //前台顶部菜单
        List<Menu> frontTopMenus = menuService.findMenuList(MenuTypeEnum.FRONT_TOP_MENU.getCode());
        model.addAttribute("frontMainMenus", frontMainMenus);
        model.addAttribute("frontTopMenus", frontTopMenus);
        return "/admin/admin_menu";
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单编号
     * @return 重定向到/admin/menu
     */
    @GetMapping(value = "/remove")
    public String removeMenu(@RequestParam("menuId") Integer menuId) {
        try {
            //1.先查看该菜单是否有子节点，如果有不能删除
            List<Menu> childMenus = menuService.findByMenuPid(menuId);
            if (childMenus == null || childMenus.size() == 0) {
                menuService.removeByMenuId(menuId);
            } else {
                String msg = localeMessageUtil.getMessage("code.admin.common.must-delete-parent-node");
                return "redirect:/admin/menu?error=" + msg;
            }
        } catch (Exception e) {
            log.error("删除菜单失败：{}", e.getMessage());
        }
        return "redirect:/admin/menu";
    }

    @GetMapping("/type/{menuType}")
    @ResponseBody
    public JsonResult listMenusByType(@PathVariable("menuType") Integer menuType) {
        List<Menu> menus = null;
        String msg = localeMessageUtil.getMessage("code.admin.common.query-success");
        try {
            if (menuType == MenuTypeEnum.FRONT_MAIN_MENU.getCode()) {
                menus = menuService.findMenuList(MenuTypeEnum.FRONT_MAIN_MENU.getCode());
            } else if (menuType == MenuTypeEnum.FRONT_TOP_MENU.getCode()) {
                menus = menuService.findMenuList(MenuTypeEnum.FRONT_TOP_MENU.getCode());
            }
        } catch (Exception e) {
            msg = localeMessageUtil.getMessage("code.admin.common.query-failed");
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), msg);
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), msg, menus);
    }
}
