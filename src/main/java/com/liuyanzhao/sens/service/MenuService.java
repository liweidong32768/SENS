package com.liuyanzhao.sens.service;

import com.liuyanzhao.sens.entity.Menu;

import java.util.List;

/**
 * <pre>
 *     菜单业务逻辑接口
 * </pre>
 *
 * @author : saysky
 * @date : 2018/1/24
 */
public interface MenuService {

    /**
     * 根据菜单Pid获得菜单
     *
     * @return List
     */
    List<Menu> findByMenuPid(Integer menuId);

    /**
     * 根据编号查询菜单
     *
     * @param menuId menuId
     * @return Optional
     */
    Menu findByMenuId(Integer menuId);

    /**
     * 根据类型查询，以树形展示，用于前台
     *
     * @param menuType 菜单类型
     * @return List
     */
    List<Menu> findMenuTree(Integer menuType);

    /**
     * 根据类型查询，以列表显示展示，用于后台管理
     *
     * @param menuType 菜单类型
     * @return 菜单
     */
    List<Menu> findMenuList(Integer menuType);

    /**
     * 新增/修改菜单
     *
     * @param menu menu
     * @return Menu
     */
    Menu saveByMenu(Menu menu);

    /**
     * 删除菜单
     *
     * @param menuId menuId
     * @return Menu
     */
    void removeByMenuId(Integer menuId);

    /**
     * 根据角色和语言类型获得菜单
     * 后台菜单
     *
     * @param roleId 角色Id
     * @param language 语言
     * @return 角色Id
     */
    List<Menu> getMenuByRoleId(Integer roleId, String language);
}
