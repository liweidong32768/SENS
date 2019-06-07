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
@TableName("sens_slide")
public class Slide implements Serializable {

    private static final long serialVersionUID = -7726233157376388786L;

    /**
     * 编号 自增
     */
    @TableId(type = IdType.AUTO)
    private Long slideId;


    /**
     * 幻灯片名称
     */
    private String slideTitle;

    /**
     * 幻灯片链接
     */
    private String slideUrl;

    /**
     * 幻灯片图片地址
     */
    private String slidePictureUrl;

    /**
     * 排序编号
     */
    private Integer slideSort = 1;

    /**
     * 打开方式
     */
    private String slideTarget;

    /**
     * 幻灯片类型(首页幻灯片0)
     */
    private Integer slideType = 0;


}
