package com.liuyanzhao.sens.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <pre>
 *     第三方应用绑定
 * </pre>
 *
 * @author : saysky
 * @date : 2018/1/19
 */
@Data
@TableName("sens_third_app_bind")
public class ThirdAppBind implements Serializable {

    private static final long serialVersionUID = -2571815432301283171L;

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * OpenId
     */
    private String openId;

    /**
     * 绑定类型：qq/github
     */
    private String appType;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 发表日期
     */
    private Date createTime;

    /**
     * 状态：1启动，0禁用
     */
    private Integer status;

}
