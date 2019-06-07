package com.liuyanzhao.sens.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 邮件验证码
 * @author 言曌
 * @date 2018/2/23 上午10:24
 */

@Data
@TableName("sens_mail_retrieve")
public class MailRetrieve implements Serializable {

    private static final long serialVersionUID = -9195267893531526707L;

    /**
     * Id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * Email
     */
    private String email;

    /**
     * 验证码
     */
    private String code;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 过期时间
     */
    private Date outTime;


}