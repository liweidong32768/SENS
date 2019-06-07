package com.liuyanzhao.sens.model.dto;

/**
 * @author 言曌
 * @date 2018/12/24 下午5:28
 */

public class PostViewsDto {

    /**
     * 文章Id
     */
    private Long postId;

    /**
     * 文章访问量
     */
    private Long postViews;

    public PostViewsDto(Long postId, Long postViews) {
        this.postId = postId;
        this.postViews = postViews;
    }

    public PostViewsDto() {
    }
}
