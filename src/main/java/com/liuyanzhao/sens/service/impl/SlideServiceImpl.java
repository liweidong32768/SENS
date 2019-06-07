package com.liuyanzhao.sens.service.impl;

import com.liuyanzhao.sens.entity.Slide;
import com.liuyanzhao.sens.mapper.SlideMapper;
import com.liuyanzhao.sens.service.SlideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <pre>
 *     幻灯片业务逻辑实现类
 * </pre>
 *
 * @author : saysky
 * @date : 2018/1/24
 */
@Service
public class SlideServiceImpl implements SlideService {

    private static final String SLIDES_CACHE_NAME = "slides";

    @Autowired(required = false)
    private SlideMapper slideMapper;

    @Override
    @Cacheable(value = SLIDES_CACHE_NAME, key = "'slides_id_'+#slideId", unless="#result == null")
    public Slide findBySlideId(Long slideId) {
        return slideMapper.selectById(slideId);
    }

    @Override
    @Cacheable(value = SLIDES_CACHE_NAME, key = "'slides_type_'+#slideType")
    public List<Slide> findBySlideType(Integer slideType) {
        return slideMapper.findBySlideType(slideType);
    }

    @Override
    @CacheEvict(value = SLIDES_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void removeBySlideId(Long slideId) {
        slideMapper.deleteById(slideId);
    }

    @Override
    @CacheEvict(value = SLIDES_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Slide saveBySlide(Slide slide) {
        //1.添加/更新菜单
        if (slide != null && slide.getSlideId() != null) {
            slideMapper.updateById(slide);
        } else {
            slideMapper.insert(slide);
        }
        return slide;
    }
}
