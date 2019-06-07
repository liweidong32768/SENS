package com.liuyanzhao.sens.entity;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("sens_rbac_role_permission_ref")
public class RolePermissionRef implements Serializable {

    private static final long serialVersionUID = -4015823675140925791L;

    /**
     * 角色Id
     */
    private Integer roleId;

    /**
     * 权限Id
     */
    private Integer permissionId;

    public RolePermissionRef() {
    }

    public RolePermissionRef(Integer roleId, Integer permissionId) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }
}