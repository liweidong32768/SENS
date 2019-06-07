package com.liuyanzhao.sens.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <pre>
 *     操作日志
 * </pre>
 *
 * @author : saysky
 * @date : 2018/1/19
 */
@Data
@TableName("sens_log")
public class Log implements Serializable {

    private static final long serialVersionUID = -2571815432301283171L;

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long logId;

    /**
     * 标题
     */
    private String logTitle;

    /**
     * 内容
     */
    private String logContent;

    /**
     * 产生日志的ip
     */
    private String logIp;

    /**
     * 产生的时间
     */
    private Date logCreated;

    public Log() {
    }

    public Log(String logTitle, String logContent, String logIp, Date logCreated) {
        this.logTitle = logTitle;
        this.logContent = logContent;
        this.logIp = logIp;
        this.logCreated = logCreated;
    }
}
