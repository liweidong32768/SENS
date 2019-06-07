package com.liuyanzhao.sens.service.impl;

import com.liuyanzhao.sens.entity.Widget;
import com.liuyanzhao.sens.mapper.WidgetMapper;
import com.liuyanzhao.sens.service.WidgetService;
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
public class WidgetServiceImpl implements WidgetService {

    private static final String WIDGETS_CACHE_NAME = "widgets";

    @Autowired(required = false)
    private WidgetMapper widgetMapper;

    @Override
    @Cacheable(value = WIDGETS_CACHE_NAME, key = "'widgets_id_'+#widgetId", unless = "#result == null")
    public Widget findByWidgetId(Long widgetId) {
        return widgetMapper.selectById(widgetId);
    }


    @Override
    @Cacheable(value = WIDGETS_CACHE_NAME, key = "'widgets_type_'+#widgetType")
    public List<Widget> findByWidgetType(Integer widgetType) {
        return widgetMapper.findByWidgetType(widgetType);
    }

    @Override
    @CacheEvict(value = WIDGETS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void removeByWidgetId(Long widgetId) {
        widgetMapper.deleteById(widgetId);
    }

    @Override
    @CacheEvict(value = WIDGETS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Widget saveByWidget(Widget widget) {
        //1.添加/更新小工具
        if (widget != null && widget.getWidgetId() != null) {
            widgetMapper.updateById(widget);
        } else {
            widgetMapper.insert(widget);
        }
        return widget;
    }
}
