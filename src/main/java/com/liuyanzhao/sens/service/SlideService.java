package com.liuyanzhao.sens.service;

import com.liuyanzhao.sens.entity.Slide;

import java.util.List;

/**
 * <pre>
 *     菜单业务逻辑接口
 * </pre>
 *
 * @author : saysky
 * @date : 2018/1/24
 */
public interface SlideService {


    /**
     * 根据编号查询菜单
     *
     * @param slideId slideId
     * @return Optional
     */
    Slide findBySlideId(Long slideId);

    /**
     * 根据类型查询，以树形展示，用于前台
     *
     * @param slideType 菜单类型
     * @return List
     */
    List<Slide> findBySlideType(Integer slideType);


    /**
     * 新增/修改菜单
     *
     * @param slide slide
     * @return Slide
     */
    Slide saveBySlide(Slide slide);

    /**
     * 删除菜单
     *
     * @param slideId slideId
     * @return Slide
     */
    void removeBySlideId(Long slideId);
}
