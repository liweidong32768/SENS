package com.liuyanzhao.sens.service.impl;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.liuyanzhao.sens.entity.Gallery;
import com.liuyanzhao.sens.mapper.GalleryMapper;
import com.liuyanzhao.sens.service.GalleryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <pre>
 *     图库业务逻辑实现类
 * </pre>
 *
 * @author : saysky
 * @date : 2018/2/26
 */
@Service
public class GalleryServiceImpl implements GalleryService {

    private static final String GALLERIES_CACHE_NAME = "galleries";

    @Autowired(required = false)
    private GalleryMapper galleryMapper;

    /**
     * 保存图片
     *
     * @param gallery gallery
     * @return Gallery
     */
    @Override
    @CacheEvict(value = GALLERIES_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Gallery saveByGallery(Gallery gallery) {
        if (gallery != null && gallery.getGalleryId() != null) {
            galleryMapper.updateById(gallery);
        } else {
            galleryMapper.insert(gallery);
        }
        return gallery;
    }

    /**
     * 根据编号删除图片
     *
     * @param galleryId galleryId
     * @return Gallery
     */
    @Override
    @CacheEvict(value = GALLERIES_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void removeByGalleryId(Long galleryId) {
        galleryMapper.deleteById(galleryId);
    }

    /**
     * 修改图片信息
     *
     * @param gallery gallery
     * @return Gallery
     */
    @Override
    @CacheEvict(value = GALLERIES_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Gallery updateByGallery(Gallery gallery) {
        galleryMapper.updateById(gallery);
        return gallery;
    }

    /**
     * 查询所有图片 分页
     *
     * @param page pagination
     * @return Page
     */
    @Override
    public Page<Gallery> findAllGalleries(Page<Gallery> page) {
        return page.setRecords(galleryMapper.findAllByPage(page));
    }

    /**
     * 查询所有图片 不分页
     *
     * @return List
     */
    @Override
    @Cacheable(value = GALLERIES_CACHE_NAME, key = "'gallery'")
    public List<Gallery> findAllGalleries() {
        return galleryMapper.findAll();
    }

    /**
     * 根据编号查询图片信息
     *
     * @param galleryId galleryId
     * @return Optional
     */
    @Override
    @Cacheable(value = GALLERIES_CACHE_NAME, key = "'gallery_id+'+#galleryId")
    public Gallery findByGalleryId(Long galleryId) {
        return galleryMapper.selectById(galleryId);
    }
}
