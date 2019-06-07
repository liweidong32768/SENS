package com.liuyanzhao.sens.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.liuyanzhao.sens.entity.Gallery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author liuyanzhao
 */
@Mapper
public interface GalleryMapper extends BaseMapper<Gallery> {

    /**
     * 查询所有
     *
     * @Param pagination 分页信息
     * @return Page
     */
    List<Gallery> findAllByPage(Pagination pagination);


    /**
     * 查询所有
     *
     * @return
     */
    List<Gallery> findAll();
}

