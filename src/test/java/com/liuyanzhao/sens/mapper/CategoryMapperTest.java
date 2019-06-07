package com.liuyanzhao.sens.mapper;

import com.liuyanzhao.sens.entity.Category;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author 言曌
 * @date 2019/4/5 上午11:30
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class CategoryMapperTest {

    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    public void findByUserId() throws Exception {
//        List<Category> categoryList = categoryMapper.findByUserId(1L);
//        System.out.println(categoryList);
    }

}