package com.liuyanzhao.sens.model.enums;

/**
 * @author 言曌
 * @date 2019/1/24 下午5:08
 */

public enum RoleEnum {

    /**
     * 管理员
     */
    ADMIN("admin"),

    /**
     * 作者
     */
    AUTHOR("author"),

    /**
     * 投稿者
     */
    CONTRIBUTOR("contributor"),

    /**
     * 订阅者
     */
    SUBSCRIBER("subscriber"),

    /**
     * 没有角色
     */
    NONE("none");

    private String desc;

    RoleEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
