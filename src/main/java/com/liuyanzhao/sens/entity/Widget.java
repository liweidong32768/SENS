package com.liuyanzhao.sens.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.Data;

import java.io.Serializable;

/**
 * <pre>
 *     幻灯片
 * </pre>
 *
 * @author : saysky
 * @date : 2018/1/24
 */
@Data
@TableName("sens_widget")
public class Widget implements Serializable {

    private static final long serialVersionUID = -7726233157376388786L;

    /**
     * 编号 自增
     */
    @TableId(type = IdType.AUTO)
    private Long widgetId;

    /**
     * 小工具标题
     */
    private String widgetTitle;

    /**
     * 小工具内容
     */
    private String widgetContent;

    /**
     * 是否显示(1是，0否)
     */
    private Integer isDisplay = 1;

    /**
     * 位置
     */
    private Integer widgetType;


}
