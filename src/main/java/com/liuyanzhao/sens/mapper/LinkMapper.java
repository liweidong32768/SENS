package com.liuyanzhao.sens.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.liuyanzhao.sens.entity.Link;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author liuyanzhao
 */
@Mapper
public interface LinkMapper extends BaseMapper<Link> {

    /**
     * 查询所有
     * @return
     */
    List<Link> findAll();

}

