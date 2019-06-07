package com.liuyanzhao.sens.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.liuyanzhao.sens.entity.Widget;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author liuyanzhao
 */
@Mapper
public interface WidgetMapper extends BaseMapper<Widget> {

    /**
     * 根据类型查询
     *
     * @param widgetType 小工具类型
     * @return List
     */
    List<Widget> findByWidgetType(Integer widgetType);

}

