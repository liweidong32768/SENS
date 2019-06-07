package com.liuyanzhao.sens.service.impl;

import cn.hutool.core.lang.Validator;
import com.baomidou.mybatisplus.plugins.Page;
import com.liuyanzhao.sens.entity.Role;
import com.liuyanzhao.sens.mapper.UserMapper;
import com.liuyanzhao.sens.entity.User;
import com.liuyanzhao.sens.model.enums.RoleEnum;
import com.liuyanzhao.sens.model.enums.TrueFalseEnum;
import com.liuyanzhao.sens.model.enums.UserStatusEnum;
import com.liuyanzhao.sens.service.CommentService;
import com.liuyanzhao.sens.service.PostService;
import com.liuyanzhao.sens.service.RoleService;
import com.liuyanzhao.sens.service.UserService;
import com.liuyanzhao.sens.utils.LocaleMessageUtil;
import com.liuyanzhao.sens.utils.Md5Util;
import com.liuyanzhao.sens.utils.Response;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * <pre>
 *     用户业务逻辑实现类
 * </pre>
 *
 * @author : saysky
 * @date : 2017/11/14
 */
@Service
public class UserServiceImpl implements UserService {

    private static final String USERS_CACHE_NAME = "users";

    private static final String ROLES_COUNT_CACHE_NAME = "roles_count";


    @Autowired(required = false)
    private UserMapper userMapper;


    @Autowired(required = false)
    private RoleService roleService;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LocaleMessageUtil localeMessageUtil;

    @Override
    @CacheEvict(value = {USERS_CACHE_NAME, ROLES_COUNT_CACHE_NAME}, allEntries = true, beforeInvocation = true)
    public Response<User> saveByUser(User user) {
        //1.验证表单数据是否合法
        Response<User> response = validateUser(user);
        if (!response.isSuccess()) {
            return response;
        }

        //2.修改
        if (user != null && user.getUserId() != null) {
            userMapper.updateById(user);
        } else {
            //web层校验用户名或邮箱是否存在
            user.setCreateTime(new Date());
            if (!Strings.isEmpty(user.getUserPass())) {
                user.setUserPass(Md5Util.toMd5(user.getUserPass(), "sens", 10));
            } else {
                user.setUserPass(Md5Util.toMd5("123456", "sens", 10));
            }
            userMapper.insert(user);
        }
        return Response.yes(user);
    }

    private Response<User> validateUser(User user) {
        if (user == null) {
            return Response.no("用户为空");
        }
        //注册时
        if (user.getUserId() == null) {
            if (user.getUserName() == null || user.getUserPass() == null || user.getUserEmail() == null) {
                return Response.no("请输入完整信息!");
            }
            String userName = user.getUserName();
            userName = userName.trim().replaceAll(" ", "-");
            if (userName.length() < 4 || userName.length() > 20) {
                return Response.no(localeMessageUtil.getMessage("code.user.validate.username-invalid"));
            }
            if (user.getUserPass().length() < 6 || user.getUserPass().length() > 20) {
                return Response.no(localeMessageUtil.getMessage("code.user.validate.password-invalid"));
            }
            user.setUserName(userName);
            if (!Validator.isEmail(user.getUserEmail())) {
                return Response.no("电子邮箱格式不合法!");
            }
        }
        //修改资料时
        else {
            if (user.getUserName() == null || user.getUserDisplayName() == null || user.getUserEmail() == null) {
                return Response.no("请输入完整信息!");
            }
            String userName = user.getUserName();
            userName = userName.trim().replaceAll(" ", "-");
            if (userName.length() < 4 || userName.length() > 20) {
                return Response.no(localeMessageUtil.getMessage("code.user.validate.username-invalid"));
            }
            if (user.getUserDisplayName().length() < 1 || user.getUserDisplayName().length() > 20) {
                return Response.no(localeMessageUtil.getMessage("code.user.validate.display-name-invalid"));
            }
            user.setUserName(userName);
            if (!Validator.isEmail(user.getUserEmail())) {
                return Response.no(localeMessageUtil.getMessage("code.user.validate.email-invalid"));
            }
        }
        return Response.yes();
    }

    @Override
    @Cacheable(value = USERS_CACHE_NAME, key = "'users_name_'+#userName", unless = "#result == null")
    public User findByUserName(String userName) {
        return userMapper.findByUserName(userName);
    }

    @Override
    @Cacheable(value = USERS_CACHE_NAME, key = "'users_email_'+#userEmail", unless = "#result == null")
    public User findByEmail(String userEmail) {
        return userMapper.findByUserEmail(userEmail);
    }

    @Override
    @Cacheable(value = USERS_CACHE_NAME, key = "'users_id_'+#userId", unless = "#result == null")
    public User findByUserId(Long userId) {
        return userMapper.findByUserId(userId);
    }


    @Override
    @CacheEvict(value = USERS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void updatePassword(Long userId, String password) {
        User user = new User();
        user.setUserId(userId);
        user.setUserPass(password);
        userMapper.updateById(user);
    }

    @Override
    @CacheEvict(value = USERS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void insertUser(User user) {
        userMapper.insert(user);
    }

    @Override
    @Cacheable(value = USERS_CACHE_NAME, key = "'users_page_'+#page.current")
    public Page<User> findAllUser(Page<User> page) {
        return page.setRecords(userMapper.findAll(page));
    }

    @Override
    public Page<User> findAllUserByRole(String roleName, Page<User> page) {
        Role role = roleService.findByRoleName(roleName);
        List<User> users;
        if (role != null && !Objects.equals(roleName, RoleEnum.NONE.getDesc())) {
            users = userMapper.findByRoleId(role.getId(), page);
        } else {
            users = userMapper.findByWithoutRole(page);
        }
        //封装文章数
        users.forEach(user -> {
            user.setPostCount(postService.countByUserId(user.getUserId()));
            user.setCommentCount(commentService.countByUserId(user.getUserId()));
        });
        //封装评论数
        return page.setRecords(users);
    }


    @Override
    public User findByUserIdAndUserPass(Long userId, String userPass) {
        return userMapper.findByUserIdAndUserPass(userId, userPass);
    }


    /**
     * 修改禁用状态
     *
     * @param enable enable
     */
    @Override
    @CacheEvict(value = USERS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void updateUserLoginEnable(User user, String enable) {
        //如果是修改为正常, 重置错误次数
        if (Objects.equals(TrueFalseEnum.TRUE.getDesc(), enable)) {
            user.setLoginError(0);
        }
        user.setLoginEnable(enable);
        user.setLoginLast(new Date());
        userMapper.updateById(user);
    }

    /**
     * 修改最后登录时间
     *
     * @param lastDate 最后登录时间
     * @return User
     */
    @Override
    @CacheEvict(value = USERS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public User updateUserLoginLast(User user, Date lastDate) {
        user.setLoginLast(lastDate);
        userMapper.updateById(user);
        return user;
    }

    /**
     * 增加登录错误次数
     *
     * @return 登录错误次数
     */
    @Override
    @CacheEvict(value = USERS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Integer updateUserLoginError(User user) {
        user.setLoginError((user.getLoginError() == null ? 0 : user.getLoginError()) + 1);
        userMapper.updateById(user);
        return user.getLoginError();
    }

    /**
     * 修改用户的状态为正常
     *
     * @return User
     */
    @Override
    @CacheEvict(value = USERS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public User updateUserLoginNormal(User user) {
        user.setLoginEnable(TrueFalseEnum.TRUE.getDesc());
        user.setLoginError(0);
        user.setLoginLast(new Date());
        userMapper.updateById(user);
        return user;
    }

    @Override
    @CacheEvict(value = {USERS_CACHE_NAME, ROLES_COUNT_CACHE_NAME}, allEntries = true, beforeInvocation = true)
    @Transactional(rollbackFor = Exception.class)
    public void removeUser(Long userId) {
        //删除用户
        User user = findByUserId(userId);
        if (user != null) {
            //1.修改用户状态为已删除
            user.setStatus(UserStatusEnum.DELETED.getCode());
            user.setUserName("deleted-" + System.currentTimeMillis() + user.getUserName());
            user.setUserDisplayName("已删除-" + user.getUserDisplayName());
            user.setUserEmail("deleted-" + user.getUserEmail());
            userMapper.updateById(user);
            //2.修改用户和角色关联
            roleService.removeByUserId(userId);
            //3.文章删除
            postService.removeByUserId(userId);
            //4.评论删除
            commentService.removeByUserId(userId);
        }
    }

    @Override
    @CacheEvict(value = USERS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void updateUserStatus(Long userId, Integer status) {
        User user = findByUserId(userId);
        if (user != null) {
            user.setStatus(status);
            userMapper.updateById(user);
        }
    }

}
