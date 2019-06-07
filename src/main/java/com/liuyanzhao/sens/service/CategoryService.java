package com.liuyanzhao.sens.service;

import com.liuyanzhao.sens.entity.Category;

import java.util.List;

/**
 * <pre>
 *     分类业务逻辑接口
 * </pre>
 *
 * @author : saysky
 * @date : 2017/11/30
 */
public interface CategoryService {

    /**
     * 新增/修改分类目录
     *
     * @param category 分类目录
     * @return 如果插入成功，返回分类目录对象
     */
    Category saveByCategory(Category category);

    /**
     * 根据编号删除分类目录
     *
     * @param cateId 分类目录编号
     * @return category
     */
    void removeByCateId(Long cateId);

    /**
     * 获取所有分类目录,根据level封装name
     *
     * @return 返回List集合
     */
    List<Category> findAllCategories();

    /**
     * 查询所有分类目录,带count和根据level封装name
     *
     * @return 返回List集合
     */
    List<Category> findAllCategoriesWithLevel();

    /**
     * 根据编号查询单个分类
     *
     * @param cateId 分类编号
     * @return 返回category实体
     */
    Category findByCateId(Long cateId);

    /**
     * 根据分类目录路径查询，用于验证是否已经存在该路径
     *
     * @param cateUrl cateUrl
     * @return category
     */
    Category findByCateUrl(String cateUrl);

    /**
     * 将分类字符串集合转化为Category泛型集合
     *
     * @param strings strings
     * @return List
     */
    List<Category> strListToCateList(List<String> strings);

    /**
     * 根据文章Id获得分类列表
     *
     * @param postId 文章id
     * @return 分类列表
     */
    List<Category> findByPostId(Long postId);

    /**
     * 查询数量
     *
     * @return 分类数量
     */
    Integer getSumCount();

    /**
     * 获得某个分类的所有文章数
     *
     * @param cateId 分类Id
     * @return 文章数
     */
    Integer countPostByCateId(Long cateId);


    /**
     * 获得某个分类的子分类Id
     *
     * @param cateId 分类Id
     * @return 子分类Id
     */
    List<Long> selectChildCateId(Long cateId);
}
