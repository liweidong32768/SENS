package com.liuyanzhao.sens.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * <pre>
 *     菜单
 * </pre>
 *
 * @author : saysky
 * @date : 2018/1/24
 */
@Data
@TableName("sens_menu")
public class Menu implements Serializable {

    private static final long serialVersionUID = -7726233157376388786L;

    /**
     * 编号 自增
     */
    @TableId(type = IdType.AUTO)
    private Integer menuId;

    /**
     * 菜单Pid
     */
    private Integer menuPid = 0;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单路径
     */
    private String menuUrl;

    /**
     * 排序编号
     */
    private Integer menuSort = 1;

    /**
     * 图标，可选，部分主题可显示
     */
    private String menuIcon;

    /**
     * 打开方式
     */
    private String menuTarget;

    /**
     * 菜单类型(0前台主要菜单，1前台顶部菜单)
     */
    private Integer menuType;

    /**
     * 菜单层级
     */
    private Integer menuLevel = 1;

    /**
     * 子菜单列表
     */
    @TableField(exist = false)
    private List<Menu> childMenus;
}
