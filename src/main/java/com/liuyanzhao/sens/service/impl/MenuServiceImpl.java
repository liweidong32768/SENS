package com.liuyanzhao.sens.service.impl;

import com.liuyanzhao.sens.entity.Menu;
import com.liuyanzhao.sens.entity.Permission;
import com.liuyanzhao.sens.mapper.MenuMapper;
import com.liuyanzhao.sens.mapper.PermissionMapper;
import com.liuyanzhao.sens.model.enums.LanguageType;
import com.liuyanzhao.sens.model.enums.MenuTypeEnum;
import com.liuyanzhao.sens.service.MenuService;
import com.liuyanzhao.sens.utils.MenuUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <pre>
 *     菜单业务逻辑实现类
 * </pre>
 *
 * @author : saysky
 * @date : 2018/1/24
 */
@Service
public class MenuServiceImpl implements MenuService {

    private static final String MENUS_CACHE_NAME = "menus";

    private static final String MENUS_ADMIN_CACHE_NAME = "menus_admin";

    @Autowired(required = false)
    private MenuMapper menuMapper;

    @Autowired(required = false)
    private PermissionMapper permissionMapper;

    @Override
    @Cacheable(value = MENUS_CACHE_NAME, key = "'menus_id_'+#menuId", unless = "#result == null")
    public Menu findByMenuId(Integer menuId) {
        return menuMapper.selectById(menuId);
    }

    @Override
    @Cacheable(value = MENUS_CACHE_NAME, key = "'menus_pid_'+#menuId")
    public List<Menu> findByMenuPid(Integer menuId) {
        return menuMapper.findByMenuPid(menuId);
    }

    @Override
    @Cacheable(value = MENUS_CACHE_NAME, key = "'menus_tree_type_'+#menuType")
    public List<Menu> findMenuTree(Integer menuType) {
        List<Menu> menuList = menuMapper.findByMenuType(menuType);
        //以层级(树)关系显示
        return MenuUtil.getMenuTree(menuList);
    }

    @Override
    @Cacheable(value = MENUS_CACHE_NAME, key = "'menus_list_type_'+#menuType")
    public List<Menu> findMenuList(Integer menuType) {
        List<Menu> menuList = menuMapper.findByMenuType(menuType);
        menuList.forEach(menu -> {
            String str = "";
            for (int i = 1; i < menu.getMenuLevel(); i++) {
                str += "——";
            }
            menu.setMenuName(str + menu.getMenuName());
        });
        //以一级菜单/二级菜单/三级菜单的顺序输出
        return MenuUtil.getMenuList(menuList);
    }

    @Override
    @CacheEvict(value = MENUS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void removeByMenuId(Integer menuId) {
        menuMapper.deleteById(menuId);
    }

    @Override
    @Cacheable(value = MENUS_ADMIN_CACHE_NAME, key = "'menus_roleid_'+#roleId+'_lang_'+#language")
    public List<Menu> getMenuByRoleId(Integer roleId, String language) {
        List<Permission> permissions = permissionMapper.getMenuByRoleId(roleId);
        List<Menu> menus = new ArrayList<>(permissions.size());
        for (Permission permission : permissions) {
            Menu menu = new Menu();
            menu.setMenuId(permission.getId());
            menu.setMenuPid(permission.getPid());
            if (Objects.equals(language, LanguageType.ENGLISH.getValue())) {
                menu.setMenuName(permission.getEnName());
            } else {
                menu.setMenuName(permission.getName());
            }
            menu.setMenuUrl(permission.getUrl());
            if (permission.getPid() == 0) {
                menu.setMenuLevel(1);
            } else {
                menu.setMenuLevel(2);
            }
            menu.setMenuIcon(permission.getIcon());
            menus.add(menu);
        }
        return MenuUtil.getMenuTree(menus);
    }

    @Override
    @CacheEvict(value = MENUS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Menu saveByMenu(Menu menu) {
        //1.设置MenuLevel
        if (menu.getMenuPid() == 0 || menu.getMenuPid() == null) {
            menu.setMenuLevel(1);
        } else {
            Menu parentMenu = this.findByMenuId(menu.getMenuPid());
            if (parentMenu != null && parentMenu.getMenuLevel() != null) {
                menu.setMenuLevel(parentMenu.getMenuLevel() + 1);
            }
        }
        //2.添加/更新菜单
        if (menu != null && menu.getMenuId() != null) {
            menuMapper.updateById(menu);
        } else {
            menuMapper.insert(menu);
        }
        return menu;
    }
}
