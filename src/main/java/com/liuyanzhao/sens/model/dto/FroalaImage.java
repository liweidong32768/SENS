package com.liuyanzhao.sens.model.dto;

import lombok.Data;

/**
 * @author 言曌
 * @date 2019/1/12 下午3:29
 */

@Data
public class FroalaImage {

    private String url;

    private String thumb;

    private String tag;

    public FroalaImage(String url, String thumb, String tag) {
        this.url = url;
        this.thumb = thumb;
        this.tag = tag;
    }

    public FroalaImage() {
    }
}
