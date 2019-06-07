package com.liuyanzhao.sens.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.liuyanzhao.sens.entity.Log;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 日志Mapper
 * @author liuyanzhao
 */
@Mapper
public interface LogMapper extends BaseMapper<Log> {

    /**
     * 查询所有
     * @param pagination 分页信息
     * @return List
     */
    List<Log> findAll(Pagination pagination);

    /**
     * 查询最新的五条数据
     *
     * @return List
     */
    List<Log> findTopFive();

    /**
     * 删除所有的记录
     * @return 影响行数
     */
    Integer deleteAll();
}

