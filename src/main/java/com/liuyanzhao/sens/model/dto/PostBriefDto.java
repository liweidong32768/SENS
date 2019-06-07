package com.liuyanzhao.sens.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 言曌
 * @date 2018/12/16 下午3:46
 */
@Data
public class PostBriefDto implements Serializable{

    private static final long serialVersionUID = -8041345763938068216L;
    /**
     * 文章id
     */
    private Long postId;

    /**
     * 文章标题
     */
    private String postTitle;

    /**
     * 文章url
     */
    private String postUrl;

    /**
     * 发表日期
     */
    private Date postDate;
}
