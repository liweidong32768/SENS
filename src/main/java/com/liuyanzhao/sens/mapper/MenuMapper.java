package com.liuyanzhao.sens.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.liuyanzhao.sens.entity.Menu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author liuyanzhao
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 根据类型查询
     * @param menuType 菜单类型

     * @return List
     */
    List<Menu> findByMenuType(Integer menuType);

    /**
     * 根据菜单Pid获得菜单
     *
     * @param menuId 菜单ID
     * @return List
     */
    List<Menu> findByMenuPid(Integer menuId);

}

