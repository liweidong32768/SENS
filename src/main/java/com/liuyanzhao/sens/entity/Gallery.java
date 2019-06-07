package com.liuyanzhao.sens.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.Data;

import java.io.Serializable;

/**
 * <pre>
 *     相册
 * </pre>
 *
 * @author : saysky
 * @date : 2018/2/26
 */
@Data
@TableName("sens_gallery")
public class Gallery implements Serializable {

    private static final long serialVersionUID = 1646093266970933841L;

    /**
     * 图片编号
     */
    @TableId(type = IdType.AUTO)
    private Long galleryId;

    /**
     * 图片名称
     */
    private String galleryName;

    /**
     * 图片描述
     */
    private String galleryDesc;

    /**
     * 图片日期/拍摄日期
     */
    private String galleryDate;

    /**
     * 图片拍摄地点
     */
    private String galleryLocation;

    /**
     * 图片缩略图地址
     */
    private String galleryThumbnailUrl;

    /**
     * 图片地址
     */
    private String galleryUrl;
}
