package com.liuyanzhao.sens.service;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.liuyanzhao.sens.entity.Gallery;

import java.util.List;

/**
 * <pre>
 *     图库业务逻辑接口
 * </pre>
 *
 * @author : saysky
 * @date : 2018/2/26
 */
public interface GalleryService {

    /**
     * 保存图片
     *
     * @param gallery gallery
     * @return Gallery
     */
    Gallery saveByGallery(Gallery gallery);

    /**
     * 根据编号删除图片
     *
     * @param galleryId galleryId
     * @return Gallery
     */
    void removeByGalleryId(Long galleryId);

    /**
     * 修改图片信息
     *
     * @param gallery gallery
     * @return Gallery
     */
    Gallery updateByGallery(Gallery gallery);

    /**
     * 查询所有图片 分页
     *
     * @param page 分页
     * @return Page
     */
    Page<Gallery> findAllGalleries(Page<Gallery> page);

    /**
     * 查询所有图片 不分页
     *
     * @return List
     */
    List<Gallery> findAllGalleries();

    /**
     * 根据编号查询图片信息
     *
     * @param galleryId galleryId
     * @return Gallery
     */
    Gallery findByGalleryId(Long galleryId);
}
