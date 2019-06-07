package com.liuyanzhao.sens.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.liuyanzhao.sens.entity.Attachment;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

/**
 * @author liuyanzhao
 */
@Mapper
public interface AttachmentMapper extends BaseMapper<Attachment> {

    /**
     * 查询所有，分页
     *
     * @param page 分页信息
     * @return
     */
    List<Attachment> findAllByPage(HashMap<String, Object> criteria, Pagination page);


    /**
     * 查询所有，不分页
     *
     * @return List
     */
    List<Attachment> findAll();

}

