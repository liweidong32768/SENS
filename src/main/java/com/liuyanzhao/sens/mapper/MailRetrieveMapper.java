package com.liuyanzhao.sens.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.liuyanzhao.sens.entity.MailRetrieve;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author liuyanzhao
 */
@Mapper
public interface MailRetrieveMapper extends BaseMapper<MailRetrieve> {

    /**
     * 查询所有
     *
     * @return
     */
    List<MailRetrieve> findAll();

    /**
     * 根据用户Id获得最新的一条
     *
     * @param userId 用户Id
     * @return 获得最新的一条记录
     */
    MailRetrieve findLatestByUserId(Long userId);

    /**
     * 根据邮箱获得最新的一条
     *
     * @param email 邮箱
     * @return 获得最新的一条记录
     */
    MailRetrieve findLatestByEmail(String email);

}

