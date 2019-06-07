package com.liuyanzhao.sens.service.impl;

import cn.hutool.extra.servlet.ServletUtil;
import com.baomidou.mybatisplus.plugins.Page;
import com.liuyanzhao.sens.entity.Log;
import com.liuyanzhao.sens.mapper.LogMapper;
import com.liuyanzhao.sens.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * <pre>
 *     日志业务逻辑实现类
 * </pre>
 *
 * @author : saysky
 * @date : 2018/1/19
 */
@Service
public class LogServiceImpl implements LogService {

    @Autowired(required = false)
    private LogMapper logMapper;


    /**
     * 保存日志
     *
     * @param log log
     * @return log
     */
    @Override
    public Log saveByLog(Log log) {
        logMapper.insert(log);
        return log;
    }

    /**
     * 保存日志
     *
     * @param logTitle   logTitle
     * @param logContent logContent
     * @param request    request
     */
    @Override
    public void saveByLog(String logTitle, String logContent, HttpServletRequest request) {
        final Log log = new Log();
        log.setLogTitle(logTitle);
        log.setLogContent(logContent);
        log.setLogCreated(new Date());
        log.setLogIp(ServletUtil.getClientIP(request));
        logMapper.insert(log);
    }

    /**
     * 根据编号移除
     *
     * @param logId logId
     */
    @Override
    public void removeByLogId(Long logId) {
        logMapper.deleteById(logId);
    }

    /**
     * 移除所有日志
     */
    @Override
    public void removeAllLog() {
        logMapper.deleteAll();
    }

    /**
     * 查询所有日志并分页
     *
     * @param page 分页信息
     * @return Page
     */
    @Override
    public Page<Log> findAllLog(Page<Log> page) {
        return page.setRecords(logMapper.findAll(page));
    }

    /**
     * 查询最新的五条日志
     *
     * @return List
     */
    @Override
    public List<Log> findLogLatest() {
        return logMapper.findTopFive();
    }

    /**
     * 根据编号查询
     *
     * @param logId logId
     * @return Optional
     */
    @Override
    public Log findLogByLogId(Long logId) {
        return logMapper.selectById(logId);
    }
}
