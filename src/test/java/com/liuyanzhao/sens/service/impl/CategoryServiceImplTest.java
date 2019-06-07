package com.liuyanzhao.sens.service.impl;

import com.liuyanzhao.sens.entity.Category;
import com.liuyanzhao.sens.service.CategoryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author 言曌
 * @date 2019/2/6 下午12:51
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class CategoryServiceImplTest {

    @Autowired
    private CategoryService categoryService;

    @Test
    public void findByPostId() throws Exception {
        List<Category> categoryList = categoryService.findByPostId(538L);
        System.out.println(categoryList);
    }

}