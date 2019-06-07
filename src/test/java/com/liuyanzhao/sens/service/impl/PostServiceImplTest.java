package com.liuyanzhao.sens.service.impl;

import com.liuyanzhao.sens.service.PostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * @author 言曌
 * @date 2019/2/6 下午2:42
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class PostServiceImplTest {

    @Autowired
    private PostService postService;

    @Test
    public void updateAllSummary() throws Exception {
        postService.updateAllSummary(120);

    }

}