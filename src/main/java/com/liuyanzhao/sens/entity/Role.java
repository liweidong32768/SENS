package com.liuyanzhao.sens.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author liuyanzhao
 */
@Data
@TableName("sens_rbac_role")
public class Role implements Serializable  {

    private static final long serialVersionUID = 2233979806802117985L;

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 角色名称：admin，author，subscriber
     */
    private String role;

    /**
     * 描述：管理员，作者，订阅者
     */
    private String description;

    /**
     * 级别，越小越大
     */
    private Integer level;

    /**
     * 该角色对应的用户数量，非数据库字段
     */
    @TableField(exist = false)
    private Integer count;

    /**
     * 当前角色的权限列表
     */
    @TableField(exist = false)
    private List<Permission> permissions;

}