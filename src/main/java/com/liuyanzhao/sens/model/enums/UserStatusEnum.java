package com.liuyanzhao.sens.model.enums;

/**
 * <pre>
 *     用户状态enum
 * </pre>
 *
 * @author : saysky
 * @date : 2018/7/1
 */
public enum UserStatusEnum {

    /**
     * 已发布
     */
    NORMAL(0, "正常"),

    /**
     * 草稿
     */
    BAN(1, "已禁用"),

    /**
     * 已删除
     */
    DELETED(2, "已删除");

    private Integer code;
    private String desc;

    UserStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
