package com.liuyanzhao.sens.web.controller.admin;

import com.baomidou.mybatisplus.plugins.Page;
import com.liuyanzhao.sens.entity.Category;
import com.liuyanzhao.sens.entity.Role;
import com.liuyanzhao.sens.entity.User;
import com.liuyanzhao.sens.entity.UserRoleRef;
import com.liuyanzhao.sens.model.dto.SensConst;
import com.liuyanzhao.sens.model.dto.JsonResult;
import com.liuyanzhao.sens.model.dto.UserToken;
import com.liuyanzhao.sens.model.enums.LoginType;
import com.liuyanzhao.sens.model.enums.ResultCodeEnum;
import com.liuyanzhao.sens.model.enums.UserStatusEnum;
import com.liuyanzhao.sens.service.PostService;
import com.liuyanzhao.sens.service.RoleService;
import com.liuyanzhao.sens.service.UserRoleRefService;
import com.liuyanzhao.sens.service.UserService;
import com.liuyanzhao.sens.utils.LocaleMessageUtil;
import cn.hutool.crypto.SecureUtil;
import com.liuyanzhao.sens.utils.Md5Util;
import com.liuyanzhao.sens.utils.Response;
import com.liuyanzhao.sens.web.controller.core.BaseController;
import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * <pre>
 *     后台用户管理控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2017/12/24
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/user")
@RequiresPermissions("user:all")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRoleRefService userRoleRefService;

    @Autowired
    private PostService postService;

    @Autowired
    private LocaleMessageUtil localeMessageUtil;

    /**
     * 查询所有分类并渲染user页面
     *
     * @return 模板路径admin/admin_user
     */
    @GetMapping
    @RequiresPermissions("user:list")
    public String users(@RequestParam(value = "role", defaultValue = "admin") String role,
                        @RequestParam(value = "page", defaultValue = "0") Integer page,
                        @RequestParam(value = "size", defaultValue = "10") Integer size, Model model) {
        //用户列表
        Page pageable = new Page(page, size);
        Page<User> users = userService.findAllUserByRole(role, pageable);
        //角色列表
        List<Role> roles = roleService.findAll();
        model.addAttribute("users", users);
        model.addAttribute("roles", roles);
        model.addAttribute("currentRole", role);
        return "admin/admin_user";
    }


    /**
     * 删除用户
     *
     * @param userId 用户Id
     * @return JsonResult
     */
    @PostMapping(value = "/remove")
    @ResponseBody
    public JsonResult removeUser(@RequestParam("userId") Long userId) {
        try {
            //1.检查用户有没有文章
            Integer postCount = postService.countByUserId(userId);
            //2.检查用户有没有评论
            Integer commentCount = postService.countByUserId(userId);
            if (postCount > 0 || commentCount > 0) {
                Object[] args = {postCount, commentCount};
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.user.delete-failed", args));
            }
            userService.removeUser(userId);
        } catch (Exception e) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-failed"));
        }
        StringBuffer sb = new StringBuffer();
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-success"));
    }

    /**
     * 添加用户页面
     *
     * @return 模板路径admin/admin_edit
     */
    @GetMapping("/new")
    public String addUser(Model model) {
        List<Role> roles = roleService.findAll();
        model.addAttribute("roles", roles);
        return "admin/admin_user_add";
    }

    /**
     * 编辑用户页面
     *
     * @return 模板路径admin/admin_edit
     */
    @GetMapping("/edit")
    public String edit(@RequestParam("userId") Long userId, Model model) {
        User user = userService.findByUserId(userId);
        if (user != null) {
            model.addAttribute("user", user);
            //当前用户的角色
            List<Role> currentRoles = roleService.listRolesByUserId(userId);
            model.addAttribute("currentRoles", currentRoles);
            //角色列表
            List<Role> roles = roleService.findAll();
            model.addAttribute("roles", roles);
            return "admin/admin_user_edit";
        } else {
            return this.renderNotFound();
        }

    }


    /**
     * 新增/修改用户
     *
     * @param user user
     * @return 重定向到/admin/user
     */
    @PostMapping(value = "/save")
    @ResponseBody
    public JsonResult saveUser(@ModelAttribute User user,
                               @RequestParam("roleList") List<Integer> roleList) {
        try {
            //1.添加/更新用户名检查用户名是否存在
            User nameCheck = userService.findByUserName(user.getUserName());
            if ((user.getUserId() == null && nameCheck != null) || (user.getUserId() != null && !Objects.equals(nameCheck.getUserId(), user.getUserId()))) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.user.user-name-exist"));
            }
            //2.添加/更新用户名检查用户名是否存在
            User emailCheck = userService.findByEmail(user.getUserEmail());
            if ((user.getUserId() == null && emailCheck != null) || (user.getUserId() != null && !Objects.equals(emailCheck.getUserId(), user.getUserId()))) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.user.user-email-exist"));
            }

            if (roleList == null || roleList.size() == 0) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.user.need-choose-role"));
            }

            //3.添加用户
            Response<User> response = userService.saveByUser(user);
            if (!response.isSuccess()) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), response.getMessage());
            }

            //4.先删除该用户的角色关联
            userRoleRefService.deleteByUserId(user.getUserId());

            //5.关联角色
            Long userId = user.getUserId();
            for (Integer roleId : roleList) {
                userRoleRefService.saveByUserRoleRef(new UserRoleRef(userId, roleId));
            }
        } catch (Exception e) {
            log.error("新增/修改用户失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.save-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.save-success"));
    }

    /**
     * 登录该账号
     *
     * @return 模板路径admin/admin_edit
     */
    @GetMapping("/login/proxy")
    public String loginIt(@RequestParam("userId") Long userId) {
        User user = userService.findByUserId(userId);
        Subject subject = SecurityUtils.getSubject();
        UserToken userToken = new UserToken(user.getUserName(), user.getUserPass(), LoginType.FREE.getDesc());
        try {
            subject.login(userToken);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("代理登录登录失败, cause:{}", e);
        }
        return "redirect:/admin";
    }


    /**
     * 管理员修改用户资料
     *
     * @param user user
     * @return JsonResult
     */
    @PostMapping(value = "/profile/save/proxy")
    @ResponseBody
    public JsonResult adminSaveProfile(@Valid @ModelAttribute User user,
                                       BindingResult result) {
        try {
            if (result.hasErrors()) {
                for (ObjectError error : result.getAllErrors()) {
                    return new JsonResult(ResultCodeEnum.FAIL.getCode(), error.getDefaultMessage());
                }
            }

            userService.saveByUser(user);

        } catch (Exception e) {
            log.error("修改用户资料失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.edit-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.edit-success"));
    }

    /**
     * 管理员给用户修改密码
     *
     * @param newPass 新密码
     * @return JsonResult
     */
    @PostMapping(value = "/changePass/proxy")
    @ResponseBody
    public JsonResult adminChangePass(@ModelAttribute("userId") Long userId,
                                      @ModelAttribute("newPass") String newPass) {
        try {
            User user = userService.findByUserId(userId);
            if (null != user) {
                String password = Md5Util.toMd5(newPass, "sens", 10);
                userService.updatePassword(user.getUserId(), password);
            } else {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.user.old-password-error"));
            }
        } catch (Exception e) {
            log.error("修改密码失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.user.update-password-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.user.update-password-success"));
    }
}
