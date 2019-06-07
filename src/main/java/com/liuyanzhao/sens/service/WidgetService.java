package com.liuyanzhao.sens.service;

import com.liuyanzhao.sens.entity.Widget;

import java.util.List;

/**
 * <pre>
 *     小工具业务逻辑接口
 * </pre>
 *
 * @author : saysky
 * @date : 2018/1/24
 */
public interface WidgetService {


    /**
     * 根据编号查询小工具
     *
     * @param widgetId widgetId
     * @return Optional
     */
    Widget findByWidgetId(Long widgetId);

    /**
     * 根据类型查询，以树形展示，用于前台
     *
     * @param widgetType 小工具类型
     * @return List
     */
    List<Widget> findByWidgetType(Integer widgetType);


    /**
     * 新增/修改小工具
     *
     * @param widget widget
     * @return Widget
     */
    Widget saveByWidget(Widget widget);

    /**
     * 删除小工具
     *
     * @param widgetId widgetId
     * @return Widget
     */
    void removeByWidgetId(Long widgetId);
}
