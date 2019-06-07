package com.liuyanzhao.sens.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.liuyanzhao.sens.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author liuyanzhao
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 查询所有
     *
     * @return 用户列表
     */
    List<User> findAll(Pagination page);

    /**
     * 根据角色Id获得用户
     *
     * @param roleId 角色Id
     * @param pagination 分页信息
     * @return 用户列表
     */
    List<User> findByRoleId(@Param("roleId") Integer roleId, Pagination pagination);

    /**
     * 获得没有角色的用户
     *
     * @param pagination 角色
     * @return 用户列表
     */
    List<User> findByWithoutRole(Pagination pagination);
    /**
     * 根据用户名获得用户
     *
     * @param userName 用户名
     * @return 用户
     */
    User findByUserName(String userName);

    /**
     * 根据用户邮箱获得用户
     *
     * @param userEmail 邮箱
     * @return 用户
     */
    User findByUserEmail(String userEmail);

    /**
     * 根据用户Id获得用户
     * @param userId 用户Id
     * @return 用户
     */
    User findByUserId(Long userId);

    /**
     * 根据用户名和密码查询
     *
     * @param userName userName
     * @param userPass userPass
     * @return User
     */
    User findByUserNameAndUserPass(@Param("userName") String userName,
                                   @Param("userPass") String userPass);

    /**
     * 根据邮箱和密码查询
     *
     * @param userEmail userEmail
     * @param userPass  userPass
     * @return User
     */
    User findByUserEmailAndUserPass(@Param("userEmail") String userEmail,
                                    @Param("userPass") String userPass);

    /**
     * 根据用户编号和密码查询
     *
     * @param userId   userId
     * @param userPass userPass
     * @return User
     */
    User findByUserIdAndUserPass(@Param("userId") Long userId,
                                 @Param("userPass") String userPass);


}

