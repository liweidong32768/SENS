package com.liuyanzhao.sens.service.impl;

import com.liuyanzhao.sens.entity.Category;
import com.liuyanzhao.sens.entity.Category;
import com.liuyanzhao.sens.mapper.CategoryMapper;
import com.liuyanzhao.sens.mapper.PostCategoryRefMapper;
import com.liuyanzhao.sens.service.CategoryService;
import com.liuyanzhao.sens.utils.CategoryUtil;
import com.liuyanzhao.sens.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     分类业务逻辑实现类
 * </pre>
 *
 * @author : saysky
 * @date : 2017/11/30
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private static final String POSTS_CACHE_NAME = "posts";

    private static final String CATEGORIES_CACHE_NAME = "categories";

    @Autowired(required = false)
    private CategoryMapper categoryMapper;

    @Autowired(required = false)
    private PostCategoryRefMapper postCategoryRefMapper;

    /**
     * 保存/修改分类目录
     *
     * @param category 分类目录
     * @return Category
     */
    @Override
    @CacheEvict(value = {CATEGORIES_CACHE_NAME, POSTS_CACHE_NAME}, allEntries = true, beforeInvocation = true)
    public Category saveByCategory(Category category) {
        //1.设置CategoryLevel和pathTrace
        if (category.getCatePid() == 0 || category.getCatePid() == null) {
            category.setCateLevel(1);
            category.setPathTrace("/");
        } else {
            Category parentCategory = this.findByCateId(category.getCatePid());
            if (parentCategory != null && parentCategory.getCateLevel() != null) {
                category.setCateLevel(parentCategory.getCateLevel() + 1);
                category.setPathTrace(parentCategory.getPathTrace() + parentCategory.getCateId() + "/");
            }
        }
        //2.添加/更新分类
        if (category != null && category.getCateId() != null) {
            categoryMapper.updateById(category);
            //添加缓存
        } else {
            categoryMapper.insert(category);
        }
        return category;
    }

    /**
     * 根据编号移除分类目录
     *
     * @param cateId 分类目录编号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {CATEGORIES_CACHE_NAME, POSTS_CACHE_NAME}, allEntries = true, beforeInvocation = true)
    public void removeByCateId(Long cateId) {
        Category category = this.findByCateId(cateId);
        if (category != null) {
            //1.删除分类和文章的关联
            postCategoryRefMapper.deleteByCateId(cateId);
            //2.删除分类
            categoryMapper.deleteById(cateId);
        }
    }

    /**
     * 查询所有分类目录，不带level的
     *
     * @return List
     */
    @Override
    @Cacheable(value = CATEGORIES_CACHE_NAME, key = "'categories_all'")
    public List<Category> findAllCategories() {
        List<Category> categories = categoryMapper.findAll();
        return CategoryUtil.getCategoryList(categories);
    }

    /**
     * 查询所有分类目录,带count和根据level封装name
     *
     * @return List
     */
    @Override
    @Cacheable(value = CATEGORIES_CACHE_NAME, key = "'categories_all_count_level'")
    public List<Category> findAllCategoriesWithLevel() {
        List<Category> categories = categoryMapper.findAllWithCount();
        categories.forEach(category -> {
            String str = "";
            for (int i = 1; i < category.getCateLevel(); i++) {
                str += "——";
            }
            category.setCateName(str + category.getCateName());
        });
        return CategoryUtil.getCategoryList(categories);
    }

    /**
     * 根据编号查询分类目录
     *
     * @param cateId 分类编号
     * @return Category
     */
    @Override
    @Cacheable(value = CATEGORIES_CACHE_NAME, key = "'categories_id_'+#cateId", unless = "#result == null")
    public Category findByCateId(Long cateId) {
        return categoryMapper.selectById(cateId);
    }

    /**
     * 根据分类目录路径查询，用于验证是否已经存在该路径
     *
     * @param cateUrl cateUrl
     * @return Category
     */
    @Override
    @Cacheable(value = CATEGORIES_CACHE_NAME, key = "'categories_url_'+#cateUrl", unless = "#result == null")
    public Category findByCateUrl(String cateUrl) {
        //子分类
        return categoryMapper.findCategoryByCateUrl(cateUrl);
    }

    /**
     * 将分类字符串集合转化为Category泛型集合
     *
     * @param strings strings
     * @return List
     */
    @Override
    public List<Category> strListToCateList(List<String> strings) {
        if (null == strings) {
            return null;
        }
        List<Category> categories = new ArrayList<>();
        Category category = null;
        for (String str : strings) {
            category = findByCateId(Long.parseLong(str));
            categories.add(category);
        }
        return categories;
    }

    @Override
    @Cacheable(value = CATEGORIES_CACHE_NAME, key = "'categories_postid_'+#postId")
    public List<Category> findByPostId(Long postId) {
        return categoryMapper.findByPostId(postId);
    }

    @Override
    @Cacheable(value = CATEGORIES_CACHE_NAME, key = "'categories_count'")
    public Integer getSumCount() {
        return categoryMapper.selectCount(null);
    }

    @Override
    @Cacheable(value = CATEGORIES_CACHE_NAME, key = "'categories_count_id_'+#cateId")
    public Integer countPostByCateId(Long cateId) {
        return postCategoryRefMapper.countPostByCateId(cateId);
    }


    @Override
    @Cacheable(value = CATEGORIES_CACHE_NAME, key = "'categories_child_id_' + #cateId")
    public List<Long> selectChildCateId(Long cateId) {
        String pathTrace = "/" + cateId + "/";
        return categoryMapper.selectChildCateIds(pathTrace);
    }
}
