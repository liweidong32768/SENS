package com.liuyanzhao.sens.model.enums;

/**
 * @author 言曌
 * @date 2019/1/24 下午5:08
 */

public enum LoginType {

    /**
     * 密码登录
     */
    NORMAL("Normal"),

    /**
     * 免密码登录
     */
    FREE("Free");


    private String desc;

    LoginType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
