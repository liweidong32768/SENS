package com.liuyanzhao.sens.service;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.liuyanzhao.sens.entity.Log;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <pre>
 *     日志业务逻辑接口
 * </pre>
 *
 * @author : saysky
 * @date : 2018/1/19
 */
public interface LogService {

    /**
     * 保存日志
     *
     * @param log log
     * @return Log
     */
    Log saveByLog(Log log);


    /**
     * 保存日志
     *
     * @param logTitle   logTitle
     * @param logContent logContent
     * @param request    request
     */
    void saveByLog(String logTitle, String logContent, HttpServletRequest request);


    /**
     * 根据编号移除
     *
     * @param logId logId
     */
    void removeByLogId(Long logId);

    /**
     * 移除所有日志
     */
    void removeAllLog();

    /**
     * 查询所有日志并分页
     *
     * @param page 分页信息
     * @return Page
     */
    Page<Log> findAllLog(Page<Log> page);

    /**
     * 查询最新的五条日志
     *
     * @return List
     */
    List<Log> findLogLatest();

    /**
     * 根据编号查询
     *
     * @param logId logId
     * @return Optional
     */
    Log findLogByLogId(Long logId);


}
