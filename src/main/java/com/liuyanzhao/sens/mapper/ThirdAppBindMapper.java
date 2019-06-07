package com.liuyanzhao.sens.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.liuyanzhao.sens.entity.ThirdAppBind;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author liuyanzhao
 */
@Mapper
public interface ThirdAppBindMapper extends BaseMapper<ThirdAppBind> {

    /**
     * 查询所有
     *
     * @return
     */
    List<ThirdAppBind> findAll();

    /**
     * 根据OpenId和类型查询
     *
     * @param appType 类型
     * @param openId openId
     * @return
     */
    ThirdAppBind findByAppTypeAndOpenId(@Param("appType") String appType,
                                        @Param("openId") String openId);

    /**
     * 根据用户Id获得绑定记录
     * @param userId 用户Id
     * @return 列表
     */
    List<ThirdAppBind> findByUserId(Long userId);
}

