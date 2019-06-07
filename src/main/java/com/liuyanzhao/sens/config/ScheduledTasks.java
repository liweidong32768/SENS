package com.liuyanzhao.sens.config;

import com.liuyanzhao.sens.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 * @author 言曌
 * @date 2018/12/24 下午4:55
 */
@Component
@Slf4j
public class ScheduledTasks {

    @Autowired
    private PostService postService;

    /**
     * 同步文章访问量
     * 每天凌晨4:30执行
     */
    @Scheduled(cron = "0 30 4 ? * * ")
    public void syncPostViews() {
        postService.syncAllPostView();
    }

}
