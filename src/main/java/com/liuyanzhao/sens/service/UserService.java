package com.liuyanzhao.sens.service;

import com.baomidou.mybatisplus.plugins.Page;
import com.liuyanzhao.sens.entity.User;
import com.liuyanzhao.sens.utils.Response;

import java.util.Date;

/**
 * <pre>
 *     用户业务逻辑接口
 * </pre>
 *
 * @author : saysky
 * @date : 2017/11/14
 */
public interface UserService {

    /**
     * 新增/修改用户
     *
     * @param user user
     * @return Role
     */
    Response<User> saveByUser(User user);
    /**
     * 根据用户名获得用户
     *
     * @param userName 用户名
     * @return 用户
     */
    User findByUserName(String userName);


    /**
     * 根据邮箱查找用户
     *
     * @param userEmail 邮箱
     * @return User
     */
    User findByEmail(String userEmail);

    /**
     * 根据用户Id获得用户
     *
     * @param userId 用户名
     * @return 用户
     */
    User findByUserId(Long userId);

    /**
     * 更新密码
     *
     * @param userId 用户Id
     * @param password 密码
     */
    void updatePassword(Long userId, String password);


    /**
     * 添加用户
     *
     * @param user 用户
     */
    void insertUser(User user);

    /**
     * 分页获取所有用户
     *
     * @param page 分页信息
     * @return 用户列表
     */
    Page<User> findAllUser(Page<User> page);

    /**
     * 分页获取所有用户
     *
     * @param roleName 角色名称
     * @param page 分页信息
     * @return 用户列表
     */
    Page<User> findAllUserByRole(String roleName, Page<User> page);

    /**
     * 根据用户编号和密码查询
     *
     * @param userId   userid
     * @param userPass userpass
     * @return User
     */
    User findByUserIdAndUserPass(Long userId, String userPass);


    /**
     * 修改禁用状态
     *
     * @param enable enable
     */
    void updateUserLoginEnable(User user, String enable);

    /**
     * 修改最后登录时间
     *
     * @param lastDate 最后登录时间
     * @return User
     */
    User updateUserLoginLast(User user, Date lastDate);

    /**
     * 增加登录错误次数
     *
     * @return 登录错误次数
     */
    Integer updateUserLoginError(User user);

    /**
     * 修改用户的登录状态为正常
     *
     * @return User
     */
    User updateUserLoginNormal(User user);

    /**
     * 删除用户
     *
     * @param userId 用户Id
     */
    void removeUser(Long userId);

    /**
     * 更新用户状态
     *
     * @param userId 用户Id
     * @param status 状态
     */
    void updateUserStatus(Long userId, Integer status);


}
