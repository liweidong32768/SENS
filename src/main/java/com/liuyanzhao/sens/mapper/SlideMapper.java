package com.liuyanzhao.sens.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.liuyanzhao.sens.entity.Slide;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author liuyanzhao
 */
@Mapper
public interface SlideMapper extends BaseMapper<Slide> {

    /**
     * 根据类型查询
     * @param slideType 幻灯片类型

     * @return List
     */
    List<Slide> findBySlideType(Integer slideType);

}

