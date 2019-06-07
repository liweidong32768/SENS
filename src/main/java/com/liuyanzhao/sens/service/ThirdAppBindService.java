package com.liuyanzhao.sens.service;

import com.liuyanzhao.sens.entity.ThirdAppBind;

import java.util.List;

/**
 * <pre>
 *     第三方应用业务逻辑接口
 * </pre>
 *
 * @author : saysky
 * @date : 2017/11/14
 */
public interface ThirdAppBindService {

    /**
     * 新增/修改关联
     *
     * @param link link
     * @return ThirdAppBind
     */
    ThirdAppBind saveByThirdAppBind(ThirdAppBind link);

    /**
     * 根据编号删除
     *
     * @param id id
     */
    void removeById(Long id);

    /**
     * 根据应用类型和OpenId查询
     *
     * @param appType 应用类型
     * @param openId OpenId
     * @return 关联
     */
    ThirdAppBind findByAppTypeAndOpenId(String appType, String openId);


    /**
     * 根据编号查询单个绑定
     *
     * @param bindId bindId
     * @return ThirdAppBind
     */
    ThirdAppBind findByThirdAppBindId(Long bindId);

    /**
     * 获得某个用户的所有绑定
     *
     * @param userId 用户Id
     * @return 绑定列表
     */
    List<ThirdAppBind> findByUserId(Long userId);

}
