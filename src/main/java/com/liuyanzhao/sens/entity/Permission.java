package com.liuyanzhao.sens.entity;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * 权限，后台的菜单
 * @author liuyanzhao
 */
@Data
@TableName("sens_rbac_permission")
public class Permission implements Serializable  {

    private static final long serialVersionUID = 2233979806802117985L;

    /**
     * ID
     */
    private Integer id;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限菜单英文名称
     */
    private String enName;

    /**
     * pid
     */
    private Integer pid;

    /**
     * 权限：user:list，user:add等
     */
    private String permission;

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 请求URL
     */
    private String url;

    /**
     * 图标
     */
    private String icon;

    public Permission() {
    }

    public Permission(Integer id) {
        this.id = id;
    }
}