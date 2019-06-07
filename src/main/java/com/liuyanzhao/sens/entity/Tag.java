package com.liuyanzhao.sens.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.Data;

import java.io.Serializable;

/**
 * <pre>
 *     文章标签
 * </pre>
 *
 * @author : saysky
 * @date : 2018/1/12
 */
@Data
@TableName("sens_tag")
public class Tag implements Serializable {

    private static final long serialVersionUID = -7501342327884372194L;

    /**
     * 标签编号
     */
    @TableId(type = IdType.AUTO)
    private Long tagId;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 标签路径
     */
    private String tagUrl;

    /**
     * 数量
     */
    @TableField(exist = false)
    private Integer count;

}
