package com.liuyanzhao.sens.entity;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("sens_rbac_user_role_ref")
public class UserRoleRef implements Serializable {

    private static final long serialVersionUID = 5091220066943292041L;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 角色Id
     */
    private Integer roleId;

    public UserRoleRef(Long userId, Integer roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public UserRoleRef() {
    }
}