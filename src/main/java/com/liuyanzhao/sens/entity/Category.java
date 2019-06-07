package com.liuyanzhao.sens.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * <pre>
 *     文章分类
 * </pre>
 *
 * @author : saysky
 * @date : 2017/11/30
 */
@Data
@TableName("sens_category")
public class Category implements Serializable {

    private static final long serialVersionUID = 8383678847517271505L;

    /**
     * 分类编号
     */
    @TableId(type = IdType.AUTO)
    private Long cateId;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String cateName;

    /**
     * 分类路径
     */
    @NotBlank(message = "分类路径不能为空")
    private String cateUrl;

    /**
     * 分类父节点
     */
    private Long catePid;

    /**
     * 分类排序号
     */
    private Integer cateSort;

    /**
     * 分类层级
     */
    private Integer cateLevel = 1;

    /**
     * 关系路径
     */
    private String pathTrace;

    /**
     * 分类描述
     */
    private String cateDesc;

    /**
     * 数量
     */
    @TableField(exist = false)
    private Integer count;

}
